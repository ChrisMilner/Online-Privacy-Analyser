# Online-Privacy-Analyser
The Online Privacy Analayser is a web app developed as part of a Computer Science University project. The web application aims to make it easy for social media users to view the amount of information they put out publicly online. 

## Running 
Although the app is open-source and all code is freely available here you will likely be unable to run the app. This is because it requires application information to be set up on Reddit, Facebook and Twitter. The information about these apps must be included in a file named config.properties inside of the resources/properties/ directory. The file must include:

```
f4j.debug=
f4j.http.prettyDebug=
f4j.oauth.appId=
f4j.oauth.appSecret=
f4j.oauth.accessToken=

t4j.debug=
t4j.prettyDebug=
t4j.oauth.consumerKey=
t4j.oauth.consumerSecret=
t4j.oauth.accessToken=602310612-
t4j.oauth.accessTokenSecret=

jraw.clientId=
jraw.clientSecret=
```

If you have all this information then you can run the application on a Tomcat server by deploying the repository to a WAR file. Alternatively, you can run the classes found in the java directory indepently of the app as most of them include main functions for testing purposes. 
