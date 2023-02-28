package searchengine.services;


import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;

@AllArgsConstructor
public class RecursiveIndexer extends RecursiveTask<List<String>> {
    private final String address;
    private final String baseUrl;
    private final Set<String> total;
    private final Stopper stopper;


    @Override
    protected List compute() {
        List<RecursiveIndexer> pageConstructors = new ArrayList<>(); // Создаем список задач
        List<String> children = new ArrayList<>();

        if(stopper.isStop()){
            System.out.println("ST___________OOOOOOOOOOOOOO____________________P");
            return new ArrayList<>();
        }

        try {
            Document doc = Jsoup.connect(address).get();
            Elements elements = doc.select("a");
            for(Element el : elements){
                String child = el.attr("abs:href");
                if(!child.startsWith(baseUrl) || child.contains("#")) {
                    continue;
                }
                int before = total.size();
                total.add(child);
                if(total.size() == before){
                    continue;
                }
                children.add(child);
            }
            for(String child : children) {  // Добавили ближайших детей, теперь каждому даем ту же задачу
                RecursiveIndexer rec = new RecursiveIndexer(child, baseUrl, total, stopper);
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
        if(stopper.isStop()){
            System.out.println("STOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOoooP");
            return new ArrayList<>();
        }

        return children;
    }
}