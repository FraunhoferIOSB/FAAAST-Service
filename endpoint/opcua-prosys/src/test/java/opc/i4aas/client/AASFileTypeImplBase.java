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
import com.prosysopc.ua.types.opcua.FileType;
import opc.i4aas.AASFileType;


/**
 * Generated on 2022-01-26 16:50:24
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1017")
public abstract class AASFileTypeImplBase extends AASSubmodelElementTypeImpl implements AASFileType {
    protected AASFileTypeImplBase(AddressSpace addressSpace, NodeId nodeId, QualifiedName browseName,
            LocalizedText displayName) {
        super(addressSpace, nodeId, browseName, displayName);
    }


    @Mandatory
    @Override
    public UaProperty getMimeTypeNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "MimeType");
        return getProperty(browseName);
    }


    @Mandatory
    @Override
    public String getMimeType() {
        UaVariable node = getMimeTypeNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Mandatory
    @Override
    public void setMimeType(String value) throws StatusException {
        UaVariable node = getMimeTypeNode();
        if (node == null) {
            throw new RuntimeException("Setting MimeType failed, the Optional node does not exist)");
        }
        node.setValue(value);
    }


    @Optional
    @Override
    public UaProperty getValueNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Value");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public String getValue() {
        UaVariable node = getValueNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Optional
    @Override
    public void setValue(String value) throws StatusException {
        UaVariable node = getValueNode();
        if (node == null) {
            throw new RuntimeException("Setting Value failed, the Optional node does not exist)");
        }
        node.setValue(value);
    }


    @Optional
    @Override
    public FileType getFileNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "File");
        return (FileType) getComponent(browseName);
    }
}
