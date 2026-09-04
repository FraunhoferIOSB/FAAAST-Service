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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.Collection;
import java.util.List;
import opc.ua.aas.ReferenceTypeIds;
import opc.ua.aas.datatypes.AASQualifiable;
import opc.ua.aas.datatypes.AASSubmodelElementCommonAttributes;
import opc.ua.aas.objecttypes.AASSubmodelElementObjectType;
import opc.ua.aas.variabletypes.AASSubmodelElementVariableType;
import org.eclipse.digitaltwin.aas4j.v3.model.Capability;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
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

                LOGGER.atTrace().log("addSubmodelElements: parentRef {}; elementRef: {}", ReferenceHelper.toString(parentRef), ReferenceHelper.toString(elementRef));
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
        UaNode childNode = createSubmodelElement(elem, elementRef, submodel, nodeManager);
        if (childNode != null) {
            if (ordered) {
                node.addReference(childNode, nodeManager.getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasOrderedComponent), false);
            }
            else {
                node.addReference(childNode, nodeManager.getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasComponent), false);
            }
        }
    }


    /**
     * Creates a SubmodelElement node from the given input.
     *
     * @param elem The desired SubmodelElement
     * @param elementRef The reference to the AAS SubmodelElement
     * @param submodel The corresponding submodel
     * @param nodeManager The corresponding Node Manager
     * @return The created node.
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     */
    public static UaNode createSubmodelElement(SubmodelElement elem, Reference elementRef, Submodel submodel, AasServiceNodeManager nodeManager)
            throws ServiceException, ServiceResultException, StatusException, AddressSpaceException, ValueFormatException {
        UaNode childNode = null;
        if (elem instanceof DataElement dataElement) {
            childNode = DataElementCreator.createAasDataElement(dataElement, elementRef, submodel, nodeManager);
        }
        else if (elem instanceof Capability capability) {
            childNode = CapabilityCreator.createAasCapability(capability, elementRef, submodel, nodeManager);
        }
        else if (elem instanceof Entity entity) {
            childNode = EntityCreator.createAasEntity(entity, elementRef, submodel, nodeManager);
        }
        else if (elem instanceof Operation operation) {
            childNode = OperationCreator.createAasOperation(operation, elementRef, submodel, nodeManager);
        }
        else if (elem instanceof EventElement eventElement) {
            childNode = EventCreator.createAasEvent(eventElement, elementRef, submodel, nodeManager);
        }
        else if (elem instanceof RelationshipElement relationshipElement) {
            childNode = RelationshipElementCreator.createAasRelationshipElement(relationshipElement, elementRef, submodel, nodeManager);
        }
        else if (elem instanceof SubmodelElementCollection submodelElementCollection) {
            childNode = SubmodelElementCollectionCreator.createAasSubmodelElementCollection(submodelElementCollection, elementRef, submodel, nodeManager);
        }
        else if (elem instanceof SubmodelElementList submodelElementList) {
            childNode = SubmodelElementListCreator.createAasSubmodelElementList(submodelElementList, elementRef, submodel, nodeManager);
        }
        else if (elem != null) {
            LOGGER.warn("createSubmodelElement: unknown SubmodelElement: {}; Class {}", elem.getIdShort(), elem.getClass());
        }
        return childNode;
    }


    /**
     * Adds base data to the given submodel element.
     *
     * @param node The desired submodel element UA node
     * @param element The corresponding AAS submodel element
     * @throws StatusException If the operation fails
     * @param nodeManager The corresponding Node Manager
     * @throws ServiceResultException If an error occurs.
     */
    protected static void addSubmodelElementBaseData(AASSubmodelElementVariableType node, SubmodelElement element, AasServiceNodeManager nodeManager)
            throws StatusException, ServiceResultException {

        if ((node != null) && (element != null)) {
            if (node.getCommonAttributes() == null) {
                node.setCommonAttributes(new AASSubmodelElementCommonAttributes());
            }

            setSubmodelElementCommonAttributes(node.getCommonAttributes(), element);

            ConceptDescription conceptDescription = null;

            // SemanticId
            if (element.getSemanticId() != null) {
                //ConceptDescriptionCreator.addSemanticId(node, element.getSemanticId());
                conceptDescription = nodeManager.getConceptDescription(element.getSemanticId());
            }

            ConceptDescriptionCreator.addConceptDescription(node, conceptDescription, element.getEmbeddedDataSpecifications(), nodeManager);

            // Referable
            ReferableCreator.setReferebleNodeData(node, element);
            //DescriptionCreator.addDescriptions(node, element.getDescription());
        }
    }


    /**
     * Adds base data to the given submodel element.
     *
     * @param node The desired submodel element UA node
     * @param element The corresponding AAS submodel element
     * @throws StatusException If the operation fails
     * @param nodeManager The corresponding Node Manager
     * @throws ServiceResultException If an error occurs.
     */
    protected static void addSubmodelElementBaseData(AASSubmodelElementObjectType node, SubmodelElement element, AasServiceNodeManager nodeManager)
            throws StatusException, ServiceResultException {
        if ((node != null) && (element != null)) {
            if (node.getCommonAttributes() == null) {
                node.setCommonAttributes(new AASSubmodelElementCommonAttributes());
            }

            setSubmodelElementCommonAttributes(node.getCommonAttributes(), element);

            ConceptDescription conceptDescription = null;

            // HasSemantics
            node.getCommonAttributes().setHasSemantics(BaseDataCreator.getHasSemantics(element));
            if (element.getSemanticId() != null) {
                conceptDescription = nodeManager.getConceptDescription(element.getSemanticId());
            }

            ConceptDescriptionCreator.addConceptDescription(node, conceptDescription, element.getEmbeddedDataSpecifications(), nodeManager);

            // Referable
            ReferableCreator.setReferebleNodeData(node, element);
            //DescriptionCreator.addDescriptions(node, element.getDescription());
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


    private static void setSubmodelElementCommonAttributes(AASSubmodelElementCommonAttributes commonAttributes, SubmodelElement element)
            throws StatusException {

        commonAttributes.setReferable(ReferableCreator.getReferable(element));

        // DataSpecifications
        HasDataSpecificationCreator.addHasDataSpecification(commonAttributes, element);

        // Qualifiers
        List<Qualifier> qualifiers = element.getQualifiers();
        if ((qualifiers != null) && (!qualifiers.isEmpty())) {
            if (commonAttributes.getQualifiable() == null) {
                commonAttributes.setQualifiable(new AASQualifiable());
            }

            QualifierCreator.addQualifiers(commonAttributes.getQualifiable(), qualifiers);
        }
    }
}
