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
package opc.i4aas.server;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.TypeDefinitionId;
import com.prosysopc.ua.nodes.Mandatory;
import com.prosysopc.ua.nodes.Optional;
import com.prosysopc.ua.server.GeneratedNodeInitializer;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.stack.builtintypes.DiagnosticInfo;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.builtintypes.Variant;
import opc.i4aas.AASAssetAdministrationShellType;


/**
 * Generated on 2022-02-08 12:58:54
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1002")
public abstract class AASAssetAdministrationShellTypeNodeBase extends AASIdentifiableTypeNode implements AASAssetAdministrationShellType {
    private static GeneratedNodeInitializer<AASAssetAdministrationShellTypeNode> aASAssetAdministrationShellTypeNodeInitializer;

    protected AASAssetAdministrationShellTypeNodeBase(NodeManagerUaNode nodeManager, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(nodeManager, nodeId, browseName, displayName);
    }


    @Override
    public void afterCreate() {
        super.afterCreate();

        // Call afterCreate for each sub-node (if the node has any)
        callAfterCreateIfExists(getAssetInformationNode());
        callAfterCreateIfExists(getDataSpecificationNode());
        callAfterCreateIfExists(getDerivedFromNode());
        callAfterCreateIfExists(getSubmodelNode());
        GeneratedNodeInitializer<AASAssetAdministrationShellTypeNode> impl = getAASAssetAdministrationShellTypeNodeInitializer();
        if (impl != null) {
            impl.init((AASAssetAdministrationShellTypeNode) this);
        }
    }


    public static GeneratedNodeInitializer<AASAssetAdministrationShellTypeNode> getAASAssetAdministrationShellTypeNodeInitializer() {
        return aASAssetAdministrationShellTypeNodeInitializer;
    }


    public static void setAASAssetAdministrationShellTypeNodeInitializer(GeneratedNodeInitializer<AASAssetAdministrationShellTypeNode> aASAssetAdministrationShellTypeNodeInitializerNewValue) {
        aASAssetAdministrationShellTypeNodeInitializer = aASAssetAdministrationShellTypeNodeInitializerNewValue;
    }


    @Mandatory
    @Override
    public AASAssetInformationTypeNode getAssetInformationNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "AssetInformation");
        return (AASAssetInformationTypeNode) getComponent(browseName);
    }


    @Mandatory
    @Override
    public AASReferenceListNode getDataSpecificationNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "DataSpecification");
        return (AASReferenceListNode) getComponent(browseName);
    }


    @Optional
    @Override
    public AASReferenceTypeNode getDerivedFromNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "DerivedFrom");
        return (AASReferenceTypeNode) getComponent(browseName);
    }


    @Mandatory
    @Override
    public AASReferenceListNode getSubmodelNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Submodel");
        return (AASReferenceListNode) getComponent(browseName);
    }


    @Override
    public Variant[] callMethod(ServiceContext serviceContext, NodeId methodId,
                                Variant[] inputArguments, StatusCode[] inputArgumentResults,
                                DiagnosticInfo[] inputArgumentDiagnosticInfos)
            throws StatusException {
        return super.callMethod(serviceContext, methodId, inputArguments, inputArgumentResults, inputArgumentDiagnosticInfos);
    }
}
