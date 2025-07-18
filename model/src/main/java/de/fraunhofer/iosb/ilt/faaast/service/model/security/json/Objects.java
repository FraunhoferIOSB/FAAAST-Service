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
package de.fraunhofer.iosb.ilt.faaast.service.model.security.json;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Objects can the routes and identifiables.
 */
public class Objects {

    @JsonProperty("ROUTE")
    private String ROUTE;

    @JsonProperty("IDENTIFIABLE")
    private String IDENTIFIABLE;

    @JsonProperty("REFERABLE")
    private String REFERABLE;

    @JsonProperty("DESCRIPTOR")
    private String DESCRIPTOR;

    public String getROUTE() {
        return ROUTE;
    }


    public void setROUTE(String ROUTE) {
        this.ROUTE = ROUTE;
    }


    public String getIDENTIFIABLE() {
        return IDENTIFIABLE;
    }


    public void setIDENTIFIABLE(String IDENTIFIABLE) {
        this.IDENTIFIABLE = IDENTIFIABLE;
    }


    public String getREFERABLE() {
        return REFERABLE;
    }


    public void setREFERABLE(String REFERABLE) {
        this.REFERABLE = REFERABLE;
    }


    public String getDESCRIPTOR() {
        return DESCRIPTOR;
    }


    public void setDESCRIPTOR(String DESCRIPTOR) {
        this.DESCRIPTOR = DESCRIPTOR;
    }
}
