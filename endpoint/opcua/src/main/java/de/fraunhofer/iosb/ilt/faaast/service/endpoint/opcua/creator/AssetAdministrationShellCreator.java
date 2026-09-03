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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaBrowseNamePath;
import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.ValueRanks;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.instantiation.TypeDefinitionBasedNodeBuilderConfiguration;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.types.opcua.BaseDataVariableType;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.UaHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.AmbiguousElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import java.util.List;
import opc.ua.aas.ObjectTypeIds;
import opc.ua.aas.datatypes.AASAssetAdministrationShellCommonAttributes;
import opc.ua.aas.datatypes.AASReference;
import opc.ua.aas.objecttypes.AASAssetAdministrationShellType;
import opc.ua.aas.objecttypes.AASAssetInformationType;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Resource;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create AssetAdministrationShells and integrate them into the OPC UA address space.
 */
public class AssetAdministrationShellCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetAdministrationShellCreator.class);

    private AssetAdministrationShellCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Adds the given AssetAdministrationShell.
     *
     * @param node The UA node in which the IdentifierKeyValuePair should be created
     * @param aas The desirted AssetAdministrationShell.
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     * @throws AmbiguousElementException if there are multiple matching elements in the environment
     * @throws ServiceResultException If the operation fails
     */
    public static void addAssetAdministrationShell(UaNode node, AssetAdministrationShell aas, AasServiceNodeManager nodeManager)
            throws StatusException, ValueFormatException, AmbiguousElementException, ServiceResultException {
        TypeDefinitionBasedNodeBuilderConfiguration.Builder conf = TypeDefinitionBasedNodeBuilderConfiguration.builder();
        Reference derivedFrom = aas.getDerivedFrom();
        if (derivedFrom != null) {
            UaBrowseNamePath bp = UaBrowseNamePath.from(ObjectTypeIds.AASAssetAdministrationShellType,
                    UaQualifiedName.from(ObjectTypeIds.AASAssetAdministrationShellType.getNamespaceUri(), AASAssetAdministrationShellType.DERIVED_FROM));
            conf.addOptional(bp);
        }

        nodeManager.setNodeBuilderConfiguration(conf.build());

        String shortId = aas.getIdShort();
        if ((shortId == null) || shortId.isEmpty()) {
            shortId = "AAS";
        }
        QualifiedName browseName = UaQualifiedName.from(AasServiceNodeManager.NAMESPACE_URI, shortId).toQualifiedName(nodeManager.getNamespaceTable());
        String displayName = "AAS:" + shortId;
        NodeId nid = new NodeId(nodeManager.getNamespaceIndex(), shortId);
        if (nodeManager.hasNode(nid)) {
            // The NodeId already exists
            nid = nodeManager.getDefaultNodeId();
        }

        AASAssetAdministrationShellType aasShell = nodeManager.createInstance(AASAssetAdministrationShellType.class, nid, browseName, LocalizedText.english(displayName));
        //if ((derivedFrom != null) && (aasShell.getDerivedFromNode() == null)) {
        //    LOGGER.info("addAssetAdministrationShell: DerivedFrom not created!");
        //}

        if (derivedFrom != null) {
            aasShell.setDerivedFrom(ReferenceCreator.getAasReference(derivedFrom));
        }

        if (aasShell.getCommonAttributes() == null) {
            aasShell.setCommonAttributes(new AASAssetAdministrationShellCommonAttributes());
        }
        //if (aasShell.getCommonAttributes().getIdentifiable() == null) {
        //    aasShell.getCommonAttributes().setIdentifiable(new AASIdentifiable());
        //}
        //IdentifiableCreator.addIdentifiable(aasShell.getCommonAttributes().getIdentifiable(), aas, nodeManager);

        aasShell.getCommonAttributes().setIdentifiable(BaseDataCreator.getIdentifiable(aas));

        // EmbeddedDataSpecifications
        HasDataSpecificationCreator.addHasDataSpecification(aasShell, aas, nodeManager);

        // AssetInformation
        AssetInformation assetInformation = aas.getAssetInformation();
        if (assetInformation != null) {
            addAssetInformation(aasShell, assetInformation, nodeManager);
        }

        // submodel references
        List<Reference> submodelRefs = aas.getSubmodels();
        if ((submodelRefs != null) && (!submodelRefs.isEmpty())) {
            addSubmodelReferences(aasShell, submodelRefs, nodeManager);
        }

        // add AAS to Environment
        nodeManager.addNodeAndReference(node, aasShell, Identifiers.Organizes);

        nodeManager.addReferable(EnvironmentHelper.asReference(aas, nodeManager.getEnvironment()), new ObjectData(aas, aasShell));
    }


    private static void addAssetInformation(AASAssetAdministrationShellType aasNode, AssetInformation assetInformation, AasServiceNodeManager nodeManager)
            throws StatusException, ValueFormatException, ServiceResultException {
        if (aasNode == null) {
            throw new IllegalArgumentException("aasNode = null");
        }
        else if (assetInformation == null) {
            throw new IllegalArgumentException("assetInformation = null");
        }

        boolean created = false;
        AASAssetInformationType assetInfoNode;
        assetInfoNode = aasNode.getAssetInformationNode();
        if (assetInfoNode == null) {
            String displayName = "AssetInformation";
            QualifiedName browseName = UaQualifiedName.from(ObjectTypeIds.AASSubmodelType.getNamespaceUri(), displayName)
                    .toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.createNodeId(aasNode, browseName);
            assetInfoNode = nodeManager.createInstance(AASAssetInformationType.class, nid, browseName, LocalizedText.english(displayName));
            created = true;
        }

        if (assetInfoNode != null) {
            setAssetInformationData(assetInformation, assetInfoNode, nodeManager);

            if (created) {
                aasNode.addComponent(assetInfoNode);
            }
        }
    }


    private static void setAssetInformationData(AssetInformation assetInformation, AASAssetInformationType assetInfoNode, AasServiceNodeManager nodeManager)
            throws StatusException, ValueFormatException, ServiceResultException {
        // AssetKind
        AssetKind assetKind = assetInformation.getAssetKind();
        assetInfoNode.setAssetKind(ValueConverter.convertAssetKind(assetKind));

        // AssetType 
        String assetType = assetInformation.getAssetType();
        if (assetType != null) {
            if (assetInfoNode.getAssetTypeNode() == null) {
                UaHelper.addStringUaProperty(assetInfoNode, nodeManager, AASAssetInformationType.ASSET_TYPE, assetType,
                        ObjectTypeIds.AASAssetInformationType.getNamespaceUri());
            }
            else {
                assetInfoNode.setAssetType(assetType);
            }
        }

        // DefaultThumbnail
        Resource thumbnail = assetInformation.getDefaultThumbnail();
        if (thumbnail != null) {
            ResourceCreator.addAasResource(assetInfoNode, thumbnail, AASAssetInformationType.DEFAULT_THUMBNAIL, nodeManager);
        }

        // GlobalAssetId
        String globalAssetId = assetInformation.getGlobalAssetId();
        if (globalAssetId != null) {
            if (assetInfoNode.getGlobalAssetIdNode() == null) {
                UaHelper.addStringUaProperty(assetInfoNode, nodeManager, AASAssetInformationType.GLOBAL_ASSET_ID, globalAssetId,
                        ObjectTypeIds.AASAssetInformationType.getNamespaceUri());
            }
            else {
                assetInfoNode.setGlobalAssetId(globalAssetId);
            }
        }

        // SpecificAssetIds
        List<SpecificAssetId> specificAssetIds = assetInformation.getSpecificAssetIds();
        if ((specificAssetIds != null) && (!specificAssetIds.isEmpty())) {
            addSpecificAssetIds(assetInfoNode, specificAssetIds, nodeManager);
        }
    }


    private static void addSpecificAssetIds(AASAssetInformationType assetInfoNode, List<SpecificAssetId> list, AasServiceNodeManager nodeManager)
            throws StatusException, ServiceResultException {
        if (assetInfoNode == null) {
            throw new IllegalArgumentException("assetInfoNode = null");
        }
        else if (list == null) {
            throw new IllegalArgumentException("list = null");
        }

        String name = AASAssetInformationType.SPECIFIC_ASSET_ID;
        LOGGER.debug("addSpecificAssetIds {}; to Node: {}", name, assetInfoNode);
        BaseDataVariableType listNode = assetInfoNode.getSpecificAssetIdNode();
        boolean created = false;

        if (listNode == null) {
            QualifiedName browseName = UaQualifiedName.from(ObjectTypeIds.AASAssetInformationType.getNamespaceUri(), name)
                    .toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.createNodeId(assetInfoNode, browseName);
            listNode = nodeManager.createInstance(BaseDataVariableType.class, nid, browseName, LocalizedText.english(name));
            created = true;
        }

        SpecificAssetIdCreator.addSpecificAssetIdList(listNode, list, nodeManager);

        if (created) {
            assetInfoNode.addComponent(listNode);
        }
    }


    private static void addSubmodelReferences(AASAssetAdministrationShellType node, List<Reference> submodelRefs, AasServiceNodeManager nodeManager) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(AasServiceNodeManager.NODE_NULL);
        }
        else if (submodelRefs == null) {
            throw new IllegalArgumentException("sumodelRefs = null");
        }

        String name = AASAssetAdministrationShellType.SUBMODEL;
        BaseDataVariableType referenceListNode = node.getSubmodelNode();
        LOGGER.debug("addSubmodelReferences: add {} Submodels to Node: {}", submodelRefs.size(), node);
        boolean added = false;
        if (referenceListNode == null) {
            QualifiedName browseName = UaQualifiedName.from(ObjectTypeIds.AASAssetAdministrationShellType.getNamespaceUri(), name)
                    .toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.createNodeId(node, browseName);
            referenceListNode = nodeManager.createInstance(BaseDataVariableType.class, nid, browseName, LocalizedText.english(name));
            LOGGER.debug("addSubmodelReferences: add Node {} to Node {}", referenceListNode.getNodeId(), node.getNodeId());
            added = true;
        }

        //int counter = 1;
        List<AASReference> refList = ReferenceCreator.getAasReferences(submodelRefs);
        //for (Reference ref: submodelRefs) {
        //    AASReference refNode = AasReferenceCreator.getAasReference(ref);
        //    refList.add(refNode);

        //            UaNode submodelNode = null;
        //            String submodelName = getSubmodelName(ref);
        //            if (submodelName.isEmpty()) {
        //                submodelName = name + counter++;
        //            }
        //
        //            // change reference to model reference here
        //            if (ref != null) {
        //                ref.setType(ReferenceTypes.MODEL_REFERENCE);
        //            }
        //            submodelNode = nodeManager.getSubmodelNode(ref);
        //
        //            UaNode refNode = AasReferenceCreator.addAasReferenceAasNS(referenceListNode, ref, submodelName, nodeManager);
        //
        //            if (refNode != null) {
        //                // add hasAddIn reference to the submodel
        //                if (submodelNode != null) {
        //                    refNode.addReference(submodelNode, Identifiers.HasAddIn, false);
        //                }
        //                else if (LOGGER.isWarnEnabled()) {
        //                    LOGGER.warn("addSubmodelReferences: Submodel {} not found in submodelRefMap", ReferenceHelper.toString(ref));
        //                }
        //            }
        //}

        if (refList.size() == 1) {
            referenceListNode.setValueRank(ValueRanks.Scalar);
            referenceListNode.setValue(refList.get(0));
        }
        else if (refList.size() > 1) {
            referenceListNode.setValueRank(ValueRanks.OneDimension);
            referenceListNode.setArrayDimensions(new UnsignedInteger[] {
                    UnsignedInteger.ZERO
            });
            referenceListNode.setValue(refList.toArray(AASReference[]::new));
        }

        if (added) {
            node.addComponent(referenceListNode);
        }
    }

    /**
     * Extracts the name from the given Submodel Reference.
     *
     * @param submodelRef The submodel reference
     * @return The Name of the Submodel
     */
    //private static String getSubmodelName(Reference submodelRef) {
    //    String retval = "";
    //    if ((submodelRef != null) && (!submodelRef.getKeys().isEmpty())) {
    //        retval = submodelRef.getKeys().get(0).getValue();
    //    }

    //    return retval;
    //}

}
