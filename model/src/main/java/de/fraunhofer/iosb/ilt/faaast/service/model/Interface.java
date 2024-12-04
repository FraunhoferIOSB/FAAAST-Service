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
package de.fraunhofer.iosb.ilt.faaast.service.model;

/**
 * Enum of AAS interfaces.
 */
public enum Interface {
    AAS("AAS"),
    AAS_BASIC_DISCOVERY("AAS-DISCOVERY"),
    AAS_REGISTRY("AAS-REGISTRY"),
    AAS_REPOSITORY("AAS-REPOSITORY"),
    AASX_FILE_SERVER("AASX-FILE"),
    CONCEPT_DESCRIPTION_REPOSITORY("CD-REPOSITORY"),
    DESCRIPTION("DESCRIPTION"),
    SERIALIZATION("SERIALIZE"),
    SUBMODEL("SUBMODEL"),
    SUBMODEL_REGISTRY("SUBMODEL-REGISTRY"),
    SUBMODEL_REPOSITORY("SUBMODEL-REPOSITORY");

    private final String name;

    private Interface(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }
}
