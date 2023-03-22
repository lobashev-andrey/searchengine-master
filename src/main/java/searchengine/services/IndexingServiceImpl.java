package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.config.ConnectionConfig;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.controllers.IndexController;
import searchengine.controllers.LemmaController;
import searchengine.controllers.PageEntityController;
import searchengine.controllers.SiteEntityController;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.indexing.IndexingResponseFalse;
import searchengine.dto.indexing.IndexingResponseTrue;
import searchengine.model.*;

import javax.transaction.UserTransaction;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService{
    private final SitesList sites;
    private final SiteEntityController siteEntityController;
    private final PageEntityController pageEntityController;
    private final LemmaController lemmaController;
    private final IndexController indexController;
    private final ConnectionConfig connectionConfig;
    private final Stopper stopper = new Stopper();


    public void sitesListIndexer(AtomicInteger numOfIndexingSites){
        List<Site> sitesForIndexing = sites.getSites();
        numOfIndexingSites.set(sitesForIndexing.size());
        for(Site s : sitesForIndexing){
            Thread thread = new Thread(() -> {

                urlPlusSlash(s);
                cleanTablesOfSite(s);
                SiteEntity newSite = createIndexingSiteEntity(s);
                boolean error = false;
                try{
                    newSitePagesAdder(newSite);
                } catch (Exception ex){
                    setLastError(newSite, ex.getMessage());
                    error = true;
                }
                statusChanger(newSite, (stopper.isStop() || error) ? Status.FAILED : Status.INDEXED);

                numOfIndexingSites.decrementAndGet();
            });
            thread.start();
        }
    }

    @Override
    public IndexingResponse getIndexing() {
        synchronized (siteEntityController){
            if(!stopper.isStop()){             //   siteEntityController.isIndexing() &&
                return new IndexingResponseFalse("Индексация уже запущена");
            }
            if(siteEntityController.isIndexing()){
                for(int i : siteEntityController.listOfIndexing()){
                    siteEntityController.setError(i, "Аварийное завершение программы");
                    siteEntityController.setStatus(i, Status.FAILED);
                }
            }
            stopper.setStop(false);
        }
        AtomicInteger numOfIndexingSites = new AtomicInteger();
        sitesListIndexer(numOfIndexingSites);
        while(numOfIndexingSites.intValue() != 0){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
        stopper.setStop(true);
        return new IndexingResponseTrue();
    }
    @Override
    public synchronized IndexingResponse stopIndexing() {
        if(siteEntityController.isIndexing()){
            stopper.setStop(true);
            System.out.println("stopIndexing " + stopper.isStop());/////////////////////////////
            for(int id : siteEntityController.listOfIndexing()) {
                siteEntityController.setStatus(id, Status.FAILED);
                siteEntityController.setError(id, "Индексация остановлена пользователем");
            }
            return new IndexingResponseTrue();
        }
        return new IndexingResponseFalse("Индексация не запущена");
    }
    @Override
    public IndexingResponse getOnePageIndexing(String url) {
        if(belongsToSite(url) == null){
            return new IndexingResponseFalse("Данная страница находится " +
                    "за пределами сайтов, указанных в конфигурационном файле");
        }
        Site s = belongsToSite(url);
        String base = s.getUrl();
        if(url.equals(base.substring(0, base.length() - 1))){
            url = base;
        }
        SiteEntity siteEntity = siteEntityController.getSiteEntityByUrl(base); // Проверяем, есть ли сайт в таблице, если нет - добавляем
        if(siteEntity == null){
            siteEntity = createIndexingSiteEntity(s);
        } else {
            siteEntity.setStatus(Status.INDEXING);
        }
        singlePageInformationDeleter(url, siteEntity);
        String message;
        try {
            pageAdder(url, url, siteEntity);
        } catch (IOException ex) {
            message = ex.getMessage().replace("Главная страница", "Страница");
            statusChanger(siteEntity, Status.FAILED);
            setLastError(siteEntity, "Неудача при индексации отдельной страницы (" + url + "): " + message);
            return new IndexingResponseFalse(message);
        }
        statusChanger(siteEntity, Status.INDEXED);
        siteEntityController.refreshSiteEntity(siteEntity.getId());
        return new IndexingResponseTrue();
    }
    public void urlPlusSlash(Site s){
        String rawUrl = s.getUrl().trim();
//        String cleanUrl = httpToHttpS(rawUrl);
//        s.setUrl(cleanUrl + (cleanUrl.endsWith("/") ? "" : "/"));
        s.setUrl(rawUrl + (rawUrl.endsWith("/") ? "" : "/"));

    }
    public String httpToHttpS(String url){
        return url.replace("http:", "https:");
    }
    public void cleanTablesOfSite(Site s){
        SiteEntity oldSite = siteEntityController.getSiteEntityByUrl(s.getUrl());
        int site_id;
        if(oldSite != null){
            // Узнали id сайта, теперь уберем упоминания из lemmas
//            lemmaController.deleteBySiteId(oldSite.getId()); ////////////  НАДО ДОБАВИТЬ @ONDELETE!!!!!!!!!!!
            // А потом и сам сайт - а он каскадом уберет страницы и индексы
            siteEntityController.deleteSiteEntity(oldSite);
        }
    }
    public SiteEntity createIndexingSiteEntity(Site s){
        SiteEntity newSite = new SiteEntity();
        newSite.setName(s.getName());
        newSite.setUrl(s.getUrl() + (s.getUrl().endsWith("/") ? "" : "/"));
        newSite.setStatus(Status.INDEXING);
        newSite.setStatus_time(new Date());
        siteEntityController.addSiteEntity(newSite);
        return newSite;
    }
    public void newSitePagesAdder(SiteEntity newSite) throws IOException {
        List<String> result = new ArrayList<>();
        Set<String> total = new HashSet<>();
        String baseUrl = newSite.getUrl();
        result.add(baseUrl);
        total.add(baseUrl);
        RecursiveIndexerParams params = new RecursiveIndexerParams(baseUrl, baseUrl, total, stopper, connectionConfig);
        result.addAll(new ForkJoinPool().invoke(new RecursiveIndexer(params)));

        for (String r : result) {

            if(stopper.isStop()){break;}
            pageAdder(r, baseUrl, newSite);
            siteEntityController.refreshSiteEntity(newSite.getId());
            long time = Math.round(100 + 50 * Math.random());
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    public Connection.Response newResponse(String url, String baseUrl) throws IOException {
        Connection.Response response = null;
        int status_code;
        try {
            response = Jsoup.connect(url)
                    .userAgent(connectionConfig.getUserAgent())
                    .referrer(connectionConfig.getReferer())
                    .execute();
        } catch (HttpStatusException hex) {
            status_code = hex.getStatusCode();
            String message = getMessageByStatusCode(status_code);
            if(url.equals(baseUrl)){
                throw new IOException(message);     //  на главной - бросаем исключение с месседжем
            }
        } catch (IOException ex){
            if(url.equals(baseUrl)){
                throw new IOException("Главная страница сайта недоступна");     //  на главной - бросаем исключение с месседжем
            }
            throw new IOException("Это не главная страница");                                 //  на остальных - просто прерываем текущую страницу
        }
        return response;
    }
    public void pageAdder(String url, String baseUrl, SiteEntity newSite) throws IOException {
        Connection.Response response;
        try{
            response = newResponse(url, baseUrl);
        } catch (IOException ex){
            String message = ex.getMessage();
            if(message.equals("Это не главная страница")) return;
            throw new IOException(message);
        }
        if(response == null) return;
        int status_code = response.statusCode();
        Document doc = null;
        try {
            doc = response.parse();
        } catch (Exception e) {
            System.out.println("IOException");
        }
        if(doc == null) return;

        String contentType = response.contentType();
        String content = contentType;
        if(contentType != null && contentType.startsWith("text/html")){
            content = doc.html();
        }
        String path = pathFromUrl(url, newSite);
        PageEntity newPage = new PageEntity(newSite, path, status_code, content);

        int page_id = pageEntityController.addPageEntity(newPage);
        System.out.println(page_id + " before");
        lemmasAndIndexesAdder(content, newPage, newSite);
        System.out.println(newPage.getId() + " after");
    }
    public void lemmasAndIndexesAdder(String content, PageEntity newPage, SiteEntity newSite) throws IOException {
        TextLemmasParser parser = new TextLemmasParser();
        String text = parser.htmlTagsRemover(content);  // Очистили от тэгов  .replace("ё", "е").replace('Ё', 'Е')
        HashMap<String, Integer> lemmas; // Список лемм и количеств на странице
        try {
            lemmas = parser.lemmasCounter(text);
        } catch (IOException e) {
            throw new IOException("Ошибка в lemmasAndIndexesAdder" + e.getMessage());
        }
        // Четвертый вариант с двумя мульти-инсертами и мульти-селектом
        List<LemmaEntity> lemmasToSaveAll = new ArrayList<>();
        List<IndexEntity> indexList = new ArrayList<>();
        // Получить все LemmaEntity по сайту и имени, повысить frequency и сохранить
        List<LemmaEntity> increasedLemmas = lemmaController.getLemmasBySiteIdAndLemmaNameAndIncreaseFrequency(newSite.getId(), lemmas.keySet().toArray(String[]::new));
        // Теперь берем оставшиеся слова
        Set<String> otherLemmaNames = new HashSet<>(lemmas.keySet());
        List<String> lemmasToRemove = increasedLemmas.stream().map(LemmaEntity::getLemma).collect(Collectors.toList());
        lemmasToRemove.forEach(otherLemmaNames::remove);
        // И создаем LemmaEntity's
        for (String lemma : otherLemmaNames) {
            LemmaEntity lemmaEntity = new LemmaEntity(newSite, lemma, 1);
            lemmasToSaveAll.add(lemmaEntity);
        }
        lemmaController.saveAll(lemmasToSaveAll); // Теперь их сохраняем
        increasedLemmas.addAll(lemmasToSaveAll);  // Складываем все в один лист
//        int count = 0;
//        for(LemmaEntity le : increasedLemmas){
//            if(le == null){
//                count++;
//            }
//        }
        // Собираем лист индексов и потом его сохраняем
        for(LemmaEntity le : increasedLemmas){
            indexList.add(new IndexEntity(newPage, le.getId(), lemmas.get(le.getLemma())));
        }
        indexController.saveAll(indexList);
    }
    public String getMessageByStatusCode(int status_code){
        String message;
        switch(status_code){
            case 200:
                message = "";
                break;
            case 400:
                message = "400 - Некорректный запрос";
                break;
            case 401:
                message = "401 - Для доступа к запрашиваемому ресурсу требуется аутентификация";
                break;
            case 403:
                message = "403 - Доступ к ресурсу ограничен";
                break;
            case 404:
                message = "404 - Страница не найдена";
                break;
            case 405:
                message = "405 - Указанный метод неприменим к данному ресурсу";
                break;
            case 500:
                message = "500 - Внутренняя ошибка сервера";
                break;
            default:
                message = status_code + " - Ошибка соединения";
                break;
        }
        return message;
    }
    public void statusChanger(SiteEntity newSite, Status status) {
        siteEntityController.setStatus(newSite.getId(), status);
    }
    public void setLastError(SiteEntity newSite, String lastError){
        siteEntityController.setError(newSite.getId(),lastError);
    }
    public Site belongsToSite(String url){
        List<Site> sitesForIndexing = sites.getSites();
        for(Site s : sitesForIndexing){
            urlPlusSlash(s);
            String base = s.getUrl();
            if(url.startsWith(base) || url.equals(base.substring(0, base.length() - 1))){
                return s;
            }
        }
        return null;
    }
    public void singlePageInformationDeleter(String url, SiteEntity newSite){
        String path = pathFromUrl(url, newSite);
        if(pageEntityController.findBySiteIdAndPath(newSite.getId(), path) != null){   // Есть такая страница(site_id & path) - удаляем
            decreaseFrequency(newSite.getId(), path);
            pageEntityController.deletePageBySiteIdAndPath(newSite.getId(), path);
        }
    }
    public String pathFromUrl(String url, SiteEntity newSite){
        return url.substring(newSite.getUrl().length() - 1);
    }
    public void decreaseFrequency(int site_id, String path){
        // Получаем в indexes все lemma_id для этого path
        PageEntity pageEntity = pageEntityController.findBySiteIdAndPath(site_id, path);
        List<Integer> lemmaIds = indexController.getLemmaIdsByPageId(pageEntity.getId());
        // в lemmas:   frequency--
        for(Integer id : lemmaIds){
            lemmaController.decreaseFrequency(id);
        }
    }

//    public void newSitePagesAdder(SiteEntity newSite) throws IOException {  // БОЛЕЕ БЫСТРЫЙ ВАРИАНТ
//        List<String> result = new ArrayList<>();                            // требует синхронизации блока вставки в lemmas в lemmasAndIndexesAdder
//        Set<String> total = new HashSet<>();
//        String baseUrl = newSite.getUrl();
//        int maxThreadsNumber = 4;
//        result.add(baseUrl);
//        total.add(baseUrl);
//        RecursiveIndexerParams params = new RecursiveIndexerParams(baseUrl, baseUrl, total, stopper, connectionConfig);
//        result.addAll(new ForkJoinPool().invoke(new RecursiveIndexer(params)));
//        AtomicInteger pagesInProcess = new AtomicInteger(0);
//        AtomicInteger threadsNumber = new AtomicInteger(0);
//
//        for (String r : result) { // Для каждой страницы:
//            if(stopper.isStop()){break;}
//
//            pagesInProcess.incrementAndGet(); // ********************
//            while (threadsNumber.intValue() > maxThreadsNumber){
//                try {
//                    Thread.sleep(500);
//                } catch (InterruptedException e) {
//                    System.out.println(e.getMessage());
//                }
//            }
//            threadsNumber.incrementAndGet();
//            if(r.equals(baseUrl)){
//                pageAdder(r, baseUrl, newSite);
//                siteEntityController.refreshSiteEntity(newSite.getId());
//                pagesInProcess.decrementAndGet();
//                threadsNumber.decrementAndGet();
//            } else{
//                long time = Math.round(100 + 50 * Math.random());  // Выдерживаем интервал
//                try {
//                    Thread.sleep(time);
//                } catch (InterruptedException e) {
//                    System.out.println(e.getMessage());
//                }
//                Thread thread = new Thread(()->{
//
//                    try {
//                        pageAdder(r, baseUrl, newSite);
//                        siteEntityController.refreshSiteEntity(newSite.getId());
//                    } catch (IOException e) {
//                        System.out.println("thread failed " + r); // ******************
//                    }
//                    pagesInProcess.decrementAndGet();   // *****************
//                    threadsNumber.decrementAndGet();
//                });
//                thread.start();
//            }
//        }
//        while (true){
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                System.out.println(e.getMessage());
//            }
//            if(pagesInProcess.intValue() == 0) return;
//        }
//    }
}


