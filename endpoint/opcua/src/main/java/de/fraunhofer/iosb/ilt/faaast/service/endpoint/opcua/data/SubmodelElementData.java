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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data;

import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;


/**
 * @author Tino Bischoff
 */
public class SubmodelElementData {

    private final SubmodelElement submodelElement;
    private final Submodel submodel;
    private final Type type;
    private final Reference reference;

    /**
     * Creates a new instance of SubmodelElementData
     * 
     * @param submodelElement The desired SubmodelElement
     * @param submodel The desired Submodel
     * @param type The desired type
     * @param reference The desired reference
     */
    public SubmodelElementData(SubmodelElement submodelElement, Submodel submodel, Type type, Reference reference) {
        this.submodelElement = submodelElement;
        this.submodel = submodel;
        this.type = type;
        this.reference = reference;
    }


    /**
     * Gets the SubmodelElement
     * 
     * @return The desired SubmodelElement
     */
    public SubmodelElement getSubmodelElement() {
        return submodelElement;
    }


    /**
     * Gets the Submodel
     * 
     * @return The desired Submodel
     */
    public Submodel getSubmodel() {
        return submodel;
    }


    /**
     * Gets the type
     * 
     * @return The type
     */
    public Type getType() {
        return type;
    }


    /**
     * Gets the reference.
     * 
     * @return The reference
     */
    public Reference getReference() {
        return reference;
    }

    /**
     * The type of the data value.
     */
    public enum Type {
        PROPERTY_VALUE,
        RANGE_MIN,
        RANGE_MAX,
        OPERATION,
        BLOB_VALUE,
        MULTI_LANGUAGE_VALUE,
        REFERENCE_ELEMENT_VALUE,
        RELATIONSHIP_ELEMENT_FIRST,
        RELATIONSHIP_ELEMENT_SECOND,
        ENTITY_GLOBAL_ASSET_ID,
        ENTITY_TYPE
    }
}
