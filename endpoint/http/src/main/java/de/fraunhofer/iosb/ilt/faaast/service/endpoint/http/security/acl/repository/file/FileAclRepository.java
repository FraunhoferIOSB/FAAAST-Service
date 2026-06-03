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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.acl.repository.file;

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util.AccessControlListRulesValidator.validate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.acl.repository.AbstractAclRepository;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * File implementation of an ACL Repository. Connected to a file system monitoring service.
 */
public class FileAclRepository extends AbstractAclRepository implements DirectoryWatcherListener {
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
        DirectoryWatcher watcher;
        try {
            watcher = new DirectoryWatcher(Paths.get(aclFolder));
            watcher.addListener(instance);
            return instance;
        }
        catch (IOException e) {
            throw new EndpointException(e);
        }
    }


    @Override
    public void onFileCreated(Path path) {
        LOGGER.debug("Adding ACL {}", path);
        update(path);
    }


    @Override
    public void onFileDeleted(Path path) {
        LOGGER.debug("Removing ACL {}", path);
        remove(aclList.get(path));
    }


    @Override
    public void onFileModified(Path path) {
        LOGGER.debug("Changing ACL {}", path);
        update(path);
    }


    private void update(Path path) {
        AllAccessPermissionRules acl = readFile(path);
        if (acl != null && acl.getRules().stream().allMatch(rule -> validate(rule, acl))) {
            aclList.put(path, acl);
            addAndResolve(acl);
        }
        else {
            LOGGER.warn("Tried to load invalid ACL: {}.", path);
        }
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
            LOGGER.warn("Could not parse latest addition to ACL folder: {}", path, e);
            return null;
        }
    }
}
