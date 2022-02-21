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
package opc.i4aas;

import com.prosysopc.ua.stack.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;


class VariableIdsInit {
    static ExpandedNodeId initAASAssetKindDataType_EnumValues() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6099L));
    }


    static ExpandedNodeId initAASCategoryDataType_EnumValues() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6109L));
    }


    static ExpandedNodeId initAASDataTypeIEC61360DataType_EnumStrings() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6111L));
    }


    static ExpandedNodeId initAASEntityTypeDataType_EnumValues() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6103L));
    }


    static ExpandedNodeId initAASIdentifierTypeDataType_EnumValues() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6093L));
    }


    static ExpandedNodeId initAASKeyElementsDataType_EnumValues() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6101L));
    }


    static ExpandedNodeId initAASKeyTypeDataType_EnumValues() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6108L));
    }


    static ExpandedNodeId initAASLevelTypeDataType_EnumValues() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6102L));
    }


    static ExpandedNodeId initAASModelingKindDataType_EnumValues() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6125L));
    }


    static ExpandedNodeId initAASValueTypeDataType_EnumStrings() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6110L));
    }


    static ExpandedNodeId initAASKeyDataType_DefaultXml_AASKeyDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6100L));
    }


    static ExpandedNodeId initAASKeyDataType_DefaultBinary_AASKeyDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6098L));
    }


    static ExpandedNodeId initAASAdministrativeInformationType_Revision() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6084L));
    }


    static ExpandedNodeId initAASAdministrativeInformationType_Version() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6083L));
    }


    static ExpandedNodeId initAASAssetInformationType_DefaultThumbnail_MimeType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6444L));
    }


    static ExpandedNodeId initAASAssetInformationType_DefaultThumbnail_ModelingKind() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6446L));
    }


    static ExpandedNodeId initAASAssetInformationType_GlobalAssetId_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6442L));
    }


    static ExpandedNodeId initAASAssetInformationType_DefaultThumbnail_Category() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6445L));
    }


    static ExpandedNodeId initAASAssetInformationType_AssetKind() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6441L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_DataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6072L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_Value() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6071L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_Identification_IdType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6087L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_ValueId_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6077L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_PreferredName() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6074L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_UnitId_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6076L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_SourceOfDefinition() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6067L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_DefaultInstanceBrowseName() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6063L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_ValueFormat() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6070L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_ShortName() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6066L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_Symbol() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6068L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_Identification_Id() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6088L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_Definition() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6073L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_LevelType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6075L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_Unit() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6069L));
    }


    static ExpandedNodeId initAASIdentifierKeyValuePairType_Key() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6447L));
    }


    static ExpandedNodeId initAASIdentifierKeyValuePairType_Value() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6448L));
    }


    static ExpandedNodeId initAASIdentifierKeyValuePairType_ExternalSubjectId_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6449L));
    }


    static ExpandedNodeId initAASIdentifierType_IdType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6085L));
    }


    static ExpandedNodeId initAASIdentifierType_Id() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6086L));
    }


    static ExpandedNodeId initAASQualifierType_ValueType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6015L));
    }


    static ExpandedNodeId initAASQualifierType_ValueId_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6079L));
    }


    static ExpandedNodeId initAASQualifierType_Value() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6078L));
    }


    static ExpandedNodeId initAASQualifierType_Type() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6010L));
    }


    static ExpandedNodeId initAASReferableType_Category() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6064L));
    }


    static ExpandedNodeId initAASIdentifiableType_Identification_Id() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6091L));
    }


    static ExpandedNodeId initAASIdentifiableType_Identification_IdType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6092L));
    }


    static ExpandedNodeId initAASAssetAdministrationShellType_AssetInformation_AssetKind() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6120L));
    }


    static ExpandedNodeId initAASAssetAdministrationShellType_DerivedFrom_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6004L));
    }


    static ExpandedNodeId initAASSubmodelType_ModelingKind() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6009L));
    }


    static ExpandedNodeId initAASSubmodelType_SubmodelElement_ModelingKind() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6014L));
    }


    static ExpandedNodeId initAASSubmodelType_SubmodelElement_Category() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6127L));
    }


    static ExpandedNodeId initAASSubmodelElementType_Category() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6126L));
    }


    static ExpandedNodeId initAASSubmodelElementType_ModelingKind() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6013L));
    }


    static ExpandedNodeId initAASBlobType_Value() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6024L));
    }


    static ExpandedNodeId initAASBlobType_MimeType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6023L));
    }


    static ExpandedNodeId initAASEntityType_GlobalAssetId_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6055L));
    }


    static ExpandedNodeId initAASEntityType_SpecificAssetId_ExternalSubjectId_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6453L));
    }


    static ExpandedNodeId initAASEntityType_SpecificAssetId_Key() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6454L));
    }


    static ExpandedNodeId initAASEntityType_EntityType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6056L));
    }


    static ExpandedNodeId initAASEntityType_SpecificAssetId_Value() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6455L));
    }


    static ExpandedNodeId initAASFileType_File_GetPosition_OutputArguments() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6040L));
    }


    static ExpandedNodeId initAASFileType_Value() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6132L));
    }


    static ExpandedNodeId initAASFileType_File_Writable() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6049L));
    }


    static ExpandedNodeId initAASFileType_File_Open_OutputArguments() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6042L));
    }


    static ExpandedNodeId initAASFileType_File_UserWritable() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6048L));
    }


    static ExpandedNodeId initAASFileType_File_Write_InputArguments() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6050L));
    }


    static ExpandedNodeId initAASFileType_MimeType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6037L));
    }


    static ExpandedNodeId initAASFileType_File_Read_OutputArguments() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6045L));
    }


    static ExpandedNodeId initAASFileType_File_Close_InputArguments() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6038L));
    }


    static ExpandedNodeId initAASFileType_File_GetPosition_InputArguments() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6039L));
    }


    static ExpandedNodeId initAASFileType_File_SetPosition_InputArguments() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6046L));
    }


    static ExpandedNodeId initAASFileType_File_Size() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6047L));
    }


    static ExpandedNodeId initAASFileType_File_Read_InputArguments() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6044L));
    }


    static ExpandedNodeId initAASFileType_File_OpenCount() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6043L));
    }


    static ExpandedNodeId initAASFileType_File_Open_InputArguments() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6041L));
    }


    static ExpandedNodeId initAASMultiLanguagePropertyType_ValueId_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6018L));
    }


    static ExpandedNodeId initAASMultiLanguagePropertyType_Value() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6019L));
    }


    static ExpandedNodeId initAASPropertyType_Value() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6020L));
    }


    static ExpandedNodeId initAASPropertyType_ValueType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6021L));
    }


    static ExpandedNodeId initAASPropertyType_ValueId_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6022L));
    }


    static ExpandedNodeId initAASRangeType_ValueType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6057L));
    }


    static ExpandedNodeId initAASRangeType_Min() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6058L));
    }


    static ExpandedNodeId initAASRangeType_Max() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6059L));
    }


    static ExpandedNodeId initAASReferenceElementType_Value_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6053L));
    }


    static ExpandedNodeId initAASRelationshipElementType_Second_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6052L));
    }


    static ExpandedNodeId initAASRelationshipElementType_First_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6051L));
    }


    static ExpandedNodeId initAASSubmodelElementCollectionType_SubmodelElement_Category() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6128L));
    }


    static ExpandedNodeId initAASSubmodelElementCollectionType_SubmodelElement_ModelingKind() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6016L));
    }


    static ExpandedNodeId initAASSubmodelElementCollectionType_AllowDuplicates() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6017L));
    }


    static ExpandedNodeId initAASOrderedSubmodelElementCollectionType_SubmodelElement_Category() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6131L));
    }


    static ExpandedNodeId initAASOrderedSubmodelElementCollectionType_SubmodelElement_ModelingKind() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6104L));
    }


    static ExpandedNodeId initAASReferenceType_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6001L));
    }


    static ExpandedNodeId initIAASReferableType_Category() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6082L));
    }


    static ExpandedNodeId initIAASIdentifiableType_Identification_IdType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6089L));
    }


    static ExpandedNodeId initIAASIdentifiableType_Identification_Id() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6090L));
    }


    static ExpandedNodeId initAASCustomConceptDescriptionType_Identification_IdType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6027L));
    }


    static ExpandedNodeId initAASCustomConceptDescriptionType_Category() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6025L));
    }


    static ExpandedNodeId initAASCustomConceptDescriptionType_Identification_Id() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6026L));
    }


    static ExpandedNodeId initAASIrdiConceptDescriptionType_Identification_Id() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6029L));
    }


    static ExpandedNodeId initAASIrdiConceptDescriptionType_Identification_IdType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6030L));
    }


    static ExpandedNodeId initAASIrdiConceptDescriptionType_Category() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6028L));
    }


    static ExpandedNodeId initAASIriConceptDescriptionType_Category() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6031L));
    }


    static ExpandedNodeId initAASIriConceptDescriptionType_Identification_IdType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6242L));
    }


    static ExpandedNodeId initAASIriConceptDescriptionType_Identification_Id() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6241L));
    }


    static ExpandedNodeId initAASEnvironmentType_Submodel_ModelingKind() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6142L));
    }


    static ExpandedNodeId initAASEnvironmentType_AAS_Identification_IdType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6011L));
    }


    static ExpandedNodeId initAASEnvironmentType_Asset_Identification_Id() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6139L));
    }


    static ExpandedNodeId initAASEnvironmentType_Asset_Identification_IdType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6140L));
    }


    static ExpandedNodeId initAASEnvironmentType_AAS_AssetInformation_AssetKind() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6121L));
    }


    static ExpandedNodeId initAASEnvironmentType_Asset_Category() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6141L));
    }


    static ExpandedNodeId initAASEnvironmentType_Submodel_Identification_IdType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6144L));
    }


    static ExpandedNodeId initAASEnvironmentType_AAS_Category() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6137L));
    }


    static ExpandedNodeId initAASEnvironmentType_Submodel_Identification_Id() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6143L));
    }


    static ExpandedNodeId initAASEnvironmentType_AAS_Identification_Id() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6008L));
    }


    static ExpandedNodeId initAASEnvironmentType_Submodel_Category() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6145L));
    }


    static ExpandedNodeId initAASIdentifierKeyValuePairList_AASIdentifierKeyValuePair_Key() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6124L));
    }


    static ExpandedNodeId initAASIdentifierKeyValuePairList_AASIdentifierKeyValuePair_Value() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6130L));
    }


    static ExpandedNodeId initAASIdentifierKeyValuePairList_AASIdentifierKeyValuePair_ExternalSubjectId_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6065L));
    }


    static ExpandedNodeId initAASQualifierList_AASQualifier_ValueType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6451L));
    }


    static ExpandedNodeId initAASQualifierList_AASQualifier_Type() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6134L));
    }


    static ExpandedNodeId initAASReferenceList_AASReference_Keys() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6452L));
    }


    static ExpandedNodeId initAASSubmodelElementList_AASSubmodelElement_Category() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6462L));
    }


    static ExpandedNodeId initAASSubmodelElementList_AASSubmodelElement_ModelingKind() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(6463L));
    }
}
