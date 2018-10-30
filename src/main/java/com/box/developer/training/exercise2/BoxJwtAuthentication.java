package com.box.developer.training.exercise2;

import com.box.sdk.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

@RestController
public class BoxJwtAuthentication {
    private static final int MAX_CACHE_ENTRIES = 100;

    private BoxConfig boxConfig = null;
    private IAccessTokenCache accessTokenCache = null;
    private BoxDeveloperEditionAPIConnection serviceAccountConnection = null;
    private BoxDeveloperEditionAPIConnection appUserConnection = null;

    public BoxJwtAuthentication() {

        try {
            // Read app config file from classpath. This is the file that was downloaded from generating the public/private keypair
            ClassLoader classLoader = getClass().getClassLoader();
            boxConfig = BoxConfig.readFrom(new FileReader(classLoader.getResource("box_config.json").getFile()));
            accessTokenCache = new InMemoryLRUAccessTokenCache(MAX_CACHE_ENTRIES);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/box/auth/jwt/service-account")
    public Map<Object, Object> getServiceAccountConnection(){
        Map<Object, Object> responseMap = new HashMap<>();
        try{
            // Create a service account connection
            serviceAccountConnection = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig, accessTokenCache);

            // Get the current user
            BoxUser.Info userInfo = BoxUser.getCurrentUser(serviceAccountConnection).getInfo();

            // Create response map to return
            responseMap.put("user_id", userInfo.getID());
            responseMap.put("login", userInfo.getLogin());
            responseMap.put("name", userInfo.getName());
            responseMap.put("access_token", serviceAccountConnection.getAccessToken());
            responseMap.put("expires_in", serviceAccountConnection.getExpires());
            responseMap.put("refresh_token", serviceAccountConnection.getRefreshToken());
            responseMap.put("last_refresh", serviceAccountConnection.getLastRefresh());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return responseMap;
    }


    @RequestMapping("/box/auth/jwt/create-app-user")
    public Map<Object, Object> createAppUser(@RequestParam String name, @RequestParam String external_app_user_id) {
        Map<Object, Object> responseMap = new HashMap<>();
        try{
            // Get a service account connection
            this.getServiceAccountConnection();

            // Create user params: -1 = unlimited storage
            CreateUserParams userParams = new CreateUserParams();
            userParams.setSpaceAmount(-1);
            userParams.setExternalAppUserId(external_app_user_id);
            System.out.println("Found external app user id: " + external_app_user_id);

            // Create the new app user
            BoxUser.Info userInfo = BoxUser.createAppUser(serviceAccountConnection, name, userParams);

            // Create response map to return
            responseMap.put("user_id", userInfo.getID());
            responseMap.put("login", userInfo.getLogin());
            responseMap.put("name", userInfo.getName());
            responseMap.put("external_app_user_id", external_app_user_id);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return  responseMap;
    }


    @RequestMapping("/box/auth/jwt/app-user")
    public Map<Object, Object> getAppUserConnection(@RequestParam String external_app_user_id) {
        Map<Object, Object> responseMap = new HashMap<>();
        try{
            // Get a service account connection
            this.getServiceAccountConnection();

            // With the service account find a user id with a specific external_app_user_id
            Iterable<BoxUser.Info> boxUserIter = BoxUser.getAppUsersByExternalAppUserID(serviceAccountConnection, external_app_user_id, BoxUser.ALL_FIELDS);
            String appUserId = boxUserIter.iterator().next().getID();

            // Get an app user connection
            appUserConnection = BoxDeveloperEditionAPIConnection.getAppUserConnection(appUserId, boxConfig, accessTokenCache);

            // Get the current user
            BoxUser.Info userInfo = BoxUser.getCurrentUser(appUserConnection).getInfo();

            // Create response map to return
            responseMap.put("user_id", userInfo.getID());
            responseMap.put("login", userInfo.getLogin());
            responseMap.put("name", userInfo.getName());
            responseMap.put("external_app_user_id", external_app_user_id);
            responseMap.put("access_token", appUserConnection.getAccessToken());
            responseMap.put("expires_in", appUserConnection.getExpires());
            responseMap.put("refresh_token", appUserConnection.getRefreshToken());
            responseMap.put("last_refresh", appUserConnection.getLastRefresh());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return responseMap;
    }
}
