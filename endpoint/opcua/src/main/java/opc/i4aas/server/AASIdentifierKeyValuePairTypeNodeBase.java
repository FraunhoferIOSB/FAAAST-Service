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
import com.prosysopc.ua.types.opcua.server.BaseObjectTypeNode;
import opc.i4aas.AASIdentifierKeyValuePairType;


/**
 * Generated on 2022-02-08 12:58:54
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1035")
public abstract class AASIdentifierKeyValuePairTypeNodeBase extends BaseObjectTypeNode implements AASIdentifierKeyValuePairType {
    private static GeneratedNodeInitializer<AASIdentifierKeyValuePairTypeNode> aASIdentifierKeyValuePairTypeNodeInitializer;

    protected AASIdentifierKeyValuePairTypeNodeBase(NodeManagerUaNode nodeManager, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(nodeManager, nodeId, browseName, displayName);
    }


    @Override
    public void afterCreate() {
        super.afterCreate();

        // Call afterCreate for each sub-node (if the node has any)
        callAfterCreateIfExists(getExternalSubjectIdNode());
        GeneratedNodeInitializer<AASIdentifierKeyValuePairTypeNode> impl = getAASIdentifierKeyValuePairTypeNodeInitializer();
        if (impl != null) {
            impl.init((AASIdentifierKeyValuePairTypeNode) this);
        }
    }


    public static GeneratedNodeInitializer<AASIdentifierKeyValuePairTypeNode> getAASIdentifierKeyValuePairTypeNodeInitializer() {
        return aASIdentifierKeyValuePairTypeNodeInitializer;
    }


    public static void setAASIdentifierKeyValuePairTypeNodeInitializer(GeneratedNodeInitializer<AASIdentifierKeyValuePairTypeNode> aASIdentifierKeyValuePairTypeNodeInitializerNewValue) {
        aASIdentifierKeyValuePairTypeNodeInitializer = aASIdentifierKeyValuePairTypeNodeInitializerNewValue;
    }


    @Mandatory
    @Override
    public UaProperty getKeyNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Key");
        return getProperty(browseName);
    }


    @Mandatory
    @Override
    public String getKey() {
        UaVariable node = getKeyNode();
        if (node == null) {
            throw new RuntimeException("Mandatory node Key does not exist");
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Mandatory
    @Override
    public void setKey(String value) {
        UaVariable node = getKeyNode();
        if (node == null) {
            throw new RuntimeException("Setting Key failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting Key failed unexpectedly", e);
        }
    }


    @Mandatory
    @Override
    public UaProperty getValueNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Value");
        return getProperty(browseName);
    }


    @Mandatory
    @Override
    public String getValue() {
        UaVariable node = getValueNode();
        if (node == null) {
            throw new RuntimeException("Mandatory node Value does not exist");
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Mandatory
    @Override
    public void setValue(String value) {
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
    public AASReferenceTypeNode getExternalSubjectIdNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "ExternalSubjectId");
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
