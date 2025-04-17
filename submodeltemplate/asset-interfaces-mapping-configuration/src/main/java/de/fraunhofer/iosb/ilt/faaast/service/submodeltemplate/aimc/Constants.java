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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc;

import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Constants related to SMT Asset Interfaces Mapping Configuration.
 */
public class Constants {
    public static final String AID_SUBMODEL_SEMANTIC_ID = "https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Submodel";
    public static final String AIMC_SUBMODEL_SEMANTIC_ID = "https://admin-shell.io/idta/AssetInterfacesMappingConfiguration/1/0/Submodel";
    public static final String AIMC_MAPPING_CONFIGURATIONS = "MappingConfigurations";
    public static final String AIMC_INTERFACE_REFERENCE = "InterfaceReference";
    public static final String AIMC_MAPPING_RELATIONS = "MappingSourceSinkRelations";
    public static final String AID_INTERFACE_SEMANTIC_ID = "https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Interface";
    public static final String AID_INTERFACE_SUPP_SEMANTIC_ID_HTTP = "http://www.w3.org/2011/http";
    public static final String AID_INTERFACE_SUPP_SEMANTIC_ID_MQTT = "http://www.w3.org/2011/mqtt";
    public static final String AID_INTERFACE_TITLE = "title";
    public static final String AID_INTERACTION_METADATA_SEMANTIC_ID = "https://www.w3.org/2019/wot/td#InteractionAffordance";
    public static final String AID_ENDPOINT_METADATA = "EndpointMetadata";
    public static final String AID_METADATA_BASE = "base";
    public static final String AID_METADATA_CONTENT_TYPE = "contentType";
    public static final String AID_METADATA_SECURITY = "security";
    public static final String AID_PROPERTY_FORMS = "forms";
    public static final String AID_PROPERTY_TYPE = "type";
    public static final String AID_PROPERTY_PROPERTIES = "properties";
    public static final String AID_PROPERTY_OBSERVABLE = "observable";
    public static final String AID_PROPERTY_KEY = "key";
    public static final String AID_FORMS_CONTENT_TYPE = "contentType";
    public static final String AID_FORMS_HREF = "href";
    public static final String AID_FORMS_HEADERS = "htv_headers";
    public static final String AID_HEADER_FIELD_NAME = "htv_fieldName";
    public static final String AID_HEADER_FIELD_VALUE = "htv_fieldValue";
    public static final String AID_SECURITY_NOSEC = "nosec_sc";
    public static final String AID_SECURITY_BASIC = "basic_sc";
    public static final String AID_TYPE_OBJECT = "object";
    public static final Reference AID_PROPERTY_ROOT_SEMANTIC_ID = ReferenceBuilder.global("https://admin-shell.io/idta/AssetInterfacesDescription/1/0/PropertyDefinition");
    public static final Reference AID_PROPERTY_NESTED_SEMANTIC_ID = ReferenceBuilder.global("https://www.w3.org/2019/wot/json-schema#propertyName");

    private Constants() {}
}
