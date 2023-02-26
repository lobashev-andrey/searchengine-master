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
import searchengine.dto.indexing.IndexingError;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService{
    private final SitesList sites;
    private final SiteEntityController siteEntityController;
    private final PageEntityController pageEntityController;
    private  final ConnectionConfig userAgent;
    private  final ConnectionConfig referer;


    @Override
    public IndexingResponse getIndexing() {
        if(siteEntityController.isIndexing()){
            System.out.println("INDEXING already");
            return new IndexingResponse("Индексация уже запущена");
        }

        List<Site> sitesForIndexing = sites.getSites();
        for(Site s : sitesForIndexing){
            // удалять все имеющиеся данные по этому сайту (записи из таблиц site и page);
            cleanTablesForSite(s);
            // создавать в таблице site новую запись со статусом INDEXING;
            SiteEntity newSite = createIndexingSiteEntity(s);
            // обходить все страницы, начиная с главной, добавлять их адреса, статусы и содержимое в базу данных в таблицу page;
            newSitePagesAdder(newSite);
            // по завершении обхода изменять статус (поле status) на INDEXED;
            statusChanger(newSite, Status.INDEXED);
            // если произошла ошибка и обход завершить не удалось, изменять статус на FAILED и вносить в поле last_error понятную информацию о произошедшей ошибке.
            String lastError = "jkjkjkjkj";
            setLastError(newSite, lastError);
        }

        System.out.println("5555555555555555555555555555555" );
        return new IndexingResponse();
    }



    // удалять все имеющиеся данные по этому сайту (записи из таблиц site и page);
    public void cleanTablesForSite(Site s){
        SiteEntity oldSite = siteEntityController.getSiteEntity(s.getUrl());
        if(oldSite != null){
            pageEntityController.deletePageEntityBySiteId(oldSite.getId());
            siteEntityController.deleteSiteEntity(oldSite);
        }
    }
    // создавать в таблице site новую запись со статусом INDEXING;
    public SiteEntity createIndexingSiteEntity(Site s){
        SiteEntity newSite = new SiteEntity();
        newSite.setName(s.getName());
        newSite.setUrl(s.getUrl() + (s.getUrl().endsWith("/") ? "" : "/"));
        newSite.setStatus(Status.INDEXING);
        newSite.setStatus_time(new Date());
        siteEntityController.addSiteEntity(newSite);
        return newSite;
    }
    // обходить все страницы, начиная с главной, добавлять их адреса, статусы и содержимое в базу данных в таблицу page;
    public void newSitePagesAdder(SiteEntity newSite) {
        List<String> result = new ArrayList<>();
        result.add(newSite.getUrl());
        result.addAll(new ForkJoinPool().invoke(new RecursiveIndexer(newSite.getUrl())));

        for (String r : result) {
            long time = Math.round(100 + 50 * Math.random());   //Math.round(500 + 4500 * Math.random());
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pageAdder(r, newSite);
            // в процессе обхода постоянно обновлять дату и время в поле status_time таблицы site на текущее;
            siteEntityController.refreshSiteEntity(newSite.getId());
        }
    }
    public void pageAdder(String path, SiteEntity newSite){
        PageEntity newPage = new PageEntity();
        Connection.Response response = null;

        System.out.println("path ------------------------");
        System.out.println(path);

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

        System.out.println(status_code); // для sites last_error



        if(response == null) return;

        status_code = response.statusCode();
        System.out.println(status_code + " status_code");///////////////////////////////////////

        try {
            doc = response.parse();
        } catch (IOException e) {
            System.out.println("IOException");
        }
        if(doc == null) return;

        String contentType = response.contentType();

        System.out.println(contentType + " contentType");////////////////////////////////////
        String content = contentType;
        if(contentType != null && contentType.startsWith("text/html")){
            content = doc.html();
        }
        String rootPath = newSite.getUrl();
        path = path.substring(rootPath.length() - 1);

        System.out.println(path); ///////////////////////////////////////////

        newPage.setSiteEntity(newSite);
        newPage.setCode(status_code);
        newPage.setPath(path);
        newPage.setContent(content);
        pageEntityController.addPageEntity(newPage);
    }
    // по завершении обхода изменять статус (поле status) на INDEXED;
    public void statusChanger(SiteEntity newSite, Status status) {
        siteEntityController.setStatus(newSite.getId(), status);
    }
    public void setLastError(SiteEntity newSite, String lastError){
        siteEntityController.setError(newSite.getId(),lastError);
    }
}
