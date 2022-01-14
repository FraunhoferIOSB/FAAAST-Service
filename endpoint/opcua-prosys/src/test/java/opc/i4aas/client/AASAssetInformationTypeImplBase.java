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

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.TypeDefinitionId;
import com.prosysopc.ua.client.AddressSpace;
import com.prosysopc.ua.nodes.Mandatory;
import com.prosysopc.ua.nodes.Optional;
import com.prosysopc.ua.nodes.UaProperty;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.types.opcua.client.BaseObjectTypeImpl;
import opc.i4aas.AASAssetInformationType;
import opc.i4aas.AASAssetKindDataType;
import opc.i4aas.AASFileType;
import opc.i4aas.AASIdentifierKeyValuePairList;
import opc.i4aas.AASReferenceList;
import opc.i4aas.AASReferenceType;


/**
 * Generated on 2021-12-15 11:39:02
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1031")
public abstract class AASAssetInformationTypeImplBase extends BaseObjectTypeImpl implements AASAssetInformationType {
    protected AASAssetInformationTypeImplBase(AddressSpace addressSpace, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(addressSpace, nodeId, browseName, displayName);
    }


    @Mandatory
    @Override
    public UaProperty getAssetKindNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "AssetKind");
        return getProperty(browseName);
    }


    @Mandatory
    @Override
    public AASAssetKindDataType getAssetKind() {
        UaVariable node = getAssetKindNode();
        if (node == null) {
            return null;
        }
        Variant value = node.getValue().getValue();
        return (AASAssetKindDataType) value.asEnum(AASAssetKindDataType.class);
    }


    @Mandatory
    @Override
    public void setAssetKind(AASAssetKindDataType value) throws StatusException {
        UaVariable node = getAssetKindNode();
        if (node == null) {
            throw new RuntimeException("Setting AssetKind failed, the Optional node does not exist)");
        }
        node.setValue(value);
    }


    @Mandatory
    @Override
    public AASReferenceList getBillOfMaterialNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "BillOfMaterial");
        return (AASReferenceList) getComponent(browseName);
    }


    @Optional
    @Override
    public AASFileType getDefaultThumbnailNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "DefaultThumbnail");
        return (AASFileType) getComponent(browseName);
    }


    @Optional
    @Override
    public AASReferenceType getGlobalAssetIdNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "GlobalAssetId");
        return (AASReferenceType) getComponent(browseName);
    }


    @Mandatory
    @Override
    public AASIdentifierKeyValuePairList getSpecificAssetIdNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "SpecificAssetId");
        return (AASIdentifierKeyValuePairList) getComponent(browseName);
    }
}
