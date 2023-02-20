package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.model.SiteEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService{
    private SitesList sites;


    @Override
    public IndexingResponse getIndexing() {
        List<Site> sitesForIndexing = sites.getSites();
        for(Site s : sitesForIndexing){
            // удалять все имеющиеся данные по этому сайту (записи из таблиц site и page);


            // создавать в таблице site новую запись со статусом INDEXING;
            // обходить все страницы, начиная с главной, добавлять их адреса, статусы и содержимое в базу данных в таблицу page;
            // в процессе обхода постоянно обновлять дату и время в поле status_time таблицы site на текущее;
            // по завершении обхода изменять статус (поле status) на INDEXED;
            // если произошла ошибка и обход завершить не удалось, изменять статус на FAILED и вносить в поле last_error понятную информацию о произошедшей ошибке.


        }









        return null;
    }
}
