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

import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;


public class HttpModel {

    public static final String SUBMODEL_OPER_DATA_ID = "https://www.example.com/ids/sm/6545_8524_6842_7236";
    public static final String OPER_DATA_HTTP = "HttpValues";
    public static final String OPER_DATA_HTTP_P1 = "L2";
    public static final String INTERFACE_HTTP = "InterfaceHTTP";
    public static final String SUBMODEL_AID_ID = "https://example.com/ids/sm/1458_5485_6587_3247";
    public static final String P1_URL = "/L2";
    private static final String SUBMODEL_AIMC_ID = "https://example.com/ids/sm/1234_5458_6587_1975";
    private static final String PROPERTY_1 = "Voltage_L2_N";

    public static Environment create(int httpPort) {
        return new DefaultEnvironment.Builder()
                .assetAdministrationShells(createAas())
                .submodels(createSubmodelAid(httpPort))
                .submodels(createSubmodelAimc())
                .submodels(createSubmodelOperationalData())
                .build();
    }


    private static Submodel createSubmodelAimc() {
        return new DefaultSubmodel.Builder()
                .idShort(Constants.AIMC_ID_SHORT)
                .id(SUBMODEL_AIMC_ID)
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.SUBMODEL)
                                .value(Constants.SEMANTIC_ID_AIMC)
                                .build())
                        .build())
                .submodelElements(createMappingConfiguration())
                .build();

    }


    private static Submodel createSubmodelAid(int httpPort) {
        return new DefaultSubmodel.Builder()
                .idShort(Constants.AID_ID_SHORT)
                .id(SUBMODEL_AID_ID)
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.SUBMODEL)
                                .value("https://admin-shell.io/idta/AssetInterfacesDescription/1/0/Submodel")
                                .build())
                        .build())
                .submodelElements(new DefaultSubmodelElementCollection.Builder()
                        .idShort(INTERFACE_HTTP)
                        .semanticId(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value(Constants.SEMANTIC_ID_INTERFACE)
                                        .build())
                                .build())
                        .supplementalSemanticIds(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value(Constants.SEMANTIC_ID_INTERFACE_HTTP)
                                        .build())
                                .build())
                        .supplementalSemanticIds(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value(Constants.SEMANTIC_ID_INTERFACE_TD)
                                        .build())
                                .build())
                        .value(new DefaultProperty.Builder()
                                .idShort(Constants.TITLE)
                                .semanticId(new DefaultReference.Builder()
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.CONCEPT_DESCRIPTION)
                                                .value(Constants.SEMANTIC_ID_TITLE)
                                                .build())
                                        .build())
                                .build())
                        .value(createEndpointMetadata(httpPort))
                        .value(createInteractionMetadata())
                        .build())
                .build();
    }


    private static Submodel createSubmodelOperationalData() {
        return new DefaultSubmodel.Builder()
                .id(SUBMODEL_OPER_DATA_ID)
                .idShort("OperationalData")
                .submodelElements(new DefaultSubmodelElementCollection.Builder()
                        .idShort(OPER_DATA_HTTP)
                        .value(new DefaultProperty.Builder()
                                .idShort(OPER_DATA_HTTP_P1)
                                .valueType(DataTypeDefXsd.DOUBLE)
                                .value("0")
                                .build())
                        .build())
                .build();
    }


    private static DefaultSubmodelElementList createMappingConfiguration() {
        return new DefaultSubmodelElementList.Builder()
                .idShort("MappingConfigurations")
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.CONCEPT_DESCRIPTION)
                                .value(Constants.SEMANTIC_ID_MAPPING_CONFIGURATIONS)
                                .build())
                        .build())
                .typeValueListElement(AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION)
                .semanticIdListElement(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value(Constants.SEMANTIC_ID_MAPPING_CONFIGURATION)
                                .build())
                        .build())
                .value(new DefaultSubmodelElementCollection.Builder()
                        .semanticId(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value(Constants.SEMANTIC_ID_MAPPING_CONFIGURATION)
                                        .build())
                                .build())
                        .value(new DefaultReferenceElement.Builder()
                                .idShort(Constants.INTERFACE_REFERENCE)
                                .semanticId(new DefaultReference.Builder()
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.GLOBAL_REFERENCE)
                                                .value(Constants.SEMANTIC_ID_INTERFACE_REFERENCE)
                                                .build())
                                        .build())
                                .value(ReferenceBuilder.forSubmodel(SUBMODEL_AID_ID, INTERFACE_HTTP))
                                .build())
                        .value(new DefaultSubmodelElementList.Builder()
                                .idShort(Constants.MAPPING_RELATIONS)
                                .semanticId(new DefaultReference.Builder()
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.GLOBAL_REFERENCE)
                                                .value(Constants.SEMANTIC_ID_MAPPING_RELATIONS)
                                                .build())
                                        .build())
                                .typeValueListElement(AasSubmodelElements.RELATIONSHIP_ELEMENT)
                                .semanticIdListElement(new DefaultReference.Builder()
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.GLOBAL_REFERENCE)
                                                .value(Constants.SEMANTIC_ID_MAPPING_RELATION)
                                                .build())
                                        .build())
                                .value(new DefaultRelationshipElement.Builder()
                                        .semanticId(new DefaultReference.Builder()
                                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                                .keys(new DefaultKey.Builder()
                                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                                        .value(Constants.SEMANTIC_ID_MAPPING_RELATION)
                                                        .build())
                                                .build())
                                        .first(ReferenceBuilder.forSubmodel(SUBMODEL_AID_ID, INTERFACE_HTTP, Constants.INTERACTION_METADATA, Constants.PROPERTIES, PROPERTY_1))
                                        .second(ReferenceBuilder.forSubmodel(SUBMODEL_OPER_DATA_ID, OPER_DATA_HTTP, OPER_DATA_HTTP_P1))
                                        .build())
                                .build())
                        .build())
                .build();
    }


    private static DefaultSubmodelElementCollection createEndpointMetadata(int httpPort) {
        return new DefaultSubmodelElementCollection.Builder()
                .idShort(Constants.ENDPOINT_METADATA)
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.CONCEPT_DESCRIPTION)
                                .value(Constants.SEMANTIC_ID_ENDPOINT_METADATA)
                                .build())
                        .build())
                .value(new DefaultProperty.Builder()
                        .idShort("base")
                        .semanticId(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value(Constants.SEMANTIC_ID_BASE)
                                        .build())
                                .build())
                        .valueType(DataTypeDefXsd.ANY_URI)
                        .value(String.format("http://localhost:%d", httpPort))
                        .build())
                .value(new DefaultProperty.Builder()
                        .idShort(Constants.CONTENT_TYPE)
                        .semanticId(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value(Constants.SEMANTIC_ID_CONTENT_TYPE)
                                        .build())
                                .build())
                        .valueType(DataTypeDefXsd.STRING)
                        .value("application/octet-stream")
                        .build())
                .value(new DefaultSubmodelElementList.Builder()
                        .idShort("security")
                        .semanticId(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.CONCEPT_DESCRIPTION)
                                        .value(Constants.SEMANTIC_ID_SECURITY)
                                        .build())
                                .build())
                        .value(new DefaultReferenceElement.Builder()
                                .value(ReferenceBuilder.forSubmodel(SUBMODEL_AID_ID, INTERFACE_HTTP, Constants.ENDPOINT_METADATA, Constants.SECURITY_DEFINITIONS,
                                        Constants.NO_SECURITY))
                                .build())
                        .typeValueListElement(AasSubmodelElements.SUBMODEL_ELEMENT)
                        .build())
                .value(new DefaultSubmodelElementCollection.Builder()
                        .idShort(Constants.SECURITY_DEFINITIONS)
                        .semanticId(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.CONCEPT_DESCRIPTION)
                                        .value(Constants.SEMANTIC_ID_SECURITY_DEFINITIONS)
                                        .build())
                                .build())
                        .value(new DefaultSubmodelElementCollection.Builder()
                                .idShort(Constants.NO_SECURITY)
                                .semanticId(new DefaultReference.Builder()
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.CONCEPT_DESCRIPTION)
                                                .value(Constants.SEMANTIC_ID_NO_SECURITY)
                                                .build())
                                        .build())
                                .value(new DefaultProperty.Builder()
                                        .idShort("scheme")
                                        .semanticId(new DefaultReference.Builder()
                                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                                .keys(new DefaultKey.Builder()
                                                        .type(KeyTypes.CONCEPT_DESCRIPTION)
                                                        .value(Constants.SEMANTIC_ID_SECURITY_SCHEME)
                                                        .build())
                                                .build())
                                        .valueType(DataTypeDefXsd.STRING)
                                        .value("nosec")
                                        .build())
                                .build())
                        .build())
                .build();
    }


    private static DefaultSubmodelElementCollection createInteractionMetadata() {
        return new DefaultSubmodelElementCollection.Builder()
                .idShort(Constants.INTERACTION_METADATA)
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value(Constants.SEMANTIC_ID_INTERACTION_METADATA)
                                .build())
                        .build())
                .value(new DefaultSubmodelElementCollection.Builder()
                        .idShort(Constants.PROPERTIES)
                        .semanticId(new DefaultReference.Builder()
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.CONCEPT_DESCRIPTION)
                                        .value(Constants.SEMANTIC_ID_PROPERTIES)
                                        .build())
                                .build())
                        .value(new DefaultSubmodelElementCollection.Builder()
                                .idShort(PROPERTY_1)
                                .semanticId(new DefaultReference.Builder()
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.CONCEPT_DESCRIPTION)
                                                .value(Constants.SEMANTIC_ID_PROPERTY_DEFINITION)
                                                .build())
                                        .build())
                                .supplementalSemanticIds(new DefaultReference.Builder()
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.GLOBAL_REFERENCE)
                                                .value(Constants.SEMANTIC_ID_PROPERTY_DEFINITION_NAME)
                                                .build())
                                        .build())
                                .value(new DefaultProperty.Builder()
                                        .idShort(Constants.TITLE)
                                        .semanticId(new DefaultReference.Builder()
                                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                                .keys(new DefaultKey.Builder()
                                                        .type(KeyTypes.CONCEPT_DESCRIPTION)
                                                        .value(Constants.SEMANTIC_ID_TITLE)
                                                        .build())
                                                .build())
                                        .valueType(DataTypeDefXsd.STRING)
                                        .value("Voltage L2 to N")
                                        .build())
                                .value(new DefaultProperty.Builder()
                                        .idShort(Constants.TYPE)
                                        .semanticId(new DefaultReference.Builder()
                                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                                .keys(new DefaultKey.Builder()
                                                        .type(KeyTypes.CONCEPT_DESCRIPTION)
                                                        .value(Constants.SEMANTIC_ID_PROPERTY_TYPE)
                                                        .build())
                                                .build())
                                        .valueType(DataTypeDefXsd.STRING)
                                        .value("double")
                                        .build())
                                .value(new DefaultProperty.Builder()
                                        .idShort(Constants.OBSERVABLE)
                                        .semanticId(new DefaultReference.Builder()
                                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                                .keys(new DefaultKey.Builder()
                                                        .type(KeyTypes.CONCEPT_DESCRIPTION)
                                                        .value(Constants.SEMANTIC_ID_PROPERTY_OBSERVABLE)
                                                        .build())
                                                .build())
                                        .valueType(DataTypeDefXsd.BOOLEAN)
                                        .value("true")
                                        .build())
                                .value(new DefaultProperty.Builder()
                                        .idShort(Constants.UNIT)
                                        .semanticId(new DefaultReference.Builder()
                                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                                .keys(new DefaultKey.Builder()
                                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                                        .value(Constants.SEMANTIC_ID_PROPERTY_UNIT)
                                                        .build())
                                                .build())
                                        .valueType(DataTypeDefXsd.STRING)
                                        .value("V")
                                        .build())
                                .value(new DefaultSubmodelElementCollection.Builder()
                                        .idShort(Constants.FORMS)
                                        .semanticId(new DefaultReference.Builder()
                                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                                .keys(new DefaultKey.Builder()
                                                        .type(KeyTypes.CONCEPT_DESCRIPTION)
                                                        .value(Constants.SEMANTIC_ID_PROPERTY_FORMS)
                                                        .build())
                                                .build())
                                        .value(new DefaultProperty.Builder()
                                                .idShort(Constants.HREF)
                                                .semanticId(new DefaultReference.Builder()
                                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                                        .keys(new DefaultKey.Builder()
                                                                .type(KeyTypes.CONCEPT_DESCRIPTION)
                                                                .value(Constants.SEMANTIC_ID_PROPERTY_HREF)
                                                                .build())
                                                        .build())
                                                .valueType(DataTypeDefXsd.STRING)
                                                .value(P1_URL)
                                                .build())
                                        .value(new DefaultProperty.Builder()
                                                .idShort(Constants.CONTENT_TYPE)
                                                .semanticId(new DefaultReference.Builder()
                                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                                        .keys(new DefaultKey.Builder()
                                                                .type(KeyTypes.CONCEPT_DESCRIPTION)
                                                                .value(Constants.SEMANTIC_ID_CONTENT_TYPE)
                                                                .build())
                                                        .build())
                                                .valueType(DataTypeDefXsd.STRING)
                                                .value(Constants.CONTENT_TYPE_JSON)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build();
    }


    private static AssetAdministrationShell createAas() {
        return new DefaultAssetAdministrationShell.Builder()
                .submodels(ReferenceBuilder.forSubmodel(SUBMODEL_AID_ID))
                .submodels(ReferenceBuilder.forSubmodel(SUBMODEL_AIMC_ID))
                .submodels(ReferenceBuilder.forSubmodel(SUBMODEL_OPER_DATA_ID))
                .build();
    }
}
