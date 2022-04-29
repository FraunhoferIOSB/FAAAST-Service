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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.util;

import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.ArrayList;
import java.util.List;


public class ReferenceBuilderHelper {

    private ReferenceBuilderHelper() {}


    public static Reference build(String aasIdentifier, String submodelIdentifier, String submodelElementIdshort) {
        List<Key> keyList = new ArrayList<>();
        keyList.add(new DefaultKey.Builder()
                .idType(KeyType.IRI)
                .type(null)
                .value(aasIdentifier)
                .build());
        keyList.add(new DefaultKey.Builder()
                .idType(KeyType.IRI)
                .type(null)
                .value(submodelIdentifier)
                .build());
        keyList.add(new DefaultKey.Builder()
                .idType(KeyType.ID_SHORT)
                .type(null)
                .value(submodelElementIdshort)
                .build());
        Reference reference = new DefaultReference.Builder()
                .keys(keyList)
                .build();
        return reference;
    }


    public static Reference build(String aasIdentifier, String submodelIdentifier, String submodelElementCollectionIdshort, String submodelElementIdshort) {
        Reference reference = ReferenceBuilderHelper.build(aasIdentifier, submodelIdentifier, submodelElementCollectionIdshort);
        reference.getKeys().add(new DefaultKey.Builder()
                .idType(KeyType.ID_SHORT)
                .type(null)
                .value(submodelElementIdshort)
                .build());
        return reference;
    }


    public static Reference build(String aasIdentifier, String submodelIdentifier) {
        List<Key> keyList = new ArrayList<>();
        keyList.add(new DefaultKey.Builder()
                .idType(KeyType.IRI)
                .type(null)
                .value(aasIdentifier)
                .build());
        keyList.add(new DefaultKey.Builder()
                .idType(KeyType.IRI)
                .type(null)
                .value(submodelIdentifier)
                .build());
        return new DefaultReference.Builder()
                .keys(keyList)
                .build();
    }

}
