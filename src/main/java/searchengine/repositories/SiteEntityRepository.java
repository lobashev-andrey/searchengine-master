package searchengine.repositories;

import org.springframework.data.repository.CrudRepository;
import searchengine.model.SiteEntity;

public interface SiteEntityRepository extends CrudRepository<SiteEntity, Integer> {
}

