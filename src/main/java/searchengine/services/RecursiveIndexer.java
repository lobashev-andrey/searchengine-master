package searchengine.services;


import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.controllers.PageEntityController;
import searchengine.controllers.SiteEntityController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

@RequiredArgsConstructor
public class RecursiveIndexer extends RecursiveTask<List<String>> {
    private final String baseUrl;
    private final String address;
    private final Set<String> urlSet;

    @Override
    protected List compute() {
        List<RecursiveIndexer> pageConstructors = new ArrayList<>(); // Создаем список задач
        List<String> children = new ArrayList<>();
//
//        urlSet.add(address);

        try {
            Document doc = Jsoup.connect(address).get();
            Elements elements = doc.select("a");
            for(Element el : elements){
                String child = el.attr("abs:href");
                if(!child.startsWith(baseUrl)
                        || child.contains("#")
                        || urlSet.contains(child)) {                       // ??????????????????????
                    continue;
                }
//                if(pageEntityController.containsUrl(child)){  // Надо понять, нет ли уже этого path в таблице
//                    continue;
//                }
                children.add(child);
                urlSet.add(child);
            }
            for(String child : children) {  // Добавили ближайших детей, теперь каждому даем ту же задачу
                RecursiveIndexer rec = new RecursiveIndexer(baseUrl, child, urlSet);
                rec.fork();
                pageConstructors.add(rec);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        for (RecursiveIndexer task : pageConstructors) {
            long time = Math.round(100 + 50 * Math.random());   //Math.round(500 + 4500 * Math.random());
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            children.addAll(task.join());    // Добавляем результаты каждого ребенка
        }
        return children;
    }






}