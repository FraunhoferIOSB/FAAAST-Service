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

import com.auth0.jwt.interfaces.Claim;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * Filters applicable AAS ACL rules using the incoming request's bearer token claims.
 */
public class AclAttributeFilter extends AbstractAclFilter {

    @Override
    protected List<AccessPermissionRule> doFilter(HttpServletRequest request, List<AccessPermissionRule> acl) {
        Map<String, Claim> claims = Optional.ofNullable(extractAndDecodeJwt(request).getClaims()).orElse(Map.of());

        acl.removeIf(rule -> {
            for (AttributeItem item: rule.getAcl().getAttributes()) {
                if (AttributeItem.Global.ANONYMOUS == item.getGlobal()) {
                    return false;
                }
                else if (item.getReference() != null) {
                    return true;
                }
                else if (item.getClaim() != null && claims.containsKey(item.getClaim())) {
                    continue;
                }
                return true;
            }
            return false;
        });
        return acl;
    }
}
