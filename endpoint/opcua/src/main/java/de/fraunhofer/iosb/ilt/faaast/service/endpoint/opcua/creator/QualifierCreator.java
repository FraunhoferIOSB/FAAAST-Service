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

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import java.util.ArrayList;
import java.util.List;
import opc.ua.aas.datatypes.AASQualifiable;
import opc.ua.aas.datatypes.AASQualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create Qualifier and integrate them into the
 * OPC UA address space.
 */
public class QualifierCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(QualifierCreator.class);

    private QualifierCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Adds a list of Qualifiers to the given Node.
     *
     * @param opcQualifiable The UA node in which the Qualifiers should be created
     * @param qualifiers The desired list of Qualifiers
     */
    public static void addQualifiers(AASQualifiable opcQualifiable, List<Qualifier> qualifiers) {
        if (opcQualifiable == null) {
            throw new IllegalArgumentException("listNode = null");
        }
        else if (qualifiers == null) {
            throw new IllegalArgumentException("qualifiers = null");
        }

        LOGGER.info("addQualifiers:; add {} qualifiers", qualifiers.size());
        List<AASQualifier> opcQualifiers = new ArrayList<>();
        for (Qualifier qualifier: qualifiers) {
            if (qualifier != null) {
                opcQualifiers.add(getQualifier(qualifier));
            }
        }
        opcQualifiable.setQualifier(opcQualifiers.toArray(AASQualifier[]::new));
    }


    /**
     * Creates and adds a Qualifier to the given Node.
     *
     * @param qualifierNode The UA Qualifier node
     * @param qualifier The desired Qualifier
     */
    public static void setQualifierData(AASQualifier qualifierNode, Qualifier qualifier) {
        if (qualifierNode == null) {
            throw new IllegalArgumentException(AasServiceNodeManager.NODE_NULL);
        }
        else if (qualifier == null) {
            throw new IllegalArgumentException("qualifier = null");
        }

        if (qualifier.getKind() != null) {
            qualifierNode.setKind(ValueConverter.convertQualifierKind(qualifier.getKind()));
        }

        // SemanticId
        qualifierNode.setHasSemantics(BaseDataCreator.getHasSemantics(qualifier));

        // Type
        qualifierNode.setType(qualifier.getType());

        // ValueType
        qualifierNode.setValueType(ValueConverter.convertDataTypeDefToString(qualifier.getValueType()));

        // Value
        qualifierNode.setValue(qualifier.getValue());

        // ValueId
        qualifierNode.setValueId(ReferenceCreator.getAasReference(qualifier.getValueId()));
    }


    private static AASQualifier getQualifier(Qualifier qualifier) {
        if (qualifier == null) {
            return null;
        }
        AASQualifier retval = new AASQualifier();
        setQualifierData(retval, qualifier);
        return retval;
    }
}
