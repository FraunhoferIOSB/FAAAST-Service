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
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import java.util.ArrayList;
import java.util.List;
import opc.i4aas.AASAssetAdministrationShellType;
import opc.i4aas.AASAssetType;
import opc.i4aas.AASConceptDescriptionType;
import opc.i4aas.AASDataSpecificationContentType;
import opc.i4aas.AASEmbeddedDataSpecificationList;
import opc.i4aas.AASEmbeddedDataSpecificationType;
import opc.i4aas.AASReferenceList;
import opc.i4aas.AASReferenceType;
import opc.i4aas.AASSubmodelElementType;
import opc.i4aas.AASSubmodelType;
import org.eclipse.digitaltwin.aas4j.v3.model.DataSpecificationContent;
import org.eclipse.digitaltwin.aas4j.v3.model.EmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Helper class to create EmbeddedDataSpecifications and integrate them into
 * the OPC UA address space.
 */
public class EmbeddedDataSpecificationCreator {

    private EmbeddedDataSpecificationCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Adds the given Embedded Data Specifications to the desired node.
     *
     * @param aasNode The desired object where the DataSpecifications should be added
     * @param list The list of the desired Data Specifications
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addEmbeddedDataSpecifications(AASAssetAdministrationShellType aasNode, List<EmbeddedDataSpecification> list, AasServiceNodeManager nodeManager)
            throws StatusException {
        if ((list != null) && (!list.isEmpty())) {
            List<Reference> refList = new ArrayList<>();
            for (EmbeddedDataSpecification eds: list) {
                refList.add(eds.getDataSpecification());
            }

            AASReferenceList listNode = aasNode.getDataSpecificationNode();

            if (listNode == null) {
                AasReferenceCreator.addAasReferenceListNode(aasNode, refList, AASAssetAdministrationShellType.DATA_SPECIFICATION, nodeManager);
            }
            else {
                addEmbeddedDataSpecificationsReferences(listNode, refList, nodeManager);
            }
        }
    }


    /**
     * Adds the given Embedded Data Specifications to the desired node.
     *
     * @param aasNode The desired object where the DataSpecifications should be added
     * @param list The list of the desired Data Specifications
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addEmbeddedDataSpecifications(AASConceptDescriptionType aasNode, List<EmbeddedDataSpecification> list, AasServiceNodeManager nodeManager)
            throws StatusException {
        if ((list != null) && (!list.isEmpty())) {
            //            List<Reference> refList = new ArrayList<>();
            //            for (EmbeddedDataSpecification eds: list) {
            //                refList.add(eds.getDataSpecification());
            //            }

            AASEmbeddedDataSpecificationList listNode = aasNode.getEmbeddedDataSpecificationNode();

            int counter = 1;
            for (var embedDataSpec: list) {
                addEmbeddedDataSpecificationNode(listNode, embedDataSpec, AASConceptDescriptionType.EMBEDDED_DATA_SPECIFICATION + counter++, nodeManager);
            }

            //            if (listNode == null) {
            //                AasReferenceCreator.addAasReferenceListNode(aasNode, refList, AASAssetAdministrationShellType.DATA_SPECIFICATION, nodeManager);
            //            }
            //            else {
            //                addEmbeddedDataSpecificationsReferences(listNode, refList, nodeManager);
            //            }
        }
    }


    /**
     * Adds the references to the given Embedded Data Specification references.
     *
     * @param refListNode The desired object where the DataSpecifications should be added
     * @param refList The list of the desired Data Specification references
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addEmbeddedDataSpecificationsReferences(AASReferenceList refListNode, List<Reference> refList, AasServiceNodeManager nodeManager) throws StatusException {
        if ((refListNode != null) && (!refList.isEmpty())) {
            int count = 0;
            for (Reference ref: refList) {
                count++;
                String name = AASAssetAdministrationShellType.DATA_SPECIFICATION;
                if (count > 1) {
                    name += count;
                }

                AasReferenceCreator.addAasReferenceAasNS(refListNode, ref, name, nodeManager);
            }
        }
    }


    /**
     * Adds the references to the given Embedded Data Specifications.
     *
     * @param submodelElementNode The desired object where the
     *            DataSpecifications should be added
     * @param list The list of the desired Data Specifications
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addEmbeddedDataSpecifications(AASSubmodelElementType submodelElementNode, List<EmbeddedDataSpecification> list, AasServiceNodeManager nodeManager)
            throws StatusException {
        if ((list != null) && (!list.isEmpty())) {
            List<Reference> refList = new ArrayList<>();
            for (EmbeddedDataSpecification eds: list) {
                refList.add(eds.getDataSpecification());
            }

            AASReferenceList listNode = submodelElementNode.getDataSpecificationNode();

            if (listNode == null) {
                AasReferenceCreator.addAasReferenceListNode(submodelElementNode, refList, AASSubmodelElementType.DATA_SPECIFICATION, nodeManager);
            }
            else {
                addEmbeddedDataSpecificationsReferences(listNode, refList, nodeManager);
            }
        }
    }


    /**
     * Adds the references to the given Embedded Data Specifications.
     *
     * @param submodelNode The desired object where the DataSpecifications should be added
     * @param list The list of the desired Data Specifications
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addEmbeddedDataSpecifications(AASSubmodelType submodelNode, List<EmbeddedDataSpecification> list, AasServiceNodeManager nodeManager) throws StatusException {
        if ((list != null) && (!list.isEmpty())) {
            //List<Reference> refList = new ArrayList<>();
            //for (EmbeddedDataSpecification eds: list) {
            //    refList.add(eds.getDataSpecification());
            //}

            AASEmbeddedDataSpecificationList listNode = submodelNode.getEmbeddedDataSpecificationNode();

            int counter = 1;
            for (var embedDataSpec: list) {
                addEmbeddedDataSpecificationNode(listNode, embedDataSpec, AASConceptDescriptionType.EMBEDDED_DATA_SPECIFICATION + counter++, nodeManager);
            }

            //AASReferenceList listNode = submodelNode.getDataSpecificationNode();

            //if (listNode == null) {
            //    AasReferenceCreator.addAasReferenceListNode(submodelNode, refList, AASSubmodelType.DATA_SPECIFICATION, nodeManager);
            //}
            //else {
            //    addEmbeddedDataSpecificationsReferences(listNode, refList, nodeManager);
            //}
        }
    }


    /**
     * Adds the references to the given Embedded Data Specifications.
     *
     * @param assetNode The desired node where the DataSpecifications should be added
     * @param list The list of the desired Data Specifications
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addEmbeddedDataSpecifications(AASAssetType assetNode, List<EmbeddedDataSpecification> list, AasServiceNodeManager nodeManager) throws StatusException {
        if ((list != null) && (!list.isEmpty())) {
            List<Reference> refList = new ArrayList<>();
            for (EmbeddedDataSpecification eds: list) {
                refList.add(eds.getDataSpecification());
            }

            AASReferenceList listNode = assetNode.getDataSpecificationNode();

            if (listNode == null) {
                AasReferenceCreator.addAasReferenceListNode(assetNode, refList, AASAssetType.DATA_SPECIFICATION, nodeManager);
            }
            else {
                addEmbeddedDataSpecificationsReferences(listNode, refList, nodeManager);
            }
        }
    }


    private static void addEmbeddedDataSpecificationNode(UaNode node, EmbeddedDataSpecification embeddedDataSpecification, String name, AasServiceNodeManager nodeManager)
            throws StatusException {
        NodeId nid = nodeManager.getDefaultNodeId();
        AASEmbeddedDataSpecificationType dataSpecNode = nodeManager.createInstance(AASEmbeddedDataSpecificationType.class, name, nid);

        if (embeddedDataSpecification.getDataSpecification() != null) {
            AASReferenceType refNode = dataSpecNode.getDataSpecificationNode();
            if (refNode == null) {
                AasReferenceCreator.addAasReferenceAasNS(node, embeddedDataSpecification.getDataSpecification(), AASEmbeddedDataSpecificationType.DATA_SPECIFICATION, nodeManager);
            }
            else {
                AasSubmodelElementHelper.setAasReferenceData(embeddedDataSpecification.getDataSpecification(), refNode);
            }
        }

        addDataSpecificationContent(dataSpecNode, embeddedDataSpecification.getDataSpecificationContent(), nodeManager);
    }


    private static void addDataSpecificationContent(AASEmbeddedDataSpecificationType dataSpecNode, DataSpecificationContent content, AasServiceNodeManager nodeManager) {
        if (content != null) {
            if (dataSpecNode.getDataSpecificationContentNode() == null) {
                NodeId nid = nodeManager.getDefaultNodeId();
                AASDataSpecificationContentType contentNode = nodeManager.createInstance(AASDataSpecificationContentType.class,
                        AASEmbeddedDataSpecificationType.DATA_SPECIFICATION_CONTENT, nid);
                dataSpecNode.addComponent(contentNode);
            }
        }
    }
}
