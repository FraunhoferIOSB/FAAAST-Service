/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.acl.repository.file;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.acl.repository.AclRepository;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileAclRepository implements AclRepository, DirectoryWatcherListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileAclRepository.class);
    private final Map<Path, AllAccessPermissionRules> aclList;
    private final ObjectMapper mapper;

    private FileAclRepository() {
        this.aclList = new HashMap<>();
        this.mapper = new ObjectMapper();
    }


    /**
     * Creates a FileAclRepository.
     *
     * @param aclFolder Directory where the observed access control rules live.
     * @return New instance of a FileAclRepository.
     * @throws EndpointException if initializing the ACL memory representation fails
     */
    public static FileAclRepository createNewInstance(String aclFolder) throws EndpointException {
        FileAclRepository instance = new FileAclRepository();
        try (DirectoryWatcher watcher = new DirectoryWatcher(Paths.get(aclFolder))) {
            watcher.addListener(instance);
            return instance;
        }
        catch (IOException e) {
            throw new EndpointException(e);
        }
    }


    /**
     * Get all currently observed rules.
     *
     * @return All access control rules.
     */
    public AllAccessPermissionRules getAllAccessPermissionRules() {
        var rules = new AllAccessPermissionRules();
        for(AllAccessPermissionRules fileRules : aclList.values()) {
            rules.setRules(fileRules.getRules());
            rules.setDefacls(fileRules.getDefacls());
            rules.setDefattributes(fileRules.getDefattributes());
            rules.setDefformulas(fileRules.getDefformulas());
            rules.setDefobjects(fileRules.getDefobjects());
        }

        return rules;
    }


    @Override
    public void onFileCreated(Path path) {
        LOGGER.debug("Added ACL {}", path);
        aclList.put(path, readFile(path));
    }


    @Override
    public void onFileDeleted(Path path) {
        LOGGER.debug("Removed ACL {}", path);
        aclList.remove(path);
    }


    @Override
    public void onFileModified(Path path) {
        LOGGER.debug("Changed ACL {}", path);
        aclList.put(path, readFile(path));
    }


    private AllAccessPermissionRules readFile(Path path) {
        try {
            JsonNode rootNode = mapper.readTree(path.toFile());
            if (rootNode.has("AllAccessPermissionRules")) {
                return mapper.treeToValue(rootNode.get("AllAccessPermissionRules"), AllAccessPermissionRules.class);
            }
            else {
                return mapper.readValue(rootNode.toString(), AllAccessPermissionRules.class);
            }
        }
        catch (IOException e) {
            throw new IllegalStateException(String.format("Could not parse latest addition to ACL folder: %s", path), e);
        }
    }

}
