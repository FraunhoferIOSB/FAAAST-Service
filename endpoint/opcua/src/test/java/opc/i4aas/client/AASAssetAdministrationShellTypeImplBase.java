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
import com.prosysopc.ua.nodes.Optional;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import opc.i4aas.AASAssetAdministrationShellType;
import opc.i4aas.AASAssetInformationType;
import opc.i4aas.AASReferenceList;
import opc.i4aas.AASReferenceType;


/**
 * Generated on 2022-02-08 12:58:54
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1002")
public abstract class AASAssetAdministrationShellTypeImplBase extends AASIdentifiableTypeImpl implements AASAssetAdministrationShellType {
    protected AASAssetAdministrationShellTypeImplBase(AddressSpace addressSpace, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(addressSpace, nodeId, browseName, displayName);
    }


    @Mandatory
    @Override
    public AASAssetInformationType getAssetInformationNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "AssetInformation");
        return (AASAssetInformationType) getComponent(browseName);
    }


    @Mandatory
    @Override
    public AASReferenceList getDataSpecificationNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "DataSpecification");
        return (AASReferenceList) getComponent(browseName);
    }


    @Optional
    @Override
    public AASReferenceType getDerivedFromNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "DerivedFrom");
        return (AASReferenceType) getComponent(browseName);
    }


    @Mandatory
    @Override
    public AASReferenceList getSubmodelNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Submodel");
        return (AASReferenceList) getComponent(browseName);
    }
}
