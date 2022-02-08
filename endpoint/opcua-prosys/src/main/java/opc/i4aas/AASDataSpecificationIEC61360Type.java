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
package opc.i4aas;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.TypeDefinitionId;
import com.prosysopc.ua.nodes.Mandatory;
import com.prosysopc.ua.nodes.Optional;
import com.prosysopc.ua.nodes.UaProperty;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.types.opcua.BaseObjectType;


/**
 * Generated on 2022-02-08 12:58:54
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1028")
public interface AASDataSpecificationIEC61360Type extends AASDataSpecificationType {
    String DATA_TYPE = "DataType";

    String DEFAULT_INSTANCE_BROWSE_NAME = "DefaultInstanceBrowseName";

    String DEFINITION = "Definition";

    String LEVEL_TYPE = "LevelType";

    String PREFERRED_NAME = "PreferredName";

    String SHORT_NAME = "ShortName";

    String SOURCE_OF_DEFINITION = "SourceOfDefinition";

    String SYMBOL = "Symbol";

    String UNIT = "Unit";

    String VALUE = "Value";

    String VALUE_FORMAT = "ValueFormat";

    String IDENTIFICATION = "Identification";

    String UNIT_ID = "UnitId";

    String VALUE_ID = "ValueId";

    String VALUE_LIST = "ValueList";

    @Optional
    UaProperty getDataTypeNode();


    @Optional
    AASDataTypeIEC61360DataType getDataType();


    @Optional
    void setDataType(AASDataTypeIEC61360DataType value) throws StatusException;


    @Mandatory
    UaProperty getDefaultInstanceBrowseNameNode();


    @Mandatory
    String getDefaultInstanceBrowseName();


    @Mandatory
    void setDefaultInstanceBrowseName(String value) throws StatusException;


    @Optional
    UaProperty getDefinitionNode();


    @Optional
    LocalizedText[] getDefinition();


    @Optional
    void setDefinition(LocalizedText[] value) throws StatusException;


    @Optional
    UaProperty getLevelTypeNode();


    @Optional
    AASLevelTypeDataType getLevelType();


    @Optional
    void setLevelType(AASLevelTypeDataType value) throws StatusException;


    @Mandatory
    UaProperty getPreferredNameNode();


    @Mandatory
    LocalizedText[] getPreferredName();


    @Mandatory
    void setPreferredName(LocalizedText[] value) throws StatusException;


    @Optional
    UaProperty getShortNameNode();


    @Optional
    LocalizedText[] getShortName();


    @Optional
    void setShortName(LocalizedText[] value) throws StatusException;


    @Optional
    UaProperty getSourceOfDefinitionNode();


    @Optional
    String getSourceOfDefinition();


    @Optional
    void setSourceOfDefinition(String value) throws StatusException;


    @Optional
    UaProperty getSymbolNode();


    @Optional
    String getSymbol();


    @Optional
    void setSymbol(String value) throws StatusException;


    @Optional
    UaProperty getUnitNode();


    @Optional
    String getUnit();


    @Optional
    void setUnit(String value) throws StatusException;


    @Optional
    UaProperty getValueNode();


    @Optional
    Object getValue();


    @Optional
    void setValue(Object value) throws StatusException;


    @Optional
    UaProperty getValueFormatNode();


    @Optional
    String getValueFormat();


    @Optional
    void setValueFormat(String value) throws StatusException;


    @Mandatory
    AASIdentifierType getIdentificationNode();


    @Optional
    AASReferenceType getUnitIdNode();


    @Optional
    AASReferenceType getValueIdNode();


    @Optional
    BaseObjectType getValueListNode();
}
