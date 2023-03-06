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
    private final TextLemmasParser parser = new TextLemmasParser();
    private final LemmaController lemmaController;
    private final PageEntityController pageEntityController;
    private final IndexController indexController;
    private final int threshold = 70;




    @Override
    public IndexingResponse getSearch(String query, String site, int offset, int limit) {
        // Разбиваем на список (количество просто отбрасываем)
        List<String> lemmas = new ArrayList<>();
        try {
            lemmas.addAll(parser.lemmasCounter(query).keySet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Сортировка по встречаемости: lemma - sum(frequency)
        Map<String, Integer> lemmaToAmountOfPages = new HashMap<>();
        for(String lemma : lemmas){
            System.out.println(lemma);
            System.out.println(lemmaController.getSumFrequency(lemma));
            Integer numPages = lemmaController.getSumFrequency(lemma);
            if(numPages != null){
                lemmaToAmountOfPages.put(lemma, numPages);
            }
        }
        deleteTooCommonLemmas(lemmaToAmountOfPages);
        List<String> order = lemmaToAmountOfPages.keySet().stream().sorted(Comparator.comparing(lemmaToAmountOfPages::get)).collect(Collectors.toList());
        System.out.println("order:"); //////////////////////////
        order.forEach(a-> System.out.println(a + " " + lemmaToAmountOfPages.get(a))); ///////////////// печатаем order


        // Отсортировали
        // Находим страницы, где есть первое слово
//        List<Integer> pagesToReduce = new ArrayList<>();
//        int flag = 0;
//        for(String lemmaName : order){
//            List<Integer> currentList = new ArrayList<>();
//            List<Integer> lemmaIds = lemmaController.getLemmaIdsByLemmaName(lemmaName); // Нашли Ids этого слова
//            System.out.println(lemmaName);
//            for(Integer i : lemmaIds) {
//                System.out.println(i);
//            }
//            List<Integer> pageIds = indexController.getPageIdsByLemmaIds(lemmaIds);  // Нашли страницы, где оно есть
//
//            for(Integer i : pageIds){
//                if(flag == 0){
//                    pagesToReduce.add(i); // Первый раз наполняем оба, пересечение будет полным
//                    currentList.add(i);
//                } else {
//                    currentList.add(i); // Теперь только меньший - и будем получать пересечение
//                }
//            }
//            pagesToReduce = intersectionList(pagesToReduce, currentList);
//            flag++;
//            System.out.println("РАЗМЕР СПИСКА: " + pagesToReduce.size());
//            pagesToReduce.forEach(System.out::print);
//            System.out.println();
//        }
        // Теперь мы имеем уменьшенный список - в нем только те страницы, где есть все леммы (до порога)
        // Берем страницу (id) и складываем rank всех лемм из order - это будет абсолютная релевантность





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
