package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.controllers.IndexController;
import searchengine.controllers.LemmaController;
import searchengine.controllers.PageEntityController;
import searchengine.controllers.SiteEntityController;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SearchResponseFalse;
import searchengine.dto.search.SearchResponseTrue;
import searchengine.dto.search.SinglePageSearchData;
import searchengine.exceptions.EmptyQueryException;
import searchengine.exceptions.NotIndexedException;
import searchengine.exceptions.WrongQueryFormatException;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{
    private final LemmaController lemmaController;
    private final PageEntityController pageEntityController;
    private final SiteEntityController siteEntityController;
    private final IndexController indexController;
    private final TextLemmasParser parser = new TextLemmasParser();
    private final int maxPercentOfPagesForLemma = 70;


    @Override
    public SearchResponse getSearch(String query, String site, int offset, int limit) {
        List<String> lemmas;
        List<String> order;
        Map<Integer, Float> pageAndRank;

        try{
            if(query.length() == 0){
                throw new EmptyQueryException("Задан пустой поисковый запрос");
            }
            lemmas = queryToLemmaList(query); // .replace("ё", "е").replace('Ё', 'Е')


            if(lemmas.size() == 0){
                throw new WrongQueryFormatException("В базе отсутствуют предложенные в запросе слова");
            }
            order = getOrderedListOfRareLemmas(lemmas);
            Integer[] sites_ids = getSitesWhereToSearch(site);
            pageAndRank = rankPages(order, sites_ids);

        } catch (Exception ex) {
            return new SearchResponseFalse(ex.getMessage());
        }
        return responseManager(pageAndRank, lemmas, limit, offset);
    }
    public List<String> getOrderedListOfRareLemmas(List<String> lemmas) throws WrongQueryFormatException {
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
    public Map<String, Integer> deleteTooCommonLemmas(Map<String, Integer> map) throws WrongQueryFormatException {
        int before = map.size();
        int noMoreThan = pageEntityController.countAll() * maxPercentOfPagesForLemma / 100;
        List<String> toRemove = map.keySet().stream()
                                .filter(a->map.get(a) > noMoreThan)
                                .collect(Collectors.toList());
        toRemove.forEach(map::remove);
        map.keySet().forEach(a->System.out.println(a + " " + map.get(a)));

        if(map.size() == 0 && before != 0){
            throw new WrongQueryFormatException("Данные слова есть на более чем " + maxPercentOfPagesForLemma +  "% проиндексированных страниц");
        }
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
    public List<String> queryToLemmaList(String query){
        List<String> lemmas;
        try {
            lemmas = new ArrayList<>(parser.lemmasCounter(query).keySet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lemmas;
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
    public SearchResponse responseManager(Map<Integer, Float> pageAndRank, List<String> lemmas, int limit, int offset){
        SearchResponseTrue searchResponse = new SearchResponseTrue();
        searchResponse.setCount(pageAndRank.size());
        List<SinglePageSearchData> totalData = new ArrayList<>();
        List<Integer> finalOrderOfPages = pageAndRank.keySet().stream()
                .sorted(Comparator.comparing(pageAndRank::get)
                .reversed()).collect(Collectors.toList());

        int count = 0;
        for(Integer f : finalOrderOfPages){
            if(count < offset) continue;
            count++;

            SinglePageSearchData pageData = getPageData(f, pageAndRank, lemmas);
            totalData.add(pageData);

            if((count + offset) % limit == 0){
                break;
            }
        }
        searchResponse.setData(totalData);
        return searchResponse;
    }
    public SinglePageSearchData getPageData(int f, Map<Integer, Float> pageAndRank, List<String> lemmas){
        SinglePageSearchData pageData = new SinglePageSearchData();
        PageEntity currentPage = pageEntityController.getPageEntityById(f);
        SiteEntity currentSite = currentPage.getSiteEntity();
        String baseUrl = currentSite.getUrl().substring(0, currentSite.getUrl().length() - 1);
        pageData.setSite(baseUrl);
        pageData.setSiteName(currentSite.getName());
        pageData.setUri(currentPage.getPath());

        String content = currentPage.getContent();
        Document doc = Jsoup.parse(content);
        Elements elements = doc.select("title");
        String title = elements.text();

        pageData.setTitle(title);
        pageData.setRelevance(pageAndRank.get(f));
        String snippet = snippetMaker(content, lemmas);
        pageData.setSnippet(snippet);
        return pageData;
    }
    public String snippetMaker(String content, List<String> lemmas){   //  (Document doc, List<String> lemmas)
        String pageText = getTextOnlyFromHtmlText(content);     //  (doc.html())
        String rawFragment = "";
        try {
            rawFragment =  parser.getFragmentWithAllLemmas(pageText, lemmas);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parser.boldTagAdder(rawFragment, lemmas);
    }
    public String getTextOnlyFromHtmlText(String htmlText){
        Document doc = Jsoup.parse( htmlText );
        doc.outputSettings().charset("UTF-8");
        htmlText = Jsoup.clean( doc.body().html(), Safelist.simpleText());
        return htmlText;
    }
    public Integer[] getSitesWhereToSearch(String site) throws NotIndexedException {
        // Это должны быть проиндексированные сайты, по которым надо искать
        List<SiteEntity> whereSearch = new ArrayList<>();
        if(!site.equals("All sites")) {
            whereSearch.add(siteEntityController.getSiteEntityByUrl(site));
        } else {
            whereSearch.addAll(siteEntityController.list());
        }
        whereSearch = whereSearch.stream().filter(a->a.getStatus().equals(Status.INDEXED)).collect(Collectors.toList());
        if(whereSearch.size() == 0){
            throw new NotIndexedException("Указанного сайта(ов) нет в списке проиндексированных");
        }
        return whereSearch.stream().map(SiteEntity::getId).toArray(Integer[]::new);
    }
    public Map<Integer, Float> rankPages(List<String> order, Integer[] sites_ids){
        List<Integer> reducingListOfPages = new ArrayList<>();
        Set<Integer> lemmaIdsForRank = new HashSet<>();
        int flag = 0;
        for(String lemmaName : order){
            List<Integer> pagesOfCurrentLemma = new ArrayList<>();

            List<Integer> lemmaIds = lemmaController.getLemmaIdsByLemmaName(lemmaName, sites_ids); // получили по имени и сайтам lemma_ids
            System.out.println("Lemma " + lemmaName);
            for(Integer i : sites_ids){
                System.out.println("site_id: " + i);////////////////////////////////
            }
            System.out.println(lemmaIds.size());


            lemmaIdsForRank.addAll(lemmaIds); // Сюда собираем lemma_id's от всех лемм, чтобы потом считать их sumRank на страницах
            Integer[] lemmaIdsArray = lemmaIds.toArray(new Integer[0]); // НЕ ОБРАЩАТЬ ВНИМАНИЯ НА ВЫДЕЛЕНИЕ

            List<Integer> pageIds = indexController.getPageIdsByLemmaIds(lemmaIdsArray); // Нашли, на каких страницах есть эта лемма

            for(Integer p : pageIds){
                pagesOfCurrentLemma.add(p);
                if(flag == 0) reducingListOfPages.add(p); // Первый раз наполняем оба, пересечение будет полным
            }
            reducingListOfPages = intersectionList(reducingListOfPages, pagesOfCurrentLemma);
            flag++;
        }
        return getPagesAndRanks(lemmaIdsForRank, reducingListOfPages);
    }
}
