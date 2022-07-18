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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper;

/**
 * @author Tino Bischoff
 */
public class TestConstants {

    public static final String AAS_ENVIRONMENT_NAME = "AASEnvironment";
    public static final String SIMPLE_AAS_NAME = "ExampleMotor";
    public static final String SIMPLE_ASSET_NAME = "ServoDCMotor";
    public static final String SUBMODEL_DOC_NODE_NAME = "Documentation";
    public static final String SUBMODEL_TECH_DATA_NODE_NAME = "TechnicalData";
    public static final String SUBMODEL_OPER_DATA_NODE_NAME = "OperationalData";
    public static final String OPERATING_MANUAL_NAME = "OperatingManual";
    public static final String ALLOW_DUPLICATES_NAME = "AllowDuplicates";
    public static final String ROTATION_SPEED_NAME = "RotationSpeed";
    public static final String MAX_ROTATION_SPEED_NAME = "MaxRotationSpeed";
    public static final String SUBMODEL_REF_NAME = "Submodel";
    public static final String SUBMODEL_DOC_NAME = "http://i40.customer.com/type/1/1/1A7B62B529F19152";
    public static final String SUBMODEL_TECH_DATA_NAME = "http://i40.customer.com/type/1/1/7A7104BDAB57E184";
    public static final String SUBMODEL_OPER_DATA_NAME = "http://i40.customer.com/instance/1/1/AC69B1CB44F07935";
    public static final String TEST_PROPERTY_NAME = "TestProperty";
    public static final String TEST_RANGE_NAME = "TestRange";
    public static final String RANGE_MIN_NAME = "Min";
    public static final String RANGE_MAX_NAME = "Max";
    public static final String TEST_BLOB_NAME = "ExampleBlob";
    public static final String TEST_MULTI_LAN_PROP_NAME = "ExampleMultiLanguageProperty";
    public static final String TEST_REF_ELEM_NAME = "ExampleReferenceElement";
    public static final String TEST_ENTITY_NAME = "ExampleEntity";
    public static final String TEST_ENTITY_PROPERTY_NAME = "ExampleProperty";
    public static final String DECIMAL_PROPERTY = "DecimalProperty";
    public static final String SUBMODEL_DOC_PROPERTY_TITLE_NAME = "Title";
    public static final String SUBMODEL_DOC_FILE_NAME = "DigitalFile_PDF";

    public static final String FULL_SUBMODEL_1_NAME = "Identification";
    public static final String FULL_SUBMODEL_1_ID = "http://acplt.org/Submodels/Assets/TestAsset/Identification";
    public static final String FULL_SUBMODEL_2_NAME = "BillOfMaterial";
    public static final String FULL_SUBMODEL_3_NAME = "TestSubmodel3";
    public static final String FULL_SUBMODEL_4_NAME = "Test_Submodel_Mandatory";
    public static final String FULL_SUBMODEL_5_NAME = "Test_Submodel2_Mandatory";
    public static final String FULL_SUBMODEL_6_NAME = "TestSubmodel6";
    public static final String FULL_SUBMODEL_7_NAME = "TestSubmodel7";
    public static final String FULL_REL_ELEMENT_NAME = "ExampleRelationshipElement";
    public static final String FULL_SM_ELEM_COLL_UO_NAME = "ExampleSubmodelCollectionUnordered";
    public static final String FULL_SM_ELEM_COLL_O_NAME = "ExampleSubmodelCollectionOrdered";
    public static final String FULL_SMEC_REL_ELEM_NAME = "ExampleReferenceElement";
    public static final String FULL_SMEC_RANGE_NAME = "ExampleRange";
    public static final String FULL_OPERATION_NAME = "ExampleOperation";
    public static final String FULL_ENTITY2_NAME = "ExampleEntity2";
    public static final String FULL_CAPABILITY_NAME = "ExampleCapability";
    public static final String FULL_DATETIME_PROP_NAME = "DateTimeProperty";

    public static final String MODELING_KIND_NAME = "ModelingKind";
    public static final String CATEGORY_NAME = "Category";
    public static final String IDENTIFICATION_NAME = "Identification";
    public static final String ADMINISTRATION_NAME = "Administration";
    public static final String DATA_SPECIFICATION_NAME = "DataSpecification";
    public static final String QUALIFIER_NAME = "Qualifier";
    public static final String PROPERTY_VALUE_NAME = "Value";
    public static final String PROPERTY_VALUE_TYPE_NAME = "ValueType";
    public static final String PROPERTY_MIME_TYPE_NAME = "MimeType";
    public static final String PROPERTY_FILE_NAME = "File";
    public static final String PROPERTY_SIZE_NAME = "Size";
    public static final String VERSION_NAME = "Version";
    public static final String REVISION_NAME = "Revision";
    public static final String ASSET_INFORMATION_NAME = "AssetInformation";
    public static final String ASSET_KIND_NAME = "AssetKind";
    public static final String BILL_OF_MATERIAL_NAME = "BillOfMaterial";
    public static final String DEFAULT_THUMB_NAME = "DefaultThumbnail";
    public static final String GLOBAL_ASSET_ID_NAME = "GlobalAssetId";
    public static final String SPECIFIC_ASSET_ID_NAME = "SpecificAssetId";
    public static final String ID_KEY_NAME = "Key";
    public static final String ID_VALUE_NAME = "Value";
    public static final String KEYS_VALUE_NAME = "Keys";

    public static final int AAS_AAS_TYPE_ID = 1002;
    public static final int AAS_REFERENCE_TYPE_ID = 1004;
    public static final int AAS_ASSET_TYPE_ID = 1005;
    public static final int AAS_SUBMODEL_TYPE_ID = 1006;
    public static final int AAS_SUBMODEL_ELEM_COLL_TYPE_ID = 1010;
    public static final int AAS_OREDER_SM_ELEM_COLL_TYPE_ID = 1011;
    public static final int AAS_PROPERTY_TYPE_ID = 1013;
    public static final int AAS_IDENTIFIER_TYPE_ID = 1029;
    public static final int AAS_ADMIN_INFO_TYPE_ID = 1030;
    public static final int AAS_ASSET_INFO_TYPE_ID = 1031;
    public static final int AAS_QUALIFIER_TYPE_ID = 1032;
    public static final int AAS_ID_KEY_VALUE_PAIR_ID = 1035;
    public static final int AAS_REFERENCE_LIST_ID = 1036;
    public static final int AAS_QUALIFIER_LIST_ID = 1037;
    public static final int AAS_ID_KEY_VALUE_PAIR_LIST_ID = 1039;
    public static final int AAS_KEY_DATA_TYPE_ID = 3011;
}
