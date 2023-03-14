package searchengine.dto.search;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class SearchResponseTrue implements SearchResponse{
    private boolean result;
    private int count;
    private List<SinglePageSearchData> data;

    public SearchResponseTrue(){
        this.result = true;
    }

}
