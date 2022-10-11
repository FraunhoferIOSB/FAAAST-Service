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
import com.prosysopc.ua.stack.core.AccessLevelType;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import io.adminshell.aas.v3.model.Constraint;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.List;
import opc.i4aas.AASSubmodelElementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create SubmodelElements and integrate them into the
 * OPC UA address space.
 */
public class SubmodelElementCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubmodelElementCreator.class);

    /**
     * Adds base data to the given submodel element.
     *
     * @param node The desired submodel element UA node
     * @param element The corresponding AAS submodel element
     * @throws StatusException If the operation fails
     * @param nodeManager The corresponding Node Manager
     */
    public static void addSubmodelElementBaseData(AASSubmodelElementType node, SubmodelElement element, AasServiceNodeManager nodeManager)
            throws StatusException {
        try {
            if ((node != null) && (element != null)) {
                // Category
                String category = element.getCategory();
                if (category == null) {
                    category = "";
                }
                node.setCategory(category);

                node.setModelingKind(ValueConverter.convertModelingKind(element.getKind()));

                // DataSpecifications
                EmbeddedDataSpecificationCreator.addEmbeddedDataSpecifications(node, element.getEmbeddedDataSpecifications(), nodeManager);

                // SemanticId
                if (element.getSemanticId() != null) {
                    ConceptDescriptionCreator.addSemanticId(node, element.getSemanticId());
                }

                // Qualifiers
                List<Constraint> qualifiers = element.getQualifiers();
                if ((qualifiers != null) && (!qualifiers.isEmpty())) {
                    if (node.getQualifierNode() == null) {
                        QualifierCreator.addQualifierNode(node, nodeManager);
                    }

                    QualifierCreator.addQualifiers(node.getQualifierNode(), qualifiers, nodeManager);
                }

                // Description
                DescriptionCreator.addDescriptions(node, element.getDescriptions());

                if (VALUES_READ_ONLY) {
                    node.getCategoryNode().setAccessLevel(AccessLevelType.CurrentRead);
                    node.getModelingKindNode().setAccessLevel(AccessLevelType.CurrentRead);
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error("addSubmodelElementBaseData Exception", ex);
            throw ex;
        }
    }

}
