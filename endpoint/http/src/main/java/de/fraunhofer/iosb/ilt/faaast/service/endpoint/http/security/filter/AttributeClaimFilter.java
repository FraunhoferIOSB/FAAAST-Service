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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.auth.SharedAttributes.ACL;

import com.auth0.jwt.interfaces.Claim;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;


/**
 * Filters applicable AAS ACL rules using the incoming request's bearer token claims.
 */
public class AttributeClaimFilter extends JwtAuthorizationFilter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        Map<String, Claim> claims = Optional.ofNullable(extractAndDecodeJwt(((HttpServletRequest) request)).getClaims()).orElse(Map.of());
        AllAccessPermissionRules filteredAcl = (AllAccessPermissionRules) request.getAttribute(ACL.getName());

        filteredAcl.getRules().removeIf(rule -> rule.getAcl().getAttributes().stream()
                .anyMatch(attributeItem -> {
                    // claim, global and reference should be subtypes of AttributeItem...
                    if (attributeItem.getGlobal().equals(AttributeItem.Global.ANONYMOUS)) {
                        return false;
                    }
                    else if (attributeItem.getClaim() != null) {
                        return !claims.containsKey(attributeItem.getClaim());
                    }
                    return false;
                }));

        request.setAttribute(ACL.getName(), filteredAcl);

        chain.doFilter(request, response);
    }
}
