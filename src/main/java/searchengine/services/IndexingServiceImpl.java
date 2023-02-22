package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.controllers.PageEntityController;
import searchengine.controllers.SiteEntityController;
import searchengine.model.PageEntity;
import searchengine.model.SiteEntity;
import searchengine.model.Status;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService{
    private final SitesList sites;
    private final SiteEntityController siteEntityController;
    private final PageEntityController pageEntityController;


    @Override
    public void getIndexing() {
        List<Site> sitesForIndexing = sites.getSites();
        for(Site s : sitesForIndexing){
            // удалять все имеющиеся данные по этому сайту (записи из таблиц site и page);
            SiteEntity oldSite = siteEntityController.getSiteEntity(s.getUrl());
            if(oldSite != null){
                pageEntityController.deletePageEntityBySiteId(oldSite.getId());
                siteEntityController.deleteSiteEntity(oldSite);
            }
            // создавать в таблице site новую запись со статусом INDEXING;
            SiteEntity newSite = new SiteEntity();
            newSite.setName(s.getName());
            newSite.setUrl(s.getUrl());
            newSite.setStatus(Status.INDEXING);
            newSite.setStatus_time(new Date());
            siteEntityController.addSiteEntity(newSite);
            int site_id = newSite.getId();                //  site_id

            // обходить все страницы, начиная с главной, добавлять их адреса, статусы и содержимое в базу данных в таблицу page;
            String baseUrl = newSite.getUrl();
            Set<String> urlSet = new HashSet<>();
            List<String> result = new ForkJoinPool().invoke(new RecursiveIndexer(baseUrl, baseUrl, urlSet));
            for(String r : result){
                long time = Math.round(100 + 50 * Math.random());   //Math.round(500 + 4500 * Math.random());
                try {
                    Thread.sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pageAdder(r, newSite);
                // в процессе обхода постоянно обновлять дату и время в поле status_time таблицы site на текущее;
                siteEntityController.refreshSiteEntity(site_id);
            }




            // по завершении обхода изменять статус (поле status) на INDEXED;
            // если произошла ошибка и обход завершить не удалось, изменять статус на FAILED и вносить в поле last_error понятную информацию о произошедшей ошибке.
        }
    }

    public void pageAdder(String path, SiteEntity newSite){
        PageEntity newPage = new PageEntity();
        Connection.Response response = null;
        try {
            response = Jsoup.connect(path)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com").execute();
            int status_code = response.statusCode();
            Document doc = response.parse();

            String content = doc.select("body").text();
            newPage.setCode(status_code);
            newPage.setPath(path);
            newPage.setContent(content);
            newPage.setSiteEntity(newSite);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pageEntityController.addPageEntity(newPage);
    }
}
