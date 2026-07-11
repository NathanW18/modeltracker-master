# README #

This README would normally document whatever steps are necessary to get your application up and running.

### What is this repository for? ###

* UHN Cancer Informatics Program's Biospecimen model tracker system. 
* Version 0.1
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### How do I get set up? ###

* Check out the code using Git.  
	* User could use bitbucket's sourcetree which has a gui interface or user can use any git clients. User will need to have an account on UHN Research bitbucket though.  
	* command to download the code: git clone https://"your bitbucket id"@collaborate.uhnresearch.ca/stash/scm/biof/biospecimengateway.git "local folder where you want to download to".
	* there is a pom.xml file in the project so you just need to go to STS's import existing maven project.
* Configuration: the project is configured using spring configuration files. 
  * STS configuration: set default java environment to 1.8. java compliance level to 1.8.
  * This is a springboot application so it does not rely on a tomcat server to run. it has its embedded tomcat and can be run as Java application.
* Dependencies: 
  * MVN build: the project dependency is managed by maven automatically.
  * STS build: STS project file is already set up.
* Build: you can build the FHIR server app using two methods, as a developer it is recommended to use STS build. Maven build is for generating deployment jar.
  1. Maven build: in STS right click project and select run as ... and then mvn build option.
  2. STS build: in STS right click project and select build option.

* Database configuration: there are two deployment environments. 
  * one is for development which uses embedded H2 database usually runs on the developer's own machine. the set up for database is already in the configuration so there is no need to setup. 
  * the other environment is for production which uses Mysql database server (version 5.7) and the database resides on technavm04 server. for this configuration, user will need to run the gateway app first time with following setting:
  spring.jpa.hibernate.ddl-auto=create so it will create the database table automatically.
  * mysql -u root -p; CREATE DATABASE dbname; GRANT ALL PRIVILEGES ON dbname.* TO 'username'@'%' IDENTIFIED BY 'password'; 
  * after first time run, change the setting to spring.jpa.hibernate.ddl-auto=none so it will not create the same table again.
  * user also need to use mysql command line to create the corresponding database and user account and set the privilleges.
  

* How to run tests: TBA

* Deployment instructions: Deployment is done manually so far. 
  * first generate the deployment jar using "mvn package", next copy the jar to "INT/QA/PROD" server. 
  * create a biospecimengateway directory on the windows server.
  * download winsw.exe which is a windows service wrapper to create a windows service.
  * create the biospecimengateway.xml configuration file in the same folder.
  * change the winsw.exe to biospecimengateway.exe.
  * run "biospecimengateway.exe install" and go to windows service manager and start the service.
  * also need to open up port 8090 using windows firewall tool.
  

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner or admin
* Other community or team contact

### Docker set up ###

* You are run the app as spring boot jar in a container
  * Dockerfile is located in the root folder
  * run "docker build -t model-tracker-server:dev ." to create the docker image
  * run "docker run -it -p 8080:8080 -v /Users/kevinwang/logs:/var/logs  model-tracker-server:dev" to start the container.
  * the "-v " mapping to for setting up the logback log file.
