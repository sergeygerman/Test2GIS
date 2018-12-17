# Test2GIS
Развертывание и запуск:
1) Сервису требуется хранилище данных - БД Oracle.
Если в наличии нет работающего инстанса БД Oracle, то сначала потребуется его развернуть.
На работающем инстансе БД Oracle создается и разворачивается схема CINEMA_RESERVATION с помощью скриптов, находящихся в каталоге CinemaReservation\DBScripts\
"00 - DBCreate.sql" - создание схемы, задание пароля и раздача прав. Выполняется из-под SYSDBA (или любой другой учетки с достаточными правами на создание новых схем)
"01-ENVIRONMENT.sql" - создание таблиц и сиквенсов, добавление базового (демонстрационного) наполнения. Выполняется из-под учетки CINEMA_RESERVATION.
После создания схемы параметры БД (хост, sid, порт, а также логин и пароль схемы, если они менялись) требуется обновить в CinemaReservation\config.xml

2) Сборка приложения осуществляется в среде Intellij Idea (не успел прикрутить gradle).
Для сборки требуется открыть проект в Intellij Idea и запустить команду Rebuild Project в меню Build, после чего все артефакты соберутся автоматически.
На случай отсутствия Intellij Idea в каталоге CinemaReservation\out\artifacts\CinemaReservation\ находятся уже собранные артефакты: CinemaReservation.jar для запуска из командной строки и CinemaReservation.war для деплоя на Tomcat.

3) Запуск
CinemaReservation.jar запускается из командной строки. Необходимо, чтобы в одном каталоге с CinemaReservation.jar лежал конфигурационный файл config.xml
Команда запуска:
java -jar <путь_до_исполняемого файла>\CinemaReservation.jar (например java -jar C:\Test2GIS\CinemaReservation.jar)
В случае использования war-сборки артефакт CinemaReservation.war необходимо просто задеплоить на Tomcat стандартным образом через веб-интерфейс Tomcat (в разделе Manager App), config.xml уже вшит внутрь CinemaReservation.war, поэтому подкидывать его дополнительно не требуется, но в случае изменений конфига потребуется либо пересобрать артефакт, либо вносить изменения непосредственно в config.xml развернутого на Tomcat сервиса CinemaReservation (TOMCAT_HOME/webapps/CinemaReservation/WEB-INF/config.xml) и рестартовать сервис.

Запущенный сервис пишет лог в консоль, из которой он был запущен (либо в консоль Tomcat). Изменить настройки логирвания можно в файле CinemaReservation\src\log4j2.xml (после этого потребуется пересобрать артефакты), например, закомментировав одни узлы Appenders и Loggers и раскомментировав другие (AppenderRef ref="STDOUT" - логирование в консоль, AppenderRef ref="RollingFile" - логирование в ротируемый файл по относительному пути logs/CinemaReservation/Log.log).

Конфигурационный файл
CinemaReservation\config.xml содержит:
1) настройки подключения к хранилищу данных (узел Storage)
2) модификатор вывода результатов (узел JsonMode). При значении true структурированные данные возвращаются в формате Json, при значении false - в xml.
3) тип используемого адаптера (узел AdapterType). На текущий момент поддерживается только адаптер Oracle.
4) номер порта (узел HttpPort). При запуске сервис занимает порт, указанный в узле HttpPort.

Обратиться к сервису после его запуска можно по адресу http://localhost:<HttpPort_value>, например http://localhost:12345