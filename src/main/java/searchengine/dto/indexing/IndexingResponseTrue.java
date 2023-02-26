package searchengine.dto.indexing;

import lombok.Getter;

@Getter
public class IndexingResponseTrue implements IndexingResponse{
    private boolean result = true;
}
