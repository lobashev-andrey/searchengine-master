package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searchengine.model.PageEntity;
import searchengine.repositories.PageEntityRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/pages")
public class PageEntityController {
    @Autowired
    PageEntityRepository pageEntityRepository;


    @PostMapping("/")
    public int addPageEntity(PageEntity pageEntity){
        pageEntityRepository.save(pageEntity);
        return pageEntity.getId();
    }

    @GetMapping("/")
    public PageEntity findBySiteIdAndPath(int site_id, String url){
        return pageEntityRepository.findBySiteIdAndPath(site_id, url);
    }

    @GetMapping("/{one_site_id}")
    public List<Integer>  getPagesBySiteId(@PathVariable int one_site_id){
        List<Integer> list = new ArrayList<>();
        Iterable<Integer> iterable = pageEntityRepository.getPagesBySiteId(one_site_id);
        iterable.forEach(list::add);
        return list;
    }

    @GetMapping("/countAll")
    public int countAll(){
        return (int) pageEntityRepository.count();
    }

    @GetMapping("/{site_id}")
    public int countBySiteId(int site_id){
        return pageEntityRepository.countBySiteId(site_id);
    }

    @GetMapping("/{id}")
    public PageEntity getPageEntityById(int id){
        Optional<PageEntity> optional = pageEntityRepository.findById(id);
        return optional.orElse(null);
    }

    @DeleteMapping("/")
    public void deletePageBySiteIdAndPath(int site_id, String url){
        PageEntity pageEntity = pageEntityRepository.findBySiteIdAndPath(site_id, url);
        pageEntityRepository.delete(pageEntity);
    }
}