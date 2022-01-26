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
import com.prosysopc.ua.nodes.UaProperty;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.server.GeneratedNodeInitializer;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.stack.builtintypes.DiagnosticInfo;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.types.opcua.server.BaseObjectTypeNode;
import opc.i4aas.AASAssetInformationType;
import opc.i4aas.AASAssetKindDataType;


/**
 * Generated on 2022-01-26 16:50:24
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1031")
public abstract class AASAssetInformationTypeNodeBase extends BaseObjectTypeNode implements AASAssetInformationType {
    private static GeneratedNodeInitializer<AASAssetInformationTypeNode> aASAssetInformationTypeNodeInitializer;

    protected AASAssetInformationTypeNodeBase(NodeManagerUaNode nodeManager, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(nodeManager, nodeId, browseName, displayName);
    }


    @Override
    public void afterCreate() {
        super.afterCreate();

        // Call afterCreate for each sub-node (if the node has any)
        callAfterCreateIfExists(getBillOfMaterialNode());
        callAfterCreateIfExists(getDefaultThumbnailNode());
        callAfterCreateIfExists(getGlobalAssetIdNode());
        callAfterCreateIfExists(getSpecificAssetIdNode());
        GeneratedNodeInitializer<AASAssetInformationTypeNode> impl = getAASAssetInformationTypeNodeInitializer();
        if (impl != null) {
            impl.init((AASAssetInformationTypeNode) this);
        }
    }


    public static GeneratedNodeInitializer<AASAssetInformationTypeNode> getAASAssetInformationTypeNodeInitializer() {
        return aASAssetInformationTypeNodeInitializer;
    }


    public static void setAASAssetInformationTypeNodeInitializer(GeneratedNodeInitializer<AASAssetInformationTypeNode> aASAssetInformationTypeNodeInitializerNewValue) {
        aASAssetInformationTypeNodeInitializer = aASAssetInformationTypeNodeInitializerNewValue;
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
            throw new RuntimeException("Mandatory node AssetKind does not exist");
        }
        Variant value = node.getValue().getValue();
        return (AASAssetKindDataType) value.asEnum(AASAssetKindDataType.class);
    }


    @Mandatory
    @Override
    public void setAssetKind(AASAssetKindDataType value) {
        UaVariable node = getAssetKindNode();
        if (node == null) {
            throw new RuntimeException("Setting AssetKind failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting AssetKind failed unexpectedly", e);
        }
    }


    @Mandatory
    @Override
    public AASReferenceListNode getBillOfMaterialNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "BillOfMaterial");
        return (AASReferenceListNode) getComponent(browseName);
    }


    @Optional
    @Override
    public AASFileTypeNode getDefaultThumbnailNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "DefaultThumbnail");
        return (AASFileTypeNode) getComponent(browseName);
    }


    @Optional
    @Override
    public AASReferenceTypeNode getGlobalAssetIdNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "GlobalAssetId");
        return (AASReferenceTypeNode) getComponent(browseName);
    }


    @Mandatory
    @Override
    public AASIdentifierKeyValuePairListNode getSpecificAssetIdNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "SpecificAssetId");
        return (AASIdentifierKeyValuePairListNode) getComponent(browseName);
    }


    @Override
    public Variant[] callMethod(ServiceContext serviceContext, NodeId methodId,
                                Variant[] inputArguments, StatusCode[] inputArgumentResults,
                                DiagnosticInfo[] inputArgumentDiagnosticInfos)
            throws StatusException {
        return super.callMethod(serviceContext, methodId, inputArguments, inputArgumentResults, inputArgumentDiagnosticInfos);
    }
}
