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
import opc.i4aas.AASModelingKindDataType;
import opc.i4aas.AASSubmodelElementType;


/**
 * Generated on 2022-02-08 12:58:54
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1009")
public abstract class AASSubmodelElementTypeNodeBase extends AASReferableTypeNode implements AASSubmodelElementType {
    private static GeneratedNodeInitializer<AASSubmodelElementTypeNode> aASSubmodelElementTypeNodeInitializer;

    protected AASSubmodelElementTypeNodeBase(NodeManagerUaNode nodeManager, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(nodeManager, nodeId, browseName, displayName);
    }


    @Override
    public void afterCreate() {
        super.afterCreate();

        // Call afterCreate for each sub-node (if the node has any)
        callAfterCreateIfExists(getDataSpecificationNode());
        callAfterCreateIfExists(getQualifierNode());
        GeneratedNodeInitializer<AASSubmodelElementTypeNode> impl = getAASSubmodelElementTypeNodeInitializer();
        if (impl != null) {
            impl.init((AASSubmodelElementTypeNode) this);
        }
    }


    public static GeneratedNodeInitializer<AASSubmodelElementTypeNode> getAASSubmodelElementTypeNodeInitializer() {
        return aASSubmodelElementTypeNodeInitializer;
    }


    public static void setAASSubmodelElementTypeNodeInitializer(GeneratedNodeInitializer<AASSubmodelElementTypeNode> aASSubmodelElementTypeNodeInitializerNewValue) {
        aASSubmodelElementTypeNodeInitializer = aASSubmodelElementTypeNodeInitializerNewValue;
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
            throw new RuntimeException("Mandatory node Category does not exist");
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Mandatory
    @Override
    public void setCategory(String value) {
        UaVariable node = getCategoryNode();
        if (node == null) {
            throw new RuntimeException("Setting Category failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting Category failed unexpectedly", e);
        }
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
            throw new RuntimeException("Mandatory node ModelingKind does not exist");
        }
        Variant value = node.getValue().getValue();
        return (AASModelingKindDataType) value.asEnum(AASModelingKindDataType.class);
    }


    @Mandatory
    @Override
    public void setModelingKind(AASModelingKindDataType value) {
        UaVariable node = getModelingKindNode();
        if (node == null) {
            throw new RuntimeException("Setting ModelingKind failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting ModelingKind failed unexpectedly", e);
        }
    }


    @Mandatory
    @Override
    public AASReferenceListNode getDataSpecificationNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "DataSpecification");
        return (AASReferenceListNode) getComponent(browseName);
    }


    @Mandatory
    @Override
    public AASQualifierListNode getQualifierNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Qualifier");
        return (AASQualifierListNode) getComponent(browseName);
    }


    @Override
    public Variant[] callMethod(ServiceContext serviceContext, NodeId methodId,
                                Variant[] inputArguments, StatusCode[] inputArgumentResults,
                                DiagnosticInfo[] inputArgumentDiagnosticInfos)
            throws StatusException {
        return super.callMethod(serviceContext, methodId, inputArguments, inputArgumentResults, inputArgumentDiagnosticInfos);
    }
}
