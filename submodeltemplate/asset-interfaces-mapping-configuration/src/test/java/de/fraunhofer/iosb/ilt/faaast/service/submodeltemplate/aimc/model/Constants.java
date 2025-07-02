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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model;

public class Constants {

    public static final String TITLE = "title";
    public static final String AID_ID_SHORT = "AssetInterfacesDescription";
    public static final String AIMC_ID_SHORT = "AssetInterfacesMappingConfiguration";
    public static final String ENDPOINT_METADATA = "EndpointMetadata";
    public static final String SECURITY_DEFINITIONS = "securityDefinitions";
    public static final String NO_SECURITY = "nosec_sc";
    public static final String INTERACTION_METADATA = "InteractionMetadata";
    public static final String INTERFACE_REFERENCE = "InterfaceReference";
    public static final String MAPPING_RELATIONS = "MappingSourceSinkRelations";
    public static final String CONTENT_TYPE = "contentType";
    public static final String PROPERTIES = "properties";
    public static final String TYPE = "type";
    public static final String OBSERVABLE = "observable";
    public static final String UNIT = "unit";
    public static final String FORMS = "forms";
    public static final String HREF = "href";
    public static final String CONTENT_TYPE_JSON = "application/json";;

    public static final String SEMANTIC_ID_TITLE = "https://www.w3.org/2019/wot/td#title";
    public static final String SEMANTIC_ID_CONTENT_TYPE = "https://www.w3.org/2019/wot/hypermedia#forContentType";
    public static final String SEMANTIC_ID_AIMC = "https://admin-shell.io/idta/AssetInterfacesMappingConfiguration/1/0/Submodel";
    public static final String SEMANTIC_ID_ENDPOINT_METADATA = "https://admin-shell.io/idta/AssetInterfacesDescription/1/0/EndpointMetadata";
    public static final String SEMANTIC_ID_INTERFACE = "https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Interface";
    public static final String SEMANTIC_ID_INTERFACE_MQTT = "http://www.w3.org/2011/mqtt";
    public static final String SEMANTIC_ID_INTERFACE_HTTP = "http://www.w3.org/2011/http";
    public static final String SEMANTIC_ID_INTERFACE_TD = "https://www.w3.org/2019/wot/td";
    public static final String SEMANTIC_ID_BASE = "https://www.w3.org/2019/wot/td#base";
    public static final String SEMANTIC_ID_SECURITY = "https://www.w3.org/2019/wot/td#hasSecurityConfiguration";
    public static final String SEMANTIC_ID_SECURITY_DEFINITIONS = "https://www.w3.org/2019/wot/td#definesSecurityScheme";
    public static final String SEMANTIC_ID_NO_SECURITY = "https://www.w3.org/2019/wot/security#NoSecurityScheme";
    public static final String SEMANTIC_ID_SECURITY_SCHEME = "https://www.w3.org/2019/wot/td#definesSecurityScheme";
    public static final String SEMANTIC_ID_MAPPING_CONFIGURATIONS = "https://admin-shell.io/idta/AssetInterfacesMappingConfiguration/1/0/MappingConfigurations";
    public static final String SEMANTIC_ID_MAPPING_CONFIGURATION = "https://admin-shell.io/idta/AssetInterfacesMappingConfiguration/1/0/MappingConfiguration";
    public static final String SEMANTIC_ID_INTERFACE_REFERENCE = "https://admin-shell.io/idta/AssetInterfacesMappingConfiguration/1/0/InterfaceReference";
    public static final String SEMANTIC_ID_MAPPING_RELATIONS = "https://admin-shell.io/idta/AssetInterfacesMappingConfiguration/1/0/MappingSourceSinkRelations";
    public static final String SEMANTIC_ID_MAPPING_RELATION = "https://admin-shell.io/idta/AssetInterfacesMappingConfiguration/1/0/MappingSourceSinkRelation";
    public static final String SEMANTIC_ID_INTERACTION_METADATA = "https://www.w3.org/2019/wot/td#InteractionAffordance";
    public static final String SEMANTIC_ID_PROPERTIES = "https://www.w3.org/2019/wot/td#PropertyAffordance";
    public static final String SEMANTIC_ID_PROPERTY_DEFINITION = "https://admin-shell.io/idta/AssetInterfacesDescription/1/0/PropertyDefinition";
    public static final String SEMANTIC_ID_PROPERTY_DEFINITION_NAME = "https://www.w3.org/2019/wot/td#name";
    public static final String SEMANTIC_ID_PROPERTY_TYPE = "https://www.w3.org/1999/02/22-rdf-syntax-ns#type";
    public static final String SEMANTIC_ID_PROPERTY_OBSERVABLE = "https://www.w3.org/2019/wot/td#isObservable";
    public static final String SEMANTIC_ID_PROPERTY_UNIT = "http://admin-shell.io/DataSpecificationTemplates/DataSpecificationIEC61360/3/0";
    public static final String SEMANTIC_ID_PROPERTY_FORMS = "https://www.w3.org/2019/wot/td#hasForm";
    public static final String SEMANTIC_ID_PROPERTY_HREF = "https://www.w3.org/2019/wot/hypermedia#hasTarget";
}
