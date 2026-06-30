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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util;

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util.AccessControlListHelper.getAcl;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util.AccessControlListHelper.getAttributes;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util.AccessControlListHelper.getObjects;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Acl;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to validate ACL rules.
 */
public class AccessControlListRulesValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessControlListRulesValidator.class);

    private AccessControlListRulesValidator() {}


    /**
     * Validates AccessPermissionRule. Checks if any of the required fields are null. Does not check semantic validity of
     * the rule!
     *
     * @param rule Rule to check.
     * @param allAccessPermissionRules Rule environment.
     * @return If the Rule is valid, i.e. contains ACL, ATTRIBUTES, RIGHTS, OBJECTS, either directly or via (resolvable)
     *         USE*.
     */
    public static boolean validate(AccessPermissionRule rule, AllAccessPermissionRules allAccessPermissionRules) {
        Acl acl = getAcl(rule, allAccessPermissionRules);
        try {
            return acl != null &&
                    getAttributes(acl, allAccessPermissionRules) != null &&
                    acl.getRights() != null &&
                    getObjects(rule, allAccessPermissionRules) != null;
        }
        catch (IllegalArgumentException illegalArgumentException) {
            LOGGER.warn("Validation failed: {}", illegalArgumentException.getMessage(), illegalArgumentException);
            return false;
        }
    }
}
