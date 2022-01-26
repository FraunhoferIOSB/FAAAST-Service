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
import opc.i4aas.AASRangeType;
import opc.i4aas.AASValueTypeDataType;


/**
 * Generated on 2022-01-26 16:50:24
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1023")
public abstract class AASRangeTypeImplBase extends AASSubmodelElementTypeImpl implements AASRangeType {
    protected AASRangeTypeImplBase(AddressSpace addressSpace, NodeId nodeId, QualifiedName browseName,
            LocalizedText displayName) {
        super(addressSpace, nodeId, browseName, displayName);
    }


    @Optional
    @Override
    public UaProperty getMaxNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Max");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public Object getMax() {
        UaVariable node = getMaxNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (Object) value;
    }


    @Optional
    @Override
    public void setMax(Object value) throws StatusException {
        UaVariable node = getMaxNode();
        if (node == null) {
            throw new RuntimeException("Setting Max failed, the Optional node does not exist)");
        }
        node.setValue(value);
    }


    @Optional
    @Override
    public UaProperty getMinNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Min");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public Object getMin() {
        UaVariable node = getMinNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (Object) value;
    }


    @Optional
    @Override
    public void setMin(Object value) throws StatusException {
        UaVariable node = getMinNode();
        if (node == null) {
            throw new RuntimeException("Setting Min failed, the Optional node does not exist)");
        }
        node.setValue(value);
    }


    @Mandatory
    @Override
    public UaProperty getValueTypeNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "ValueType");
        return getProperty(browseName);
    }


    @Mandatory
    @Override
    public AASValueTypeDataType getValueType() {
        UaVariable node = getValueTypeNode();
        if (node == null) {
            return null;
        }
        Variant value = node.getValue().getValue();
        return (AASValueTypeDataType) value.asEnum(AASValueTypeDataType.class);
    }


    @Mandatory
    @Override
    public void setValueType(AASValueTypeDataType value) throws StatusException {
        UaVariable node = getValueTypeNode();
        if (node == null) {
            throw new RuntimeException("Setting ValueType failed, the Optional node does not exist)");
        }
        node.setValue(value);
    }
}
