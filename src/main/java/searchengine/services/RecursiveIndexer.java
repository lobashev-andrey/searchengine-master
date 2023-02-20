//package searchengine.services;
//
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import searchengine.model.PageEntity;
//import searchengine.model.SiteEntity;
//
//import java.io.IOException;
//        import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//        import java.util.Set;
//        import java.util.concurrent.RecursiveTask;
//
//public class RecursiveIndexer extends RecursiveTask<Set<String>> {
//    private final SiteEntity siteEntity;
//    public RecursiveIndexer(SiteEntity siteEntity) {
//        this.siteEntity = siteEntity;
//    }
//
//    @Override
//    protected Set compute() {
//        String address = siteEntity.getUrl();
//        List<PageEntity> children = siteEntity.getChildren();
//
//        Set<String> seenPages = new HashSet<>(); // Сюда складываем просмотренные страницы (адреса)
//
//        List<RecursiveIndexer> pageConstructors = new ArrayList<>(); // Создаем список задач
//        try {
//            Document doc = Jsoup.connect(address).get();
//            Elements elements = doc.select("a");
//            for(Element el : elements){
//                String child = el.attr("abs:href");
//                if(!child.startsWith(address) || child.equals(address)) {
//                    continue;
//                }
//                for(String s : seenPages){
//                    if(s.equals(child))
//                }
//
//
//
//                int start = address.length();
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