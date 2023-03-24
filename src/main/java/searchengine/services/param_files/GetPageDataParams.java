package searchengine.services.param_files;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class GetPageDataParams {
    private int page_id;
    private Map<Integer, Float> pageAndRank;
    private List<String> lemmas;
    private String query;
}
