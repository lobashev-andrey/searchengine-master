package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import searchengine.model.PageEntity;
import searchengine.repositories.PageEntityRepository;

import java.util.List;

@Controller
public class PageEntityController {
    @Autowired
    PageEntityRepository pageEntityRepository;



    @PostMapping("/pages")
    public int addPageEntity(PageEntity pageEntity){
        pageEntityRepository.save(pageEntity);
        return pageEntity.getId();
    }

//    @GetMapping("/pages")
//    public boolean containsUrl(String url){
//        return pageEntityRepository.findByUrl(url) > 0;
//    }

    @GetMapping("/pages")
    public boolean containsSiteIdAndPath(int site_id, String url){
        return pageEntityRepository.findBySiteIdAndPath(site_id, url) != null;
    }

    @DeleteMapping("/pages")
    public void deletePageBySiteIdAndPath(int site_id, String url){
        PageEntity pageEntity = pageEntityRepository.findBySiteIdAndPath(site_id, url);
        pageEntityRepository.delete(pageEntity);
    }



//    @DeleteMapping("/pages")
//    public int deletePageEntityBySiteId(int site_id){
//        List<PageEntity> list = pageEntityRepository.findBySiteId(site_id);
//        int count = 0;
//        for(PageEntity pe : list){
//            pageEntityRepository.delete(pe);
//            count++;
//        }
//        return count;
//    }



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