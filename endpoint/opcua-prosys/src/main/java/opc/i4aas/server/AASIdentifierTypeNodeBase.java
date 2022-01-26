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
import opc.i4aas.AASIdentifierType;
import opc.i4aas.AASIdentifierTypeDataType;


/**
 * Generated on 2022-01-26 16:50:24
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1029")
public abstract class AASIdentifierTypeNodeBase extends BaseObjectTypeNode implements AASIdentifierType {
    private static GeneratedNodeInitializer<AASIdentifierTypeNode> aASIdentifierTypeNodeInitializer;

    protected AASIdentifierTypeNodeBase(NodeManagerUaNode nodeManager, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(nodeManager, nodeId, browseName, displayName);
    }


    @Override
    public void afterCreate() {
        super.afterCreate();

        // Call afterCreate for each sub-node (if the node has any)
        GeneratedNodeInitializer<AASIdentifierTypeNode> impl = getAASIdentifierTypeNodeInitializer();
        if (impl != null) {
            impl.init((AASIdentifierTypeNode) this);
        }
    }


    public static GeneratedNodeInitializer<AASIdentifierTypeNode> getAASIdentifierTypeNodeInitializer() {
        return aASIdentifierTypeNodeInitializer;
    }


    public static void setAASIdentifierTypeNodeInitializer(GeneratedNodeInitializer<AASIdentifierTypeNode> aASIdentifierTypeNodeInitializerNewValue) {
        aASIdentifierTypeNodeInitializer = aASIdentifierTypeNodeInitializerNewValue;
    }


    @Mandatory
    @Override
    public UaProperty getIdNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Id");
        return getProperty(browseName);
    }


    @Mandatory
    @Override
    public String getId() {
        UaVariable node = getIdNode();
        if (node == null) {
            throw new RuntimeException("Mandatory node Id does not exist");
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Mandatory
    @Override
    public void setId(String value) {
        UaVariable node = getIdNode();
        if (node == null) {
            throw new RuntimeException("Setting Id failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting Id failed unexpectedly", e);
        }
    }


    @Mandatory
    @Override
    public UaProperty getIdTypeNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "IdType");
        return getProperty(browseName);
    }


    @Mandatory
    @Override
    public AASIdentifierTypeDataType getIdType() {
        UaVariable node = getIdTypeNode();
        if (node == null) {
            throw new RuntimeException("Mandatory node IdType does not exist");
        }
        Variant value = node.getValue().getValue();
        return (AASIdentifierTypeDataType) value.asEnum(AASIdentifierTypeDataType.class);
    }


    @Mandatory
    @Override
    public void setIdType(AASIdentifierTypeDataType value) {
        UaVariable node = getIdTypeNode();
        if (node == null) {
            throw new RuntimeException("Setting IdType failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting IdType failed unexpectedly", e);
        }
    }


    @Override
    public Variant[] callMethod(ServiceContext serviceContext, NodeId methodId,
                                Variant[] inputArguments, StatusCode[] inputArgumentResults,
                                DiagnosticInfo[] inputArgumentDiagnosticInfos)
            throws StatusException {
        return super.callMethod(serviceContext, methodId, inputArguments, inputArgumentResults, inputArgumentDiagnosticInfos);
    }
}
