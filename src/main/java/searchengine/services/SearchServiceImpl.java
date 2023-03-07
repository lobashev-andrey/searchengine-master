package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.controllers.IndexController;
import searchengine.controllers.LemmaController;
import searchengine.controllers.PageEntityController;
import searchengine.dto.indexing.IndexingResponse;

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
    public IndexingResponse getSearch(String query, String site, int offset, int limit) {
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
        List<Integer> finalOrderOfPages = pageAndRank.keySet().stream()
                .sorted(Comparator.comparing(pageAndRank::get)
                        .reversed()).collect(Collectors.toList());
        for(Integer f : finalOrderOfPages){
            System.out.println("Страница " + f + " и ее relRank: " + pageAndRank.get(f));
        }


        return null;
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

}
