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
import java.util.ArrayList;
import java.util.List;
import opc.ua.aas.datatypes.AASConceptDescriptionCommonAttributes;
import opc.ua.aas.datatypes.AASHasDataSpecification;
import opc.ua.aas.datatypes.AASReference;
import opc.ua.aas.datatypes.AASSubmodelElementCommonAttributes;
import opc.ua.aas.objecttypes.AASAssetAdministrationShellType;
import opc.ua.aas.objecttypes.AASSubmodelType;
import org.eclipse.digitaltwin.aas4j.v3.model.HasDataSpecification;


/**
 * Helper class to create EmbeddedDataSpecifications and integrate them into
 * the OPC UA address space.
 */
public class HasDataSpecificationCreator {

    private HasDataSpecificationCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Adds the given Embedded Data Specifications to the desired node.
     *
     * @param aasNode The desired object where the DataSpecifications should be added
     * @param dataSpecification The desired Data Specification
     * @throws StatusException If the operation fails
     */
    public static void addHasDataSpecification(AASAssetAdministrationShellType aasNode, HasDataSpecification dataSpecification)
            throws StatusException {
        if (aasNode.getCommonAttributes().getHasDataSpecification() == null) {
            aasNode.getCommonAttributes().setHasDataSpecification(new AASHasDataSpecification());
        }
        AASHasDataSpecification listNode = aasNode.getCommonAttributes().getHasDataSpecification();
        addHasDataSpecification(dataSpecification, listNode);
    }


    /**
     * Adds the given Embedded Data Specifications to the desired node.
     *
     * @param aasNode The desired object where the DataSpecifications should be added
     * @param dataSpecification The desired Data Specification
     * @throws StatusException If the operation fails
     */
    public static void addHasDataSpecification(AASConceptDescriptionCommonAttributes aasNode, HasDataSpecification dataSpecification)
            throws StatusException {
        if (aasNode.getHasDataSpecification() == null) {
            aasNode.setHasDataSpecification(new AASHasDataSpecification());
        }
        addHasDataSpecification(dataSpecification, aasNode.getHasDataSpecification());
    }


    /**
     * Adds the references to the given Embedded Data Specifications.
     *
     * @param submodelElementCommonNode The desired object where the DataSpecifications should be added.
     * @param dataSpecification The desired Data Specification
     * @throws StatusException If the operation fails
     */
    public static void addHasDataSpecification(AASSubmodelElementCommonAttributes submodelElementCommonNode, HasDataSpecification dataSpecification)
            throws StatusException {
        if (submodelElementCommonNode.getHasDataSpecification() == null) {
            submodelElementCommonNode.setHasDataSpecification(new AASHasDataSpecification());
        }
        AASHasDataSpecification listNode = submodelElementCommonNode.getHasDataSpecification();
        addHasDataSpecification(dataSpecification, listNode);
    }


    /**
     * Adds the references to the given Embedded Data Specifications.
     *
     * @param submodelNode The desired object where the DataSpecifications should be added
     * @param dataSpecification The desired Data Specification
     * @throws StatusException If the operation fails
     */
    public static void addHasDataSpecification(AASSubmodelType submodelNode, HasDataSpecification dataSpecification) throws StatusException {
        if (submodelNode.getCommonAttributes().getHasDataSpecification() == null) {
            submodelNode.getCommonAttributes().setHasDataSpecification(new AASHasDataSpecification());
        }
        AASHasDataSpecification listNode = submodelNode.getCommonAttributes().getHasDataSpecification();
        addHasDataSpecification(dataSpecification, listNode);
    }


    private static void addHasDataSpecification(HasDataSpecification dataSpecification, AASHasDataSpecification listNode)
            throws StatusException {
        List<AASReference> refList = new ArrayList<>();
        var list = dataSpecification.getEmbeddedDataSpecifications();
        if ((list != null) && (!list.isEmpty())) {
            for (var embedDataSpec: list) {
                refList.add(ReferenceCreator.getAasReference(embedDataSpec.getDataSpecification()));
                //addEmbeddedDataSpecificationNode(listNode, embedDataSpec, name + counter++, nodeManager);
            }
            listNode.setDataSpecification(refList.toArray(AASReference[]::new));
        }
    }
}
