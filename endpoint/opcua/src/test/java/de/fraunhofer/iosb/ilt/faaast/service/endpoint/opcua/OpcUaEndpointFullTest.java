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

import com.prosysopc.ua.MethodCallStatusException;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.BrowsePathResult;
import com.prosysopc.ua.stack.core.BrowsePathTarget;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.ReferenceDescription;
import com.prosysopc.ua.stack.core.RelativePath;
import com.prosysopc.ua.stack.core.RelativePathElement;
import com.prosysopc.ua.stack.core.ServerState;
import com.prosysopc.ua.stack.core.StatusCodes;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestConstants;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestService;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestUtils;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.assetconnection.TestAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.assetconnection.TestOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.DateTimeValue;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Qualifier;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultQualifier;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import opc.i4aas.AASEntityType;
import opc.i4aas.AASIdentifierTypeDataType;
import opc.i4aas.AASKeyDataType;
import opc.i4aas.AASKeyElementsDataType;
import opc.i4aas.AASKeyTypeDataType;
import opc.i4aas.AASModelingKindDataType;
import opc.i4aas.AASRelationshipElementType;
import opc.i4aas.AASValueTypeDataType;
import opc.i4aas.VariableIds;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test class for the general OPC UA Endpoint test with the full example
 *
 * @author Tino Bischoff
 */
public class OpcUaEndpointFullTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaEndpointFullTest.class);

    private static final int OPC_TCP_PORT = 18123;
    private static final long DEFAULT_TIMEOUT = 500;

    private static final String ENDPOINT_URL = "opc.tcp://localhost:" + OPC_TCP_PORT;

    private static TestService service;
    private static int aasns;

    /**
     * Initialize and start the test.
     *
     * @throws Exception If the operation fails
     */
    @BeforeClass
    public static void startTest() throws Exception {

        OpcUaEndpointConfig config = new OpcUaEndpointConfig();
        config.setTcpPort(OPC_TCP_PORT);
        config.setSecondsTillShutdown(0);

        TestAssetConnectionConfig assetConnectionConfig = new TestAssetConnectionConfig();

        // register Test Operation
        List<Key> keys = new ArrayList<>();
        keys.add(new DefaultKey.Builder().type(KeyElements.SUBMODEL).idType(KeyType.IRI).value("https://acplt.org/Test_Submodel3").build());
        keys.add(new DefaultKey.Builder().type(KeyElements.OPERATION).idType(KeyType.ID_SHORT).value("ExampleOperation").build());
        Reference ref = new DefaultReference.Builder().keys(keys).build();
        List<OperationVariable> outputArgs = new ArrayList<>();
        outputArgs.add(new DefaultOperationVariable.Builder().value(new DefaultProperty.Builder().idShort("Test Output 1").valueType("string").value("XYZ1").build()).build());
        assetConnectionConfig.setOperationProviders(new HashMap<Reference, TestOperationProviderConfig>() {
            {
                put(ref, new TestOperationProviderConfig(outputArgs));
            }
        });

        service = new TestService(config, assetConnectionConfig, true);
        service.start();
    }


    /**
     * Stop the test.
     */
    @AfterClass
    public static void stopTest() {
        LOGGER.trace("stopTest");

        if (service != null) {
            service.stop();
        }
    }


    /**
     * Test method for testing the OPC UA Endpoint
     *
     * @throws InterruptedException If the operation fails
     * @throws Exception If the operation fails
     */
    @Test
    public void testOpcUaEndpointFull() throws InterruptedException, Exception {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        Assert.assertTrue("client not connected", client.isConnected());
        System.out.println("client connected");

        DataValue value = client.readValue(Identifiers.Server_ServerStatus_State);
        System.out.println(value);
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Assert.assertEquals(ServerState.Running.ordinal(), value.getValue().intValue());

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

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

        NodeId submodel1Node = null;
        for (ReferenceDescription ref: refs) {
            NodeId rid = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
            switch (ref.getBrowseName().getName()) {
                case TestConstants.FULL_SUBMODEL_1_NAME: {
                    submodel1Node = rid;
                    break;
                }
                default:
                    //intentionally left empty
            }
        }

        Assert.assertNotNull("Submodel 1 Node not found", submodel1Node);

        testSubmodel1(client, submodel1Node);

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for writing a RelationshipElement. Writes the property in the
     * OPC UA Server and checks the new value in the server.
     *
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws StatusException If the operation fails
     * @throws InterruptedException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    @Test
    public void testWriteRelationshipElementValue() throws SecureIdentityException, IOException, ServiceException, StatusException, InterruptedException, ServiceResultException {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_4_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_REL_ELEMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, AASRelationshipElementType.SECOND)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.KEYS_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteRelationshipElementValue Browse Result Null", bpres);
        Assert.assertEquals("testWriteRelationshipElementValue Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWriteRelationshipElementValue Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteRelationshipElementValue ValueType Null", targets);
        Assert.assertTrue("testWriteRelationshipElementValue ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        List<AASKeyDataType> oldValue = new ArrayList<>();
        oldValue.add(new AASKeyDataType(AASKeyElementsDataType.Submodel, "https://acplt.org/Test_Submodel_Mandatory", AASKeyTypeDataType.IRI));
        oldValue.add(new AASKeyDataType(AASKeyElementsDataType.SubmodelElementCollection, "ExampleSubmodelCollectionOrdered", AASKeyTypeDataType.IdShort));
        oldValue.add(new AASKeyDataType(AASKeyElementsDataType.MultiLanguageProperty, "ExampleMultiLanguageProperty", AASKeyTypeDataType.IdShort));

        // The DataElementValueMapper changes the order of the elements
        List<AASKeyDataType> newValue = new ArrayList<>();
        newValue.add(new AASKeyDataType(AASKeyElementsDataType.Submodel, "https://acplt.org/Test_Submodel_Mandatory", AASKeyTypeDataType.IRI));
        newValue.add(new AASKeyDataType(AASKeyElementsDataType.SubmodelElementCollection, "ExampleSubmodelCollectionOrdered", AASKeyTypeDataType.IdShort));
        newValue.add(new AASKeyDataType(AASKeyElementsDataType.Range, "ExampleRange", AASKeyTypeDataType.IdShort));

        TestUtils.writeNewValueArray(client, writeNode, oldValue.toArray(AASKeyDataType[]::new), newValue.toArray(AASKeyDataType[]::new));

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for writing a Value of a SubmodelElementCollection. Writes
     * the property in the OPC UA Server and checks the new value in the server.
     *
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws StatusException If the operation fails
     * @throws InterruptedException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    @Test
    public void testWriteSubmodelElementCollectionValue()
            throws SecureIdentityException, IOException, ServiceException, StatusException, InterruptedException, ServiceResultException {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_4_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SM_ELEM_COLL_UO_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SMEC_REL_ELEM_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.KEYS_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteSubmodelElementCollectionValue Browse Result Null", bpres);
        Assert.assertEquals("testWriteSubmodelElementCollectionValue Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWriteSubmodelElementCollectionValue Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteSubmodelElementCollectionValue ValueType Null", targets);
        Assert.assertTrue("testWriteSubmodelElementCollectionValue ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        // The DataElementValueMapper changes the order of the elements
        List<AASKeyDataType> newValue = new ArrayList<>();
        newValue.add(new AASKeyDataType(AASKeyElementsDataType.GlobalReference, "https://iosb.fraunhofer.de/TestValue1", AASKeyTypeDataType.IRI));

        TestUtils.writeNewValueArray(client, writeNode, null, newValue.toArray(AASKeyDataType[]::new));

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for writing a Value of a SubmodelElementCollection. Writes
     * the property in the OPC UA Server and checks the new value in the server.
     *
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws StatusException If the operation fails
     * @throws InterruptedException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    @Test
    public void testWriteSubmodelElementCollectionValue2()
            throws SecureIdentityException, IOException, ServiceException, StatusException, InterruptedException, ServiceResultException {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_6_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SM_ELEM_COLL_O_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SMEC_RANGE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.RANGE_MIN_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteSubmodelElementCollectionValue2 Browse Result Null", bpres);
        Assert.assertEquals("testWriteSubmodelElementCollectionValue2 Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWriteSubmodelElementCollectionValue2 Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteSubmodelElementCollectionValue2 ValueType Null", targets);
        Assert.assertTrue("testWriteSubmodelElementCollectionValue2 ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        TestUtils.writeNewValueIntern(client, writeNode, 0, 4);

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for writing a Value of a SubmodelElementCollection. Writes
     * the property in the OPC UA Server and checks the new value in the server.
     *
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws StatusException If the operation fails
     * @throws InterruptedException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    @Test
    public void testWriteSubmodelElementCollectionValue3()
            throws SecureIdentityException, IOException, ServiceException, StatusException, InterruptedException, ServiceResultException {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_7_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SM_ELEM_COLL_O_NAME)));
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

        // The DataElementValueMapper changes the order of the elements in some cases
        List<LocalizedText> newValue = new ArrayList<>();
        newValue.add(new LocalizedText("english test element", "en-us"));
        newValue.add(new LocalizedText("deutsches Test-Element", "de"));

        TestUtils.writeNewValueArray(client, writeNode, new ArrayList<>().toArray(LocalizedText[]::new), newValue.toArray(LocalizedText[]::new));

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for writing a GlobalAssetId of an Entity. Writes the property
     * in the OPC UA Server and checks the new value in the server.
     *
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws StatusException If the operation fails
     * @throws InterruptedException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    @Test
    public void testWriteEntityGlobalAssetId()
            throws SecureIdentityException, IOException, ServiceException, StatusException, InterruptedException, ServiceResultException {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("testWriteEntityGlobalAssetId: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_2_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_ENTITY2_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, AASEntityType.GLOBAL_ASSET_ID)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.KEYS_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteEntityGlobalAssetId Browse Result Null", bpres);
        Assert.assertEquals("testWriteEntityGlobalAssetId Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWriteEntityGlobalAssetId Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteEntityGlobalAssetId ValueType Null", targets);
        Assert.assertTrue("testWriteEntityGlobalAssetId ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        List<AASKeyDataType> oldValue = new ArrayList<>();
        oldValue.add(new AASKeyDataType(AASKeyElementsDataType.Asset, "https://acplt.org/Test_Asset2", AASKeyTypeDataType.IRI));

        List<AASKeyDataType> newValue = new ArrayList<>();
        newValue.add(new AASKeyDataType(AASKeyElementsDataType.Asset, "https://acplt2.org/Test_Asset3", AASKeyTypeDataType.IRI));

        TestUtils.writeNewValueArray(client, writeNode, oldValue.toArray(AASKeyDataType[]::new), newValue.toArray(AASKeyDataType[]::new));

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for successfully calling an operation.
     *
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws MethodCallStatusException If the operation fails
     */
    @Test
    public void testCallOperationSuccess() throws SecureIdentityException, IOException, ServiceException, ServiceResultException, MethodCallStatusException {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());
        int serverns = client.getAddressSpace().getNamespaceTable().getIndex(AasServiceNodeManager.NAMESPACE_URI);

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_3_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_OPERATION_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        browsePath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_3_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_OPERATION_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(serverns, TestConstants.FULL_OPERATION_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testCallOperationSuccess Browse Result Null", bpres);
        Assert.assertEquals("testCallOperationSuccess Browse Result: size doesn't match", 2, bpres.length);
        Assert.assertTrue("testCallOperationSuccess Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testCallOperationSuccess Object Targets Null", targets);
        Assert.assertTrue("testCallOperationSuccess Object Targets empty", targets.length > 0);

        NodeId objectNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        Assert.assertNotNull("testCallOperationSuccess objectNode Null", objectNode);

        targets = bpres[1].getTargets();
        Assert.assertNotNull("testCallOperationSuccess Method Targets Null", targets);
        Assert.assertTrue("testCallOperationSuccess Method Targets empty", targets.length > 0);

        NodeId methodNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        Assert.assertNotNull("testCallOperationSuccess methodNode Null", methodNode);

        Variant[] inputArguments = new Variant[1];
        inputArguments[0] = new Variant("123454");
        Variant[] outputs = client.call(objectNode, methodNode, inputArguments);
        Assert.assertNotNull("testCallOperationSuccess output Arguments Null", outputs);
        Assert.assertEquals("testCallOperationSuccess output Arguments length not equal", 1, outputs.length);
        Assert.assertEquals("testCallOperationSuccess output Argument 0 not equal", new Variant("XYZ1"), outputs[0]);

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for calling an operation with not enough arguments.
     *
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws MethodCallStatusException If the operation fails
     */
    @Test
    public void testCallOperationArgsMissing() throws SecureIdentityException, IOException, ServiceException, ServiceResultException, MethodCallStatusException {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());
        int serverns = client.getAddressSpace().getNamespaceTable().getIndex(AasServiceNodeManager.NAMESPACE_URI);

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_3_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_OPERATION_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        browsePath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_3_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_OPERATION_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(serverns, TestConstants.FULL_OPERATION_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testCallOperationArgsMissing Browse Result Null", bpres);
        Assert.assertEquals("testCallOperationArgsMissing Browse Result: size doesn't match", 2, bpres.length);
        Assert.assertTrue("testCallOperationArgsMissing Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testCallOperationArgsMissing Object Targets Null", targets);
        Assert.assertTrue("testCallOperationArgsMissing Object Targets empty", targets.length > 0);

        NodeId objectNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        Assert.assertNotNull("testCallOperationArgsMissing objectNode Null", objectNode);

        targets = bpres[1].getTargets();
        Assert.assertNotNull("testCallOperationArgsMissing Method Targets Null", targets);
        Assert.assertTrue("testCallOperationArgsMissing Method Targets empty", targets.length > 0);

        NodeId methodNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        Assert.assertNotNull("testCallOperationArgsMissing methodNode Null", methodNode);

        Variant[] inputArguments = new Variant[0];
        StatusException exception = Assert.assertThrows(StatusException.class, () -> {
            client.call(objectNode, methodNode, inputArguments);
        });
        Assert.assertEquals(StatusCodes.Bad_ArgumentsMissing, exception.getStatusCode().getValue());

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for adding a new property to an existing
     * SubmodelElementCollection.
     *
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws Exception If the operation fails
     */
    @Test
    @SuppressWarnings("java:S2925")
    public void testAddProperty() throws SecureIdentityException, IOException, ServiceException, Exception {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("testAddProperty: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        String propName = "NewProperty789";

        // make sure the element doesn't exist yet
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_3_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SM_ELEM_COLL_O_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, propName)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testAddProperty Browse Result Null", bpres);
        Assert.assertEquals("testAddProperty Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testAddProperty Browse Result Bad", bpres[0].getStatusCode().isBad());

        // Send event to MessageBus
        ElementCreateEventMessage msg = new ElementCreateEventMessage();
        msg.setElement(new DefaultReference.Builder()
                .key(new DefaultKey.Builder().idType(KeyType.IRI).type(KeyElements.SUBMODEL).value("https://acplt.org/Test_Submodel3").build())
                .key(new DefaultKey.Builder().idType(KeyType.ID_SHORT).type(KeyElements.SUBMODEL_ELEMENT_COLLECTION).value(TestConstants.FULL_SM_ELEM_COLL_O_NAME).build())
                .build());
        msg.setValue(new DefaultProperty.Builder()
                .kind(ModelingKind.INSTANCE)
                .idShort(propName)
                .category("Variable")
                .value("3465")
                .valueType("int")
                .build());
        service.getMessageBus().publish(msg);

        Thread.sleep(DEFAULT_TIMEOUT);

        // check that the element is there now
        bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testAddProperty Browse Result Null", bpres);
        Assert.assertEquals("testAddProperty Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testAddProperty Browse Result Good", bpres[0].getStatusCode().isGood());

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for deleting a complete submodel.
     *
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws Exception If the operation fails
     */
    @Test
    @SuppressWarnings("java:S2925")
    public void testDeleteSubmodel() throws SecureIdentityException, IOException, ServiceException, Exception {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("testDeleteSubmodel: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        // make sure the element exists
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_5_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        // add more elements to the browse path
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.IDENTIFICATION_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, "Id")));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testDeleteSubmodel Browse Result Null", bpres);
        Assert.assertEquals("testDeleteSubmodel Browse Result: size doesn't match", 2, bpres.length);
        Assert.assertTrue("testDeleteSubmodel Browse Result 1 Good", bpres[0].getStatusCode().isGood());
        Assert.assertTrue("testDeleteSubmodel Browse Result 2 Good", bpres[1].getStatusCode().isGood());

        // Send event to MessageBus
        ElementDeleteEventMessage msg = new ElementDeleteEventMessage();
        msg.setElement(new DefaultReference.Builder()
                .key(new DefaultKey.Builder().idType(KeyType.IRI).type(KeyElements.SUBMODEL).value("https://acplt.org/Test_Submodel2_Mandatory").build())
                .build());
        service.getMessageBus().publish(msg);

        Thread.sleep(DEFAULT_TIMEOUT);

        // check that the element is not there anymore
        bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testDeleteSubmodel Browse Result Null", bpres);
        Assert.assertEquals("testDeleteSubmodel Browse Result: size doesn't match", 2, bpres.length);
        Assert.assertTrue("testDeleteSubmodel Browse Result 1 Bad", bpres[0].getStatusCode().isBad());
        Assert.assertTrue("testDeleteSubmodel Browse Result 2 Bad", bpres[1].getStatusCode().isBad());

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for deleting a Capability.
     *
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws Exception If the operation fails
     */
    @Test
    @SuppressWarnings("java:S2925")
    public void testDeleteCapability() throws SecureIdentityException, IOException, ServiceException, Exception {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("testDeleteCapability: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        // make sure the element exists
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_7_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_CAPABILITY_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        // add more elements to the browse path
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.CATEGORY_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testDeleteCapability Browse Result Null", bpres);
        Assert.assertEquals("testDeleteCapability Browse Result: size doesn't match", 2, bpres.length);
        Assert.assertTrue("testDeleteCapability Browse Result 1 Good", bpres[0].getStatusCode().isGood());
        Assert.assertTrue("testDeleteCapability Browse Result 2 Good", bpres[1].getStatusCode().isGood());

        // Send event to MessageBus
        ElementDeleteEventMessage msg = new ElementDeleteEventMessage();
        msg.setElement(new DefaultReference.Builder()
                .key(new DefaultKey.Builder().idType(KeyType.IRI).type(KeyElements.SUBMODEL).value("https://acplt.org/Test_Submodel_Template").build())
                .key(new DefaultKey.Builder().idType(KeyType.ID_SHORT).type(KeyElements.CAPABILITY).value(TestConstants.FULL_CAPABILITY_NAME).build())
                .build());
        service.getMessageBus().publish(msg);

        Thread.sleep(DEFAULT_TIMEOUT);

        // check that the element is not there anymore
        bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testDeleteCapability Browse Result Null", bpres);
        Assert.assertEquals("testDeleteCapability Browse Result: size doesn't match", 2, bpres.length);
        Assert.assertTrue("testDeleteCapability Browse Result 1 Bad", bpres[0].getStatusCode().isBad());
        Assert.assertTrue("testDeleteCapability Browse Result 2 Bad", bpres[1].getStatusCode().isBad());

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for an OrderedSubmodelElementCollection.
     *
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws AddressSpaceException If the operation fails
     */
    @Test
    public void testOrderedSubmodelElementCollection() throws SecureIdentityException, IOException, ServiceException, ServiceResultException, AddressSpaceException {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_6_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SM_ELEM_COLL_O_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testOrderedSubmodelElementCollection Browse Result Null", bpres);
        Assert.assertEquals("testOrderedSubmodelElementCollection Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testOrderedSubmodelElementCollection Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testOrderedSubmodelElementCollection ValueType Null", targets);
        Assert.assertTrue("testOrderedSubmodelElementCollection ValueType empty", targets.length > 0);

        NodeId smNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        Assert.assertNotNull("testOrderedSubmodelElementCollection Node Null", smNode);

        TestUtils.checkType(client, smNode, new NodeId(aasns, TestConstants.AAS_OREDER_SM_ELEM_COLL_TYPE_ID));

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for an UnorderedSubmodelElementCollection.
     *
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws AddressSpaceException If the operation fails
     */
    @Test
    public void testUnorderedSubmodelElementCollection() throws SecureIdentityException, IOException, ServiceException, ServiceResultException, AddressSpaceException {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_6_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SM_ELEM_COLL_UO_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testUnorderedSubmodelElementCollection Browse Result Null", bpres);
        Assert.assertEquals("testUnorderedSubmodelElementCollection Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testUnorderedSubmodelElementCollection Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testUnorderedSubmodelElementCollection ValueType Null", targets);
        Assert.assertTrue("testUnorderedSubmodelElementCollection ValueType empty", targets.length > 0);

        NodeId smNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        Assert.assertNotNull("testUnorderedSubmodelElementCollection Node Null", smNode);

        TestUtils.checkType(client, smNode, new NodeId(aasns, TestConstants.AAS_SUBMODEL_ELEM_COLL_TYPE_ID));

        System.out.println("disconnect client");
        client.disconnect();
    }


    @Test
    public void testDateTimeProperty()
            throws SecureIdentityException, IOException, ServiceException, ServiceResultException, AddressSpaceException, StatusException, InterruptedException {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_6_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testDateTimeProperty Browse Result Null", bpres);
        Assert.assertEquals("testDateTimeProperty Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testDateTimeProperty Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testDateTimeProperty Submodel Null", targets);
        Assert.assertTrue("testDateTimeProperty Submodel empty", targets.length > 0);

        NodeId smNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        Assert.assertNotNull("testDateTimeProperty SubmodelNode Null", smNode);

        browsePath.clear();
        relPath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_DATETIME_PROP_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(smNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testDateTimeProperty Browse (2) Result Null", bpres);
        Assert.assertEquals("testDateTimeProperty Browse (2) Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testDateTimeProperty Browse (2) Result Good", bpres[0].getStatusCode().isGood());
        targets = bpres[0].getTargets();
        Assert.assertNotNull("testDateTimeProperty Property Null", targets);
        Assert.assertTrue("testDateTimeProperty Property empty", targets.length > 0);

        NodeId propValueNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        Assert.assertNotNull("testDateTimeProperty Node Null", propValueNode);

        DateTime dt = new DateTime(2022, Calendar.JULY, 8, 10, 22, 4, 0, TimeZone.getTimeZone("UTC"));
        TestUtils.checkAasPropertyObject(client, smNode, aasns, TestConstants.FULL_DATETIME_PROP_NAME, AASModelingKindDataType.Instance, "Parameter",
                AASValueTypeDataType.DateTime, dt, new ArrayList<>());

        ZonedDateTime zdtnew = ZonedDateTime.now(ZoneId.of(DateTimeValue.DEFAULT_TIMEZONE));
        DateTime dtnew = new DateTime(GregorianCalendar.from(zdtnew));
        TestUtils.writeNewValueIntern(client, propValueNode, dt, dtnew);

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Tests the submodel 1 (Identification).
     *
     * @param client The OPC UA Client.
     * @param submodelNode The desired Submodel
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    private void testSubmodel1(UaClient client, NodeId submodelNode) throws ServiceException, AddressSpaceException, ServiceResultException, StatusException {
        TestUtils.checkDisplayName(client, submodelNode, "Submodel:" + TestConstants.FULL_SUBMODEL_1_NAME);
        TestUtils.checkType(client, submodelNode, new NodeId(aasns, TestConstants.AAS_SUBMODEL_TYPE_ID));

        TestUtils.checkIdentificationNode(client, submodelNode, aasns, AASIdentifierTypeDataType.IRI, TestConstants.FULL_SUBMODEL_1_ID);
        TestUtils.checkAdministrationNode(client, submodelNode, aasns, "0.9", "0");
        TestUtils.checkModelingKindNode(client, submodelNode, aasns, AASModelingKindDataType.Instance);
        TestUtils.checkCategoryNode(client, submodelNode, aasns, "");
        TestUtils.checkDataSpecificationNode(client, submodelNode, aasns);

        TestUtils.checkQualifierNode(client, submodelNode, aasns, new ArrayList<>());

        ArrayList<Qualifier> list = new ArrayList<>();
        list.add(new DefaultQualifier.Builder()
                .value("100")
                .valueType("int")
                .type("http://acplt.org/Qualifier/ExampleQualifier")
                .build());
        list.add(new DefaultQualifier.Builder()
                .value("50")
                .valueType("int")
                .type("http://acplt.org/Qualifier/ExampleQualifier2")
                .build());
        TestUtils.checkAasPropertyString(client, submodelNode, aasns, "ManufacturerName", AASModelingKindDataType.Instance, "", AASValueTypeDataType.String,
                "http://acplt.org/ValueId/ACPLT", list);
    }
}
