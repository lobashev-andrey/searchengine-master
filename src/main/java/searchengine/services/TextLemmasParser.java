package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public String getFragmentWithAllLemmas(String htmlText, List<String> lemmas) throws IOException {
        String text = getTextOnlyFromHtmlText(htmlText);
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        HashMap<Integer, String> indexToLemma = new HashMap<>();
        Pattern p = Pattern.compile("[а-яё-]+");
        Matcher m = p.matcher(text.toLowerCase());
        ArrayList<String> words = new ArrayList<>();
        while(m.find()){
            String word = m.group();
            Integer index = m.start();
            List<String> wordBaseForms = luceneMorph.getMorphInfo(word);
            for(String s : wordBaseForms){
                if(lemmas.contains(s.substring(0, s.indexOf("|")))){
                    indexToLemma.put(index, s.substring(0, s.indexOf("|")));
                }
            }
        }
        int fragmentStart = 0;
        for(Integer i : indexToLemma.keySet()){

            int DELTA = 300;
            int count = 0;
            for(String lemma : lemmas){
                if(indexToLemma.entrySet().stream().anyMatch
                        (a -> (a.getKey() >= i && a.getKey() < i + DELTA && a.getValue().equals(lemma))))
                {
                    count++;
                }
            }
            if(count == lemmas.size()){
                fragmentStart = i;
                return text.substring(fragmentStart, fragmentStart + 300);
            }

        }
        return "";
    }

    public static String getTextOnlyFromHtmlText(String htmlText){
        Document doc = Jsoup.parse( htmlText );
        doc.outputSettings().charset("UTF-8");
        htmlText = Jsoup.clean( doc.body().html(), Safelist.simpleText());
        return htmlText;
    }




}
