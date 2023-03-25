**_<h1>SearchEngine</h1>_**
***
***
Проект SearchEngine представляет собой систему поиска предложенных фраз на определенном сайте (группе сайтов).
Программа индексирует страницы сайтов и сохраняет в базу данных имеющиеся на их страницах леммы.
На вкладке статистики можно видеть, сколько проиндексировано сайтов и страниц, а также общее количество лемм. 

После этого при введении текстового запроса пользователь получает список страниц, содержащих составляющие его слова, 
начиная с наиболее релевантных (где предложенных в запросе слов больше всего). <br>
В результате поиска указывается число страниц, соответствующих запросу. 

При желании пользователь может проиндексировать отдельную страницу, принадлежающую одному из указанных 
в конфигурационном файле сайтов. <br>
Это можно сделать как до полной индексации сайта, так и после - с целью обновления данных.

Также имеется возможность осуществлять поиск как по полному списку проиндексированных сайтов, так и по конкретному сайту. 
*** 
*** 
<h4>_____В проекте задействованы технологии Java, Spring, Hibernate, MySQL, JavaScript._____</h4>

***
*** 
<h3>Перед запуском проекта </h3>
1. следует создать на локальном сервере SQL базу данных с названием search_engine, <br>
задав characterset utf8mb4 и collation utf8mb4_0900_as_ci, <br>
2. в файле application.yaml указать данные для доступа к серверу, <br>а также адреса и имена сайтов для индексации и поиска:<br>

<img src="https://github.com/lobashev-andrey/searchengine-master/blob/master/1.png"/>

<img src="https://github.com/lobashev-andrey/searchengine-master/blob/master/2.png"/>

при желании можно изменить размер сниппета <br>(фрагмента текста, который будет представлен в результатах поиска)

<img src="https://github.com/lobashev-andrey/searchengine-master/blob/master/3.png"/>

3. данные для доступа к серверу нужно указать и в файле META-INF/persistence.xml (в двух блоках):

<img src="https://github.com/lobashev-andrey/searchengine-master/blob/master/4.png"/>


***
*** 
<h3>Для начала работы с приложением </h3>
нужно запустить файл SearchEngine.jar, например, с помощью командной строки:  java -jar SearchEngine.jar<br>
После этого интерфейс программы будет доступен в браузере по адресу http://localhost:8080/<br>
Программа откроется на вкладке статистики: DASHBOARD<br>
Разумеется, если база данных search_engine пуста, на всех кнопках будет отображаться нулевое количество.

<img src="https://github.com/lobashev-andrey/searchengine-master/blob/master/11.png"/>

Нажав на имя любого из сайтов можно получить более подробную информацию:

<img src="https://github.com/lobashev-andrey/searchengine-master/blob/master/12.png"/>

На вкладке MANAGEMENT можно запустить индексацию всех указанных в конфигурационном файле сайтов,
нажав на кнопку START INDEXING.

<img src="https://github.com/lobashev-andrey/searchengine-master/blob/master/13.png"/>

Или ввести адрес отдельной страницы, относящейся к одному из этих сайтов, и нажать ADD/UPDATE

<img src="https://github.com/lobashev-andrey/searchengine-master/blob/master/14.png"/>

В случае, если страница недоступна или не существует, отразится соответствующее сообщение.<br>
Аналогично будут отображаться и сообщения об ошибках ввода, индексации или поиска.

<img src="https://github.com/lobashev-andrey/searchengine-master/blob/master/15.png"/>

Для поиска по проиндексированным страницам нужно перейти на вкладку SEARCH
Искать можно как сразу по всем сайтам:

<img src="https://github.com/lobashev-andrey/searchengine-master/blob/master/16.png"/>

так и по выбранному в выпадающем списке:

<img src="https://github.com/lobashev-andrey/searchengine-master/blob/master/17.png"/>

