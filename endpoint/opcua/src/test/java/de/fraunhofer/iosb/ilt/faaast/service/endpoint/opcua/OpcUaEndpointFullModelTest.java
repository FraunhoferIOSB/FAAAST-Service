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
import com.prosysopc.ua.stack.builtintypes.ByteString;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.BrowseDirection;
import com.prosysopc.ua.stack.core.BrowsePathResult;
import com.prosysopc.ua.stack.core.BrowsePathTarget;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.ReferenceDescription;
import com.prosysopc.ua.stack.core.RelativePath;
import com.prosysopc.ua.stack.core.RelativePathElement;
import com.prosysopc.ua.stack.core.ServerState;
import com.prosysopc.ua.stack.core.StatusCodes;
import com.prosysopc.ua.stack.core.UserTokenType;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.CommonAttributesData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestConstants;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestService;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestUtils;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.assetconnection.TestAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.assetconnection.TestOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import opc.ua.aas.ReferenceTypeIds;
import opc.ua.aas.VariableIds;
import opc.ua.aas.datatypes.AASKey;
import opc.ua.aas.datatypes.AASKeyTypes;
import opc.ua.aas.datatypes.AASModellingKind;
import opc.ua.aas.datatypes.AASReference;
import opc.ua.aas.datatypes.AASReferenceTypes;
import opc.ua.aas.datatypes.AASSubmodelElements;
import opc.ua.aas.objecttypes.AASEntityType;
import opc.ua.aas.objecttypes.AASRelationshipElementType;
import org.awaitility.Awaitility;
import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultQualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultRelationshipElement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test class for the general OPC UA Endpoint test with the full example
 */
public class OpcUaEndpointFullModelTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaEndpointFullModelTest.class);

    private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);
    private static final Duration MAX_TIMEOUT = Duration.ofSeconds(5);

    private static TestService service;
    private static int aasns;
    private static int opcTcpPort;
    private static String endpointUrl;

    private UaClient client;

    @BeforeClass
    public static void startTest() throws Exception {
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

        TestAssetConnectionConfig assetConnectionConfig = new TestAssetConnectionConfig();

        // register Test Operation
        List<Key> keys = new ArrayList<>();
        keys.add(new DefaultKey.Builder().type(KeyTypes.SUBMODEL).value("https://acplt.org/Test_Submodel").build());
        keys.add(new DefaultKey.Builder().type(KeyTypes.OPERATION).value("ExampleOperation").build());
        Reference ref = new DefaultReference.Builder()
                .type(ReferenceTypes.MODEL_REFERENCE)
                .keys(keys)
                .build();
        List<OperationVariable> outputArgs = new ArrayList<>();
        outputArgs
                .add(new DefaultOperationVariable.Builder().value(new DefaultProperty.Builder().idShort("ExampleProperty2").valueType(DataTypeDefXsd.STRING).value("XYZ1").build())
                        .build());

        // register another Operation 
        keys = new ArrayList<>();
        keys.add(new DefaultKey.Builder().type(KeyTypes.SUBMODEL).value(TestConstants.FULL_SUBMODEL_4_ID).build());
        keys.add(new DefaultKey.Builder().type(KeyTypes.OPERATION).value("ExampleOperation").build());
        Reference ref2 = new DefaultReference.Builder()
                .type(ReferenceTypes.MODEL_REFERENCE)
                .keys(keys)
                .build();

        assetConnectionConfig.getOperationProviders().put(ref, new TestOperationProviderConfig(outputArgs));
        assetConnectionConfig.getOperationProviders().put(ref2, new TestOperationProviderConfig(null));

        service = new TestService(config, assetConnectionConfig, true);
        service.start();
    }


    @AfterClass
    public static void stopTest() {
        LOGGER.trace("stopTest");

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
    public void testOpcUaEndpointFull()
            throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException, AddressSpaceException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        Assert.assertTrue("client not connected", client.hasConnected());
        //System.out.println("client connected");

        DataValue value = client.readValue(Identifiers.Server_ServerStatus_State);
        //System.out.println(value);
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
                case TestConstants.FULL_SUBMODEL_1_NAME -> {
                    submodel1Node = rid;
                }
                default -> {
                }
            }
            //intentionally left empty
        }

        Assert.assertNotNull("Submodel 1 Node not found", submodel1Node);

        testSubmodel1(submodel1Node);

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testWriteRelationshipElementValue() throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_4_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_REL_ELEMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, AASRelationshipElementType.SECOND)));
        //browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.KEYS_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteRelationshipElementValue Browse Result Null", bpres);
        Assert.assertEquals("testWriteRelationshipElementValue Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWriteRelationshipElementValue Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteRelationshipElementValue ValueType Null", targets);
        Assert.assertTrue("testWriteRelationshipElementValue ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        List<AASKey> oldKeys = new ArrayList<>();
        oldKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.Submodel), TestConstants.FULL_SUBMODEL_4_ID));
        oldKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.SubmodelElementList), "ExampleSubmodelElementListUnordered"));
        oldKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.MultiLanguageProperty), "ExampleMultiLanguageProperty"));
        AASReference oldValue = new AASReference(AASReferenceTypes.of(AASReferenceTypes.Options.ModelReference), null, oldKeys.toArray(AASKey[]::new));

        // The DataElementValueMapper changes the order of the elements
        List<AASKey> newKeys = new ArrayList<>();
        newKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.Submodel), TestConstants.FULL_SUBMODEL_4_ID));
        newKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.SubmodelElementList), "ExampleSubmodelElementCollection"));
        newKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.Blob), "ExampleBlob"));
        AASReference newValue = new AASReference(AASReferenceTypes.of(AASReferenceTypes.Options.ModelReference), null, newKeys.toArray(AASKey[]::new));

        //TestUtils.writeNewValueArray(client, writeNode, oldValue.toArray(AASKey[]::new), newValue.toArray(AASKey[]::new));
        TestUtils.writeNewValueReference(client, writeNode, oldValue, newValue);

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testWriteSubmodelElementCollectionValue()
            throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_6_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SM_ELEM_COLL_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.TEST_REF_ELEM_NAME)));
        //browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        //browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.KEYS_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteSubmodelElementCollectionValue Browse Result Null", bpres);
        Assert.assertEquals("testWriteSubmodelElementCollectionValue Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWriteSubmodelElementCollectionValue Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteSubmodelElementCollectionValue ValueType Null", targets);
        Assert.assertTrue("testWriteSubmodelElementCollectionValue ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        List<AASKey> oldKeys = new ArrayList<>();
        oldKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.Submodel), "https://acplt.org/Test_Submodel_Missing"));
        oldKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.SubmodelElementCollection), "ExampleSubmodelElementCollection"));
        oldKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.File), "ExampleFile"));
        AASReference oldValue = new AASReference(AASReferenceTypes.of(AASReferenceTypes.Options.ModelReference), null, oldKeys.toArray(AASKey[]::new));

        // The DataElementValueMapper changes the order of the elements
        List<AASKey> newKeys = new ArrayList<>();
        newKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.GlobalReference), "https://iosb.fraunhofer.de/TestValue1"));
        AASReference newValue = new AASReference(AASReferenceTypes.of(AASReferenceTypes.Options.ModelReference), null, newKeys.toArray(AASKey[]::new));

        TestUtils.writeNewValueReference(client, writeNode, oldValue, newValue);

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testWriteSubmodelElementListValue()
            throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_6_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_ELEMENT_LIST_ORDERED_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_PROPERTY_NAME)));
        //browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.RANGE_MIN_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteSubmodelElementListValue Browse Result Null", bpres);
        Assert.assertEquals("testWriteSubmodelElementListValue Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWriteSubmodelElementListValue Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteSubmodelElementListValue ValueType Null", targets);
        Assert.assertTrue("testWriteSubmodelElementListValue ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        TestUtils.writeNewValueIntern(client, writeNode, "exampleValue", "a new value");

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testWriteSubmodelElementListValue2()
            throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_7_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_ELEMENT_LIST_ORDERED_NAME)));
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

        // The DataElementValueMapper changes the order of the elements in some cases
        List<LocalizedText> newValue = new ArrayList<>();
        newValue.add(new LocalizedText("english test element", "en-us"));
        newValue.add(new LocalizedText("deutsches Test-Element", "de"));

        TestUtils.writeNewValueArray(client, writeNode, new ArrayList<>().toArray(LocalizedText[]::new), newValue.toArray(LocalizedText[]::new));

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testWriteEntityGlobalAssetId()
            throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("testWriteEntityGlobalAssetId: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_2_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_ENTITY2_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasComponent, false, true, new QualifiedName(aasns, AASEntityType.GLOBAL_ASSET_ID)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteEntityGlobalAssetId Browse Result Null", bpres);
        Assert.assertEquals("testWriteEntityGlobalAssetId Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWriteEntityGlobalAssetId Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteEntityGlobalAssetId ValueType Null", targets);
        Assert.assertTrue("testWriteEntityGlobalAssetId ValueType empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        String oldValue = "https://acplt.org/Test_Asset2";
        String newValue = "https://acplt2.org/Test_Asset3";

        TestUtils.writeNewValueIntern(client, writeNode, oldValue, newValue);

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testCallOperationSuccess() throws SecureIdentityException, IOException, ServiceException, ServiceResultException, MethodCallStatusException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("client connected");

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

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testCallOperationArgsMissing() throws SecureIdentityException, IOException, ServiceException, ServiceResultException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("client connected");

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

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testAddProperty() throws SecureIdentityException, IOException, ServiceException, MessageBusException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("testAddProperty: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        String propName = "NewProperty789";

        // make sure the element doesn't exist yet
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_3_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SM_ELEM_COLL_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, propName)));
        //browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testAddProperty Browse Result Null", bpres);
        Assert.assertEquals("testAddProperty Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testAddProperty Browse Result Bad", bpres[0].getStatusCode().isBad());

        // Send event to MessageBus
        ElementCreateEventMessage msg = new ElementCreateEventMessage();
        msg.setElement(new DefaultReference.Builder()
                .type(ReferenceTypes.MODEL_REFERENCE)
                .keys(new DefaultKey.Builder().type(KeyTypes.SUBMODEL).value("https://acplt.org/Test_Submodel").build())
                .keys(new DefaultKey.Builder().type(KeyTypes.SUBMODEL_ELEMENT_COLLECTION).value(TestConstants.FULL_SM_ELEM_COLL_NAME).build())
                .keys(new DefaultKey.Builder().type(KeyTypes.PROPERTY).value(propName).build())
                .build());
        msg.setValue(new DefaultProperty.Builder()
                .idShort(propName)
                .category("PARAMETER")
                .value("3465")
                .valueType(DataTypeDefXsd.INT)
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

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testDeleteSubmodel() throws SecureIdentityException, IOException, ServiceException, MessageBusException, ServiceResultException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("testDeleteSubmodel: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        // make sure the element exists
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_5_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        // add more elements to the browse path
        browsePath.add(new RelativePathElement(client.getAddressSpace().getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasCommonAttribute), false, true,
                new QualifiedName(aasns, TestConstants.COMMON_ATTRIBUTES)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testDeleteSubmodel Browse Result Null", bpres);
        Assert.assertEquals("testDeleteSubmodel Browse Result: size doesn't match", 2, bpres.length);
        Assert.assertTrue("testDeleteSubmodel Browse Result 1 Good", bpres[0].getStatusCode().isGood());
        Assert.assertTrue("testDeleteSubmodel Browse Result 2 Good", bpres[1].getStatusCode().isGood());

        // Send event to MessageBus
        ElementDeleteEventMessage msg = new ElementDeleteEventMessage();
        msg.setElement(new DefaultReference.Builder()
                .type(ReferenceTypes.MODEL_REFERENCE)
                .keys(new DefaultKey.Builder().type(KeyTypes.SUBMODEL).value("https://acplt.org/Test_Submodel2_Mandatory").build())
                .build());
        service.getMessageBus().publish(msg);

        // check that the element is not there anymore
        // unable to deterministically know when the changes will materialize, therefore wait for some time
        Awaitility.await()
                .alias("check value updated in OPC UA endpoint")
                .pollInterval(POLL_TIMEOUT)
                .atMost(MAX_TIMEOUT)
                .until(() -> {
                    BrowsePathResult[] bpr = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
                    return bpr != null && bpr.length == 2 && bpr[0].getStatusCode().isBad() && bpr[1].getStatusCode().isBad();
                });

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testDeleteCapability() throws SecureIdentityException, IOException, ServiceException, MessageBusException, ServiceResultException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("testDeleteCapability: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        // make sure the element exists
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_7_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_CAPABILITY_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        // add more elements to the browse path
        browsePath.add(new RelativePathElement(client.getAddressSpace().getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasCommonAttribute), false, true,
                new QualifiedName(aasns, TestConstants.COMMON_ATTRIBUTES)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testDeleteCapability Browse Result Null", bpres);
        Assert.assertEquals("testDeleteCapability Browse Result: size doesn't match", 2, bpres.length);
        Assert.assertTrue("testDeleteCapability Browse Result 1 Good", bpres[0].getStatusCode().isGood());
        Assert.assertTrue("testDeleteCapability Browse Result 2 Good", bpres[1].getStatusCode().isGood());

        // Send event to MessageBus
        ElementDeleteEventMessage msg = new ElementDeleteEventMessage();
        msg.setElement(new DefaultReference.Builder()
                .type(ReferenceTypes.MODEL_REFERENCE)
                .keys(new DefaultKey.Builder().type(KeyTypes.SUBMODEL).value("https://acplt.org/Test_Submodel_Template").build())
                .keys(new DefaultKey.Builder().type(KeyTypes.CAPABILITY).value(TestConstants.FULL_CAPABILITY_NAME).build())
                .build());
        service.getMessageBus().publish(msg);

        // check that the element is not there anymore
        // unable to deterministically know when the changes will materialize, therefore wait for some time
        Awaitility.await()
                .alias("check value updated in OPC UA endpoint")
                .pollInterval(POLL_TIMEOUT)
                .atMost(MAX_TIMEOUT)
                .until(() -> {
                    BrowsePathResult[] bpr = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
                    return bpr != null && bpr.length == 2 && bpr[0].getStatusCode().isBad() && bpr[1].getStatusCode().isBad();
                });

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testDateTimeProperty()
            throws SecureIdentityException, IOException, ServiceException, ServiceResultException, AddressSpaceException, StatusException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("client connected");

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
        //browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
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

        DateTime dt = DateTime.fromInstant(ZonedDateTime.of(2022, 7, 8, 10, 22, 4, 0, ZoneId.systemDefault()).toInstant());
        TestUtils.checkAasPropertyObject(client, smNode, aasns, TestConstants.FULL_DATETIME_PROP_NAME, "Parameter",
                Datatype.DATE_TIME, dt, new ArrayList<>());

        OffsetDateTime odtnew = OffsetDateTime.now(ZoneId.systemDefault());
        DateTime dtnew = DateTime.fromInstant(odtnew.toInstant());
        TestUtils.writeNewValueIntern(client, propValueNode, dt, dtnew);

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testCallOperationNoArgs() throws SecureIdentityException, IOException, ServiceException, ServiceResultException, MethodCallStatusException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());
        int serverns = client.getAddressSpace().getNamespaceTable().getIndex(AasServiceNodeManager.NAMESPACE_URI);

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_4_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_OPERATION_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        browsePath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_4_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_OPERATION_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(serverns, TestConstants.FULL_OPERATION_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testCallOperationNoArgs Browse Result Null", bpres);
        Assert.assertEquals("testCallOperationNoArgs Browse Result: size doesn't match", 2, bpres.length);
        Assert.assertTrue("testCallOperationNoArgs Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testCallOperationNoArgs Object Targets Null", targets);
        Assert.assertTrue("testCallOperationNoArgs Object Targets empty", targets.length > 0);

        NodeId objectNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        Assert.assertNotNull("testCallOperationNoArgs objectNode Null", objectNode);

        targets = bpres[1].getTargets();
        Assert.assertNotNull("testCallOperationNoArgs Method Targets Null", targets);
        Assert.assertTrue("testCallOperationNoArgs Method Targets empty", targets.length > 0);

        NodeId methodNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        Assert.assertNotNull("testCallOperationNoArgs methodNode Null", methodNode);

        Variant[] outputs = client.call(objectNode, methodNode);
        Assert.assertNotNull("testCallOperationNoArgs output Arguments Null", outputs);
        Assert.assertEquals("testCallOperationNoArgs output Arguments length not equal", 0, outputs.length);

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testSubmodelElementListOrdered() throws ServiceException, SecureIdentityException, IOException, StatusException, ServiceResultException, AddressSpaceException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("testSubmodelElementList: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_6_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_ELEMENT_LIST_ORDERED_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasComponent, false, true, new QualifiedName(aasns, TestConstants.ORDER_RELEVANT)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        browsePath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_6_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_ELEMENT_LIST_ORDERED_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasComponent, false, true, new QualifiedName(aasns, TestConstants.TYPE_VALUE_LIST_ELEMENT)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        browsePath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_6_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_ELEMENT_LIST_ORDERED_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasComponent, false, true, new QualifiedName(aasns, TestConstants.VALUE_TYPE_LIST_ELEMENT)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        browsePath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_6_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_ELEMENT_LIST_ORDERED_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testSubmodelElementListOrdered Browse Result Null", bpres);
        Assert.assertEquals("testSubmodelElementListOrdered Browse Result: size doesn't match", 4, bpres.length);
        Assert.assertTrue("testSubmodelElementListOrdered Browse Result 1 Good", bpres[0].getStatusCode().isGood());
        Assert.assertTrue("testSubmodelElementListOrdered Browse Result 2 Good", bpres[1].getStatusCode().isGood());
        // ValueTypeListElement not set
        Assert.assertTrue("testSubmodelElementListOrdered Browse Result 3 Bad", bpres[2].getStatusCode().isBad());
        Assert.assertTrue("testSubmodelElementListOrdered Browse Result 4 Good", bpres[3].getStatusCode().isGood());

        // OrderRelevant
        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testSubmodelElementListOrdered OrderRelevant Null", targets);
        Assert.assertTrue("testSubmodelElementListOrdered OrderRelevant empty", targets.length > 0);
        DataValue value = client.readValue(targets[0].getTargetId());
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Boolean orderRelevantExpected = true;
        Assert.assertEquals("OrderRelevant not equal", orderRelevantExpected, value.getValue().getValue());

        // TypeValueListElement
        targets = bpres[1].getTargets();
        Assert.assertNotNull("testSubmodelElementListOrdered TypeValueListElement Null", targets);
        Assert.assertTrue("testSubmodelElementListOrdered TypeValueListElement empty", targets.length > 0);
        value = client.readValue(targets[0].getTargetId());
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        AASSubmodelElements tvListElementExpected = ValueConverter.getAasSubmodelElementsType(AasSubmodelElements.SUBMODEL_ELEMENT);
        Assert.assertEquals("TypeValueListElement not equal", tvListElementExpected, value.getValue().asOptionSet(AASSubmodelElements.SPECIFICATION));

        // SubmodelElementList type
        targets = bpres[3].getTargets();
        NodeId listNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        TestUtils.checkType(client, listNode, TestConstants.AAS_SUBMODEL_ELEM_LIST_TYPE);

        TestUtils.checkDescriptions(client, listNode, List.of(LocalizedText.from(
                "Example SubmodelElementListOrdered object",
                "en-us")));

        // List elements
        List<ReferenceDescription> refs = client.getAddressSpace().browse(listNode, BrowseDirection.Forward,
                client.getAddressSpace().getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasOrderedComponent));
        Assert.assertNotNull(refs);
        Assert.assertEquals(3, refs.size());
        Assert.assertEquals(QualifiedName.from(aasns, "ExampleProperty"), refs.get(0).getBrowseName());
        Assert.assertEquals(QualifiedName.from(aasns, "ExampleMultiLanguageProperty"), refs.get(1).getBrowseName());
        Assert.assertEquals(QualifiedName.from(aasns, "ExampleCapability"), refs.get(2).getBrowseName());
        //Assert.assertEquals(QualifiedName.from(aasns, "ExampleRange"), refs.get(2).getBrowseName());

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testSubmodelElementListUnordered() throws ServiceException, SecureIdentityException, IOException, StatusException, ServiceResultException, AddressSpaceException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("testSubmodelElementList: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_4_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SM_ELEM_COLL_UO_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasComponent, false, true, new QualifiedName(aasns, TestConstants.ORDER_RELEVANT)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        browsePath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_4_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SM_ELEM_COLL_UO_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasComponent, false, true, new QualifiedName(aasns, TestConstants.TYPE_VALUE_LIST_ELEMENT)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        browsePath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_4_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SM_ELEM_COLL_UO_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasComponent, false, true, new QualifiedName(aasns, TestConstants.VALUE_TYPE_LIST_ELEMENT)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        browsePath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_4_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SM_ELEM_COLL_UO_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull(bpres);
        Assert.assertEquals(4, bpres.length);
        Assert.assertTrue(bpres[0].getStatusCode().isGood());
        Assert.assertTrue(bpres[1].getStatusCode().isGood());
        // ValueTypeListElement not set
        Assert.assertTrue(bpres[2].getStatusCode().isBad());
        Assert.assertTrue(bpres[3].getStatusCode().isGood());

        // OrderRelevant
        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull(targets);
        Assert.assertTrue(targets.length > 0);
        DataValue value = client.readValue(targets[0].getTargetId());
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Boolean orderRelevantExpected = false;
        Assert.assertEquals(orderRelevantExpected, value.getValue().getValue());

        // TypeValueListElement
        targets = bpres[1].getTargets();
        Assert.assertNotNull(targets);
        Assert.assertTrue(targets.length > 0);
        value = client.readValue(targets[0].getTargetId());
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        AASSubmodelElements tvListElementExpected = ValueConverter.getAasSubmodelElementsType(AasSubmodelElements.SUBMODEL_ELEMENT);
        Assert.assertEquals(tvListElementExpected, value.getValue().asOptionSet(AASSubmodelElements.SPECIFICATION));

        // SubmodelElementList type
        targets = bpres[3].getTargets();
        NodeId listNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        TestUtils.checkType(client, listNode, TestConstants.AAS_SUBMODEL_ELEM_LIST_TYPE);

        TestUtils.checkDescriptions(client, listNode, List.of());

        // List elements
        List<ReferenceDescription> refs = client.getAddressSpace().browse(listNode, BrowseDirection.Forward,
                client.getAddressSpace().getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasOrderedComponent));
        Assert.assertNotNull(refs);
        Assert.assertEquals(3, refs.size());
        Assert.assertEquals(QualifiedName.from(aasns, "ExampleProperty"), refs.get(0).getBrowseName());
        Assert.assertEquals(QualifiedName.from(aasns, "ExampleCollection"), refs.get(1).getBrowseName());
        Assert.assertEquals(QualifiedName.from(aasns, "ExampleMultiLanguageProperty"), refs.get(2).getBrowseName());
        //Assert.assertEquals(QualifiedName.from(aasns, "ExampleRange"), refs.get(2).getBrowseName());

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testWriteProperty()
            throws SecureIdentityException, ServiceException, IOException, StatusException, ServiceResultException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("testWriteProperty: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_6_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_INT64_PROP_NAME)));
        //browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteProperty Browse Result Null", bpres);
        Assert.assertEquals("testWriteProperty Browse Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testWriteProperty Browse Result Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteProperty ValueType Null", targets);
        Assert.assertTrue("testWriteProperty ValueType empty", targets.length > 0);
        Long oldValue = Long.MAX_VALUE;

        final Long newValue = Long.valueOf(159785);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        TestUtils.writeNewValueIntern(client, writeNode, oldValue, newValue);

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testUpdateSubmodelElement() throws SecureIdentityException, IOException, ServiceException, MessageBusException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        //System.out.println("testUpdateSubmodelElement: client connected");

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        // make sure one of the old elements exists, the new element exists not yet
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_6_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_REL_ELEMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, AASRelationshipElementType.SECOND)));
        //browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.KEYS_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testUpdateSubmodelElement Browse Result Null", bpres);
        Assert.assertEquals("testUpdateSubmodelElement Browse 1 Result: size doesn't match", 1, bpres.length);
        Assert.assertTrue("testUpdateSubmodelElement Browse 1 Result 1 not Good", bpres[0].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testUpdateSubmodelElement Target 1 Null", targets);
        Assert.assertTrue("testUpdateSubmodelElement Target 1 empty", targets.length > 0);

        // update submodel 
        // Send update event to MessageBus
        ElementUpdateEventMessage msg = new ElementUpdateEventMessage();
        msg.setElement(new DefaultReference.Builder()
                .type(ReferenceTypes.MODEL_REFERENCE)
                .keys(new DefaultKey.Builder().type(KeyTypes.SUBMODEL).value(TestConstants.FULL_SUBMODEL_6_ID).build())
                .keys(new DefaultKey.Builder().type(KeyTypes.RELATIONSHIP_ELEMENT).value(TestConstants.FULL_REL_ELEMENT_NAME).build())
                .build());
        msg.setValue(new DefaultRelationshipElement.Builder()
                .idShort("ExampleRelationshipElement")
                .category("PARAMETER")
                .description(new DefaultLangStringTextType.Builder().text("Example RelationshipElement object").language("en-us").build())
                .description(new DefaultLangStringTextType.Builder().text("Beispiel RelationshipElement Element").language("de").build())
                .semanticId(new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("http://acplt.org/RelationshipElements/ExampleRelationshipElement")
                                .build())
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .build())
                .first(new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.SUBMODEL)
                                .value("https://acplt.org/Test_Submodel_Missing")
                                .build())
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.SUBMODEL_ELEMENT_LIST)
                                .value("ExampleSubmodelElementListOrdered")
                                .build())
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.PROPERTY)
                                .value("ExampleProperty")
                                .build())
                        .type(ReferenceTypes.MODEL_REFERENCE)
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
                .build());
        service.getMessageBus().publish(msg);

        // check that the element was updated
        // unable to deterministically know when the changes will materialize, therefore wait for some time
        Awaitility.await()
                .alias("check value updated in OPC UA endpoint")
                .pollInterval(POLL_TIMEOUT)
                .atMost(MAX_TIMEOUT)
                .until(() -> {
                    BrowsePathResult[] bpr = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
                    if ((bpr == null) || (bpr.length != 1) || bpr[0].getStatusCode().isNotGood()) {
                        return false;
                    }
                    BrowsePathTarget[] targets2 = bpr[0].getTargets();
                    if ((targets2 == null) || (targets2.length == 0)) {
                        return false;
                    }

                    List<AASKey> smeKeys = new ArrayList<>();
                    smeKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.Submodel), "http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial"));
                    smeKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.Entity), "ExampleEntity"));
                    smeKeys.add(new AASKey(AASKeyTypes.of(AASKeyTypes.Options.Property), "ExampleProperty2"));
                    AASReference newValue = new AASReference(AASReferenceTypes.of(AASReferenceTypes.Options.ModelReference), null, smeKeys.toArray(AASKey[]::new));

                    DataValue value = client.readValue(client.getAddressSpace().getNamespaceTable().toNodeId(targets2[0].getTargetId()));
                    return value.getStatusCode().isGood() && (value.getValue() != null) && Objects.equals((AASReference) value.getValue().getValue(), newValue);
                    //return value.getStatusCode().isGood() && Arrays.equals(smeValue.toArray(AASKey[]::new), (AASKey[]) value.getValue().getValue());
                });

        //System.out.println("disconnect client");
        //client.disconnect();
    }


    @Test
    public void testWriteAnnotatedRelationshipAnnotations() throws SecureIdentityException, IOException, ServiceException, StatusException, ServiceResultException {
        client = new UaClient(endpointUrl);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_6_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_ANN_REL_ELEMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, "ExampleProperty")));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        browsePath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_SUBMODEL_6_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_ANN_REL_ELEMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, "AnnotationBlob")));
        browsePath.add(new RelativePathElement(Identifiers.HasComponent, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testWriteAnnotatedRelationshipAnnotations Browse Result Null", bpres);
        Assert.assertEquals("testWriteAnnotatedRelationshipAnnotations Browse Result: size doesn't match", 2, bpres.length);
        Assert.assertTrue("testWriteAnnotatedRelationshipAnnotations Browse Result 1 Good", bpres[0].getStatusCode().isGood());
        Assert.assertTrue("testWriteAnnotatedRelationshipAnnotations Browse Result 2 Good", bpres[1].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testWriteAnnotatedRelationshipAnnotations Property Null", targets);
        Assert.assertTrue("testWriteAnnotatedRelationshipAnnotations Property empty", targets.length > 0);

        // SubmodelElementVariable
        NodeId writeVariable = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        String oldValue = "some example annotation";
        String newValue = "any other strange annptation value!";
        TestUtils.writeNewValueIntern(client, writeVariable, oldValue, newValue);

        targets = bpres[1].getTargets();
        Assert.assertNotNull("testWriteAnnotatedRelationshipAnnotations Blob Null", targets);
        Assert.assertTrue("testWriteAnnotatedRelationshipAnnotations Blob empty", targets.length > 0);

        // SubmodelElementObject
        NodeId writeBlob = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        ByteString oldBlob = ByteString.valueOf(Base64.getDecoder().decode("AQIDBAU="));
        ByteString newBlob = ByteString.valueOf(Base64.getDecoder().decode("ERITFBU="));
        TestUtils.writeNewValueIntern(client, writeBlob, oldBlob, newBlob);

        //client.disconnect();
    }


    private void testSubmodel1(NodeId submodelNode) throws ServiceException, AddressSpaceException, ServiceResultException, StatusException {
        TestUtils.checkDisplayName(client, submodelNode, TestConstants.SUBMODEL_PREFIX + TestConstants.FULL_SUBMODEL_1_NAME);
        TestUtils.checkType(client, submodelNode, TestConstants.AAS_SUBMODEL_TYPE_ID);
        //TestUtils.checkType(client, submodelNode, ObjectTypeIds.AASSubmodelType);

        TestUtils.checkCommonAttributes(client, submodelNode, aasns,
                new CommonAttributesData("0", "9", "", TestConstants.FULL_SUBMODEL_1_ID, AASModellingKind.of(AASModellingKind.Options.Instance), new ArrayList<>()));

        //TestUtils.checkAdministrationNode(client, submodelNode, aasns, "0", "9");
        //TestUtils.checkModelingKindNode(client, submodelNode, aasns, AASModellingKind.of(AASModellingKind.Options.Instance));
        //TestUtils.checkCategoryNode(client, submodelNode, aasns, "");
        //TestUtils.checkEmbeddedDataSpecificationNode(client, submodelNode, aasns);

        //TestUtils.checkQualifierNode(client, submodelNode, aasns, new ArrayList<>());

        ArrayList<Qualifier> list = new ArrayList<>();
        list.add(new DefaultQualifier.Builder()
                .value("100")
                .valueType(DataTypeDefXsd.INT)
                .type("http://acplt.org/Qualifier/ExampleQualifier")
                .build());
        list.add(new DefaultQualifier.Builder()
                .value("50")
                .valueType(DataTypeDefXsd.INT)
                .type("http://acplt.org/Qualifier/ExampleQualifier2")
                .build());
        TestUtils.checkAasPropertyString(client, submodelNode, aasns, "ManufacturerName", null, Datatype.STRING,
                "http://acplt.org/ValueId/ACPLT", list);
    }

}
