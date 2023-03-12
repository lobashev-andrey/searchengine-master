package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
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

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService{
    private final SitesList sites;
    private final SiteEntityController siteEntityController;
    private final PageEntityController pageEntityController;
    private final LemmaController lemmaController;
    private final IndexController indexController;
    private final ConnectionConfig connectionConfig;
//    private final ConnectionConfig referer;
    private final Stopper stopper = new Stopper();



    @Override
    public IndexingResponse getIndexing() {
        synchronized (siteEntityController){
            if(siteEntityController.isIndexing() && !stopper.isStop()){
                return new IndexingResponseFalse("Индексация уже запущена");
            }
            stopper.setStop(false);
        }
        List<Site> sitesForIndexing = sites.getSites();
        for(Site s : sitesForIndexing){
            Thread thread = new Thread(() -> {
                urlPlusSlash(s);
                cleanTablesForSite(s);
                SiteEntity newSite = createIndexingSiteEntity(s);
                newSitePagesAdder(newSite);
                statusChanger(newSite, stopper.isStop() ? Status.FAILED : Status.INDEXED);
            });
            thread.start();
        }
        stopIndexing();
        return new IndexingResponseTrue();
    }

    public void urlPlusSlash(Site s){
        String rawUrl = s.getUrl().trim();
        String cleanUrl = httpToHttpS(rawUrl);
        s.setUrl(cleanUrl + (cleanUrl.endsWith("/") ? "" : "/"));
    }
    public String httpToHttpS(String url){
        return url.replace("http:", "https:");
    }
    public void cleanTablesForSite(Site s){
        SiteEntity oldSite = siteEntityController.getSiteEntityByUrl(s.getUrl());
        int site_id;
        if(oldSite != null){
            // Узнали id сайта, теперь уберем упоминания из lemmas
            lemmaController.deleteBySiteId(oldSite.getId());
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
    public void newSitePagesAdder(SiteEntity newSite) {
        List<String> result = new ArrayList<>();
        Set<String> total = new HashSet<>();
        String baseUrl = newSite.getUrl();
        result.add(baseUrl);
        total.add(baseUrl);
        result.addAll(new ForkJoinPool().invoke(new RecursiveIndexer(baseUrl, baseUrl, total, stopper)));

        for (String r : result) {
            if(stopper.isStop()){break;}
            pageAdder(r, baseUrl, newSite);
//            String message = pageAdder(r, baseUrl, newSite);
//            siteEntityController.setError(newSite.getId(), message);
            siteEntityController.refreshSiteEntity(newSite.getId());
        }
        long time = Math.round(100 + 50 * Math.random());
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
             System.out.println(e.getMessage());
        }
    }
    // Старый pageAdder - на всякий случай
//    public void pageAdder(String path, SiteEntity newSite){
//        PageEntity newPage = new PageEntity();
//        Connection.Response response = null;
//        Document doc = null;
//        int status_code;
//        try {
//            response = Jsoup.connect(path)
//                        .userAgent(userAgent.getUserAgent())
//                        .referrer(referer.getReferer())
//                        .execute();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if(response == null) return;
//        status_code = response.statusCode();
//
//        try {
//            doc = response.parse();
//        } catch (IOException e) {
//            System.out.println("IOException");
//        }
//        if(doc == null) return;
//
//        String contentType = response.contentType();
//        String content = contentType;
//        if(contentType != null && contentType.startsWith("text/html")){
//            content = doc.html();
//        }
//        String rootPath = newSite.getUrl();
//        path = path.substring(rootPath.length() - 1);
//
//        newPage.setSiteEntity(newSite);
//        newPage.setCode(status_code);
//        newPage.setPath(path);
//        newPage.setContent(content);
//        pageEntityController.addPageEntity(newPage);
//    }  //

    public void pageAdder(String url, String baseUrl, SiteEntity newSite){
        String message = "";
        Connection.Response response = getResponse(url);
        if(response == null){
            if(url.equals(baseUrl)) {
                siteEntityController.setError(newSite.getId(), "Ошибка индексации: главная страница сайта недоступна");
            }
            return;
        }

        int status_code = response.statusCode();
        if(url.equals(baseUrl) && status_code != 200) {
            message = getMessageByStatusCode(status_code);
            siteEntityController.setError(newSite.getId(), message);
        }

        Document doc = null;
        try {
            doc = response.parse();
        } catch (IOException e) {
            System.out.println("IOException");
        }
        if(doc == null) return;

        String contentType = response.contentType(); // Контент
        String content = contentType;
        if(contentType != null && contentType.startsWith("text/html")){
            content = doc.html();
        }
        String path = pathFromUrl(url, newSite);
        PageEntity newPage = new PageEntity(newSite, path, status_code, content);
        int page_id = pageEntityController.addPageEntity(newPage);

        // Добавили страницу, теперь добавляем леммы и индексы
        TextLemmasParser parser = new TextLemmasParser(); // НАДО СДЕЛАТЬ ОБЩИМ ПОТОМ для потока
        String text = parser.htmlTagsRemover(content);  // Очистили от тэгов
        HashMap<String, Integer> lemmas; // Список лемм
        try {
            lemmas = parser.lemmasCounter(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for(String lemma : lemmas.keySet()){
            Integer lemma_id = lemmaController.getLemmaId(newSite.getId(), lemma);  // Получаем id, если есть
            if(lemma_id == null){
                LemmaEntity lemmaEntity = new LemmaEntity(newSite.getId(), lemma, 1);
                lemma_id = lemmaController.addLemma(lemmaEntity);  //  Или добавили новую
            } else {
                lemmaController.increaseFrequency(lemma_id);  // Или повысили frequency
            }
            //  Теперь будем заниматься индексом
            IndexEntity indexEntity = new IndexEntity(newPage, lemma_id, lemmas.get(lemma));
            indexController.addIndex(indexEntity);
        }
    }

    public String getMessageByStatusCode(int status_code){
        String message;
        switch(status_code){
            case 200:
                message = "";
                break;
            case 400:
                message = "Некорректный запрос";
                break;
            case 401:
                message = "Для доступа к запрашиваемому ресурсу требуется аутентификация";
                break;
            case 403:
                message = "Доступ к ресурсу ограничен";
                break;
            case 404:
                message = "Страница не найдена";
                break;
            case 405:
                message = "Указанный метод неприменим к данному ресурсу";
                break;
            case 500:
                message = "Внутренняя ошибка сервера";
                break;
            default:
                message = status_code + "Неизвестная ошибка";
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
        }

        try {
            pageAndLemmasAdder(url, siteEntity);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        siteEntityController.refreshSiteEntity(siteEntity.getId()); // После лемматизации обновляем время в sites
        statusChanger(siteEntity, Status.INDEXED);
        return new IndexingResponseTrue();
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

    public void pageAndLemmasAdder(String url, SiteEntity newSite) throws IOException {
        String path = pathFromUrl(url, newSite);
        if(pageEntityController.findBySiteIdAndPath(newSite.getId(), path) != null){   // Есть такая страница(site_id & path) - удаляем
            System.out.println("YES");
            // Тут бы надо хранимую процедуру с уменьшением frequency, но пока так:
            decreaseFrequency(newSite.getId(), path);
            pageEntityController.deletePageBySiteIdAndPath(newSite.getId(), path);
        }

        Connection.Response response = getResponse(url);
        Document doc = null;
        int status_code;
        if(response == null) return;
        status_code = response.statusCode();

        try {
            doc = response.parse();
        } catch (IOException e) {
            System.out.println("IOException");
        }
        if(doc == null) return;

        String contentType = response.contentType(); // Контент
        String content = contentType;
        if(contentType != null && contentType.startsWith("text/html")){
            content = doc.html();
        }
        PageEntity newPage = new PageEntity(newSite, path, status_code, content);
        int page_id = pageEntityController.addPageEntity(newPage);

        // Добавили страницу, теперь добавляем леммы и индексы
        TextLemmasParser parser = new TextLemmasParser(); // НАДО СДЕЛАТЬ ОБЩИМ ПОТОМ для потока
        String text = parser.htmlTagsRemover(content);  // Очистили от тэгов


        HashMap<String, Integer> lemmas = parser.lemmasCounter(text); // Список лемм
        System.out.println("После леммас");


        for(String lemma : lemmas.keySet()){
            System.out.println("Лемма " + lemma);
            Integer lemma_id = lemmaController.getLemmaId(newSite.getId(), lemma);  // Получаем id, если есть
            if(lemma_id == null){
                LemmaEntity lemmaEntity = new LemmaEntity(newSite.getId(), lemma, 1);
                lemma_id = lemmaController.addLemma(lemmaEntity);  //  Или добавили новую
            } else {
                lemmaController.increaseFrequency(lemma_id);  // Или повысили frequency
            }

            //  Теперь будем заниматься индексом
            IndexEntity indexEntity = new IndexEntity(newPage, lemma_id, lemmas.get(lemma));
            indexController.addIndex(indexEntity);
        }
    }

    public Connection.Response getResponse(String path){
        Connection.Response response = null;
        try {
            response = Jsoup.connect(path)
                    .userAgent(connectionConfig.getUserAgent())
                    .referrer(connectionConfig.getReferer())
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
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
}


