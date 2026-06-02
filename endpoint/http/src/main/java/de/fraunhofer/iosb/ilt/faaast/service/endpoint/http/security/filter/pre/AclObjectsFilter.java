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
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util.AccessControlListHelper.getObjects;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Filters applicable AAS ACL rules using the incoming request's path.
 */
public class AclObjectsFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String path = ((HttpServletRequest) request).getRequestURI();

        AllAccessPermissionRules filteredAcl = (AllAccessPermissionRules) request.getAttribute(ACL.getName());

        List<AccessPermissionRule> filteredRules = new ArrayList<>();

        for (AccessPermissionRule rule: filteredAcl.getRules()) {

            boolean anyMatch = getObjects(rule, filteredAcl).stream().anyMatch(objectItem -> {
                if (objectItem.getRoute() != null) {
                    String route = objectItem.getRoute();
                    // Warning: potentially does not allow trailing slash at requests.
                    return route.contains("*") && path.startsWith(route.substring(0, route.indexOf("*"))) || route.equals(path);
                }
                else if (objectItem.getIdentifiable() != null) {
                    return checkIdentifiable(path, objectItem.getIdentifiable());
                }
                else if (objectItem.getReferable() != null) {
                    return false; // TODO
                }
                else if (objectItem.getFragment() != null) {
                    return false; // TODO
                }
                else if (objectItem.getDescriptor() != null) {
                    return checkDescriptor(path, objectItem.getDescriptor());
                }
                return false;
            });

            if (anyMatch) {
                filteredRules.add(rule);
            }
        }

        filteredAcl.setRules(filteredRules);

        request.setAttribute(ACL.getName(), filteredAcl);
        chain.doFilter(request, response);
    }


    private boolean checkIdentifiable(String path, String identifiable) {
        if (!(path.startsWith("/submodels") || path.startsWith("/shells"))) {
            return false;
        }

        if ("(Submodel)*".equals(identifiable)) {
            return true;
        }
        else if (identifiable.startsWith("(Submodel)")) {
            String id = identifiable.substring(10);
            return path.contains(Objects.requireNonNull(EncodingHelper.base64Encode(id)));
        }
        if ("(AssetAdministrationShell)*".equals(identifiable)) {
            return true;
        }
        else if (identifiable.startsWith("(AssetAdministrationShell)")) {
            String id = identifiable.substring(26);
            return path.contains(Objects.requireNonNull(EncodingHelper.base64Encode(id)));
        }
        return false;
    }


    private static boolean checkDescriptor(String path, String descriptor) {
        if (descriptor.startsWith("(aasDesc)")) {
            if (!path.startsWith("/shell-descriptors")) {
                return false;
            }
            if ("(aasDesc)*".equals(descriptor)) {
                return true;
            }
            else if (descriptor.startsWith("(aasDesc)")) {
                String id = descriptor.substring(9);
                return path.contains(Objects.requireNonNull(EncodingHelper.base64UrlEncode(id)));
            }
        }
        else if (descriptor.startsWith("(smDesc)")) {
            if (!path.startsWith("/submodel-descriptors")) {
                return false;
            }
            if ("(smDesc)*".equals(descriptor)) {
                return true;
            }
            else if (descriptor.startsWith("(smDesc)")) {
                String id = descriptor.substring(8);
                return path.contains(Objects.requireNonNull(EncodingHelper.base64UrlEncode(id)));
            }
        }
        return false;
    }

}
