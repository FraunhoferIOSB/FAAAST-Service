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
import opc.i4aas.AASDataSpecificationIEC61360Type;
import opc.i4aas.AASDataTypeIEC61360DataType;
import opc.i4aas.AASLevelTypeDataType;


/**
 * Generated on 2022-02-08 12:58:54
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1028")
public abstract class AASDataSpecificationIEC61360TypeNodeBase extends AASDataSpecificationTypeNode implements AASDataSpecificationIEC61360Type {
    private static GeneratedNodeInitializer<AASDataSpecificationIEC61360TypeNode> aASDataSpecificationIEC61360TypeNodeInitializer;

    protected AASDataSpecificationIEC61360TypeNodeBase(NodeManagerUaNode nodeManager, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(nodeManager, nodeId, browseName, displayName);
    }


    @Override
    public void afterCreate() {
        super.afterCreate();

        // Call afterCreate for each sub-node (if the node has any)
        callAfterCreateIfExists(getIdentificationNode());
        callAfterCreateIfExists(getUnitIdNode());
        callAfterCreateIfExists(getValueIdNode());
        callAfterCreateIfExists(getValueListNode());
        GeneratedNodeInitializer<AASDataSpecificationIEC61360TypeNode> impl = getAASDataSpecificationIEC61360TypeNodeInitializer();
        if (impl != null) {
            impl.init((AASDataSpecificationIEC61360TypeNode) this);
        }
    }


    public static GeneratedNodeInitializer<AASDataSpecificationIEC61360TypeNode> getAASDataSpecificationIEC61360TypeNodeInitializer() {
        return aASDataSpecificationIEC61360TypeNodeInitializer;
    }


    public static void setAASDataSpecificationIEC61360TypeNodeInitializer(GeneratedNodeInitializer<AASDataSpecificationIEC61360TypeNode> aASDataSpecificationIEC61360TypeNodeInitializerNewValue) {
        aASDataSpecificationIEC61360TypeNodeInitializer = aASDataSpecificationIEC61360TypeNodeInitializerNewValue;
    }


    @Optional
    @Override
    public UaProperty getDataTypeNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "DataType");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public AASDataTypeIEC61360DataType getDataType() {
        UaVariable node = getDataTypeNode();
        if (node == null) {
            return null;
        }
        Variant value = node.getValue().getValue();
        return (AASDataTypeIEC61360DataType) value.asEnum(AASDataTypeIEC61360DataType.class);
    }


    @Optional
    @Override
    public void setDataType(AASDataTypeIEC61360DataType value) {
        UaVariable node = getDataTypeNode();
        if (node == null) {
            throw new RuntimeException("Setting DataType failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting DataType failed unexpectedly", e);
        }
    }


    @Mandatory
    @Override
    public UaProperty getDefaultInstanceBrowseNameNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "DefaultInstanceBrowseName");
        return getProperty(browseName);
    }


    @Mandatory
    @Override
    public String getDefaultInstanceBrowseName() {
        UaVariable node = getDefaultInstanceBrowseNameNode();
        if (node == null) {
            throw new RuntimeException("Mandatory node DefaultInstanceBrowseName does not exist");
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Mandatory
    @Override
    public void setDefaultInstanceBrowseName(String value) {
        UaVariable node = getDefaultInstanceBrowseNameNode();
        if (node == null) {
            throw new RuntimeException("Setting DefaultInstanceBrowseName failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting DefaultInstanceBrowseName failed unexpectedly", e);
        }
    }


    @Optional
    @Override
    public UaProperty getDefinitionNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Definition");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public LocalizedText[] getDefinition() {
        UaVariable node = getDefinitionNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (LocalizedText[]) value;
    }


    @Optional
    @Override
    public void setDefinition(LocalizedText[] value) {
        UaVariable node = getDefinitionNode();
        if (node == null) {
            throw new RuntimeException("Setting Definition failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting Definition failed unexpectedly", e);
        }
    }


    @Optional
    @Override
    public UaProperty getLevelTypeNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "LevelType");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public AASLevelTypeDataType getLevelType() {
        UaVariable node = getLevelTypeNode();
        if (node == null) {
            return null;
        }
        Variant value = node.getValue().getValue();
        return (AASLevelTypeDataType) value.asEnum(AASLevelTypeDataType.class);
    }


    @Optional
    @Override
    public void setLevelType(AASLevelTypeDataType value) {
        UaVariable node = getLevelTypeNode();
        if (node == null) {
            throw new RuntimeException("Setting LevelType failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting LevelType failed unexpectedly", e);
        }
    }


    @Mandatory
    @Override
    public UaProperty getPreferredNameNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "PreferredName");
        return getProperty(browseName);
    }


    @Mandatory
    @Override
    public LocalizedText[] getPreferredName() {
        UaVariable node = getPreferredNameNode();
        if (node == null) {
            throw new RuntimeException("Mandatory node PreferredName does not exist");
        }
        Object value = node.getValue().getValue().getValue();
        return (LocalizedText[]) value;
    }


    @Mandatory
    @Override
    public void setPreferredName(LocalizedText[] value) {
        UaVariable node = getPreferredNameNode();
        if (node == null) {
            throw new RuntimeException("Setting PreferredName failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting PreferredName failed unexpectedly", e);
        }
    }


    @Optional
    @Override
    public UaProperty getShortNameNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "ShortName");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public LocalizedText[] getShortName() {
        UaVariable node = getShortNameNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (LocalizedText[]) value;
    }


    @Optional
    @Override
    public void setShortName(LocalizedText[] value) {
        UaVariable node = getShortNameNode();
        if (node == null) {
            throw new RuntimeException("Setting ShortName failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting ShortName failed unexpectedly", e);
        }
    }


    @Optional
    @Override
    public UaProperty getSourceOfDefinitionNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "SourceOfDefinition");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public String getSourceOfDefinition() {
        UaVariable node = getSourceOfDefinitionNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Optional
    @Override
    public void setSourceOfDefinition(String value) {
        UaVariable node = getSourceOfDefinitionNode();
        if (node == null) {
            throw new RuntimeException("Setting SourceOfDefinition failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting SourceOfDefinition failed unexpectedly", e);
        }
    }


    @Optional
    @Override
    public UaProperty getSymbolNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Symbol");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public String getSymbol() {
        UaVariable node = getSymbolNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Optional
    @Override
    public void setSymbol(String value) {
        UaVariable node = getSymbolNode();
        if (node == null) {
            throw new RuntimeException("Setting Symbol failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting Symbol failed unexpectedly", e);
        }
    }


    @Optional
    @Override
    public UaProperty getUnitNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Unit");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public String getUnit() {
        UaVariable node = getUnitNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Optional
    @Override
    public void setUnit(String value) {
        UaVariable node = getUnitNode();
        if (node == null) {
            throw new RuntimeException("Setting Unit failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting Unit failed unexpectedly", e);
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


    @Optional
    @Override
    public UaProperty getValueFormatNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "ValueFormat");
        return getProperty(browseName);
    }


    @Optional
    @Override
    public String getValueFormat() {
        UaVariable node = getValueFormatNode();
        if (node == null) {
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Optional
    @Override
    public void setValueFormat(String value) {
        UaVariable node = getValueFormatNode();
        if (node == null) {
            throw new RuntimeException("Setting ValueFormat failed: does not exist (Optional Nodes must be configured in NodeBuilder)");
        }
        try {
            node.setValue(value);
        }
        catch (StatusException e) {
            throw new RuntimeException("Setting ValueFormat failed unexpectedly", e);
        }
    }


    @Mandatory
    @Override
    public AASIdentifierTypeNode getIdentificationNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Identification");
        return (AASIdentifierTypeNode) getComponent(browseName);
    }


    @Optional
    @Override
    public AASReferenceTypeNode getUnitIdNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "UnitId");
        return (AASReferenceTypeNode) getComponent(browseName);
    }


    @Optional
    @Override
    public AASReferenceTypeNode getValueIdNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "ValueId");
        return (AASReferenceTypeNode) getComponent(browseName);
    }


    @Optional
    @Override
    public BaseObjectTypeNode getValueListNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "ValueList");
        return (BaseObjectTypeNode) getComponent(browseName);
    }


    @Override
    public Variant[] callMethod(ServiceContext serviceContext, NodeId methodId,
                                Variant[] inputArguments, StatusCode[] inputArgumentResults,
                                DiagnosticInfo[] inputArgumentDiagnosticInfos)
            throws StatusException {
        return super.callMethod(serviceContext, methodId, inputArguments, inputArgumentResults, inputArgumentDiagnosticInfos);
    }
}
