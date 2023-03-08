package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import searchengine.model.PageEntity;
import searchengine.repositories.PageEntityRepository;

import java.util.List;
import java.util.Optional;

@Controller
public class PageEntityController {
    @Autowired
    PageEntityRepository pageEntityRepository;

    @PostMapping("/pages")
    public int addPageEntity(PageEntity pageEntity){
        pageEntityRepository.save(pageEntity);
        return pageEntity.getId();
    }

    @GetMapping("/pages")
    public PageEntity findBySiteIdAndPath(int site_id, String url){
        return pageEntityRepository.findBySiteIdAndPath(site_id, url);
    }

    @GetMapping("/pages/countAll")
    public int countAll(){
        return (int) pageEntityRepository.count();
    }

    @GetMapping("/pages/{site_id}")
    public int countBySiteId(int site_id){
        return pageEntityRepository.countBySiteId(site_id);
    }

    @GetMapping("/pages/{id}")
    public PageEntity getPageEntityById(int id){
        Optional<PageEntity> optional = pageEntityRepository.findById(id);
        return optional.orElse(null);
    }

    @DeleteMapping("/pages")
    public void deletePageBySiteIdAndPath(int site_id, String url){
        PageEntity pageEntity = pageEntityRepository.findBySiteIdAndPath(site_id, url);
        pageEntityRepository.delete(pageEntity);
    }
}

//@Repository
//public interface WordRepository extends JpaRepository<WordEntity, Long> {
//
//    /**
//     * @param wordPart часть слова
//     * @param limit макс количество результатов
//     * @return список подходящих слов
//     *
//     * <p>Для создания SQL запроса, необходимо указать nativeQuery = true</p>
//     * <p>каждый параметр в SQL запросе можно вставить, используя запись :ИМЯ_ПЕРЕМEННОЙ
//     * перед именем двоеточие, так hibernate поймет, что надо заменить на значение переменной</p>
//     */
//    @Query(value = "SELECT * from words where word LIKE %:wordPart% LIMIT :limit", nativeQuery = true)
//    List<WordEntity> findAllContains(String wordPart, int limit);