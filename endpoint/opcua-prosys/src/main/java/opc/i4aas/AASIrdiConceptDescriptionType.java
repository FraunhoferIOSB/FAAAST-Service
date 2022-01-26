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
import com.prosysopc.ua.nodes.UaProperty;
import com.prosysopc.ua.types.opcua.IrdiDictionaryEntryType;


/**
 * Generated on 2022-01-26 16:50:24
 */
@TypeDefinitionId("nsu=http://opcfoundation.org/UA/I4AAS/V3/;i=1024")
public interface AASIrdiConceptDescriptionType extends IrdiDictionaryEntryType {
    String CATEGORY = "Category";

    String ADMINISTRATION = "Administration";

    String DATA_SPECIFICATION = "DataSpecification";

    String IDENTIFICATION = "Identification";

    String IS_CASE_OF = "IsCaseOf";

    @Mandatory
    UaProperty getCategoryNode();


    @Mandatory
    String getCategory();


    @Mandatory
    void setCategory(String value) throws StatusException;


    @Mandatory
    AASAdministrativeInformationType getAdministrationNode();


    @Mandatory
    AASReferenceList getDataSpecificationNode();


    @Mandatory
    AASIdentifierType getIdentificationNode();


    @Mandatory
    AASReferenceList getIsCaseOfNode();
}
