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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.ArrayList;
import java.util.List;
import opc.ua.aas.datatypes.AASKey;
import opc.ua.aas.datatypes.AASReference;
import opc.ua.aas.variabletypes.AASReferenceElementType;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Helper class to create AAS References and integrate then into
 * the OPC UA address space.
 */
public class ReferenceCreator {

    private ReferenceCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Sets the data in the given Reference node.
     *
     * @param ref The desired UA reference object
     * @param refNode The AAS Reference object with the source data
     * @throws StatusException If the operation fails
     */
    public static void setAasReferenceData(Reference ref, AASReferenceElementType refNode) throws StatusException {
        if (ref == null) {
            return;
        }
        AASReference aasref = new AASReference();
        setAasReferenceData(ref, aasref);
        refNode.setValue(aasref);
    }


    /**
     * Sets the data in the given Reference node.
     *
     * @param ref The desired UA reference object
     * @param refNode The AAS Reference object with the source data
     */
    public static void setAasReferenceData(Reference ref, AASReference refNode) {
        Ensure.requireNonNull(refNode, "refNode must be non-null");
        Ensure.requireNonNull(ref, "ref must be non-null");

        AASKey[] keys = ref.getKeys().stream().map(k -> {
            AASKey keyValue = new AASKey();
            keyValue.setType(ValueConverter.convertKeyTypes(k.getType()));
            keyValue.setValue(k.getValue());
            return keyValue;
        }).toArray(AASKey[]::new);

        refNode.setReferredSemanticId(getAasReference(ref.getReferredSemanticId()));
        refNode.setType(ValueConverter.convertReferenceTypes(ref.getType()));
        refNode.setKey(keys);
    }


    /**
     * Gets an OPC UA reference from an AAS reference.
     *
     * @param ref The desired AAS reference.
     * @return The corresponding OPC UA reference.
     */
    public static AASReference getAasReference(Reference ref) {
        if (ref == null) {
            return null;
        }
        AASReference referenceNode = new AASReference();
        setAasReferenceData(ref, referenceNode);
        return referenceNode;
    }


    /**
     * Gets a list of OPC UA references from a list of AAS references.
     *
     * @param refs The desired list of AAS references.
     * @return The corresponding list of OPC UA references.
     */
    public static List<AASReference> getAasReferences(List<Reference> refs) {
        List<AASReference> retval = new ArrayList<>();
        for (Reference ref: refs) {
            retval.add(getAasReference(ref));
        }
        return retval;
    }
}
