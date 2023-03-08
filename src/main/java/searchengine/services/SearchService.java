package searchengine.services;

import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.search.SearchResponse;

public interface SearchService {
    SearchResponse getSearch(String query, String site, int offset, int limit);
}
