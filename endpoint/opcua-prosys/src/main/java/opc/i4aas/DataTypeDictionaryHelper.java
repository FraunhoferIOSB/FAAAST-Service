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
import com.prosysopc.ua.typedictionary.GeneratedDataTypeDictionary;


/**
 * Generated on 2021-12-15 11:39:02
 */
public class DataTypeDictionaryHelper {
    public static GeneratedDataTypeDictionary createDataTypeDictionary() {
        GeneratedDataTypeDictionary r = new GeneratedDataTypeDictionary("http://opcfoundation.org/UA/I4AAS/V3/");
        r.addTypeInformation(eni("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3003"), "AASAssetKindDataType", AASAssetKindDataType.class);
        r.addTypeInformation(eni("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3007"), "AASCategoryDataType", AASCategoryDataType.class);
        r.addTypeInformation(eni("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3008"), "AASDataTypeIEC61360DataType", AASDataTypeIEC61360DataType.class);
        r.addTypeInformation(eni("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3006"), "AASEntityTypeDataType", AASEntityTypeDataType.class);
        r.addTypeInformation(eni("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3010"), "AASIdentifierTypeDataType", AASIdentifierTypeDataType.class);
        r.addTypeInformation(eni("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3012"), "AASKeyElementsDataType", AASKeyElementsDataType.class);
        r.addTypeInformation(eni("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3002"), "AASKeyTypeDataType", AASKeyTypeDataType.class);
        r.addTypeInformation(eni("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3009"), "AASLevelTypeDataType", AASLevelTypeDataType.class);
        r.addTypeInformation(eni("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3015"), "AASModelingKindDataType", AASModelingKindDataType.class);
        r.addTypeInformation(eni("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3004"), "AASValueTypeDataType", AASValueTypeDataType.class);
        r.addTypeInformation(eni("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3016"), "AASMimeDataType", String.class);
        r.addTypeInformation(eni("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3005"), "AASPathDataType", String.class);
        r.addTypeInformation(eni("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3014"), "AASPropertyValueDataType", String.class);
        r.addTypeInformation(eni("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3013"), "AASQualifierDataType", String.class);
        r.addTypeInformation(eni("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=3011"), "AASKeyDataType", AASKeyDataType.class);
        return r;
    }


    private static ExpandedNodeId eni(String id) {
        return ExpandedNodeId.parseExpandedNodeId(id);
    }
}
