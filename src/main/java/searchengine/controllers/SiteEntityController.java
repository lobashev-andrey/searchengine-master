package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.SiteEntityRepository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
public class SiteEntityController {
    @Autowired
    private SiteEntityRepository siteEntityRepository;

    @PostMapping("/sites")
    public void addSiteEntity(SiteEntity siteEntity){
        siteEntityRepository.save(siteEntity);
    }
    @DeleteMapping("/sites")
    public void deleteSiteEntity(SiteEntity siteEntity){
        siteEntityRepository.delete(siteEntity);
    }
    @GetMapping("/sites/{url}")
    public SiteEntity getSiteEntityByUrl(@PathVariable String url){
        Iterable<SiteEntity> allSites = siteEntityRepository.findAll();
        for(SiteEntity se : allSites){
            if(se.getUrl().equals(url)){
                return se;
            }
        }
        return null;
    }

    @GetMapping("/sites")
    public List<SiteEntity> list(){
        Iterable<SiteEntity> iterable = siteEntityRepository.findAll();
        List<SiteEntity> siteEntities = new ArrayList<>();
        iterable.forEach(siteEntities::add);
        return siteEntities;
    }

    @GetMapping("/sites/{id}")
    public SiteEntity getSiteEntityById(int id){
        Optional<SiteEntity> optional = siteEntityRepository.findById(id);
        return optional.orElse(null);
    }

    @GetMapping("/sites/isIndexing")
    public boolean isIndexing(){
        return siteEntityRepository.countIndexing() > 0;
    }

    @GetMapping("/sites/whichAreIndexing")
    public int[] listOfIndexing(){
        return siteEntityRepository.listOfIndexing();
    }

    @PutMapping("/sites")
    public void refreshSiteEntity(int id){
        Optional<SiteEntity> optional = siteEntityRepository.findById(id);
        if(optional.isPresent()) {
            SiteEntity sE = optional.get();
            sE.setStatus_time(new Date());
            siteEntityRepository.save(sE);
        }
    }
    @PutMapping("/sites/{id}")
    public void setStatus(@PathVariable int id, Status status ){
        Optional<SiteEntity> optional = siteEntityRepository.findById(id);
        if(optional.isPresent()){
            SiteEntity sE = optional.get();
            sE.setStatus(status);
            sE.setStatus_time(new Date());
            siteEntityRepository.save(sE);
        }
    }
    @PutMapping("/sites/{id}/{lastError}")
    public void setError(@PathVariable int id, @PathVariable String lastError ) {
        Optional<SiteEntity> optional = siteEntityRepository.findById(id);
        if (optional.isPresent()) {
            SiteEntity sE = optional.get();
            sE.setLast_error(lastError);
            siteEntityRepository.save(sE);
        }
    }
}
