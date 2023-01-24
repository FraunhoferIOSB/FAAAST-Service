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

import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import opc.i4aas.AASEntityType;


/**
 * Helper class to create Entities and integrate them into the
 * OPC UA address space.
 */
public class EntityCreator extends SubmodelElementCreator {

    /**
     * Adds an AAS entity to the given node.
     *
     * @param node The desired UA node
     * @param aasEntity The AAS entity to add
     * @param submodel The corresponding Submodel
     * @param parentRef The AAS reference to the parent object
     * @param ordered Specifies whether the entity should be added ordered
     *            (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    public static void addAasEntity(UaNode node, Entity aasEntity, Submodel submodel, Reference parentRef, boolean ordered, AasServiceNodeManager nodeManager)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        if ((node != null) && (aasEntity != null)) {
            String name = aasEntity.getIdShort();
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASEntityType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.getDefaultNodeId();
            AASEntityType entityNode = nodeManager.createInstance(AASEntityType.class, nid, browseName, LocalizedText.english(name));
            addSubmodelElementBaseData(entityNode, aasEntity, nodeManager);

            Reference entityRef = AasUtils.toReference(parentRef, aasEntity);

            // EntityType
            entityNode.setEntityType(ValueConverter.getAasEntityType(aasEntity.getEntityType()));

            nodeManager.addSubmodelElementAasMap(entityNode.getEntityTypeNode().getNodeId(),
                    new SubmodelElementData(aasEntity, submodel, SubmodelElementData.Type.ENTITY_TYPE, entityRef));

            // GlobalAssetId
            if (aasEntity.getGlobalAssetId() != null) {
                setGlobalAssetIdData(entityNode, aasEntity, nodeManager, submodel, entityRef);
            }

            // SpecificAssetIds
            IdentifierKeyValuePair specificAssetId = aasEntity.getSpecificAssetId();
            if (specificAssetId != null) {
                setSpecificAssetIdData(entityNode, specificAssetId, nodeManager);
            }

            // Statements
            SubmodelElementCreator.addSubmodelElements(entityNode.getStatementNode(), aasEntity.getStatements(), submodel, entityRef, nodeManager);

            nodeManager.addSubmodelElementOpcUA(entityRef, entityNode);

            if (ordered) {
                node.addReference(entityNode, Identifiers.HasOrderedComponent, false);
            }
            else {
                node.addComponent(entityNode);
            }

            nodeManager.addReferable(entityRef, new ObjectData(aasEntity, entityNode, submodel));
        }
    }


    private static void setSpecificAssetIdData(AASEntityType entityNode, IdentifierKeyValuePair specificAssetId, AasServiceNodeManager nodeManager) throws StatusException {
        if (entityNode.getSpecificAssetIdNode() == null) {
            IdentifierKeyValuePairCreator.addIdentifierKeyValuePair(entityNode, specificAssetId, AASEntityType.SPECIFIC_ASSET_ID, nodeManager);
        }
        else {
            IdentifierKeyValuePairCreator.setIdentifierKeyValuePairData(entityNode.getSpecificAssetIdNode(), specificAssetId, nodeManager);
        }
    }


    private static void setGlobalAssetIdData(AASEntityType entityNode, Entity aasEntity, AasServiceNodeManager nodeManager, Submodel submodel, Reference entityRef)
            throws StatusException {
        if (entityNode.getGlobalAssetIdNode() == null) {
            AasReferenceCreator.addAasReferenceAasNS(entityNode, aasEntity.getGlobalAssetId(), AASEntityType.GLOBAL_ASSET_ID, false, nodeManager);
        }
        else {
            AasSubmodelElementHelper.setAasReferenceData(aasEntity.getGlobalAssetId(), entityNode.getGlobalAssetIdNode(), false);
        }

        nodeManager.addSubmodelElementAasMap(entityNode.getGlobalAssetIdNode().getKeysNode().getNodeId(),
                new SubmodelElementData(aasEntity, submodel, SubmodelElementData.Type.ENTITY_GLOBAL_ASSET_ID, entityRef));
    }

}
