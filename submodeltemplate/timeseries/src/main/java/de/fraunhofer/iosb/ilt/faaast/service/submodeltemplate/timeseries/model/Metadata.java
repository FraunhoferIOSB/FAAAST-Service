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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ExtendableSubmodelElementCollection;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ValueWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.Wrapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.SubmodelElementCollectionBuilder;


/**
 * Represents metadata according to SMT TimeSeries.
 */
public class Metadata extends ExtendableSubmodelElementCollection {

    @JsonIgnore
    private Wrapper<Record, SubmodelElementCollection> recordMetadata = new ValueWrapper<>(
            value,
            new Record(),
            true,
            SubmodelElementCollection.class,
            x -> {
                if (x != null) {
                    x.setIdShort(Constants.METADATA_RECORD_METADATA_ID_SHORT);
                }
                return x;
            },
            x -> ((x == null) || Objects.equals(x.getSemanticId(), ReferenceBuilder.global(Constants.RECORD_SEMANTIC_ID))),
            x -> ExtendableSubmodelElementCollection.genericOf(new Record(), x));

    public Metadata() {
        withAdditionalValues(recordMetadata);
        this.idShort = Constants.TIMESERIES_METADATA_ID_SHORT;
        this.semanticId = ReferenceBuilder.global(Constants.METADATA_SEMANTIC_ID);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (obj == null) {
            return false;
        }
        else if (this.getClass() != obj.getClass()) {
            return false;
        }
        return super.equals(obj);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }


    /**
     * Creates a new instance based on a {@link io.adminshell.aas.v3.model.SubmodelElementCollection}.
     *
     * @param smc the {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to parse
     * 
     * @return the parsed {@link io.adminshell.aas.v3.model.SubmodelElementCollection} as {@link Metadata}, or null if
     *         input is null
     */
    public static Metadata of(SubmodelElementCollection smc) {
        Metadata target = new Metadata();
        Optional<SubmodelElementCollection> smcMetaRecord = smc.getValue().stream().filter(Objects::nonNull)
                .filter(x -> SubmodelElementCollection.class.isAssignableFrom(x.getClass()))
                .filter(x -> Objects.equals(x.getSemanticId(), ReferenceBuilder.global(Constants.RECORD_SEMANTIC_ID)))
                .filter(x -> Objects.equals(x.getIdShort(), Constants.METADATA_RECORD_METADATA_ID_SHORT))
                .map(SubmodelElementCollection.class::cast)
                .findFirst();
        SubmodelElementCollection toParse = smc;
        if (smcMetaRecord.isPresent()) {
            target.setRecordMetadata(Record.of(smcMetaRecord.get()));
            toParse = DeepCopyHelper.deepCopy(smc, SubmodelElementCollection.class);
            toParse.setValue(smc.getValue().stream()
                    .filter(x -> !Objects.equals(smcMetaRecord.get(), x))
                    .collect(Collectors.toList()));
        }
        return ExtendableSubmodelElementCollection.genericOf(target, toParse);
    }


    /**
     * Get metadata of records without timestamp. Transforms TypedValue of metadata to Datatype.
     *
     * @return metadata of the variables.
     */
    @JsonIgnore
    public Map<String, Property> getMetadataRecordVariables() {
        return this.recordMetadata.getValue().getTimesAndVariables();
    }


    /**
     * Transform Datatype to TypedValues and sets the record matadata accordingly.
     */
    @JsonIgnore
    void setMetadataRecordVariables(Map<String, Property> recordMetadata) {
        this.recordMetadata.getValue().setTimesAndVariables(recordMetadata);
    }


    /**
     * Get metadata of records.
     *
     * @return metadata of the records.
     */
    public Record getRecordMetadata() {
        return this.recordMetadata.getValue();
    }


    /**
     * TransformDatatype to TypedValues and sets the record matadata accordingly.
     */
    void setRecordMetadata(Record recordMetadata) {
        this.recordMetadata.setValue(recordMetadata);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends Metadata, B extends AbstractBuilder<T, B>> extends SubmodelElementCollectionBuilder<T, B> {

        public B recordMetadata(Record value) {
            getBuildingInstance().setRecordMetadata(value);
            return getSelf();
        }


        public B recordMetadataVariables(Map<String, Property> recordVariables) {
            getBuildingInstance().setMetadataRecordVariables(recordVariables);
            return getSelf();
        }


        public B recordMetadataTimeOrVariable(String name, Property value) {
            getBuildingInstance().getRecordMetadata().getTimesAndVariables().put(name, value);
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
