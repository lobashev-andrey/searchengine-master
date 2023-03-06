package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import searchengine.model.IndexEntity;
import searchengine.repositories.IndexRepository;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    IndexRepository indexRepository;

    @PostMapping("/index")
    public int addIndex(IndexEntity indexEntity){
        indexRepository.save(indexEntity);
        return indexEntity.getId();
    }

    @GetMapping("/index/{id}")
    public List<Integer> getLemmaIdsByPageId(@PathVariable int id){
        return indexRepository.lemmaIdsOfPage(id);
    }

//    @GetMapping("/index/{lemma}")
//    public List<Integer> getPageIdsByLemmaIds(List<Integer> lemmaIds){
//        return indexRepository.getPageIdsByLemmaIds(lemmaIds);
//    }


}
