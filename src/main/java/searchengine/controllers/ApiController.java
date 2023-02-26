package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.SiteEntity;
import searchengine.repositories.SiteEntityRepository;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {



    private final StatisticsService statisticsService;
    private final IndexingService indexingService;    ////  ?????????????
    public ApiController(StatisticsService statisticsService
            , IndexingService indexingService        ////  ?????????????
    ) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;       ////  ??????????
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing(){
        return ResponseEntity.ok(indexingService.getIndexing());
    }

}
