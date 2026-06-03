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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.acl.repository;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;

import java.util.List;


/**
 * Keeps an in-memory version of the aclFolder's rules. When access control rules are added/deleted/modified, updates
 * its own state accordingly.
 */
public interface AclRepository {
    /**
     * Get the fully resolved AccessPermissionRules, i.e. USE(ACL|ATTRIBUTE|...) are already replaced by their DEF*
     * counterparts. For file-based repositories, this is the merged
     * versions of all ACL files.
     *
     * @return All resolved AccessPermissionRules.
     */
    List<AccessPermissionRule> getAccessPermissionRules();


    /**
     * Add an environment to the current ACL and resolve all DEF* into the AccessPermissionRule list.
     *
     * @param allAccessPermissionRules Rule environment to add.
     */
    void addAndResolve(AllAccessPermissionRules allAccessPermissionRules);


    /**
     * Remove an environment from the current ACL along with its DEF*.
     *
     * @param allAccessPermissionRules Rule environment to remove.
     */
    void remove(AllAccessPermissionRules allAccessPermissionRules);

}
