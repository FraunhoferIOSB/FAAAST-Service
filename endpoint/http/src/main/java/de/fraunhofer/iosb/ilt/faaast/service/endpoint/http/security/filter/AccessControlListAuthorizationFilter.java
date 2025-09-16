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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.auth.AuthState.ANONYMOUS;

import com.auth0.jwk.JwkProvider;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.FormulaEvaluator;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.ACL;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.AllAccessPermissionRulesRoot;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.DefACL;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.json.Rule;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import java.time.Clock;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;


/**
 * Filters any incoming request with respect to the given ACL rules.
 */
public class AccessControlListAuthorizationFilter extends JwtAuthorizationFilter {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AccessControlListAuthorizationFilter.class);

    //private final JwkProvider jwkProvider;
    private Map<Path, AllAccessPermissionRulesRoot> aclList;
    private final String abortMessage = "Invalid ACL folder path, AAS Security will not enforce rules.)";

    public AccessControlListAuthorizationFilter(JwkProvider jwkProvider, String aclFolder) {
        //this.jwkProvider = jwkProvider;
        initializeAclList(aclFolder);
        // TODO Maybe we can read the ACL rules when a request comes in?
        monitorAclRules(aclFolder);
    }


    /**
     * Verify JWT claims by counterchecking with ACL.
     *
     * @param servletRequest the <code>ServletRequest</code> object contains the client's request
     * @param servletResponse the <code>ServletResponse</code> object contains the filter's response
     * @param filterChain the <code>FilterChain</code> for invoking the next filter or the resource
     * @throws IOException doFilter of further Filter steps
     * @throws ServletException doFilter of further Filter steps
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;

        if (ANONYMOUS.equals(servletRequest.getAttribute("auth.state"))) {
            // Check request without any claims
            if (checkClaims(httpRequest)) {
                filterChain.doFilter(servletRequest, servletResponse);
            }
            return;
        }

        // Extract JWT
        DecodedJWT jwt = extractAndDecodeJwt(httpRequest);
        if (jwt == null) {
            LOGGER.info("Could not extract JWT");
            return;
        }

        if (!checkClaims(httpRequest, jwt.getClaims())) {
            httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
        }
        else {
            // Continue with the request
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }


    private boolean checkClaims(HttpServletRequest request) {
        return checkClaims(request, null);
    }


    private boolean checkClaims(HttpServletRequest request, Map<String, Claim> claims) {
        return AuthServer.filterRules(this.aclList, claims, request);
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
                            .anyMatch(r -> evalueRule(r, path, method, claims, a.getAllAccessPermissionRules())))
                    .collect(Collectors.toList());
            return !relevantRules.isEmpty();
        }


        private static boolean verifyAllClaims(Map<String, Claim> claims, Rule rule, AllAccessPermissionRules allAccess) {
            ACL acl = getAcl(rule, allAccess);
            if (acl.getATTRIBUTES().stream()
                    .anyMatch(attr -> "ANONYMOUS".equals(attr.getGLOBAL())
                            && Boolean.TRUE.equals(rule.getFORMULA().get("$boolean")))) {
                return true;
            }
            if (claims == null) {
                return false;
            }
            List<String> claimValues = acl.getATTRIBUTES().stream()
                    .filter(attr -> attr.getGLOBAL() == null)
                    .map(Attribute::getCLAIM)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            Map<String, String> claimList = new HashMap<>();
            for (String val: claimValues) {
                Object claim = claims.get(val);
                if (claim != null) {
                    claimList.put(val, claim.toString());
                }
            }
            return !claimValues.isEmpty()
                    && claimValues.stream()
                            .allMatch(value -> {
                                //Claim claim = claims.get(value);
                                //return claim != null
                                return evaluateFormula(rule.getFORMULA(), claimList);
                            });
        }


        private static boolean evaluateFormula(Map<String, Object> formula,
                                               Map<String, String> claims) {
            Map<String, Object> ctx = new HashMap<>();
            for (var c: claims.entrySet()) {
                ctx.put("CLAIM:" + c.getKey(), c.getValue());
            }
            //ctx.put("CLAIM:" + claimName, claimValue);
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


        private static boolean checkDescriptor(String path, String descriptor) {
            if (descriptor.startsWith("(aasDesc)")) {
                if (!path.startsWith("/shell-descriptors")) {
                    return false;
                }
                if (descriptor.equals("(aasDesc)*")) {
                    return true;
                }
                else if (descriptor.startsWith("(aasDesc)")) {
                    String id = descriptor.substring(9);
                    return path.contains(EncodingHelper.base64UrlEncode(id));
                }
            }
            else if (descriptor.startsWith("(smDesc)")) {
                if (!path.startsWith("/submodel-descriptors")) {
                    return false;
                }
                if (descriptor.equals("(smDesc)*")) {
                    return true;
                }
                else if (descriptor.startsWith("(smDesc)")) {
                    String id = descriptor.substring(8);
                    return path.contains(EncodingHelper.base64UrlEncode(id));
                }
            }
            return false;
        }


        private static boolean evalueRule(Rule rule, String path, String method, Map<String, Claim> claims, AllAccessPermissionRules allAccess) {
            ACL acl = getAcl(rule, allAccess);
            return acl != null
                    && acl.getATTRIBUTES() != null
                    && acl.getRIGHTS() != null
                    && rule.getOBJECTS() != null
                    && rule.getOBJECTS().stream().anyMatch(attr -> {
                        if (attr.getROUTE() != null) {
                            return "*".equals(attr.getROUTE()) || attr.getROUTE().contains(path);
                        }
                        else if (attr.getIDENTIFIABLE() != null) {
                            return checkIdentifiable(path, attr.getIDENTIFIABLE());
                        }
                        else if (attr.getDESCRIPTOR() != null) {
                            return checkDescriptor(path, attr.getDESCRIPTOR());
                        }
                        else {
                            return false;
                        }
                    })
                    && "ALLOW".equals(acl.getACCESS())
                    && acl.getRIGHTS().contains(getRequiredRight(method))
                    && verifyAllClaims(claims, rule, allAccess);
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


    private static ACL getAcl(Rule rule, AllAccessPermissionRules allAccess) {
        if (rule.getACL() != null) {
            return rule.getACL();
        }
        else if (rule.getUSEACL() != null) {
            Optional<DefACL> acl = allAccess.getDEFACLS().stream().filter(a -> (a.getName() == null ? a.getName() == null : a.getName().equals(a.getName()))).findAny();
            if (acl.isPresent()) {
                return acl.get().getAcl();
            }
            else {
                throw new IllegalArgumentException("DEFACL not found: " + rule.getUSEACL());
            }
        }
        else {
            throw new IllegalArgumentException("invalid rule: ACL or USEACL must be specified");
        }
    }
}
