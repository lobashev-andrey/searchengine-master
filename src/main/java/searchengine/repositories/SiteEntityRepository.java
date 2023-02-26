package searchengine.repositories;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.SiteEntity;

@Repository
public interface SiteEntityRepository extends CrudRepository<SiteEntity, Integer> {

    @Query(value = "SELECT COUNT(*) from sites where `status` = 'INDEXING'" , nativeQuery = true)
    int countIndexing();



}

