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
package opc.i4aas.client;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.TypeDefinitionId;
import com.prosysopc.ua.client.AddressSpace;
import com.prosysopc.ua.nodes.Mandatory;
import com.prosysopc.ua.nodes.Optional;
import com.prosysopc.ua.nodes.UaProperty;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.types.opcua.BaseObjectType;
import opc.i4aas.AASDataSpecificationIEC61360Type;
import opc.i4aas.AASDataTypeIEC61360DataType;
import opc.i4aas.AASIdentifierType;
import opc.i4aas.AASLevelTypeDataType;
import opc.i4aas.AASReferenceType;


/**
 * Generated on 2021-12-15 11:39:02
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1028")
public abstract class AASDataSpecificationIEC61360TypeImplBase extends AASDataSpecificationTypeImpl implements AASDataSpecificationIEC61360Type {
    protected AASDataSpecificationIEC61360TypeImplBase(AddressSpace addressSpace, NodeId nodeId,
            QualifiedName browseName, LocalizedText displayName) {
        super(addressSpace, nodeId, browseName, displayName);
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
    public void setDataType(AASDataTypeIEC61360DataType value) throws StatusException {
        UaVariable node = getDataTypeNode();
        if (node == null) {
            throw new RuntimeException("Setting DataType failed, the Optional node does not exist)");
        }
        node.setValue(value);
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
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (String) value;
    }


    @Mandatory
    @Override
    public void setDefaultInstanceBrowseName(String value) throws StatusException {
        UaVariable node = getDefaultInstanceBrowseNameNode();
        if (node == null) {
            throw new RuntimeException("Setting DefaultInstanceBrowseName failed, the Optional node does not exist)");
        }
        node.setValue(value);
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
    public void setDefinition(LocalizedText[] value) throws StatusException {
        UaVariable node = getDefinitionNode();
        if (node == null) {
            throw new RuntimeException("Setting Definition failed, the Optional node does not exist)");
        }
        node.setValue(value);
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
    public void setLevelType(AASLevelTypeDataType value) throws StatusException {
        UaVariable node = getLevelTypeNode();
        if (node == null) {
            throw new RuntimeException("Setting LevelType failed, the Optional node does not exist)");
        }
        node.setValue(value);
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
            return null;
        }
        Object value = node.getValue().getValue().getValue();
        return (LocalizedText[]) value;
    }


    @Mandatory
    @Override
    public void setPreferredName(LocalizedText[] value) throws StatusException {
        UaVariable node = getPreferredNameNode();
        if (node == null) {
            throw new RuntimeException("Setting PreferredName failed, the Optional node does not exist)");
        }
        node.setValue(value);
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
    public void setShortName(LocalizedText[] value) throws StatusException {
        UaVariable node = getShortNameNode();
        if (node == null) {
            throw new RuntimeException("Setting ShortName failed, the Optional node does not exist)");
        }
        node.setValue(value);
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
    public void setSourceOfDefinition(String value) throws StatusException {
        UaVariable node = getSourceOfDefinitionNode();
        if (node == null) {
            throw new RuntimeException("Setting SourceOfDefinition failed, the Optional node does not exist)");
        }
        node.setValue(value);
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
    public void setSymbol(String value) throws StatusException {
        UaVariable node = getSymbolNode();
        if (node == null) {
            throw new RuntimeException("Setting Symbol failed, the Optional node does not exist)");
        }
        node.setValue(value);
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
    public void setUnit(String value) throws StatusException {
        UaVariable node = getUnitNode();
        if (node == null) {
            throw new RuntimeException("Setting Unit failed, the Optional node does not exist)");
        }
        node.setValue(value);
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
    public void setValue(Object value) throws StatusException {
        UaVariable node = getValueNode();
        if (node == null) {
            throw new RuntimeException("Setting Value failed, the Optional node does not exist)");
        }
        node.setValue(value);
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
    public void setValueFormat(String value) throws StatusException {
        UaVariable node = getValueFormatNode();
        if (node == null) {
            throw new RuntimeException("Setting ValueFormat failed, the Optional node does not exist)");
        }
        node.setValue(value);
    }


    @Mandatory
    @Override
    public AASIdentifierType getIdentificationNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "Identification");
        return (AASIdentifierType) getComponent(browseName);
    }


    @Optional
    @Override
    public AASReferenceType getUnitIdNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "UnitId");
        return (AASReferenceType) getComponent(browseName);
    }


    @Optional
    @Override
    public AASReferenceType getValueIdNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "ValueId");
        return (AASReferenceType) getComponent(browseName);
    }


    @Optional
    @Override
    public BaseObjectType getValueListNode() {
        QualifiedName browseName = getQualifiedName("http://opcfoundation.org/UA/I4AAS/V3/", "ValueList");
        return (BaseObjectType) getComponent(browseName);
    }
}
