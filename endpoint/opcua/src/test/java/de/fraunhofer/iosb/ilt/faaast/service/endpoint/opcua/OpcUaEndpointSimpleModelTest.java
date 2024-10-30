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
import com.prosysopc.ua.stack.common.ServiceResultException;
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
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import opc.i4aas.VariableIds;
import opc.i4aas.datatypes.AASDataTypeDefXsd;
import opc.i4aas.datatypes.AASKeyDataType;
import opc.i4aas.datatypes.AASKeyTypesDataType;
import opc.i4aas.datatypes.AASModellingKindDataType;
import opc.i4aas.objecttypes.AASEntityType;
import opc.i4aas.objecttypes.AASRelationshipElementType;
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
        LOGGER.trace("stopTest");

        if (service != null) {
            service.stop();
        }
    }


    @Test
    public void testOpcUaEndpoint()
            throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException, AddressSpaceException {
        UaClient client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("testOpcUaEndpoint: client connected");

        DataValue value = client.readValue(Identifiers.Server_ServerStatus_State);
        System.out.println(value);
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
                case TestConstants.SIMPLE_AAS_NAME:
                    aasNode = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
                    break;
                case TestConstants.SUBMODEL_DOC_NODE_NAME:
                    submodelDocNode = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
                    break;
                case TestConstants.SUBMODEL_OPER_DATA_NODE_NAME:
                    submodelOperDataNode = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
                    break;
                case TestConstants.SUBMODEL_TECH_DATA_NODE_NAME:
                    submodelTechDataNode = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
                    break;
                default:
                    break;
            }
        }

        Assert.assertNotNull("AAS Node not found", aasNode);
        Assert.assertNotNull("Submodel Documentation Node not found", submodelDocNode);
        Assert.assertNotNull("Submodel TechnicalData Node not found", submodelTechDataNode);
        Assert.assertNotNull("Submodel OperationalData Node not found", submodelOperDataNode);

        // check Browse and Display Names
        TestUtils.checkBrowseName(client, aasNode, TestConstants.SIMPLE_AAS_NAME);
        TestUtils.checkDisplayName(client, aasNode, "AAS:" + TestConstants.SIMPLE_AAS_NAME);
        TestUtils.checkDisplayName(client, submodelDocNode, "Submodel:" + TestConstants.SUBMODEL_DOC_NODE_NAME);

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        // Submodels
        testSubmodelDoc(client, submodelDocNode);
        testSubmodelOperationalData(client, submodelOperDataNode);
        testSubmodelTechnicalData(client, submodelTechDataNode);

        // AAS
        refs = client.getAddressSpace().browse(aasNode);
        Assert.assertNotNull("Browse AASNode Refs Null", refs);
        Assert.assertFalse("Browse AASNode Refs empty", refs.isEmpty());

        testAas(client, aasNode, submodelDocNode, submodelOperDataNode, submodelTechDataNode);

        System.out.println("disconnect client");
        client.disconnect();
    }


    @Test
    public void testWritePropertyValue() throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        UaClient client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("testWritePropertyValue: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.TEST_PROPERTY_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
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

        System.out.println("disconnect client");
        client.disconnect();
    }


    @Test
    public void testWriteRangeValue() throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        UaClient client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("testWriteRangeValue: client connected");

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

        System.out.println("disconnect client");
        client.disconnect();
    }


    @Test
    public void testWriteMultiLanguagePropertyValue() throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        UaClient client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("testWriteMultiLanguagePropertyValue: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.TEST_MULTI_LAN_PROP_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
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

        System.out.println("disconnect client");
        client.disconnect();
    }


    @Test
    public void testWriteReferenceElementValue() throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        UaClient client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.TEST_REF_ELEM_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.KEYS_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteReferenceElementValue Browse Result Null", bpres);
        Assert.assertEquals("testWriteReferenceElementValue Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWriteReferenceElementValue Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteReferenceElementValue ValueType Null", targets);
        Assert.assertTrue("testWriteReferenceElementValue ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        List<AASKeyDataType> oldValue = new ArrayList<>();
        oldValue.add(new AASKeyDataType(AASKeyTypesDataType.Submodel, TestConstants.SUBMODEL_TECH_DATA_NAME));
        oldValue.add(new AASKeyDataType(AASKeyTypesDataType.Property, TestConstants.MAX_ROTATION_SPEED_NAME));

        // The DataElementValueMapper changes the order of the elements
        List<AASKeyDataType> newValue = new ArrayList<>();
        newValue.add(new AASKeyDataType(AASKeyTypesDataType.Submodel, TestConstants.SUBMODEL_TECH_DATA_NAME));
        newValue.add(new AASKeyDataType(AASKeyTypesDataType.Property, "Another property"));

        TestUtils.writeNewValueArray(client, writeNode, oldValue.toArray(AASKeyDataType[]::new), newValue.toArray(AASKeyDataType[]::new));

        System.out.println("testWriteReferenceElementValue: disconnect client");
        client.disconnect();
    }


    @Test
    public void testWriteEntityType() throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        UaClient client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("testWriteEntityType: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.TEST_ENTITY_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, AASEntityType.ENTITY_TYPE)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteEntityType Browse Result Null", bpres);
        Assert.assertEquals("testWriteEntityType Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWriteEntityType Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteEntityType ValueType Null", targets);
        Assert.assertTrue("testWriteEntityType ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        TestUtils.writeNewValueIntern(client, writeNode, 0, 1);

        System.out.println("disconnect client");
        client.disconnect();
    }


    @Test
    public void testAddProperty() throws SecureIdentityException, IOException, ServiceException {
        UaClient client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("testAddProperty: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        String propName = "NewProperty99";

        // make sure the element doesn't exist yet
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_TECH_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, propName)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
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

        System.out.println("disconnect client");
        client.disconnect();
    }


    @Test
    public void testAddSubmodel() throws SecureIdentityException, IOException, ServiceException, MessageBusException {
        UaClient client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("testAddProperty: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        String submodelName = "NewSubmodelTest1";

        // make sure the element doesn't exist yet
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, submodelName)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_REL_ELEMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, AASRelationshipElementType.SECOND)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.KEYS_VALUE_NAME)));
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

        System.out.println("disconnect client");
        client.disconnect();
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


    private void testAas(UaClient client, NodeId aasNode, NodeId submodelDocNode, NodeId submodelOperDataNode, NodeId submodelTechDataNode)
            throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, aasNode, "AAS:" + TestConstants.SIMPLE_AAS_NAME);
        TestUtils.checkType(client, aasNode, new NodeId(aasns, TestConstants.AAS_AAS_TYPE_ID));
        TestUtils.checkIdentification(client, aasNode, aasns, "http://customer.com/aas/9175_7013_7091_9168");
        TestUtils.checkAdministrationNode(client, aasNode, aasns, "1", "2");
        TestUtils.checkCategoryNode(client, aasNode, aasns, "");
        TestUtils.checkEmbeddedDataSpecificationNode(client, aasNode, aasns);
        TestUtils.checkAssetInformationNode(client, aasNode, aasns);
        testSubmodelRefs(client, aasNode, aasns, submodelDocNode, submodelOperDataNode, submodelTechDataNode);
    }


    private void testSubmodelDoc(UaClient client, NodeId submodelNode)
            throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, submodelNode, "Submodel:" + TestConstants.SUBMODEL_DOC_NODE_NAME);
        TestUtils.checkType(client, submodelNode, new NodeId(aasns, TestConstants.AAS_SUBMODEL_TYPE_ID));

        String submodelName = "SubmodelOperationalData";

        NodeId operatingManualNode = null;

        List<ReferenceDescription> refs = client.getAddressSpace().browse(submodelNode);
        Assert.assertNotNull("Browse " + submodelName + " Refs Null", refs);
        Assert.assertFalse("Browse " + submodelName + " Refs empty", refs.isEmpty());
        for (ReferenceDescription ref: refs) {
            NodeId nid = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
            switch (ref.getBrowseName().getName()) {
                case TestConstants.OPERATING_MANUAL_NAME:
                    operatingManualNode = nid;
                    break;
                default:
                    break;
            }
        }

        Assert.assertNotNull(submodelName + " OperatingManual Node not found", operatingManualNode);

        TestUtils.checkIdentification(client, submodelNode, aasns, TestConstants.SUBMODEL_DOC_NAME);
        TestUtils.checkAdministrationNode(client, submodelNode, aasns, "11", "159");
        TestUtils.checkModelingKindNode(client, submodelNode, aasns, AASModellingKindDataType.Instance);
        TestUtils.checkCategoryNode(client, submodelNode, aasns, "");
        TestUtils.checkEmbeddedDataSpecificationNode(client, submodelNode, aasns);
        TestUtils.checkQualifierNode(client, submodelNode, aasns, new ArrayList<>());
        testOperatingManual(client, operatingManualNode);
    }


    private void testSubmodelOperationalData(UaClient client, NodeId submodelNode) throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, submodelNode, "Submodel:" + TestConstants.SUBMODEL_OPER_DATA_NODE_NAME);
        TestUtils.checkType(client, submodelNode, new NodeId(aasns, TestConstants.AAS_SUBMODEL_TYPE_ID));

        TestUtils.checkIdentification(client, submodelNode, aasns, TestConstants.SUBMODEL_OPER_DATA_NAME);
        TestUtils.checkAdministrationNode(client, submodelNode, aasns, null, null);
        TestUtils.checkCategoryNode(client, submodelNode, aasns, "");
        TestUtils.checkModelingKindNode(client, submodelNode, aasns, AASModellingKindDataType.Instance);
        TestUtils.checkEmbeddedDataSpecificationNode(client, submodelNode, aasns);
        TestUtils.checkQualifierNode(client, submodelNode, aasns, new ArrayList<>());
        TestUtils.checkAasPropertyObject(client, submodelNode, aasns, TestConstants.ROTATION_SPEED_NAME, "VARIABLE", AASDataTypeDefXsd.Integer,
                "4370", new ArrayList<>());
    }


    private void testSubmodelTechnicalData(UaClient client, NodeId submodelNode) throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, submodelNode, "Submodel:" + TestConstants.SUBMODEL_TECH_DATA_NODE_NAME);
        TestUtils.checkType(client, submodelNode, new NodeId(aasns, TestConstants.AAS_SUBMODEL_TYPE_ID));

        TestUtils.checkIdentification(client, submodelNode, aasns, TestConstants.SUBMODEL_TECH_DATA_NAME);
        TestUtils.checkAdministrationNode(client, submodelNode, aasns, null, null);
        TestUtils.checkCategoryNode(client, submodelNode, aasns, "");
        // no kind available here, check for null
        TestUtils.checkModelingKindNode(client, submodelNode, aasns, AASModellingKindDataType.Instance);
        TestUtils.checkEmbeddedDataSpecificationNode(client, submodelNode, aasns);
        TestUtils.checkQualifierNode(client, submodelNode, aasns, new ArrayList<>());
        TestUtils.checkAasPropertyObject(client, submodelNode, aasns, TestConstants.MAX_ROTATION_SPEED_NAME, "PARAMETER",
                AASDataTypeDefXsd.Integer, "5000", new ArrayList<>());
        TestUtils.checkAasPropertyObject(client, submodelNode, aasns, TestConstants.DECIMAL_PROPERTY, "PARAMETER",
                AASDataTypeDefXsd.Decimal, "123456", new ArrayList<>());
    }


    private void testOperatingManual(UaClient client, NodeId node) throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, node, TestConstants.OPERATING_MANUAL_NAME);
        TestUtils.checkType(client, node, new NodeId(aasns, TestConstants.AAS_SUBMODEL_ELEM_COLL_TYPE_ID));
        TestUtils.checkCategoryNode(client, node, aasns, "");
        TestUtils.checkEmbeddedDataSpecificationNode(client, node, aasns);
        TestUtils.checkQualifierNode(client, node, aasns, new ArrayList<>());
        TestUtils.checkAasPropertyFile(client, node, aasns, "DigitalFile_PDF", AASModellingKindDataType.Instance, "", "application/pdf", "file:///aasx/OperatingManual.pdf", 0);
    }


    private void testSubmodelRefs(UaClient client, NodeId baseNode, int aasns, NodeId submodelDocNode, NodeId submodelOperDataNode, NodeId submodelTechDataNode)
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
        TestUtils.checkType(client, refNode, new NodeId(aasns, TestConstants.AAS_REFERENCE_LIST_ID));

        TestUtils.checkSubmodelRef(client, refNode, aasns, TestConstants.SUBMODEL_DOC_NAME, submodelDocNode);
        TestUtils.checkSubmodelRef(client, refNode, aasns, TestConstants.SUBMODEL_OPER_DATA_NAME, submodelOperDataNode);
        TestUtils.checkSubmodelRef(client, refNode, aasns, TestConstants.SUBMODEL_TECH_DATA_NAME, submodelTechDataNode);
    }
}
