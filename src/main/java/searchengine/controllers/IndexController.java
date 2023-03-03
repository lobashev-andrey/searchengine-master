package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import searchengine.model.IndexEntity;
import searchengine.repositories.IndexRepository;

@Controller
public class IndexController {

    @Autowired
    IndexRepository indexRepository;

    @PostMapping("/index")
    public int addIndex(IndexEntity indexEntity){
        indexRepository.save(indexEntity);
        return indexEntity.getId();
    }



}
