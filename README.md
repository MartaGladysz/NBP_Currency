# NBP_Currency

This repository contains the source of web application which is a Spring Boot project.

Tools used:

* Java 8
* Spring Boot
* Spring Data
* JPA/Hibernate
* MySQL relational database 
* JSON.simple
* iText
* Thymeleaf
* Bootstrap
* HTML
* CSS

To start this application is needed:
* Java IDE
* MySQL database which should be configured according to settings contained in the application.properties file in the resources directory

This application retrieves data from NBP site: http://api.nbp.pl using JSON format and enables keeping track of changes in currencies rates from table A. Moreover, user can take choice of all currencies and give number of quotations in which he is interested and then he gets report, which displays these data, extra with the average, the highest and the lowest rates. In addition, chosen data can be saved in pdf file.


The project was created by Marta Gładysz.
