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

import com.auth0.jwt.interfaces.Claim;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessPermissionRule;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Inject claims and global attributes into the remaining ACL rules.
 */
public class AclAttributeInjectionInterceptor extends AbstractAclFilter {
    @Override
    protected List<AccessPermissionRule> doFilter(HttpServletRequest request, List<AccessPermissionRule> rules) {
        // TODO do this in request handler servlet
        Map<String, Claim> claims = extractClaims(request);
        for (AccessPermissionRule rule: rules) {
            rule.formula().evaluatePartially(new EvaluationContext(
                    claims.entrySet().stream().map(e -> Map.entry(e.getKey(), e.getValue().toString())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                    request.getServletPath()));
        }
        return rules;
    }
}
