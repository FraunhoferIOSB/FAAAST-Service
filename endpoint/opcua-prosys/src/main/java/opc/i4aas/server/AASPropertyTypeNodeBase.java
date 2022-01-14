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
import opc.i4aas.AASPropertyType;
import opc.i4aas.AASValueTypeDataType;


/**
 * Generated on 2021-12-15 11:39:02
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1013")
public abstract class AASPropertyTypeNodeBase extends AASSubmodelElementTypeNode implements AASPropertyType {
    private static GeneratedNodeInitializer<AASPropertyTypeNode> aASPropertyTypeNodeInitializer;

    protected AASPropertyTypeNodeBase(NodeManagerUaNode nodeManager, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(nodeManager, nodeId, browseName, displayName);
    }


    @Override
    public void afterCreate() {
        super.afterCreate();

        // Call afterCreate for each sub-node (if the node has any)
        callAfterCreateIfExists(getValueIdNode());
        GeneratedNodeInitializer<AASPropertyTypeNode> impl = getAASPropertyTypeNodeInitializer();
        if (impl != null) {
            impl.init((AASPropertyTypeNode) this);
        }
    }


    public static GeneratedNodeInitializer<AASPropertyTypeNode> getAASPropertyTypeNodeInitializer() {
        return aASPropertyTypeNodeInitializer;
    }


    public static void setAASPropertyTypeNodeInitializer(GeneratedNodeInitializer<AASPropertyTypeNode> aASPropertyTypeNodeInitializerNewValue) {
        aASPropertyTypeNodeInitializer = aASPropertyTypeNodeInitializerNewValue;
    }


    @Optional
    @Override
    public UaProperty getValueNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Value");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public Object getValue() {
        UaVariable node = getValueNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (Object) value;
    }


    @Optional
    @Override
    public void setValue(Object value) {
        UaVariable node = getValueNode();
        if (node == null) {
            throw new RuntimeException("Setting Value failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting Value failed unexpectedly", e);
        }
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
            throw new RuntimeException("Mandatory node ValueType does not exist");
        }
        Variant value = node.getValue().getValue();
        return (AASValueTypeDataType) value.asEnum(AASValueTypeDataType.class);
    }


    @Mandatory
    @Override
    public void setValueType(AASValueTypeDataType value) {
        UaVariable node = getValueTypeNode();
        if (node == null) {
            throw new RuntimeException("Setting ValueType failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting ValueType failed unexpectedly", e);
        }
    }


    @Optional
    @Override
    public AASReferenceTypeNode getValueIdNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "ValueId");
        return (AASReferenceTypeNode) getComponent(browseName);
    }


    @Override
    public Variant[] callMethod(ServiceContext serviceContext, NodeId methodId,
                                Variant[] inputArguments, StatusCode[] inputArgumentResults,
                                DiagnosticInfo[] inputArgumentDiagnosticInfos)
            throws StatusException {
        return super.callMethod(serviceContext, methodId, inputArguments, inputArgumentResults, inputArgumentDiagnosticInfos);
    }
}
