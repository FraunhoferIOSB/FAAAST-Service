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
package de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.MultiLanguagePropertyValue;
import io.adminshell.aas.v3.model.MultiLanguageProperty;
import java.util.ArrayList;
import java.util.HashSet;


public class MultiLanguagePropertyValueMapper implements DataValueMapper<MultiLanguageProperty, MultiLanguagePropertyValue> {

    @Override
    public MultiLanguagePropertyValue toValue(MultiLanguageProperty submodelElement) {
        if (submodelElement == null) {
            return null;
        }
        MultiLanguagePropertyValue multiLanguagePropertyValue = new MultiLanguagePropertyValue();

        //TODO: Check Why is in AAS Model value a List and in MultilanguagePropertyValue a Set?
        multiLanguagePropertyValue.setLangStringSet(submodelElement.getValues() != null ? new HashSet<>(submodelElement.getValues()) : new HashSet<>());
        return multiLanguagePropertyValue;
    }


    @Override
    public MultiLanguageProperty setValue(MultiLanguageProperty submodelElement, MultiLanguagePropertyValue value) {
        DataValueMapper.super.setValue(submodelElement, value);
        submodelElement.setValues(value.getLangStringSet() != null ? new ArrayList<>(value.getLangStringSet()) : new ArrayList<>());
        return submodelElement;
    }
}
