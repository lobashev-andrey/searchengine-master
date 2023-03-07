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
        // Разбиваем запрос на список (количество просто отбрасываем)
        List<String> lemmas = new ArrayList<>();
        try {
            lemmas.addAll(parser.lemmasCounter(query).keySet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Сортировка по встречаемости: lemma - sum(frequency)
        Map<String, Integer> lemmaToAmountOfPages = new HashMap<>();
        for(String lemma : lemmas){
//            System.out.print("Лемма " + lemma); System.out.println(" Встречаемость: " + lemmaController.getSumFrequency(lemma)); ////////
            Integer numPages = lemmaController.getSumFrequency(lemma);
            if(numPages != null){
                lemmaToAmountOfPages.put(lemma, numPages);
            }
        }
        deleteTooCommonLemmas(lemmaToAmountOfPages); // Убираем из HashMap слишком частые леммы
        List<String> order = lemmaToAmountOfPages.keySet().stream()  //  - ТОЛЬКО РЕДКИЕ ЛЕММЫ
                .sorted(Comparator.comparing(lemmaToAmountOfPages::get)).collect(Collectors.toList());
//        System.out.println("order:"); //////////////////////////
//        order.forEach(a-> System.out.println(a + " " + lemmaToAmountOfPages.get(a))); ///////////////// печатаем order

        // Отсортировали
        // Находим страницы, где есть эти РЕДКИЕ ЛЕММЫ
        List<Integer> pagesToReduce = new ArrayList<>();
        Set<Integer> lemmaIdsForRank = new HashSet<>();
        int flag = 0;
        for(String lemmaName : order){
            List<Integer> currentList = new ArrayList<>();
            List<Integer> lemmaIds = lemmaController.getLemmaIdsByLemmaName(lemmaName);
            lemmaIdsForRank.addAll(lemmaIds); // Сюда собираем lemma_id's от всех лемм, чтобы потом считать их sumRank на страницах
            Integer[] lemmaIdsArray = new Integer[lemmaIds.size()];
            for(int i = 0; i < lemmaIds.size(); i++){
                lemmaIdsArray[i] = lemmaIds.get(i);
            }
            List<Integer> pageIds = indexController.getPageIdsByLemmaIds(lemmaIdsArray);  // Нашли страницы, где оно есть
            for(Integer p : pageIds){
                if(flag == 0){
                    pagesToReduce.add(p); // Первый раз наполняем оба, пересечение будет полным
                    currentList.add(p);
                } else {
                    currentList.add(p); // Теперь только меньший - и будем получать пересечение
                }
            }
            pagesToReduce = intersectionList(pagesToReduce, currentList);
            flag++;
//            System.out.println("РАЗМЕР СПИСКА: " + pagesToReduce.size());
//            pagesToReduce.forEach(System.out::println);
        }
        // Теперь в pagesToReduce мы имеем УМЕНЬШЕННЫЙ СПИСОК - в нем только те страницы, где есть все леммы (до порога)
//        for(Integer i : pagesToReduce){
//            System.out.println("page_id    , где есть все слова: "    + i);
//        }
        // Берем страницу (id) и складываем rank всех лемм из order - это будет абсолютная релевантность

        Integer[] lemmaIdsForRankArray = new Integer[lemmaIdsForRank.size()];
        int i = 0;
        for(Integer l : lemmaIdsForRank){
            lemmaIdsForRankArray[i++] = l;
        }

        HashMap<Integer, Float> pageAndRank = new HashMap<>();
        float maxRank = 0f;
        for(Integer p : pagesToReduce){
            Float absRank = indexController.sumRankByPageId(p, lemmaIdsForRankArray);
            maxRank = Math.max(maxRank, absRank);
//            System.out.println("Страница " + p + " absRank: " + absRank + " maxRank: " + maxRank);
        }
        for(Integer p : pagesToReduce){
            Float absRank = indexController.sumRankByPageId(p, lemmaIdsForRankArray);
            Float relRank = absRank / maxRank;
            pageAndRank.put(p, relRank);
            System.out.println("Страница " + p + " relRank: " + relRank);
        }
        List<Integer> finalOrderOfPages = pageAndRank.keySet().stream()
                .sorted(Comparator.comparing(pageAndRank::get)
                        .reversed()).collect(Collectors.toList());
        for(Integer f : finalOrderOfPages){
            System.out.println("Страница " + f + " и ее relRank: " + pageAndRank.get(f));





        }







        return null;
    }
    public Map<String, Integer> deleteTooCommonLemmas(Map<String, Integer> map){
        int noMoreThan = pageEntityController.countAll() * threshold / 100 + 1; // Может, + min(numOfPages)?
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

}
