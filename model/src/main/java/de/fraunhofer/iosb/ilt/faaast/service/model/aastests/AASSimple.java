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
package de.fraunhofer.iosb.ilt.faaast.service.model.aastests;

import io.adminshell.aas.v3.model.Asset;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.AssetKind;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.DataTypeIEC61360;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.impl.DefaultAdministrativeInformation;
import io.adminshell.aas.v3.model.impl.DefaultAsset;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShell;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultAssetInformation;
import io.adminshell.aas.v3.model.impl.DefaultConceptDescription;
import io.adminshell.aas.v3.model.impl.DefaultDataSpecificationIEC61360;
import io.adminshell.aas.v3.model.impl.DefaultEmbeddedDataSpecification;
import io.adminshell.aas.v3.model.impl.DefaultFile;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultIdentifierKeyValuePair;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.util.Arrays;


public class AASSimple {

    private static final String DOCUMENT_DEF = "Feste und geordnete Menge von für die Verwendung durch Personen bestimmte Informationen, die verwaltet und als Einheit zwischen Benutzern und System ausgetauscht werden kann.";
    private static final String ISO15519_1_2010 = "[ISO15519-1:2010]";
    private static final String DOKUMENT = "Dokument";
    private static final String DOCUMENT = "Document";
    private static final String WWW_VDI2770_COM_BLATT1_ENTWURF_OKT18_CD_DOCUMENT = "www.vdi2770.com/blatt1/Entwurf/Okt18/cd/Document";
    private static final String ACTUAL_ROTATIONSPEED_WITH_WHICH_THE_MOTOR_OR_FEEDINGUNIT_IS_OPERATED = "Actual rotationspeed with which the motor or feedingunit is operated";
    private static final String AKTUELLE_DREHZAHL_MITWELCHER_DER_MOTOR_ODER_DIE_SPEISEINHEIT_BETRIEBEN_WIRD = "Aktuelle Drehzahl, mitwelcher der Motor oder die Speiseinheit betrieben wird";
    private static final String ACTUAL_ROTATION_SPEED = "ActualRotationSpeed";
    private static final String ACTUALROTATIONSPEED = "Actualrotationspeed";
    private static final String AKTUELLE_DREHZAHL = "AktuelleDrehzahl";
    private static final String _1_MIN = "1/min";
    private static final String HTTP_CUSTOMER_COM_CD_1_1_18EBD56F6B43D895 = "http://customer.com/cd/1/1/18EBD56F6B43D895";
    private static final String ROTATION_SPEED = "RotationSpeed";
    private static final String MAX_ROTATE_DEF_EN = "Greatestpermissiblerotationspeedwithwhichthemotororfeedingunitmaybeoperated";
    private static final String MAX_ROTATE_DEF_DE = "HöchstezulässigeDrehzahl,mitwelcherderMotoroderdieSpeiseinheitbetriebenwerdendarf";
    private static final String _0173_1_05_AAA650_002 = "0173-1#05-AAA650#002";
    private static final String MAX_ROTATIONSPEED = "Max.rotationspeed";
    private static final String MAX_DREHZAHL = "max.Drehzahl";
    private static final String _0173_1_02_BAA120_008 = "0173-1#02-BAA120#008";
    private static final String PROPERTY = "PROPERTY";
    private static final String MAX_ROTATION_SPEED = "MaxRotationSpeed";
    private static final String DIGITAL_FILE_DEFINITION = "A file representing the document version. In addition to the mandatory PDF file, other files can be specified."; //"Eine Datei, die die Document Version repräsentiert. Neben der obligatorischen PDF Datei können weitere Dateien angegeben werden.";
    private static final String DIGITALE_DATEI = "DigitaleDatei";
    private static final String WWW_VDI2770_COM_BLATT1_ENTWURF_OKT18_CD_STORED_DOCUMENT_REPRESENTATION_DIGITAL_FILE = "www.vdi2770.com/blatt1/Entwurf/Okt18/cd/StoredDocumentRepresentation/DigitalFile";
    private static final String DIGITAL_FILE = "DigitalFile";
    private static final String SPRACHABHÄNGIGER_TITELDES_DOKUMENTS = "SprachabhängigerTiteldesDokuments.";
    private static final String TITEL = "Titel";
    private static final String WWW_VDI2770_COM_BLATT1_ENTWURF_OKT18_CD_DESCRIPTION_TITLE = "www.vdi2770.com/blatt1/Entwurf/Okt18/cd/Description/Title";
    private static final String TITLE = "Title";
    private static final String SERVO_DC_MOTOR = "ServoDCMotor";
    private static final String HTTPS_GITHUB_COM_ADMIN_SHELL_IO_BLOB_MASTER_VERWALTUNGSSCHALE_DETAIL_PART1_PNG = "https://github.com/admin-shell/io/blob/master/verwaltungsschale-detail-part1.png";
    private static final String IMAGE_PNG = "image/png";
    private static final String THUMBNAIL = "thumbnail";
    private static final String HTTP_CUSTOMER_COM_SYSTEMS_IO_T_1 = "http://customer.com/Systems/IoT/1";
    private static final String QJ_YG_PGGJWKI_HK4_RR_QI_YS_LG = "QjYgPggjwkiHk4RrQiYSLg==";
    private static final String DEVICE_ID = "DeviceID";
    private static final String HTTP_CUSTOMER_COM_SYSTEMS_ERP_012 = "http://customer.com/Systems/ERP/012";
    private static final String _538FD1B3_F99F_4A52_9C75_72E9FA921270 = "538fd1b3-f99f-4a52-9c75-72e9fa921270";
    private static final String EQUIPMENT_ID = "EquipmentID";
    private static final String HTTP_CUSTOMER_COM_ASSETS_KHBVZJSQKIY = "http://customer.com/assets/KHBVZJSQKIY";
    // AAS
    public static final String AAS_ID = "ExampleMotor";
    public static final String AAS_IDENTIFIER = "http://customer.com/aas/9175_7013_7091_9168";

    // SUBMODEL_TECHNICAL_DATA
    public static final String SUBMODEL_TECHNICAL_DATA_PROPERTY_ID_SHORT = MAX_ROTATION_SPEED;
    public static final String SUBMODEL_TECHNICAL_DATA_ID_SHORT = "TechnicalData";
    public static final String SUBMODEL_TECHNICAL_DATA_ID = "http://i40.customer.com/type/1/1/7A7104BDAB57E184";
    public static final String SUBMODEL_TECHNICAL_DATA_SEMANTIC_ID = "0173-1#01-AFZ615#016";
    public static final String SUBMODEL_TECHNICAL_DATA_SEMANTIC_ID_PROPERTY = _0173_1_02_BAA120_008;
    public static final String SUBMODEL_TECHNICAL_DATA_PROPERTY_CATEGORY = "Parameter";
    public static final String SUBMODEL_TECHNICAL_DATA_PROPERTY_VALUE = "5000";
    public static final String SUBMODEL_TECHNICAL_DATA_PROPERTY_VALUETYPE = "integer";

    // SUBMODEL_DOCUMENTATION
    private static final String SUBMODEL_DOCUMENTATION_ID_SHORT = "Documentation";
    private static final String SUBMODEL_DOCUMENTATION_ID = "http://i40.customer.com/type/1/1/1A7B62B529F19152";
    private static final String SUBMODEL_DOCUMENTATION_ELEMENTCOLLECTION_SEMANTIC_ID = WWW_VDI2770_COM_BLATT1_ENTWURF_OKT18_CD_DOCUMENT;
    private static final String SUBMODEL_DOCUMENTATION_ELEMENTCOLLECTION_ID_SHORT = "OperatingManual";
    private static final String SUBMODEL_DOCUMENTATION_PROPERTY_SEMANTIC_ID = WWW_VDI2770_COM_BLATT1_ENTWURF_OKT18_CD_DESCRIPTION_TITLE;
    private static final String SUBMODEL_DOCUMENTATION_PROPERTY_ID_SHORT = TITLE;
    private static final String SUBMODEL_DOCUMENTATION_PROPERTY_VALUE = "OperatingManual";
    private static final String SUBMODEL_DOCUMENTATION_PROPERTY_VALUETYPE = "langString";
    private static final String SUBMODEL_DOCUMENTATION_FILE_SEMANTIC_ID = WWW_VDI2770_COM_BLATT1_ENTWURF_OKT18_CD_STORED_DOCUMENT_REPRESENTATION_DIGITAL_FILE;
    private static final String SUBMODEL_DOCUMENTATION_FILE_ID_SHORT = "DigitalFile_PDF";
    private static final String SUBMODEL_DOCUMENTATION_FILE_MIMETYPE = "application/pdf";
    private static final String SUBMODEL_DOCUMENTATION_FILE_VALUE = "/aasx/OperatingManual.pdf";

    // SUBMODEL_OPERATIONAL_DATA
    private static final String SUBMODEL_OPERATIONAL_DATA_ID_SHORT = "OperationalData";
    private static final String SUBMODEL_OPERATIONAL_DATA_ID = "http://i40.customer.com/instance/1/1/AC69B1CB44F07935";
    private static final String SUBMODEL_OPERATIONAL_DATA_SEMANTIC_ID_PROPERTY = HTTP_CUSTOMER_COM_CD_1_1_18EBD56F6B43D895;
    private static final String SUBMODEL_OPERATIONAL_DATA_PROPERTY_ID_SHORT = ROTATION_SPEED;
    private static final String SUBMODEL_OPERATIONAL_DATA_PROPERTY_CATEGORY = "Variable";
    private static final String SUBMODEL_OPERATIONAL_DATA_PROPERTY_VALUE = "4370";
    private static final String SUBMODEL_OPERATIONAL_DATA_PROPERTY_VALUETYPE = "integer";

    public AASSimple() {}

    public static final AssetAdministrationShell AAS = createAAS();
    public static final Asset ASSET = createAsset();
    public static final Submodel SUBMODEL_TECHNICAL_DATA = createSubmodelTechnicalData();
    public static final Submodel SUBMODEL_OPERATIONAL_DATA = createSubmodelOperationalData();
    public static final Submodel SUBMODEL_DOCUMENTATION = createSubmodelDocumentation();
    public static final ConceptDescription CONCEPT_DESCRIPTION_TITLE = createConceptDescriptionTitle();
    public static final ConceptDescription CONCEPT_DESCRIPTION_DIGITALFILE = createConceptDescriptionDigitalFile();
    public static final ConceptDescription CONCEPT_DESCRIPTION_MAXROTATIONSPEED = createConceptDescriptionMaxRotationSpeed();
    public static final ConceptDescription CONCEPT_DESCRIPTION_ROTATIONSPEED = createConceptDescriptionRotationSpeed();
    public static final ConceptDescription CONCEPT_DESCRIPTION_DOCUMENT = createConceptDescriptionDocument();
    public static final AssetAdministrationShellEnvironment ENVIRONMENT = createEnvironment();

    public static AssetAdministrationShell createAAS() {
        return new DefaultAssetAdministrationShell.Builder()
                .idShort(AAS_ID)
                .identification(
                        new DefaultIdentifier.Builder()
                                .idType(IdentifierType.IRI)
                                .identifier(AAS_IDENTIFIER)
                                .build())
                .assetInformation(new DefaultAssetInformation.Builder()
                        .assetKind(AssetKind.INSTANCE)
                        .globalAssetId(new DefaultReference.Builder()
                                .key(new DefaultKey.Builder()
                                        .type(KeyElements.ASSET)
                                        .value(HTTP_CUSTOMER_COM_ASSETS_KHBVZJSQKIY)
                                        .idType(KeyType.IRI)
                                        .build())
                                .build())
                        .specificAssetId(new DefaultIdentifierKeyValuePair.Builder()
                                .key(EQUIPMENT_ID)
                                .value(_538FD1B3_F99F_4A52_9C75_72E9FA921270)
                                .externalSubjectId(new DefaultReference.Builder()
                                        .key(new DefaultKey.Builder()
                                                .type(KeyElements.GLOBAL_REFERENCE)
                                                .value(HTTP_CUSTOMER_COM_SYSTEMS_ERP_012)
                                                .idType(KeyType.IRI)
                                                .build())
                                        .build())
                                .build())
                        .specificAssetId(new DefaultIdentifierKeyValuePair.Builder()
                                .key(DEVICE_ID)
                                .value(QJ_YG_PGGJWKI_HK4_RR_QI_YS_LG)
                                .externalSubjectId(new DefaultReference.Builder()
                                        .key(new DefaultKey.Builder()
                                                .type(KeyElements.GLOBAL_REFERENCE)
                                                .value(HTTP_CUSTOMER_COM_SYSTEMS_IO_T_1)
                                                .idType(KeyType.IRI)
                                                .build())
                                        .build())
                                .build())
                        .defaultThumbnail(new DefaultFile.Builder()
                                .kind(ModelingKind.INSTANCE)
                                .idShort(THUMBNAIL)
                                .mimeType(IMAGE_PNG)
                                .value(HTTPS_GITHUB_COM_ADMIN_SHELL_IO_BLOB_MASTER_VERWALTUNGSSCHALE_DETAIL_PART1_PNG)
                                .build())
                        .build())
                .submodel(new DefaultReference.Builder()
                        .key(new DefaultKey.Builder()
                                .type(KeyElements.SUBMODEL)
                                .value(SUBMODEL_TECHNICAL_DATA_ID)
                                .idType(KeyType.IRI).build())
                        .build())
                .submodel(
                        new DefaultReference.Builder()
                                .key(new DefaultKey.Builder()
                                        .type(KeyElements.SUBMODEL)
                                        .value(SUBMODEL_OPERATIONAL_DATA_ID)
                                        .idType(KeyType.IRI)
                                        .build())
                                .build())
                .submodel(new DefaultReference.Builder().key(new DefaultKey.Builder()
                        .type(KeyElements.SUBMODEL)
                        .value(SUBMODEL_DOCUMENTATION_ID)
                        .idType(KeyType.IRI)
                        .build())
                        .build())
                .build();
    }


    public static Asset createAsset() {
        return new DefaultAsset.Builder().idShort(SERVO_DC_MOTOR)
                .identification(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.IRI)
                        .identifier(HTTP_CUSTOMER_COM_ASSETS_KHBVZJSQKIY)
                        .build())
                .build();
    }


    public static Submodel createSubmodelTechnicalData() {
        return new DefaultSubmodel.Builder()
                .semanticId(new DefaultReference.Builder()
                        .key(new DefaultKey.Builder()
                                .type(KeyElements.GLOBAL_REFERENCE)
                                .value(SUBMODEL_TECHNICAL_DATA_SEMANTIC_ID)
                                .idType(KeyType.IRDI)
                                .build())
                        .build())
                .kind(ModelingKind.INSTANCE)
                .idShort(SUBMODEL_TECHNICAL_DATA_ID_SHORT)
                .identification(new DefaultIdentifier.Builder()
                        .identifier(SUBMODEL_TECHNICAL_DATA_ID)
                        .idType(IdentifierType.IRI)
                        .build())
                .submodelElement(new DefaultProperty.Builder()
                        .kind(ModelingKind.INSTANCE)
                        .semanticId(new DefaultReference.Builder()
                                .key(new DefaultKey.Builder()
                                        .type(KeyElements.CONCEPT_DESCRIPTION)
                                        .value(SUBMODEL_TECHNICAL_DATA_SEMANTIC_ID_PROPERTY)
                                        .idType(KeyType.IRDI)
                                        .build())
                                .build())
                        .idShort(SUBMODEL_TECHNICAL_DATA_PROPERTY_ID_SHORT)
                        .category(SUBMODEL_TECHNICAL_DATA_PROPERTY_CATEGORY)
                        .value(SUBMODEL_TECHNICAL_DATA_PROPERTY_VALUE)
                        .valueType(SUBMODEL_TECHNICAL_DATA_PROPERTY_VALUETYPE)
                        .build())
                .build();
    }


    public static Submodel createSubmodelOperationalData() {
        return new DefaultSubmodel.Builder()
                .kind(ModelingKind.INSTANCE)
                .idShort(SUBMODEL_OPERATIONAL_DATA_ID_SHORT)
                .identification(new DefaultIdentifier.Builder()
                        .identifier(SUBMODEL_OPERATIONAL_DATA_ID)
                        .idType(IdentifierType.IRI)
                        .build())
                .submodelElement(new DefaultProperty.Builder()
                        .kind(ModelingKind.INSTANCE)
                        .semanticId(new DefaultReference.Builder()
                                .key(new DefaultKey.Builder()
                                        .type(KeyElements.CONCEPT_DESCRIPTION)
                                        .value(SUBMODEL_OPERATIONAL_DATA_SEMANTIC_ID_PROPERTY)
                                        .idType(KeyType.IRI)
                                        .build())
                                .build())
                        .idShort(SUBMODEL_OPERATIONAL_DATA_PROPERTY_ID_SHORT)
                        .category(SUBMODEL_OPERATIONAL_DATA_PROPERTY_CATEGORY)
                        .value(SUBMODEL_OPERATIONAL_DATA_PROPERTY_VALUE)
                        .valueType(SUBMODEL_OPERATIONAL_DATA_PROPERTY_VALUETYPE)
                        .build())
                .build();
    }


    public static Submodel createSubmodelDocumentation() {
        return new DefaultSubmodel.Builder()
                .kind(ModelingKind.INSTANCE)
                .idShort(SUBMODEL_DOCUMENTATION_ID_SHORT)
                .identification(new DefaultIdentifier.Builder()
                        .identifier(SUBMODEL_DOCUMENTATION_ID)
                        .idType(IdentifierType.IRI)
                        .build())
                .submodelElement(new DefaultSubmodelElementCollection.Builder()
                        .kind(ModelingKind.INSTANCE)
                        .semanticId(new DefaultReference.Builder()
                                .key(new DefaultKey.Builder()
                                        .type(KeyElements.CONCEPT_DESCRIPTION)
                                        .value(SUBMODEL_DOCUMENTATION_ELEMENTCOLLECTION_SEMANTIC_ID)
                                        .idType(KeyType.IRI)
                                        .build())
                                .build())
                        .idShort(SUBMODEL_DOCUMENTATION_ELEMENTCOLLECTION_ID_SHORT)
                        .value(new DefaultProperty.Builder()
                                .kind(ModelingKind.INSTANCE)
                                .semanticId(new DefaultReference.Builder()
                                        .key(new DefaultKey.Builder()
                                                .type(KeyElements.CONCEPT_DESCRIPTION)
                                                .value(SUBMODEL_DOCUMENTATION_PROPERTY_SEMANTIC_ID)
                                                .idType(KeyType.IRI)
                                                .build())
                                        .build())
                                .idShort(SUBMODEL_DOCUMENTATION_PROPERTY_ID_SHORT)
                                .value(SUBMODEL_DOCUMENTATION_PROPERTY_VALUE)
                                .valueType(SUBMODEL_DOCUMENTATION_PROPERTY_VALUETYPE)
                                .build())
                        .value(new DefaultFile.Builder()
                                .kind(ModelingKind.INSTANCE)
                                .semanticId(new DefaultReference.Builder()
                                        .key(new DefaultKey.Builder()
                                                .type(KeyElements.CONCEPT_DESCRIPTION)
                                                .value(SUBMODEL_DOCUMENTATION_FILE_SEMANTIC_ID)
                                                .idType(KeyType.IRI)
                                                .build())
                                        .build())
                                .idShort(SUBMODEL_DOCUMENTATION_FILE_ID_SHORT)
                                .mimeType(SUBMODEL_DOCUMENTATION_FILE_MIMETYPE)
                                .value(SUBMODEL_DOCUMENTATION_FILE_VALUE)
                                .build())
                        .ordered(false)
                        .allowDuplicates(false)
                        .build())
                .build();
    }


    public static ConceptDescription createConceptDescriptionTitle() {
        return new DefaultConceptDescription.Builder()
                .idShort(TITLE)
                .identification(new DefaultIdentifier.Builder()
                        .identifier(WWW_VDI2770_COM_BLATT1_ENTWURF_OKT18_CD_DESCRIPTION_TITLE)
                        .idType(IdentifierType.IRI)
                        .build())
                .embeddedDataSpecification(new DefaultEmbeddedDataSpecification.Builder()
                        .dataSpecificationContent(new DefaultDataSpecificationIEC61360.Builder()
                                .preferredName(new LangString(TITLE, "EN"))
                                .preferredName(new LangString(TITEL, "DE"))
                                .shortName(new LangString(TITLE, "EN"))
                                .shortName(new LangString(TITEL, "DE"))
                                .unit("ExampleString")
                                .sourceOfDefinition("ExampleString")
                                .dataType(DataTypeIEC61360.STRING_TRANSLATABLE)
                                .definition(new LangString(SPRACHABHÄNGIGER_TITELDES_DOKUMENTS, "EN"))
                                .build())
                        .build())
                .build();
    }


    public static ConceptDescription createConceptDescriptionDigitalFile() {
        return new DefaultConceptDescription.Builder()
                .idShort(DIGITAL_FILE)
                .identification(new DefaultIdentifier.Builder()
                        .identifier(WWW_VDI2770_COM_BLATT1_ENTWURF_OKT18_CD_STORED_DOCUMENT_REPRESENTATION_DIGITAL_FILE)
                        .idType(IdentifierType.IRI)
                        .build())
                .embeddedDataSpecification(new DefaultEmbeddedDataSpecification.Builder()
                        .dataSpecificationContent(
                                new DefaultDataSpecificationIEC61360.Builder()
                                        .preferredName(new LangString(DIGITAL_FILE, "EN"))
                                        .preferredName(new LangString(DIGITAL_FILE, "EN"))
                                        .shortName(new LangString(DIGITAL_FILE, "EN"))
                                        .shortName(new LangString(DIGITALE_DATEI, "DE"))
                                        .unit("ExampleString")
                                        .sourceOfDefinition("ExampleString")
                                        .dataType(DataTypeIEC61360.STRING)
                                        .definition(new LangString(DIGITAL_FILE_DEFINITION, "EN"))
                                        .build())
                        .build())
                .build();
    }


    public static ConceptDescription createConceptDescriptionMaxRotationSpeed() {
        return new DefaultConceptDescription.Builder()
                .idShort(MAX_ROTATION_SPEED).category(PROPERTY)
                .administration(new DefaultAdministrativeInformation.Builder()
                        .version("2")
                        .revision("2.1")
                        .build())
                .identification(new DefaultIdentifier.Builder()
                        .identifier(_0173_1_02_BAA120_008)
                        .idType(IdentifierType.IRDI)
                        .build())
                .embeddedDataSpecifications(
                        Arrays.asList(
                                new DefaultEmbeddedDataSpecification.Builder()
                                        .dataSpecificationContent(new DefaultDataSpecificationIEC61360.Builder()
                                                .preferredName(new LangString(MAX_DREHZAHL, "de"))
                                                .preferredName(new LangString(MAX_ROTATIONSPEED, "en"))
                                                .unit(_1_MIN)
                                                .unitId(new DefaultReference.Builder()
                                                        .key(new DefaultKey.Builder()
                                                                .type(KeyElements.GLOBAL_REFERENCE)
                                                                .value(_0173_1_05_AAA650_002)
                                                                .idType(KeyType.IRDI)
                                                                .build())
                                                        .build())
                                                .sourceOfDefinition("ExampleString")
                                                .dataType(DataTypeIEC61360.REAL_MEASURE)
                                                .definition(new LangString(MAX_ROTATE_DEF_DE, "de"))
                                                .definition(new LangString(MAX_ROTATE_DEF_EN, "EN"))
                                                .build())
                                        .build()))
                .build();
    }


    public static ConceptDescription createConceptDescriptionRotationSpeed() {
        return new DefaultConceptDescription.Builder()
                .idShort(ROTATION_SPEED)
                .category(PROPERTY)
                .identification(new DefaultIdentifier.Builder()
                        .identifier(HTTP_CUSTOMER_COM_CD_1_1_18EBD56F6B43D895)
                        .idType(IdentifierType.IRI)
                        .build())
                .embeddedDataSpecification(
                        new DefaultEmbeddedDataSpecification.Builder()
                                .dataSpecificationContent(
                                        new DefaultDataSpecificationIEC61360.Builder()
                                                .preferredName(new LangString(AKTUELLE_DREHZAHL, "DE"))
                                                .preferredName(new LangString(ACTUALROTATIONSPEED, "EN"))
                                                .shortName(new LangString(AKTUELLE_DREHZAHL, "DE"))
                                                .shortName(new LangString(ACTUAL_ROTATION_SPEED, "EN"))
                                                .unit(_1_MIN)
                                                .unitId(new DefaultReference.Builder()
                                                        .key(new DefaultKey.Builder()
                                                                .type(KeyElements.GLOBAL_REFERENCE)
                                                                .value(_0173_1_05_AAA650_002)
                                                                .idType(KeyType.IRDI)
                                                                .build())
                                                        .build())
                                                .sourceOfDefinition("ExampleString")
                                                .dataType(DataTypeIEC61360.REAL_MEASURE)
                                                .definition(new LangString(AKTUELLE_DREHZAHL_MITWELCHER_DER_MOTOR_ODER_DIE_SPEISEINHEIT_BETRIEBEN_WIRD, "DE"))
                                                .definition(new LangString(ACTUAL_ROTATIONSPEED_WITH_WHICH_THE_MOTOR_OR_FEEDINGUNIT_IS_OPERATED, "EN"))
                                                .build())
                                .build())
                .build();
    }


    public static ConceptDescription createConceptDescriptionDocument() {
        return new DefaultConceptDescription.Builder()
                .idShort(DOCUMENT)
                .identification(new DefaultIdentifier.Builder()
                        .identifier(WWW_VDI2770_COM_BLATT1_ENTWURF_OKT18_CD_DOCUMENT)
                        .idType(IdentifierType.IRI)
                        .build())
                .embeddedDataSpecification(new DefaultEmbeddedDataSpecification.Builder()
                        .dataSpecificationContent(new DefaultDataSpecificationIEC61360.Builder()
                                .preferredName(new LangString(DOCUMENT, "EN"))
                                .shortName(new LangString(DOCUMENT, "EN"))
                                .shortName(new LangString(DOKUMENT, "DE"))
                                .unit("ExampleString")
                                .sourceOfDefinition(ISO15519_1_2010)
                                .dataType(DataTypeIEC61360.STRING)
                                .definition(new LangString(DOCUMENT_DEF, "EN"))
                                .build())
                        .build())
                .build();
    }


    public static AssetAdministrationShellEnvironment createEnvironment() {
        return new DefaultAssetAdministrationShellEnvironment.Builder()
                .assetAdministrationShells(createAAS())
                .submodels(createSubmodelTechnicalData())
                .submodels(createSubmodelDocumentation())
                .submodels(createSubmodelOperationalData())
                .conceptDescriptions(createConceptDescriptionTitle())
                .conceptDescriptions(createConceptDescriptionDigitalFile())
                .conceptDescriptions(createConceptDescriptionMaxRotationSpeed())
                .conceptDescriptions(createConceptDescriptionRotationSpeed())
                .conceptDescriptions(createConceptDescriptionDocument())
                .assets(createAsset())
                .build();
    }
}