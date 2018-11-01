package com.box.developer.training.exercise7;

import com.box.sdk.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class BoxMetadataDrivenRetention {
    private static final int MAX_CACHE_ENTRIES = 100;

    private BoxConfig boxConfig = null;
    private IAccessTokenCache accessTokenCache = null;
    private BoxDeveloperEditionAPIConnection serviceAccountConnection = null;

    private static final String ACCOUNT_METADATA_TEMPLATE_KEY = "account";
    private static final String ACCOUNT_STATUS_FIELD_KEY = "accountStatus";
    private static final String ACCOUNT_STATUS_OPTION_ID = "d28951ec-d9c7-4dc1-b443-561bd0146d5b";

    private static final String RETENTION_POLICY_NAME = "RETENTION-DEMO-1";
    private static final int RETENTION_POLICY_DURATION = 5;

    public BoxMetadataDrivenRetention() {
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

    @RequestMapping("/box/governance/retention")
    public Map<Object, Object> createMetadataDrivenRetentionPolicy() {
        Map<Object, Object> responseMap = new HashMap<>();
        try{
            // Create a service account connection
            serviceAccountConnection = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig, accessTokenCache);

            // Create Retention Policy Parameters
            RetentionPolicyParams optionalParams = new RetentionPolicyParams();
            optionalParams.setCanOwnerExtendRetention(true);
            optionalParams.setAreOwnersNotified(true);

            // Create Retention Policy
            BoxRetentionPolicy.Info boxRetentionPolicyInfo = BoxRetentionPolicy.createFinitePolicy(
                    serviceAccountConnection,
                    RETENTION_POLICY_NAME,
                    RETENTION_POLICY_DURATION,
                    BoxRetentionPolicy.ACTION_REMOVE_RETENTION);

            // Get the Account metadata template
            MetadataTemplate accountMetadataTemplate = MetadataTemplate.getMetadataTemplate(serviceAccountConnection, ACCOUNT_METADATA_TEMPLATE_KEY);
            System.out.println("Found account id md template: " + accountMetadataTemplate.getID());

            // Loop through the metadata field and get the field id for the account status field
            // NOTE: There is a bug in the Java SDK where the optinons of a dropdown field is not returning the id for the enum
            // To ge the ID, you can issue a REST call in your terminal:
            // curl https://api.box.com/2.0/metadata_templates/enterprise/account/schema -H "Authorization: Bearer ACCESS_TOKEN"
            AtomicReference<String> accountStatusFieldId = new AtomicReference<String>();
            accountMetadataTemplate.getFields().forEach(field -> {
                System.out.println("Found field display name: "
                        + field.getDisplayName()
                        + " and key: "
                        + field.getKey() + " id: "
                        + field.getID());
                if(field.getKey().equalsIgnoreCase(ACCOUNT_STATUS_FIELD_KEY)) {
                    accountStatusFieldId.set(field.getID());
                }
            });

            // Create a metadata field field
            MetadataFieldFilter metadataFieldFilter = new MetadataFieldFilter(accountStatusFieldId.get(), ACCOUNT_STATUS_OPTION_ID);

            // Get the BoxRetentionPolicy object and assign it to a metadata template
            BoxRetentionPolicy boxRetentionPolicy = new BoxRetentionPolicy(serviceAccountConnection, boxRetentionPolicyInfo.getID());
            BoxRetentionPolicyAssignment.Info boxRetentionPolicyAssignmentInfo = boxRetentionPolicy.assignToMetadataTemplate(accountMetadataTemplate.getID(), metadataFieldFilter);

            // Create response map to return
            responseMap.put("retention_policy_assignment_id", boxRetentionPolicyAssignmentInfo.getID());
            responseMap.put("retention_policy_assigned_at", boxRetentionPolicyAssignmentInfo.getAssignedAt());
            responseMap.put("retention_policy_assigned_by", boxRetentionPolicyAssignmentInfo.getAssignedBy().getLogin());
            responseMap.put("retention_policy_assigned_to_id", boxRetentionPolicyAssignmentInfo.getAssignedToID());
            responseMap.put("retention_policy_assigned_to_type", boxRetentionPolicyAssignmentInfo.getAssignedToType());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return responseMap;
    }
}
