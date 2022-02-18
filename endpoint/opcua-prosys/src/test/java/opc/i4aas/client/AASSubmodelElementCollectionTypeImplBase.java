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
import com.prosysopc.ua.nodes.Optional;
import com.prosysopc.ua.nodes.UaProperty;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import opc.i4aas.AASSubmodelElementCollectionType;


/**
 * Generated on 2022-02-08 12:58:54
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1010")
public abstract class AASSubmodelElementCollectionTypeImplBase extends AASSubmodelElementTypeImpl implements AASSubmodelElementCollectionType {
    protected AASSubmodelElementCollectionTypeImplBase(AddressSpace addressSpace, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(addressSpace, nodeId, browseName, displayName);
    }


    @Optional
    @Override
    public UaProperty getAllowDuplicatesNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "AllowDuplicates");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public Boolean isAllowDuplicates() {
        UaVariable node = getAllowDuplicatesNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (Boolean) value;
    }


    @Optional
    @Override
    public void setAllowDuplicates(Boolean value) throws StatusException {
        UaVariable node = getAllowDuplicatesNode();
        if (node == null) {
            throw new RuntimeException("Setting AllowDuplicates failed, the Optional node does not exist)");
        }
        node.setValue(value);
    }
}
