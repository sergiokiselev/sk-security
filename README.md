# sk-security

Spring boot server application which represents which represents secure server for storing files.

This application uses mail server for sending one-time passwords to clients.
To configure it change next mail properties in application.properties file
spring.mail.username
spring.mail.password

To use application on local machine you should postgresql installed. After that uncomment next properties:
spring.datasource.url
spring.datasource.username
spring.datasource.password
and create database.

When run application on local machine pass next param into arguments line —localMachine=true

This app uses Google Drive api. All configuration for it is stored in DriveServiceImpl.java