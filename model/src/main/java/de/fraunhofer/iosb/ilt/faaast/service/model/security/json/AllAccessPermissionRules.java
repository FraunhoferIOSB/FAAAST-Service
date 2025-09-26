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
 * Describes all access permissiom rules.
 */
public class AllAccessPermissionRules {

    @JsonProperty("DEFACLS")
    private List<DefACL> DEFACLS;

    @JsonProperty("DEFOBJECTS")
    private List<DefObjects> DEFOBJECTS;

    @JsonProperty("DEFFORMULAS")
    private List<DefFormula> DEFFORMULAS;

    @JsonProperty("rules")
    private List<Rule> rules;

    @JsonProperty("DEFATTRIBUTES")
    private List<DefAttributes> DEFATTRIBUTES;

    public List<DefACL> getDEFACLS() {
        return DEFACLS;
    }


    public void setDEFACLS(List<DefACL> DEFACLS) {
        this.DEFACLS = DEFACLS;
    }


    public List<DefObjects> getDEFOBJECTS() {
        return DEFOBJECTS;
    }


    public void setDEFOBJECTS(List<DefObjects> DEFOBJECTS) {
        this.DEFOBJECTS = DEFOBJECTS;
    }


    public List<DefFormula> getDEFFORMULAS() {
        return DEFFORMULAS;
    }


    public void setDEFFORMULAS(List<DefFormula> DEFFORMULAS) {
        this.DEFFORMULAS = DEFFORMULAS;
    }


    public List<Rule> getRules() {
        return rules;
    }


    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }


    public List<DefAttributes> getDEFATTRIBUTES() {
        return DEFATTRIBUTES;
    }


    public void setDEFATTRIBUTES(List<DefAttributes> DEFATTRIBUTES) {
        this.DEFATTRIBUTES = DEFATTRIBUTES;
    }
}
