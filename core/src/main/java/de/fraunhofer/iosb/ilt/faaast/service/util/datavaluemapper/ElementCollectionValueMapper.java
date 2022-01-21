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
package de.fraunhofer.iosb.ilt.faaast.service.util.datavaluemapper;

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.DataElementValueMapper;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.util.stream.Collectors;


public class ElementCollectionValueMapper extends DataValueMapper<SubmodelElementCollection, ElementCollectionValue> {

    @Override
    public ElementCollectionValue toDataElementValue(SubmodelElementCollection elementCollection) {
        if (elementCollection == null) {
            return null;
        }
        return ElementCollectionValue.builder()
                .values(elementCollection.getValues().stream().collect(Collectors.toMap(
                        x -> x.getIdShort(),
                        x -> DataElementValueMapper.toDataElement(x))))
                .build();
    }


    @Override
    public SubmodelElementCollection setDataElementValue(SubmodelElementCollection elementCollection, ElementCollectionValue value) {
        if (elementCollection == null || value == null) {
            return elementCollection;
        }
        for (SubmodelElement element: elementCollection.getValues()) {
            if (value.getValues().containsKey(element.getIdShort())) {
                DataElementValueMapper.setDataElementValue(element, value.getValues().get(element.getIdShort()));
            }
        }
        return elementCollection;
    }
}
