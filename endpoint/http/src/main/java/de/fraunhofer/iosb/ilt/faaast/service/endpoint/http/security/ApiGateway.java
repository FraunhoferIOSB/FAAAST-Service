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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkException;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.UrlJwkProvider;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.AllAccessPermissionRulesRoot;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.Rule;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.interfaces.RSAPublicKey;
import java.time.Clock;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;


/**
 * An ApiGateway that verifies JWT tokens against the provided jwkProvider.
 * Initially all ACL rules from the folder are read and stored. After that,
 * the folder is monitored for deletion or addition of new rules.
 */
public class ApiGateway {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ApiGateway.class);
    private String jwkProvider;
    private Map<Path, AllAccessPermissionRulesRoot> aclList;
    private final String abortMessage = "Invalid ACL folder path, AAS Security will not enforce rules.)";

    public ApiGateway(String jwkProvider, String aclFolder) {
        this.jwkProvider = jwkProvider;
        initializeAclList(aclFolder);
        monitorAclRules(aclFolder);
    }


    /**
     * Verifies the token by decoding it.
     * Additionally, AuthServer is used to verify claims.
     *
     * @param token the JWT token
     * @param request the request parameters
     * @return true if the token is valid
     */
    public boolean isAuthorized(String token, HttpServletRequest request) {
        if (Objects.isNull(token)) {
            return AuthServer.filterRules(this.aclList, null, request);
        }
        else {
            token = token.startsWith("Bearer ") ? token.substring("Bearer ".length()).trim() : token;
            DecodedJWT jwt = JWT.decode(token);
            JwkProvider provider = new UrlJwkProvider(this.jwkProvider);
            try {
                Jwk jwk = provider.get(jwt.getKeyId());
                Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);
                algorithm.verify(jwt);
                JWTVerifier verifier = JWT.require(algorithm)
                        //.withIssuer(this.jwkProvider)
                        .build();
                verifier.verify(token);
            }
            catch (JwkException e) {
                return false;
            }
            return AuthServer.filterRules(this.aclList, jwt.getClaims(), request);
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
         * @param claims
         * @param request
         * @return
         */
        private static boolean filterRules(Map<Path, AllAccessPermissionRulesRoot> aclList, Map<String, Claim> claims, HttpServletRequest request) {
            String requestPath = request.getRequestURI();
            String path = requestPath.startsWith(apiPrefix) ? requestPath.substring(9) : requestPath;
            String method = request.getMethod();
            List<AllAccessPermissionRulesRoot> relevantRules = aclList.values().stream()
                    .filter(a -> a.getAllAccessPermissionRules()
                            .getRules().stream()
                            .anyMatch(r -> r.getACL() != null
                                    && r.getACL().getATTRIBUTES() != null
                                    && r.getACL().getRIGHTS() != null
                                    && r.getOBJECTS() != null
                                    && r.getOBJECTS().stream().anyMatch(attr -> {
                                        if (attr.getROUTE() != null) {
                                            return "*".equals(attr.getROUTE()) || attr.getROUTE().contains(path);
                                        }
                                        else if (attr.getIDENTIFIABLE() != null) {
                                            return checkIdentifiable(path, attr.getIDENTIFIABLE());
                                        }
                                        else {
                                            return false;
                                        }
                                    })
                                    && "ALLOW".equals(r.getACL().getACCESS())
                                    && r.getACL().getRIGHTS().contains(getRequiredRight(method))
                                    && verifyAllClaims(claims, r)))
                    .collect(Collectors.toList());
            return !relevantRules.isEmpty();
        }


        private static boolean verifyAllClaims(Map<String, Claim> claims, Rule rule) {
            if (rule.getACL().getATTRIBUTES().stream()
                    .anyMatch(attr -> "ANONYMOUS".equals(attr.getGLOBAL())
                            && Boolean.TRUE.equals(rule.getFORMULA().get("$boolean")))) {
                return true;
            }
            if (claims == null) {
                return false;
            }
            List<String> claimValues = rule.getACL().getATTRIBUTES().stream()
                    .filter(attr -> attr.getGLOBAL() == null)
                    .map(Attribute::getCLAIM)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return !claimValues.isEmpty()
                    && claimValues.stream()
                            .allMatch(value -> {
                                Claim claim = claims.get(value);
                                return claim != null
                                        && evaluateFormula(rule.getFORMULA(), value, claim.asString());
                            });
        }


        private static boolean evaluateFormula(Map<String, Object> formula,
                                               String claimName,
                                               String claimValue) {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("CLAIM:" + claimName, claimValue);
            ctx.put("UTCNOW", LocalTime.now(Clock.systemUTC())); // $GLOBAL → UTCNOW
            return FormulaEvaluator.evaluate(formula, ctx);
        }


        private static String getRequiredRight(String method) {
            switch (method) {
                case "GET":
                    return "READ";
                case "POST":
                    return "WRITE";
                case "PUT":
                    return "UPDATE";
                case "DELETE":
                    return "DELETE";
                default:
                    throw new IllegalArgumentException("Unsupported method: " + method);
            }
        }


        private static boolean checkIdentifiable(String path, String identifiable) {
            //check submodel path
            if (!path.startsWith("/submodels")) {
                return false;
            }

            if (identifiable.equals("(Submodel)*")) {
                return true;
            }
            else if (identifiable.startsWith("(Submodel)")) {
                String id = identifiable.substring(10);
                return path.contains(EncodingHelper.base64Encode(id));
            }
            return false;
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
                String content = null;
                try {
                    content = new String(Files.readAllBytes(filePath), StandardCharsets.UTF_8);
                    aclList.put(filePath, mapper.readValue(
                            content, AllAccessPermissionRulesRoot.class));
                }
                catch (IOException e) {
                    LOGGER.error(abortMessage);
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
        WatchService watchService = null;
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
            LOGGER.error(abortMessage);
        }

    }


    private void monitorLoop(WatchService watchService, Path folderToWatch) {
        ObjectMapper mapper = new ObjectMapper();
        Thread monitoringThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey watchKey = null;
                try {
                    watchKey = watchService.take();
                }
                catch (InterruptedException e) {
                    LOGGER.error(abortMessage);
                }
                for (WatchEvent<?> event: watchKey.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();
                    Path filePath = (Path) event.context();
                    Path absolutePath = folderToWatch.resolve(filePath).toAbsolutePath();
                    // Check if the file is a JSON file
                    if (filePath.toString().toLowerCase().endsWith(".json")) {
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            try {
                                aclList.put(absolutePath, mapper.readValue(
                                        new String(Files.readAllBytes(absolutePath), StandardCharsets.UTF_8), AllAccessPermissionRulesRoot.class));
                            }
                            catch (IOException e) {
                                LOGGER.error(abortMessage);
                            }
                            LOGGER.info("Added new ACL rule.");
                        }
                        else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            aclList.remove(absolutePath);
                            LOGGER.info("Removed ACL rule.");
                        }
                    }
                }
                // Reset the key to receive further watch events
                boolean valid = watchKey.reset();
                if (!valid) {
                    System.out.println("WatchKey no longer valid; exiting.");
                    break;
                }
            }
        });
        monitoringThread.start();
    }
}
