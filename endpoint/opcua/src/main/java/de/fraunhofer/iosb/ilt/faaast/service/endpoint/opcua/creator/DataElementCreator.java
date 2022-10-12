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
import io.adminshell.aas.v3.model.Blob;
import io.adminshell.aas.v3.model.DataElement;
import io.adminshell.aas.v3.model.File;
import io.adminshell.aas.v3.model.MultiLanguageProperty;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.ReferenceElement;
import io.adminshell.aas.v3.model.Submodel;
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
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The AAS reference to the parent object
     * @param ordered Specifies whether the element should be added ordered
     *            (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addAasDataElement(UaNode node, DataElement aasDataElement, Submodel submodel, Reference parentRef, boolean ordered, AasServiceNodeManager nodeManager)
            throws StatusException {
        try {
            if ((node != null) && (aasDataElement != null)) {
                if (aasDataElement instanceof Property) {
                    PropertyCreator.addAasProperty(node, (Property) aasDataElement, submodel, parentRef, ordered, nodeManager);
                }
                else if (aasDataElement instanceof File) {
                    FileCreator.addAasFile(node, (File) aasDataElement, submodel, parentRef, ordered, null, nodeManager);
                }
                else if (aasDataElement instanceof Blob) {
                    BlobCreator.addAasBlob(node, (Blob) aasDataElement, submodel, parentRef, ordered, nodeManager);
                }
                else if (aasDataElement instanceof ReferenceElement) {
                    ReferenceElementCreator.addAasReferenceElement(node, (ReferenceElement) aasDataElement, submodel, parentRef, ordered, nodeManager);
                }
                else if (aasDataElement instanceof Range) {
                    RangeCreator.addAasRange(node, (Range) aasDataElement, submodel, parentRef, ordered, nodeManager);
                }
                else if (aasDataElement instanceof MultiLanguageProperty) {
                    MultiLanguagePropertyCreator.addAasMultiLanguageProperty(node, (MultiLanguageProperty) aasDataElement, submodel, parentRef, ordered, nodeManager);
                }
                else {
                    LOGGER.warn("addAasDataElement: unknown DataElement: {}; Class {}", aasDataElement.getIdShort(), aasDataElement.getClass());
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error("addAasDataElement Exception", ex);
            throw ex;
        }
    }

}
