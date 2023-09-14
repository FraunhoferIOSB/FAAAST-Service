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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
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
     */
    public static void addAasDataElement(UaNode node, DataElement aasDataElement, Reference elementRef, Submodel submodel, boolean ordered, AasServiceNodeManager nodeManager)
            throws StatusException {
        if ((node != null) && (aasDataElement != null)) {
            if (aasDataElement instanceof Property) {
                PropertyCreator.addAasProperty(node, (Property) aasDataElement, elementRef, submodel, ordered, nodeManager);
            }
            else if (aasDataElement instanceof File) {
                FileCreator.addAasFile(node, (File) aasDataElement, elementRef, submodel, ordered, null, nodeManager);
            }
            else if (aasDataElement instanceof Blob) {
                BlobCreator.addAasBlob(node, (Blob) aasDataElement, elementRef, submodel, ordered, nodeManager);
            }
            else if (aasDataElement instanceof ReferenceElement) {
                ReferenceElementCreator.addAasReferenceElement(node, (ReferenceElement) aasDataElement, elementRef, submodel, ordered, nodeManager);
            }
            else if (aasDataElement instanceof Range) {
                RangeCreator.addAasRange(node, (Range) aasDataElement, elementRef, submodel, ordered, nodeManager);
            }
            else if (aasDataElement instanceof MultiLanguageProperty) {
                MultiLanguagePropertyCreator.addAasMultiLanguageProperty(node, (MultiLanguageProperty) aasDataElement, elementRef, submodel, ordered, nodeManager);
            }
            else {
                LOGGER.warn("addAasDataElement: unknown DataElement: {}; Class {}", aasDataElement.getIdShort(), aasDataElement.getClass());
            }
        }
    }

}
