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
package de.fraunhofer.iosb.ilt.faaast.service.model.submodeltemplate;

import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.serialization.EnumSerializer;


/**
 * Describes the cardinality of relationships used with submodel templates.
 */
public enum Cardinality {
    ONE(false),
    ZERO_TO_ONE(false),
    ZERO_TO_MANY(true),
    ONE_TO_MANY(true);

    public static final String SEMANTIC_ID = "https://admin-shell.io/SubmodelTemplates/Cardinality/1/0";
    public static final Cardinality DEFAULT = Cardinality.ONE;
    private final boolean allowsMultipleValues;

    private Cardinality(boolean allowsMultipleValues) {
        this.allowsMultipleValues = allowsMultipleValues;
    }


    public boolean getAllowsMultipleValues() {
        return allowsMultipleValues;
    }


    public String getNameForSerialization() {
        return EnumSerializer.serializeEnumName(name());
    }
}
