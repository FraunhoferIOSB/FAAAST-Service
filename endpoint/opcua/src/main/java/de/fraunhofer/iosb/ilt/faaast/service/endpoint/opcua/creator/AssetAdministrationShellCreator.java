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
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.instantiation.TypeDefinitionBasedNodeBuilderConfiguration;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.UaHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.AmbiguousElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.List;
import opc.i4aas.objecttypes.AASAssetAdministrationShellType;
import opc.i4aas.objecttypes.AASAssetInformationType;
import opc.i4aas.objecttypes.AASReferenceList;
import opc.i4aas.objecttypes.AASSpecificAssetIdList;
import opc.i4aas.objecttypes.server.AASAssetAdministrationShellTypeNode;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
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
     */
    public static void addAssetAdministrationShell(UaNode node, AssetAdministrationShell aas, AasServiceNodeManager nodeManager)
            throws StatusException, ValueFormatException, AmbiguousElementException {
        TypeDefinitionBasedNodeBuilderConfiguration.Builder conf = TypeDefinitionBasedNodeBuilderConfiguration.builder();
        Reference derivedFrom = aas.getDerivedFrom();
        if (derivedFrom != null) {
            UaBrowseNamePath bp = UaBrowseNamePath.from(opc.i4aas.ObjectTypeIds.AASAssetAdministrationShellType,
                    UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASAssetAdministrationShellType.getNamespaceUri(), AASAssetAdministrationShellType.DERIVED_FROM));
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

        AASAssetAdministrationShellType aasShell = nodeManager.createInstance(AASAssetAdministrationShellTypeNode.class, nid, browseName, LocalizedText.english(displayName));
        IdentifiableCreator.addIdentifiable(aasShell, aas.getId(), aas.getAdministration(), aas.getCategory(), nodeManager);

        // EmbeddedDataSpecifications
        EmbeddedDataSpecificationCreator.addEmbeddedDataSpecifications(aasShell, aas.getEmbeddedDataSpecifications(), nodeManager);

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


    /**
     * Adds an AssetInformation object to the given Node.
     *
     * @param aasNode The AAS node where the AssetInformation should be added
     * @param assetInformation The desired AssetInformation object
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    private static void addAssetInformation(AASAssetAdministrationShellType aasNode, AssetInformation assetInformation, AasServiceNodeManager nodeManager)
            throws StatusException, ValueFormatException {
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
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASSubmodelType.getNamespaceUri(), displayName)
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
            throws StatusException, ValueFormatException {
        // AssetKind
        AssetKind assetKind = assetInformation.getAssetKind();
        assetInfoNode.setAssetKind(ValueConverter.convertAssetKind(assetKind));

        // AssetType 
        String assetType = assetInformation.getAssetType();
        if (assetType != null) {
            if (assetInfoNode.getAssetTypeNode() == null) {
                UaHelper.addStringUaProperty(assetInfoNode, nodeManager, AASAssetInformationType.ASSET_TYPE, assetType,
                        opc.i4aas.ObjectTypeIds.AASAssetInformationType.getNamespaceUri());
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
                        opc.i4aas.ObjectTypeIds.AASAssetInformationType.getNamespaceUri());
            }
            else {
                assetInfoNode.setGlobalAssetId(globalAssetId);
            }
        }

        // SpecificAssetIds
        List<SpecificAssetId> specificAssetIds = assetInformation.getSpecificAssetIds();
        if ((specificAssetIds != null) && (!specificAssetIds.isEmpty())) {
            addSpecificAssetIds(assetInfoNode, specificAssetIds, "SpecificAssetIds", nodeManager);
        }
    }


    /**
     * Adds a list of IdentifierKeyValuePairs to the given Node.
     *
     * @param assetInfoNode The AssetInformation node in which the IdentifierKeyValuePairs should be created or added
     * @param list The desired list of IdentifierKeyValuePairs
     * @param name The desired name of the Node
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    private static void addSpecificAssetIds(AASAssetInformationType assetInfoNode, List<SpecificAssetId> list, String name, AasServiceNodeManager nodeManager)
            throws StatusException {
        if (assetInfoNode == null) {
            throw new IllegalArgumentException("assetInfoNode = null");
        }
        else if (list == null) {
            throw new IllegalArgumentException("list = null");
        }

        LOGGER.debug("addSpecificAssetIds {}; to Node: {}", name, assetInfoNode);
        AASSpecificAssetIdList listNode = assetInfoNode.getSpecificAssetIdNode();
        boolean created = false;

        if (listNode == null) {
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASSpecificAssetIdList.getNamespaceUri(), name)
                    .toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.createNodeId(assetInfoNode, browseName);
            listNode = nodeManager.createInstance(AASSpecificAssetIdList.class, nid, browseName, LocalizedText.english(name));
            created = true;
        }

        SpecificAssetIdCreator.addSpecificAssetIdList(listNode, list, nodeManager);

        if (created) {
            assetInfoNode.addComponent(listNode);
        }
    }


    /**
     * Adds the given submodel references to the given node.
     *
     * @param node The desired UA node in which the objects should be created
     * @param submodelRefs The desired submodel references
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    private static void addSubmodelReferences(AASAssetAdministrationShellType node, List<Reference> submodelRefs, AasServiceNodeManager nodeManager) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(AasServiceNodeManager.NODE_NULL);
        }
        else if (submodelRefs == null) {
            throw new IllegalArgumentException("sumodelRefs = null");
        }

        String name = "Submodel";
        AASReferenceList referenceListNode = node.getSubmodelNode();
        LOGGER.debug("addSubmodelReferences: add {} Submodels to Node: {}", submodelRefs.size(), node);
        boolean added = false;
        if (referenceListNode == null) {
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASReferenceList.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.createNodeId(node, browseName);
            referenceListNode = nodeManager.createInstance(AASReferenceList.class, nid, browseName, LocalizedText.english(name));
            LOGGER.debug("addSubmodelReferences: add Node {} to Node {}", referenceListNode.getNodeId(), node.getNodeId());
            added = true;
        }

        int counter = 1;
        for (Reference ref: submodelRefs) {
            UaNode submodelNode = null;
            String submodelName = getSubmodelName(ref);
            if (submodelName.isEmpty()) {
                submodelName = name + counter++;
            }

            // change reference to model reference here
            if (ref != null) {
                ref.setType(ReferenceTypes.MODEL_REFERENCE);
            }
            submodelNode = nodeManager.getSubmodelNode(ref);

            UaNode refNode = AasReferenceCreator.addAasReferenceAasNS(referenceListNode, ref, submodelName, nodeManager);

            if (refNode != null) {
                // add hasAddIn reference to the submodel
                if (submodelNode != null) {
                    refNode.addReference(submodelNode, Identifiers.HasAddIn, false);
                }
                else if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("addSubmodelReferences: Submodel {} not found in submodelRefMap", ReferenceHelper.toString(ref));
                }
            }
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
    private static String getSubmodelName(Reference submodelRef) {
        String retval = "";
        if ((submodelRef != null) && (!submodelRef.getKeys().isEmpty())) {
            retval = submodelRef.getKeys().get(0).getValue();
        }

        return retval;
    }

}
