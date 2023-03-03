package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

import java.util.List;
/**
 * Интерфейс для работы с БД, содержит базовые метод save(T), Optional<T> findById(ID id) и прочие
 * Возможно добавлять свои собственные запросы в формате HQL или SQL
 */
@Repository
public interface PageEntityRepository extends CrudRepository<PageEntity, Integer> {
    /**
     * @param wordPart часть слова
     * @param limit макс количество результатов
     * @return список подходящих слов
     *
     * <p>Для создания SQL запроса, необходимо указать nativeQuery = true</p>
     * <p>каждый параметр в SQL запросе можно вставить, используя запись :ИМЯ_ПЕРЕМEННОЙ
     * перед именем двоеточие, так hibernate поймет, что надо заменить на значение переменной</p>
     */
    @Query(value = "SELECT COUNT(*) from pages where `path` = :currentUrl", nativeQuery = true)
    int findByUrl(String currentUrl);

//    @Query(value = "SELECT * from pages where `site_id` = :site_id", nativeQuery = true)
//    List<PageEntity> findBySiteId(int site_id);

    @Query(value = "SELECT * from pages where `site_id` = :site_id AND `path` = :currentUrl", nativeQuery = true)
    PageEntity findBySiteIdAndPath(int site_id, String currentUrl);




}




