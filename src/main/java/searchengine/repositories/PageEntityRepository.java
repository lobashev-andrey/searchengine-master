package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

@Repository
public interface PageEntityRepository extends CrudRepository<PageEntity, Integer> {

    @Query(value = "SELECT COUNT(*) from pages where `path` = :currentUrl", nativeQuery = true)
    int findByUrl(String currentUrl);

    @Query(value = "SELECT COUNT(*) from pages where `site_id` = :site_id", nativeQuery = true)
    int countBySiteId(int site_id);

    @Query(value = "SELECT * from pages where `site_id` = :site_id AND `path` = :currentUrl", nativeQuery = true)
    PageEntity findBySiteIdAndPath(int site_id, String currentUrl);

    @Query(value = "SELECT id FROM pages WHERE site_id = :site_id", nativeQuery = true)
    Iterable<Integer> getPagesBySiteId(int site_id);




}




