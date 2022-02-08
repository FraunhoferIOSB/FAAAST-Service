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
import com.prosysopc.ua.stack.builtintypes.Variant;
import opc.i4aas.AASModelingKindDataType;
import opc.i4aas.AASQualifierList;
import opc.i4aas.AASReferenceList;
import opc.i4aas.AASSubmodelElementType;


/**
 * Generated on 2022-02-08 12:58:54
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1009")
public abstract class AASSubmodelElementTypeImplBase extends AASReferableTypeImpl implements AASSubmodelElementType {
    protected AASSubmodelElementTypeImplBase(AddressSpace addressSpace, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(addressSpace, nodeId, browseName, displayName);
    }


    @Mandatory
    @Override
    public UaProperty getCategoryNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Category");
        return getProperty(browseName);
    }


    @Mandatory
    @Override
    public String getCategory() {
        UaVariable node = getCategoryNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Mandatory
    @Override
    public void setCategory(String value) throws StatusException {
        UaVariable node = getCategoryNode();
        if (node == null) {
            throw new RuntimeException("Setting Category failed, the Optional node does not exist)");
        }
        node.setValue(value);
    }


    @Mandatory
    @Override
    public UaProperty getModelingKindNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "ModelingKind");
        return getProperty(browseName);
    }


    @Mandatory
    @Override
    public AASModelingKindDataType getModelingKind() {
        UaVariable node = getModelingKindNode();
        if (node == null) {
            return null;
        }
        Variant value = node.getValue().getValue();
        return (AASModelingKindDataType) value.asEnum(AASModelingKindDataType.class);
    }


    @Mandatory
    @Override
    public void setModelingKind(AASModelingKindDataType value) throws StatusException {
        UaVariable node = getModelingKindNode();
        if (node == null) {
            throw new RuntimeException("Setting ModelingKind failed, the Optional node does not exist)");
        }
        node.setValue(value);
    }


    @Mandatory
    @Override
    public AASReferenceList getDataSpecificationNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "DataSpecification");
        return (AASReferenceList) getComponent(browseName);
    }


    @Mandatory
    @Override
    public AASQualifierList getQualifierNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Qualifier");
        return (AASQualifierList) getComponent(browseName);
    }
}
