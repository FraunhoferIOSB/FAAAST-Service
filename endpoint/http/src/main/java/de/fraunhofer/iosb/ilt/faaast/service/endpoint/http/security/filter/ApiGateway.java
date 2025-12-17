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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod.POST;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.FormulaEvaluator;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.GetAllAssetAdministrationShellsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.GetAllSubmodelsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Acl;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Defacl;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Defattribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Defformula;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Defobject;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.ObjectItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.RightsEnum;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Clock;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.LoggerFactory;


/**
 * Filters any incoming request with respect to the given ACL rules.
 */
public class ApiGateway {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ApiGateway.class);
    private static final String BEARER_KWD = "Bearer";

    private Map<Path, AllAccessPermissionRules> aclList;
    private final String abortMessage = "Invalid ACL folder path, AAS Security will not enforce rules.)";
    private final String errorMessage = "Invalid ACL rule, skipping.";

    public ApiGateway(String aclFolder) {
        initializeAclList(aclFolder);
        monitorAclRules(aclFolder);
    }


    /**
     * Checks if the user is authorized to receive the response of the request.
     *
     * @param request the HttpRequest
     * @return true if authorized and ACL exists
     */
    public boolean isAuthorized(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return AuthServer.filterRules(this.aclList, null, request);
        }
        else {
            if (token.startsWith(BEARER_KWD + " ")) {
                token = token.substring(BEARER_KWD.length() + 1).trim();
            }
            DecodedJWT jwt = JWT.decode(token);
            return AuthServer.filterRules(this.aclList, jwt.getClaims(), request);
        }
    }


    /**
     * Filters out AAS that the user is not authorized for.
     *
     * @param request the HttpRequest
     * @param response the ApiResponse
     * @return the ApiResponse with only allowed AAS
     */
    public Response filterAas(HttpServletRequest request, GetAllAssetAdministrationShellsResponse response) {
        response.getPayload().getContent()
                .removeIf(aas -> aclList.values().stream()
                        .noneMatch(a -> a.getRules().stream()
                                .anyMatch(r -> AuthServer.evaluateRule(r, "/shells/" + EncodingHelper.base64Encode(aas.getId()),
                                        request.getMethod(), extractClaims(request), a))));
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
            String method = request.getMethod();
            Map<String, Claim> claims = extractClaims(request);

            Map<String, Object> fieldCtx = new HashMap<>();
            if (submodel.getSemanticId() != null) {
                fieldCtx.put("$sm#semanticId", submodel.getSemanticId().getKeys().get(0).getValue());
            }

            return aclList.values().stream()
                    .noneMatch(allAccess -> allAccess.getRules().stream().anyMatch(rule -> AuthServer.evaluateRule(rule, path, method, claims, allAccess, fieldCtx)));
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
        String method = request.getMethod();
        Map<String, Claim> claims = extractClaims(request);

        Map<String, Object> fieldCtx = new HashMap<>();
        if (submodel.getSemanticId() != null) {
            fieldCtx.put("$sm#semanticId", submodel.getSemanticId().getKeys().get(0).getValue());
        }

        return aclList.values().stream()
                .anyMatch(allAccess -> allAccess.getRules().stream().anyMatch(rule -> AuthServer.evaluateRule(rule, path, method, claims, allAccess, fieldCtx)));
    }


    private Map<String, Claim> extractClaims(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token == null) {
            return null;
        }
        if (token.startsWith(BEARER_KWD + " ")) {
            token = token.substring(BEARER_KWD.length() + 1).trim();
        }
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaims();
        }
        catch (com.auth0.jwt.exceptions.JWTDecodeException e) {
            return null;
        }
    }

    /**
     * Simple whitelist AuthServer implementation that supports ANONYMOUS access,
     * claims with simple eq formulas and route authorization.
     * Access must be explicitly defined, otherwise it is blocked.
     */
    public static class AuthServer {
        private static final String apiPrefix = "/api/v3.0/";

        /**
         * Check all rules that explicitly allows the request.
         * If a rule exists after all filters, true is returned
         *
         * @param claims the claims found in the token
         * @param request the request coming in
         * @return true if there is a valid rule
         */
        private static boolean filterRules(Map<Path, AllAccessPermissionRules> aclList, Map<String, Claim> claims, HttpServletRequest request) {
            String requestPath = request.getRequestURI();
            String path = requestPath.startsWith(apiPrefix) ? requestPath.substring(apiPrefix.length()) : requestPath;
            String method = request.getMethod();
            List<AllAccessPermissionRules> relevantRules = aclList.values().stream()
                    .filter(a -> a.getRules().stream()
                            .anyMatch(r -> evaluateRule(r, path, method, claims, a)))
                    .toList();
            return !relevantRules.isEmpty();
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


        private static boolean evaluateRights(List<RightsEnum> aclRights, String method, String path) {
            String requiredRight = isOperationRequest(method, path) ? "EXECUTE" : getRequiredRight(method);
            return aclRights.contains(RightsEnum.ALL) || aclRights.contains(RightsEnum.valueOf(requiredRight));
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

            return POST.name().equals(method) && ("invoke".equals(cleanPath) || "invoke-async".equals(cleanPath));
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


        private static boolean evaluateRule(AccessPermissionRule rule, String path, String method, Map<String, Claim> claims, AllAccessPermissionRules allAccess) {
            return evaluateRule(rule, path, method, claims, allAccess, null);
        }


        private static boolean evaluateRule(AccessPermissionRule rule, String path, String method, Map<String, Claim> claims, AllAccessPermissionRules allAccess,
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
                    }) && "ALLOW".equals(acl.getAccess().value()) && evaluateRights(acl.getRights(), method, path) && verifyAllClaims(claims, rule, allAccess, fieldCtx);
        }
    }

    private void initializeAclList(String aclFolder) {
        this.aclList = new HashMap<>();
        if (aclFolder == null
                || aclFolder.trim().isEmpty()
                || !new File(aclFolder.trim()).isDirectory()) {
            LOGGER.error(abortMessage);
            return;
        }
        File folder = new File(aclFolder.trim());
        File[] jsonFiles = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        ObjectMapper mapper = new ObjectMapper();
        if (jsonFiles != null) {
            for (File file: jsonFiles) {
                Path filePath = file.toPath();
                try {
                    String jsonContent = Files.readString(filePath);
                    JsonNode rootNode = mapper.readTree(jsonContent);
                    AllAccessPermissionRules allRules;
                    if (rootNode.has("AllAccessPermissionRules")) {
                        allRules = mapper.treeToValue(rootNode.get("AllAccessPermissionRules"), AllAccessPermissionRules.class);
                    }
                    else {
                        allRules = mapper.readValue(jsonContent, AllAccessPermissionRules.class);
                    }
                    aclList.put(filePath, allRules);
                }
                catch (IOException e) {
                    LOGGER.error(errorMessage);
                }
            }
        }
    }


    private void monitorAclRules(String aclFolder) {
        if (aclFolder == null
                || aclFolder.trim().isEmpty()
                || !new File(aclFolder.trim()).isDirectory()) {
            LOGGER.error(abortMessage);
            return;
        }
        Path folderToWatch = Paths.get(aclFolder);
        WatchService watchService;
        try {
            watchService = FileSystems.getDefault().newWatchService();
            // Register the folder with the WatchService for CREATE and DELETE events
            folderToWatch.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE);
            monitorLoop(watchService, folderToWatch);
        }
        catch (IOException e) {
            LOGGER.error(errorMessage);
        }

    }


    private void monitorLoop(WatchService watchService, Path folderToWatch) {
        ObjectMapper mapper = new ObjectMapper();
        Thread monitoringThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey watchKey;
                try {
                    watchKey = watchService.take();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // restore interrupt status
                    LOGGER.warn("ACL monitoring thread interrupted", e);
                    break; // exit loop
                }
                for (WatchEvent<?> event: watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path filePath = (Path) event.context();
                    Path absolutePath = folderToWatch.resolve(filePath).toAbsolutePath();
                    // Check if the file is a JSON file
                    if (filePath.toString().toLowerCase().endsWith(".json")) {
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            try {
                                String jsonContent = Files.readString(absolutePath);
                                JsonNode rootNode = mapper.readTree(jsonContent);
                                AllAccessPermissionRules allRules;
                                if (rootNode.has("AllAccessPermissionRules")) {
                                    allRules = mapper.treeToValue(rootNode.get("AllAccessPermissionRules"), AllAccessPermissionRules.class);
                                }
                                else {
                                    allRules = mapper.readValue(jsonContent, AllAccessPermissionRules.class);
                                }
                                aclList.put(absolutePath, allRules);
                            }
                            catch (IOException e) {
                                LOGGER.error(errorMessage);
                            }
                            LOGGER.info("Added new ACL rule.");
                        }
                        else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            aclList.remove(absolutePath);
                            LOGGER.info("Removed ACL rule.");
                        }
                    }
                }
                boolean valid = watchKey.reset();
                if (!valid) {
                    LOGGER.info("WatchKey no longer valid; exiting.");
                    break;
                }
            }
        });
        monitoringThread.start();
    }


    private static Acl getAcl(AccessPermissionRule rule, AllAccessPermissionRules allAccess) {
        if (rule.getAcl() != null) {
            return rule.getAcl();
        }
        else if (rule.getUseacl() != null) {
            Optional<Defacl> acl = allAccess.getDefacls().stream()
                    .filter(a -> Objects.equals(a.getName(), rule.getUseacl()))
                    .findAny();
            if (acl.isPresent()) {
                return acl.get().getAcl();
            }
            else {
                throw new IllegalArgumentException("DEFACL not found: " + rule.getUseacl());
            }
        }
        else {
            throw new IllegalArgumentException("invalid rule: ACL or USEACL must be specified");
        }
    }


    private static List<AttributeItem> getAttributes(Acl acl, AllAccessPermissionRules allAccess) {
        if ((acl.getAttributes() != null) && (!acl.getAttributes().isEmpty())) {
            return acl.getAttributes();
        }
        else if (acl.getUseattributes() != null) {
            Optional<Defattribute> attribute = allAccess.getDefattributes().stream()
                    .filter(a -> Objects.equals(a.getName(), acl.getUseattributes()))
                    .findAny();
            if (attribute.isPresent()) {
                return attribute.get().getAttributes();
            }
            else {
                throw new IllegalArgumentException("DEFATTRIBUTES not found: " + acl.getUseattributes());
            }
        }
        else {
            throw new IllegalArgumentException("invalid rule: ATTRIBUTES or USEATTRIBUTES must be specified");
        }
    }


    private static LogicalExpression getFormula(AccessPermissionRule rule, AllAccessPermissionRules allAccess) {
        if (rule.getFormula() != null) {
            return rule.getFormula();
        }
        else if (rule.getUseformula() != null) {
            Optional<Defformula> formula = allAccess.getDefformulas().stream()
                    .filter(a -> Objects.equals(a.getName(), rule.getUseformula()))
                    .findAny();
            if (formula.isPresent()) {
                return formula.get().getFormula();
            }
            else {
                throw new IllegalArgumentException("DEFFORMULA not found: " + rule.getUseformula());
            }
        }
        else {
            throw new IllegalArgumentException("invalid rule: FORMULA or USEFORMULA must be specified");
        }
    }


    private static List<ObjectItem> getObjects(AccessPermissionRule rule, AllAccessPermissionRules allAccess) {
        if ((rule.getObjects() != null) && (!rule.getObjects().isEmpty())) {
            return rule.getObjects();
        }
        else if (rule.getUseobjects() != null) {
            // We must collect all Defobjects in all Useobjects
            List<Defobject> objectList = allAccess.getDefobjects().stream()
                    .filter(a -> rule.getUseobjects().contains(a.getName()))
                    .toList();
            if (objectList.isEmpty()) {
                throw new IllegalArgumentException("DEFOBJECTS not found: " + rule.getUseobjects());
            }
            else {
                Set<ObjectItem> retval = new HashSet<>();
                for (Defobject item: objectList) {
                    retval.addAll(item.getObjects());
                }
                return retval.stream().toList();
            }
        }
        else {
            throw new IllegalArgumentException("invalid rule: OBJECTS or USEOBJECTS must be specified");
        }
    }
}
