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
import com.prosysopc.ua.stack.core.AccessLevelType;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import io.adminshell.aas.v3.model.AdministrativeInformation;
import io.adminshell.aas.v3.model.Identifier;
import opc.i4aas.AASIdentifiableType;


/**
 * Helper class to create Identifiables and integrate them into the
 * OPC UA address space.
 */
public class IdentifiableCreator {

    private IdentifiableCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Adds AAS Identifiable information to the given node.
     *
     * @param identifiableNode The desired node where the Identifiable
     *            information should be added
     * @param identifier The corresponding AAS Identifier
     * @param adminInfo The corresponding AAS Administrative Information
     * @param category The desired category
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException if an error occurs
     */
    public static void addIdentifiable(AASIdentifiableType identifiableNode, Identifier identifier, AdministrativeInformation adminInfo, String category,
                                       AasServiceNodeManager nodeManager)
            throws StatusException {
        if (identifier != null) {
            identifiableNode.getIdentificationNode().setId(identifier.getIdentifier());
            identifiableNode.getIdentificationNode().setIdType(ValueConverter.convertIdentifierType(identifier.getIdType()));
        }

        AdministrativeInformationCreator.addAdminInformationProperties(identifiableNode.getAdministrationNode(), adminInfo, nodeManager);

        if (category == null) {
            category = "";
        }
        identifiableNode.setCategory(category);

        if (AasServiceNodeManager.VALUES_READ_ONLY) {
            identifiableNode.getIdentificationNode().getIdNode().setAccessLevel(AccessLevelType.CurrentRead);
            identifiableNode.getIdentificationNode().getIdTypeNode().setAccessLevel(AccessLevelType.CurrentRead);
            identifiableNode.getCategoryNode().setAccessLevel(AccessLevelType.CurrentRead);
        }
    }

}
