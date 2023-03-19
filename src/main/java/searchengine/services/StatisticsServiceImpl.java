package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.controllers.LemmaController;
import searchengine.controllers.PageEntityController;
import searchengine.controllers.SiteEntityController;
import searchengine.dto.statistics.*;
import searchengine.model.SiteEntity;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final SiteEntityController siteEntityController;
    private final PageEntityController pageEntityController;
    private final LemmaController lemmaController;

    @Override
    public synchronized StatisticsResponse getStatistics() {
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

        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);

        return new StatisticsResponse(data);
    }
}


