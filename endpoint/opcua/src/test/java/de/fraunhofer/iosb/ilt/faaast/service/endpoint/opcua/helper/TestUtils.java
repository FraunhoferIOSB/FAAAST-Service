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

import com.prosysopc.ua.ApplicationIdentity;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import opc.ua.aas.Ids;
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
import opc.ua.iosb.aas.datatypes.AASDirection;
import opc.ua.iosb.aas.datatypes.AASStateOfEvent;
import org.awaitility.Awaitility;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.junit.Assert;


/**
 * Test utilities
 */
public class TestUtils {

    private static final Duration POLL_TIMEOUT = Duration.ofMillis(100);
    private static final Duration MAX_TIMEOUT = Duration.ofSeconds(5);

    public static void initialize(UaClient client) {
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


    public static void checkQualifier(AASQualifiable qualifiable, List<Qualifier> qualifierList) {
        if ((qualifierList == null) || qualifierList.isEmpty()) {
            Assert.assertNull(qualifiable);
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
        Assert.assertNotNull(bpres);
        Assert.assertEquals(1, bpres.length);

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull(targets);
        Assert.assertTrue(targets.length > 0);
        NodeId assetInfoNode = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());

        Assert.assertNotNull(assetInfoNode);
        Assert.assertNotEquals(NodeId.NULL, assetInfoNode);

        checkType(client, assetInfoNode, TestConstants.AAS_ASSET_INFO_TYPE_ID);
        checkAssetKindNode(client, assetInfoNode, aasns, AASAssetKind.of(AASAssetKind.Options.Instance));
        checkAasPropertyThumbnail(client, assetInfoNode, aasns, TestConstants.DEFAULT_THUMB_NAME, "image/png",
                "file:///master/verwaltungsschale-detail-part1.png");

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
            throws ServiceException, AddressSpaceException, ServiceResultException {
        NodeId propertyNode = getSubmodelElement(aasns, name, client, node);

        checkType(client, propertyNode, TestConstants.AAS_PROPERTY_TYPE_ID);
        checkDisplayName(client, propertyNode, name);

        checkSubmodelElementCommonAttributes(client, aasns, propertyNode, category, qualifierList);

        UaVariable varNode = (UaVariable) client.getAddressSpace().getNode(propertyNode);
        NodeId datatypeNode = varNode.getDataTypeId();
        Assert.assertEquals(ValueConverter.convertDataTypeToNodeId(valueType, client), datatypeNode);

        DataValue value = varNode.getValue();

        Variant variant = new Variant(propValue);
        Assert.assertEquals(variant, value.getValue());
    }


    public static void checkAasPropertyObject(UaClient client, NodeId node, int aasns, String name, String category, Datatype valueType,
                                              Object propValue, List<Qualifier> qualifierList)
            throws ServiceException, AddressSpaceException, ServiceResultException {
        NodeId propertyNode = getSubmodelElement(aasns, name, client, node);

        checkType(client, propertyNode, TestConstants.AAS_PROPERTY_TYPE_ID);
        checkDisplayName(client, propertyNode, name);
        checkSubmodelElementCommonAttributes(client, aasns, propertyNode, category, qualifierList);
        //checkEmbeddedDataSpecificationNode(client, propertyNode, aasns);

        UaVariable varNode = (UaVariable) client.getAddressSpace().getNode(propertyNode);
        NodeId datatypeNode = varNode.getDataTypeId();
        Assert.assertEquals(ValueConverter.convertDataTypeToNodeId(valueType, client), datatypeNode);

        DataValue value = varNode.getValue();

        Variant variant = new Variant(propValue);
        Assert.assertEquals(variant, value.getValue());
    }


    public static void checkAasPropertyFile(UaClient client, NodeId node, int aasns, String name, String category, String mimeType, String propPath)
            throws ServiceException, ServiceResultException, AddressSpaceException, StatusException {
        NodeId propertyNode = getSubmodelElement(aasns, name, client, node);

        checkType(client, propertyNode, TestConstants.AAS_FILE_TYPE_ID);
        checkDisplayName(client, propertyNode, name);
        checkSubmodelElementCommonAttributes(client, aasns, propertyNode, category, null);

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HasComponent, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_CONTENT_TYPE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
        browsePath.clear();
        browsePath.add(new RelativePathElement(Identifiers.HasComponent, false, true, new QualifiedName(aasns, TestConstants.PROPERTY_VALUE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
        browsePath.clear();

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(propertyNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull(bpres);
        Assert.assertEquals(2, bpres.length);

        // ContentType
        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull(targets);
        Assert.assertTrue(targets.length > 0);
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


    public static void checkSubmodelRefs(UaClient client, NodeId baseNode, List<AASReference> submodelRefs)
            throws ServiceException, AddressSpaceException {

        Object value = getVariableValue(client, baseNode);
        Assert.assertTrue(value instanceof AASReference[]);
        AASReference[] vars = (AASReference[]) value;
        Assert.assertEquals(submodelRefs.size(), vars.length);
        for (int index = 0; index < vars.length; index++) {
            Assert.assertEquals((AASReference) vars[index], submodelRefs.get(index));
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
                    //if (val.getStatusCode().isGood()) {
                    //    Object v = val.getValue().getValue();
                    //    LOGGER.info("writeNewValueIntern: val: {}; old: {}; new: {}", v, oldValue, newValue);
                    //}
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
                    return val.getStatusCode().isGood() && (val.getValue() != null) && Objects.equals((AASReference) val.getValue().getValue(), newValue);
                });
    }


    public static void checkCommonAttributes(UaClient client, NodeId baseNode, int aasns, CommonAttributesData data)
            throws ServiceException, AddressSpaceException, ServiceResultException {

        NodeId commonAttrNode = getCommonAttributes(client, baseNode, aasns);

        NodeId type = getType(client, commonAttrNode);
        if (client.getNamespaceTable().nodeIdEquals(type, Ids.AASSubmodelCommonAttributes)) {
            checkSubmodelCommonAttributes(client, commonAttrNode, data);
        }
        else if (client.getNamespaceTable().nodeIdEquals(type, Ids.AASAssetAdministrationShellCommonAttributes)) {
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


    public static void checkAasPropertyThumbnail(UaClient client, NodeId node, int aasns, String name, String mimeType,
                                                 String propPath)
            throws ServiceException, ServiceResultException, AddressSpaceException, StatusException {
        NodeId propertyNode = getSubmodelElement(aasns, name, client, node);

        checkType(client, propertyNode, TestConstants.RESOURCE_TYPE);
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

        Variant variant = new Variant(propValue);
        Assert.assertEquals(variant, value.getValue());
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

        Variant variant = value.getValue();
        Assert.assertTrue(variant.getValue() instanceof AASEmbeddedConceptDescription);
        AASEmbeddedConceptDescription cd = (AASEmbeddedConceptDescription) variant.getValue();

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


    public static void checkBasicEvent(UaClient client, NodeId submodelNode, int aasns, int iltns, String name, String category, AASDirection direction, AASStateOfEvent state,
                                       AASReference observed)
            throws ServiceException, ServiceResultException, AddressSpaceException, StatusException {
        NodeId eventNode = getSubmodelElement(iltns, name, client, submodelNode);

        checkType(client, eventNode, TestConstants.BASIC_EVENT_TYPE);
        checkDisplayName(client, eventNode, name);

        checkSubmodelElementCommonAttributes(client, aasns, eventNode, category, null);

        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(client.getAddressSpace().getNamespaceTable().toNodeId(Ids.AASHasAttribute), false, true,
                new QualifiedName(iltns, TestConstants.EVENT_DIRECTION)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
        browsePath.clear();
        browsePath.add(new RelativePathElement(client.getAddressSpace().getNamespaceTable().toNodeId(Ids.AASHasAttribute), false, true,
                new QualifiedName(iltns, TestConstants.EVENT_STATE)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));
        browsePath.clear();
        browsePath.add(new RelativePathElement(client.getAddressSpace().getNamespaceTable().toNodeId(Ids.AASHasAttribute), false, true,
                new QualifiedName(iltns, TestConstants.EVENT_OBSERVED)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(eventNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull(bpres);
        Assert.assertEquals(3, bpres.length);

        // Direction
        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull(targets);
        Assert.assertTrue(targets.length > 0);
        DataValue value = client.readValue(targets[0].getTargetId());
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Assert.assertNotNull(value.getValue());
        Assert.assertEquals(direction, value.getValue().asOptionSet(AASDirection.SPECIFICATION));

        // State
        targets = bpres[1].getTargets();
        Assert.assertNotNull(targets);
        Assert.assertTrue(targets.length > 0);
        value = client.readValue(targets[0].getTargetId());
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Assert.assertNotNull(value.getValue());
        Assert.assertEquals(state, value.getValue().asOptionSet(AASStateOfEvent.SPECIFICATION));
    }


    private static NodeId getConceptDescription(UaClient client, NodeId baseNode, int aasns) throws ServiceResultException, ServiceException {
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(client.getAddressSpace().getNamespaceTable().toNodeId(Ids.AASHasConceptDescription), false, true,
                new QualifiedName(aasns, TestConstants.CONCEPT_DESCRIPTION_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(baseNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull(bpres);
        Assert.assertEquals(1, bpres.length);

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull(targets);
        Assert.assertTrue(targets.length > 0);
        return client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
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


    private static void checkModelingKind(AASHasKind kindNode, AASModellingKind modelingKind) {
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
        Assert.assertNotNull(bpres);
        Assert.assertEquals(1, bpres.length);

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull(targets);
        Assert.assertTrue(targets.length > 0);

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
            throws ServiceException, ServiceResultException, AddressSpaceException {
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, AASAssetInformationType.SPECIFIC_ASSET_ID)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(baseNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull(bpres);
        Assert.assertEquals(1, bpres.length);

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull(targets);
        Assert.assertTrue(targets.length > 0);
        NodeId specificAssetIdNodeId = client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
        Assert.assertNotNull(specificAssetIdNodeId);
        Assert.assertNotEquals(NodeId.NULL, specificAssetIdNodeId);

        checkType(client, specificAssetIdNodeId, Identifiers.BaseDataVariableType);
        checkDatatype(client, specificAssetIdNodeId, TestConstants.SPECIFIC_ASSET_TYPE);

        UaVariable specificAssetIdVariable = (UaVariable) client.getAddressSpace().getNode(specificAssetIdNodeId);
        DataValue dv = specificAssetIdVariable.getValue();
        Assert.assertEquals(StatusCode.GOOD, dv.getStatusCode());
        Variant variant = dv.getValue();
        if (variant.isArray()) {
            Assert.assertTrue(variant.getValue() instanceof AASSpecificAssetId[]);
            AASSpecificAssetId[] arr = variant.asClass(AASSpecificAssetId[].class, null);

            for (var spec: arr) {
                Assert.assertTrue(map.containsKey(spec.getName()));
                Assert.assertEquals(map.get(spec.getName()), spec.getValue());
            }
        }
        else {
            AASSpecificAssetId value = variant.asClass(AASSpecificAssetId.class, null);
            Assert.assertNotNull(value);
            Assert.assertTrue(map.containsKey(value.getName()));
            Assert.assertEquals(map.get(value.getName()), value.getValue());
        }
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
    }


    private static void checkSubmodelCommonAttributes(UaClient client, NodeId commonAttributesNodeId, CommonAttributesData data)
            throws ServiceResultException, ServiceException, AddressSpaceException {

        checkType(client, commonAttributesNodeId, Identifiers.BaseDataVariableType);
        checkDatatype(client, commonAttributesNodeId, TestConstants.SUBMODEL_COMMON_ATTRIBUTES_TYPE);

        Object value = getVariableValue(client, commonAttributesNodeId);
        Assert.assertNotNull(value);
        Assert.assertTrue(value instanceof AASSubmodelCommonAttributes);
        AASSubmodelCommonAttributes commonAttributesValue = (AASSubmodelCommonAttributes) value;

        AASIdentifiable ident = commonAttributesValue.getIdentifiable();
        checkIdentifiable(ident, data.id(), data.version(), data.revision());
        checkModelingKind(commonAttributesValue.getHasKind(), data.modelingKind());

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
        browsePath.add(new RelativePathElement(client.getAddressSpace().getNamespaceTable().toNodeId(Ids.AASHasCommonAttribute), false, true,
                new QualifiedName(aasns, TestConstants.COMMON_ATTRIBUTES)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(baseNode, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull(bpres);
        Assert.assertEquals(1, bpres.length);

        BrowsePathTarget[] targets = bpres[0].getTargets();
        Assert.assertNotNull(targets);
        Assert.assertTrue(targets.length > 0);
        return client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
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
        return client.getAddressSpace().getNamespaceTable().toNodeId(targets[0].getTargetId());
    }

}
