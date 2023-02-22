package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import searchengine.model.SiteEntity;
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
    @PatchMapping("/sites")
    public void refreshSiteEntity(int site_id){
        siteEntityRepository.findById(site_id).get().setStatus_time(new Date());
    }
}
