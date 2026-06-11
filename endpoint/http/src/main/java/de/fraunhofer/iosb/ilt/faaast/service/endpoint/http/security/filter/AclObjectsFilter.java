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

import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.GET;

import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;


/**
 * Filters applicable AAS ACL rules using the incoming request's path.
 */
public class AclObjectsFilter extends AbstractAclFilter {

    private static final String identifiableObjectSplitRegex = "^(?:IDENTIFIABLE\\s+)?(\\$(?:aas|sm|cd))\\s*\\(\\s*\"([^\"]*)\"\\s*\\)$";
    private static final String referableObjectSplitRegex = "^(?:REFERABLE\\s+)?(\\$sme)\\s*\\(\\s*\"([^\"]*)\"\\s*\\)\\.(.+)$";

    private final Map<String, String> identifiableToPathMapping = Map.of("$aas", "shells",
            "$sm", "submodels",
            "$cd", "concept-descriptions",
            "$sme", "submodels");

    @Override
    protected List<AccessPermissionRule> doFilter(HttpServletRequest request, List<AccessPermissionRule> rules) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        List<AccessPermissionRule> filteredRules = new ArrayList<>();

        for (AccessPermissionRule rule: rules) {

            boolean anyMatch = rule.getObjects().stream().anyMatch(objectItem -> {
                if (objectItem.getRoute() != null) {
                    return checkRoute(objectItem.getRoute(), path);
                }
                else if (objectItem.getIdentifiable() != null) {
                    return checkIdentifiable(path, objectItem.getIdentifiable(), method);
                }
                else if (objectItem.getReferable() != null) {
                    return checkReferable(path, objectItem.getReferable(), method);
                }
                else if (objectItem.getFragment() != null) {
                    // TODO Objects to be protected are either
                    //  API Routes, Identifiables, Referables, Descriptors
                    //                                  or
                    //  Fragments of all those (e.g. AssetId, SemanticId, SpecificAssetId).
                    //      -> What to do
                    return false;
                }
                else if (objectItem.getDescriptor() != null) {
                    // don't have descriptors
                    return false;
                }
                return false;
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
        // Escape all regex metacharacters, then turn '*' into '.*'
        String regex = "^" + Pattern.quote(route).replace("\\*", ".*") + "$";
        return Pattern.compile(regex).matcher(requestPath).matches();
    }


    private boolean checkIdentifiable(String identifiable, String requestPath, String method) {
        // ["$aas/sm/cd", identifier]
        String[] identifiableObjectSegments = identifiable.split(identifiableObjectSplitRegex);
        if (identifiableObjectSegments.length != 2) {
            return false;
        }

        // requestPath does not lead or end with /
        String[] requestPathSegments = requestPath.split("/");
        String identifiableMarker = identifiableObjectSegments[0];
        short actualResourceSegment = 0;

        if (requestPathSegments[0].equals("lookup") && requestPathSegments.length > 1) {
            // /lookup/shells/{aasIdentifier}
            actualResourceSegment = 1;
        }

        if (identifiableObjectSegments[0].equals("$sm") && requestPathSegments[0].equals("shells") && requestPathSegments.length > 2) {
            // /shells/{aasIdentifier}/submodels/{submodelIdentifier}
            actualResourceSegment = 2;
        }

        if (!requestPathSegments[actualResourceSegment].equals(identifiableToPathMapping.get(identifiableMarker))) {
            return false;
        }

        return checkIdentifierInstanceOrAll(identifiableObjectSegments[1], requestPathSegments, method);
    }


    private boolean checkReferable(String referable, String requestPath, String method) {
        // ["$sme", identifier, idShortPath]
        String[] referableObjectSegments = referable.split(referableObjectSplitRegex);
        if (referableObjectSegments.length != 3) {
            return false;
        }

        // requestPath does not lead or end with /
        String[] requestPathSegments = requestPath.split("/");
        String identifiableMarker = referableObjectSegments[0];
        short actualResourceSegment = 0;

        if (requestPathSegments[0].equals("shells") && requestPathSegments.length > 2) {
            // /shells/{aasIdentifier}/submodels/{submodelIdentifier}
            actualResourceSegment = 2;
        }

        if (!requestPathSegments[actualResourceSegment].equals(identifiableToPathMapping.get(identifiableMarker))) {
            return false;
        }

        if (checkIdentifierInstanceOrAll(referableObjectSegments[1], requestPathSegments, method)) {
            return true;
        }

        if (requestPathSegments.length < 4) {
            // all submodel elements or more requested
            return method.equalsIgnoreCase(GET.name());
        }

        // IdShortPath matches
        return IdShortPath.parse(requestPathSegments[3]).equals(IdShortPath.parse(referableObjectSegments[2]));
    }


    private boolean checkIdentifierInstanceOrAll(String identifier, String[] requestPathSegments, String httpMethod) {
        // grammar states that wildcard is surrounded by escaped quotes, identifiers are not
        if (identifier.equals("\"*\"")) {
            return true;
        }
        if (requestPathSegments.length < 2) {
            // Specific identifiable permitted, all requested -> Will be filtered at persistence
            // If requests tries to manipulate state, wildcard or the id to manipulate needs to be present
            return httpMethod.equalsIgnoreCase(GET.name());
        }
        return requestPathSegments[1].equals(Objects.requireNonNull(EncodingHelper.base64Encode(identifier)));
    }
}
