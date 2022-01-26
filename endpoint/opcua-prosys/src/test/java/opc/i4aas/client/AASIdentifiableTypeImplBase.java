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

import com.prosysopc.ua.TypeDefinitionId;
import com.prosysopc.ua.client.AddressSpace;
import com.prosysopc.ua.nodes.Mandatory;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import opc.i4aas.AASAdministrativeInformationType;
import opc.i4aas.AASIdentifiableType;
import opc.i4aas.AASIdentifierType;


/**
 * Generated on 2022-01-26 16:50:24
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1007")
public abstract class AASIdentifiableTypeImplBase extends AASReferableTypeImpl implements AASIdentifiableType {
    protected AASIdentifiableTypeImplBase(AddressSpace addressSpace, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(addressSpace, nodeId, browseName, displayName);
    }


    @Mandatory
    @Override
    public AASAdministrativeInformationType getAdministrationNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Administration");
        return (AASAdministrativeInformationType) getComponent(browseName);
    }


    @Mandatory
    @Override
    public AASIdentifierType getIdentificationNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Identification");
        return (AASIdentifierType) getComponent(browseName);
    }
}
