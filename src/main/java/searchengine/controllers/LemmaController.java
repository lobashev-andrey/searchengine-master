package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searchengine.model.LemmaEntity;
import searchengine.repositories.LemmaRepository;

import java.util.*;

@Controller
public class LemmaController {

    @Autowired
    LemmaRepository lemmaRepository;

    @GetMapping("/lemmas")
    public Integer getLemmaId(int site_id, String lemma){
        Optional<Integer> optional = lemmaRepository.getLemmaId(site_id, lemma);
        return optional.orElse(null);
    }

    @GetMapping("/lemmas/{id}")
    public int countLemmaBySiteId(@PathVariable int id){
        return lemmaRepository.countLemmaBySiteId(id);
    }

    @GetMapping("/lemmas/{lemma}")
    public Integer getSumFrequency(@PathVariable String lemma){
        Optional<Integer> optional = lemmaRepository.getSumFrequency(lemma);
        return optional.orElse(null);
    }

    @GetMapping("/lemmas/{lemmaName}")
    public List<Integer> getLemmaIdsByLemmaName(@PathVariable String lemmaName){
        List<Integer> list = new ArrayList<>();
        Iterable<Integer> iterable = lemmaRepository.getLemmaIdsByLemmaName(lemmaName);
        iterable.forEach(list::add);
        return list;
    }




    @PutMapping("/lemmas")
    public void increaseFrequency(int id){
        Optional<LemmaEntity> optional = lemmaRepository.findById(id);
        if(optional.isPresent()){
            LemmaEntity lem = optional.get();
            lem.setFrequency(lem.getFrequency() + 1);
            lemmaRepository.save(lem);
        }
    }
    @PostMapping("/lemmas/{id}")
    public void decreaseFrequency(@PathVariable int id){
        Optional<LemmaEntity> optional = lemmaRepository.findById(id);
        if(optional.isPresent()){
            LemmaEntity lem = optional.get();
            lem.setFrequency(lem.getFrequency() - 1);
            lemmaRepository.save(lem);
        }
    }

    @PostMapping("/lemmas")
    public int addLemma(LemmaEntity lemmaEntity){
        lemmaRepository.save(lemmaEntity);
        return lemmaEntity.getId();
    }

    @DeleteMapping("/lemmas")
    public void deleteBySiteId(int site_id){
        Iterable<Integer> iterable = lemmaRepository.getLemmaIdBySiteId(site_id);
        lemmaRepository.deleteAllById(iterable);
    }


}
