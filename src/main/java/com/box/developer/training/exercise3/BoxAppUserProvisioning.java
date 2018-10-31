package com.box.developer.training.exercise3;


import com.box.sdk.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

@RestController
public class BoxAppUserProvisioning {
    private static final int MAX_CACHE_ENTRIES = 100;

    private BoxConfig boxConfig = null;
    private IAccessTokenCache accessTokenCache = null;
    private BoxDeveloperEditionAPIConnection serviceAccountConnection = null;
    private BoxDeveloperEditionAPIConnection appUserConnection = null;


    public BoxAppUserProvisioning() {
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

    @CrossOrigin(origins = "http://localhost:8080")
    @RequestMapping("/box/users/app-user")
    public Map<Object, Object> getAppUser(@RequestParam String name, @RequestParam String external_app_user_id){
        Map<Object, Object> responseMap = new HashMap<>();
        try{
            // Create a service account connection
            serviceAccountConnection = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig, accessTokenCache);

            // With the service account find a user id with a specific external_app_user_id
            Iterable<BoxUser.Info> boxUserIter = BoxUser.getAppUsersByExternalAppUserID(serviceAccountConnection, external_app_user_id, BoxUser.ALL_FIELDS);

            // Check if the users already exists in the iterator
            BoxUser.Info userInfo = null;
            if(boxUserIter.iterator().hasNext()) {
                String appUserId = boxUserIter.iterator().next().getID();

                // Get an app user connection
                appUserConnection = BoxDeveloperEditionAPIConnection.getAppUserConnection(appUserId, boxConfig, accessTokenCache);

                // Get the current user
                userInfo = BoxUser.getCurrentUser(appUserConnection).getInfo();
            }
            // If the user does not exist, lets create an app user
            else {
                // Create user params: -1 = unlimited storage
                CreateUserParams userParams = new CreateUserParams();
                userParams.setSpaceAmount(-1);
                userParams.setExternalAppUserId(external_app_user_id);
                System.out.println("Found external app user id: " + external_app_user_id);

                // Create the new app user
                userInfo = BoxUser.createAppUser(serviceAccountConnection, name, userParams);
            }
            // Create response map to return
            responseMap.put("user_id", userInfo.getID());
            responseMap.put("login", userInfo.getLogin());
            responseMap.put("name", userInfo.getName());
            responseMap.put("external_app_user_id", external_app_user_id);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return  responseMap;
    }
}
