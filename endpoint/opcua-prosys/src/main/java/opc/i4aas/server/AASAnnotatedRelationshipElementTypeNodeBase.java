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
import com.prosysopc.ua.server.GeneratedNodeInitializer;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.stack.builtintypes.DiagnosticInfo;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.builtintypes.Variant;
import opc.i4aas.AASAnnotatedRelationshipElementType;


/**
 * Generated on 2022-02-08 12:58:54
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1019")
public abstract class AASAnnotatedRelationshipElementTypeNodeBase extends AASRelationshipElementTypeNode implements AASAnnotatedRelationshipElementType {
    private static GeneratedNodeInitializer<AASAnnotatedRelationshipElementTypeNode> aASAnnotatedRelationshipElementTypeNodeInitializer;

    protected AASAnnotatedRelationshipElementTypeNodeBase(NodeManagerUaNode nodeManager,
            NodeId nodeId, QualifiedName browseName, LocalizedText displayName) {
        super(nodeManager, nodeId, browseName, displayName);
    }


    @Override
    public void afterCreate() {
        super.afterCreate();

        // Call afterCreate for each sub-node (if the node has any)
        callAfterCreateIfExists(getAnnotationNode());
        GeneratedNodeInitializer<AASAnnotatedRelationshipElementTypeNode> impl = getAASAnnotatedRelationshipElementTypeNodeInitializer();
        if (impl != null) {
            impl.init((AASAnnotatedRelationshipElementTypeNode) this);
        }
    }


    public static GeneratedNodeInitializer<AASAnnotatedRelationshipElementTypeNode> getAASAnnotatedRelationshipElementTypeNodeInitializer() {
        return aASAnnotatedRelationshipElementTypeNodeInitializer;
    }


    public static void setAASAnnotatedRelationshipElementTypeNodeInitializer(GeneratedNodeInitializer<AASAnnotatedRelationshipElementTypeNode> aASAnnotatedRelationshipElementTypeNodeInitializerNewValue) {
        aASAnnotatedRelationshipElementTypeNodeInitializer = aASAnnotatedRelationshipElementTypeNodeInitializerNewValue;
    }


    @Mandatory
    @Override
    public AASSubmodelElementListNode getAnnotationNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Annotation");
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
