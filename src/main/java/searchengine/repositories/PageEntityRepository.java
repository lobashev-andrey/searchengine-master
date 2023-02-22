package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

import java.util.List;

@Repository
public interface PageEntityRepository extends CrudRepository<PageEntity, Integer> {

    @Query(value = "SELECT COUNT(*) from pages where url = currentUrl", nativeQuery = true)
    int findByUrl(String currentUrl);

    @Query(value = "SELECT * from pages where `site_id` = :site_id", nativeQuery = true)
    List<PageEntity> findBySiteId(int site_id);




}




