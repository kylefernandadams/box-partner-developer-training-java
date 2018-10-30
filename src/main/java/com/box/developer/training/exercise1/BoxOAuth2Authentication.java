package com.box.developer.training.exercise1;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxUser;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@RestController
public class BoxOAuth2Authentication {
    private static final String BOX_AUTH_URI = "https://account.box.com/api/oauth2/authorize";
    private static final String BOX_CLIENT_ID = "";
    private static final String BOX_CLIENT_SECRET = "";
    private static final String REDIRECT_URI = "http://localhost:8080/box/auth/oauth/redirect";

    @RequestMapping("/box/auth/oauth")
    public void startOAuthFlow(HttpServletResponse response) {
        try {
            // Create the box authorization Url
            String boxRedirectUrl = BOX_AUTH_URI
                + "?response_type=code"
                + "&client_id=" + BOX_CLIENT_ID
                + "&redirect_uri=" + REDIRECT_URI;

            // Redirect to the Box authorization endpoint
            response.sendRedirect(boxRedirectUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @RequestMapping("/box/auth/oauth/redirect")
    public Map<Object, Object> handleOAuthRedirect(@RequestParam String code) {
        Map<Object, Object> responseMap = new HashMap<>();
        try{
            // Create BoxAPIConnection with code returned from the Box Callback
            BoxAPIConnection api = new BoxAPIConnection(BOX_CLIENT_ID, BOX_CLIENT_SECRET, code);
            BoxUser.Info userInfo = BoxUser.getCurrentUser(api).getInfo();

            // Create response map to return
            responseMap.put("user_id", userInfo.getID());
            responseMap.put("login", userInfo.getLogin());
            responseMap.put("name", userInfo.getName());
            responseMap.put("access_token", api.getAccessToken());
            responseMap.put("expires_in", api.getExpires());
            responseMap.put("refresh_token", api.getRefreshToken());
            responseMap.put("last_refresh", api.getLastRefresh());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return responseMap;
    }
}
