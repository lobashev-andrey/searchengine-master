//package searchengine.services;
//
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import searchengine.controllers.PageEntityController;
//import searchengine.model.PageEntity;
//import searchengine.model.SiteEntity;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.RecursiveTask;
//
//public class RecursiveIndexer extends RecursiveTask<Set<String>> {
//    private final SiteEntity siteEntity;
//    private final PageEntityController pageEntityController;
//    public RecursiveIndexer(SiteEntity siteEntity, PageEntityController pageEntityController) {
//        this.siteEntity = siteEntity;
//        this.pageEntityController = pageEntityController;
//    }
//
//    @Override
//    protected Set compute() {
//        String url = siteEntity.getUrl();        // Возможно, очистить от '/' в конце ?????????
//
//        List<RecursiveIndexer> pageConstructors = new ArrayList<>(); // Создаем список задач
//
//        try {
//            Document doc = Jsoup.connect(url).get();
//            Elements elements = doc.select("a");
//            for(Element el : elements){
//                String child = el.attr("abs:href");
//                if(!child.startsWith(url) || child.equals(url)) {
//                    continue;
//                }
//                // Надо понять, нет ли уже этого адреса в таблице
//                if(pageEntityController.containsUrl(url)){
//                    continue;
//                }
//
//
//                int start = url.length();
//                int end = child.length();
//                if(child.substring(start).contains("/")) {
//                    end = child.indexOf("/", start);
//                }
//                child = child.substring(0, Math.min(end + 1, child.length()));
//                children.add(child);
//            }
//
//
//            for(String child : children) {
//                RecursiveIndexer pc = new RecursiveIndexer(new Page(child, "\t" + indent));
//                pc.fork();
//                pageConstructors.add(pc);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        hierarchy.append(indent + address + System.lineSeparator());  // Добавляем в hierarchy адрес узла с indent'ом
//        for (RecursiveIndexer task : pageConstructors) {
//            long time = Math.round(100 + 50 * Math.random());
//            try {
//                Thread.sleep(time);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            hierarchy.append(task.join().toString());    // Добавляем результаты каждого ребенка
//        }
//        if(pageConstructors.size() == 0) {                  ////////////////   УБИРАЕМ "БЕЗДЕТНЫЕ" СТРАНИЦЫ  /////////////
//            return hierarchy.delete(0, hierarchy.length()); ////////////////      НО ЕСЛИ ОНИ НУЖНЫ,      /////////////
//        }                                                   ////////////////     МОЖНО ЗАКОММЕНТИРОВАТЬ   /////////////
//        return hierarchy;
//    }
//
//
//
//
//
//
//}