package searchengine.dto.statistics;

import lombok.Data;

@Data
public class StatisticsResponse {
    private boolean result = true;
    private StatisticsData statistics;

    public StatisticsResponse(StatisticsData data){
        this.statistics = data;
    }
}
