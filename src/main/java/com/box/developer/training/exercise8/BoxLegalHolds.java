package com.box.developer.training.exercise8;

import com.box.sdk.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileReader;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class BoxLegalHolds {
    private static final int MAX_CACHE_ENTRIES = 100;

    private BoxConfig boxConfig = null;
    private IAccessTokenCache accessTokenCache = null;
    private BoxDeveloperEditionAPIConnection serviceAccountConnection = null;

    private static final String LEGAL_HOLD_POLICY_NAME = "Legal_Hold_DEMO";

    private static final String ACCOUNT_METADATA_TEMPLATE_KEY = "account";
    private static final String ACCOUNT_ID_KEY = "accountId";
    private static final String ACCOUNT_ID_VALUE = "123456";

    public BoxLegalHolds() {
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

    @RequestMapping("/box/governance/legal-holds")
    public Map<Object, Object> createLegalHold() {
        Map<Object, Object> responseMap = new HashMap<>();
        try{
            // Create a service account connection
            serviceAccountConnection = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig, accessTokenCache);

            // Get Legal Hold start and end dates
            LocalDate now = LocalDate.now();
            Date start = Date.valueOf(now.minusDays(2));
            Date end = Date.valueOf(now);

            // Create Legal Hold Policy
            BoxLegalHoldPolicy.Info boxLegalHoldPolicyInfo = BoxLegalHoldPolicy.create(serviceAccountConnection, LEGAL_HOLD_POLICY_NAME, "Demo Legal Hold", start, end);
            BoxLegalHoldPolicy boxLegalHoldPolicy = new BoxLegalHoldPolicy(serviceAccountConnection, boxLegalHoldPolicyInfo.getID());

            // Search for files using metadata
            // Note: With Box there can be an search indexing delay of up to 10mins
            BoxSearch boxSearch = new BoxSearch(serviceAccountConnection);

            // Create metadata filter for search
            BoxMetadataFilter boxMetadataFilter = new BoxMetadataFilter();
            boxMetadataFilter.setTemplateKey(ACCOUNT_METADATA_TEMPLATE_KEY);
            boxMetadataFilter.addFilter(ACCOUNT_ID_KEY, ACCOUNT_ID_VALUE);

            // Add the metadata filter to the search parameters
            BoxSearchParameters boxSearchParameters = new BoxSearchParameters();
            boxSearchParameters.setMetadataFilter(boxMetadataFilter);
            long offset = 0;
            long limit = 10;

            // Execute the search and loop through the results
            PartialCollection<BoxItem.Info> searchResults = boxSearch.searchRange(offset, limit, boxSearchParameters);
            searchResults.stream().forEach(result -> {
                if(result instanceof BoxFile.Info) {
                    System.out.println("Found search result with id: " + result.getID() + " and name: " + result.getName());
                    BoxFile boxFile = new BoxFile(serviceAccountConnection, result.getID());
                    boxLegalHoldPolicy.assignTo(boxFile);
                }
            });

            // Create response map to return
            responseMap.put("legal_hold_policy_id", boxLegalHoldPolicyInfo.getID());
            responseMap.put("legal_hold_policy_name", boxLegalHoldPolicyInfo.getPolicyName());
            responseMap.put("legal_hold_policy_created_by", boxLegalHoldPolicyInfo.getCreatedBy().getLogin());
            responseMap.put("legal_hold_policy_created_at", boxLegalHoldPolicyInfo.getCreatedAt());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return responseMap;
    }
}
