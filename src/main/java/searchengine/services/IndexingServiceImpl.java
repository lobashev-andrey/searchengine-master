package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.controllers.PageEntityController;
import searchengine.controllers.SiteEntityController;
import searchengine.model.SiteEntity;
import searchengine.model.Status;

import java.util.Date;
import java.util.List;

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

            // обходить все страницы, начиная с главной, добавлять их адреса, статусы и содержимое в базу данных в таблицу page;
//            RecursiveIndexer recursiveIndexer = new RecursiveIndexer(newSite, );
//            recursiveIndexer.compute();

            // в процессе обхода постоянно обновлять дату и время в поле status_time таблицы site на текущее;
            // по завершении обхода изменять статус (поле status) на INDEXED;
            // если произошла ошибка и обход завершить не удалось, изменять статус на FAILED и вносить в поле last_error понятную информацию о произошедшей ошибке.
        }
    }
}
