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
import opc.i4aas.AASAdministrativeInformationType;


/**
 * Generated on 2022-02-08 12:58:54
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1030")
public abstract class AASAdministrativeInformationTypeNodeBase extends BaseObjectTypeNode implements AASAdministrativeInformationType {
    private static GeneratedNodeInitializer<AASAdministrativeInformationTypeNode> aASAdministrativeInformationTypeNodeInitializer;

    protected AASAdministrativeInformationTypeNodeBase(NodeManagerUaNode nodeManager, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(nodeManager, nodeId, browseName, displayName);
    }


    @Override
    public void afterCreate() {
        super.afterCreate();

        // Call afterCreate for each sub-node (if the node has any)
        GeneratedNodeInitializer<AASAdministrativeInformationTypeNode> impl = getAASAdministrativeInformationTypeNodeInitializer();
        if (impl != null) {
            impl.init((AASAdministrativeInformationTypeNode) this);
        }
    }


    public static GeneratedNodeInitializer<AASAdministrativeInformationTypeNode> getAASAdministrativeInformationTypeNodeInitializer() {
        return aASAdministrativeInformationTypeNodeInitializer;
    }


    public static void setAASAdministrativeInformationTypeNodeInitializer(GeneratedNodeInitializer<AASAdministrativeInformationTypeNode> aASAdministrativeInformationTypeNodeInitializerNewValue) {
        aASAdministrativeInformationTypeNodeInitializer = aASAdministrativeInformationTypeNodeInitializerNewValue;
    }


    @Optional
    @Override
    public UaProperty getRevisionNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Revision");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public String getRevision() {
        UaVariable node = getRevisionNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Optional
    @Override
    public void setRevision(String value) {
        UaVariable node = getRevisionNode();
        if (node == null) {
            throw new RuntimeException("Setting Revision failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting Revision failed unexpectedly", e);
        }
    }


    @Optional
    @Override
    public UaProperty getVersionNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Version");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public String getVersion() {
        UaVariable node = getVersionNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Optional
    @Override
    public void setVersion(String value) {
        UaVariable node = getVersionNode();
        if (node == null) {
            throw new RuntimeException("Setting Version failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting Version failed unexpectedly", e);
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
