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


/**
 * Generated on 2022-02-08 12:58:54
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1022")
public interface AASEntityType extends AASSubmodelElementType {
    String ENTITY_TYPE = "EntityType";

    String GLOBAL_ASSET_ID = "GlobalAssetId";

    String SPECIFIC_ASSET_ID = "SpecificAssetId";

    String STATEMENT = "Statement";

    @Mandatory
    UaProperty getEntityTypeNode();


    @Mandatory
    AASEntityTypeDataType getEntityType();


    @Mandatory
    void setEntityType(AASEntityTypeDataType value) throws StatusException;


    @Optional
    AASReferenceType getGlobalAssetIdNode();


    @Optional
    AASIdentifierKeyValuePairType getSpecificAssetIdNode();


    @Mandatory
    AASSubmodelElementList getStatementNode();
}
