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

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager.VALUES_READ_ONLY;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.server.nodes.PlainProperty;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import opc.i4aas.objecttypes.AASAdministrativeInformationType;
import org.eclipse.digitaltwin.aas4j.v3.model.AdministrativeInformation;


/**
 * Helper class to create AdministrativeInformations and integrate them into the
 * OPC UA address space.
 */
public class AdministrativeInformationCreator {

    private AdministrativeInformationCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Adds the AdminInformation Properties to the given node (if they don't
     * exist).
     *
     * @param adminInfNode The desired AdminInformation node
     * @param nodeManager The corresponding Node Manager
     * @param info The corresponding AAS AdministrativeInformation object
     * @throws StatusException If an error occurs
     */
    public static void addAdminInformationProperties(AASAdministrativeInformationType adminInfNode, AdministrativeInformation info, AasServiceNodeManager nodeManager)
            throws StatusException {
        if ((adminInfNode != null) && (info != null)) {
            if (info.getVersion() != null) {
                if (adminInfNode.getVersionNode() == null) {
                    createVersionNode(nodeManager, adminInfNode);
                }

                adminInfNode.setVersion(info.getVersion());
            }

            if (info.getRevision() != null) {
                if (adminInfNode.getRevisionNode() == null) {
                    createRevisionNode(nodeManager, adminInfNode);
                }

                adminInfNode.setRevision(info.getRevision());
            }
        }
    }


    private static void createRevisionNode(AasServiceNodeManager nodeManager, AASAdministrativeInformationType adminInfNode) {
        NodeId myPropertyId = new NodeId(nodeManager.getNamespaceIndex(),
                adminInfNode.getNodeId().getValue().toString() + "." + AASAdministrativeInformationType.REVISION);
        PlainProperty<String> myProperty = new PlainProperty<>(nodeManager, myPropertyId,
                UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASAssetAdministrationShellType.getNamespaceUri(), AASAdministrativeInformationType.REVISION)
                        .toQualifiedName(nodeManager.getNamespaceTable()),
                LocalizedText.english(AASAdministrativeInformationType.REVISION));
        myProperty.setDataTypeId(Identifiers.String);
        if (VALUES_READ_ONLY) {
            myProperty.setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
        }
        myProperty.setDescription(new LocalizedText("", ""));
        adminInfNode.addProperty(myProperty);
    }


    private static void createVersionNode(AasServiceNodeManager nodeManager, AASAdministrativeInformationType adminInfNode) {
        NodeId myPropertyId = new NodeId(nodeManager.getNamespaceIndex(),
                adminInfNode.getNodeId().getValue().toString() + "." + AASAdministrativeInformationType.VERSION);
        PlainProperty<String> myProperty = new PlainProperty<>(nodeManager, myPropertyId,
                UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASAssetAdministrationShellType.getNamespaceUri(), AASAdministrativeInformationType.VERSION)
                        .toQualifiedName(nodeManager.getNamespaceTable()),
                LocalizedText.english(AASAdministrativeInformationType.VERSION));
        myProperty.setDataTypeId(Identifiers.String);
        if (VALUES_READ_ONLY) {
            myProperty.setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
        }
        myProperty.setDescription(new LocalizedText("", ""));
        adminInfNode.addProperty(myProperty);
    }

}
