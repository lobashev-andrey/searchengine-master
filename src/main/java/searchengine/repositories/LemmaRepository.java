package searchengine.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;

import java.util.Optional;

@Repository
public interface LemmaRepository extends CrudRepository<LemmaEntity, Integer> {

    @Query(value = "SELECT id FROM lemmas WHERE `site_id` = :site_id AND `lemma` = :lemma", nativeQuery = true)
    Optional<Integer> getLemmaId(int site_id, String lemma);


}
