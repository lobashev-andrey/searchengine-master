package searchengine.repositories;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;

@Repository
public interface SiteEntityRepository extends CrudRepository<SiteEntity, Integer> {



}

