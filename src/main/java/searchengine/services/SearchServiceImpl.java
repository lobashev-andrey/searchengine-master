package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.controllers.LemmaController;
import searchengine.controllers.PageEntityController;
import searchengine.dto.indexing.IndexingResponse;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService{
    private TextLemmasParser parser;
    private LemmaController lemmaController;
    private PageEntityController pageEntityController;
    private final int threshold = 70;

    @Override
    public IndexingResponse getSearch(String query) {
        // Разбиваем на список
        List<String> lemmas = new ArrayList<>();
        try {
            lemmas.addAll(parser.lemmasCounter(query).keySet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // Сортировка по встречаемости: lemma - sum(frequency)
        Map<String, Integer> lemmaToPagesAmount = new HashMap<>();
        for(String lemma : lemmas){
            int numPages = lemmaController.getSumFrequency(lemma);
            lemmaToPagesAmount.put(lemma, numPages);
        }
        deleteTooCommonLemmas(lemmaToPagesAmount);
        List<String> order = lemmaToPagesAmount.keySet().stream().sorted(Comparator.comparing(lemmaToPagesAmount::get)).collect(Collectors.toList());
        // Отсортировали










        return null;
    }
    public Map<String, Integer> deleteTooCommonLemmas(Map<String, Integer> map){
        int noMoreThan = pageEntityController.countAll() * threshold / 100;
        for(String key : map.keySet()){
            if(map.get(key) > noMoreThan){
                map.remove(key);
            }
        }
        return map;
    }

}
