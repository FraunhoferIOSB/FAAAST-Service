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
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.pre.JwtAuthorizationFilter.AUTHORIZATION;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.pre.JwtAuthorizationFilter.BEARER;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.AccessControlListHelper.getAcl;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.AccessControlListHelper.getAttributes;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.AccessControlListHelper.getFormula;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.AccessControlListHelper.getObjects;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.FormulaEvaluator;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.GetAllAssetAdministrationShellsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.GetAllSubmodelsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Acl;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;


/**
 * Filters any incoming request with respect to the given ACL rules.
 */
public class ApiGateway {

    /**
     * Checks if the user is authorized to receive the response of the request.
     *
     * @param request the HttpRequest
     * @return true if authorized and ACL exists
     */
    public boolean isAuthorized(HttpServletRequest request) {
        return AuthServer.filterRules((AllAccessPermissionRules) request.getAttribute(ACL.getName()), extractClaims(request), request);
    }


    /**
     * Filters out AAS that the user is not authorized for.
     *
     * @param request the HttpRequest
     * @param response the ApiResponse
     * @return the ApiResponse with only allowed AAS
     */
    public Response filterAas(HttpServletRequest request, GetAllAssetAdministrationShellsResponse response) {
        // remove AAS if none of the ALL_ACL have any rule that matches
        AllAccessPermissionRules allAccessPermissionRules = (AllAccessPermissionRules) request.getAttribute(ACL.getName());

        response.getPayload().getContent()
                .removeIf(aas -> allAccessPermissionRules.getRules()
                        .stream().noneMatch(r -> AuthServer.evaluateRule(r, "/shells/" + EncodingHelper.base64Encode(aas.getId()),
                                extractClaims(request), allAccessPermissionRules)));
        return response;
    }


    /**
     * Filters out Submodels that the user is not authorized for.
     *
     * @param request the HttpRequest
     * @param response the ApiResponse
     * @return the ApiResponse with only allowed Submodels
     */
    public Response filterSubmodels(HttpServletRequest request, GetAllSubmodelsResponse response) {
        response.getPayload().getContent().removeIf(submodel -> {
            String path = "/submodels/" + EncodingHelper.base64Encode(submodel.getId());
            Map<String, Claim> claims = extractClaims(request);

            Map<String, Object> fieldCtx = new HashMap<>();
            if (submodel.getSemanticId() != null) {
                fieldCtx.put("$sm#semanticId", submodel.getSemanticId().getKeys().get(0).getValue());
            }

            return ((AllAccessPermissionRules) request.getAttribute(ACL.getName())).getRules().stream()
                    .noneMatch(rule -> AuthServer.evaluateRule(rule, path, claims, (AllAccessPermissionRules) request.getAttribute(ACL.getName()), fieldCtx));
        });
        return response;
    }


    /**
     * Filters out the Submodel that the user is not authorized for.
     *
     * @param request the HttpRequest
     * @param response the ApiResponse
     * @return true if user is authorized
     */
    public boolean filterSubmodel(HttpServletRequest request, GetSubmodelResponse response) {
        Submodel submodel = response.getPayload();
        if (Objects.isNull(submodel)) {
            return true;
        }
        String path = "/submodels/" + EncodingHelper.base64Encode(submodel.getId());
        Map<String, Claim> claims = extractClaims(request);

        Map<String, Object> fieldCtx = new HashMap<>();
        if (submodel.getSemanticId() != null) {
            fieldCtx.put("$sm#semanticId", submodel.getSemanticId().getKeys().get(0).getValue());
        }

        return ((AllAccessPermissionRules) request.getAttribute(ACL.getName())).getRules().stream()
                .anyMatch(rule -> AuthServer.evaluateRule(rule, path, claims, (AllAccessPermissionRules) request.getAttribute(ACL.getName()), fieldCtx));
    }


    private Map<String, Claim> extractClaims(HttpServletRequest request) {
        var authHeaderValue = request.getHeader(AUTHORIZATION);

        if (authHeaderValue == null || !authHeaderValue.startsWith(BEARER.concat(" "))) {
            return null;
        }

        // Remove "Bearer "
        String token = authHeaderValue.substring(BEARER.length()).trim();

        return JWT.decode(token).getClaims();
    }

    /**
     * Simple whitelist AuthServer implementation that supports ANONYMOUS access, claims with simple eq formulas and route
     * authorization. Access must be explicitly defined,
     * otherwise it is blocked.
     */
    public static class AuthServer {

        /**
         * Check that at least one rule exists that allows access to the resource.
         *
         * @param claims the claims found in the token
         * @param request the request coming in
         * @return true if there is a valid rule
         */
        private static boolean filterRules(AllAccessPermissionRules aclList, Map<String, Claim> claims, HttpServletRequest request) {
            aclList.getRules().removeIf(r -> !evaluateRule(r, request.getRequestURI(), claims, aclList));
            return !aclList.getRules().isEmpty();
        }


        private static boolean verifyAllClaims(Map<String, Claim> claims, AccessPermissionRule rule, AllAccessPermissionRules allAccess, Map<String, Object> fieldCtx) {
            Acl acl = getAcl(rule, allAccess);

            boolean isAnonymous = getAttributes(acl, allAccess).stream()
                    .anyMatch(attr -> attr.getGlobal() != null && "ANONYMOUS".equals(attr.getGlobal().value()));

            List<String> claimNames = getAttributes(acl, allAccess).stream()
                    .filter(attr -> attr.getGlobal() == null)
                    .map(AttributeItem::getClaim)
                    .filter(Objects::nonNull)
                    .toList();

            // Build  context
            Map<String, Object> ctx = new HashMap<>();
            if (claims != null) {
                for (String name: claimNames) {
                    Claim c = claims.get(name);
                    if (c != null) {
                        ctx.put("CLAIM:" + name, c.asString());
                    }
                }
            }
            // Add $sm#semanticId
            if (fieldCtx != null && !fieldCtx.isEmpty()) {
                ctx.putAll(fieldCtx);
            }
            ctx.put("UTCNOW", LocalTime.now(Clock.systemUTC()));
            if (isAnonymous) {
                return FormulaEvaluator.evaluate(getFormula(rule, allAccess), ctx);
            }
            return !ctx.entrySet().stream().filter(e -> e.getKey().startsWith("CLAIM:")).toList().isEmpty() &&
                    FormulaEvaluator.evaluate(getFormula(rule, allAccess), ctx);
        }


        private static boolean checkIdentifiable(String path, String identifiable) {
            //check submodel path
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


        private static boolean evaluateRule(AccessPermissionRule rule, String path, Map<String, Claim> claims, AllAccessPermissionRules allAccess) {
            return evaluateRule(rule, path, claims, allAccess, null);
        }


        private static boolean evaluateRule(AccessPermissionRule rule, String path, Map<String, Claim> claims, AllAccessPermissionRules allAccess,
                                            Map<String, Object> fieldCtx) {
            Acl acl = getAcl(rule, allAccess);
            return acl != null && getAttributes(acl, allAccess) != null && acl.getRights() != null && getObjects(rule, allAccess) != null
                    && getObjects(rule, allAccess).stream().anyMatch(attr -> {
                        if (attr.getRoute() != null) {
                            return "*".equals(attr.getRoute()) || attr.getRoute().contains(path);
                        }
                        else if (attr.getIdentifiable() != null) {
                            return checkIdentifiable(path, attr.getIdentifiable());
                        }
                        else if (attr.getDescriptor() != null) {
                            return checkDescriptor(path, attr.getDescriptor());
                        }
                        else {
                            return false;
                        }
                    }) && verifyAllClaims(claims, rule, allAccess, fieldCtx);
        }
    }

}
