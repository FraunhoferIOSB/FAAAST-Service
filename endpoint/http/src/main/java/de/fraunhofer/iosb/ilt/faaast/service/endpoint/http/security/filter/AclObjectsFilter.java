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

import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.AccessObject;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Filters applicable AAS ACL rules using the incoming request's path.
 */
public class AclObjectsFilter extends AbstractAclFilter {

    private final String pathPrefix;

    public AclObjectsFilter(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }


    @Override
    protected List<AccessPermissionRule> doFilter(HttpServletRequest request, List<AccessPermissionRule> rules) {
        String path = request.getPathInfo().replaceFirst(pathPrefix, "");

        List<AccessPermissionRule> filteredRules = new ArrayList<>();

        for (AccessPermissionRule rule: rules) {
            List<AccessObject> attributes = rule.objects();

            boolean anyMatch = attributes.stream().anyMatch(attribute -> {
                if (attribute.isRoute()) {
                    return checkRoute(attribute.asRoute().route(), path);
                }
                else {
                    // This is only a shortcut for checking route.
                    return true;
                }
            });

            if (anyMatch) {
                filteredRules.add(rule);
            }
        }

        return filteredRules;
    }


    private boolean checkRoute(String route, String requestPath) {
        if (route == null) {
            return false;
        }
        return toRegex(route).matcher(requestPath).matches();
    }


    private static Pattern toRegex(String routePattern) {
        return Pattern.compile(routePattern.replaceAll("\\*", ".*"));
    }
}
