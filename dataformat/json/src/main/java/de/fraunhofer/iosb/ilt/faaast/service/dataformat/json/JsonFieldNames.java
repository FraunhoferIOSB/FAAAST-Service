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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json;

/**
 * Utility class defining JSON properties names.
 */
public class JsonFieldNames {

    public static final String ANNOTATED_RELATIONSHIP_ELEMENT_VALUE_ANNOTATION = "annotation";

    public static final String ANNOTATED_RELATIONSHIP_ELEMENT_VALUE_FIRST = "first";
    public static final String ANNOTATED_RELATIONSHIP_ELEMENT_VALUE_SECOND = "second";

    public static final String BLOB_VALUE_CONTENT_TYPE = "contentType";
    public static final String BLOB_VALUE_VALUE = "value";

    public static final String ENTITY_VALUE_ENTITY_TYPE = "entityType";
    public static final String ENTITY_VALUE_GLOBAL_ASSET_ID = "globalAssetId";
    public static final String ENTITY_VALUE_STATEMENTS = "statements";

    public static final String FILE_VALUE_CONTENT_TYPE = "contentType";
    public static final String FILE_VALUE_VALUE = "value";

    public static final String RANGE_VALUE_MAX = "max";
    public static final String RANGE_VALUE_MIN = "min";

    public static final String REFERENCE_ELEMENT_VALUE_TYPE = "type";
    public static final String REFERENCE_ELEMENT_VALUE_VALUE = "value";

    public static final String RELATIONSHIP_ELEMENT_FIRST = "first";
    public static final String RELATIONSHIP_ELEMENT_SECOND = "second";

    public static final String EVENT_MODELTYPE = "modelType";
    public static final String EVENT_VALUE = "value";
    public static final String EVENT_DATATYPE = "dataType";

    private JsonFieldNames() {}
}
