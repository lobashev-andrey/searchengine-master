package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.controllers.LemmaController;
import searchengine.controllers.PageEntityController;
import searchengine.controllers.SiteEntityController;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final SiteEntityController siteEntityController;
    private final PageEntityController pageEntityController;
    private final LemmaController lemmaController;

    @Override
    public synchronized StatisticsResponse getStatistics() {
//        String[] statuses = { "INDEXED", "FAILED", "INDEXING" };
//        String[] errors = {
//                "Ошибка индексации: главная страница сайта не доступна",
//                "Ошибка индексации: сайт не доступен",
//                ""
//        };

        TotalStatistics total = new TotalStatistics();
        List<SiteEntity> entities = siteEntityController.list();
        total.setSites(entities.size());
        total.setIndexing(siteEntityController.isIndexing());
        List<DetailedStatisticsItem> detailed = new ArrayList<>();

        for(SiteEntity siteEntity : entities) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            int site_id = siteEntity.getId();
            int pages = pageEntityController.countBySiteId(site_id);
            int lemmas = lemmaController.countLemmaBySiteId(site_id);
            item.setName(siteEntity.getName());
            item.setUrl(siteEntity.getUrl());
            item.setPages(pages);
            item.setStatus(siteEntity.getStatus().toString());
            item.setError(siteEntity.getLast_error());
            item.setStatusTime(siteEntity.getStatus_time().getTime());
            item.setLemmas(lemmas);

            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(entities.size() > 0);
        return response;
    }


//    for(int i = 0; i < sitesList.size(); i++) {
//        Site site = sitesList.get(i);
//        DetailedStatisticsItem item = new DetailedStatisticsItem();
//        item.setName(site.getName());
//        item.setUrl(site.getUrl());
//        int pages = random.nextInt(1_000);
//        int lemmas = pages * random.nextInt(1_000);
//        item.setPages(pages);
//        item.setLemmas(lemmas);
//        item.setStatus(statuses[i % 3]);
//        item.setError(errors[i % 3]);
//        item.setStatusTime(System.currentTimeMillis() -
//                (random.nextInt(10_000)));
//        total.setPages(total.getPages() + pages);
//        total.setLemmas(total.getLemmas() + lemmas);
//        detailed.add(item);
//    }
}


