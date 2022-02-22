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


class ObjectTypeIdsInit {
    static ExpandedNodeId initAASAdministrativeInformationType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1030L));
    }


    static ExpandedNodeId initAASAssetInformationType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1031L));
    }


    static ExpandedNodeId initAASDataSpecificationType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1027L));
    }


    static ExpandedNodeId initAASDataSpecificationIEC61360Type() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1028L));
    }


    static ExpandedNodeId initAASIdentifierKeyValuePairType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1035L));
    }


    static ExpandedNodeId initAASIdentifierType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1029L));
    }


    static ExpandedNodeId initAASQualifierType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1032L));
    }


    static ExpandedNodeId initAASReferableType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1003L));
    }


    static ExpandedNodeId initAASIdentifiableType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1007L));
    }


    static ExpandedNodeId initAASAssetAdministrationShellType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1002L));
    }


    static ExpandedNodeId initAASAssetType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1005L));
    }


    static ExpandedNodeId initAASSubmodelType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1006L));
    }


    static ExpandedNodeId initAASSubmodelElementType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1009L));
    }


    static ExpandedNodeId initAASBlobType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1016L));
    }


    static ExpandedNodeId initAASCapabilityType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1014L));
    }


    static ExpandedNodeId initAASEntityType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1022L));
    }


    static ExpandedNodeId initAASEventType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1021L));
    }


    static ExpandedNodeId initAASFileType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1017L));
    }


    static ExpandedNodeId initAASMultiLanguagePropertyType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1012L));
    }


    static ExpandedNodeId initAASOperationType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1015L));
    }


    static ExpandedNodeId initAASPropertyType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1013L));
    }


    static ExpandedNodeId initAASRangeType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1023L));
    }


    static ExpandedNodeId initAASReferenceElementType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1020L));
    }


    static ExpandedNodeId initAASRelationshipElementType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1018L));
    }


    static ExpandedNodeId initAASAnnotatedRelationshipElementType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1019L));
    }


    static ExpandedNodeId initAASSubmodelElementCollectionType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1010L));
    }


    static ExpandedNodeId initAASOrderedSubmodelElementCollectionType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1011L));
    }


    static ExpandedNodeId initAASReferenceType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1004L));
    }


    static ExpandedNodeId initIAASReferableType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1033L));
    }


    static ExpandedNodeId initIAASIdentifiableType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1034L));
    }


    static ExpandedNodeId initAASCustomConceptDescriptionType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1026L));
    }


    static ExpandedNodeId initAASIrdiConceptDescriptionType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1024L));
    }


    static ExpandedNodeId initAASIriConceptDescriptionType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1025L));
    }


    static ExpandedNodeId initAASEnvironmentType() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1008L));
    }


    static ExpandedNodeId initAASIdentifierKeyValuePairList() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1039L));
    }


    static ExpandedNodeId initAASQualifierList() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1037L));
    }


    static ExpandedNodeId initAASReferenceList() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1036L));
    }


    static ExpandedNodeId initAASSubmodelElementList() {
        return new ExpandedNodeId("http://opcfoundation.org/UA/I4AAS/V3/", UnsignedInteger.valueOf(1038L));
    }
}
