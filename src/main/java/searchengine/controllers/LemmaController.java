package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searchengine.model.LemmaEntity;
import searchengine.repositories.LemmaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/lemmas")
public class LemmaController {

    @Autowired
    LemmaRepository lemmaRepository;

    @GetMapping("/{id}")
    public int countLemmaBySiteId(@PathVariable int id){
        return lemmaRepository.countLemmaBySiteId(id);
    }

    @GetMapping("/lemmas/{lemma}")
    public Integer getSumFrequency(@PathVariable String lemma){
        Optional<Integer> optional = lemmaRepository.getSumFrequency(lemma);
        return optional.orElse(null);
    }

    @GetMapping("/{lemmaName}")
    public List<Integer> getLemmaIdsByLemmaName(@PathVariable String lemmaName, Integer[] sites_ids){
        List<Integer> list = new ArrayList<>();
        Iterable<Integer> iterable = lemmaRepository.getLemmaIdsByLemmaName(lemmaName, sites_ids);
        iterable.forEach(list::add);
        return list;
    }


    @GetMapping("/{site_id}")
    public List<LemmaEntity> getLemmasBySiteIdAndLemmaNameAndIncreaseFrequency(int site_id, String[] lemmaNames){
        List<LemmaEntity> list = new ArrayList<>();
        Iterable<LemmaEntity> iterable = lemmaRepository.getLemmasBySiteIdAndLemmaName(site_id, lemmaNames);
        for(LemmaEntity le : iterable){
            le.setFrequency(le.getFrequency() + 1);
            list.add(le);
        }
        lemmaRepository.saveAll(list);
        return list;
    }

    @PostMapping("/saveAll")
    public void saveAll(List<LemmaEntity> list){
        lemmaRepository.saveAll(list);
    }

    @PostMapping("/{id}")
    public void decreaseFrequency(@PathVariable int id){
        Optional<LemmaEntity> optional = lemmaRepository.findById(id);
        if(optional.isPresent()){
            LemmaEntity lem = optional.get();
            lem.setFrequency(lem.getFrequency() - 1);
            lemmaRepository.save(lem);
        }
    }




    @PutMapping("/")
    public LemmaEntity increaseFrequency(int id){
        Optional<LemmaEntity> optional = lemmaRepository.findById(id);
        LemmaEntity lem = null;
        if(optional.isPresent()){
            lem = optional.get();
            lem.setFrequency(lem.getFrequency() + 1);
            lemmaRepository.save(lem);
        }
        return lem;
    }
    @PostMapping("/")
    public int addLemma(LemmaEntity lemmaEntity){
        lemmaRepository.save(lemmaEntity);
        return lemmaEntity.getId();
    }
    @DeleteMapping("/")
    public void deleteBySiteId(int site_id){
        Iterable<Integer> iterable = lemmaRepository.getLemmaIdBySiteId(site_id);
        lemmaRepository.deleteAllById(iterable);
    }

    @GetMapping("/")
    public Integer getLemmaId(int site_id, String lemma){
        Optional<Integer> optional = lemmaRepository.getLemmaId(site_id, lemma);
        return optional.orElse(null);
    }








}
