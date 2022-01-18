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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys;

import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.stack.builtintypes.ByteString;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.BrowsePathResult;
import com.prosysopc.ua.stack.core.BrowsePathTarget;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.ReferenceDescription;
import com.prosysopc.ua.stack.core.RelativePath;
import com.prosysopc.ua.stack.core.RelativePathElement;
import com.prosysopc.ua.stack.core.ServerState;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys.helper.TestDefines;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys.helper.TestService;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys.helper.TestUtils;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import opc.i4aas.AASIdentifierTypeDataType;
import opc.i4aas.AASModelingKindDataType;
import opc.i4aas.AASValueTypeDataType;
import opc.i4aas.VariableIds;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test class for the general OPC UA Endpoint test with the simple example
 *
 * @author Tino Bischoff
 */
public class OpcUaEndpointTest {

    private static final Logger logger = LoggerFactory.getLogger(OpcUaEndpointTest.class);

    private static final int OPC_TCP_PORT = 18123;
    private static final long DEFAULT_TIMEOUT = 1000;

    private static final String ENDPOINT_URL = "opc.tcp://localhost:" + OPC_TCP_PORT;

    private static OpcUaEndpoint endpoint;
    private static TestService service;
    private static int aasns;

    @BeforeClass
    public static void startTest() throws ConfigurationException, Exception {

        CoreConfig coreConfig = new CoreConfig();

        OpcUaEndpointConfig config = new OpcUaEndpointConfig();
        config.setTcpPort(OPC_TCP_PORT);

        endpoint = new OpcUaEndpoint();
        endpoint.init(coreConfig, config);
        service = new TestService(endpoint, false);
        endpoint.setService(service);
        service.start();
    }


    @AfterClass
    public static void stopTest() {
        logger.trace("stopTest");
        if (endpoint != null) {
            endpoint.stop();
        }

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
    public void testOpcUaEndpoint() throws InterruptedException, Exception {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

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
            if (ref.getBrowseName().getName().equals(TestDefines.AAS_ENVIRONMENT_NAME)) {
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
        NodeId assetNode = null;
        NodeId submodelDocNode = null;
        NodeId submodelTechDataNode = null;
        NodeId submodelOperDataNode = null;
        for (ReferenceDescription ref: refs) {
            switch (ref.getBrowseName().getName()) {
                case TestDefines.SIMPLE_AAS_NAME:
                    aasNode = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
                    break;
                case TestDefines.SIMPLE_ASSET_NAME:
                    assetNode = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
                    break;
                case TestDefines.SUBMODEL_DOC_NODE_NAME:
                    submodelDocNode = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
                    break;
                case TestDefines.SUBMODEL_OPER_DATA_NODE_NAME:
                    submodelOperDataNode = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
                    break;
                case TestDefines.SUBMODEL_TECH_DATA_NODE_NAME:
                    submodelTechDataNode = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
                    break;
                default:
                    break;
            }
        }

        Assert.assertNotNull("AAS Node not found", aasNode);
        Assert.assertNotNull("Asset Node not found", assetNode);
        Assert.assertNotNull("Submodel Documentation Node not found", submodelDocNode);
        Assert.assertNotNull("Submodel TechnicalData Node not found", submodelTechDataNode);
        Assert.assertNotNull("Submodel OperationalData Node not found", submodelOperDataNode);

        // check Browse and Display Names
        TestUtils.checkBrowseName(client, aasNode, TestDefines.SIMPLE_AAS_NAME);
        TestUtils.checkDisplayName(client, aasNode, "AAS:" + TestDefines.SIMPLE_AAS_NAME);
        TestUtils.checkDisplayName(client, submodelDocNode, "Submodel:" + TestDefines.SUBMODEL_DOC_NODE_NAME);

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        // Asset
        testAsset(client, assetNode);

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


    /**
     * Test method for changing a property based on an event from the MessageBus. Sets an event
     * on the MessageBus and checks the new value in the server.
     *
     * @throws SecureIdentityException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws IOException If the operation fails
     * @throws StatusException If the operation fails
     */
    @Test
    public void testUpdatePropertyValue() throws SecureIdentityException, ServiceException, IOException, StatusException, Exception {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.ROTATION_SPEED_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestDefines.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteProperty Browse Result Null", bpres);
        Assert.assertTrue("testWriteProperty Browse Result: size doesn't match", bpres.length == 1);
        Assert.assertTrue("testWriteProperty Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteProperty ValueType Null", targets);
        Assert.assertTrue("testWriteProperty ValueType empty", targets.length > 0);
        DataValue value = client.readValue(targets[0].getTargetId());
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        String oldValue = "4370";
        Assert.assertEquals("intial value not equal", oldValue, value.getValue().getValue().toString());

        CountDownLatch condition = new CountDownLatch(1);
        final AtomicReference<EventMessage> response = new AtomicReference<>();
        service.getMessageBus().subscribe(SubscriptionInfo.create(
                ValueChangeEventMessage.class,
                x -> {
                    response.set(x);
                    condition.countDown();
                }));

        String newValue = "9999";
        List<Key> keys = new ArrayList<>();
        keys.add(new DefaultKey.Builder().idType(KeyType.IRI).type(KeyElements.SUBMODEL).value(TestDefines.SUBMODEL_OPER_DATA_NAME).build());
        keys.add(new DefaultKey.Builder().idType(KeyType.ID_SHORT).type(KeyElements.PROPERTY).value(TestDefines.ROTATION_SPEED_NAME).build());
        Reference propRef = new DefaultReference.Builder().keys(keys).build();
        ValueChangeEventMessage valueChangeMessage = new ValueChangeEventMessage();
        valueChangeMessage.setElement(propRef);
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.setValue(oldValue);
        valueChangeMessage.setOldValue(propertyValue);
        propertyValue.setValue(newValue);
        valueChangeMessage.setNewValue(propertyValue);
        service.getMessageBus().publish(valueChangeMessage);
        Thread.sleep(100);

        // check MessageBus
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(valueChangeMessage, response.get());

        // read new value
        value = client.readValue(targets[0].getTargetId());
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Assert.assertEquals("new value not equal", newValue, value.getValue().getValue().toString());

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for writing a property. Writes the property in the OPC UA
     * Server and checks the new value in the server.
     * 
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws StatusException If the operation fails
     * @throws InterruptedException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    @Test
    public void testWritePropertyValue() throws SecureIdentityException, IOException, ServiceException, StatusException, InterruptedException, ServiceResultException {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.TEST_PROPERTY_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestDefines.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWritePropertyValue Browse Result Null", bpres);
        Assert.assertTrue("testWritePropertyValue Browse Result: size doesn't match", bpres.length == 1);
        Assert.assertTrue("testWritePropertyValue Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWritePropertyValue ValueType Null", targets);
        Assert.assertTrue("testWritePropertyValue ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        TestUtils.writeNewValueIntern(client, writeNode, "50", "222");

        //        DataValue value = client.readValue(targets[0].getTargetId());
        //        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        //        String oldValue = "50";
        //        Assert.assertEquals("intial value not equal", oldValue, value.getValue().getValue().toString());
        //
        //        String newValue = "222";
        //        client.writeValue(writeNode, newValue);
        //
        //        // wait until the write is finished completely
        //        Thread.sleep(WRITE_TIMEOUT);
        //
        //        // read new value
        //        value = client.readValue(writeNode);
        //        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        //        Assert.assertEquals("new value not equal", newValue, value.getValue().getValue().toString());

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method to check whether the new value message from the MessageBus is
     * processed correctly
     *
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws Exception If the operation fails
     */
    @Test
    public void testPropertyChangeFromMessageBus() throws SecureIdentityException, IOException, ServiceException, Exception {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.SUBMODEL_TECH_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.MAX_ROTATION_SPEED_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestDefines.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testPropertyChangeFromMessageBus Browse Result Null", bpres);
        Assert.assertTrue("testPropertyChangeFromMessageBus Browse Result: size doesn't match", bpres.length == 1);
        Assert.assertTrue("testPropertyChangeFromMessageBus Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testPropertyChangeFromMessageBus ValueType Null", targets);
        Assert.assertTrue("testPropertyChangeFromMessageBus ValueType empty", targets.length > 0);

        List<Key> keys = new ArrayList<>();
        keys.add(new DefaultKey.Builder().idType(KeyType.IRI).type(KeyElements.SUBMODEL).value(TestDefines.SUBMODEL_TECH_DATA_NAME).build());
        keys.add(new DefaultKey.Builder().idType(KeyType.ID_SHORT).type(KeyElements.PROPERTY).value(TestDefines.MAX_ROTATION_SPEED_NAME).build());
        Reference propRef = new DefaultReference.Builder().keys(keys).build();

        ValueChangeEventMessage valueChangeMessage = new ValueChangeEventMessage();
        valueChangeMessage.setElement(propRef);
        PropertyValue propertyValue = new PropertyValue();
        propertyValue.setValue("5000");
        valueChangeMessage.setOldValue(propertyValue);
        String newValue = "5005";
        propertyValue.setValue(newValue);
        valueChangeMessage.setNewValue(propertyValue);
        service.getMessageBus().publish(valueChangeMessage);

        Thread.sleep(DEFAULT_TIMEOUT);

        // read new value
        DataValue value = client.readValue(targets[0].getTargetId());
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Assert.assertEquals("new value not equal", newValue, value.getValue().getValue().toString());

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for writing a range. Writes the property in the OPC UA
     * Server and checks the new value in the server.
     * 
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws StatusException If the operation fails
     * @throws InterruptedException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    @Test
    public void testWriteRangeValue() throws SecureIdentityException, IOException, ServiceException, StatusException, InterruptedException, ServiceResultException {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.TEST_RANGE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestDefines.RANGE_MAX_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteRangeValue Browse Result Null", bpres);
        Assert.assertTrue("testWriteRangeValue Browse Result: size doesn't match", bpres.length == 1);
        Assert.assertTrue("testWriteRangeValue Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteRangeValue ValueType Null", targets);
        Assert.assertTrue("testWriteRangeValue ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        TestUtils.writeNewValueIntern(client, writeNode, "100", "111.0");

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for writing a range. Writes the property in the OPC UA
     * Server and checks the new value in the server.
     * 
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws StatusException If the operation fails
     * @throws InterruptedException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    @Test
    public void testWriteBlobValue() throws SecureIdentityException, IOException, ServiceException, StatusException, InterruptedException, ServiceResultException {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.TEST_BLOB_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestDefines.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteBlobValue Browse Result Null", bpres);
        Assert.assertTrue("testWriteBlobValue Browse Result: size doesn't match", bpres.length == 1);
        Assert.assertTrue("testWriteBlobValue Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteBlobValue ValueType Null", targets);
        Assert.assertTrue("testWriteBlobValue ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        //byte[] oldValue = Base64.getDecoder().decode("AQIDBAU=");
        ByteString oldValue = ByteString.valueOf(Base64.getDecoder().decode("AQIDBAU="));
        ByteString newValue = ByteString.valueOf(Base64.getDecoder().decode("QUJDREU="));
        TestUtils.writeNewValueIntern(client, writeNode, oldValue.toString(), newValue);

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Test method for writing a property. Writes the property in the OPC UA
     * Server and checks the new value in the server.
     * 
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws StatusException If the operation fails
     * @throws InterruptedException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    @Test
    public void testWriteMultiLanguagePropertyValue() throws SecureIdentityException, IOException, ServiceException, StatusException, InterruptedException, ServiceResultException {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.TEST_MULTI_LAN_PROP_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestDefines.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWritePropertyValue Browse Result Null", bpres);
        Assert.assertTrue("testWritePropertyValue Browse Result: size doesn't match", bpres.length == 1);
        Assert.assertTrue("testWritePropertyValue Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWritePropertyValue ValueType Null", targets);
        Assert.assertTrue("testWritePropertyValue ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        List<LocalizedText> oldValue = new ArrayList<>();
        oldValue.add(new LocalizedText("Example value of a MultiLanguageProperty element", "en-us"));
        oldValue.add(new LocalizedText("Beispielswert für ein MulitLanguageProperty-Element", "de"));

        // The DataElementValueMapper changes the order of the elements
        List<LocalizedText> newValue = new ArrayList<>();
        newValue.add(new LocalizedText("Beispielswert2 fuer ein anderes MulitLanguageProperty-Element", "de"));
        newValue.add(new LocalizedText("Example value of a MultiLanguageProperty element", "en-us"));

        TestUtils.writeNewValueLocalizedTextArray(client, writeNode, oldValue.toArray(LocalizedText[]::new), newValue.toArray(LocalizedText[]::new));

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Tests the given AAS Node.
     *
     * @param client The OPC UA> Client.
     * @param aasNode The desired AAS Node.
     * @param submodelDocNode The Submodel Documentation Node
     * @param submodelOperDataNode The Submodel OperationalData Node
     * @param submodelTechDataNode The Submodel TechnicalData Node
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws StatusException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    private void testAas(UaClient client, NodeId aasNode, NodeId submodelDocNode, NodeId submodelOperDataNode, NodeId submodelTechDataNode)
            throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, aasNode, "AAS:" + TestDefines.SIMPLE_AAS_NAME);
        TestUtils.checkType(client, aasNode, new NodeId(aasns, TestDefines.AAS_AAS_TYPE_ID));
        TestUtils.checkIdentificationNode(client, aasNode, aasns, AASIdentifierTypeDataType.IRI, "http://customer.com/aas/9175_7013_7091_9168");
        TestUtils.checkAdministrationNode(client, aasNode, aasns, "1", "2");
        TestUtils.checkCategoryNode(client, aasNode, aasns, "");
        TestUtils.checkDataSpecificationNode(client, aasNode, aasns);
        TestUtils.checkAssetInformationNode(client, aasNode, aasns);
        testSubmodelRefs(client, aasNode, aasns, submodelDocNode, submodelOperDataNode, submodelTechDataNode);
    }


    /**
     * Tests the given Asset Node.
     *
     * @param client The OPC UA> Client.
     * @param assetNode The desired Asset Node.
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws StatusException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    private void testAsset(UaClient client, NodeId assetNode) throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, assetNode, "Asset:" + TestDefines.SIMPLE_ASSET_NAME);
        TestUtils.checkType(client, assetNode, new NodeId(aasns, TestDefines.AAS_ASSET_TYPE_ID));
        TestUtils.checkIdentificationNode(client, assetNode, aasns, AASIdentifierTypeDataType.IRI, "http://customer.com/assets/KHBVZJSQKIY");
        TestUtils.checkAdministrationNode(client, assetNode, aasns, null, null);
        TestUtils.checkCategoryNode(client, assetNode, aasns, "");
        TestUtils.checkDataSpecificationNode(client, assetNode, aasns);
    }


    /**
     * Tests the given Submodel Documentation.
     *
     * @param client The OPC UA Client.
     * @param submodelNode The desired Submodel
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws StatusException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    private void testSubmodelDoc(UaClient client, NodeId submodelNode) throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, submodelNode, "Submodel:" + TestDefines.SUBMODEL_DOC_NODE_NAME);
        TestUtils.checkType(client, submodelNode, new NodeId(aasns, TestDefines.AAS_SUBMODEL_TYPE_ID));

        String submodelName = "SubmodelOperationalData";

        NodeId operatingManualNode = null;

        List<ReferenceDescription> refs = client.getAddressSpace().browse(submodelNode);
        Assert.assertNotNull("Browse " + submodelName + " Refs Null", refs);
        Assert.assertFalse("Browse " + submodelName + " Refs empty", refs.isEmpty());
        for (ReferenceDescription ref: refs) {
            NodeId nid = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
            switch (ref.getBrowseName().getName()) {
                case TestDefines.OPERATING_MANUAL_NAME:
                    operatingManualNode = nid;
                    break;
                default:
                    break;
            }
        }

        Assert.assertNotNull(submodelName + " OperatingManual Node not found", operatingManualNode);

        TestUtils.checkIdentificationNode(client, submodelNode, aasns, AASIdentifierTypeDataType.IRI, TestDefines.SUBMODEL_DOC_NAME);
        TestUtils.checkAdministrationNode(client, submodelNode, aasns, "11", "159");
        TestUtils.checkModelingKindNode(client, submodelNode, aasns, AASModelingKindDataType.Instance);
        TestUtils.checkCategoryNode(client, submodelNode, aasns, "");
        TestUtils.checkDataSpecificationNode(client, submodelNode, aasns);
        TestUtils.checkQualifierNode(client, submodelNode, aasns, new ArrayList<>());
        testOperatingManual(client, operatingManualNode);
    }


    /**
     * Test the given Submodel OperationalData
     *
     * @param client The OPC UA Client.
     * @param submodelNode The desired Submodel
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws StatusException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    private void testSubmodelOperationalData(UaClient client, NodeId submodelNode) throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, submodelNode, "Submodel:" + TestDefines.SUBMODEL_OPER_DATA_NODE_NAME);
        TestUtils.checkType(client, submodelNode, new NodeId(aasns, TestDefines.AAS_SUBMODEL_TYPE_ID));

        TestUtils.checkIdentificationNode(client, submodelNode, aasns, AASIdentifierTypeDataType.IRI, TestDefines.SUBMODEL_OPER_DATA_NAME);
        TestUtils.checkAdministrationNode(client, submodelNode, aasns, null, null);
        TestUtils.checkCategoryNode(client, submodelNode, aasns, "");
        TestUtils.checkModelingKindNode(client, submodelNode, aasns, AASModelingKindDataType.Instance);
        TestUtils.checkDataSpecificationNode(client, submodelNode, aasns);
        TestUtils.checkQualifierNode(client, submodelNode, aasns, new ArrayList<>());
        TestUtils.checkAasPropertyString(client, submodelNode, aasns, TestDefines.ROTATION_SPEED_NAME, AASModelingKindDataType.Instance, "Variable", AASValueTypeDataType.Int32,
                "4370", new ArrayList<>());
    }


    /**
     * Tests the desired Submodel TechnicalData.
     *
     * @param client the OPC UA Client.
     * @param submodelNode The desired Submodel
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws StatusException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    private void testSubmodelTechnicalData(UaClient client, NodeId submodelNode) throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, submodelNode, "Submodel:" + TestDefines.SUBMODEL_TECH_DATA_NODE_NAME);
        TestUtils.checkType(client, submodelNode, new NodeId(aasns, TestDefines.AAS_SUBMODEL_TYPE_ID));

        TestUtils.checkIdentificationNode(client, submodelNode, aasns, AASIdentifierTypeDataType.IRI, TestDefines.SUBMODEL_TECH_DATA_NAME);
        TestUtils.checkAdministrationNode(client, submodelNode, aasns, null, null);
        TestUtils.checkCategoryNode(client, submodelNode, aasns, "");
        TestUtils.checkModelingKindNode(client, submodelNode, aasns, AASModelingKindDataType.Instance);
        TestUtils.checkDataSpecificationNode(client, submodelNode, aasns);
        TestUtils.checkQualifierNode(client, submodelNode, aasns, new ArrayList<>());
        TestUtils.checkAasPropertyString(client, submodelNode, aasns, TestDefines.MAX_ROTATION_SPEED_NAME, AASModelingKindDataType.Instance, "Parameter",
                AASValueTypeDataType.Int32, "5000", new ArrayList<>());
    }


    /**
     * Tests the SubodelElement OperatingManual from the Submodel Documentation
     *
     * @param client the OPC UA Client.
     * @param node The desired OperatingManual
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws StatusException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    private void testOperatingManual(UaClient client, NodeId node) throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        TestUtils.checkDisplayName(client, node, TestDefines.OPERATING_MANUAL_NAME);
        TestUtils.checkType(client, node, new NodeId(aasns, TestDefines.AAS_SUBMODEL_ELEM_COLL_TYPE_ID));
        TestUtils.checkCategoryNode(client, node, aasns, "");
        TestUtils.checkModelingKindNode(client, node, aasns, AASModelingKindDataType.Instance);
        TestUtils.checkDataSpecificationNode(client, node, aasns);
        TestUtils.checkQualifierNode(client, node, aasns, new ArrayList<>());
        TestUtils.checkVariableBool(client, node, aasns, TestDefines.ALLOW_DUPLICATES_NAME, false);
        TestUtils.checkAasPropertyString(client, node, aasns, "Title", AASModelingKindDataType.Instance, "", AASValueTypeDataType.LocalizedText, "OperatingManual",
                new ArrayList<>());
        TestUtils.checkAasPropertyFile(client, node, aasns, "DigitalFile_PDF", AASModelingKindDataType.Instance, "", "application/pdf", "./data/OPC-UA-Getting-Started.pdf",
                1419637);
    }


    /**
     * Searches for the Submodel reference node and checks the corresponding
     * values
     *
     * @param client The OPC UA Client.
     * @param baseNode The base node where the Node is searched.
     * @param aasns The namespace index of the AAS namespace.
     * @param submodelDocNode The Submodel Documentation Node
     * @param submodelOperDataNode The Submodel OperationalData Node
     * @param submodelTechDataNode The Submodel TechnicalData Node
     * @throws ServiceException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws StatusException If the operation fails
     */
    private void testSubmodelRefs(UaClient client, NodeId baseNode, int aasns, NodeId submodelDocNode, NodeId submodelOperDataNode, NodeId submodelTechDataNode)
            throws ServiceException, ServiceResultException, AddressSpaceException, StatusException {
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.SUBMODEL_REF_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(baseNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testSubmodelRefs Browse Result Null", bpres);
        Assert.assertTrue("testSubmodelRefs Browse Result: size doesn't match", bpres.length == 1);
        Assert.assertTrue("testSubmodelRefs Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testSubmodelRefs Target Null", targets);
        Assert.assertTrue("testSubmodelRefs Target empty", targets.length > 0);
        NodeId refNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        Assert.assertNotNull("testSubmodelRefs RefNode Null", refNode);
        TestUtils.checkType(client, refNode, new NodeId(aasns, TestDefines.AAS_REFERENCE_LIST_ID));

        TestUtils.checkSubmodelRef(client, refNode, aasns, TestDefines.SUBMODEL_DOC_NAME, submodelDocNode);
        TestUtils.checkSubmodelRef(client, refNode, aasns, TestDefines.SUBMODEL_OPER_DATA_NAME, submodelOperDataNode);
        TestUtils.checkSubmodelRef(client, refNode, aasns, TestDefines.SUBMODEL_TECH_DATA_NAME, submodelTechDataNode);
    }
}
