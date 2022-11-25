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
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.Blob;
import io.adminshell.aas.v3.model.File;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Represents a linked segment according to SMT TimeSeries.
 */
public class ExternalSegment extends Segment {

    @JsonIgnore
    private List<File> files;
    @JsonIgnore
    private List<Blob> blobs;

    /**
     * Creates a new instance based on a {@link io.adminshell.aas.v3.model.SubmodelElementCollection}.
     *
     * @param smc the {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to parse
     * @return the parsed {@link io.adminshell.aas.v3.model.SubmodelElementCollection} as {@link LinkedSegment}, or null
     *         if input is null
     */
    public static ExternalSegment of(SubmodelElementCollection smc) {
        if (smc == null) {
            return null;
        }
        ExternalSegment result = new ExternalSegment();
        Segment.of(result, smc);
        // TODO
        //Property endpoint = AasHelper.getElementByIdShort(result.values, Constants.LINKED_SEGMENT_ENDPOINT_ID_SHORT, Property.class);
        //if (endpoint != null) {
        //    result.files = endpoint.getValue();
        //    result.values.remove(endpoint);
        //}
        //Property query = AasHelper.getElementByIdShort(result.values, Constants.LINKED_SEGMENT_QUERY_ID_SHORT, Property.class);
        //if (query != null) {
        //    result.blobs = query.getValue();
        //    result.values.remove(query);
        //}
        return result;
    }


    public ExternalSegment() {
        super();
        this.files = new ArrayList<>();
        this.blobs = new ArrayList<>();
        this.idShort = IdentifierHelper.randomId("LinkedSegment");
        this.semanticId = ReferenceHelper.globalReference(Constants.LINKED_SEGMENT_SEMANTIC_ID);
    }


    public List<File> getFiles() {
        return files;
    }


    public void setFiles(List<File> files) {
        this.files = files;
    }


    public List<Blob> getBlobs() {
        return blobs;
    }


    public void setBlobs(List<Blob> blobs) {
        this.blobs = blobs;
    }


    @Override
    public Collection<SubmodelElement> getValues() {
        Collection<SubmodelElement> result = super.getValues();
        // TODO
        //result.add(new DefaultProperty.Builder()
        //        .idShort(Constants.LINKED_SEGMENT_ENDPOINT_ID_SHORT)
        //        .valueType(Datatype.STRING.getName())
        //        .value(files)
        //        .build());
        //result.add(new DefaultProperty.Builder()
        //        .idShort(Constants.LINKED_SEGMENT_QUERY_ID_SHORT)
        //        .valueType(Datatype.STRING.getName())
        //        .value(blobs)
        //        .build());
        return result;
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends ExternalSegment, B extends AbstractBuilder<T, B>> extends Segment.AbstractBuilder<T, B> {

        public B files(List<File> value) {
            getBuildingInstance().setFiles(value);
            return getSelf();
        }


        public B file(File value) {
            getBuildingInstance().getFiles().add(value);
            return getSelf();
        }


        public B blobs(List<Blob> value) {
            getBuildingInstance().setBlobs(value);
            return getSelf();
        }


        public B blob(Blob value) {
            getBuildingInstance().getBlobs().add(value);
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
