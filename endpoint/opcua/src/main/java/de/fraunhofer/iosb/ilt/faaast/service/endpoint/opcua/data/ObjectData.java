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

import com.prosysopc.ua.types.opcua.BaseObjectType;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.Submodel;


/**
 * @author Tino Bischoff
 */
public class ObjectData {

    /**
     * The Referable
     */
    private final Referable referable;

    /**
     * The OPC UA Node
     */
    private final BaseObjectType node;

    /**
     * The corresponding Submodel, if available
     */
    private final Submodel submodel;

    /**
     * Creates a new instance of ObjectData.
     * 
     * @param referable The Referable
     * @param node The OPC UA Node
     */
    public ObjectData(Referable referable, BaseObjectType node) {
        this.referable = referable;
        this.node = node;
        this.submodel = null;
    }


    /**
     * Creates a new instance of ObjectData.
     * 
     * @param referable The Referable
     * @param node The OPC UA Node
     * @param submodel The corresponding Submodel
     */
    public ObjectData(Referable referable, BaseObjectType node, Submodel submodel) {
        this.referable = referable;
        this.node = node;
        this.submodel = submodel;
    }


    /**
     * Gets the Referable,
     * 
     * @return The Referable
     */
    public Referable getReferable() {
        return referable;
    }


    /**
     * Gets the OPC UA Node
     * 
     * @return
     */
    public BaseObjectType getNode() {
        return node;
    }


    /**
     * Gets the corresponding Submodel
     * 
     * @return The corresponding Submodel, null if not available
     */
    public Submodel getSubmodel() {
        return submodel;
    }
}
