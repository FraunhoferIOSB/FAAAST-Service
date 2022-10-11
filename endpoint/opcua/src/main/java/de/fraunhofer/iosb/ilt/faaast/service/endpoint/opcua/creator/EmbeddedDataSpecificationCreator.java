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
import io.adminshell.aas.v3.model.EmbeddedDataSpecification;
import io.adminshell.aas.v3.model.Reference;
import java.util.ArrayList;
import java.util.List;
import opc.i4aas.AASAssetAdministrationShellType;
import opc.i4aas.AASAssetType;
import opc.i4aas.AASReferenceList;
import opc.i4aas.AASSubmodelElementType;
import opc.i4aas.AASSubmodelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create EmbeddedDataSpecifications and integrate them into
 * the OPC UA address space.
 */
public class EmbeddedDataSpecificationCreator {
    /**
     * Text for addEmbeddedDataSpecifications Exception
     */
    private static final String ADD_EMBED_DS_EXC = "addEmbeddedDataSpecifications Exception";

    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedDataSpecificationCreator.class);

    /**
     * Adds the references to the given Embedded Data Specifications.
     *
     * @param aasNode The desired object where the DataSpecifications should be added
     * @param list The list of the desired Data Specifications
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addEmbeddedDataSpecifications(AASAssetAdministrationShellType aasNode, List<EmbeddedDataSpecification> list, AasServiceNodeManager nodeManager)
            throws StatusException {
        try {
            if ((list != null) && (!list.isEmpty())) {
                List<Reference> refList = new ArrayList<>();
                for (EmbeddedDataSpecification eds: list) {
                    refList.add(eds.getDataSpecification());
                }

                AASReferenceList listNode = aasNode.getDataSpecificationNode();

                if (listNode == null) {
                    AasReferenceCreator.addAasReferenceList(aasNode, refList, AASAssetAdministrationShellType.DATA_SPECIFICATION, nodeManager);
                }
                else {
                    addEmbeddedDataSpecificationsReferences(listNode, refList, nodeManager);
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error(ADD_EMBED_DS_EXC, ex);
            throw ex;
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
        try {
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
        catch (Exception ex) {
            LOGGER.error(ADD_EMBED_DS_EXC, ex);
            throw ex;
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
        try {
            if ((list != null) && (!list.isEmpty())) {
                List<Reference> refList = new ArrayList<>();
                for (EmbeddedDataSpecification eds: list) {
                    refList.add(eds.getDataSpecification());
                }

                AASReferenceList listNode = submodelElementNode.getDataSpecificationNode();

                if (listNode == null) {
                    AasReferenceCreator.addAasReferenceList(submodelElementNode, refList, AASSubmodelElementType.DATA_SPECIFICATION, nodeManager);
                }
                else {
                    addEmbeddedDataSpecificationsReferences(listNode, refList, nodeManager);
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error(ADD_EMBED_DS_EXC, ex);
            throw ex;
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
        try {
            if ((list != null) && (!list.isEmpty())) {
                List<Reference> refList = new ArrayList<>();
                for (EmbeddedDataSpecification eds: list) {
                    refList.add(eds.getDataSpecification());
                }

                AASReferenceList listNode = submodelNode.getDataSpecificationNode();

                if (listNode == null) {
                    AasReferenceCreator.addAasReferenceList(submodelNode, refList, AASSubmodelType.DATA_SPECIFICATION, nodeManager);
                }
                else {
                    addEmbeddedDataSpecificationsReferences(listNode, refList, nodeManager);
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error(ADD_EMBED_DS_EXC, ex);
            throw ex;
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
        try {
            if ((list != null) && (!list.isEmpty())) {
                List<Reference> refList = new ArrayList<>();
                for (EmbeddedDataSpecification eds: list) {
                    refList.add(eds.getDataSpecification());
                }

                AASReferenceList listNode = assetNode.getDataSpecificationNode();

                if (listNode == null) {
                    AasReferenceCreator.addAasReferenceList(assetNode, refList, AASAssetType.DATA_SPECIFICATION, nodeManager);
                }
                else {
                    addEmbeddedDataSpecificationsReferences(listNode, refList, nodeManager);
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error(ADD_EMBED_DS_EXC, ex);
            throw ex;
        }
    }

}
