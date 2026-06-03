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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util.ExpressionInjectionHelper.injectLogicalExpression;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import jakarta.servlet.http.HttpServletRequest;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Inject claims into ACL formula.
 */
public class AclClaimInjectionFilter extends AbstractAclFilter {
    @Override
    protected List<AccessPermissionRule> doFilter(HttpServletRequest request, List<AccessPermissionRule> acl) {
        Map<String, String> claims = extractAndDecodeJwt(request).getClaims().entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().asString()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        acl.forEach(rule -> injectLogicalExpression(rule.getFormula(), claims));

        return acl;
    }

}
