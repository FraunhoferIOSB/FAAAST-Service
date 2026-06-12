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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter;

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.acl.repository.AclRepository;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Objects;


/**
 * Helper filter to inject the current ACL rules into a request.
 */
public class AclRulesInceptionFilter extends AbstractAclFilter {

    private static final String RULES_ALREADY_PRESENT_TEMPLATE = "%s called after ACL rules were injected into request: %s";

    private final AclRepository aclRepository;

    /**
     * Class constructor.
     *
     * @param aclRepository Retrieval of ACL
     */
    public AclRulesInceptionFilter(AclRepository aclRepository) {
        this.aclRepository = Objects.requireNonNull(aclRepository);
    }


    @Override
    protected List<AccessPermissionRule> doFilter(HttpServletRequest request, List<AccessPermissionRule> rules) {
        if (rules != null) {
            throw new IllegalStateException(String.format(RULES_ALREADY_PRESENT_TEMPLATE, AclRulesInceptionFilter.class.getName(), rules));
        }
        return aclRepository.getAccessPermissionRules();
    }
}
