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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper;

/**
 * Utility class for query parameters used in HTTP requests.
 */
public class QueryParameters {

    public static final String AAS_ID = "aasId";
    public static final String AAS_IDS = "aasIds";
    public static final String ASSET_IDS = "assetIds";
    public static final String ASYNC = "async";
    public static final String DATA_SPECIFICATION_REF = "dataSpecificationRef";
    public static final String EXTENT = "extend";
    public static final String ID_SHORT = "idShort";
    public static final String INCLUDE_CONCEPT_DESCRIPTIONS = "includeConceptDescriptions";
    public static final String IS_CASE_OF = "isCaseOf";
    public static final String LEVEL = "level";
    public static final String PARENT_PATH = "parentPath";
    public static final String SEMANTIC_ID = "semanticId";
    public static final String SUBMODEL_IDS = "submodelIds";

    private QueryParameters() {}
}
