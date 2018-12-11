Box Partner Developer Training
=====================================
This is project is used during Box partner developer enablement session. It provides code examples in Java around numerous endpoints ranging from authentication to basic content services to governance services.


Setup
-------------------------
1) Clone the repo.
```
git clone https://github.com/kylefernandadams/box-partner-developer-training-java.git
```
2) Create a new 3-legged OAuth Box Developer Application using the following [instructions](https://developer.box.com/docs/setting-up-an-oauth-app).
  * Set the redirect URI to: `https://localhost:8081/box/auth/oauth/redirect`
  * Get the `BOX_CLIENT_ID` value from the application configuration and set it in the [BoxOAuth2Authentication.java class](/src/main/java/com/box/developer/training/exercise1/BoxOAuth2Authentication.java#L18)
  * Get the `BOX_CLIENT_SECRET` value from the application configuration and set it in the [BoxOAuth2Authentication.java class](/src/main/java/com/box/developer/training/exercise1/BoxOAuth2Authentication.java#L19)
3) Create a new JWT Box developer application with the following [instructions](https://developer.box.com/docs/setting-up-a-jwt-app).
  * Take the automatically generated public/private key pair json file and copy it to the [/resources project directory](/src/main/resources).
  * Rename the json file to `box_config.json`.
  * Add `http://localhost:8080` to the CORS Domains' Allowed Origins configuration of the application configuration.
4) Build the project using Maven.
```
mvn clean package
```
5) Run Spring Boot
```
mvn spring-boot:run
```
6) Confirm the application is running by navigating to the [/box endpoint](http://localhost:8081/box)
