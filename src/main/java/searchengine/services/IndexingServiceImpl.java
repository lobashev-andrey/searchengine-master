package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.ConnectionConfig;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.controllers.PageEntityController;
import searchengine.controllers.SiteEntityController;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.indexing.IndexingResponseFalse;
import searchengine.dto.indexing.IndexingResponseTrue;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService{
    private final SitesList sites;
    private final SiteEntityController siteEntityController;
    private final PageEntityController pageEntityController;
    private final ConnectionConfig userAgent;
    private final ConnectionConfig referer;
    private final Stopper stopper = new Stopper();


    @Override
    public IndexingResponse getIndexing() {
        if(siteEntityController.isIndexing()){
            return new IndexingResponseFalse("Индексация уже запущена");
        }

        stopper.setStop(false);
        System.out.println("getIndexing " + stopper.isStop());
        List<Site> sitesForIndexing = sites.getSites();

        for(Site s : sitesForIndexing){

            System.out.println("THREAD " + s.getUrl());
            new Thread(() -> {
                urlPlusSlash(s);
                cleanTablesForSite(s);
                SiteEntity newSite = createIndexingSiteEntity(s);
                newSitePagesAdder(newSite);
                statusChanger(newSite, stopper.isStop() ? Status.FAILED : Status.INDEXED);
            }).start();
        }


        return new IndexingResponseTrue();
    }

    public void urlPlusSlash(Site s){
        String cleanUrl = s.getUrl().trim();
        s.setUrl(cleanUrl + (cleanUrl.endsWith("/") ? "" : "/"));
    }


    public void cleanTablesForSite(Site s){
        SiteEntity oldSite = siteEntityController.getSiteEntity(s.getUrl());
        if(oldSite != null){
//            int count = pageEntityController.deletePageEntityBySiteId(oldSite.getId());
//            System.out.println("УДАЛЕНО " + count + s.getUrl());
            siteEntityController.deleteSiteEntity(oldSite);
        }
    }
    public SiteEntity createIndexingSiteEntity(Site s){
        SiteEntity newSite = new SiteEntity();
        newSite.setName(s.getName());
        newSite.setUrl(s.getUrl() + (s.getUrl().endsWith("/") ? "" : "/"));
        newSite.setStatus(Status.INDEXING);
        newSite.setStatus_time(new Date());
        siteEntityController.addSiteEntity(newSite);
        return newSite;
    }
    public void newSitePagesAdder(SiteEntity newSite) {
        List<String> result = new ArrayList<>();
        Set<String> total = new HashSet<>();
        result.add(newSite.getUrl());
        total.add(newSite.getUrl());

        System.out.println("ПЕРЕД result.addALL");
        System.out.println("STOPPER " + stopper.isStop());

        result.addAll(new ForkJoinPool().invoke(new RecursiveIndexer(newSite.getUrl(), newSite.getUrl(), total, stopper)));

        System.out.println("ПОСЛЕ result.addALL");
        System.out.println("STOPPER " + stopper.isStop());
        if(stopper.isStop()){
            System.out.println("55555555555555555555555555555555555555555555555555555555555555555555555");
            result = new ArrayList<>();
        }

        System.out.println("RESULT *************" + result.size());
        System.out.println("TOTAL **************" + total.size());
        for (String r : result) {
            long time = Math.round(100 + 50 * Math.random());
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pageAdder(r, newSite);
            siteEntityController.refreshSiteEntity(newSite.getId());


        }
    }
    public void pageAdder(String path, SiteEntity newSite){
        PageEntity newPage = new PageEntity();
        Connection.Response response = null;
        Document doc = null;
        int status_code = 800;
        try {
            response = Jsoup.connect(path)
                        .userAgent(userAgent.getUserAgent())
                        .referrer(referer.getReferer())
                        .execute();
        } catch (HttpStatusException ex) {
            status_code = ex.getStatusCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(response == null) return;
        status_code = response.statusCode();

        try {
            doc = response.parse();
        } catch (IOException e) {
            System.out.println("IOException");
        }
        if(doc == null) return;

        String contentType = response.contentType();
        String content = contentType;
        if(contentType != null && contentType.startsWith("text/html")){
            content = doc.html();
        }
        String rootPath = newSite.getUrl();
        path = path.substring(rootPath.length() - 1);

        newPage.setSiteEntity(newSite);
        newPage.setCode(status_code);
        newPage.setPath(path);
        newPage.setContent(content);
        pageEntityController.addPageEntity(newPage);
    }
    public void statusChanger(SiteEntity newSite, Status status) {
        siteEntityController.setStatus(newSite.getId(), status);
    }
    public void setLastError(SiteEntity newSite, String lastError){
        siteEntityController.setError(newSite.getId(),lastError);
    }

    @Override
    public IndexingResponse stopIndexing() {
        if(siteEntityController.isIndexing()){
            stopper.setStop(true);
            System.out.println("stopIndexing " + stopper.isStop());
            for(int id : siteEntityController.listOfIndexing()) {
                siteEntityController.setStatus(id, Status.FAILED);
                siteEntityController.setError(id, "Индексация остановлена пользователем");
            }
            return new IndexingResponseTrue();
        }
        return new IndexingResponseFalse("Индексация не запущена");
    }
}


