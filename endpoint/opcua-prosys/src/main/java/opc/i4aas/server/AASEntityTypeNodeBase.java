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
import opc.i4aas.AASEntityType;
import opc.i4aas.AASEntityTypeDataType;


/**
 * Generated on 2021-12-15 11:39:02
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1022")
public abstract class AASEntityTypeNodeBase extends AASSubmodelElementTypeNode implements AASEntityType {
    private static GeneratedNodeInitializer<AASEntityTypeNode> aASEntityTypeNodeInitializer;

    protected AASEntityTypeNodeBase(NodeManagerUaNode nodeManager, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(nodeManager, nodeId, browseName, displayName);
    }


    @Override
    public void afterCreate() {
        super.afterCreate();

        // Call afterCreate for each sub-node (if the node has any)
        callAfterCreateIfExists(getGlobalAssetIdNode());
        callAfterCreateIfExists(getSpecificAssetIdNode());
        callAfterCreateIfExists(getStatementNode());
        GeneratedNodeInitializer<AASEntityTypeNode> impl = getAASEntityTypeNodeInitializer();
        if (impl != null) {
            impl.init((AASEntityTypeNode) this);
        }
    }


    public static GeneratedNodeInitializer<AASEntityTypeNode> getAASEntityTypeNodeInitializer() {
        return aASEntityTypeNodeInitializer;
    }


    public static void setAASEntityTypeNodeInitializer(GeneratedNodeInitializer<AASEntityTypeNode> aASEntityTypeNodeInitializerNewValue) {
        aASEntityTypeNodeInitializer = aASEntityTypeNodeInitializerNewValue;
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
            throw new RuntimeException("Mandatory node EntityType does not exist");
        }
        Variant value = node.getValue().getValue();
        return (AASEntityTypeDataType) value.asEnum(AASEntityTypeDataType.class);
    }


    @Mandatory
    @Override
    public void setEntityType(AASEntityTypeDataType value) {
        UaVariable node = getEntityTypeNode();
        if (node == null) {
            throw new RuntimeException("Setting EntityType failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting EntityType failed unexpectedly", e);
        }
    }


    @Optional
    @Override
    public AASReferenceTypeNode getGlobalAssetIdNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "GlobalAssetId");
        return (AASReferenceTypeNode) getComponent(browseName);
    }


    @Optional
    @Override
    public AASIdentifierKeyValuePairTypeNode getSpecificAssetIdNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "SpecificAssetId");
        return (AASIdentifierKeyValuePairTypeNode) getComponent(browseName);
    }


    @Mandatory
    @Override
    public AASSubmodelElementListNode getStatementNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Statement");
        return (AASSubmodelElementListNode) getComponent(browseName);
    }


    @Override
    public Variant[] callMethod(ServiceContext serviceContext, NodeId methodId,
                                Variant[] inputArguments, StatusCode[] inputArgumentResults,
                                DiagnosticInfo[] inputArgumentDiagnosticInfos)
            throws StatusException {
        return super.callMethod(serviceContext, methodId, inputArguments, inputArgumentResults, inputArgumentDiagnosticInfos);
    }
}
