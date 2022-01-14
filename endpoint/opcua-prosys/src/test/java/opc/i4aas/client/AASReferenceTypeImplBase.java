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
import com.prosysopc.ua.nodes.UaProperty;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.types.opcua.client.BaseObjectTypeImpl;
import opc.i4aas.AASKeyDataType;
import opc.i4aas.AASReferenceType;


/**
 * Generated on 2021-12-15 11:39:02
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1004")
public abstract class AASReferenceTypeImplBase extends BaseObjectTypeImpl implements AASReferenceType {
    protected AASReferenceTypeImplBase(AddressSpace addressSpace, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(addressSpace, nodeId, browseName, displayName);
    }


    @Mandatory
    @Override
    public UaProperty getKeysNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Keys");
        return getProperty(browseName);
    }


    @Mandatory
    @Override
    public AASKeyDataType[] getKeys() {
        UaVariable node = getKeysNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (AASKeyDataType[]) value;
    }


    @Mandatory
    @Override
    public void setKeys(AASKeyDataType[] value) throws StatusException {
        UaVariable node = getKeysNode();
        if (node == null) {
            throw new RuntimeException("Setting Keys failed, the Optional node does not exist)");
        }
        node.setValue(value);
    }
}
