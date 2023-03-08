package searchengine.controllers;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import searchengine.services.TextLemmasParser;

import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        TextLemmasParser textLemmasParser = new TextLemmasParser();

//        String text = "Повторное появление леопарда в Осетии позволяет предположить, " +
//                "что леопард постоянно обитает в некоторых районах Северного Кавказа.";
//        HashMap<String, Integer> map = textLemmasParser.lemmasCounter(text);
//        for(String s : map.keySet()){
//            System.out.println(s + " - " + map.get(s));
//        }

        String htmlText = "<div class=\"hidden-block\">\n" +
                "    <div id=\"icontact-us\" class=\"iauth\">\n" +
                "          <p class=\"iauth__title\">Связаться с нами</p>\n" +
                "          <div class=\"iauth__nav\">\n" +
                "              <button type=\"button\" data-icontact-tab=\"1\" class=\"iauth__nav-item js-icontact-btn active\">Заказать обратный звонок</button>\n" +
                "              <button type=\"button\" data-icontact-tab=\"2\" class=\"iauth__nav-item js-icontact-btn\">Написать сообщение</button>\n" +
                "            <a href=\"https://www.dextra.ru/contacts/\" class=\"iauth__nav-item iauth__nav-item--contact\">Контакты</a>\n" +
                "          </div>\n" +
                "          <div id=\"data-icontact-tab1\" class=\"iauth__tab active\">\n" +
                "            \n" +
                "<script type=\"text/javascript\">\n" +
                "    document.addEventListener(\"DOMContentLoaded\", function () {\n" +
                "        const form = document.querySelector('#callorderForm') || [];\n" +
                "        if (form.length) {\n" +
                "            form.innerHTML += '<input type=\"hidden\" name=\"callorderFormNumber\" value=\"8f76f2dc36860fa404b466f17980ae82\">';\n" +
                "        }\n" +
                "    });\n" +
                "</script>\n" +
                "    <form id=\"callorderForm\" method=\"post\" class=\" iauth__form jAjax \">\n" +
                "        <input type=\"hidden\" name=\"action\" value=\"callorder\" />\n" +
                "        <input type=\"text\" name=\"iambot\" value=\"\" style=\"display: none;\" />\n" +
                "        <div class=\"pseudoplaceholder\">\n" +
                "            <input type=\"text\" name=\"name\" data-req=\"req\" data-type=\"text\" class=\"iinput iauth__input pseudoplaceholder__area\"/>\n" +
                "            <label for=\"#\" class=\"pseudoplaceholder__label\">Имя</label>\n" +
                "        </div>\n" +
                "        <div class=\"pseudoplaceholder\">\n" +
                "            <input name=\"phone\" type=\"text\" data-req=\"req\" data-type=\"phone\" class=\"iinput iauth__input pseudoplaceholder__area js-phone\"/>\n" +
                "            <label for=\"#\" class=\"pseudoplaceholder__label\">Телефон</label>\n" +
                "            <p class=\"star-field\" style=\"    font-size: 14px;\">\n" +
                "                Все поля обязательны для заполнения.\n" +
                "            </p>\n" +
                "        </div>\n" +
                "\n" +
                "        <div class=\"ipseudocheckbox iauth__agreement\">\n" +
                "        <input type=\"checkbox\" id=\"personal-data10\" checked=\"checked\" data-req=\"req\" name=\"agreement\" class=\"ipseudocheckbox__checkbox\"/>\n" +
                "        <label for=\"personal-data10\" class=\"ipseudocheckbox__label\">\n" +
                "            Даю согласие на обработку <a target=\"_blank\" href=\"/private-agreement/\">персональных&nbsp;данных</a>.\n" +
                "                    </label>\n" +
                "        </div>\n" +
                "        <button type=\"submit\" class=\"ibtn\">Отправить</button>\n" +
                "        <div class=\"errors\" data-type=\"message\"></div>\n" +
                "        <div class=\"message\" data-type=\"message\">\n" +
                "                                </div>\n" +
                "    </form>          </div>\n" +
                "          <div id=\"data-icontact-tab2\" class=\"iauth__tab\">\n" +
                "            \n" +
                "<script type=\"text/javascript\">\n" +
                "    document.addEventListener(\"DOMContentLoaded\", function () {\n" +
                "        const form = document.querySelector('#feedbackForm') || [];\n" +
                "        if (form.length) {\n" +
                "            form.innerHTML += '<input type=\"hidden\" name=\"feedbackFormNumber\" value=\"0a3d5944a2496819c97df96b707cc8e6\">';\n" +
                "        }\n" +
                "    });\n" +
                "</script>\n" +
                "        <form id=\"feedbackForm\" method=\"post\" enctype=\"multipart/form-data\" class=\"iauth__form jAjax \">\n" +
                "            <input type=\"hidden\" name=\"action\" value=\"feedback\" />\n" +
                "            <input type=\"text\" name=\"iambot\" value=\"\" style=\"display: none;\" />\n" +
                "\n" +
                "            <div class=\"pseudoplaceholder\">\n" +
                "                <input type=\"text\" name=\"name\" data-req=\"req\" data-type=\"text\" class=\"iinput iauth__input pseudoplaceholder__area\"/>\n" +
                "                <label for=\"#\" class=\"pseudoplaceholder__label\">Имя<span>*</span></label>\n" +
                "            </div>\n" +
                "              <div class=\"iauth__row\">\n" +
                "                <div class=\"pseudoplaceholder pseudoplaceholder--half30\">\n" +
                "                  <input  name=\"email\" type=\"text\" data-req=\"req\" data-type=\"email\" class=\"iinput iauth__input pseudoplaceholder__area\"/>\n" +
                "                  <label for=\"#\" class=\"pseudoplaceholder__label\">Электронная почта<span>*</span></label>\n" +
                "                </div>\n" +
                "                <div class=\"pseudoplaceholder pseudoplaceholder--half30\">\n" +
                "                  <input type=\"text\" name=\"phone\" data-req=\"req\" data-type=\"phone\" class=\"iinput iauth__input pseudoplaceholder__area js-phone\"/>\n" +
                "                  <label for=\"#\" class=\"pseudoplaceholder__label\">Телефон<span>*</span></label>\n" +
                "                </div>\n" +
                "              </div>\n" +
                "              <div class=\"pseudoplaceholder\">\n" +
                "                <textarea name=\"text\" class=\"itextarea iauth__textarea pseudoplaceholder__area\"></textarea>\n" +
                "                <label for=\"#\" class=\"pseudoplaceholder__label\">Сообщение</label>\n" +
                "                <p class=\"star-field\" style=\"    font-size: 14px;\">\n" +
                "                    <span class=\"star\">*</span><span> — поля со звёздочкой обязательны для заполнения.</span>\n" +
                "                </p>\n" +
                "              </div>\n" +
                "              <div class=\"pseudofile\">\n" +
                "                <input type=\"file\" class=\"pseudofile__input\" name=\"file\"/>\n" +
                "                <button type=\"button\" value=\"Прикрепить\" class=\"pseudofile__btn\">Прикрепить файл</button>\n" +
                "                <div class=\"pseudofile__file-name\"></div>\n" +
                "                <button type=\"button\" class=\"pseudofile__remove js-pseudofile-clear\"></button>\n" +
                "              </div>\n" +
                "              <div class=\"ipseudocheckbox iauth__agreement\">\n" +
                "                <input type=\"checkbox\" id=\"personal-data11\" checked=\"checked\" data-req=\"req\" name=\"agreement\" class=\"ipseudocheckbox__checkbox\"/>\n" +
                "                <label for=\"personal-data11\" class=\"ipseudocheckbox__label\">\n" +
                "                    Даю согласие на обработку <a target=\"_blank\" href=\"/private-agreement/\">персональных&nbsp;данных</a>.\n" +
                "                                    </label>\n" +
                "              </div>\n" +
                "              <button type=\"submit\" class=\"ibtn\">Отправить</button>\n" +
                "              <div class=\"errors\" data-type=\"message\"></div>\n" +
                "              <div class=\"message\" data-type=\"message\">\n" +
                "                                                        </div>\n" +
                "        </form>              </div>\n" +
                "    </div>\n" +
                "    <div id=\"igift\" class=\"igift\">\n" +
                "        <img src=\"/pics/uploads/form-gift-image.png\" alt=\"\">\n" +
                "        <div class=\"iauth\">\n" +
                "            <p class=\"iauth__title\">Получите подарок</p>\n" +
                "            <p class=\"iauth__text\">Оставив заявку на обратный звонок сейчас, Вы получите подарок – уникальный чек-лист «Профессиональные фишки для самостоятельного аудита сайта».</p>\n" +
                "            <!-- <p class=\"iauth__text\">Заполните, пожалуйста, короткую форму ниже, и мы отправим на Вашу почту подарок – чек-лист «Профессиональные фишки для самостоятельного аудита сайта».</p> -->\n" +
                "  ";

        // Parse str into a Document
        Document doc = Jsoup.parse(htmlText);

// Clean the document.
//        doc = new Cleaner(Whitelist.simpleText()).clean(doc);

// Adjust escape mode
        doc.outputSettings().escapeMode(Entities.EscapeMode.xhtml);

// Get back the string of the body.
//        str = doc.body().html();

        String text = textLemmasParser.htmlTagsRemover(htmlText);
//        for(String i : textLemmasParser.lemmasCounter(text).keySet()){
//            System.out.println(i);
//        }
        System.out.println(getTextOnlyFromHtmlText(htmlText));

//        LuceneMorphology luceneMorph =
//                new RussianLuceneMorphology();
//        List<String> wordBaseForms =
//                luceneMorph.getMorphInfo("ох");
//        wordBaseForms.forEach(System.out::println);



    }

    public static String getTextOnlyFromHtmlText(String htmlText){
        Document doc = Jsoup.parse( htmlText );
        doc.outputSettings().charset("UTF-8");
        htmlText = Jsoup.clean( doc.body().html(), Safelist.simpleText());
        return htmlText;
    }
}
