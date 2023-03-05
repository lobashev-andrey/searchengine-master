package searchengine.controllers;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.services.TextLemmasParser;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
//        TextLemmasParser textLemmasParser = new TextLemmasParser();

//        String text = "Повторное появление леопарда в Осетии позволяет предположить, " +
//                "что леопард постоянно обитает в некоторых районах Северного Кавказа.";
//        HashMap<String, Integer> map = textLemmasParser.lemmasCounter(text);
//        for(String s : map.keySet()){
//            System.out.println(s + " - " + map.get(s));
//        }

//        String htmlText = "Федя array(1) тихо-мирно";
//
//        String text = textLemmasParser.htmlTagsRemover(htmlText);
//        for(String i : textLemmasParser.lemmasCounter(text).keySet()){
//            System.out.println(i);
//        }

        LuceneMorphology luceneMorph =
                new RussianLuceneMorphology();
        List<String> wordBaseForms =
                luceneMorph.getMorphInfo("ох");
        wordBaseForms.forEach(System.out::println);



    }
}
