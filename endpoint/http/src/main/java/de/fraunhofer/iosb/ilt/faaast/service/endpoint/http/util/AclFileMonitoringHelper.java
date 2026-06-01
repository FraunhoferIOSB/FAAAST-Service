package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class AclFileMonitoringHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(AclFileMonitoringHelper.class);
    private final Map<Path, AllAccessPermissionRules> aclList;

    private final String abortMessage = "Invalid ACL folder path, AAS Security will not enforce rules.";
    private final String errorMessage = "Invalid ACL rule, skipping.";


    public AclFileMonitoringHelper(String aclFolder) {
        this.aclList = new HashMap<>();
        initializeAclList(aclFolder);
        monitorAclRules(aclFolder);
    }


    public Collection<AllAccessPermissionRules> getAll() {
        return aclList.values();
    }


    private void initializeAclList(String aclFolder) {
        if (aclFolder == null
                || aclFolder.trim().isEmpty()
                || !new File(aclFolder.trim()).isDirectory()) {
            LOGGER.error(abortMessage);
            return;
        }
        File folder = new File(aclFolder.trim());
        File[] jsonFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        ObjectMapper mapper = new ObjectMapper();
        if (jsonFiles != null) {
            for (File file: jsonFiles) {
                Path filePath = file.toPath();
                try {
                    String jsonContent = Files.readString(filePath);
                    JsonNode rootNode = mapper.readTree(jsonContent);
                    AllAccessPermissionRules allRules;
                    if (rootNode.has("AllAccessPermissionRules")) {
                        allRules = mapper.treeToValue(rootNode.get("AllAccessPermissionRules"), AllAccessPermissionRules.class);
                    }
                    else {
                        allRules = mapper.readValue(jsonContent, AllAccessPermissionRules.class);
                    }
                    aclList.put(filePath, allRules);
                }
                catch (IOException e) {
                    LOGGER.error(errorMessage);
                }
            }
        }
    }


    private void monitorAclRules(String aclFolder) {
        if (aclFolder == null
                || aclFolder.trim().isEmpty()
                || !new File(aclFolder.trim()).isDirectory()) {
            LOGGER.error(abortMessage);
            return;
        }
        Path folderToWatch = Paths.get(aclFolder);
        WatchService watchService;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            // Register the folder with the WatchService for CREATE and DELETE events
            folderToWatch.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            monitorLoop(watchService, folderToWatch);
        }
        catch (IOException e) {
            LOGGER.error(errorMessage);
        }
    }


    private void monitorLoop(WatchService watchService, Path folderToWatch) {
        ObjectMapper mapper = new ObjectMapper();
        Thread monitoringThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey watchKey;
                try {
                    watchKey = watchService.take();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // restore interrupt status
                    LOGGER.warn("ACL monitoring thread interrupted", e);
                    break; // exit loop
                }
                for (WatchEvent<?> event: watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path filePath = (Path) event.context();
                    Path absolutePath = folderToWatch.resolve(filePath).toAbsolutePath();
                    // Check if the file is a JSON file
                    if (filePath.toString().toLowerCase().endsWith(".json")) {
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            try {
                                String jsonContent = Files.readString(absolutePath);
                                JsonNode rootNode = mapper.readTree(jsonContent);
                                AllAccessPermissionRules allRules;
                                if (rootNode.has("AllAccessPermissionRules")) {
                                    allRules = mapper.treeToValue(rootNode.get("AllAccessPermissionRules"), AllAccessPermissionRules.class);
                                }
                                else {
                                    allRules = mapper.readValue(jsonContent, AllAccessPermissionRules.class);
                                }
                                aclList.put(absolutePath, allRules);
                            }
                            catch (IOException e) {
                                LOGGER.error(errorMessage);
                            }
                            LOGGER.info("Added new ACL rule.");
                        }
                        else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            aclList.remove(absolutePath);
                            LOGGER.info("Removed ACL rule.");
                        }
                    }
                }
                boolean valid = watchKey.reset();
                if (!valid) {
                    LOGGER.info("WatchKey no longer valid; exiting.");
                    break;
                }
            }
        });
        monitoringThread.start();
    }


}
