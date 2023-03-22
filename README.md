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
Перед запуском проекта следует создать на локальном сервере SQL базу данных с названием search_engine, <br>
задав characterset utf8mb4 и collation utf8mb4_0900_as_ci, <br>
а в файле application.yaml указать username и password для доступа к серверу. <br>
В этом же файле пользователь указывает адреса и имена файлов, с которыми он собирается работать, в формате:<br>
sites:    
  - url: https://www.playback.ru
  - name: PlayBack.Ru
   
Также есть возможность изменить размер сниппета (фрагмента текста, который будет представлен в результатах поиска)
***
*** 
Для начала работы с приложением нужно запустить файл searchengine.jar,<br> 
например, с помощью командной строки:  java -jar SearchEngine.jar<br>
После этого интерфейс программы будет доступен в браузере по адресу http://localhost:8080/<br>
Программа откроется на вкладке статистики: DASHBOARD<br>
Разумеется, если база данных search_engine пуста, на всех кнопках будет отображаться нулевое количество.

<img src="C:\Users\lobas\Downloads\2023-03-22_18-34-17.png" width="1060"/>

Нажав на имя любого из сайтов можно получить более подробную информацию:

<img src="C:\Users\lobas\Downloads\2023-03-22_18-39-27.png" width="1053"/>

На вкладке MANAGEMENT можно запустить индексацию всех указанных в конфигурационном файле сайтов,
нажав на кнопку START INDEXING.

<img src="C:\Users\lobas\Downloads\2023-03-22_10-29-15.png" width="1046"/>

Или ввести адрес отдельной страницы, относящейся к одному из этих сайтов, и нажать ADD/UPDATE

<img src="C:\Users\lobas\Downloads\2023-03-22_10-31-12.png" width="1062"/>

В случае, если страница недоступна или не существует, отразится соответствующее сообщение.<br>
Аналогично будут отображаться и сообщения об ошибках ввода, индексации или поиска.

<img src="C:\Users\lobas\Downloads\2023-03-22_18-28-44.png"/>

Для поиска по проиндексированным страницам нужно перейти на вкладку SEARCH
Искать можно как сразу по всем сайтам:

<img src="C:\Users\lobas\Downloads\2023-03-22_10-45-02.png" width="1066"/>

так и по выбранному в выпадающем списке:

<img src="C:\Users\lobas\Downloads\2023-03-22_10-35-56.png" width="1046"/>
