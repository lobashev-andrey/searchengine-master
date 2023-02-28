package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.SiteEntityRepository;

import java.util.Date;

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
    @GetMapping("/sites")
    public SiteEntity getSiteEntity(String url){
        Iterable<SiteEntity> allSites = siteEntityRepository.findAll();
        for(SiteEntity se : allSites){
            if(se.getUrl().equals(url)){
                return se;
            }
        }
        return null;
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
    public void refreshSiteEntity(int site_id){
        siteEntityRepository.findById(site_id).get().setStatus_time(new Date());
    }
    @PutMapping("/sites/{id}")
    public void setStatus(@PathVariable int id, Status status ){
        SiteEntity sE = siteEntityRepository.findById(id).get();
        sE.setStatus(status);


        System.out.println("Ставим статус " + status);
        siteEntityRepository.save(sE);
        System.out.println("Поставили статус " + status);

    }
    @PutMapping("/sites/{id}/{lastError}")
    public void setError(@PathVariable int id, @PathVariable String lastError){
        SiteEntity sE = siteEntityRepository.findById(id).get();
        sE.setLast_error(lastError);
        siteEntityRepository.save(sE);
    }





}
