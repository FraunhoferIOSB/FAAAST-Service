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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import io.adminshell.aas.v3.model.MultiLanguageProperty;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Represents metadata according to SMT TimeSeries.
 */
public abstract class Metadata extends DefaultSubmodelElementCollection {

    protected MultiLanguageProperty name;
    protected Map<String, Datatype> recordMetadata;

    @Override
    public Collection<SubmodelElement> getValues() {
        return new ArrayList<>(Arrays.asList(
                new DefaultSubmodelElementCollection.Builder()
                        .idShort("RecordMetadata")
                        .values(recordMetadata.entrySet().stream()
                                .map(field -> new DefaultProperty.Builder()
                                        .idShort(field.getKey())
                                        .valueType(field.getValue().getName())
                                        .build())
                                .collect(Collectors.toList()))
                        .build()));
    }
}
