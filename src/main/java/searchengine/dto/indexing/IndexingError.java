package searchengine.dto.indexing;

import lombok.Getter;

@Getter
public class IndexingError {
    private final String error = "Индексация уже запущена";
}
