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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper;

import static opc.ua.aas.UaDataTypeIds.AASEmbeddedConceptDescription;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaReference;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.ApplicationDescription;
import com.prosysopc.ua.stack.core.ApplicationType;
import com.prosysopc.ua.stack.core.BrowsePathResult;
import com.prosysopc.ua.stack.core.BrowsePathTarget;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.NodeClass;
import com.prosysopc.ua.stack.core.RelativePath;
import com.prosysopc.ua.stack.core.RelativePathElement;
import com.prosysopc.ua.stack.core.StatusCodes;
import com.prosysopc.ua.types.opcua.BaseDataVariableType;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import java.io.IOException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import opc.ua.aas.DataTypeIds;
import opc.ua.aas.ReferenceTypeIds;
import opc.ua.aas.datatypes.AASAdministrativeInformation;
import opc.ua.aas.datatypes.AASAssetAdministrationShellCommonAttributes;
import opc.ua.aas.datatypes.AASAssetKind;
import opc.ua.aas.datatypes.AASDataSpecificationIec61360;
import opc.ua.aas.datatypes.AASEmbeddedConceptDescription;
import opc.ua.aas.datatypes.AASEmbeddedDataSpecification;
import opc.ua.aas.datatypes.AASHasKind;
import opc.ua.aas.datatypes.AASIdentifiable;
import opc.ua.aas.datatypes.AASModellingKind;
import opc.ua.aas.datatypes.AASQualifiable;
import opc.ua.aas.datatypes.AASQualifier;
import opc.ua.aas.datatypes.AASReferable;
import opc.ua.aas.datatypes.AASReference;
import opc.ua.aas.datatypes.AASSpecificAssetId;
import opc.ua.aas.datatypes.AASSubmodelCommonAttributes;
import opc.ua.aas.datatypes.AASSubmodelElementCommonAttributes;
import opc.ua.aas.objecttypes.AASAssetInformationType;
import org.awaitility.Awaitility;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test utilities
 */
public class TestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

    private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);
    private static final Duration MAX_TIMEOUT = Duration.ofSeconds(5);

    public static void initialize(UaClient client) throws SecureIdentityException, IOException, UnknownHostException {
        ApplicationDescription appDescription = new ApplicationDescription();
        appDescription.setApplicationName(new LocalizedText("AAS UnitTest Client", Locale.ENGLISH));
        // 'localhost' (all lower case) in the URI is converted to the actual
        // host name of the computer in which the application is run
        appDescription.setApplicationUri("urn:localhost:UA:AASUnitTestClient");
        appDescription.setProductUri("urn:iosb.fraunhofer.de:UA:AASUnitTestClient");
        appDescription.setApplicationType(ApplicationType.Client);

        final ApplicationIdentity identity = new ApplicationIdentity();
        identity.setApplicationDescription(appDescription);
        client.setApplicationIdentity(identity);
    }


    public static void checkBrowseName(UaClient client, NodeId nodeId, String desiredName) throws ServiceException, AddressSpaceException {
        UaNode node = client.getAddressSpace().getNode(nodeId);
        Assert.assertNotNull("Node is null: " + desiredName, node);
        checkBrowseName(node, desiredName);
    }


    public static void checkBrowseName(UaNode node, String desiredName) {
        QualifiedName qname = node.getBrowseName();
        Assert.assertNotNull(qname);
        Assert.assertEquals("BrowseName not equal", desiredName, qname.getName());
    }


    public static void checkDisplayName(UaClient client, NodeId nodeId, String desiredName) throws ServiceException, AddressSpaceException {
        UaNode node = client.getAddressSpace().getNode(nodeId);
        Assert.assertNotNull("Node is null: " + desiredName, node);
        LocalizedText lt = node.getDisplayName();
        Assert.assertEquals(desiredName, lt.getText());
    }

    //    public static void checkModelingKindNode(UaClient client, NodeId baseNode, int aasns, AASModellingKind modelingKind)
    //            throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
    //        List<RelativePath> relPath = new ArrayList<>();
    //        List<RelativePathElement> browsePath = new ArrayList<>();
    //        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.KIND_NAME)));
    //        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
    //
    //        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(baseNode, relPath.toArray(RelativePath[]::new));
    //        Assert.assertNotNull("checkModelingKindNode Browse Result Null", bpres);
    //        Assert.assertEquals("checkModelingKindNode Browse Result: size doesn't match", 1, bpres.length);
    //
    //        BrowsePathTarget[] targets = bpres[0].getTargets();
    //        if (modelingKind == null) {
    //            Assert.assertNull("checkModelingKindNode Browse Target Node not Null", targets);
    //        }
    //        else {
    //            Assert.assertNotNull("checkModelingKindNode Browse Target Node Null", targets);
    //            Assert.assertTrue("checkModelingKindNode Browse targets empty", targets.length > 0);
    //
    //            checkModelingKind(client, client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId()), modelingKind);
    //        }
    //    }


    public static void checkSubmodelElementCommonAttributes(UaClient client, int aasns, NodeId baseNode, String category, List<Qualifier> qualifier)
            throws ServiceException, AddressSpaceException, ServiceResultException {

        NodeId commonAttributesNodeId = getCommonAttributes(client, baseNode, aasns);

        checkType(client, commonAttributesNodeId, Identifiers.BaseDataVariableType);
        checkDatatype(client, commonAttributesNodeId, TestConstants.SUBMODEL_ELEMENT_COMMON_ATTRIBUTES_TYPE);

        Object value = getVariableValue(client, commonAttributesNodeId);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof AASSubmodelElementCommonAttributes);
        AASSubmodelElementCommonAttributes commonAttributesValue = (AASSubmodelElementCommonAttributes) value;

        checkReferable(commonAttributesValue.getReferable(), category);
        checkQualifier(commonAttributesValue.getQualifiable(), qualifier);
    }

    //    public static void checkCategoryNode(UaClient client, NodeId node, int aasns, String category)
    //            throws ServiceException, StatusException, AddressSpaceException, ServiceResultException {
    //        List<RelativePath> relPath = new ArrayList<>();
    //        List<RelativePathElement> browsePath = new ArrayList<>();
    //        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.CATEGORY_NAME)));
    //        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
    //
    //        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(node, relPath.toArray(RelativePath[]::new));
    //        Assert.assertNotNull("Category Result Null", bpres);
    //        Assert.assertEquals("Category Result: size doesn't match", 1, bpres.length);
    //
    //        BrowsePathTarget[] targets = bpres[0].getTargets();
    //        Assert.assertNotNull("Browse Category Null", targets);
    //        Assert.assertTrue("Category targets empty", targets.length > 0);
    //        checkType(client, targets[0].getTargetId(), new NodeId(aasns, TestConstants.AAS_PROPERTY_TYPE_ID));
    //
    //        DataValue value = client.readValue(targets[0].getTargetId());
    //        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
    //        String str = "";
    //        if (!value.getValue().isEmpty()) {
    //            str = value.getValue().toString();
    //        }
    //        Assert.assertEquals(category.isEmpty(), str.isEmpty());
    //        if (!category.isEmpty()) {
    //            Assert.assertEquals(category, value.getValue().toString());
    //        }
    //    }

    //    public static void checkDataSpecificationNode(UaClient client, NodeId node, int aasns) throws ServiceException, ServiceResultException, AddressSpaceException {
    //        List<RelativePath> relPath = new ArrayList<>();
    //        List<RelativePathElement> browsePath = new ArrayList<>();
    //        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.DATA_SPECIFICATION_NAME)));
    //        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
    //
    //        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(node, relPath.toArray(RelativePath[]::new));
    //        Assert.assertNotNull("checkDataSpecificationNode Browse Result Null", bpres);
    //        Assert.assertEquals("checkDataSpecificationNode Browse Result: size doesn't match", 1, bpres.length);
    //
    //        BrowsePathTarget[] targets = bpres[0].getTargets();
    //        Assert.assertNotNull("checkDataSpecificationNode Node Targets Null", targets);
    //        Assert.assertTrue("checkDataSpecificationNode Node targets empty", targets.length > 0);
    //
    //        // Currently we only check that the NodeId is not null and we have the correct type
    //        NodeId dataSpecNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
    //        Assert.assertFalse("checkDataSpecificationNode Node not found", NodeId.isNull(dataSpecNode));
    //
    //        checkType(client, dataSpecNode, new NodeId(aasns, TestConstants.AAS_REFERENCE_LIST_ID));
    //    }

    //    public static void checkEmbeddedDataSpecificationNode(AASHasDataSpecification dataSpecificationNode, NodeId node, int aasns) throws ServiceException, ServiceResultException, AddressSpaceException {
    //        List<RelativePath> relPath = new ArrayList<>();
    //        List<RelativePathElement> browsePath = new ArrayList<>();
    //        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.EMBEDDED_DATA_SPECIFICATION_NAME)));
    //        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
    //
    //        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(node, relPath.toArray(RelativePath[]::new));
    //        Assert.assertNotNull("checkEmbeddedDataSpecificationNode Browse Result Null", bpres);
    //        Assert.assertEquals("checkEmbeddedDataSpecificationNode Browse Result: size doesn't match", 1, bpres.length);
    //
    //        BrowsePathTarget[] targets = bpres[0].getTargets();
    //        Assert.assertNotNull("checkEmbeddedDataSpecificationNode Node Targets Null", targets);
    //        Assert.assertTrue("checkEmbeddedDataSpecificationNode Node targets empty", targets.length > 0);
    //
    //        // Currently we only check that the NodeId is not null and we have the correct type
    //        NodeId dataSpecNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
    //        Assert.assertFalse("checkEmbeddedDataSpecificationNode Node not found", NodeId.isNull(dataSpecNode));
    //
    //        checkType(client, dataSpecNode, new NodeId(aasns, TestConstants.AAS_EMBEDDED_DATA_SPECIFICATION_LIST));
    //    }


    public static void checkQualifier(AASQualifiable qualifiable, List<Qualifier> qualifierList) {
        if (qualifiable == null) {
            Assert.assertTrue(qualifierList.isEmpty());
        }
        else {
            checkQualifierList(qualifierList, List.of(qualifiable.getQualifier()));
        }
    }


    public static void checkAssetInformationNode(UaClient client, NodeId baseNode, int aasns)
            throws ServiceException, ServiceResultException, AddressSpaceException, StatusException {
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestConstants.ASSET_INFORMATION_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(baseNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("checkAssetInformationNode Browse(1) Result Null", bpres);
        Assert.assertEquals("checkAssetInformationNode Browse(1) Result: size doesn't match", 1, bpres.length);

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("checkAssetInformationNode Browse AssetInfo Node Null", targets);
        Assert.assertTrue("checkAssetInformationNode Browse AssetInfo targets empty", targets.length > 0);
        NodeId assetInfoNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        Assert.assertNotNull(assetInfoNode);
        Assert.assertNotEquals(NodeId.NULL, assetInfoNode);

        checkType(client, assetInfoNode, TestConstants.AAS_ASSET_INFO_TYPE_ID);
        //checkType(client, assetInfoNode, client.getAddressSpace().getNamespaceTable().toNodeId(ObjectTypeIds.AASAssetInformationType));
        checkAssetKindNode(client, assetInfoNode, aasns, AASAssetKind.of(AASAssetKind.Options.Instance));
        checkAasPropertyThumbnail(client, assetInfoNode, aasns, TestConstants.DEFAULT_THUMB_NAME, AASModellingKind.Instance, "", "image/png",
                "file:///master/verwaltungsschale-detail-part1.png", 0);

        checkVariableString(client, assetInfoNode, aasns, TestConstants.GLOBAL_ASSET_ID_NAME,
                "http://customer.com/assets/KHBVZJSQKIY");

        Map<String, String> map = new HashMap<>();
        map.put("DeviceID", "QjYgPggjwkiHk4RrQiYSLg==");
        map.put("EquipmentID", "538fd1b3-f99f-4a52-9c75-72e9fa921270");
        checkSpecificAssetIdListNode(client, assetInfoNode, aasns, map);
    }


    public static void checkVariableBool(UaClient client, NodeId node, int aasns, String name, boolean propValue) throws ServiceException, StatusException {
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, name)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(node, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("checkVariableBool Browse Result Null", bpres);
        Assert.assertEquals("checkVariableBool Browse Result: size doesn't match", 1, bpres.length);

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("checkVariableBool Node Targets Null", targets);
        Assert.assertTrue("checkVariableBool Node targets empty", targets.length > 0);

        DataValue value = client.readValue(targets[0].getTargetId());
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Assert.assertEquals(propValue, value.getValue().booleanValue());
    }


    public static void checkAasPropertyString(UaClient client, NodeId node, int aasns, String name, String category, Datatype valueType,
                                              String propValue, List<Qualifier> qualifierList)
            throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        NodeId propertyNode = getSubmodelElement(aasns, name, client, node);

        checkType(client, propertyNode, TestConstants.AAS_PROPERTY_TYPE_ID);
        checkDisplayName(client, propertyNode, name);

        checkSubmodelElementCommonAttributes(client, aasns, propertyNode, category, qualifierList);
        //checkCategoryNode(client, propertyNode, aasns, category);
        //checkEmbeddedDataSpecificationNode(client, propertyNode, aasns);
        //checkQualifierNode(client, propertyNode, aasns, qualifierList);

        //        relPath.clear();
        //        browsePath.clear();
        //        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_TYPE_NAME)));
        //        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
        //        browsePath.clear();
        //        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        //        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
        //
        //        bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(propertyNode, relPath.toArray(RelativePath[]::new));
        //        Assert.assertNotNull("checkAasPropertyString Browse Value & Type Result Null", bpres);
        //        Assert.assertEquals("checkAasPropertyString Browse Value & Type Result: size doesn't match", 2, bpres.length);
        //
        //        targets = bpres[0].getTargets();
        //        Assert.assertNotNull("checkAasPropertyString ValueType Null", targets);
        //        Assert.assertTrue("checkAasPropertyString ValueType empty", targets.length > 0);
        //        DataValue value = client.readValue(targets[0].getTargetId());
        //        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        //        Assert.assertEquals(valueType.getName(), value.getValue().toString());
        //
        //        targets = bpres[1].getTargets();
        //        Assert.assertNotNull("checkAasPropertyString Value Null", targets);
        //        Assert.assertTrue("checkAasPropertyString value empty", targets.length > 0);
        //        value = client.readValue(targets[0].getTargetId());
        //        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());

        UaVariable varNode = (UaVariable) client.getAddressSpace().getNode(propertyNode);
        NodeId datatypeNode = varNode.getDataTypeId();
        Assert.assertEquals(ValueConverter.convertDataTypeToNodeId(valueType, client), datatypeNode);

        DataValue value = varNode.getValue();

        Variant var = new Variant(propValue);
        Assert.assertEquals(var, value.getValue());
    }


    public static void checkAasPropertyObject(UaClient client, NodeId node, int aasns, String name, String category, Datatype valueType,
                                              Object propValue, List<Qualifier> qualifierList)
            throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        NodeId propertyNode = getSubmodelElement(aasns, name, client, node);

        checkType(client, propertyNode, TestConstants.AAS_PROPERTY_TYPE_ID);
        checkDisplayName(client, propertyNode, name);
        checkSubmodelElementCommonAttributes(client, aasns, propertyNode, category, qualifierList);
        //checkCategoryNode(client, propertyNode, aasns, category);
        //checkEmbeddedDataSpecificationNode(client, propertyNode, aasns);
        //checkQualifierNode(client, propertyNode, aasns, qualifierList);

        UaVariable varNode = (UaVariable) client.getAddressSpace().getNode(propertyNode);
        NodeId datatypeNode = varNode.getDataTypeId();
        Assert.assertEquals(ValueConverter.convertDataTypeToNodeId(valueType, client), datatypeNode);

        DataValue value = varNode.getValue();

        //        relPath.clear();
        //        browsePath.clear();
        //        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_TYPE_NAME)));
        //        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
        //        browsePath.clear();
        //        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        //        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
        //
        //        bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(propertyNode, relPath.toArray(RelativePath[]::new));
        //        Assert.assertNotNull("checkAasPropertyObject Browse Value & Type Result Null", bpres);
        //        Assert.assertEquals("checkAasPropertyObject Browse Value & Type Result: size doesn't match", 2, bpres.length);
        //
        //        targets = bpres[0].getTargets();
        //        Assert.assertNotNull("checkAasPropertyObject ValueType Null", targets);
        //        Assert.assertTrue("checkAasPropertyObject ValueType empty", targets.length > 0);
        //        DataValue value = client.readValue(targets[0].getTargetId());
        //        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        //        Assert.assertEquals(valueType.getName(), value.getValue().toString());
        //
        //        targets = bpres[1].getTargets();
        //        Assert.assertNotNull("checkAasPropertyObject Value Null", targets);
        //        Assert.assertTrue("checkAasPropertyObject value empty", targets.length > 0);
        //        value = client.readValue(targets[0].getTargetId());
        //        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());

        Variant var = new Variant(propValue);
        Assert.assertEquals(var, value.getValue());
    }


    public static void checkAasPropertyFile(UaClient client, NodeId node, int aasns, String name, AASModellingKind kind, String category, String mimeType, String propPath,
                                            int fileSize)
            throws ServiceException, ServiceResultException, AddressSpaceException, StatusException {
        NodeId propertyNode = getSubmodelElement(aasns, name, client, node);

        checkType(client, propertyNode, TestConstants.AAS_FILE_TYPE_ID);
        checkDisplayName(client, propertyNode, name);

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HasComponent, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_CONTENT_TYPE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
        browsePath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HasComponent, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
        browsePath.clear();

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(propertyNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("checkAasPropertyFile Browse Value & Type Result Null", bpres);
        Assert.assertEquals("checkAasPropertyFile Browse Value & Type Result: size doesn't match", 2, bpres.length);

        // ContentType
        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("checkAasPropertyFile ContentType Null", targets);
        Assert.assertTrue("checkAasPropertyFile ContentType empty", targets.length > 0);
        DataValue value = client.readValue(targets[0].getTargetId());
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Assert.assertEquals(mimeType, value.getValue().toString());

        // Value
        targets = bpres[1].getTargets();
        Assert.assertNotNull("checkAasPropertyFile Path Null", targets);
        Assert.assertTrue("checkAasPropertyFile Path empty", targets.length > 0);
        value = client.readValue(targets[0].getTargetId());
        Assert.assertEquals(propPath, value.getValue().toString());
    }


    public static void checkType(UaClient client, NodeId node, ExpandedNodeId typeNode) throws ServiceException, AddressSpaceException, ServiceResultException {
        checkType(client, node, client.getAddressSpace().getNamespaceTable().toNodeId(typeNode));
    }


    public static void checkType(UaClient client, ExpandedNodeId node, NodeId typeNode) throws ServiceException, AddressSpaceException, ServiceResultException {
        checkType(client, client.getAddressSpace().getNamespaceTable().toNodeId(node), typeNode);
    }


    public static void checkType(UaClient client, NodeId node, NodeId typeNode) throws ServiceException, AddressSpaceException, ServiceResultException {
        NodeId refId = getType(client, node);
        Assert.assertEquals("type not equal", typeNode, refId);
    }


    public static void checkSubmodelRefs(UaClient client, NodeId baseNode, int aasns, List<AASReference> submodelRefs)
            throws ServiceException, ServiceResultException, AddressSpaceException, StatusException {

        Object value = getVariableValue(client, baseNode);
        Assert.assertTrue(value instanceof AASReference[]);
        AASReference[] vars = (AASReference[]) value;
        Assert.assertEquals(submodelRefs.size(), vars.length);
        for (int index = 0; index < vars.length; index++) {
            Assert.assertTrue(Objects.equals((AASReference) vars[index], submodelRefs.get(index)));
        }
    }


    public static void writeNewValueIntern(UaClient client, NodeId writeNode, Object oldValue, Object newValue) throws ServiceException, StatusException {
        DataValue value = client.readValue(writeNode);
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        if (oldValue == null) {
            Assert.assertTrue("intial null value not equal", value.getValue().isEmpty());
        }
        else {
            Assert.assertEquals("intial value not equal", oldValue, value.getValue().getValue());
        }

        client.writeValue(writeNode, newValue);

        // check new value
        // unable to deterministically know when the changes will materialize, therefore wait for some time
        Awaitility.await()
                .alias("check value updated in OPC UA endpoint")
                .pollInterval(POLL_TIMEOUT)
                .atMost(MAX_TIMEOUT)
                .until(() -> {
                    DataValue val = client.readValue(writeNode);
                    if (val.getStatusCode().isGood()) {
                        Object v = val.getValue().getValue();
                        LOGGER.info("writeNewValueIntern: val: {}; old: {}; new: {}", v, oldValue, newValue);
                    }
                    return val.getStatusCode().isGood() && (val.getValue() != null) && Objects.equals(val.getValue().getValue(), newValue);
                });
    }


    public static void writeNewValueArray(UaClient client, NodeId writeNode, LocalizedText[] oldValue, LocalizedText[] newValue)
            throws ServiceException, StatusException {
        DataValue value = client.readValue(writeNode);
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Assert.assertArrayEquals("intial value not equal", oldValue, (LocalizedText[]) value.getValue().getValue());

        client.writeValue(writeNode, newValue);

        // check new value
        // unable to deterministically know when the changes will materialize, therefore wait for some time
        Awaitility.await()
                .alias("check value updated in OPC UA endpoint")
                .pollInterval(POLL_TIMEOUT)
                .atMost(MAX_TIMEOUT)
                .until(() -> {
                    DataValue val = client.readValue(writeNode);
                    return val.getStatusCode().isGood() && (val.getValue() != null) && Arrays.equals((LocalizedText[]) val.getValue().getValue(), newValue);
                });
    }


    public static void writeNewValueReference(UaClient client, NodeId writeNode, AASReference oldValue, AASReference newValue)
            throws ServiceException, StatusException {
        DataValue value = client.readValue(writeNode);
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Assert.assertEquals("intial value not equal", oldValue, (AASReference) value.getValue().getValue());

        boolean rv = client.writeValue(writeNode, newValue);
        Assert.assertTrue(rv);

        // check new value
        // unable to deterministically know when the changes will materialize, therefore wait for some time
        Awaitility.await()
                .alias("check value updated in OPC UA endpoint")
                .pollInterval(POLL_TIMEOUT)
                .atMost(MAX_TIMEOUT)
                .until(() -> {
                    DataValue val = client.readValue(writeNode);
                    //if (val.getStatusCode().isGood()) {
                    //    boolean eq = AasReferenceEquals((AASReference) val.getValue().getValue(), oldValue);
                    //    LOGGER.info("writeNewValueArray: equal: {}; rv: {}; old: {}", eq, (AASReference) val.getValue().getValue(), oldValue);
                    //}
                    return val.getStatusCode().isGood() && (val.getValue() != null) && Objects.equals((AASReference) val.getValue().getValue(), newValue);
                });
    }


    public static void checkCommonAttributes(UaClient client, NodeId baseNode, int aasns, CommonAttributesData data)
            throws ServiceException, StatusException, AddressSpaceException, ServiceResultException {

        NodeId commonAttrNode = getCommonAttributes(client, baseNode, aasns);

        NodeId type = getType(client, commonAttrNode);
        if (client.getNamespaceTable().nodeIdEquals(type, DataTypeIds.AASSubmodelCommonAttributes)) {
            checkSubmodelCommonAttributes(client, commonAttrNode, data);
        }
        else if (client.getNamespaceTable().nodeIdEquals(type, DataTypeIds.AASAssetAdministrationShellCommonAttributes)) {
            checkAasCommonAttributes(client, commonAttrNode, data);
        }
    }


    public static void checkIdentifiable(AASIdentifiable identifiable, String id, String version, String revision) {
        if (identifiable == null) {
            Assert.assertNull(id);
            Assert.assertNull(version);
            Assert.assertNull(revision);
        }
        else {
            Assert.assertEquals(id, identifiable.getId());

            checkAdministration(identifiable, version, revision);
        }
    }


    public static void checkAasPropertyThumbnail(UaClient client, NodeId node, int aasns, String name, AASModellingKind kind, String category, String mimeType,
                                                 String propPath,
                                                 int fileSize)
            throws ServiceException, ServiceResultException, AddressSpaceException, StatusException {
        NodeId propertyNode = getSubmodelElement(aasns, name, client, node);

        checkDisplayName(client, propertyNode, name);

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_CONTENT_TYPE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
        browsePath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_PATH_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
        browsePath.clear();

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(propertyNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("checkAasPropertyThumbnail Browse Path & Type Result Null", bpres);
        Assert.assertEquals("checkAasPropertyThumbnail Browse Path & Type Result: size doesn't match", 2, bpres.length);

        // ContentType
        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("checkAasPropertyThumbnail ContentType Null", targets);
        Assert.assertTrue("checkAasPropertyThumbnail ContentType empty", targets.length > 0);
        DataValue value = client.readValue(targets[0].getTargetId());
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Assert.assertEquals(mimeType, value.getValue().toString());

        // Path
        targets = bpres[1].getTargets();
        Assert.assertNotNull("checkAasPropertyThumbnail Path Null", targets);
        Assert.assertTrue("checkAasPropertyThumbnail Path empty", targets.length > 0);
        value = client.readValue(targets[0].getTargetId());
        Assert.assertEquals(propPath, value.getValue().toString());
    }


    public static void checkVariableString(UaClient client, NodeId node, int aasns, String name, String propValue)
            throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, name)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(node, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("checkVariableString Browse Property Result Null", bpres);
        Assert.assertEquals("checkVariableString Browse Property Result: size doesn't match", 1, bpres.length);

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("checkAasPropertyString Property Null", targets);
        Assert.assertTrue("checkAasPropertyString Property empty", targets.length > 0);
        NodeId propertyNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        checkDisplayName(client, propertyNode, name);

        DataValue value = client.readValue(propertyNode);
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());

        Variant var = new Variant(propValue);
        Assert.assertEquals(var, value.getValue());
    }


    public static void checkDescriptions(UaClient client, NodeId nodeId, List<LocalizedText> descriptions) throws ServiceException, AddressSpaceException {
        UaNode node = client.getAddressSpace().getNode(nodeId);
        Assert.assertNotNull(node);
        LocalizedText text = node.getDescription();
        if ((descriptions == null) || (descriptions.isEmpty())) {
            Assert.assertNull(text);
        }
        else {
            Assert.assertEquals(descriptions.get(0), text);
        }
    }


    public static void checkConceptDescription(UaClient client, NodeId nodeId, int aasns, String id, String version, String revision, DataSpecificationData data)
            throws ServiceResultException, ServiceException, StatusException {
        NodeId conceptDescriptionNode = getConceptDescription(client, nodeId, aasns);

        DataValue value = client.readValue(conceptDescriptionNode);
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());

        Variant var = value.getValue();
        Assert.assertTrue(var.getValue() instanceof AASEmbeddedConceptDescription);
        AASEmbeddedConceptDescription cd = (AASEmbeddedConceptDescription) var.getValue();

        Assert.assertNotNull(cd.getCommonAttributes());
        checkIdentifiable(cd.getCommonAttributes().getIdentifiable(), id, version, revision);

        Assert.assertNotNull(cd.getEmbeddedDataSpecification());
        Assert.assertEquals(1, cd.getEmbeddedDataSpecification().length);

        AASEmbeddedDataSpecification embed = cd.getEmbeddedDataSpecification()[0];
        Assert.assertNotNull(embed);
        if (data != null) {
            Assert.assertEquals(data.dataSpecification(), embed.getDataSpecification());
            Assert.assertNotNull(embed.getDataSpecificationContent());
            Assert.assertTrue(embed.getDataSpecificationContent() instanceof AASDataSpecificationIec61360);
            AASDataSpecificationIec61360 ds61360 = (AASDataSpecificationIec61360) embed.getDataSpecificationContent();
            Assert.assertEquals(data.unit(), ds61360.getUnit());
            Assert.assertArrayEquals(data.preferredName(), ds61360.getPreferredName());
            Assert.assertEquals(data.sourceOfDefinition(), ds61360.getSourceOfDefinition());
            Assert.assertEquals(data.datatype(), ds61360.getDataType());
            Assert.assertArrayEquals(data.definition(), ds61360.getDefinition());
            Assert.assertEquals(data.unitId(), ds61360.getUnitId());
        }
    }


    public static void checkSubmodelElementConceptDescription(UaClient client, NodeId baseNodeId, String name, int aasns, String id, String version, String revision,
                                                              DataSpecificationData data)
            throws ServiceException, ServiceResultException, StatusException {
        NodeId submodelElementNode = getSubmodelElement(aasns, name, client, baseNodeId);
        checkConceptDescription(client, submodelElementNode, aasns, id, version, revision, data);
    }


    private static NodeId getConceptDescription(UaClient client, NodeId baseNode, int aasns) throws ServiceResultException, ServiceException {
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(client.getAddressSpace().getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasConceptDescription), false, true,
                new QualifiedName(aasns, TestConstants.CONCEPT_DESCRIPTION_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(baseNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull(bpres);
        Assert.assertEquals(1, bpres.length);

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull(targets);
        Assert.assertTrue(targets.length > 0);
        NodeId node = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        return node;
    }


    private static void checkDatatype(UaClient client, NodeId nodeId, ExpandedNodeId datatype) throws ServiceException, AddressSpaceException, ServiceResultException {
        checkDatatype(client, nodeId, client.getAddressSpace().getNamespaceTable().toNodeId(datatype));
    }


    private static void checkDatatype(UaClient client, NodeId nodeId, NodeId datatype) throws ServiceException, AddressSpaceException {
        UaNode uanode = client.getAddressSpace().getNode(nodeId);
        Assert.assertEquals(NodeClass.Variable, uanode.getNodeClass());
        UaVariable varnode = (UaVariable) uanode;
        Assert.assertEquals(datatype, varnode.getDataTypeId());
    }


    private static void checkModelingKind(AASHasKind kindNode, AASModellingKind modelingKind)
            throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        if (kindNode == null) {
            Assert.assertNull(modelingKind);
        }
        else {
            Assert.assertEquals(modelingKind, kindNode.getKind());
        }
    }


    private static void checkAssetKindNode(UaClient client, NodeId baseNode, int aasns, AASAssetKind assetKind)
            throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HasComponent, false, true, new QualifiedName(aasns, TestConstants.ASSET_KIND_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(baseNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("checkAssetKindNode Browse Result Null", bpres);
        Assert.assertEquals("checkAssetKindNode Browse Result: size doesn't match", 1, bpres.length);

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("checkAssetKindNode Browse Target Node Null", targets);
        Assert.assertTrue("checkAssetKindNode Browse targets empty", targets.length > 0);

        checkAssetKind(client, client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId()), assetKind);
    }


    private static void checkAssetKind(UaClient client, NodeId kindNode, AASAssetKind assetKind)
            throws ServiceException, AddressSpaceException, StatusException, ServiceResultException {
        checkDisplayName(client, kindNode, TestConstants.ASSET_KIND_NAME);
        checkType(client, kindNode, Identifiers.BaseDataVariableType);
        checkDatatype(client, kindNode, TestConstants.ASSET_KIND_TYPE);

        DataValue value = client.readValue(kindNode);
        Assert.assertEquals(assetKind, value.getValue().asOptionSet(AASAssetKind.SPECIFICATION));
    }

    //    private static void checkAasReference(UaClient client, NodeId node, int aasns, List<AASKey> refKeys)
    //            throws ServiceException, AddressSpaceException, ServiceResultException, StatusException {
    //        checkType(client, node, new NodeId(aasns, TestConstants.AAS_REFERENCE_TYPE_ID));
    //
    //        List<RelativePathElement> browsePath = new ArrayList<>();
    //        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, "Keys")));
    //        BrowsePathTarget[] targetsProp = client.getAddressSpace().translateBrowsePathToNodeId(node, browsePath.toArray(RelativePathElement[]::new));
    //        Assert.assertNotNull("Property Keys Null", targetsProp);
    //        Assert.assertTrue("Property Keys empty", targetsProp.length > 0);
    //
    //        checkType(client, targetsProp[0].getTargetId(), new NodeId(aasns, TestConstants.AAS_PROPERTY_TYPE_ID));
    //        UaVariable variable = (UaVariable) client.getAddressSpace().getNode(targetsProp[0].getTargetId());
    //        UaType dataType = variable.getDataType();
    //        Assert.assertNotNull("DataType null", dataType);
    //        Assert.assertEquals("DataType not equal", new NodeId(aasns, TestConstants.AAS_KEY_DATA_TYPE_ID), dataType.getNodeId());
    //
    //        DataValue value = client.readValue(targetsProp[0].getTargetId());
    //        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
    //        Assert.assertNotNull("Value null", value.getValue());
    //        Variant var = value.getValue();
    //        Object o = var.getValue();
    //        Assert.assertTrue("Keys no array", var.isArray());
    //
    //        AASKey[] arr = (AASKey[]) o;
    //        Assert.assertEquals(refKeys.size(), arr.length);
    //        Assert.assertArrayEquals(refKeys.toArray(), arr);
    //    }


    private static void checkQualifierList(List<Qualifier> listExpected, List<AASQualifier> listCurrent) {
        Assert.assertEquals(listExpected.size(), listCurrent.size());

        for (int i = 0; i < listExpected.size(); i++) {
            Qualifier exp = listExpected.get(i);
            AASQualifier curr = listCurrent.get(i);
            Assert.assertEquals("Qualifier Kind not equal", ValueConverter.convertQualifierKind(exp.getKind()), curr.getKind());
            Assert.assertEquals("Qualifier Type not equal", exp.getType(), curr.getType());
            Assert.assertEquals("Qualifier ValueType not equal", ValueConverter.convertDataTypeDefToString(exp.getValueType()), curr.getValueType());
            Assert.assertEquals("Qualifier Value not equal", exp.getValue(), curr.getValue());
        }
    }


    private static void checkSpecificAssetIdListNode(UaClient client, NodeId baseNode, int aasns, Map<String, String> map)
            throws ServiceException, ServiceResultException, AddressSpaceException, StatusException {
        //String name = AASAssetInformationType.SPECIFIC_ASSET_ID;
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, AASAssetInformationType.SPECIFIC_ASSET_ID)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(baseNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("checkSpecificAssetIdListNode Browse Result Null", bpres);
        Assert.assertEquals("checkSpecificAssetIdListNode Browse Result: size doesn't match", 1, bpres.length);

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull("checkSpecificAssetIdListNode Browse Target Node Null", targets);
        Assert.assertTrue("checkSpecificAssetIdListNode Browse targets empty", targets.length > 0);
        NodeId specificAssetIdNodeId = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        Assert.assertNotNull("checkSpecificAssetIdListNode Ref Node Null", specificAssetIdNodeId);
        Assert.assertNotEquals("checkSpecificAssetIdListNode Ref Node Null", NodeId.NULL, specificAssetIdNodeId);

        //checkType(client, listNode, new NodeId(aasns, TestConstants.AAS_SPECIFIC_ASSET_ID_LIST_TYPE_ID));
        //checkType(client, listNode, client.getAddressSpace().getNamespaceTable().toNodeId(DataTypeIds.AASSpecificAssetId));
        checkType(client, specificAssetIdNodeId, Identifiers.BaseDataVariableType);
        checkDatatype(client, specificAssetIdNodeId, TestConstants.SPECIFIC_ASSET_TYPE);

        UaVariable specificAssetIdVariable = (UaVariable) client.getAddressSpace().getNode(specificAssetIdNodeId);
        DataValue dv = specificAssetIdVariable.getValue();
        Assert.assertEquals(StatusCode.GOOD, dv.getStatusCode());
        Variant variant = dv.getValue();
        if (variant.isArray()) {
            Assert.assertTrue(variant.getValue() instanceof AASSpecificAssetId[]);
            AASSpecificAssetId[] arr = variant.asClass(AASSpecificAssetId[].class, null);
            //Assert.assertTrue(variant.getValue() instanceof Variant[]);
            //Variant[] vars = (Variant[]) variant.getValue();
            //Variant[] arr = variant.asClass(Variant[].class, null);
            //Assert.assertNotNull(arr);

            for (var spec: arr) {
                Assert.assertTrue(map.containsKey(spec.getName()));
                Assert.assertEquals(map.get(spec.getName()), spec.getValue());
            }
            //for (int index = 0; index < arr.length; index++) {
            //    //Assert.assertTrue(arr[index] instanceof AASSpecificAssetId);
            //}
        }
        else {
            AASSpecificAssetId value = variant.asClass(AASSpecificAssetId.class, null);
            Assert.assertNotNull(value);
            Assert.assertTrue(map.containsKey(value.getName()));
            Assert.assertEquals(map.get(value.getName()), value.getValue());
        }

        //List<NodeId> nodeList = new ArrayList<>();
        //List<ReferenceDescription> refs = client.getAddressSpace().browse(listNode);
        //for (ReferenceDescription ref: refs) {
        //    NodeId nid = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
        //    nodeList.add(nid);
        //}

        //for (NodeId node: nodeList) {
        //    checkSpecificAssetIdNode(client, node, aasns, map);
        //}
    }


    private static NodeId getType(UaClient client, NodeId nodeId) throws ServiceException, AddressSpaceException, ServiceResultException {
        UaNode uanode = client.getAddressSpace().getNode(nodeId);
        Assert.assertNotNull("getType UaNode Null", uanode);
        UaReference ref = uanode.getReference(Identifiers.HasTypeDefinition, false);
        Assert.assertNotNull("getType Reference Null", ref);

        return client.getAddressSpace().getNamespaceTable().toNodeId(ref.getTargetId());
    }


    private static void checkAasCommonAttributes(UaClient client, NodeId commonAttributesNodeId, CommonAttributesData data)
            throws ServiceResultException, ServiceException, AddressSpaceException {

        checkType(client, commonAttributesNodeId, Identifiers.BaseDataVariableType);
        checkDatatype(client, commonAttributesNodeId, TestConstants.AAS_COMMON_ATTRIBUTES_TYPE);

        Object value = getVariableValue(client, commonAttributesNodeId);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof AASAssetAdministrationShellCommonAttributes);
        AASAssetAdministrationShellCommonAttributes commonAttributesValue = (AASAssetAdministrationShellCommonAttributes) value;

        checkIdentifiable(commonAttributesValue.getIdentifiable(), data.id(), data.version(), data.revision());
        //checkIdentificationAas(client, commonAttributesNodeId, id);
    }


    private static void checkSubmodelCommonAttributes(UaClient client, NodeId commonAttributesNodeId, CommonAttributesData data)
            throws ServiceResultException, ServiceException, AddressSpaceException, StatusException {

        checkType(client, commonAttributesNodeId, Identifiers.BaseDataVariableType);
        checkDatatype(client, commonAttributesNodeId, TestConstants.SUBMODEL_COMMON_ATTRIBUTES_TYPE);

        Object value = getVariableValue(client, commonAttributesNodeId);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof AASSubmodelCommonAttributes);
        AASSubmodelCommonAttributes commonAttributesValue = (AASSubmodelCommonAttributes) value;

        AASIdentifiable ident = commonAttributesValue.getIdentifiable();
        checkIdentifiable(ident, data.id(), data.version(), data.revision());
        checkModelingKind(commonAttributesValue.getHasKind(), data.modelingKind());
        //checkIdentificationSubmodel(client, commonAttributesNodeId, id);

        // HasSemantics
        if (commonAttributesValue.getHasSemantics() == null) {
            Assert.assertNull(data.semanticId());
            Assert.assertNull(data.supplementalSemanticIds());
        }
        else {
            Assert.assertEquals(data.semanticId(), commonAttributesValue.getHasSemantics().getSemanticId());
            Assert.assertArrayEquals(data.supplementalSemanticIds(), commonAttributesValue.getHasSemantics().getSupplementalSemanticId());
        }
    }

    //    private static void checkIdentificationAas(UaClient client, NodeId commonAttributesNodeId, String id)
    //            throws ServiceResultException, ServiceException, AddressSpaceException {
    //
    //        Object value = getVariableValue(client, commonAttributesNodeId);
    //        Assert.assertTrue(value instanceof AASAssetAdministrationShellCommonAttributes);
    //        AASAssetAdministrationShellCommonAttributes commonAttributesValue = (AASAssetAdministrationShellCommonAttributes) value;
    //        checkIdentifiable(commonAttributesValue.getIdentifiable(), id);
    //    }

    //    private static void checkIdentificationSubmodel(UaClient client, NodeId commonAttributesNodeId, String id)
    //            throws ServiceResultException, ServiceException, AddressSpaceException {
    //
    //        Object value = getVariableValue(client, commonAttributesNodeId);
    //        Assert.assertTrue(value instanceof AASSubmodelCommonAttributes);
    //        AASSubmodelCommonAttributes commonAttributesValue = (AASSubmodelCommonAttributes) value;
    //        checkIdentifiable(commonAttributesValue.getIdentifiable(), id);
    //    }


    private static void checkReferable(AASReferable referable, String category) {
        if (referable == null) {
            Assert.assertNull(category);
        }
        else {
            Assert.assertEquals(category, referable.getCategory());
        }
    }


    private static void checkAdministration(AASIdentifiable identifiable, String version, String revision) {

        AASAdministrativeInformation adminInfo = identifiable.getAdministration();
        if (adminInfo == null) {
            Assert.assertNull(version);
            Assert.assertNull(revision);
        }
        else {
            Assert.assertEquals(version, adminInfo.getVersion());
            Assert.assertEquals(revision, adminInfo.getRevision());
        }
    }


    private static Object getVariableValue(UaClient client, NodeId nodeId) throws AddressSpaceException, ServiceException {
        UaNode uanode = client.getAddressSpace().getNode(nodeId);
        Assert.assertEquals(NodeClass.Variable, uanode.getNodeClass());
        BaseDataVariableType variableNode = (BaseDataVariableType) uanode;
        DataValue dv = variableNode.getValue();
        Assert.assertEquals(StatusCodes.Good, dv.getStatusCode().getValue());
        Assert.assertNotNull(dv.getValue());
        Object value = dv.getValue().getValue();
        Assert.assertNotNull(value);
        return value;
    }


    private static NodeId getCommonAttributes(UaClient client, NodeId baseNode, int aasns) throws ServiceResultException, ServiceException {
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(client.getAddressSpace().getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasCommonAttribute), false, true,
                new QualifiedName(aasns, TestConstants.COMMON_ATTRIBUTES)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(baseNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull(bpres);
        Assert.assertEquals(1, bpres.length);

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull(targets);
        Assert.assertTrue(targets.length > 0);
        NodeId commonAttrNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        return commonAttrNode;
    }


    private static NodeId getSubmodelElement(int aasns, String name, UaClient client, NodeId node) throws ServiceException, ServiceResultException {
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, name)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(node, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull(bpres);
        Assert.assertEquals(1, bpres.length);
        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull(targets);
        Assert.assertTrue(targets.length > 0);
        NodeId submodelElementNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        return submodelElementNode;
    }

    //    private static void checkSpecificAssetIdNode(UaClient client, NodeId node, int aasns, Map<String, String> map)
    //            throws ServiceException, AddressSpaceException, ServiceResultException, StatusException {
    //        checkType(client, node, new NodeId(aasns, TestConstants.AAS_SPECIFIC_ASSET_ID_TYPE_ID));
    //
    //        List<RelativePath> relPath = new ArrayList<>();
    //        List<RelativePathElement> browsePath = new ArrayList<>();
    //        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, AASSpecificAssetIdType.NAME)));
    //        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
    //        browsePath.clear();
    //        browsePath.add(new RelativePathElement(Identifiers.HasProperty, false, true, new QualifiedName(aasns, AASSpecificAssetIdType.VALUE)));
    //        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
    //
    //        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(node, relPath.toArray(RelativePath[]::new));
    //        Assert.assertNotNull("checkIdentifierKeyValuePairNode Browse Result Null", bpres);
    //        Assert.assertEquals("checkIdentifierKeyValuePairNode Browse Result: size doesn't match", 2, bpres.length);
    //
    //        // Name
    //        BrowsePathTarget[] targets = bpres[0].getTargets();
    //        Assert.assertNotNull("checkIdentifierKeyValuePairNode Browse Name Null", targets);
    //        Assert.assertTrue("checkIdentifierKeyValuePairNode Browse Name empty", targets.length > 0);
    //        DataValue dataValue = client.readValue(targets[0].getTargetId());
    //        Assert.assertEquals(StatusCode.GOOD, dataValue.getStatusCode());
    //        String key = dataValue.getValue().toString();
    //
    //        // Value
    //        targets = bpres[1].getTargets();
    //        Assert.assertNotNull("checkIdentifierKeyValuePairNode Browse Value Null", targets);
    //        Assert.assertTrue("checkIdentifierKeyValuePairNode Browse Value empty", targets.length > 0);
    //        dataValue = client.readValue(targets[0].getTargetId());
    //        Assert.assertEquals(StatusCode.GOOD, dataValue.getStatusCode());
    //        String value = dataValue.getValue().toString();
    //
    //        Assert.assertTrue("Key not found in Map", map.containsKey(key));
    //        Assert.assertEquals("Value not equal", map.get(key), value);
    //    }

    //private static boolean aasReferenceEquals(AASReference ref1, AASReference ref2) {
    //    return ref1.getType() == ref2.getType() && ref1.getReferredSemanticId() == ref2.getReferredSemanticId() && Arrays.equals(ref1.getKey(), ref2.getKey());
    //}
}
