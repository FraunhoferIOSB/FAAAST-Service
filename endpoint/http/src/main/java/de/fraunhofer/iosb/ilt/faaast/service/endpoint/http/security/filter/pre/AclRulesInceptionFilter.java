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

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.acl.repository.AclRepository;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;


/**
 * Helper filter to inject the current ACL rules into a request.
 */
public class AclRulesInceptionFilter extends AbstractAclFilter {

    private final AclRepository aclRepository;

    /**
     * Class constructor.
     *
     * @param aclRepository Retrieval of ACL
     */
    public AclRulesInceptionFilter(AclRepository aclRepository) {
        this.aclRepository = aclRepository;
    }


    @Override
    protected List<AccessPermissionRule> doFilter(HttpServletRequest request, List<AccessPermissionRule> rules) {
        return aclRepository.getAccessPermissionRules();
    }
}
