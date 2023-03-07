package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.IndexEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface IndexRepository extends CrudRepository<IndexEntity, Integer> {

    @Query(value = "SELECT `lemma_id` FROM `indexes` WHERE page_id = :page_id", nativeQuery = true)
    List<Integer> lemmaIdsOfPage(int page_id);

    @Query(value = "SELECT page_id FROM `indexes` WHERE `lemma_id` IN :lemmaIds", nativeQuery = true)
    Iterable<Integer> getPageIdsByLemmaIds(Integer[] lemmaIds);

    @Query(value = "SELECT SUM(`rank`) FROM `indexes` WHERE page_id = :page_id AND lemma_id IN :array", nativeQuery = true)
    Optional<Float> sumRankByPageId(int page_id, Integer[] array);
}
