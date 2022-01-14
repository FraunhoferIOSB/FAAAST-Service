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


class DataTypeIdsInit {
    static ExpandedNodeId initAASAssetKindDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(3003L));
    }


    static ExpandedNodeId initAASCategoryDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(3007L));
    }


    static ExpandedNodeId initAASDataTypeIEC61360DataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(3008L));
    }


    static ExpandedNodeId initAASEntityTypeDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(3006L));
    }


    static ExpandedNodeId initAASIdentifierTypeDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(3010L));
    }


    static ExpandedNodeId initAASKeyElementsDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(3012L));
    }


    static ExpandedNodeId initAASKeyTypeDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(3002L));
    }


    static ExpandedNodeId initAASLevelTypeDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(3009L));
    }


    static ExpandedNodeId initAASModelingKindDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(3015L));
    }


    static ExpandedNodeId initAASValueTypeDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(3004L));
    }


    static ExpandedNodeId initAASMimeDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(3016L));
    }


    static ExpandedNodeId initAASPathDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(3005L));
    }


    static ExpandedNodeId initAASPropertyValueDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(3014L));
    }


    static ExpandedNodeId initAASQualifierDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(3013L));
    }


    static ExpandedNodeId initAASKeyDataType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(3011L));
    }
}
