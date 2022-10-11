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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager.NAMESPACE_URI;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager.NODE_NULL;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaBrowsePath;
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
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetInformation;
import io.adminshell.aas.v3.model.AssetKind;
import io.adminshell.aas.v3.model.File;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.Reference;
import java.util.List;
import opc.i4aas.AASAssetAdministrationShellType;
import opc.i4aas.AASAssetInformationType;
import opc.i4aas.AASIdentifierKeyValuePairList;
import opc.i4aas.AASReferenceList;
import opc.i4aas.server.AASAssetAdministrationShellTypeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create AssetAdministrationShells and integrate them into the
 * OPC UA address space.
 */
public class AssetAdministrationShellCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssetAdministrationShellCreator.class);

    /**
     * Adds the given AssetAdministrationShell.
     * 
     * @param node The UA node in which the IdentifierKeyValuePair should be created
     * @param aas The desirted AssetAdministrationShell.
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addAssetAdministrationShell(UaNode node, AssetAdministrationShell aas, AasServiceNodeManager nodeManager) throws StatusException {
        try {
            TypeDefinitionBasedNodeBuilderConfiguration.Builder conf = TypeDefinitionBasedNodeBuilderConfiguration.builder();
            Reference derivedFrom = aas.getDerivedFrom();
            if (derivedFrom != null) {
                UaBrowsePath bp = UaBrowsePath.from(opc.i4aas.ObjectTypeIds.AASAssetAdministrationShellType,
                        UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASAssetAdministrationShellType.getNamespaceUri(), AASAssetAdministrationShellType.DERIVED_FROM));
                conf.addOptional(bp);
            }

            nodeManager.setNodeBuilderConfiguration(conf.build());

            QualifiedName browseName = UaQualifiedName.from(NAMESPACE_URI, aas.getIdShort()).toQualifiedName(nodeManager.getNamespaceTable());
            String displayName = "AAS:" + aas.getIdShort();
            NodeId nid = new NodeId(nodeManager.getNamespaceIndex(), aas.getIdShort());
            if (nodeManager.findNode(nid) != null) {
                // The NodeId already exists
                nid = nodeManager.getDefaultNodeId();
            }

            AASAssetAdministrationShellType aasShell = nodeManager.createInstance(AASAssetAdministrationShellTypeNode.class, nid, browseName, LocalizedText.english(displayName));
            IdentifiableCreator.addIdentifiable(aasShell, aas.getIdentification(), aas.getAdministration(), aas.getCategory(), nodeManager);

            // DataSpecifications
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

            nodeManager.addReferable(AasUtils.toReference(aas), new ObjectData(aas, aasShell));
        }
        catch (Exception ex) {
            LOGGER.error("addAssetAdministrationShell Exception", ex);
            throw ex;
        }
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
            throws StatusException {
        if (aasNode == null) {
            throw new IllegalArgumentException("aasNode = null");
        }
        else if (assetInformation == null) {
            throw new IllegalArgumentException("assetInformation = null");
        }

        try {
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
                // AssetKind
                AssetKind assetKind = assetInformation.getAssetKind();
                assetInfoNode.setAssetKind(ValueConverter.convertAssetKind(assetKind));

                // BillOfMaterials
                List<Reference> assetBills = assetInformation.getBillOfMaterials();
                if ((assetBills != null) && (!assetBills.isEmpty())) {
                    AASReferenceList assetBillsNode = assetInfoNode.getBillOfMaterialNode();
                    addBillOfMaterials(assetBillsNode, assetBills, nodeManager);
                }

                // DefaultThumbnail
                File thumbnail = assetInformation.getDefaultThumbnail();
                if (thumbnail != null) {
                    FileCreator.addAasFile(assetInfoNode, thumbnail, null, null, false, AASAssetInformationType.DEFAULT_THUMBNAIL, nodeManager);
                }

                // GlobalAssetId
                Reference globalAssetId = assetInformation.getGlobalAssetId();
                if (globalAssetId != null) {
                    AasReferenceCreator.addAasReferenceAasNS(assetInfoNode, globalAssetId, AASAssetInformationType.GLOBAL_ASSET_ID, nodeManager);
                }

                // SpecificAssetIds
                List<IdentifierKeyValuePair> specificAssetIds = assetInformation.getSpecificAssetIds();
                if ((specificAssetIds != null) && (!specificAssetIds.isEmpty())) {
                    addSpecificAssetIds(assetInfoNode, specificAssetIds, "SpecificAssetIds", nodeManager);
                }

                if (created) {
                    aasNode.addComponent(assetInfoNode);
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error("addAssetInformation Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the list of BillOfMaterial objects to the given Node.
     *
     * @param node The desired node where the BillOfMaterials should be added
     * @param billOfMaterials The desired list of BillOfMaterials
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    private static void addBillOfMaterials(UaNode node, List<Reference> billOfMaterials, AasServiceNodeManager nodeManager) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(NODE_NULL);
        }
        else if (billOfMaterials == null) {
            throw new IllegalArgumentException("billOfMaterials = null");
        }

        try {
            AasReferenceCreator.addAasReferenceList(node, billOfMaterials, "BillOfMaterial", nodeManager);
        }
        catch (Exception ex) {
            LOGGER.error("addBillOfMaterials Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds a list of IdentifierKeyValuePairs to the given Node.
     *
     * @param assetInfoNode The AssetInformation node in which the
     *            IdentifierKeyValuePairs should be created or added
     * @param list The desired list of IdentifierKeyValuePairs
     * @param name The desired name of the Node
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    private static void addSpecificAssetIds(AASAssetInformationType assetInfoNode, List<IdentifierKeyValuePair> list, String name, AasServiceNodeManager nodeManager)
            throws StatusException {
        if (assetInfoNode == null) {
            throw new IllegalArgumentException("assetInfoNode = null");
        }
        else if (list == null) {
            throw new IllegalArgumentException("list = null");
        }

        try {
            LOGGER.debug("addSpecificAssetIds {}; to Node: {}", name, assetInfoNode);
            AASIdentifierKeyValuePairList listNode = assetInfoNode.getSpecificAssetIdNode();
            boolean created = false;

            if (listNode == null) {
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASIdentifierKeyValuePairList.getNamespaceUri(), name)
                        .toQualifiedName(nodeManager.getNamespaceTable());
                NodeId nid = nodeManager.createNodeId(assetInfoNode, browseName);
                listNode = nodeManager.createInstance(AASIdentifierKeyValuePairList.class, nid, browseName, LocalizedText.english(name));
                created = true;
            }

            for (IdentifierKeyValuePair ikv: list) {
                if (ikv != null) {
                    IdentifierKeyValuePairCreator.addIdentifierKeyValuePair(listNode, ikv, ikv.getKey(), nodeManager);
                }
            }

            if (created) {
                assetInfoNode.addComponent(listNode);
            }
        }
        catch (Exception ex) {
            LOGGER.error("addSpecificAssetIds Exception", ex);
            throw ex;
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
            throw new IllegalArgumentException(NODE_NULL);
        }
        else if (submodelRefs == null) {
            throw new IllegalArgumentException("sumodelRefs = null");
        }

        try {
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

                submodelNode = nodeManager.getSubmodelNode(ref);

                UaNode refNode = AasReferenceCreator.addAasReferenceAasNS(referenceListNode, ref, submodelName, nodeManager);

                if (refNode != null) {
                    // add hasAddIn reference to the submodel
                    if (submodelNode != null) {
                        refNode.addReference(submodelNode, Identifiers.HasAddIn, false);
                    }
                    else {
                        LOGGER.warn("addSubmodelReferences: Submodel {} not found in submodelRefMap", ref);
                    }
                }
            }

            if (added) {
                node.addComponent(referenceListNode);
            }
        }
        catch (Exception ex) {
            LOGGER.error("addSubmodelReferences Exception", ex);
            throw ex;
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
