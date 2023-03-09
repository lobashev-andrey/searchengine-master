package searchengine.dto.search;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchResponseFalse implements SearchResponse{
    private boolean result = false;
    private String error;

    public SearchResponseFalse(String error) {
        this.result = false;
        this.error = error;
    }
}
