package com.box.developer.training.exercise9;

import com.box.sdk.*;
import org.springframework.web.bind.annotation.*;

import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class BoxTokenExchange {
    private static final int MAX_CACHE_ENTRIES = 100;

    private BoxConfig boxConfig = null;
    private IAccessTokenCache accessTokenCache = null;
    private BoxDeveloperEditionAPIConnection serviceAccountConnection = null;

    public BoxTokenExchange() {
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
    @RequestMapping(value = "/box/auth/token-exchange", method = RequestMethod.POST, consumes = "application/json")
    public Map<Object, Object> exchangeToken(@RequestBody List<String> scopes) {
        Map<Object, Object> responseMap = new HashMap<>();
        try{
            // Create a service account connection
            serviceAccountConnection = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig, accessTokenCache);

            // Get lower scoped token
            ScopedToken scopedToken = serviceAccountConnection.getLowerScopedToken(scopes, null);

            // Create response map to return
            responseMap.put("access_token", scopedToken.getAccessToken());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return responseMap;
    }
}
