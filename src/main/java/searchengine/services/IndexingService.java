package searchengine.services;

import searchengine.dto.indexing.IndexingResponse;

public interface IndexingService {   // Реализации лучше ограничить этим модулем
    IndexingResponse getIndexing();
    IndexingResponse stopIndexing();
}
