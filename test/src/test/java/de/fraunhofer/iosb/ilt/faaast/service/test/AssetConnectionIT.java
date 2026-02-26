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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.PatchSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PatchSubmodelElementValueByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.UnsupportedModifierException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.ApiPaths;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
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
    private static final String initialValue = "initial value";
    private static final Property propertySource1 = new DefaultProperty.Builder()
            .idShort("source1")
            .value(initialValue)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    private static final Property propertySource2 = new DefaultProperty.Builder()
            .idShort("source2")
            .value(initialValue)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    private static final Property propertySource3 = new DefaultProperty.Builder()
            .idShort("source3")
            .value(initialValue)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    private static final Property propertySource4 = new DefaultProperty.Builder()
            .idShort("source4")
            .value(initialValue)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    private static final Submodel submodelSource = new DefaultSubmodel.Builder()
            .idShort("SubmodelSource")
            .id("http://example.org/submodel/source")
            .submodelElements(propertySource1)
            .submodelElements(propertySource2)
            .submodelElements(propertySource3)
            .submodelElements(propertySource4)
            .build();
    private static final Property propertyTarget1 = new DefaultProperty.Builder()
            .idShort("target1")
            .value(initialValue)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    private static final Property propertyTarget2 = new DefaultProperty.Builder()
            .idShort("target2")
            .value(initialValue)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    private static final Property propertyTarget3 = new DefaultProperty.Builder()
            .idShort("target3")
            .value(initialValue)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    private static final Property propertyTarget4 = new DefaultProperty.Builder()
            .idShort("target4")
            .value(initialValue)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    private static final Submodel submodelTarget = new DefaultSubmodel.Builder()
            .idShort("SubmodelTarget")
            .id("http://example.org/submodel/target")
            .submodelElements(propertyTarget1)
            .submodelElements(propertyTarget2)
            .submodelElements(propertyTarget3)
            .submodelElements(propertyTarget4)
            .build();
    private static final Reference referenceSubmodelSource = ReferenceBuilder.forSubmodel(submodelSource);
    private static final Reference referenceSubmodelTarget = ReferenceBuilder.forSubmodel(submodelTarget);
    private static final Reference referencePropertySource1 = ReferenceBuilder.forSubmodel(submodelSource, propertySource1);
    private static final Reference referencePropertySource2 = ReferenceBuilder.forSubmodel(submodelSource, propertySource2);
    private static final Reference referencePropertySource3 = ReferenceBuilder.forSubmodel(submodelSource, propertySource3);
    private static final Reference referencePropertySource4 = ReferenceBuilder.forSubmodel(submodelSource, propertySource4);
    private static final Reference referencePropertyTarget1 = ReferenceBuilder.forSubmodel(submodelTarget, propertyTarget1);
    private static final Reference referencePropertyTarget2 = ReferenceBuilder.forSubmodel(submodelTarget, propertyTarget2);
    private static final Reference referencePropertyTarget3 = ReferenceBuilder.forSubmodel(submodelTarget, propertyTarget3);
    private static final Reference referencePropertyTarget4 = ReferenceBuilder.forSubmodel(submodelTarget, propertyTarget4);
    private static final Environment environment = new DefaultEnvironment.Builder()
            .assetAdministrationShells(new DefaultAssetAdministrationShell.Builder()
                    .idShort("AAS1")
                    .id("https://example.org/aas/1")
                    .submodels(referenceSubmodelSource)
                    .submodels(referenceSubmodelTarget)
                    .build())
            .submodels(submodelSource)
            .submodels(submodelTarget)
            .build();
    private static final String nodeIdSource1 = "ns=3;s=1.Value";
    private static final String nodeIdSource2 = "ns=3;s=2.Value";
    private static final String nodeIdSource3 = "ns=3;s=3.Value";
    private static final String nodeIdSource4 = "ns=3;s=4.Value";

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
        config.getAssetConnections().add(connectionOpcUa(portOpcUa, referencePropertyTarget1, "invalid node id"));
        service = new Service(config);
        service.start();
        assertServiceAvailabilityHttp(portHttp);
    }


    @Test
    public void testServiceStartValidAssetConnection() throws Exception {
        int portHttp = PortHelper.findFreePort();
        int portOpcUa = PortHelper.findFreePort();
        ServiceConfig config = serviceConfig(portHttp, portOpcUa);
        config.getAssetConnections().add(connectionOpcUa(portOpcUa, referencePropertyTarget1, nodeIdSource1));
        service = new Service(config);
        service.start();
        String newValue = "new value";
        setValue(referencePropertySource1, newValue);
        awaitAssetConnected(service);
        assertValue(referencePropertyTarget1, newValue);
    }


    @Test
    public void testAssetConnectionUpdateRuntime_addConnection() throws Exception {
        int portHttp = PortHelper.findFreePort();
        int portOpcUa = PortHelper.findFreePort();
        service = new Service(serviceConfig(portHttp, portOpcUa));
        service.start();
        assertServiceAvailabilityHttp(portHttp);
        assertValue(referencePropertyTarget1, initialValue);
        List<AssetConnectionConfig> newConnections = List.of(
                connectionOpcUa(portOpcUa, referencePropertyTarget1, nodeIdSource1));
        service.getAssetConnectionManager().updateConnections(null, newConnections);
        awaitAssetConnected(service);
        String newValue = "new value";
        setValue(referencePropertySource1, newValue);
        assertValue(referencePropertyTarget1, newValue);
    }


    @Test
    public void testAssetConnectionUpdateRuntime_complexUpdate() throws Exception {
        int portHttp = PortHelper.findFreePort();
        int portOpcUa = PortHelper.findFreePort();
        int portHttpAsset = PortHelper.findFreePort();
        int portOpcUaAsset = PortHelper.findFreePort();
        ServiceConfig config = serviceConfig(portHttp, portOpcUa);
        config.getAssetConnections().add(
                OpcUaAssetConnectionConfig.builder()
                        .host("opc.tcp://localhost:" + portOpcUaAsset)
                        .securityBaseDir(securityBaseDir)
                        .valueProvider(referencePropertyTarget1,
                                OpcUaValueProviderConfig.builder()
                                        .nodeId(nodeIdSource1)
                                        .build())
                        .valueProvider(referencePropertyTarget2,
                                OpcUaValueProviderConfig.builder()
                                        .nodeId(nodeIdSource2)
                                        .build())
                        .build());
        config.getAssetConnections().add(
                OpcUaAssetConnectionConfig.builder()
                        .host("opc.tcp://localhost:" + portOpcUaAsset)
                        .securityBaseDir(securityBaseDir)
                        .valueProvider(referencePropertyTarget3,
                                OpcUaValueProviderConfig.builder()
                                        .nodeId(nodeIdSource3)
                                        .build())
                        .build());
        Service serviceAsset = new Service(serviceConfig(portHttpAsset, portOpcUaAsset));
        serviceAsset.start();
        assertServiceAvailabilityOpcUa(portOpcUaAsset);
        assertServiceAvailabilityHttp(portHttpAsset);
        String newValue1 = "new value 1";
        setValue(serviceAsset, referencePropertySource1, newValue1);
        setValue(serviceAsset, referencePropertySource2, newValue1);
        setValue(serviceAsset, referencePropertySource3, newValue1);
        setValue(serviceAsset, referencePropertySource4, newValue1);

        service = new Service(config);
        service.start();
        awaitAssetConnected(service);
        assertValue(referencePropertyTarget1, newValue1);
        assertValue(referencePropertyTarget2, newValue1);
        assertValue(referencePropertyTarget3, newValue1);
        assertValue(referencePropertyTarget4, initialValue);

        List<AssetConnectionConfig> oldConfigs = List.of(
                OpcUaAssetConnectionConfig.builder()
                        .host("opc.tcp://localhost:" + portOpcUaAsset)
                        .securityBaseDir(securityBaseDir)
                        .valueProvider(referencePropertyTarget1,
                                OpcUaValueProviderConfig.builder()
                                        .nodeId(nodeIdSource1)
                                        .build())
                        .valueProvider(referencePropertyTarget2,
                                OpcUaValueProviderConfig.builder()
                                        .nodeId(nodeIdSource2)
                                        .build())
                        .build());
        List<AssetConnectionConfig> newConfigs = List.of(
                OpcUaAssetConnectionConfig.builder()
                        .host("opc.tcp://localhost:" + portOpcUaAsset)
                        .securityBaseDir(securityBaseDir)
                        .valueProvider(referencePropertyTarget1,
                                OpcUaValueProviderConfig.builder()
                                        .nodeId(nodeIdSource1)
                                        .build())
                        .valueProvider(referencePropertyTarget3,
                                OpcUaValueProviderConfig.builder()
                                        .nodeId(nodeIdSource3)
                                        .build())
                        .build(),
                connectionHttp(portHttpAsset, referencePropertyTarget4, referencePropertySource4));
        service.getAssetConnectionManager().updateConnections(oldConfigs, newConfigs);
        awaitAssetConnected(service);
        String newValue2 = "new value 2";
        setValue(serviceAsset, referencePropertySource1, newValue2);
        setValue(serviceAsset, referencePropertySource2, newValue2);
        setValue(serviceAsset, referencePropertySource3, newValue2);
        setValue(serviceAsset, referencePropertySource4, newValue2);

        assertValue(referencePropertyTarget1, newValue2);
        assertValue(referencePropertyTarget2, newValue1);
        assertValue(referencePropertyTarget3, newValue2);
        assertValue(referencePropertyTarget4, newValue2);
        serviceAsset.stop();
        service.stop();
    }


    @Test
    public void testServiceStartValidAssetConnectionDelayed() throws Exception {
        int portHttp = PortHelper.findFreePort();
        int portOpcUa = PortHelper.findFreePort();
        int portHttpAsset = PortHelper.findFreePort();
        int portOpcUaAsset = PortHelper.findFreePort();
        ServiceConfig config = serviceConfig(portHttp, portOpcUa);
        config.getAssetConnections().add(connectionOpcUa(portOpcUaAsset, referencePropertyTarget1, nodeIdSource1));
        service = new Service(config);
        service.start();
        assertServiceAvailabilityHttp(portHttp);
        assertValue(referencePropertyTarget1, initialValue);
        Service serviceAsset = new Service(serviceConfig(portHttpAsset, portOpcUaAsset));
        serviceAsset.start();
        assertServiceAvailabilityOpcUa(portOpcUaAsset);
        awaitAssetConnected(service);
        String newValue = "new value";
        setValue(serviceAsset, referencePropertySource1, newValue);
        assertValue(referencePropertyTarget1, newValue);
        serviceAsset.stop();
    }


    private AssetConnectionConfig connectionOpcUa(int port, Reference reference, String nodeId) throws IOException {
        return OpcUaAssetConnectionConfig.builder()
                .host("opc.tcp://localhost:" + port)
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
                        .initialModel(DeepCopyHelper.deepCopy(environment))
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
                .host("opc.tcp://localhost:" + port)
                .build());
        DataValue value = OpcUaHelper.readValue(client, nodeIdSource1);
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
