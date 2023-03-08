package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.config.ConnectionConfig;
import searchengine.controllers.IndexController;
import searchengine.controllers.LemmaController;
import searchengine.controllers.PageEntityController;
import searchengine.controllers.SiteEntityController;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResponseTrue;
import searchengine.dto.search.SinglePageSearchData;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;

import javax.print.Doc;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{
    private final LemmaController lemmaController;
    private final PageEntityController pageEntityController;
    private final IndexController indexController;
    private final TextLemmasParser parser = new TextLemmasParser();
    private final int threshold = 70;


    @Override
    public SearchResponse getSearch(String query, String site, int offset, int limit) {
        List<String> order = getOrderedListOfRareLemmas(query);
        List<Integer> pagesToReduce = new ArrayList<>();
        Set<Integer> lemmaIdsForRank = new HashSet<>();

        int flag = 0;
        for(String lemmaName : order){
            List<Integer> currentList = new ArrayList<>();
            List<Integer> lemmaIds = lemmaController.getLemmaIdsByLemmaName(lemmaName);
            lemmaIdsForRank.addAll(lemmaIds); // Сюда собираем lemma_id's от всех лемм, чтобы потом считать их sumRank на страницах
            Integer[] lemmaIdsArray = lemmaIds.toArray(new Integer[0]); // НЕ ОБРАЩАТЬ ВНИМАНИЯ НА ВЫДЕЛЕНИЕ
            List<Integer> pageIds = indexController.getPageIdsByLemmaIds(lemmaIdsArray);  // Нашли страницы, где оно есть

            for(Integer p : pageIds){
                if(flag == 0){
                    pagesToReduce.add(p); // Первый раз наполняем оба, пересечение будет полным
                }
                currentList.add(p); // Теперь только меньший - и будем получать пересечение
            }
            pagesToReduce = intersectionList(pagesToReduce, currentList);
            flag++;
        }
        Map<Integer, Float> pageAndRank = getPagesAndRanks(lemmaIdsForRank, pagesToReduce);
        return responseManager(pageAndRank, order);
    }
    public Map<String, Integer> deleteTooCommonLemmas(Map<String, Integer> map){
        Integer minValue = map.values().stream().min(Comparator.naturalOrder()).orElse(0);
        int noMoreThan = Math.max(
                pageEntityController.countAll() * threshold / 100,
                minValue);
        List<String> toRemove = map.keySet().stream()
                                .filter(a->map.get(a) > noMoreThan)
                                .collect(Collectors.toList());
        toRemove.forEach(map::remove);
        return map;
    }
    public List<Integer> intersectionList(List<Integer> a, List<Integer> b){
        List<Integer> list = new ArrayList<>();
        for (Integer t : a) {
            if(b.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }
    public List<String> getOrderedListOfRareLemmas(String query){
        List<String> lemmas;
        try {
            lemmas = new ArrayList<>(parser.lemmasCounter(query).keySet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<String, Integer> lemmaToAmountOfPages = new HashMap<>();
        for(String lemma : lemmas){
            Integer numPages = lemmaController.getSumFrequency(lemma);
            if(numPages != null){
                lemmaToAmountOfPages.put(lemma, numPages);
            }
        }
        lemmaToAmountOfPages = deleteTooCommonLemmas(lemmaToAmountOfPages); // Убираем из HashMap слишком частые леммы
        return lemmaToAmountOfPages.keySet().stream()  //  - ТОЛЬКО РЕДКИЕ ЛЕММЫ
                .sorted(Comparator.comparing(lemmaToAmountOfPages::get)).collect(Collectors.toList());
    }
    public Map<Integer, Float> getPagesAndRanks(Set<Integer> lemmaIdsForRank, List<Integer> pagesToReduce){
        Integer[] lemmaIdsForRankArray = lemmaIdsForRank.toArray(new Integer[0]); // НЕ ОБРАЩАТЬ ВНИМАНИЯ
        HashMap<Integer, Float> pageAndRank = new HashMap<>();
        float maxRank = 0f;
        for(Integer p : pagesToReduce){
            Float absRank = indexController.sumRankByPageId(p, lemmaIdsForRankArray);
            maxRank = Math.max(maxRank, absRank);
        }
        for(Integer p : pagesToReduce){
            Float absRank = indexController.sumRankByPageId(p, lemmaIdsForRankArray);
            Float relRank = absRank / maxRank;
            pageAndRank.put(p, relRank);
        }
        return pageAndRank;
    }

    public SearchResponse responseManager(Map<Integer, Float> pageAndRank, List<String> order){
        SearchResponseTrue searchResponse = new SearchResponseTrue();
        searchResponse.setResult(true);
        searchResponse.setCount(pageAndRank.size());
        List<SinglePageSearchData> totalData = new ArrayList<>();

        List<Integer> finalOrderOfPages = pageAndRank.keySet().stream()
                .sorted(Comparator.comparing(pageAndRank::get)
                        .reversed()).collect(Collectors.toList());

        for(Integer f : finalOrderOfPages){
            System.out.println("Страница " + f + " и ее relRank: " + pageAndRank.get(f));
            SinglePageSearchData pageData = new SinglePageSearchData();
            PageEntity currentPage = pageEntityController.getPageEntityById(f);
            SiteEntity currentSite = currentPage.getSiteEntity();
            String baseUrl = currentSite.getUrl().substring(0, currentSite.getUrl().length() - 1);
            pageData.setSite(baseUrl); // УБРАТЬ слэш
            pageData.setSiteName(currentSite.getName());
            pageData.setUri(currentPage.getPath());


            Document doc = null;
            try {
                doc = Jsoup.connect(baseUrl + currentPage.getPath()).get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Elements elements = doc.select("title");
            String title = elements.text();
            pageData.setTitle(title);
            pageData.setRelevance(pageAndRank.get(f));
            //     snippet
            pageData.setSnippet(snippetMaker(order, doc));
            System.out.println(
                    snippetMaker(order, doc) //////////////////////////////////
            );


            totalData.add(pageData);
        }
        searchResponse.setData(totalData);

        return searchResponse;
    }
    public String snippetMaker(List<String> order, Document doc){

        String pageText = getTextOnlyFromHtmlText(doc.html()); //ИЛИ
        try {
            return parser.getFragmentWithAllLemmas(pageText, order);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getTextOnlyFromHtmlText(String htmlText){
        Document doc = Jsoup.parse( htmlText );
        doc.outputSettings().charset("UTF-8");
        htmlText = Jsoup.clean( doc.body().html(), Safelist.simpleText());
        return htmlText;
    }

}
