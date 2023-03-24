package searchengine.services;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import searchengine.config.ConnectionConfig;
import searchengine.services.param_files.RecursiveIndexerParams;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveTask;


public class RecursiveIndexer extends RecursiveTask<List<String>> {
    private final String address;
    private final String baseUrl;
    private final Set<String> total;
    private final Stopper stopper;
    private final ConnectionConfig connectionConfig;

    public RecursiveIndexer(RecursiveIndexerParams params){
        this.address = params.getAddress();
        this.baseUrl = params.getBaseUrl();
        this.total = params.getTotal();
        this.stopper = params.getStopper();
        this.connectionConfig = params.getConnectionConfig();
    }


    @Override
    protected List<String> compute() {
        List<RecursiveIndexer> pageConstructors = new ArrayList<>();

        if(stopper.isStop()){
            return new ArrayList<>();
        }

        List<String> children = getChildren();
        for(String child : children) {
            RecursiveIndexerParams params = new RecursiveIndexerParams(child, baseUrl, total, stopper, connectionConfig);
            RecursiveIndexer rec = new RecursiveIndexer(params);
            rec.fork();
            pageConstructors.add(rec);
        }

        for (RecursiveIndexer task : pageConstructors) {
            long time = Math.round(100 + 50 * Math.random());
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            children.addAll(task.join());
        }
        if(stopper.isStop()){
            return new ArrayList<>();
        }

        return children;
    }

    public List<String> getChildren(){
        List<String> children = new ArrayList<>();
        Document doc;
        try {
            Connection.Response response = Jsoup.connect(address)
                    .userAgent(connectionConfig.getUserAgent())
                    .referrer(connectionConfig.getReferer())
                    .execute();
            doc = response.parse();
        } catch (IOException e) {
            return children;
        }
        Elements elements = doc.select("a");
        for(Element el : elements){
            if(el.hasAttr("onclick")){continue;}
            String child = el.attr("abs:href");
            if(!child.startsWith(baseUrl) || child.contains("#") || child.contains(".pdf")|| child.contains(".jpeg")  || child.contains(".jpg") || child.contains(".png") || child.contains(".mp4") || child.contains(".docx") || child.contains(".doc") || child.contains(".xls") || child.contains(".pptx") || child.contains("?http") || child.contains("\\") || child.contains("JPG") || child.contains("JPEG")) {
                continue;
            }
            int before = total.size();
            total.add(child);
            if(total.size() == before){
                continue;
            }
            children.add(child);
        }
        return children;
    }

}