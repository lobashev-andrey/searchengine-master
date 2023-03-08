package searchengine.dto.search;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SearchResponseTrue implements SearchResponse{
    private boolean result;
    private int count;
    private List<SinglePageSearchData> data;

}
