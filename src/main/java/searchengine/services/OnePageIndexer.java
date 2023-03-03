//package searchengine.services;
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import searchengine.config.ConnectionConfig;
//import searchengine.config.Site;
//import searchengine.config.SitesList;
//import searchengine.controllers.PageEntityController;
//import searchengine.controllers.SiteEntityController;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class OnePageIndexer {
//    private final SitesList sites;
//    private final SiteEntityController siteEntityController;
//    private final PageEntityController pageEntityController;
//    private final ConnectionConfig userAgent;
//    private final ConnectionConfig referer;
//
//    public void getOnePageIndex(String url){
//        List<Site> sitesForIndexing = sites.getSites();
//        for(Site s : sitesForIndexing){
//
//        }
//    }
//}
