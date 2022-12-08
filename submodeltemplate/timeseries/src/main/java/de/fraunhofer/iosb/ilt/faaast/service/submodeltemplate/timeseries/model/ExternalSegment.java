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
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.File;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.util.Objects;


/**
 * Represents a linked segment according to SMT TimeSeries.
 */
public class ExternalSegment extends Segment {

    @JsonIgnore
    private final Wrapper<File, File> file = new ValueWrapper<>(
            values,
            null,
            true,
            File.class,
            x -> {
                x.setSemanticId(ReferenceHelper.globalReference(Constants.FILE_SEMANTIC_ID));
                return x;
            },
            x -> Objects.equals(ReferenceHelper.globalReference(Constants.FILE_SEMANTIC_ID), x.getSemanticId()),
            x -> x);

    /**
     * Creates a new instance based on a {@link io.adminshell.aas.v3.model.SubmodelElementCollection}.
     *
     * @param smc the {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to parse
     * @return the parsed {@link io.adminshell.aas.v3.model.SubmodelElementCollection} as {@link ExternalSegment}, or
     *         null if input is null
     */
    public static ExternalSegment of(SubmodelElementCollection smc) {
        return Segment.of(new ExternalSegment(), smc);
    }


    public ExternalSegment() {
        this.idShort = IdentifierHelper.randomId("ExternalSegment");
        this.semanticId = ReferenceHelper.globalReference(Constants.EXTERNAL_SEGMENT_SEMANTIC_ID);
        withAdditionalValues(file);
    }


    public File getFile() {
        return file.getValue();
    }


    /**
     * Sets the file.
     *
     * @param file the file to set.
     */
    public void setFile(File file) {
        this.file.setValue(file);
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
            return super.equals(obj);
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

        public B file(File value) {
            getBuildingInstance().setFile(value);
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
