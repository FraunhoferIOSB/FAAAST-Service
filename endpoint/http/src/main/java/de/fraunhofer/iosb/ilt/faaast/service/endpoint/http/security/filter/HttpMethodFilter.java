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

import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.RightsEnum;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;


/**
 * Filters applicable AAS ACL rules using the incoming request's HTTP method.
 */
public class HttpMethodFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        String method = ((HttpServletRequest) request).getMethod();

        AllAccessPermissionRules filteredAcl = (AllAccessPermissionRules) request.getAttribute(ACL.getName());

        String requiredRight = isOperationRequest(method, ((HttpServletRequest) request).getContextPath()) ? "EXECUTE" : getRequiredRight(method);

        filteredAcl.getRules().removeIf(
                rules -> rules.getAcl().getRights().contains(RightsEnum.ALL) || rules.getAcl().getRights().contains(RightsEnum.valueOf(requiredRight)));

        request.setAttribute(ACL.getName(), filteredAcl);
        chain.doFilter(request, response);
    }


    private static boolean isOperationRequest(String method, String path) {
        // Requirements: POST and URL suffix: invoke, invoke-async, invoke/$value, invoke-async/$value
        String cleanPath;
        String[] pathParts = path.split("/");

        if (pathParts.length > 1 && "$value".equals(pathParts[pathParts.length - 1])) {
            cleanPath = pathParts[pathParts.length - 2];
        }
        else {
            cleanPath = pathParts[pathParts.length - 1];
        }

        return HttpMethod.POST.name().equals(method) && ("invoke".equals(cleanPath) || "invoke-async".equals(cleanPath));
    }


    private static String getRequiredRight(String method) {
        return switch (method) {
            case "GET" -> "READ";
            case "POST" -> "CREATE";
            case "PUT" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
    }

}
