package searchengine.services;

import lombok.AllArgsConstructor;
import lombok.Getter;
import searchengine.config.ConnectionConfig;

import java.util.Set;

@Getter
@AllArgsConstructor
public class RecursiveIndexerParams {
    private String address;
    private String baseUrl;
    private Set<String> total;
    private Stopper stopper;
    private ConnectionConfig connectionConfig;
}
