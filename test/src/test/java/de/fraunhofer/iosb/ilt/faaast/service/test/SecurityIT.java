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
package de.fraunhofer.iosb.ilt.faaast.service.test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.AbstractJwtFilter.AUTHORIZATION;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.AbstractJwtFilter.BEARER;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper.toHttpStatusCode;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.memory.FileStorageInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.StaticRequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.ApiPaths;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReflectionHelper;
import java.io.IOException;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.serialization.EnumSerializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;


public class SecurityIT extends AbstractIntegrationTest {

    private static final String keyId = "kid";

    @ClassRule
    public static WireMockClassRule server = new WireMockClassRule(options().dynamicPort());
    // required, see https://wiremock.org/docs/junit-extensions/#other-rule-configurations
    @Rule
    public WireMockClassRule instanceRule = server;

    private static Service service;
    private static Environment environment;
    private static ApiPaths apiPaths;
    private static MessageBus messageBus;
    private static Persistence persistence;
    private static FileStorage fileStorage;
    private static String endpointAclFolder;
    private static JsonDeserializer jsonDeserializer;
    private static KeyPair keyPair;

    public SecurityIT() {
        jsonDeserializer = new JsonDeserializer();
    }


    @BeforeClass
    public static void beforeClass() throws IOException {
        PORT = PortHelper.findFreePort();
        apiPaths = new ApiPaths(HOST, PORT);
        endpointAclFolder = Files.createTempDirectory("endpoint-acl").toFile().getAbsolutePath();
    }


    @Before
    public void before() throws Exception {
        environment = AASFull.createEnvironment();
        keyPair = generateKeyPair();
        String jwkProviderUrl = stubJwkProvider(server, keyPair);

        ServiceConfig serviceConfig = ServiceConfig.builder()
                .core(CoreConfig.builder()
                        .build())
                .persistence(PersistenceInMemoryConfig.builder()
                        .initialModel(DeepCopyHelper.deepCopy(environment))
                        .build())
                .fileStorage(new FileStorageInMemoryConfig())
                .endpoints(List.of(HttpEndpointConfig.builder()
                        .port(PORT)
                        .sni(false)
                        .certificate(CertificateConfig.builder()
                                .keyStorePath(httpEndpointKeyStoreFile)
                                .keyStoreType(HTTP_ENDPOINT_KEYSTORE_TYPE)
                                .keyPassword(HTTP_ENDPOINT_KEYSTORE_PASSWORD)
                                .keyStorePassword(HTTP_ENDPOINT_KEYSTORE_PASSWORD)
                                .build())
                        .aclFolder(endpointAclFolder)
                        .jwkProvider(jwkProviderUrl)
                        .build()))
                .messageBus(MessageBusInternalConfig.builder()
                        .build())
                .build();
        service = new Service(serviceConfig);
        messageBus = service.getMessageBus();
        persistence = ReflectionHelper.getField(service, "persistence", Persistence.class);
        fileStorage = ReflectionHelper.getField(service, "fileStorage", FileStorage.class);
        StaticRequestExecutionContext requestExecutionContext = new StaticRequestExecutionContext(
                serviceConfig.getCore(),
                persistence,
                fileStorage,
                messageBus,
                service.getAssetConnectionManager());
        ReflectionHelper.setField(service, "requestExecutionContext", requestExecutionContext);
        service.start();
    }


    @After
    public void after() {
        service.stop();
        WireMock.resetAllRequests();
    }


    protected static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }


    protected static String stubJwkProvider(WireMockServer server, KeyPair keyPair) {
        RSAPublicKey pub = (RSAPublicKey) keyPair.getPublic();
        String n = Base64.getUrlEncoder().withoutPadding().encodeToString(pub.getModulus().toByteArray());
        String e = Base64.getUrlEncoder().withoutPadding().encodeToString(pub.getPublicExponent().toByteArray());

        String jwkJson = String.format("""
                {
                  "keys": [{
                    "kty": "RSA",
                    "kid": "%s",
                    "n": "%s",
                    "e": "%s"
                  }]
                }
                """, keyId, n, e);

        stubFor(get(urlEqualTo("/.well-known/jwks.json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(jwkJson)));

        return "http://localhost:" + server.port() + "/.well-known/jwks.json";
    }


    protected void writeAclRules(String fileName, String content) throws IOException {
        Path aclFile = Paths.get(endpointAclFolder, fileName);
        Files.createDirectories(aclFile.getParent());
        Files.write(aclFile, content.getBytes(StandardCharsets.UTF_8));
    }


    protected void writeAclRules(String content) throws IOException {
        writeAclRules("acls.json", content);
    }


    protected String loadAclRulesFromFile(String fileName) throws IOException {
        Path aclFile = Paths.get("src/test/resources/security", fileName);
        return Files.readString(aclFile, StandardCharsets.UTF_8);
    }


    protected void applyAclRulesFromFile(String fileName) throws IOException {
        String content = loadAclRulesFromFile(fileName);
        writeAclRules(content);
    }


    protected Map<String, String> authHeaderFromClaims(Map<String, String> claims, KeyPair keyPair) {
        var jwtBuilder = JWT.create()
                .withKeyId(keyId);

        for (Map.Entry<String, String> claim: claims.entrySet()) {
            jwtBuilder.withClaim(claim.getKey(), claim.getValue());
        }

        return Map.of(AUTHORIZATION, BEARER.concat(" ")
                .concat(jwtBuilder.sign(Algorithm.RSA256(((RSAPublicKey) keyPair.getPublic()), (RSAPrivateKey) keyPair.getPrivate()))));
    }


    protected HttpResponse<String> executeRequest(HttpMethod method, String url) throws Exception {
        return HttpHelper.execute(httpClient, method, url, Map.of());
    }


    protected HttpResponse<String> executeRequest(HttpMethod method, String url, Object payload, Map<String, String> headers) throws Exception {
        return HttpHelper.execute(httpClient, method, url, payload, headers);
    }


    protected int getStatusCode(HttpResponse<String> response) {
        return response.statusCode();
    }


    @Test
    public void testSecurityWithJwkProvider() {}


    @Test
    public void testSecurityWithoutJwkProvider() {}


    @Test
    public void testSecurityWithEmptyAclRules() throws Exception {
        assertForbidden(environment, ReferenceBuilder.forAas(environment.getAssetAdministrationShells().get(0)));
        assertForbidden(environment, ReferenceBuilder.forSubmodel(environment.getSubmodels().get(0)));
        assertForbidden(environment, ReferenceBuilder.forConceptDescription(environment.getConceptDescriptions().get(0)));
        assertForbidden(environment, ReferenceBuilder.forSubmodel(environment.getSubmodels().get(0), environment.getSubmodels().get(0).getSubmodelElements().get(0)));
    }


    @Test
    public void testSecurityWithAnonymousAccessToAll() throws Exception {
        applyAclRulesFromFile("anonymous.json");

        Submodel sm = environment.getSubmodels().get(3);
        SubmodelElementCollection smc = (SubmodelElementCollection) sm.getSubmodelElements().get(6);
        assertAllowed(environment, ReferenceBuilder.forSubmodel(sm));
        assertAllowed(environment, ReferenceBuilder.forSubmodel(sm, smc));
        assertAllowed(environment, ReferenceBuilder.forSubmodel(sm, smc, smc.getValue().get(0)));
        assertAllowed(environment, ReferenceBuilder.forSubmodel(environment.getSubmodels().get(0)));
        assertAllowed(environment, ReferenceBuilder.forAas(environment.getAssetAdministrationShells().get(0)));
        assertAllowed(environment, ReferenceBuilder.forConceptDescription(environment.getConceptDescriptions().get(0)));
    }


    @Test
    public void testSecurityWithAnonymousAccessToSubmodels() throws Exception {
        applyAclRulesFromFile("only_submodels.json");

        assertAllowed(environment, ReferenceBuilder.forSubmodel(environment.getSubmodels().get(0)));
        assertAllowed(environment, ReferenceBuilder.forSubmodel(environment.getSubmodels().get(0), environment.getSubmodels().get(0).getSubmodelElements().get(0)));

        assertForbidden(environment, ReferenceBuilder.forAas(environment.getAssetAdministrationShells().get(0)));
        assertForbidden(environment, ReferenceBuilder.forConceptDescription(environment.getConceptDescriptions().get(0)));
    }


    @Test
    public void testSecurityWithAnonymousAccessToSpecificSubmodel() throws Exception {
        applyAclRulesFromFile("specific_submodel.json");
        Submodel sm = environment.getSubmodels().get(3);
        SubmodelElementCollection smc = (SubmodelElementCollection) sm.getSubmodelElements().get(6);

        assertAllowed(environment, ReferenceBuilder.forSubmodel(sm));
        assertAllowed(environment, ReferenceBuilder.forSubmodel(sm, smc));
        assertAllowed(environment, ReferenceBuilder.forSubmodel(sm, smc, smc.getValue().get(1)));
        assertAllowed(environment, ReferenceBuilder.forSubmodel(sm, smc, smc.getValue().get(0)));

        assertForbidden(environment, ReferenceBuilder.forSubmodel(environment.getSubmodels().get(0)));
        assertForbidden(environment, ReferenceBuilder.forAas(environment.getAssetAdministrationShells().get(0)));
        assertForbidden(environment, ReferenceBuilder.forConceptDescription(environment.getConceptDescriptions().get(0)));
    }


    @Test
    public void testSecurityWithAnonymousAccessToSpecificSubmodelElement() throws Exception {
        applyAclRulesFromFile("specific_submodel_element.json");

        assertAllowed(environment, ReferenceBuilder.forSubmodel(environment.getSubmodels().get(2), environment.getSubmodels().get(2).getSubmodelElements().get(0)));

        assertForbidden(environment, ReferenceBuilder.forSubmodel(environment.getSubmodels().get(2), environment.getSubmodels().get(2).getSubmodelElements().get(1)));
        assertForbidden(environment, ReferenceBuilder.forSubmodel(environment.getSubmodels().get(0)));
        assertForbidden(environment, ReferenceBuilder.forAas(environment.getAssetAdministrationShells().get(0)));
        assertForbidden(environment, ReferenceBuilder.forConceptDescription(environment.getConceptDescriptions().get(0)));
    }


    @Test
    public void testSecurityWithAnonymousAccessToSpecificSubmodelCollectionAndChildren() throws Exception {
        applyAclRulesFromFile("specific_submodel_element_and_children.json");
        Submodel sm = environment.getSubmodels().get(3);
        SubmodelElementCollection smc = (SubmodelElementCollection) sm.getSubmodelElements().get(6);
        assertAllowed(environment, ReferenceBuilder.forSubmodel(sm, smc));
        assertAllowed(environment, ReferenceBuilder.forSubmodel(sm, smc, smc.getValue().get(1)));
        assertAllowed(environment, ReferenceBuilder.forSubmodel(sm, smc, smc.getValue().get(0)));

        assertForbidden(environment, ReferenceBuilder.forSubmodel(environment.getSubmodels().get(0)));
        assertForbidden(environment, ReferenceBuilder.forAas(environment.getAssetAdministrationShells().get(0)));
        assertForbidden(environment, ReferenceBuilder.forConceptDescription(environment.getConceptDescriptions().get(0)));
    }


    @Test
    public void testSecurityWithAuthenticatedAccessToAllResources() throws Exception {
        applyAclRulesFromFile("bpn.json");
        var authHeader = authHeaderFromClaims(Map.of("BusinessPartnerNumber", "BPN1234"), keyPair);

        Submodel sm = environment.getSubmodels().get(3);
        SubmodelElementCollection smc = (SubmodelElementCollection) sm.getSubmodelElements().get(6);
        assertAllowed(environment, ReferenceBuilder.forSubmodel(sm), authHeader);
        assertForbidden(environment, ReferenceBuilder.forSubmodel(sm));
        assertAllowed(environment, ReferenceBuilder.forSubmodel(sm, smc), authHeader);
        assertForbidden(environment, ReferenceBuilder.forSubmodel(sm, smc));
        assertAllowed(environment, ReferenceBuilder.forSubmodel(sm, smc, smc.getValue().get(0)), authHeader);
        assertForbidden(environment, ReferenceBuilder.forSubmodel(sm, smc, smc.getValue().get(0)));
        assertAllowed(environment, ReferenceBuilder.forSubmodel(environment.getSubmodels().get(0)), authHeader);
        assertForbidden(environment, ReferenceBuilder.forSubmodel(environment.getSubmodels().get(0)));
        assertAllowed(environment, ReferenceBuilder.forAas(environment.getAssetAdministrationShells().get(0)), authHeader);
        assertForbidden(environment, ReferenceBuilder.forAas(environment.getAssetAdministrationShells().get(0)));
        assertAllowed(environment, ReferenceBuilder.forConceptDescription(environment.getConceptDescriptions().get(0)), authHeader);
        assertForbidden(environment, ReferenceBuilder.forConceptDescription(environment.getConceptDescriptions().get(0)));
    }


    private void assertForbidden(Environment environment, Reference reference, Map<String, String> headers) throws Exception {
        HttpResponse<String> response = execute(environment, reference, headers);

        Assert.assertEquals(toHttpStatusCode(StatusCode.CLIENT_FORBIDDEN), getStatusCode(response));
    }


    private void assertForbidden(Environment environment, Reference reference) throws Exception {
        assertForbidden(environment, reference, Map.of());
    }


    private void assertAllowed(Environment environment, Reference reference, Map<String, String> headers) throws Exception {

        HttpResponse<String> response = execute(environment, reference, headers);

        Assert.assertEquals(toHttpStatusCode(StatusCode.SUCCESS), getStatusCode(response));
        Assert.assertEquals(EnvironmentHelper.resolve(reference, environment), jsonDeserializer.read(response.body(),
                keyTypeToClass(ReferenceHelper.getEffectiveKeyType(reference))));
    }


    private void assertAllowed(Environment environment, Reference reference) throws Exception {
        assertAllowed(environment, reference, Map.of());
    }


    private HttpResponse<String> execute(Environment environment, Reference reference, Map<String, String> headers) throws Exception {
        String path = null;
        Referable referable = EnvironmentHelper.resolve(reference, environment);
        if (referable instanceof AssetAdministrationShell aas) {
            path = apiPaths.aasRepository().assetAdministrationShell(aas.getId());
        }
        else if (referable instanceof Submodel sm) {
            path = apiPaths.submodelRepository().submodel(sm.getId());
        }
        else if (referable instanceof ConceptDescription cd) {
            path = apiPaths.conceptDescriptionRepository().conceptDescription(cd.getId());
        }
        else if (referable instanceof SubmodelElement) {
            path = apiPaths.submodelRepository()
                    .submodelInterface(((Submodel) EnvironmentHelper.resolve(
                            new DefaultReference.Builder().keys(ReferenceHelper.getRoot(reference)).build(), environment)))
                    .submodelElement(ReferenceHelper.toPath(reference));
        }

        return executeRequest(HttpMethod.GET, path, null, headers);
    }


    private static Class<? extends Referable> keyTypeToClass(KeyTypes key) {
        return Stream.concat(
                org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.util.ReflectionHelper.INTERFACES.stream(),
                org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.util.ReflectionHelper.INTERFACES_WITHOUT_DEFAULT_IMPLEMENTATION.stream())
                .filter(x -> x.getSimpleName().equals(EnumSerializer.serializeEnumName(key.name())))
                .findAny()
                .orElse(null);
    }

}
