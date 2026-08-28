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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import java.util.ArrayList;
import java.util.List;
import opc.ua.aas.datatypes.AASConceptDescriptionCommonAttributes;
import opc.ua.aas.datatypes.AASHasDataSpecification;
import opc.ua.aas.datatypes.AASReference;
import opc.ua.aas.datatypes.AASSubmodelElementCommonAttributes;
import opc.ua.aas.objecttypes.AASAssetAdministrationShellType;
import opc.ua.aas.objecttypes.AASSubmodelType;
import org.eclipse.digitaltwin.aas4j.v3.model.EmbeddedDataSpecification;


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
            //AASEmbeddedDataSpecificationList listNode = aasNode.getEmbeddedDataSpecificationNode();
            AASHasDataSpecification listNode = aasNode.getCommonAttributes().getHasDataSpecification();
            addEmbeddedDataSpecifications(list, listNode);
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
    public static void addEmbeddedDataSpecifications(AASConceptDescriptionCommonAttributes aasNode, List<EmbeddedDataSpecification> list, AasServiceNodeManager nodeManager)
            throws StatusException {
        if ((list != null) && (!list.isEmpty())) {
            //AASEmbeddedDataSpecificationList listNode = aasNode.getEmbeddedDataSpecificationNode();
            addEmbeddedDataSpecifications(list, aasNode.getHasDataSpecification());
        }
    }


    /**
     * Adds the references to the given Embedded Data Specifications.
     *
     * @param submodelElementCommonNode The desired object where the DataSpecifications should be added.
     * @param list The list of the desired Data Specifications
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addEmbeddedDataSpecifications(AASSubmodelElementCommonAttributes submodelElementCommonNode, List<EmbeddedDataSpecification> list,
                                                     AasServiceNodeManager nodeManager)
            throws StatusException {
        if ((list != null) && (!list.isEmpty())) {
            AASHasDataSpecification listNode = submodelElementCommonNode.getHasDataSpecification();
            addEmbeddedDataSpecifications(list, listNode);
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

            AASHasDataSpecification listNode = submodelNode.getCommonAttributes().getHasDataSpecification();
            addEmbeddedDataSpecifications(list, listNode);
        }
    }


    private static void addEmbeddedDataSpecifications(List<EmbeddedDataSpecification> list, AASHasDataSpecification listNode)
            throws StatusException {
        //int counter = 1;
        List<AASReference> refList = new ArrayList<>();
        for (var embedDataSpec: list) {
            refList.add(ReferenceCreator.getAasReference(embedDataSpec.getDataSpecification()));
            //addEmbeddedDataSpecificationNode(listNode, embedDataSpec, name + counter++, nodeManager);
        }
        listNode.setDataSpecification(refList.toArray(AASReference[]::new));
    }

    //    private static void addEmbeddedDataSpecificationNode(UaNode node, EmbeddedDataSpecification embeddedDataSpecification, String name, AasServiceNodeManager nodeManager)
    //            throws StatusException {
    //        NodeId nid = nodeManager.getDefaultNodeId();
    //        AASEmbeddedDataSpecificationType dataSpecNode = nodeManager.createInstance(AASEmbeddedDataSpecificationType.class, name, nid);
    //
    //        if (embeddedDataSpecification.getDataSpecification() != null) {
    //            AASReferenceType refNode = dataSpecNode.getDataSpecificationNode();
    //            if (refNode == null) {
    //                AasReferenceCreator.addAasReferenceAasNS(node, embeddedDataSpecification.getDataSpecification(), AASEmbeddedDataSpecificationType.DATA_SPECIFICATION, nodeManager);
    //            }
    //            else {
    //                AasReferenceCreator.setAasReferenceData(embeddedDataSpecification.getDataSpecification(), refNode);
    //            }
    //        }
    //
    //        addDataSpecificationContent(dataSpecNode, embeddedDataSpecification.getDataSpecificationContent(), nodeManager);
    //    }

    //    private static void addDataSpecificationContent(AASEmbeddedDataSpecificationType dataSpecNode, DataSpecificationContent content, AasServiceNodeManager nodeManager) {
    //        if ((content != null) && (dataSpecNode.getDataSpecificationContentNode() == null)) {
    //            NodeId nid = nodeManager.getDefaultNodeId();
    //            AASDataSpecificationContentType contentNode = nodeManager.createInstance(AASDataSpecificationContentType.class,
    //                    AASEmbeddedDataSpecificationType.DATA_SPECIFICATION_CONTENT, nid);
    //            dataSpecNode.addComponent(contentNode);
    //        }
    //    }
}
