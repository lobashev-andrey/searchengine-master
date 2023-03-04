package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;

import java.util.List;

@Repository
public interface IndexRepository extends CrudRepository<IndexEntity, Integer> {

    @Query(value = "SELECT `lemma_id` FROM `indexes` WHERE page_id = :page_id", nativeQuery = true)
    List<Integer> lemmaIdsOfPage(int page_id);
}
