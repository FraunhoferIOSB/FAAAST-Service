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
import java.util.List;


/**
 * Describes the access control list.
 */
public class ACL {

    @JsonProperty("ATTRIBUTES")
    private List<Attribute> ATTRIBUTES; // e.g., "CLAIM", "GLOBAL"

    @JsonProperty("RIGHTS")
    private List<String> RIGHTS;

    @JsonProperty("ACCESS")
    private String ACCESS;

    @JsonProperty("USEATTRIBUTES")
    private String USEATTRIBUTES;

    public List<Attribute> getATTRIBUTES() {
        return ATTRIBUTES;
    }


    public void setATTRIBUTES(List<Attribute> ATTRIBUTES) {
        this.ATTRIBUTES = ATTRIBUTES;
    }


    public List<String> getRIGHTS() {
        return RIGHTS;
    }


    public void setRIGHTS(List<String> RIGHTS) {
        this.RIGHTS = RIGHTS;
    }


    public String getACCESS() {
        return ACCESS;
    }


    public void setACCESS(String ACCESS) {
        this.ACCESS = ACCESS;
    }


    public String getUSEATTRIBUTES() {
        return USEATTRIBUTES;
    }


    public void setUSEATTRIBUTES(String USEATTRIBUTES) {
        this.USEATTRIBUTES = USEATTRIBUTES;
    }
}
