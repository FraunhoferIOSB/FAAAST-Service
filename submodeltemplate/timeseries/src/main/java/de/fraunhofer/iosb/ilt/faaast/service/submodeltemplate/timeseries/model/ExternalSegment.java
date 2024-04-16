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
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.ValueWrapper;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper.Wrapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.DataElement;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;


/**
 * Represents an external segment according to SMT TimeSeries.
 */
public class ExternalSegment extends Segment {

    @JsonIgnore
    private Wrapper<File, File> file = new ValueWrapper<>(
            value,
            null,
            true,
            File.class,
            x -> {
                if (x != null) {
                    x.setSemanticId(ReferenceBuilder.global(Constants.FILE_SEMANTIC_ID));
                }
                return x;
            },
            x -> Objects.equals(ReferenceBuilder.global(Constants.FILE_SEMANTIC_ID), x.getSemanticId()),
            x -> x);

    @JsonIgnore
    private Wrapper<Blob, Blob> blob = new ValueWrapper<>(
            value,
            null,
            true,
            Blob.class,
            x -> {
                if (x != null) {
                    x.setSemanticId(ReferenceBuilder.global(Constants.BLOB_SEMANTIC_ID));
                }
                return x;
            },
            x -> Objects.equals(ReferenceBuilder.global(Constants.BLOB_SEMANTIC_ID), x.getSemanticId()),
            x -> x);

    @JsonIgnore
    private Wrapper<? extends DataElement, ? extends DataElement> data;

    /**
     * Creates a new instance based on a {@link io.adminshell.aas.v3.model.SubmodelElementCollection}.
     *
     * @param smc the {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to parse
     * @return the parsed {@link io.adminshell.aas.v3.model.SubmodelElementCollection} as {@link ExternalSegment}, or
     *         null if input is null
     */
    public static ExternalSegment of(SubmodelElementCollection smc) {
        ExternalSegment target = new ExternalSegment();
        Optional<File> smcFile = createDataObject(smc, File.class);
        Optional<Blob> smcBlob = createDataObject(smc, Blob.class);
        SubmodelElementCollection toParse = DeepCopyHelper.deepCopy(smc, SubmodelElementCollection.class);
        if (smcFile.isPresent()) {
            target.setData(smcFile.get());
            toParse.setValue(smc.getValue().stream()
                    .filter(x -> !Objects.equals(smcFile.get(), x))
                    .collect(Collectors.toList()));
        }
        else if (smcBlob.isPresent()) {
            target.setData(smcBlob.get());
            toParse.setValue(smc.getValue().stream()
                    .filter(x -> !Objects.equals(smcBlob.get(), x))
                    .collect(Collectors.toList()));
        }

        return Segment.of(target, toParse);
    }


    private static <T extends DataElement> Optional<T> createDataObject(SubmodelElementCollection smc, Class<T> type) {
        String semanticID = type.equals(File.class) ? Constants.FILE_SEMANTIC_ID : Constants.BLOB_SEMANTIC_ID;
        return smc.getValue().stream()
                .filter(Objects::nonNull)
                .filter(x -> type.isAssignableFrom(x.getClass()))
                .map(type::cast)
                .filter(x -> Objects.equals(x.getSemanticId(), ReferenceBuilder.global(semanticID)))
                .findFirst();
    }


    public ExternalSegment() {
        this.idShort = IdHelper.randomId("ExternalSegment");
        this.semanticId = ReferenceBuilder.global(Constants.EXTERNAL_SEGMENT_SEMANTIC_ID);
        if (file != null) {
            data = file;
        }
        else {
            data = blob;
        }
        withAdditionalValues(data);
    }


    public DataElement getData() {
        return data.getValue();
    }


    /**
     * Sets the data, if data is of type File.
     *
     * @param data the data to set.
     */
    public void setData(File data) {
        this.file.setValue(data);
        this.data = this.file;
    }


    /**
     * Sets the data, if data is of type Blob.
     *
     * @param data the data to set. Either File or Blob. Other data is ignored.
     */
    public void setData(Blob data) {
        this.blob.setValue(data);
        this.data = this.blob;
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
        else {
            ExternalSegment other = (ExternalSegment) obj;
            return super.equals(obj)
                    && Objects.equals(this.data, other.data);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends ExternalSegment, B extends AbstractBuilder<T, B>> extends Segment.AbstractBuilder<T, B> {

        public B data(File value) {
            getBuildingInstance().setData(value);
            return getSelf();
        }


        public B data(Blob value) {
            getBuildingInstance().setData(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<ExternalSegment, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ExternalSegment newBuildingInstance() {
            return new ExternalSegment();
        }

    }

}
