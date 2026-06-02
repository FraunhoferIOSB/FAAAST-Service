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
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util.AccessControlListHelper.getAcl;

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
public class AclAttributeFilter extends JwtAuthorizationFilter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        Map<String, Claim> claims = Optional.ofNullable(extractAndDecodeJwt(((HttpServletRequest) request)).getClaims()).orElse(Map.of());
        AllAccessPermissionRules acl = (AllAccessPermissionRules) request.getAttribute(ACL.getName());

        acl.getRules().removeIf(rule -> getAcl(rule, acl).getAttributes().stream()
                .noneMatch(attributeItem -> {
                    // claim, global and reference should be subtypes of AttributeItem...
                    if (AttributeItem.Global.ANONYMOUS == attributeItem.getGlobal()) {
                        return true;
                    }
                    else if (attributeItem.getClaim() != null) {
                        return claims.containsKey(attributeItem.getClaim());
                    }
                    return true;
                }));

        request.setAttribute(ACL.getName(), acl);

        chain.doFilter(request, response);
    }
}
