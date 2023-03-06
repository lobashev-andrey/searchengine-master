package searchengine.services;

import searchengine.dto.indexing.IndexingResponse;

public interface SearchService {
    IndexingResponse getSearch(String query, String site, int offset, int limit);
}
