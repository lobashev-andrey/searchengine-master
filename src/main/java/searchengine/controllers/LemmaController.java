package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PostMapping("/lemmas")
    public int addLemma(LemmaEntity lemmaEntity){
        lemmaRepository.save(lemmaEntity);
        return lemmaEntity.getId();
    }

    @PutMapping("lemmas")
    public void increaseFrequency(int lemma_id){
        Optional<LemmaEntity> optional = lemmaRepository.findById(lemma_id);
        if(optional.isPresent()){
            LemmaEntity lem = optional.get();
            lem.setFrequency(lem.getFrequency() + 1);
            lemmaRepository.save(lem);
        }
    }

}
