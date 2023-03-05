package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextLemmasParser {

    public String htmlTagsRemover(String text){
        Pattern p = Pattern.compile("<script.+?/script>|<.+?>");
        Matcher m = p.matcher(text);
        return m.replaceAll(" ");
    }


    public HashMap<String, Integer> lemmasCounter(String text) throws IOException {
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        HashMap<String, Integer> map = new HashMap<>();

        Pattern p = Pattern.compile("[а-яё-]+");
        Matcher m = p.matcher(text.toLowerCase());
        ArrayList<String> words = new ArrayList<>();
        while(m.find()){
            String word = m.group();
            List<String> wordBaseForms = luceneMorph.getMorphInfo(word);
            wordBaseForms.stream()
                    .filter(a->!a.contains("СОЮЗ") && !a.contains("ПРЕДЛ")
                            && !a.contains("ЧАСТ") && !a.contains("МЕЖД")
                            && !a.startsWith("-") && !a.contains("-|")
                            && (a.indexOf("|") > 1 || a.charAt(0) == 'я'))
                    .map(a-> a.substring(0, a.indexOf("|")))
                    .forEach(a->map.put(a, map.containsKey(a) ? map.get(a) + 1 : 1));
        }
        return map;
    }




}
