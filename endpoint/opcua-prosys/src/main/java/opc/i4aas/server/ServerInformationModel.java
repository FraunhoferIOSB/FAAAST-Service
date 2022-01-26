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
package opc.i4aas.server;

import com.prosysopc.ua.server.ServerCodegenModel;
import com.prosysopc.ua.server.ServerCodegenModelProvider;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
public class ServerInformationModel implements ServerCodegenModelProvider {
    public static final ServerCodegenModel MODEL;

    static {
        ServerCodegenModel.Builder b = ServerCodegenModel.builder();
        b.addClass(AASAdministrativeInformationTypeNode.class);
        b.addClass(AASAssetInformationTypeNode.class);
        b.addClass(AASDataSpecificationTypeNode.class);
        b.addClass(AASDataSpecificationIEC61360TypeNode.class);
        b.addClass(AASIdentifierKeyValuePairTypeNode.class);
        b.addClass(AASIdentifierTypeNode.class);
        b.addClass(AASQualifierTypeNode.class);
        b.addClass(AASReferableTypeNode.class);
        b.addClass(AASIdentifiableTypeNode.class);
        b.addClass(AASAssetAdministrationShellTypeNode.class);
        b.addClass(AASAssetTypeNode.class);
        b.addClass(AASSubmodelTypeNode.class);
        b.addClass(AASSubmodelElementTypeNode.class);
        b.addClass(AASBlobTypeNode.class);
        b.addClass(AASCapabilityTypeNode.class);
        b.addClass(AASEntityTypeNode.class);
        b.addClass(AASEventTypeNode.class);
        b.addClass(AASFileTypeNode.class);
        b.addClass(AASMultiLanguagePropertyTypeNode.class);
        b.addClass(AASOperationTypeNode.class);
        b.addClass(AASPropertyTypeNode.class);
        b.addClass(AASRangeTypeNode.class);
        b.addClass(AASReferenceElementTypeNode.class);
        b.addClass(AASRelationshipElementTypeNode.class);
        b.addClass(AASAnnotatedRelationshipElementTypeNode.class);
        b.addClass(AASSubmodelElementCollectionTypeNode.class);
        b.addClass(AASOrderedSubmodelElementCollectionTypeNode.class);
        b.addClass(AASReferenceTypeNode.class);
        b.addClass(IAASReferableTypeNode.class);
        b.addClass(IAASIdentifiableTypeNode.class);
        b.addClass(AASCustomConceptDescriptionTypeNode.class);
        b.addClass(AASIrdiConceptDescriptionTypeNode.class);
        b.addClass(AASIriConceptDescriptionTypeNode.class);
        b.addClass(AASEnvironmentTypeNode.class);
        b.addClass(AASIdentifierKeyValuePairListNode.class);
        b.addClass(AASQualifierListNode.class);
        b.addClass(AASReferenceListNode.class);
        b.addClass(AASSubmodelElementListNode.class);
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
    public ServerCodegenModel get() {
        return MODEL;
    }


    /**
     * Returns URI for the NodeSet XML file 'Opc.Ua.I4AAS_V3Draft.NodeSet2.xml', assuming it is put into classpath next to
     * classfile of this class. You can use the 'server_model' codegen target to do it automatically as part of the code
     * generation. If the file is not found this method will throw RuntimeException.
     */
    public static URI getLocationURI() {
        URL nodeset = ServerInformationModel.class.getResource("Opc.Ua.I4AAS_V3Draft.NodeSet2.xml");
        if (nodeset == null) {
            throw new RuntimeException("Cannot find nodeset 'Opc.Ua.I4AAS_V3Draft.NodeSet2.xml' in classpath next to " + ServerInformationModel.class);
        }
        try {
            return nodeset.toURI();
        }
        catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
