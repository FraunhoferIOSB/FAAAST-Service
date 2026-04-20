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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper.toHttpStatusCode;
import static de.fraunhofer.iosb.ilt.faaast.service.test.model.AssetConnectionModelSimple.ENVIRONMENT;
import static de.fraunhofer.iosb.ilt.faaast.service.test.model.AssetConnectionModelSimple.INITIAL_VALUE;
import static de.fraunhofer.iosb.ilt.faaast.service.test.model.AssetConnectionModelSimple.NODE_ID_SOURCE_1;
import static de.fraunhofer.iosb.ilt.faaast.service.test.model.AssetConnectionModelSimple.NODE_ID_SOURCE_2;
import static de.fraunhofer.iosb.ilt.faaast.service.test.model.AssetConnectionModelSimple.NODE_ID_SOURCE_3;
import static de.fraunhofer.iosb.ilt.faaast.service.test.model.AssetConnectionModelSimple.REFERENCE_PROPERTY_1;
import static de.fraunhofer.iosb.ilt.faaast.service.test.model.AssetConnectionModelSimple.REFERENCE_PROPERTY_2;
import static de.fraunhofer.iosb.ilt.faaast.service.test.model.AssetConnectionModelSimple.REFERENCE_PROPERTY_3;
import static de.fraunhofer.iosb.ilt.faaast.service.test.model.AssetConnectionModelSimple.REFERENCE_PROPERTY_4;
import static de.fraunhofer.iosb.ilt.faaast.service.test.model.AssetConnectionModelSimple.REFERENCE_PROPERTY_SOURCE_1;
import static de.fraunhofer.iosb.ilt.faaast.service.test.model.AssetConnectionModelSimple.REFERENCE_PROPERTY_SOURCE_2;
import static de.fraunhofer.iosb.ilt.faaast.service.test.model.AssetConnectionModelSimple.REFERENCE_PROPERTY_SOURCE_3;
import static de.fraunhofer.iosb.ilt.faaast.service.test.model.AssetConnectionModelSimple.REFERENCE_PROPERTY_SOURCE_4;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.prosysopc.ua.stack.core.UserTokenType;
import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.HttpAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.OpcUaAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util.OpcUaHelper;
import de.fraunhofer.iosb.ilt.faaast.service.config.CertificateConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.OpcUaEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.memory.FileStorageInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.PatchSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.AbstractResponseWithPayload;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PatchSubmodelElementValueByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.UnsupportedModifierException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.AssetAdministrationShellElementWalker;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.DefaultAssetAdministrationShellElementVisitor;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.test.model.AssetConnectionModelRecursive;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.ApiPaths;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.json.JSONException;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AssetConnectionIT extends AbstractIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetConnectionIT.class);

    private static Service service;
    private static Path securityBaseDir;

    @BeforeClass
    public static void init() throws IOException {
        securityBaseDir = Files.createTempDirectory("asset-connection");
    }


    @After
    public void shutdown() {
        service.stop();
    }


    @Test
    public void testServiceStartInvalidAssetConnection() throws Exception {
        int portHttp = PortHelper.findFreePort();
        int portOpcUa = PortHelper.findFreePort();
        ServiceConfig config = serviceConfig(portHttp, portOpcUa);
        config.getAssetConnections().add(connectionOpcUa(portOpcUa, REFERENCE_PROPERTY_1, "invalid node id"));
        service = new Service(config);
        service.start();
        assertServiceAvailabilityHttp(portHttp);
    }


    @Test
    public void testServiceStartValidAssetConnection() throws Exception {
        int portHttp = PortHelper.findFreePort();
        int portOpcUa = PortHelper.findFreePort();
        ServiceConfig config = serviceConfig(portHttp, portOpcUa);
        config.getAssetConnections().add(connectionOpcUa(portOpcUa, REFERENCE_PROPERTY_1, NODE_ID_SOURCE_1));
        service = new Service(config);
        service.start();
        String newValue = "new value";
        setValue(REFERENCE_PROPERTY_SOURCE_1, newValue);
        awaitAssetConnected(service);
        assertValue(REFERENCE_PROPERTY_1, newValue);
    }


    @Test
    public void testAssetConnectionUpdateRuntime_addConnection() throws Exception {
        int portHttp = PortHelper.findFreePort();
        int portOpcUa = PortHelper.findFreePort();
        service = new Service(serviceConfig(portHttp, portOpcUa));
        service.start();
        assertServiceAvailabilityHttp(portHttp);
        assertValue(REFERENCE_PROPERTY_1, INITIAL_VALUE);
        List<AssetConnectionConfig> newConnections = List.of(connectionOpcUa(portOpcUa, REFERENCE_PROPERTY_1, NODE_ID_SOURCE_1));
        service.getAssetConnectionManager().updateConnections(null, newConnections);
        awaitAssetConnected(service);
        String newValue = "new value";
        setValue(REFERENCE_PROPERTY_SOURCE_1, newValue);
        assertValue(REFERENCE_PROPERTY_1, newValue);
    }


    @Test
    public void testAssetConnectionUpdateRuntime_complexUpdate() throws Exception {
        int portHttp = PortHelper.findFreePort();
        int portOpcUa = PortHelper.findFreePort();
        int portHttpAsset = PortHelper.findFreePort();
        int portOpcUaAsset = PortHelper.findFreePort();
        ServiceConfig config = serviceConfig(portHttp, portOpcUa);
        config.getAssetConnections().add(OpcUaAssetConnectionConfig.builder()
                .host(getOpcUaHost(portOpcUaAsset))
                .securityBaseDir(securityBaseDir)
                .valueProvider(REFERENCE_PROPERTY_1,
                        OpcUaValueProviderConfig.builder()
                                .nodeId(NODE_ID_SOURCE_1)
                                .build())
                .valueProvider(REFERENCE_PROPERTY_2,
                        OpcUaValueProviderConfig.builder()
                                .nodeId(NODE_ID_SOURCE_2)
                                .build())
                .build());
        config.getAssetConnections().add(OpcUaAssetConnectionConfig.builder()
                .host(getOpcUaHost(portOpcUaAsset))
                .securityBaseDir(securityBaseDir)
                .valueProvider(REFERENCE_PROPERTY_3,
                        OpcUaValueProviderConfig.builder()
                                .nodeId(NODE_ID_SOURCE_3)
                                .build())
                .build());
        Service serviceAsset = new Service(serviceConfig(portHttpAsset, portOpcUaAsset));
        serviceAsset.start();
        assertServiceAvailabilityOpcUa(portOpcUaAsset);
        assertServiceAvailabilityHttp(portHttpAsset);
        String newValue1 = "new value 1";
        setValue(serviceAsset, REFERENCE_PROPERTY_SOURCE_1, newValue1);
        setValue(serviceAsset, REFERENCE_PROPERTY_SOURCE_2, newValue1);
        setValue(serviceAsset, REFERENCE_PROPERTY_SOURCE_3, newValue1);
        setValue(serviceAsset, REFERENCE_PROPERTY_SOURCE_4, newValue1);

        service = new Service(config);
        service.start();
        awaitAssetConnected(service);
        assertValue(REFERENCE_PROPERTY_1, newValue1);
        assertValue(REFERENCE_PROPERTY_2, newValue1);
        assertValue(REFERENCE_PROPERTY_3, newValue1);
        assertValue(REFERENCE_PROPERTY_4, INITIAL_VALUE);

        List<AssetConnectionConfig> oldConfigs = List.of(OpcUaAssetConnectionConfig.builder()
                .host(getOpcUaHost(portOpcUaAsset))
                .securityBaseDir(securityBaseDir)
                .valueProvider(REFERENCE_PROPERTY_1,
                        OpcUaValueProviderConfig.builder()
                                .nodeId(NODE_ID_SOURCE_1)
                                .build())
                .valueProvider(REFERENCE_PROPERTY_2,
                        OpcUaValueProviderConfig.builder()
                                .nodeId(NODE_ID_SOURCE_2)
                                .build())
                .build());
        List<AssetConnectionConfig> newConfigs = List.of(OpcUaAssetConnectionConfig.builder()
                .host(getOpcUaHost(portOpcUaAsset))
                .securityBaseDir(securityBaseDir)
                .valueProvider(REFERENCE_PROPERTY_1,
                        OpcUaValueProviderConfig.builder()
                                .nodeId(NODE_ID_SOURCE_1)
                                .build())
                .valueProvider(REFERENCE_PROPERTY_3,
                        OpcUaValueProviderConfig.builder()
                                .nodeId(NODE_ID_SOURCE_3)
                                .build())
                .build(),
                connectionHttp(portHttpAsset, REFERENCE_PROPERTY_4, REFERENCE_PROPERTY_SOURCE_4));
        service.getAssetConnectionManager().updateConnections(oldConfigs, newConfigs);
        awaitAssetConnected(service);
        String newValue2 = "new value 2";
        setValue(serviceAsset, REFERENCE_PROPERTY_SOURCE_1, newValue2);
        setValue(serviceAsset, REFERENCE_PROPERTY_SOURCE_2, newValue2);
        setValue(serviceAsset, REFERENCE_PROPERTY_SOURCE_3, newValue2);
        setValue(serviceAsset, REFERENCE_PROPERTY_SOURCE_4, newValue2);

        assertValue(REFERENCE_PROPERTY_1, newValue2);
        assertValue(REFERENCE_PROPERTY_2, newValue1);
        assertValue(REFERENCE_PROPERTY_3, newValue2);
        assertValue(REFERENCE_PROPERTY_4, newValue2);
        serviceAsset.stop();
        service.stop();
    }


    private void assertPropertyValuesRecursive(Reference reference, String expectedValue) throws URISyntaxException {
        getPropertiesRecursive(reference).forEach(x -> assertEquals(
                String.format("Property '%s' does not have expected value", x.getIdShort()),
                expectedValue,
                x.getValue()));
    }


    private List<Property> getPropertiesRecursive(Reference reference) throws URISyntaxException {
        Referable element;
        Request request = ReferenceHelper.getEffectiveKeyType(reference) == KeyTypes.SUBMODEL
                ? GetSubmodelRequest.builder()
                        .submodelId(SubmodelElementIdentifier.fromReference(reference).getSubmodelId())
                        .build()
                : GetSubmodelElementByPathRequest.builder()
                        .submodelId(SubmodelElementIdentifier.fromReference(reference).getSubmodelId())
                        .path(SubmodelElementIdentifier.fromReference(reference).getIdShortPath().toString())
                        .build();
        AbstractResponseWithPayload<? extends Referable> response = (AbstractResponseWithPayload<? extends Referable>) service.execute(request);
        if (!response.getStatusCode().isSuccess()) {
            throw new RuntimeException("failed to get value for reference " + ReferenceHelper.asString(reference));
        }
        element = response.getPayload();
        final List<Property> result = new ArrayList<>();
        AssetAdministrationShellElementWalker.builder()
                .visitor(new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(Property property) {
                        result.add(property);
                    }
                })
                .build()
                .walk(element);
        return result;
    }


    @Test
    public void testAssetConnectionUpdateRuntime_readValueProvidersRecursive() throws Exception {
        int portHttp = PortHelper.findFreePort();
        ServiceConfig config = ServiceConfig.builder()
                .core(CoreConfig.DEFAULT)
                .persistence(PersistenceInMemoryConfig.builder()
                        .initialModel(DeepCopyHelper.deepCopy(ENVIRONMENT))
                        .build())
                .fileStorage(new FileStorageInMemoryConfig())
                .endpoint(HttpEndpointConfig.builder()
                        .port(portHttp)
                        .certificate(CertificateConfig.builder()
                                .keyStorePath(httpEndpointKeyStoreFile)
                                .keyStoreType(HTTP_ENDPOINT_KEYSTORE_TYPE)
                                .keyStorePassword(HTTP_ENDPOINT_KEYSTORE_PASSWORD)
                                .build())
                        .build())
                .messageBus(MessageBusInternalConfig.builder()
                        .build())
                .build();
        config.getPersistence().setInitialModel(DeepCopyHelper.deepCopy(AssetConnectionModelRecursive.ENVIRONMENT));
        final HttpAssetConnectionConfig assetConnectionConfig = HttpAssetConnectionConfig.builder()
                .baseUrl("https://localhost:" + portHttp)
                .trustedCertificates(CertificateConfig.builder()
                        .keyStorePath(httpEndpointKeyStoreFile)
                        .keyStoreType(HTTP_ENDPOINT_KEYSTORE_TYPE)
                        .keyStorePassword(HTTP_ENDPOINT_KEYSTORE_PASSWORD)
                        .build())
                .valueProviders(
                        AssetConnectionModelRecursive.PROPERTY_REFERENCES.stream()
                                .collect(Collectors.toMap(
                                        x -> x,
                                        x -> HttpValueProviderConfig.builder()
                                                .format("JSON")
                                                .path(String.format("https://localhost:%s/api/v3.0/submodels/%s/submodel-elements/%s/$value",
                                                        portHttp,
                                                        EncodingHelper.base64UrlEncode(AssetConnectionModelRecursive.SUBMODEL_SOURCE.getId()),
                                                        EncodingHelper.urlEncode(AssetConnectionModelRecursive.PROPERTY_SOURCE.getIdShort())))
                                                .query("$." + AssetConnectionModelRecursive.PROPERTY_SOURCE.getIdShort())
                                                .build())))
                .build();
        config.getAssetConnections().add(assetConnectionConfig);

        service = new Service(config);
        service.start();
        awaitAssetConnected(service);
        assertPropertyValuesRecursive(AssetConnectionModelRecursive.REFERENCE_SUBMODEL, AssetConnectionModelRecursive.INITIAL_VALUE);
        int count = 1;
        for (var containerReference: AssetConnectionModelRecursive.CONTAINER_REFERENCES) {
            String newValue = "newValue" + count;
            setValue(AssetConnectionModelRecursive.REFERENCE_PROPERTY_SOURCE, newValue);
            await().atMost(10000, TimeUnit.MILLISECONDS)
                    .until(() -> !service.getAssetConnectionManager().hasPendingWrites());
            assertPropertyValuesRecursive(containerReference, newValue);
            count++;
        }
        service.stop();
    }


    @Test
    public void testServiceStartValidAssetConnectionDelayed() throws Exception {
        int portHttp = PortHelper.findFreePort();
        int portOpcUa = PortHelper.findFreePort();
        int portHttpAsset = PortHelper.findFreePort();
        int portOpcUaAsset = PortHelper.findFreePort();
        ServiceConfig config = serviceConfig(portHttp, portOpcUa);
        config.getAssetConnections().add(connectionOpcUa(portOpcUaAsset, REFERENCE_PROPERTY_1, NODE_ID_SOURCE_1));
        service = new Service(config);
        service.start();
        assertServiceAvailabilityHttp(portHttp);
        assertValue(REFERENCE_PROPERTY_1, INITIAL_VALUE);
        Service serviceAsset = new Service(serviceConfig(portHttpAsset, portOpcUaAsset));
        serviceAsset.start();
        assertServiceAvailabilityOpcUa(portOpcUaAsset);
        awaitAssetConnected(service);
        String newValue = "new value";
        setValue(serviceAsset, REFERENCE_PROPERTY_SOURCE_1, newValue);
        assertValue(REFERENCE_PROPERTY_1, newValue);
        serviceAsset.stop();
    }


    private static String getOpcUaHost(int port) {
        return "opc.tcp://localhost:" + port;
    }


    private AssetConnectionConfig connectionOpcUa(int port, Reference reference, String nodeId) throws IOException {
        return OpcUaAssetConnectionConfig.builder()
                .host(getOpcUaHost(port))
                .securityBaseDir(securityBaseDir)
                .valueProvider(reference,
                        OpcUaValueProviderConfig.builder()
                                .nodeId(nodeId)
                                .build())
                .build();
    }


    private AssetConnectionConfig connectionHttp(int port, Reference target, Reference source) throws IOException, URISyntaxException {
        ApiPaths paths = new ApiPaths(HOST, port);

        SubmodelElementIdentifier identifier = SubmodelElementIdentifier.fromReference(source);
        URI uri = new URI(paths.submodelRepository()
                .submodelInterface(identifier.getSubmodelId())
                .submodelElement(identifier.getIdShortPath(), Content.VALUE));
        String baseUrl = uri.getScheme() + "://" + uri.getAuthority();
        String urlPath = uri.getRawPath() + (uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "");
        String idShort = identifier.getIdShortPath().getElements().get(identifier.getIdShortPath().getElements().size() - 1);
        return HttpAssetConnectionConfig.builder()
                .baseUrl(baseUrl)
                .trustedCertificates(CertificateConfig.builder()
                        .keyStorePath(httpEndpointKeyStoreFile)
                        .keyStoreType(HTTP_ENDPOINT_KEYSTORE_TYPE)
                        .keyStorePassword(HTTP_ENDPOINT_KEYSTORE_PASSWORD)
                        .build())
                .valueProvider(target,
                        HttpValueProviderConfig.builder()
                                .format("JSON")
                                .path(urlPath)
                                .query("$." + idShort)
                                .build())
                .build();
    }


    private void setValue(Reference reference, String value) throws ResourceNotFoundException, PersistenceException, ValueFormatException {
        setValue(service, reference, value);
    }


    private void setValue(Service service, Reference reference, String value) throws ResourceNotFoundException, PersistenceException, ValueFormatException {
        Property property = (Property) service.getPersistence().getSubmodelElement(reference, QueryModifier.MINIMAL, Property.class);
        property.setValue(value);
        PatchSubmodelElementValueByPathResponse response = service.execute(PatchSubmodelElementValueByPathRequest.builder()
                .submodelId(SubmodelElementIdentifier.fromReference(reference).getSubmodelId())
                .path(SubmodelElementIdentifier.fromReference(reference).getIdShortPath().toString())
                .value(PropertyValue.of(Datatype.STRING, value))
                .build());
        assertTrue(response.getStatusCode().isSuccess());
    }


    private static ServiceConfig serviceConfig(int portHttp, int portOpcUa) {
        return ServiceConfig.builder()
                .core(CoreConfig.DEFAULT)
                .persistence(PersistenceInMemoryConfig.builder()
                        .initialModel(DeepCopyHelper.deepCopy(ENVIRONMENT))
                        .build())
                .fileStorage(new FileStorageInMemoryConfig())
                .endpoint(OpcUaEndpointConfig.builder()
                        .tcpPort(portOpcUa)
                        .supportedAuthentication(UserTokenType.Anonymous)
                        .build())
                .endpoint(HttpEndpointConfig.builder()
                        .port(portHttp)
                        .certificate(CertificateConfig.builder()
                                .keyStorePath(httpEndpointKeyStoreFile)
                                .keyStoreType(HTTP_ENDPOINT_KEYSTORE_TYPE)
                                .keyStorePassword(HTTP_ENDPOINT_KEYSTORE_PASSWORD)
                                .build())
                        .build())
                .messageBus(MessageBusInternalConfig.builder()
                        .build())
                .build();
    }


    private void assertServiceAvailabilityHttp(int port)
            throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        assertExecutePage(
                HttpMethod.GET,
                new ApiPaths(HOST, port).aasRepository().assetAdministrationShells(),
                StatusCode.SUCCESS,
                null,
                null,
                AssetAdministrationShell.class);
    }


    private void assertServiceAvailabilityOpcUa(int port) throws IOException, DeserializationException, InterruptedException, URISyntaxException, SerializationException,
            AssetConnectionException, ConfigurationInitializationException, UaException, ExecutionException {
        OpcUaClient client = OpcUaHelper.connect(OpcUaAssetConnectionConfig.builder()
                .securityBaseDir(securityBaseDir)
                .host(getOpcUaHost(port))
                .build());
        DataValue value = OpcUaHelper.readValue(client, NODE_ID_SOURCE_1);
        assertTrue(value.getStatusCode().isGood());
    }


    private void assertValue(Reference reference, Object expectedValue)
            throws IOException, InterruptedException, URISyntaxException, JSONException, NoSuchAlgorithmException, KeyManagementException {
        GetSubmodelElementByPathResponse response = service.execute(GetSubmodelElementByPathRequest.builder()
                .submodelId(SubmodelElementIdentifier.fromReference(reference).getSubmodelId())
                .path(SubmodelElementIdentifier.fromReference(reference).getIdShortPath().toString())
                .build());
        if (!response.getStatusCode().isSuccess()) {
            throw new RuntimeException("failed to get property value for reference " + ReferenceHelper.asString(reference));
        }
        String actual = ((Property) response.getPayload()).getValue();
        assertEquals(expectedValue.toString(), actual);
    }


    private <T> Page<T> assertExecutePage(HttpMethod method, String url, StatusCode statusCode, Object input, List<T> expected, Class<T> type)
            throws IOException, InterruptedException, URISyntaxException, SerializationException, DeserializationException, NoSuchAlgorithmException, KeyManagementException,
            UnsupportedModifierException {
        HttpResponse response = HttpHelper.execute(httpClient, method, url, input);
        assertEquals(toHttpStatusCode(statusCode), response.statusCode());
        Page<T> actual = HttpHelper.readResponsePage(response, type);
        if (expected != null) {
            assertEquals(expected, actual.getContent());
        }
        return actual;
    }


    private void awaitAssetConnected(Service service) {
        await().atMost(60, TimeUnit.SECONDS)
                .with()
                .pollInterval(1, TimeUnit.SECONDS)
                .until(() -> service.getAssetConnectionManager().isFullyConnected());
    }

}
