package searchengine.controllers;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import searchengine.model.LemmaEntity;
import searchengine.services.IndexingService;
import searchengine.services.IndexingServiceImpl;
import searchengine.services.TextLemmasParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws IOException {
        String text = "однажды в студёную зимнюю пору... дддддддддддддддддд ололд йцуф элэлэдж щщщщщгггшшш смотрю: всё Ёмкость Емкость поднимаются лёгкий медленно; в гору... лошадку ведет под уздцы!";
//        TextLemmasParser parser = new TextLemmasParser();
//        HashMap<String, Integer> lemmas = parser.lemmasCounter(text);
//        lemmas.keySet().forEach(System.out::println);
//        LemmaEntity lemmaEntity = new LemmaEntity(1, "ёмкость", 1);
//        System.out.println(lemmaEntity.getLemma() + "///");
//
//        System.out.println(lemmas.get(lemmaEntity.getLemma()));
        LuceneMorphology luceneMorph = new RussianLuceneMorphology();
        HashMap<String, Integer> map = new HashMap<>();

        Pattern p = Pattern.compile("[а-яё-]+");
        Matcher m = p.matcher(text.toLowerCase());
        while(m.find()) {
            String word = m.group();
            List<String> wordBaseForms = luceneMorph.getMorphInfo(word);
            System.out.println(word + " *********************");
            wordBaseForms.forEach(System.out::println);
        }

    }

}
