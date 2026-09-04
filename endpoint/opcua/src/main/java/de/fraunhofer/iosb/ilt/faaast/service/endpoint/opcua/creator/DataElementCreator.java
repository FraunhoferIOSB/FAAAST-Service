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
import com.prosysopc.ua.stack.common.ServiceResultException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import opc.ua.aas.ReferenceTypeIds;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.DataElement;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create DataElements and integrate them into the
 * OPC UA address space.
 */
public class DataElementCreator extends SubmodelElementCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataElementCreator.class);

    /**
     * Adds an AAS data element the given node.
     *
     * @param node The desired node
     * @param aasDataElement The corresponding AAS data element to add
     * @param elementRef The AAS reference to the AAS data element.
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param ordered Specifies whether the element should be added ordered
     *            (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     * @throws ServiceResultException If an error occurs
     */
    public static void addAasDataElement(UaNode node, DataElement aasDataElement, Reference elementRef, Submodel submodel, boolean ordered, AasServiceNodeManager nodeManager)
            throws StatusException, ServiceResultException {
        UaNode childNode = createAasDataElement(aasDataElement, elementRef, submodel, nodeManager);
        if (childNode != null) {
            if (ordered) {
                node.addReference(childNode, nodeManager.getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasOrderedComponent), false);
            }
            else {
                node.addReference(childNode, nodeManager.getNamespaceTable().toNodeId(ReferenceTypeIds.AASHasComponent), false);
            }
        }
    }


    public static UaNode createAasDataElement(DataElement aasDataElement, Reference elementRef, Submodel submodel, AasServiceNodeManager nodeManager)
            throws StatusException {
        UaNode retval = null;
        if (aasDataElement != null) {
            if (aasDataElement instanceof Property property) {
                retval = PropertyCreator.createAasProperty(property, elementRef, submodel, nodeManager);
            }
            else if (aasDataElement instanceof File file) {
                retval = FileCreator.createAasFile(file, elementRef, submodel, null, nodeManager);
            }
            else if (aasDataElement instanceof Blob blob) {
                retval = BlobCreator.createAasBlob(blob, elementRef, submodel, nodeManager);
            }
            else if (aasDataElement instanceof ReferenceElement referenceElement) {
                retval = ReferenceElementCreator.createAasReferenceElement(referenceElement, elementRef, submodel, nodeManager);
            }
            else if (aasDataElement instanceof Range range) {
                retval = RangeCreator.createAasRange(range, elementRef, submodel, nodeManager);
            }
            else if (aasDataElement instanceof MultiLanguageProperty multiLanguageProperty) {
                retval = MultiLanguagePropertyCreator.createAasMultiLanguageProperty(multiLanguageProperty, elementRef, submodel, nodeManager);
            }
            else {
                LOGGER.warn("createAasDataElement: unknown DataElement: {}; Class {}", aasDataElement.getIdShort(), aasDataElement.getClass());
            }
        }
        return retval;
    }
}
