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
import com.prosysopc.ua.types.opcua.server.FileTypeNode;
import opc.i4aas.AASFileType;


/**
 * Generated on 2021-12-15 11:39:02
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1017")
public abstract class AASFileTypeNodeBase extends AASSubmodelElementTypeNode implements AASFileType {
    private static GeneratedNodeInitializer<AASFileTypeNode> aASFileTypeNodeInitializer;

    protected AASFileTypeNodeBase(NodeManagerUaNode nodeManager, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(nodeManager, nodeId, browseName, displayName);
    }


    @Override
    public void afterCreate() {
        super.afterCreate();

        // Call afterCreate for each sub-node (if the node has any)
        callAfterCreateIfExists(getFileNode());
        GeneratedNodeInitializer<AASFileTypeNode> impl = getAASFileTypeNodeInitializer();
        if (impl != null) {
            impl.init((AASFileTypeNode) this);
        }
    }


    public static GeneratedNodeInitializer<AASFileTypeNode> getAASFileTypeNodeInitializer() {
        return aASFileTypeNodeInitializer;
    }


    public static void setAASFileTypeNodeInitializer(GeneratedNodeInitializer<AASFileTypeNode> aASFileTypeNodeInitializerNewValue) {
        aASFileTypeNodeInitializer = aASFileTypeNodeInitializerNewValue;
    }


    @Mandatory
    @Override
    public UaProperty getMimeTypeNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "MimeType");
        return getProperty(browseName);
    }


    @Mandatory
    @Override
    public String getMimeType() {
        UaVariable node = getMimeTypeNode();
        if (node == null) {
            throw new RuntimeException("Mandatory node MimeType does not exist");
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Mandatory
    @Override
    public void setMimeType(String value) {
        UaVariable node = getMimeTypeNode();
        if (node == null) {
            throw new RuntimeException("Setting MimeType failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting MimeType failed unexpectedly", e);
        }
    }


    @Optional
    @Override
    public UaProperty getValueNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Value");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public String getValue() {
        UaVariable node = getValueNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Optional
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


    @Optional
    @Override
    public FileTypeNode getFileNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "File");
        return (FileTypeNode) getComponent(browseName);
    }


    @Override
    public Variant[] callMethod(ServiceContext serviceContext, NodeId methodId,
                                Variant[] inputArguments, StatusCode[] inputArgumentResults,
                                DiagnosticInfo[] inputArgumentDiagnosticInfos)
            throws StatusException {
        return super.callMethod(serviceContext, methodId, inputArguments, inputArgumentResults, inputArgumentDiagnosticInfos);
    }
}
