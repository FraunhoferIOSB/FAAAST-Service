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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.auth.SharedAttributes.ACL;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util.AccessControlListHelper.getFormula;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util.ExpressionInjectionHelper.injectLogicalExpression;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Inject claims into ACL formula.
 */
public class AclClaimInjectionFilter extends JwtAuthorizationFilter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        AllAccessPermissionRules acl = (AllAccessPermissionRules) request.getAttribute(ACL.getName());
        Map<String, String> claims = extractAndDecodeJwt((HttpServletRequest) request).getClaims().entrySet().stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().asString()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        acl.getRules().forEach(rule -> injectLogicalExpression(getFormula(rule, acl), claims));

        request.setAttribute(ACL.getName(), acl);
        chain.doFilter(request, response);
    }

}
