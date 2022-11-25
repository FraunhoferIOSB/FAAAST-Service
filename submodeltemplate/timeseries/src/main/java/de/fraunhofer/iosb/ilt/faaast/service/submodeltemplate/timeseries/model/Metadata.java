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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.util.AasHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.builder.SubmodelElementCollectionBuilder;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Represents metadata according to SMT TimeSeries.
 */
public class Metadata extends DefaultSubmodelElementCollection {

    @JsonIgnore
    protected Map<String, Datatype> recordMetadata;

    public Metadata() {
        this.recordMetadata = new HashMap<>();
        this.idShort = Constants.TIMESERIES_METADATA_ID_SHORT;
        this.semanticId = ReferenceHelper.globalReference(Constants.METADATA_SEMANTIC_ID);
    }


    /**
     * Creates a new instance based on a {@link io.adminshell.aas.v3.model.SubmodelElementCollection}.
     *
     * @param smc the {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to parse
     * @return the parsed {@link io.adminshell.aas.v3.model.SubmodelElementCollection} as {@link Metadata}, or null if
     *         input is null
     */
    public static Metadata of(SubmodelElementCollection smc) {
        if (smc == null) {
            return null;
        }
        Metadata result = new Metadata();
        AasHelper.applyBasicProperties(smc, result);
        Collection<SubmodelElement> elements = smc.getValues();
        SubmodelElementCollection recordMetadata = AasHelper.getElementByIdShort(
                elements,
                Constants.METADATA_RECORD_METADATA_ID_SHORT, SubmodelElementCollection.class);
        result.recordMetadata = Record.of(recordMetadata)
                .getVariables().entrySet().stream()
                .collect(Collectors.toMap(
                        x -> x.getKey(),
                        x -> x.getValue().getDataType()));
        elements.remove(recordMetadata);
        result.values = elements;
        return result;
    }


    public Map<String, Datatype> getRecordMetadata() {
        return recordMetadata;
    }


    public void setRecordMetadata(Map<String, Datatype> recordMetadata) {
        this.recordMetadata = recordMetadata;
    }


    @Override
    public Collection<SubmodelElement> getValues() {
        return new ArrayList<>(Arrays.asList(
                new DefaultSubmodelElementCollection.Builder()
                        .idShort(Constants.METADATA_RECORD_METADATA_ID_SHORT)
                        .values(recordMetadata.entrySet().stream()
                                .map(field -> new DefaultProperty.Builder()
                                        .idShort(field.getKey())
                                        .valueType(field.getValue().getName())
                                        .build())
                                .collect(Collectors.toList()))
                        .build()));
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends Metadata, B extends AbstractBuilder<T, B>> extends SubmodelElementCollectionBuilder<T, B> {

        public B recordMetadata(Map<String, Datatype> value) {
            getBuildingInstance().setRecordMetadata(value);
            return getSelf();
        }


        public B recordMetadata(String name, Datatype value) {
            getBuildingInstance().getRecordMetadata().put(name, value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<Metadata, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected Metadata newBuildingInstance() {
            return new Metadata();
        }
    }
}
