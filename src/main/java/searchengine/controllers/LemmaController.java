package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searchengine.model.LemmaEntity;
import searchengine.repositories.LemmaRepository;

import java.util.Optional;

@Controller
public class LemmaController {

    @Autowired
    LemmaRepository lemmaRepository;

    @GetMapping("/lemmas")
    public Integer getLemmaId(int site_id, String lemma){
        Optional<Integer> optional = lemmaRepository.getLemmaId(site_id, lemma);
        return optional.orElse(null);
    }

//    @GetMapping("/lemmas/{id}")
//    public LemmaEntity getLemmaById(@PathVariable int id){
//        return lemmaRepository.findById(id).orElse(null);
//    }

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
