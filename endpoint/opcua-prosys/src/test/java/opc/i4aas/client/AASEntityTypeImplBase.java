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
import opc.i4aas.AASEntityType;
import opc.i4aas.AASEntityTypeDataType;
import opc.i4aas.AASIdentifierKeyValuePairType;
import opc.i4aas.AASReferenceType;
import opc.i4aas.AASSubmodelElementList;


/**
 * Generated on 2021-12-15 11:39:02
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1022")
public abstract class AASEntityTypeImplBase extends AASSubmodelElementTypeImpl implements AASEntityType {
    protected AASEntityTypeImplBase(AddressSpace addressSpace, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(addressSpace, nodeId, browseName, displayName);
    }


    @Mandatory
    @Override
    public UaProperty getEntityTypeNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "EntityType");
        return getProperty(browseName);
    }


    @Mandatory
    @Override
    public AASEntityTypeDataType getEntityType() {
        UaVariable node = getEntityTypeNode();
        if (node == null) {
            return null;
        }
        Variant value = node.getValue().getValue();
        return (AASEntityTypeDataType) value.asEnum(AASEntityTypeDataType.class);
    }


    @Mandatory
    @Override
    public void setEntityType(AASEntityTypeDataType value) throws StatusException {
        UaVariable node = getEntityTypeNode();
        if (node == null) {
            throw new RuntimeException("Setting EntityType failed, the Optional node does not exist)");
        }
        node.setValue(value);
    }


    @Optional
    @Override
    public AASReferenceType getGlobalAssetIdNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "GlobalAssetId");
        return (AASReferenceType) getComponent(browseName);
    }


    @Optional
    @Override
    public AASIdentifierKeyValuePairType getSpecificAssetIdNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "SpecificAssetId");
        return (AASIdentifierKeyValuePairType) getComponent(browseName);
    }


    @Mandatory
    @Override
    public AASSubmodelElementList getStatementNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Statement");
        return (AASSubmodelElementList) getComponent(browseName);
    }
}
