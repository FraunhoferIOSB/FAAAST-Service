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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Represents a record according to SMT TimeSeries.
 */
public class Record extends DefaultSubmodelElementCollection {

    private ZonedDateTime time;
    private Map<String, TypedValue> variables;

    public Record() {
        this.variables = new HashMap<>();
    }


    public Record(ZonedDateTime time) {
        this.time = time;
        this.variables = new HashMap<>();
    }


    @Override
    public Collection<SubmodelElement> getValues() {
        List<SubmodelElement> result = variables.entrySet().stream()
                .map(x -> new DefaultProperty.Builder()
                        .idShort(x.getKey())
                        .valueType(x.getValue().getDataType().getName())
                        .value(x.getValue().asString())
                        .build())
                .collect(Collectors.toList());
        result.add(new DefaultProperty.Builder()
                .idShort("time")
                .valueType(Datatype.DATE_TIME.getName())
                .value(time.toString())
                .build());
        return result;
    }


    public ZonedDateTime getTime() {
        return time;
    }


    public Map<String, TypedValue> getVariables() {
        return variables;
    }
}
