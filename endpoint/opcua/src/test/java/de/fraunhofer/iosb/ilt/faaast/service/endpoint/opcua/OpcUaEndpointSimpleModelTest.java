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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua;

import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaAddress;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.builtintypes.UnsignedShort;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.BrowseDirection;
import com.prosysopc.ua.stack.core.BrowsePathResult;
import com.prosysopc.ua.stack.core.BrowsePathTarget;
import com.prosysopc.ua.stack.core.EndpointDescription;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.ReferenceDescription;
import com.prosysopc.ua.stack.core.RelativePath;
import com.prosysopc.ua.stack.core.RelativePathElement;
import com.prosysopc.ua.stack.core.ServerState;
import com.prosysopc.ua.stack.core.UserTokenType;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import com.prosysopc.ua.stack.transport.security.SecurityPolicy;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.CommonAttributesData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestConstants;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestService;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestUtils;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PostSubmodelElementRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PostSubmodelElementResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import opc.ua.aas.ReferenceTypeIds;
import opc.ua.aas.VariableIds;
import opc.ua.aas.datatypes.AASEntityEnumType;
import opc.ua.aas.datatypes.AASKey;
import opc.ua.aas.datatypes.AASKeyTypes;
import opc.ua.aas.datatypes.AASModellingKind;
import opc.ua.aas.datatypes.AASReference;
import opc.ua.aas.datatypes.AASReferenceTypes;
import opc.ua.aas.objecttypes.AASEntityType;
import opc.ua.aas.objecttypes.AASRelationshipElementType;
import org.awaitility.Awaitility;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ModellingKind;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test class for the general OPC UA Endpoint test with the simple example
 */
public class OpcUaEndpointSimpleModelTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaEndpointSimpleModelTest.class);

    private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);
    private static final Duration MAX_TIMEOUT = Duration.ofSeconds(5);

    private static int opcTcpPort;
    private static String endpointUrl;

    private static TestService service;
    private static int aasns;

    private UaClient client;

    @BeforeClass
    public static void startTest() throws ConfigurationException, AssetConnectionException, MessageBusException, EndpointException, PersistenceException {
        opcTcpPort = PortHelper.findFreePort();
        endpointUrl = "opc.tcp://localhost:" + opcTcpPort;

        OpcUaEndpointConfig config = new OpcUaEndpointConfig.Builder()
                .tcpPort(opcTcpPort)
                .secondsTillShutdown(0)
                .supportedAuthentication(UserTokenType.Anonymous)
                .serverCertificateBasePath(TestConstants.SERVER_CERT_PATH)
                .userCertificateBasePath(TestConstants.USER_CERT_PATH)
                .discoveryServerUrl(null)
                .build();

        service = new TestService(config, null, false);
        service.start();
    }


    @AfterClass
    public static void stopTest() {
        if (service != null) {
            service.stop();
        }
    }


    @After
    public void shutdown() {
        if (client != null) {
            if (client.hasConnected()) {
                client.disconnect();
            }
            client = null;
        }
    }


    @Test
    public void testOpcUaEndpoint()
            throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException, AddressSpaceException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();

        DataValue value = client.readValue(Identifiers.Server_ServerStatus_State);
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Assert.assertEquals(ServerState.Running.ordinal(), value.getValue().intValue());

        // browse for AAS Environment
        List<ReferenceDescription> refs = client.getAddressSpace().browse(Identifiers.ObjectsFolder);
        Assert.assertNotNull("Browse ObjectsFolder Refs Null", refs);
        Assert.assertFalse("Browse ObjectsFolder Refs empty", refs.isEmpty());
        NodeId envNode = null;
        for (ReferenceDescription ref: refs) {
            if (ref.getBrowseName().getName().equals(TestConstants.AAS_ENVIRONMENT_NAME)) {
                envNode = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
                break;
            }
        }

        Assert.assertNotNull("AASEnvironment Null", envNode);

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        // browse AAS Environment
        refs = client.getAddressSpace().browse(envNode);
        Assert.assertNotNull("Browse Environment Refs Null", refs);
        Assert.assertTrue("Browse Environment Refs empty", !refs.isEmpty());
        NodeId aasNode = null;
        NodeId submodelDocNode = null;
        NodeId submodelTechDataNode = null;
        NodeId submodelOperDataNode = null;
        for (ReferenceDescription ref: refs) {
            switch (ref.getBrowseName().getName()) {
                case TestConstants.SIMPLE_AAS_NAME -> aasNode = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
                case TestConstants.SUBMODEL_DOC_NODE_NAME -> submodelDocNode = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
                case TestConstants.SUBMODEL_OPER_DATA_NODE_NAME -> submodelOperDataNode = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
                case TestConstants.SUBMODEL_TECH_DATA_NODE_NAME -> submodelTechDataNode = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
                default -> {
                }
            }
        }

        Assert.assertNotNull("AAS Node not found", aasNode);
        Assert.assertNotNull("Submodel Documentation Node not found", submodelDocNode);
        Assert.assertNotNull("Submodel TechnicalData Node not found", submodelTechDataNode);
        Assert.assertNotNull("Submodel OperationalData Node not found", submodelOperDataNode);

        TestUtils.checkCommonAttributes(client, aasNode, aasns, new CommonAttributesData("1", "2", null, "http://customer.com/aas/9175_7013_7091_9168"));

        // check Browse and Display Names
        TestUtils.checkBrowseName(client, aasNode, TestConstants.SIMPLE_AAS_NAME);
        TestUtils.checkDisplayName(client, aasNode, "AAS:" + TestConstants.SIMPLE_AAS_NAME);
        TestUtils.checkDisplayName(client, submodelDocNode, TestConstants.SUBMODEL_PREFIX + TestConstants.SUBMODEL_DOC_NODE_NAME);

        // Submodels
        testSubmodelDoc(submodelDocNode);
        testSubmodelOperationalData(submodelOperDataNode);
        testSubmodelTechnicalData(submodelTechDataNode);

        // AAS
        refs = client.getAddressSpace().browse(aasNode);
        Assert.assertNotNull("Browse AASNode Refs Null", refs);
        Assert.assertFalse("Browse AASNode Refs empty", refs.isEmpty());

        testAas(aasNode);
    }


    @Test
    public void testWritePropertyValue() throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.TEST_PROPERTY_NAME)));
        //browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWritePropertyValue Browse Result Null", bpres);
        Assert.assertEquals("testWritePropertyValue Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWritePropertyValue Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWritePropertyValue ValueType Null", targets);
        Assert.assertTrue("testWritePropertyValue ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        TestUtils.writeNewValueIntern(client, writeNode, 50, 222);
    }


    // Test temporarily deactivated
    //@Test
    public void testWriteRangeValue() throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.TEST_RANGE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.RANGE_MAX_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteRangeValue Browse Result Null", bpres);
        Assert.assertEquals("testWriteRangeValue Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWriteRangeValue Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteRangeValue ValueType Null", targets);
        Assert.assertTrue("testWriteRangeValue ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        TestUtils.writeNewValueIntern(client, writeNode, 100, 111);
    }


    @Test
    public void testWriteMultiLanguagePropertyValue() throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.TEST_MULTI_LAN_PROP_NAME)));
        //browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteMultiLanguagePropertyValue Browse Result Null", bpres);
        Assert.assertEquals("testWriteMultiLanguagePropertyValue Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWriteMultiLanguagePropertyValue Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteMultiLanguagePropertyValue ValueType Null", targets);
        Assert.assertTrue("testWriteMultiLanguagePropertyValue ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        List<LocalizedText> oldValue = new ArrayList<>();
        oldValue.add(new LocalizedText("Example value of a MultiLanguageProperty element", "en-us"));
        oldValue.add(new LocalizedText("Beispielswert für ein MulitLanguageProperty-Element", "de"));

        // The DataElementValueMapper changes the order of the elements in some cases
        List<LocalizedText> newValue = new ArrayList<>();
        newValue.add(new LocalizedText("Beispielswert2 fuer ein anderes MulitLanguageProperty-Element", "de"));
        newValue.add(new LocalizedText("Example value of a MultiLanguageProperty element", "en-us"));

        TestUtils.writeNewValueArray(client, writeNode, oldValue.toArray(LocalizedText[]::new), newValue.toArray(LocalizedText[]::new));
    }


    @Test
    public void testWriteReferenceElementValue() throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.TEST_REF_ELEM_NAME)));
        //browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        //browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.KEYS_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteReferenceElementValue Browse Result Null", bpres);
        Assert.assertEquals("testWriteReferenceElementValue Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWriteReferenceElementValue Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteReferenceElementValue ValueType Null", targets);
        Assert.assertTrue("testWriteReferenceElementValue ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        List<AASKey> oldKeys = new ArrayList<>();
        oldKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.Submodel), TestConstants.SUBMODEL_TECH_DATA_NAME));
        oldKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.Property), TestConstants.MAX_ROTATION_SPEED_NAME));
        AASReference oldValue = new AASReference(AASReferenceTypes.of(UnsignedShort.valueOf(0)), null, oldKeys.toArray(AASKey[]::new));

        // The DataElementValueMapper changes the order of the elements
        List<AASKey> newKeys = new ArrayList<>();
        newKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.Submodel), TestConstants.SUBMODEL_TECH_DATA_NAME));
        newKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.Property), "Another property"));
        //AASReference newValue = new AASReference(null, null, newKeys.toArray(AASKey[]::new));
        AASReference newValue = new AASReference(AASReferenceTypes.of(AASReferenceTypes.Options.ModelReference), null, newKeys.toArray(AASKey[]::new));

        //TestUtils.writeNewValueArray(client, writeNode, oldValue.toArray(AASKey[]::new), newValue.toArray(AASKey[]::new));
        TestUtils.writeNewValueReference(client, writeNode, oldValue, newValue);
    }


    @Test
    public void testWriteEntityType() throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.TEST_ENTITY_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasComponent, false, true, new QualifiedName(aasns, AASEntityType.ENTITY_TYPE)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteEntityType Browse Result Null", bpres);
        Assert.assertEquals("testWriteEntityType Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWriteEntityType Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteEntityType ValueType Null", targets);
        Assert.assertTrue("testWriteEntityType ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        AASEntityEnumType oldValue = AASEntityEnumType.of(AASEntityEnumType.Options.CoManagedEntity);
        AASEntityEnumType newValue = AASEntityEnumType.of(AASEntityEnumType.Options.SelfManagedEntity);

        TestUtils.writeNewValueIntern(client, writeNode, oldValue.asBuiltInType(), newValue.asBuiltInType());
    }


    @Test
    public void testAddProperty() throws SecureIdentityException, IOException, ServiceException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        String propName = "NewProperty99";

        // make sure the element doesn't exist yet
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_TECH_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, propName)));
        //browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testAddProperty Browse Result Null", bpres);
        Assert.assertEquals("testAddProperty Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testAddProperty Browse Result Bad", bpres[0].getStatusCode().isBad());

        PostSubmodelElementRequest request = new PostSubmodelElementRequest.Builder()
                .submodelId("http://i40.customer.com/type/1/1/7A7104BDAB57E184")
                .submodelElement(new DefaultProperty.Builder()
                        .idShort(propName)
                        .category("Variable")
                        .value("AZF45")
                        .valueType(DataTypeDefXsd.STRING)
                        .build())
                .build();
        PostSubmodelElementResponse response = (PostSubmodelElementResponse) service.execute(request);
        Assert.assertEquals(de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode.SUCCESS_CREATED, response.getStatusCode());

        // check that the element is there now
        // unable to deterministically know when the changes will materialize, therefore wait for some time
        Awaitility.await()
                .alias("check value updated in OPC UA endpoint")
                .pollInterval(POLL_TIMEOUT)
                .atMost(MAX_TIMEOUT)
                .until(() -> {
                    BrowsePathResult[] bpr = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
                    return bpr != null && bpr.length == 1 && bpr[0].getStatusCode().isGood();
                });
    }


    @Test
    public void testAddSubmodel() throws SecureIdentityException, IOException, ServiceException, MessageBusException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        String submodelName = "NewSubmodelTest1";

        // make sure the element doesn't exist yet
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, submodelName)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_REL_ELEMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, AASRelationshipElementType.SECOND)));
        //browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.KEYS_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testAddSubmodel Browse Result Null", bpres);
        Assert.assertEquals("testAddSubmodel Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testAddSubmodel Browse Result Bad", bpres[0].getStatusCode().isBad());

        // Send event to MessageBus
        ElementCreateEventMessage msg = new ElementCreateEventMessage();
        msg.setElement(new DefaultReference.Builder()
                .type(ReferenceTypes.MODEL_REFERENCE)
                .keys(new DefaultKey.Builder().type(KeyTypes.ASSET_ADMINISTRATION_SHELL).value("http://customer.com/aas/9175_7013_7091_9168").build())
                .build());
        msg.setValue(new DefaultSubmodel.Builder()
                .idShort(submodelName)
                .id("https://acplt.org/NewSubmodelTest1")
                .administration(new DefaultAdministrativeInformation.Builder()
                        .version("0.9")
                        .revision("0")
                        .build())
                .kind(ModellingKind.INSTANCE)
                .submodelElements(new DefaultRelationshipElement.Builder()
                        .idShort("ExampleRelationshipElement")
                        .category("Parameter")
                        .description(new DefaultLangStringTextType.Builder().text("Example RelationshipElement object").language("en-us").build())
                        .description(new DefaultLangStringTextType.Builder().text("Beispiel RelationshipElement Element").language("de").build())
                        .semanticId(new DefaultReference.Builder()
                                .type(ReferenceTypes.MODEL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value("http://acplt.org/RelationshipElements/ExampleRelationshipElement")
                                        .build())
                                .build())
                        .first(new DefaultReference.Builder()
                                .type(ReferenceTypes.MODEL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.SUBMODEL)
                                        .value("https://acplt.org/Test_Submodel")
                                        .build())
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.SUBMODEL_ELEMENT_COLLECTION)
                                        .value("ExampleSubmodelCollectionOrdered")
                                        .build())
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.PROPERTY)
                                        .value("ExampleProperty")
                                        .build())
                                .build())
                        .second(new DefaultReference.Builder()
                                .type(ReferenceTypes.MODEL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.SUBMODEL)
                                        .value("http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial")
                                        .build())
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.ENTITY)
                                        .value("ExampleEntity")
                                        .build())
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.PROPERTY)
                                        .value("ExampleProperty2")
                                        .build())
                                .build())
                        .build())
                .build());
        service.getMessageBus().publish(msg);

        // check that the element is there now
        // unable to deterministically know when the changes will materialize, therefore wait for some time
        Awaitility.await()
                .alias("check value updated in OPC UA endpoint")
                .pollInterval(POLL_TIMEOUT)
                .atMost(MAX_TIMEOUT)
                .until(() -> {
                    BrowsePathResult[] bpr = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
                    return bpr != null && bpr.length == 1 && bpr[0].getStatusCode().isGood();
                });
    }


    @Test
    public void testSecurityPolicies() throws ConfigurationException, AssetConnectionException, MessageBusException, EndpointException, ServiceException, PersistenceException {
        Assert.assertTrue(testConfig(
                Set.of(SecurityPolicy.NONE),
                Set.of(UserTokenType.Anonymous)));
        Assert.assertTrue(testConfig(
                Set.of(SecurityPolicy.BASIC256SHA256),
                Set.of(UserTokenType.UserName)));
        Assert.assertTrue(testConfig(
                Set.of(SecurityPolicy.BASIC128RSA15),
                Set.of(UserTokenType.Anonymous,
                        UserTokenType.Certificate)));
        Assert.assertTrue(testConfig(
                SecurityPolicy.ALL_SECURE_104,
                Set.of(UserTokenType.Anonymous,
                        UserTokenType.UserName,
                        UserTokenType.Certificate)));
        Assert.assertTrue(testConfig(
                Set.of(SecurityPolicy.BASIC256SHA256,
                        SecurityPolicy.NONE,
                        SecurityPolicy.BASIC256),
                Set.of(UserTokenType.Anonymous,
                        UserTokenType.Certificate)));
        Assert.assertTrue(testConfig(
                Set.of(SecurityPolicy.BASIC256SHA256,
                        SecurityPolicy.AES128_SHA256_RSAOAEP,
                        SecurityPolicy.AES256_SHA256_RSAPSS,
                        SecurityPolicy.BASIC128RSA15),
                Set.of(UserTokenType.UserName,
                        UserTokenType.Certificate)));
        Assert.assertTrue(testConfig(
                Set.of(SecurityPolicy.NONE,
                        SecurityPolicy.BASIC256SHA256,
                        SecurityPolicy.AES128_SHA256_RSAOAEP,
                        SecurityPolicy.AES256_SHA256_RSAPSS,
                        SecurityPolicy.BASIC128RSA15),
                Set.of(UserTokenType.Certificate,
                        UserTokenType.Anonymous)));
    }


    private boolean testConfig(Set<SecurityPolicy> expectedPolicies, Set<UserTokenType> expectedUserTokens)
            throws ConfigurationException, AssetConnectionException, MessageBusException, EndpointException, ServiceException, PersistenceException {
        int port = PortHelper.findFreePort();
        String url = "opc.tcp://localhost:" + port;

        List<String> expectedPolicyUris = new ArrayList<>();
        expectedPolicies.stream().forEach(ep -> {
            expectedPolicyUris.add(ep.getPolicyUri());
        });
        OpcUaEndpointConfig config = new OpcUaEndpointConfig.Builder()
                .tcpPort(port)
                .secondsTillShutdown(0)
                .supportedAuthentication(UserTokenType.Anonymous)
                .serverCertificateBasePath(TestConstants.SERVER_CERT_PATH)
                .userCertificateBasePath(TestConstants.USER_CERT_PATH)
                .discoveryServerUrl(null)
                .supportedSecurityPolicies(expectedPolicies)
                .supportedAuthentications(expectedUserTokens)
                .build();

        TestService localService = new TestService(config, null, false);
        localService.start();

        UaClient discoveryClient = new UaClient();
        discoveryClient.setAddress(UaAddress.parse(url));
        List<String> currentPolicies = new ArrayList<>();
        List<UserTokenType> currentUserTokens = new ArrayList<>();
        for (EndpointDescription ed: discoveryClient.discoverEndpoints()) {
            if (!currentPolicies.contains(ed.getSecurityPolicyUri())) {
                LOGGER.info("testConfig: found SecurityPolicyUri {}", ed.getSecurityPolicyUri());
                currentPolicies.add(ed.getSecurityPolicyUri());
            }
            if (currentUserTokens.isEmpty()) {
                for (var t: ed.getUserIdentityTokens()) {
                    currentUserTokens.add(t.getTokenType());
                }
            }
        }

        LOGGER.info("testConfig: found {} policyUris and {} userTokens", currentPolicies.size(), currentUserTokens.size());
        Assert.assertEquals(expectedPolicies.size(), currentPolicies.size());
        Assert.assertTrue(
                expectedPolicyUris.size() == currentPolicies.size() && expectedPolicyUris.containsAll(currentPolicies) && currentPolicies.containsAll(expectedPolicyUris));
        Assert.assertTrue(
                expectedUserTokens.size() == currentUserTokens.size() && expectedUserTokens.containsAll(currentUserTokens) && currentUserTokens.containsAll(expectedUserTokens));
        localService.stop();
        return true;
    }


    private void testAas(NodeId aasNode)
            throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, aasNode, "AAS:" + TestConstants.SIMPLE_AAS_NAME);
        TestUtils.checkType(client, aasNode, TestConstants.AAS_AAS_TYPE_ID);
        TestUtils.checkCommonAttributes(client, aasNode, aasns, new CommonAttributesData("1", "2", "", "http://customer.com/aas/9175_7013_7091_9168"));

        //TestUtils.checkAdministrationNode(client, aasNode, aasns, "1", "2");
        //TestUtils.checkCategoryNode(client, aasNode, aasns, "");
        //TestUtils.checkEmbeddedDataSpecificationNode(client, aasNode, aasns);
        TestUtils.checkAssetInformationNode(client, aasNode, aasns);
        testSubmodelRefs(aasNode, aasns);
    }


    private void testSubmodelDoc(NodeId submodelNode)
            throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, submodelNode, TestConstants.SUBMODEL_PREFIX + TestConstants.SUBMODEL_DOC_NODE_NAME);
        TestUtils.checkType(client, submodelNode, TestConstants.AAS_SUBMODEL_TYPE_ID);
        //TestUtils.checkType(client, submodelNode, ObjectTypeIds.AASSubmodelType);

        String submodelName = "SubmodelOperationalData";

        NodeId operatingManualNode = null;

        List<ReferenceDescription> refs = client.getAddressSpace().browse(submodelNode);
        Assert.assertNotNull("Browse " + submodelName + " Refs Null", refs);
        Assert.assertFalse("Browse " + submodelName + " Refs empty", refs.isEmpty());
        for (ReferenceDescription ref: refs) {
            NodeId nid = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
            switch (ref.getBrowseName().getName()) {
                case TestConstants.OPERATING_MANUAL_NAME -> operatingManualNode = nid;
                default -> {
                }
            }
        }

        Assert.assertNotNull(submodelName + " OperatingManual Node not found", operatingManualNode);

        TestUtils.checkCommonAttributes(client, submodelNode, aasns,
                new CommonAttributesData("11", "159", "", TestConstants.SUBMODEL_DOC_NAME, null, null, AASModellingKind.of(AASModellingKind.Options.Instance), new ArrayList<>()));

        //TestUtils.checkAdministrationNode(client, submodelNode, aasns, );
        //TestUtils.checkModelingKindNode(client, submodelNode, aasns, AASModellingKind.of(AASModellingKind.Options.Instance));
        //TestUtils.checkCategoryNode(client, submodelNode, aasns, "");
        //TestUtils.checkEmbeddedDataSpecificationNode(client, submodelNode, aasns);
        //TestUtils.checkQualifierNode(client, submodelNode, aasns, new ArrayList<>());
        testOperatingManual(operatingManualNode);
    }


    private void testSubmodelOperationalData(NodeId submodelNode) throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, submodelNode, TestConstants.SUBMODEL_PREFIX + TestConstants.SUBMODEL_OPER_DATA_NODE_NAME);
        TestUtils.checkType(client, submodelNode, TestConstants.AAS_SUBMODEL_TYPE_ID);
        //TestUtils.checkType(client, submodelNode, ObjectTypeIds.AASSubmodelType);

        TestUtils.checkCommonAttributes(client, submodelNode, aasns,
                new CommonAttributesData(null, null, "", TestConstants.SUBMODEL_OPER_DATA_NAME, null, null, AASModellingKind.of(AASModellingKind.Options.Instance),
                        new ArrayList<>()));

        testEntity(submodelNode);

        //TestUtils.checkAdministrationNode(client, submodelNode, aasns, null, null);
        //TestUtils.checkCategoryNode(client, submodelNode, aasns, "");
        //TestUtils.checkModelingKindNode(client, submodelNode, aasns, AASModellingKind.of(AASModellingKind.Options.Instance));
        //TestUtils.checkEmbeddedDataSpecificationNode(client, submodelNode, aasns);
        //TestUtils.checkQualifierNode(client, submodelNode, aasns, new ArrayList<>());
        TestUtils.checkAasPropertyObject(client, submodelNode, aasns, TestConstants.ROTATION_SPEED_NAME, "VARIABLE", Datatype.INTEGER,
                new BigDecimal(4370), new ArrayList<>());
    }


    private void testSubmodelTechnicalData(NodeId submodelNode) throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, submodelNode, TestConstants.SUBMODEL_PREFIX + TestConstants.SUBMODEL_TECH_DATA_NODE_NAME);
        TestUtils.checkType(client, submodelNode, TestConstants.AAS_SUBMODEL_TYPE_ID);
        //TestUtils.checkType(client, submodelNode, ObjectTypeIds.AASSubmodelType);

        TestUtils.checkCommonAttributes(client, submodelNode, aasns,
                new CommonAttributesData(null, null, "", TestConstants.SUBMODEL_TECH_DATA_NAME,
                        new AASReference(AASReferenceTypes.of(AASReferenceTypes.Options.ExternalReference), null,
                                List.of(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.GlobalReference), "0173-1#01-AFZ615#016")).toArray(AASKey[]::new)),
                        null, AASModellingKind.of(AASModellingKind.Options.Instance), new ArrayList<>()));

        //TestUtils.checkAdministrationNode(client, submodelNode, aasns, null, null);
        //TestUtils.checkCategoryNode(client, submodelNode, aasns, "");
        // no kind available here, check for null
        //TestUtils.checkModelingKindNode(client, submodelNode, aasns, AASModellingKind.of(AASModellingKind.Options.Instance));
        //TestUtils.checkEmbeddedDataSpecificationNode(client, submodelNode, aasns);
        //TestUtils.checkQualifierNode(client, submodelNode, aasns, new ArrayList<>());
        TestUtils.checkAasPropertyObject(client, submodelNode, aasns, TestConstants.MAX_ROTATION_SPEED_NAME, "PARAMETER",
                Datatype.INTEGER, new BigDecimal(5000), new ArrayList<>());
        TestUtils.checkAasPropertyObject(client, submodelNode, aasns, TestConstants.DECIMAL_PROPERTY, "PARAMETER",
                Datatype.DECIMAL, new BigDecimal(123456), new ArrayList<>());

        TestUtils.checkSubmodelElementConceptDescription(client, submodelNode, TestConstants.MAX_ROTATION_SPEED_NAME, aasns, "0173-1#02-BAA120#008", "2", "1");
    }


    private void testOperatingManual(NodeId node) throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, node, TestConstants.OPERATING_MANUAL_NAME);
        TestUtils.checkType(client, node, TestConstants.AAS_SUBMODEL_ELEM_COLL_TYPE_ID);
        TestUtils.checkSubmodelElementCommonAttributes(client, aasns, node, null, new ArrayList<>());
        //TestUtils.checkCategoryNode(client, node, aasns, "");
        //TestUtils.checkEmbeddedDataSpecificationNode(client, node, aasns);
        //TestUtils.checkQualifierNode(client, node, aasns, new ArrayList<>());

        // browse for SubmodelElements
        List<ReferenceDescription> refs = client.getAddressSpace().browse(node, BrowseDirection.Forward,
                client.getAddressSpace().getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasComponent));
        Assert.assertNotNull(refs);
        Assert.assertEquals(2, refs.size());
        Assert.assertEquals(QualifiedName.from(aasns, "Title"), refs.get(0).getBrowseName());
        Assert.assertEquals(QualifiedName.from(aasns, "DigitalFile_PDF"), refs.get(1).getBrowseName());

        // check ConceptDescription
        TestUtils.checkConceptDescription(client, node, aasns, TestConstants.OPERATING_MANUAL_CONCEPT_DESCRIPTION, null, null);

        TestUtils.checkAasPropertyFile(client, node, aasns, "DigitalFile_PDF", AASModellingKind.of(AASModellingKind.Options.Instance), "", "application/pdf",
                "file:///aasx/OperatingManual.pdf", 0);
    }


    private void testSubmodelRefs(NodeId baseNode, int aasns)
            throws ServiceException, ServiceResultException, AddressSpaceException, StatusException {
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_REF_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(baseNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testSubmodelRefs Browse Result Null", bpres);
        Assert.assertEquals("testSubmodelRefs Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testSubmodelRefs Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testSubmodelRefs Target Null", targets);
        Assert.assertTrue("testSubmodelRefs Target empty", targets.length > 0);
        NodeId refNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        Assert.assertNotNull("testSubmodelRefs RefNode Null", refNode);
        TestUtils.checkType(client, refNode, Identifiers.BaseDataVariableType);

        List<AASReference> refs = new ArrayList<>();
        refs.add(new AASReference(AASReferenceTypes.of(AASReferenceTypes.Options.ModelReference), null,
                List.of(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.Submodel), TestConstants.SUBMODEL_TECH_DATA_NAME)).toArray(AASKey[]::new)));
        refs.add(new AASReference(AASReferenceTypes.of(AASReferenceTypes.Options.ModelReference), null,
                List.of(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.Submodel), TestConstants.SUBMODEL_OPER_DATA_NAME)).toArray(AASKey[]::new)));
        refs.add(new AASReference(AASReferenceTypes.of(AASReferenceTypes.Options.ModelReference), null,
                List.of(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.Submodel), TestConstants.SUBMODEL_DOC_NAME)).toArray(AASKey[]::new)));

        TestUtils.checkSubmodelRefs(client, refNode, aasns, refs);
        //TestUtils.checkSubmodelRef(client, refNode, aasns, TestConstants.SUBMODEL_TECH_DATA_NAME, submodelTechDataNode);
        //TestUtils.checkSubmodelRef(client, refNode, aasns, TestConstants.SUBMODEL_OPER_DATA_NAME, submodelOperDataNode);
        //TestUtils.checkSubmodelRef(client, refNode, aasns, TestConstants.SUBMODEL_DOC_NAME, submodelDocNode);
    }


    private void testEntity(NodeId submodelNode) throws ServiceException, ServiceResultException, AddressSpaceException, StatusException {
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.TEST_ENTITY_NAME)));
        //browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        //browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.KEYS_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(submodelNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testEntity Browse Result Null", bpres);
        Assert.assertEquals("testEntity Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testEntity Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testEntity Entity Null", targets);
        Assert.assertTrue("testEntity Entity empty", targets.length > 0);

        NodeId entityNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        TestUtils.checkDescriptions(client, entityNode, List.of(LocalizedText.from(
                "Legally valid designation of the natural or judicial person which is directly responsible for the design, production, packaging and labeling of a product in respect to its being brought into circulation.",
                "en-us")));

        // browse for Statements
        List<ReferenceDescription> refs = client.getAddressSpace().browse(entityNode, BrowseDirection.Forward,
                client.getAddressSpace().getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasComponent));
        Assert.assertNotNull(refs);
        Assert.assertEquals(3, refs.size());
        Assert.assertEquals(QualifiedName.from(aasns, "ExampleProperty2"), refs.get(0).getBrowseName());
        Assert.assertEquals(QualifiedName.from(aasns, "ExampleProperty"), refs.get(1).getBrowseName());
        Assert.assertEquals(QualifiedName.from(aasns, "ExampleCollection1"), refs.get(2).getBrowseName());
    }
}
