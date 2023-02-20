package searchengine.services;

import searchengine.dto.statistics.StatisticsResponse;

public interface StatisticsService {   // Вроде как лучше ограничить видимость пакетом - без public
    StatisticsResponse getStatistics();
}
