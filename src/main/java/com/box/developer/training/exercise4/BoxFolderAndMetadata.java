package com.box.developer.training.exercise4;

import com.box.sdk.*;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
public class BoxFolderAndMetadata {
    private static final int MAX_CACHE_ENTRIES = 100;

    private BoxConfig boxConfig = null;
    private IAccessTokenCache accessTokenCache = null;
    private BoxDeveloperEditionAPIConnection serviceAccountConnection = null;

    private static final String LOCAL_FILE_PATH = "/Users/kadams/Desktop/Box Developer Training/20728235.jpg";

    public BoxFolderAndMetadata() {
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

    @RequestMapping("/box/folders/create")
    public Map<Object, Object> createFolder(@RequestParam String name) {
        Map<Object, Object> responseMap = new HashMap<>();
        try{
            // Create a service account connection
            serviceAccountConnection = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig, accessTokenCache);

            // Get Root Folder
            BoxFolder rootFolder = BoxFolder.getRootFolder(serviceAccountConnection);

            // Create a new folder
            BoxFolder.Info boxFolderInfo = rootFolder.createFolder(name);

            // Create response map to return
            responseMap.put("folder_id", boxFolderInfo.getID());
            responseMap.put("folder_name", boxFolderInfo.getName());
            responseMap.put("parent_id", boxFolderInfo.getParent().getID());
            responseMap.put("created_by", boxFolderInfo.getCreatedBy().getLogin());
            responseMap.put("created_at", boxFolderInfo.getCreatedAt());
            responseMap.put("last_modified_by", boxFolderInfo.getModifiedBy().getLogin());
            responseMap.put("last_modified_at", boxFolderInfo.getModifiedAt());
            responseMap.put("is_watermarked", boxFolderInfo.getIsWatermarked());
            responseMap.put("size", boxFolderInfo.getSize());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return responseMap;
    }

    @RequestMapping("/box/metadata/templates")
    public Map<Object, Object> getMetadataTemplate() {
        Map<Object, Object> responseMap = new HashMap<>();
        try{
            // Create a service account connection
            serviceAccountConnection = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig, accessTokenCache);

            MetadataTemplate.getEnterpriseMetadataTemplates(serviceAccountConnection).forEach(metadataTemplate -> {
                responseMap.put("md_template_" + metadataTemplate.getID(), metadataTemplate);
            });
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return responseMap;
    }

    @RequestMapping("/box/items/metadata/create")
    public Map<Object, Object> addMetadata(@RequestParam String id) {
        Map<Object, Object> responseMap = new HashMap<>();
        try{
            // Create a service account connection
            serviceAccountConnection = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(boxConfig, accessTokenCache);

            // Get a Box Folder
            BoxFolder boxFolder = new BoxFolder(serviceAccountConnection, id);

            // Upload a file to Box
            FileInputStream fileInputStream = new FileInputStream(LOCAL_FILE_PATH);
            BoxFile.Info boxFileInfo = boxFolder.uploadFile(fileInputStream, "MyUploadedImage.jpg");

            // Get the instance of a file
            BoxFile boxfile = new BoxFile(serviceAccountConnection, boxFileInfo.getID());

            // Add metadata to the file
            String invoiceTemplateKey = "invoice";
            boxfile.createMetadata(invoiceTemplateKey,
                    new Metadata()
                    .add("/invoiceId", "12345")
                    .add("/invoiceStatus", "New"));

            // Metadata Cascade policies are currently in Beta and must be enabled before usage. Otherwise you will receive a 403- Forbidden exception
            // Create a Metadata Cascade Policy on the folder
            // String scope = "enterprise";
            // boxFolder.addMetadataCascadePolicy(scope, invoiceTemplateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return Collections.singletonMap("response", "Folder creation and metadadta added successfully!");
    }
}
