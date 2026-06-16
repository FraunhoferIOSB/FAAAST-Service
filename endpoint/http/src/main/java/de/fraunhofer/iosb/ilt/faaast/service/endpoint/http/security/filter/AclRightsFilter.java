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

import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.CONNECT;
import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.DELETE;
import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.GET;
import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.HEAD;
import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.OPTIONS;
import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.PATCH;
import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.POST;
import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.PUT;
import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.TRACE;

import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.RightsEnum;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;


/**
 * Filters applicable AAS ACL rules using the incoming request's HTTP method.
 */
public class AclRightsFilter extends AbstractAclFilter {

    // EXECUTE is handled via isOperationRequest.
    private static final Map<RightsEnum, List<HttpMethod>> RIGHT_TO_HTTP_METHOD_MAPPING = Map.of(
            RightsEnum.CREATE, List.of(POST, PUT),
            RightsEnum.READ, List.of(GET),
            RightsEnum.UPDATE, List.of(PATCH, PUT),
            RightsEnum.DELETE, List.of(DELETE),
            RightsEnum.VIEW, List.of(GET),
            RightsEnum.EXECUTE, List.of(),
            RightsEnum.ALL, List.of(GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD, TRACE, CONNECT));

    @Override
    protected List<AccessPermissionRule> doFilter(HttpServletRequest request, List<AccessPermissionRule> rules) {
        String method = request.getMethod();
        boolean isOperation = isOperationRequest(method, request.getPathInfo());

        return rules.stream().filter(
                rule -> rule.getAcl().getRights().stream()
                        .anyMatch(right -> isOperation && right == RightsEnum.EXECUTE ||
                                RIGHT_TO_HTTP_METHOD_MAPPING.get(right).stream()
                                        .map(Enum::name)
                                        .anyMatch(m -> m.equalsIgnoreCase(method))))
                .toList();
    }


    private static boolean isOperationRequest(String method, String path) {
        // Requirements: POST and URL suffix: invoke, invoke-async, invoke/$value, invoke-async/$value
        String maybeInvokeKeyword;
        String[] pathParts = path.split("/");

        if (pathParts.length <= 1) {
            return false;
        }

        if ("$value".equals(pathParts[pathParts.length - 1])) {
            maybeInvokeKeyword = pathParts[pathParts.length - 2];
        }
        else {
            maybeInvokeKeyword = pathParts[pathParts.length - 1];
        }

        return POST.name().equalsIgnoreCase(method) && ("invoke".equals(maybeInvokeKeyword) || "invoke-async".equals(maybeInvokeKeyword));
    }
}
