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
import com.prosysopc.ua.types.opcua.server.IrdiDictionaryEntryTypeNode;
import opc.i4aas.AASIrdiConceptDescriptionType;


/**
 * Generated on 2021-12-15 11:39:02
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1024")
public abstract class AASIrdiConceptDescriptionTypeNodeBase extends IrdiDictionaryEntryTypeNode implements AASIrdiConceptDescriptionType {
    private static GeneratedNodeInitializer<AASIrdiConceptDescriptionTypeNode> aASIrdiConceptDescriptionTypeNodeInitializer;

    protected AASIrdiConceptDescriptionTypeNodeBase(NodeManagerUaNode nodeManager, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(nodeManager, nodeId, browseName, displayName);
    }


    @Override
    public void afterCreate() {
        super.afterCreate();

        // Call afterCreate for each sub-node (if the node has any)
        callAfterCreateIfExists(getAdministrationNode());
        callAfterCreateIfExists(getDataSpecificationNode());
        callAfterCreateIfExists(getIdentificationNode());
        callAfterCreateIfExists(getIsCaseOfNode());
        GeneratedNodeInitializer<AASIrdiConceptDescriptionTypeNode> impl = getAASIrdiConceptDescriptionTypeNodeInitializer();
        if (impl != null) {
            impl.init((AASIrdiConceptDescriptionTypeNode) this);
        }
    }


    public static GeneratedNodeInitializer<AASIrdiConceptDescriptionTypeNode> getAASIrdiConceptDescriptionTypeNodeInitializer() {
        return aASIrdiConceptDescriptionTypeNodeInitializer;
    }


    public static void setAASIrdiConceptDescriptionTypeNodeInitializer(GeneratedNodeInitializer<AASIrdiConceptDescriptionTypeNode> aASIrdiConceptDescriptionTypeNodeInitializerNewValue) {
        aASIrdiConceptDescriptionTypeNodeInitializer = aASIrdiConceptDescriptionTypeNodeInitializerNewValue;
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
    public AASAdministrativeInformationTypeNode getAdministrationNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Administration");
        return (AASAdministrativeInformationTypeNode) getComponent(browseName);
    }


    @Mandatory
    @Override
    public AASReferenceListNode getDataSpecificationNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "DataSpecification");
        return (AASReferenceListNode) getComponent(browseName);
    }


    @Mandatory
    @Override
    public AASIdentifierTypeNode getIdentificationNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Identification");
        return (AASIdentifierTypeNode) getComponent(browseName);
    }


    @Mandatory
    @Override
    public AASReferenceListNode getIsCaseOfNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "IsCaseOf");
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
