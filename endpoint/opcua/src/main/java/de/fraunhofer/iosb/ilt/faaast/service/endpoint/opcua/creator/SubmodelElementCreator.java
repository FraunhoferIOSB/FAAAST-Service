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
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.AccessLevelType;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.Collection;
import java.util.List;
import opc.i4aas.objecttypes.AASSubmodelElementType;
import org.eclipse.digitaltwin.aas4j.v3.model.Capability;
import org.eclipse.digitaltwin.aas4j.v3.model.DataElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.EventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create SubmodelElements and integrate them into the
 * OPC UA address space.
 */
public class SubmodelElementCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubmodelElementCreator.class);

    protected SubmodelElementCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Adds a list of submodel elements to the given node.
     *
     * @param node The desired node in which the objects should be created
     * @param elements The desired list of submodel elements
     * @param submodel The corresponding submodel
     * @param parentRef The AAS reference to the parent object
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     */
    public static void addSubmodelElements(UaNode node, List<SubmodelElement> elements, Submodel submodel, Reference parentRef, AasServiceNodeManager nodeManager)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException, ValueFormatException {
        addSubmodelElements(node, elements, parentRef, submodel, false, nodeManager);
    }


    /**
     * Adds a list of submodel elements to the given node (ordered, if requested).
     *
     * @param node The desired node in which the objects should be created
     * @param elements The desired list of submodel elements
     * @param parentRef The AAS reference to the parent object
     * @param submodel The corresponding submodel
     * @param ordered Specifies where the elements are from a list (true) or not (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     */
    public static void addSubmodelElements(UaNode node, Collection<SubmodelElement> elements, Reference parentRef, Submodel submodel, boolean ordered,
                                           AasServiceNodeManager nodeManager)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException, ValueFormatException {
        if ((elements != null) && (!elements.isEmpty())) {
            for (SubmodelElement elem: elements) {
                Reference elementRef = ReferenceBuilder.with(parentRef).element(elem).build();

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("addSubmodelElements: parentRef {}; elementRef: {}", ReferenceHelper.toString(parentRef), ReferenceHelper.toString(elementRef));
                }
                addSubmodelElement(elem, node, elementRef, submodel, ordered, nodeManager);
            }
        }
    }


    /**
     * Adds a submodel element to the given node.
     * 
     * @param elem The desired SubmodelElement
     * @param node The desired node in which the objects should be created
     * @param elementRef The reference to the AAS SubmodelElement
     * @param submodel The corresponding submodel
     * @param ordered Specifies where the elements are from a list (true) or not (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     */
    public static void addSubmodelElement(SubmodelElement elem, UaNode node, Reference elementRef, Submodel submodel, boolean ordered, AasServiceNodeManager nodeManager)
            throws ServiceException, ServiceResultException, StatusException, AddressSpaceException, ValueFormatException {
        if (elem instanceof DataElement) {
            DataElementCreator.addAasDataElement(node, (DataElement) elem, elementRef, submodel, ordered, nodeManager);
        }
        else if (elem instanceof Capability) {
            CapabilityCreator.addAasCapability(node, (Capability) elem, elementRef, submodel, ordered, nodeManager);
        }
        else if (elem instanceof Entity) {
            EntityCreator.addAasEntity(node, (Entity) elem, elementRef, submodel, ordered, nodeManager);
        }
        else if (elem instanceof Operation) {
            OperationCreator.addAasOperation(node, (Operation) elem, elementRef, submodel, ordered, nodeManager);
        }
        else if (elem instanceof EventElement) {
            EventCreator.addAasEvent(node, (EventElement) elem, elementRef, submodel, ordered, nodeManager);
        }
        else if (elem instanceof RelationshipElement) {
            RelationshipElementCreator.addAasRelationshipElement(node, (RelationshipElement) elem, elementRef, submodel, ordered, nodeManager);
        }
        else if (elem instanceof SubmodelElementCollection) {
            SubmodelElementCollectionCreator.addAasSubmodelElementCollection(node, (SubmodelElementCollection) elem, elementRef, submodel, nodeManager);
        }
        else if (elem instanceof SubmodelElementList) {
            SubmodelElementListCreator.addAasSubmodelElementList(node, (SubmodelElementList) elem, elementRef, submodel, nodeManager);
        }
        else if (elem != null) {
            LOGGER.warn("addSubmodelElements: unknown SubmodelElement: {}; Class {}", elem.getIdShort(), elem.getClass());
        }
    }


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
        if ((node != null) && (element != null)) {
            // Category
            String category = element.getCategory();
            node.setCategory(category != null ? category : "");

            // DataSpecifications
            EmbeddedDataSpecificationCreator.addEmbeddedDataSpecifications(node, element.getEmbeddedDataSpecifications(), nodeManager);

            // SemanticId
            if (element.getSemanticId() != null) {
                ConceptDescriptionCreator.addSemanticId(node, element.getSemanticId());
            }

            // Qualifiers
            List<Qualifier> qualifiers = element.getQualifiers();
            if ((qualifiers != null) && (!qualifiers.isEmpty())) {
                if (node.getQualifierNode() == null) {
                    QualifierCreator.addQualifierNode(node, nodeManager);
                }

                QualifierCreator.addQualifiers(node.getQualifierNode(), qualifiers, nodeManager);
            }

            // Description
            DescriptionCreator.addDescriptions(node, element.getDescription());

            if (AasServiceNodeManager.VALUES_READ_ONLY) {
                node.getCategoryNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
            }
        }
    }


    protected static String getNameFromReference(Reference reference) {
        String retval;
        Ensure.requireNonNull(reference);
        IdShortPath path = IdShortPath.fromReference(reference);
        if (path.isEmpty()) {
            throw new IllegalArgumentException("unable to extract path");
        }
        else {
            retval = path.getElements().get(path.getElements().size() - 1);
        }
        return retval;
    }
}
