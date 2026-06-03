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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.pre;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Acl;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import jakarta.servlet.http.HttpServletRequest;

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util.AccessControlListHelper.getAcl;


/**
 * Filters applicable AAS ACL rules using the rules' "Access" field.
 */
public class AclDisabledFilter extends AbstractAclFilter {
    @Override
    public AllAccessPermissionRules doFilter(HttpServletRequest request, AllAccessPermissionRules acl) {
        acl.getRules().removeIf(rule -> getAcl(rule, acl).getAccess() == Acl.Access.DISABLED);
        return acl;
    }
}
