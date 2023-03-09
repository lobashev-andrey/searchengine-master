package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import searchengine.config.SnippetParams;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
public class TextLemmasParser {
//    private final SnippetParams snippetParams;

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
//        ArrayList<String> words = new ArrayList<>();
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
        int fragLength = 230;
        HashMap<Integer, Integer> indexToNumberOfLemmas = new HashMap<>();
        for(Integer i : indexToLemma.keySet()){
            int count = 0;
            for(String lemma : lemmas){
                if(indexToLemma.entrySet().stream().anyMatch
                        (a -> (a.getKey() >= i && a.getKey() < i + fragLength && a.getValue().equals(lemma))))
                {
                    count++;
                }
            }
            indexToNumberOfLemmas.put(i, count);
            Integer best = indexToNumberOfLemmas.keySet().stream()
                    .sorted(Comparator.comparing(indexToNumberOfLemmas::get)
                            .reversed()).collect(Collectors.toList()).get(0);
            return text.substring(best,
                    Math.min((best + fragLength), text.length()));
        }
        return "";
    }

    public static String getTextOnlyFromHtmlText(String htmlText){
        Document doc = Jsoup.parse( htmlText );
        doc.outputSettings().charset("UTF-8");
        htmlText = Jsoup.clean( doc.body().html(), Safelist.simpleText());
        Pattern p = Pattern.compile("<.+?>");
        Matcher m = p.matcher(htmlText);
        return m.replaceAll("");
    }

    public String boldTagAdder(String rawFragment, List<String> lemmas){
        rawFragment = " " + rawFragment + " ";
        LuceneMorphology luceneMorph = null;
        try {
            luceneMorph = new RussianLuceneMorphology();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Pattern p = Pattern.compile("[а-яё-]+");
        Matcher m = p.matcher(rawFragment.toLowerCase());

        // КАК-ТО НАДО СДЕЛАТЬ ВЫДЕЛЕНИЕ <b></b>
        StringBuilder builder = new StringBuilder();
        int afterWord = 0;
        while(m.find()){
            String lowCaseWord = m.group();
            int index = m.start();
            builder.append(rawFragment.substring(afterWord, index));  // Добавили то, что с начала или после предыдущего слова
            List<String> wordBaseForms = luceneMorph.getMorphInfo(lowCaseWord);
            String originalWord = rawFragment.substring(index, index + lowCaseWord.length());
            boolean containsLemma = false;
            for(String s : wordBaseForms){
                if (lemmas.contains(s.substring(0, s.indexOf("|")))) {
                    containsLemma = true;
                    break;
                }
            }
            builder.append(containsLemma ? "<b>" + originalWord + "</b>" : originalWord);
            afterWord = index + lowCaseWord.length();
        }
        return "*** " + builder.toString();
    }


}
