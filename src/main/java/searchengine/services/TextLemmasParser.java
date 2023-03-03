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
        Pattern p = Pattern.compile("<.+?>");
        Matcher m = p.matcher(text);
        return m.replaceAll(" ").replaceAll("\n", "");
    }

    public HashMap<String, Integer> lemmasCounter(String text) throws IOException {
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        HashMap<String, Integer> map = new HashMap<>();
        ////////
        // LowerText !!!!
        Pattern p = Pattern.compile("[а-я]+");  ////////////?????????????????????????
        Matcher m = p.matcher(text.toLowerCase());
        ArrayList<String> words = new ArrayList<>();
        while(m.find()){
            String line = m.group();
            System.out.println("/// " + line);
            words.add(line);
        }



//        String[] words = text.toLowerCase().split("[^а-я]+");

        for(String word : words){
            System.out.println("** " + word + (int)word.charAt(0));

        }
        System.out.println("ЭТО ВСЕ СЛОВА " + words.size());
        for(String word : words){
            List<String> wordBaseForms = luceneMorph.getMorphInfo(word);
            wordBaseForms.stream()
                    .filter(a->!a.contains("СОЮЗ") && !a.contains("ПРЕДЛ") && !a.contains("ЧАСТ"))
                    .map(a-> a.substring(0, a.indexOf("|")))
                    .forEach(a->map.put(a, map.containsKey(a) ? map.get(a) + 1 : 1));
        }
        return map;
    }




}
