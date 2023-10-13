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
import com.prosysopc.ua.stack.core.AccessLevelType;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.UaHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import java.util.List;
import opc.i4aas.AASSubmodelType;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create Submodels and integrate them into the
 * OPC UA address space.
 */
public class SubmodelCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubmodelCreator.class);

    private SubmodelCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Adds a submodel to a given Node
     *
     * @param node The desired Node where the submodel should be added
     * @param submodel The desired AAS submodel
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     */
    public static void addSubmodel(UaNode node, Submodel submodel, AasServiceNodeManager nodeManager)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException, ValueFormatException {
        if (submodel == null) {
            throw new IllegalArgumentException("submodel is null");
        }

        String shortId = submodel.getIdShort();
        if (!shortId.isEmpty()) {
            String displayName = "Submodel:" + shortId;
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASSubmodelType.getNamespaceUri(), shortId)
                    .toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.createNodeId(node, browseName);
            if (nodeManager.findNode(nid) != null) {
                // The NodeId already exists
                nid = nodeManager.getDefaultNodeId();
            }

            LOGGER.trace("addSubmodel: create Submodel {}; NodeId: {}", submodel.getIdShort(), nid);
            AASSubmodelType smNode = nodeManager.createInstance(AASSubmodelType.class, nid, browseName, LocalizedText.english(displayName));

            IdentifiableCreator.addIdentifiable(smNode, submodel.getId(), submodel.getAdministration(), submodel.getCategory(), nodeManager);

            // Kind
            if (submodel.getKind() != null) {
                if (smNode.getKindNode() == null) {
                    UaHelper.addKindProperty(smNode, nodeManager, AASSubmodelType.KIND, submodel.getKind(),
                            opc.i4aas.ObjectTypeIds.AASSubmodelType.getNamespaceUri());
                }
                else {
                    smNode.setKind(ValueConverter.convertModellingKind(submodel.getKind()));
                }
            }

            // DataSpecifications
            EmbeddedDataSpecificationCreator.addEmbeddedDataSpecifications(smNode, submodel.getEmbeddedDataSpecifications(), nodeManager);

            // Qualifiers
            List<Qualifier> qualifiers = submodel.getQualifiers();
            setQualifierData(qualifiers, smNode, nodeManager);

            // SemanticId
            if (submodel.getSemanticID() != null) {
                ConceptDescriptionCreator.addSemanticId(smNode, submodel.getSemanticID());
            }

            // Description
            DescriptionCreator.addDescriptions(smNode, submodel.getDescription());

            Reference refSubmodel = AasUtils.toReference(submodel);

            // SubmodelElements
            SubmodelElementCreator.addSubmodelElements(smNode, submodel.getSubmodelElements(), submodel, refSubmodel, nodeManager);

            if (AasServiceNodeManager.VALUES_READ_ONLY) {
                if (smNode.getKindNode() != null) {
                    smNode.getKindNode().setAccessLevel(AccessLevelType.CurrentRead);
                }
            }

            nodeManager.addSubmodelOpcUA(AasUtils.toReference(submodel), smNode);

            node.addComponent(smNode);

            nodeManager.addReferable(AasUtils.toReference(submodel), new ObjectData(submodel, smNode));
        }
        else {
            LOGGER.warn("addSubmodel: IdShort is empty!");
        }
    }


    private static void setQualifierData(List<Qualifier> qualifiers, AASSubmodelType smNode, AasServiceNodeManager nodeManager) throws StatusException {
        if ((qualifiers != null) && (!qualifiers.isEmpty())) {
            if (smNode.getQualifierNode() == null) {
                QualifierCreator.addQualifierNode(smNode, nodeManager);
            }

            QualifierCreator.addQualifiers(smNode.getQualifierNode(), qualifiers, nodeManager);
        }
    }

}
