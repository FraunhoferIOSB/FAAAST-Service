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
import com.prosysopc.ua.SessionActivationException;
import com.prosysopc.ua.UserIdentity;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.core.BrowsePathResult;
import com.prosysopc.ua.stack.core.BrowsePathTarget;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.RelativePath;
import com.prosysopc.ua.stack.core.RelativePathElement;
import com.prosysopc.ua.stack.core.UserTokenType;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestConstants;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestService;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.TestUtils;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASSimple;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.impl.DefaultAdministrativeInformation;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import io.adminshell.aas.v3.model.impl.DefaultRelationshipElement;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import opc.i4aas.AASKeyDataType;
import opc.i4aas.AASKeyElementsDataType;
import opc.i4aas.AASKeyTypeDataType;
import opc.i4aas.AASRelationshipElementType;
import opc.i4aas.VariableIds;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test class for additional tests of the OPC UA Endpoint with the simple example
 */
public class OpcUaEndpoint2Test {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpcUaEndpoint2Test.class);

    private static final int OPC_TCP_PORT = 18123;
    private static final long DEFAULT_TIMEOUT = 100;
    private static final String USERNAME = "testuser";
    private static final String PASSWORD = "testpassword";
    private static final String ENDPOINT_URL = "opc.tcp://localhost:" + OPC_TCP_PORT;

    private static TestService service;

    @BeforeClass
    public static void startTest() throws ConfigurationException, Exception {
        LOGGER.trace("startTest");

        Map<String, String> users = new HashMap<>();
        users.put(USERNAME, PASSWORD);
        OpcUaEndpointConfig config = new OpcUaEndpointConfig.Builder()
                .tcpPort(OPC_TCP_PORT)
                .secondsTillShutdown(0)
                .supportedAuthentication(UserTokenType.UserName)
                .serverCertificateBasePath(TestConstants.SERVER_CERT_PATH)
                .userCertificateBasePath(TestConstants.USER_CERT_PATH)
                .discoveryServerUrl(null)
                .userMap(users)
                .build();
        Path certPath = Paths.get(TestConstants.SERVER_CERT_PATH);
        if (Files.exists(certPath)) {
            Files.walk(certPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        certPath = Paths.get(TestConstants.USER_CERT_PATH);
        if (Files.exists(certPath)) {
            Files.walk(certPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }

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
    public void testDeleteSubmodel() throws SecureIdentityException, IOException, ServiceException, Exception {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        client.setUserIdentity(new UserIdentity(USERNAME, PASSWORD));
        TestUtils.initialize(client);
        client.connect();
        System.out.println("testDeleteSubmodel: client connected");

        int aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        // make sure the element exists
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_TECH_DATA_NODE_NAME)));
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

        // Send delete event to MessageBus
        CountDownLatch condition = new CountDownLatch(1);
        ElementDeleteEventMessage msg = new ElementDeleteEventMessage();
        msg.setElement(new DefaultReference.Builder()
                .key(new DefaultKey.Builder().idType(KeyType.IRI).type(KeyElements.SUBMODEL).value(AASSimple.SUBMODEL_TECHNICAL_DATA_ID).build())
                .build());
        service.getMessageBus().publish(msg);

        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);

        // check that the element is not there anymore
        bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testDeleteSubmodel Browse Result Null", bpres);
        Assert.assertEquals("testDeleteSubmodel Browse Result: size doesn't match", 2, bpres.length);
        Assert.assertTrue("testDeleteSubmodel Browse Result 1 Bad", bpres[0].getStatusCode().isBad());
        Assert.assertTrue("testDeleteSubmodel Browse Result 2 Bad", bpres[1].getStatusCode().isBad());

        System.out.println("disconnect client");
        client.disconnect();
    }


    @Test
    public void testUpdateSubmodel() throws SecureIdentityException, IOException, ServiceException, Exception {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        client.setUserIdentity(new UserIdentity(USERNAME, PASSWORD));
        TestUtils.initialize(client);
        client.connect();
        System.out.println("testUpdateSubmodel: client connected");

        int aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        // make sure one of the old elements exists, the new element exists not yet
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.TEST_RANGE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.RANGE_MAX_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
        browsePath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_OPER_DATA_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.FULL_REL_ELEMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, AASRelationshipElementType.SECOND)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.KEYS_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testUpdateSubmodel Browse Result Null", bpres);
        Assert.assertEquals("testUpdateSubmodel Browse 1 Result: size doesn't match", 2, bpres.length);
        Assert.assertTrue("testUpdateSubmodel Browse 1 Result 1 Good", bpres[0].getStatusCode().isGood());
        Assert.assertTrue("testUpdateSubmodel Browse 1 Result 2 Bad", bpres[1].getStatusCode().isBad());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testUpdateSubmodel RangeMax Null", targets);
        Assert.assertTrue("testUpdateSubmodel RangeMax empty", targets.length > 0);

        // update submodel 
        // Send update event to MessageBus
        CountDownLatch condition = new CountDownLatch(1);
        ElementUpdateEventMessage msg = new ElementUpdateEventMessage();
        msg.setElement(new DefaultReference.Builder()
                .key(new DefaultKey.Builder().idType(KeyType.IRI).type(KeyElements.SUBMODEL).value(AASSimple.SUBMODEL_OPERATIONAL_DATA_ID).build())
                .build());
        msg.setValue(new DefaultSubmodel.Builder()
                .idShort(TestConstants.SUBMODEL_OPER_DATA_NODE_NAME)
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.IRI)
                        .identifier("https://acplt.org/NewOperationalData")
                        .build())
                .administration(new DefaultAdministrativeInformation.Builder()
                        .version("1.1")
                        .revision("5")
                        .build())
                .kind(ModelingKind.INSTANCE)
                .submodelElement(new DefaultRelationshipElement.Builder()
                        .idShort("ExampleRelationshipElement")
                        .category("Parameter")
                        .description(new LangString("Example RelationshipElement object", "en-us"))
                        .description(new LangString("Beispiel RelationshipElement Element", "de"))
                        .semanticId(new DefaultReference.Builder()
                                .key(new DefaultKey.Builder()
                                        .type(KeyElements.GLOBAL_REFERENCE)
                                        .value("http://acplt.org/RelationshipElements/ExampleRelationshipElement")
                                        .idType(KeyType.IRI)
                                        .build())
                                .build())
                        .first(new DefaultReference.Builder()
                                .key(new DefaultKey.Builder()
                                        .type(KeyElements.SUBMODEL)
                                        .value("https://acplt.org/Test_Submodel")
                                        .idType(KeyType.IRI)
                                        .build())
                                .key(new DefaultKey.Builder()
                                        .type(KeyElements.SUBMODEL_ELEMENT_COLLECTION)
                                        .value("ExampleSubmodelCollectionOrdered")
                                        .idType(KeyType.ID_SHORT)
                                        .build())
                                .key(new DefaultKey.Builder()
                                        .type(KeyElements.PROPERTY)
                                        .value("ExampleProperty")
                                        .idType(KeyType.ID_SHORT)
                                        .build())
                                .build())
                        .second(new DefaultReference.Builder()
                                .key(new DefaultKey.Builder()
                                        .type(KeyElements.SUBMODEL)
                                        .value("http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial")
                                        .idType(KeyType.IRI)
                                        .build())
                                .key(new DefaultKey.Builder()
                                        .type(KeyElements.ENTITY)
                                        .value("ExampleEntity")
                                        .idType(KeyType.ID_SHORT)
                                        .build())
                                .key(new DefaultKey.Builder()
                                        .type(KeyElements.PROPERTY)
                                        .value("ExampleProperty2")
                                        .idType(KeyType.ID_SHORT)
                                        .build())
                                .build())
                        .build())
                .build());
        service.getMessageBus().publish(msg);

        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);

        // check that the old element is not there anymore, but the new element
        bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testUpdateSubmodel Browse Result old Null", bpres);
        Assert.assertEquals("testUpdateSubmodel Browse 2 Result: size doesn't match", 2, bpres.length);
        Assert.assertTrue("testUpdateSubmodel Browse 2 Result 1 Bad", bpres[0].getStatusCode().isBad());
        Assert.assertTrue("testUpdateSubmodel Browse 2 Result 2 Good", bpres[1].getStatusCode().isGood());

        // read a value of the new SubmodelElement
        targets = bpres[1].getTargets();
        Assert.assertNotNull("testUpdateSubmodel RelationshipElement Null", targets);
        Assert.assertTrue("testUpdateSubmodel RelationshipElement empty", targets.length > 0);

        List<AASKeyDataType> smeValue = new ArrayList<>();
        smeValue.add(new AASKeyDataType(AASKeyElementsDataType.Submodel, "http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial", AASKeyTypeDataType.IRI));
        smeValue.add(new AASKeyDataType(AASKeyElementsDataType.Entity, "ExampleEntity", AASKeyTypeDataType.IdShort));
        smeValue.add(new AASKeyDataType(AASKeyElementsDataType.Property, "ExampleProperty2", AASKeyTypeDataType.IdShort));

        DataValue value = client.readValue(client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId()));
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Assert.assertArrayEquals("new SubmodelElement value not equal", smeValue.toArray(AASKeyDataType[]::new), (AASKeyDataType[]) value.getValue().getValue());

        System.out.println("disconnect client");
        client.disconnect();
    }


    @Test
    public void testUpdateSubmodelElement() throws SecureIdentityException, IOException, ServiceException, Exception {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        client.setUserIdentity(new UserIdentity(USERNAME, PASSWORD));
        TestUtils.initialize(client);
        client.connect();
        System.out.println("testUpdateSubmodelElement: client connected");

        int aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        // make sure one of the old elements exists, the new element exists not yet
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_DOC_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.OPERATING_MANUAL_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_DOC_PROPERTY_TITLE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
        browsePath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_DOC_NODE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.OPERATING_MANUAL_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.SUBMODEL_DOC_FILE_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testUpdateSubmodelElement Browse Result Null", bpres);
        Assert.assertEquals("testUpdateSubmodelElement Browse 1 Result: size doesn't match", 2, bpres.length);
        Assert.assertTrue("testUpdateSubmodelElement Browse 1 Result 1 Good", bpres[0].getStatusCode().isGood());
        Assert.assertTrue("testUpdateSubmodelElement Browse 1 Result 2 Good", bpres[1].getStatusCode().isGood());

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("testUpdateSubmodelElement Property Value Null", targets);
        Assert.assertTrue("testUpdateSubmodelElement Property Value empty", targets.length > 0);

        // read the (old) value of the SubmodelElement
        DataValue value = client.readValue(client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId()));
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Assert.assertEquals("old SubmodelElement value not equal", "OperatingManual", value.getValue().toString());

        String newValue = "New Test Value";

        // update SubmodelElement 
        // Send update event to MessageBus
        CountDownLatch condition = new CountDownLatch(1);
        ElementUpdateEventMessage msg = new ElementUpdateEventMessage();
        msg.setElement(new DefaultReference.Builder()
                .key(new DefaultKey.Builder().idType(KeyType.IRI).type(KeyElements.SUBMODEL).value(TestConstants.SUBMODEL_DOC_NAME).build())
                .key(new DefaultKey.Builder().idType(KeyType.ID_SHORT).type(KeyElements.SUBMODEL_ELEMENT_COLLECTION).value(TestConstants.OPERATING_MANUAL_NAME).build())
                .build());
        msg.setValue(new DefaultSubmodelElementCollection.Builder()
                .kind(ModelingKind.INSTANCE)
                .idShort("OperatingManual")
                .value(new DefaultProperty.Builder()
                        .kind(ModelingKind.INSTANCE)
                        .idShort("Title")
                        .value(newValue)
                        .valueType("string")
                        .build())
                .ordered(false)
                .allowDuplicates(false)
                .build());
        service.getMessageBus().publish(msg);

        // check MessageBus
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);

        bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testUpdateSubmodelElement Browse 2 Result Null", bpres);
        Assert.assertEquals("testUpdateSubmodelElement Browse 2 Result: size doesn't match", 2, bpres.length);
        Assert.assertTrue("testUpdateSubmodelElement Browse 2 Result 1 Good", bpres[0].getStatusCode().isGood());
        Assert.assertTrue("testUpdateSubmodelElement Browse 2 Result 2 Bad", bpres[1].getStatusCode().isBad());

        // read the (new) value of the SubmodelElement
        targets = bpres[0].getTargets();
        Assert.assertNotNull("testUpdateSubmodelElement Property New Value Null", targets);
        Assert.assertTrue("testUpdateSubmodelElement Property New Value empty", targets.length > 0);

        NodeId writeNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        value = client.readValue(writeNode);
        System.out.println("testUpdateSubmodelElement: writeNode: " + writeNode.toString());

        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Assert.assertEquals("new SubmodelElement value not equal", newValue, value.getValue().toString());

        System.out.println("disconnect client");
        client.disconnect();
    }


    @Test(expected = SessionActivationException.class)
    public void testPreventAnonymousAccess() throws SessionActivationException, SecureIdentityException, IOException, ServiceException {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        // The call to connect is expected to throw an exception as anonymous access is not allowed
        client.connect();
    }
}
