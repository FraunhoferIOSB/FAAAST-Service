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
package opc.i4aas.client;

import com.prosysopc.ua.client.ClientCodegenModel;
import com.prosysopc.ua.client.ClientCodegenModelProvider;
import opc.i4aas.AASAssetKindDataType;
import opc.i4aas.AASCategoryDataType;
import opc.i4aas.AASDataTypeIEC61360DataType;
import opc.i4aas.AASEntityTypeDataType;
import opc.i4aas.AASIdentifierTypeDataType;
import opc.i4aas.AASKeyDataType;
import opc.i4aas.AASKeyElementsDataType;
import opc.i4aas.AASKeyTypeDataType;
import opc.i4aas.AASLevelTypeDataType;
import opc.i4aas.AASModelingKindDataType;
import opc.i4aas.AASValueTypeDataType;
import opc.i4aas.DataTypeDictionaryHelper;
import opc.i4aas.Serializers;


/**
 * Generated on 2022-01-26 16:50:24
 */
public class ClientInformationModel implements ClientCodegenModelProvider {
    public static final ClientCodegenModel MODEL;

    static {
        ClientCodegenModel.Builder b = ClientCodegenModel.builder();
        b.addClass(AASAdministrativeInformationTypeImpl.class);
        b.addClass(AASAssetInformationTypeImpl.class);
        b.addClass(AASDataSpecificationTypeImpl.class);
        b.addClass(AASDataSpecificationIEC61360TypeImpl.class);
        b.addClass(AASIdentifierKeyValuePairTypeImpl.class);
        b.addClass(AASIdentifierTypeImpl.class);
        b.addClass(AASQualifierTypeImpl.class);
        b.addClass(AASReferableTypeImpl.class);
        b.addClass(AASIdentifiableTypeImpl.class);
        b.addClass(AASAssetAdministrationShellTypeImpl.class);
        b.addClass(AASAssetTypeImpl.class);
        b.addClass(AASSubmodelTypeImpl.class);
        b.addClass(AASSubmodelElementTypeImpl.class);
        b.addClass(AASBlobTypeImpl.class);
        b.addClass(AASCapabilityTypeImpl.class);
        b.addClass(AASEntityTypeImpl.class);
        b.addClass(AASEventTypeImpl.class);
        b.addClass(AASFileTypeImpl.class);
        b.addClass(AASMultiLanguagePropertyTypeImpl.class);
        b.addClass(AASOperationTypeImpl.class);
        b.addClass(AASPropertyTypeImpl.class);
        b.addClass(AASRangeTypeImpl.class);
        b.addClass(AASReferenceElementTypeImpl.class);
        b.addClass(AASRelationshipElementTypeImpl.class);
        b.addClass(AASAnnotatedRelationshipElementTypeImpl.class);
        b.addClass(AASSubmodelElementCollectionTypeImpl.class);
        b.addClass(AASOrderedSubmodelElementCollectionTypeImpl.class);
        b.addClass(AASReferenceTypeImpl.class);
        b.addClass(IAASReferableTypeImpl.class);
        b.addClass(IAASIdentifiableTypeImpl.class);
        b.addClass(AASCustomConceptDescriptionTypeImpl.class);
        b.addClass(AASIrdiConceptDescriptionTypeImpl.class);
        b.addClass(AASIriConceptDescriptionTypeImpl.class);
        b.addClass(AASEnvironmentTypeImpl.class);
        b.addClass(AASIdentifierKeyValuePairListImpl.class);
        b.addClass(AASQualifierListImpl.class);
        b.addClass(AASReferenceListImpl.class);
        b.addClass(AASSubmodelElementListImpl.class);
        b.addSerializers(Serializers.SERIALIZERS);
        b.setDataTypeDictionary(DataTypeDictionaryHelper.createDataTypeDictionary());
        b.addStructureSpecification(AASKeyDataType.SPECIFICATION);
        b.addEnumerationSpecification(AASAssetKindDataType.SPECIFICATION);
        b.addEnumerationSpecification(AASCategoryDataType.SPECIFICATION);
        b.addEnumerationSpecification(AASDataTypeIEC61360DataType.SPECIFICATION);
        b.addEnumerationSpecification(AASEntityTypeDataType.SPECIFICATION);
        b.addEnumerationSpecification(AASIdentifierTypeDataType.SPECIFICATION);
        b.addEnumerationSpecification(AASKeyElementsDataType.SPECIFICATION);
        b.addEnumerationSpecification(AASKeyTypeDataType.SPECIFICATION);
        b.addEnumerationSpecification(AASLevelTypeDataType.SPECIFICATION);
        b.addEnumerationSpecification(AASModelingKindDataType.SPECIFICATION);
        b.addEnumerationSpecification(AASValueTypeDataType.SPECIFICATION);
        MODEL = b.build();
    }

    @Override
    public ClientCodegenModel get() {
        return MODEL;
    }
}
