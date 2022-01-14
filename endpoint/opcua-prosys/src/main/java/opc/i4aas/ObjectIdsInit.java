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


class ObjectIdsInit {
    static ExpandedNodeId initAASKeyDataType_DefaultJson() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5040L));
    }


    static ExpandedNodeId initAASKeyDataType_DefaultBinary() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5038L));
    }


    static ExpandedNodeId initAASKeyDataType_DefaultXml() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5039L));
    }


    static ExpandedNodeId initAASAssetInformationType_DefaultThumbnail_DataSpecification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5371L));
    }


    static ExpandedNodeId initAASAssetInformationType_SpecificAssetId() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5049L));
    }


    static ExpandedNodeId initAASAssetInformationType_DefaultThumbnail_Qualifier() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5384L));
    }


    static ExpandedNodeId initAASAssetInformationType_BillOfMaterial() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5005L));
    }


    static ExpandedNodeId initAASAssetInformationType_GlobalAssetId() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5359L));
    }


    static ExpandedNodeId initAASAssetInformationType_DefaultThumbnail() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5362L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_UnitId() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5028L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_Identification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5026L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_ValueId() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5030L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type_ValueList() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5029L));
    }


    static ExpandedNodeId initAASIdentifierKeyValuePairType_ExternalSubjectId() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5363L));
    }


    static ExpandedNodeId initAASQualifierType_ValueId() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5033L));
    }


    static ExpandedNodeId initAASIdentifiableType_Identification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5037L));
    }


    static ExpandedNodeId initAASIdentifiableType_Administration() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5036L));
    }


    static ExpandedNodeId initAASAssetAdministrationShellType_DerivedFrom() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5007L));
    }


    static ExpandedNodeId initAASAssetAdministrationShellType_Submodel() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5004L));
    }


    static ExpandedNodeId initAASAssetAdministrationShellType_AssetInformation_SpecificAssetId() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5050L));
    }


    static ExpandedNodeId initAASAssetAdministrationShellType_AssetInformation() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5156L));
    }


    static ExpandedNodeId initAASAssetAdministrationShellType_AssetInformation_BillOfMaterial() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5045L));
    }


    static ExpandedNodeId initAASAssetAdministrationShellType_DataSpecification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5001L));
    }


    static ExpandedNodeId initAASAssetType_DataSpecification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5008L));
    }


    static ExpandedNodeId initAASSubmodelType_SubmodelElement_DataSpecification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5364L));
    }


    static ExpandedNodeId initAASSubmodelType_SubmodelElement() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5010L));
    }


    static ExpandedNodeId initAASSubmodelType_DataSpecification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5009L));
    }


    static ExpandedNodeId initAASSubmodelType_Qualifier() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5032L));
    }


    static ExpandedNodeId initAASSubmodelType_SubmodelElement_Qualifier() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5379L));
    }


    static ExpandedNodeId initAASSubmodelElementType_DataSpecification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5011L));
    }


    static ExpandedNodeId initAASSubmodelElementType_Qualifier() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5031L));
    }


    static ExpandedNodeId initAASEntityType_Statement() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5021L));
    }


    static ExpandedNodeId initAASEntityType_GlobalAssetId() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5022L));
    }


    static ExpandedNodeId initAASEntityType_SpecificAssetId() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5365L));
    }


    static ExpandedNodeId initAASEntityType_SpecificAssetId_ExternalSubjectId() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5366L));
    }


    static ExpandedNodeId initAASFileType_File() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5016L));
    }


    static ExpandedNodeId initAASMultiLanguagePropertyType_ValueId() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5013L));
    }


    static ExpandedNodeId initAASPropertyType_ValueId() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5014L));
    }


    static ExpandedNodeId initAASReferenceElementType_Value() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5020L));
    }


    static ExpandedNodeId initAASRelationshipElementType_Second() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5018L));
    }


    static ExpandedNodeId initAASRelationshipElementType_First() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5017L));
    }


    static ExpandedNodeId initAASAnnotatedRelationshipElementType_Annotation() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5019L));
    }


    static ExpandedNodeId initAASSubmodelElementCollectionType_SubmodelElement_Qualifier() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5380L));
    }


    static ExpandedNodeId initAASSubmodelElementCollectionType_SubmodelElement_DataSpecification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5367L));
    }


    static ExpandedNodeId initAASSubmodelElementCollectionType_SubmodelElement() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5012L));
    }


    static ExpandedNodeId initAASOrderedSubmodelElementCollectionType_SubmodelElement() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5042L));
    }


    static ExpandedNodeId initAASOrderedSubmodelElementCollectionType_SubmodelElement_Qualifier() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5383L));
    }


    static ExpandedNodeId initAASOrderedSubmodelElementCollectionType_SubmodelElement_DataSpecification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5370L));
    }


    static ExpandedNodeId initIAASIdentifiableType_Identification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5034L));
    }


    static ExpandedNodeId initIAASIdentifiableType_Administration() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5035L));
    }


    static ExpandedNodeId initAASCustomConceptDescriptionType_IsCaseOf() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5048L));
    }


    static ExpandedNodeId initAASCustomConceptDescriptionType_DataSpecification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5043L));
    }


    static ExpandedNodeId initAASCustomConceptDescriptionType_Administration() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5015L));
    }


    static ExpandedNodeId initAASCustomConceptDescriptionType_Identification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5155L));
    }


    static ExpandedNodeId initAASIrdiConceptDescriptionType_IsCaseOf() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5044L));
    }


    static ExpandedNodeId initAASIrdiConceptDescriptionType_Administration() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5157L));
    }


    static ExpandedNodeId initAASIrdiConceptDescriptionType_DataSpecification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5024L));
    }


    static ExpandedNodeId initAASIrdiConceptDescriptionType_Identification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5158L));
    }


    static ExpandedNodeId initAASIriConceptDescriptionType_Administration() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5159L));
    }


    static ExpandedNodeId initAASIriConceptDescriptionType_DataSpecification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5025L));
    }


    static ExpandedNodeId initAASIriConceptDescriptionType_Identification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5160L));
    }


    static ExpandedNodeId initAASIriConceptDescriptionType_IsCaseOf() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5046L));
    }


    static ExpandedNodeId initAASEnvironmentType_AAS_Identification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5101L));
    }


    static ExpandedNodeId initAASEnvironmentType_Asset_Administration() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5150L));
    }


    static ExpandedNodeId initAASEnvironmentType_Submodel_Administration() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5153L));
    }


    static ExpandedNodeId initAASEnvironmentType_Submodel() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5152L));
    }


    static ExpandedNodeId initAASEnvironmentType_Submodel_Qualifier() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5404L));
    }


    static ExpandedNodeId initAASEnvironmentType_AAS() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5002L));
    }


    static ExpandedNodeId initAASEnvironmentType_Submodel_DataSpecification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5401L));
    }


    static ExpandedNodeId initAASEnvironmentType_Asset_DataSpecification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5398L));
    }


    static ExpandedNodeId initAASEnvironmentType_Asset() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5103L));
    }


    static ExpandedNodeId initAASEnvironmentType_AAS_Administration() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5003L));
    }


    static ExpandedNodeId initAASEnvironmentType_AAS_AssetInformation() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5610L));
    }


    static ExpandedNodeId initAASEnvironmentType_AAS_DataSpecification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5392L));
    }


    static ExpandedNodeId initAASEnvironmentType_AAS_AssetInformation_BillOfMaterial() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5047L));
    }


    static ExpandedNodeId initAASEnvironmentType_AAS_Submodel() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5395L));
    }


    static ExpandedNodeId initAASEnvironmentType_Asset_Identification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5151L));
    }


    static ExpandedNodeId initAASEnvironmentType_Submodel_Identification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5154L));
    }


    static ExpandedNodeId initAASEnvironmentType_AAS_AssetInformation_SpecificAssetId() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5360L));
    }


    static ExpandedNodeId initAASIdentifierKeyValuePairList_AASIdentifierKeyValuePair_ExternalSubjectId() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5612L));
    }


    static ExpandedNodeId initAASIdentifierKeyValuePairList_AASIdentifierKeyValuePair() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5027L));
    }


    static ExpandedNodeId initAASQualifierList_AASQualifier() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5613L));
    }


    static ExpandedNodeId initAASReferenceList_AASReference() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5614L));
    }


    static ExpandedNodeId initAASSubmodelElementList_AASSubmodelElement_DataSpecification() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5616L));
    }


    static ExpandedNodeId initAASSubmodelElementList_AASSubmodelElement() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5615L));
    }


    static ExpandedNodeId initAASSubmodelElementList_AASSubmodelElement_Qualifier() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(5617L));
    }
}
