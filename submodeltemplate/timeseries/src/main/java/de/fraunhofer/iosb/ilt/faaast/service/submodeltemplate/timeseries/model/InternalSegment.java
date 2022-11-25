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

import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.util.AasHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Represents an internal segment according to SMT TimeSeries.
 */
public class InternalSegment extends Segment {

    public InternalSegment() {
        super();
        this.idShort = IdentifierHelper.randomId("InternalSegment");
        this.semanticId = ReferenceHelper.globalReference(Constants.INTERNAL_SEGMENT_SEMANTIC_ID);
    }


    @Override
    public Collection<SubmodelElement> getValues() {
        Collection<SubmodelElement> result = super.getValues();
        result.add(new DefaultSubmodelElementCollection.Builder()
                .idShort(Constants.INTERNAL_SEGMENT_RECORDS_ID_SHORT)
                .values(records.stream().map(x -> (SubmodelElement) x).collect(Collectors.toList()))
                .build());
        return result;
    }


    /**
     * Creates a new instance based on a {@link io.adminshell.aas.v3.model.SubmodelElementCollection}.
     *
     * @param smc the {@link io.adminshell.aas.v3.model.SubmodelElementCollection} to parse
     * @return the parsed {@link io.adminshell.aas.v3.model.SubmodelElementCollection} as {@link InternalSegment}, or
     *         null if input is null
     */
    public static InternalSegment of(SubmodelElementCollection smc) {
        if (smc == null) {
            return null;
        }
        InternalSegment result = new InternalSegment();
        Segment.of(result, smc);
        SubmodelElementCollection recordsSMC = AasHelper.getElementByIdShort(result.values, Constants.INTERNAL_SEGMENT_RECORDS_ID_SHORT, SubmodelElementCollection.class);
        if (recordsSMC != null) {
            result.records.addAll((recordsSMC).getValues().stream()
                    .filter(Objects::nonNull)
                    .filter(x -> SubmodelElementCollection.class.isAssignableFrom(x.getClass()))
                    .map(x -> Record.of((SubmodelElementCollection) x))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()));
            result.values.remove(recordsSMC);
        }
        return result;
    }


    public static Builder builder() {
        return new Builder();

    }

    public abstract static class AbstractBuilder<T extends InternalSegment, B extends AbstractBuilder<T, B>> extends Segment.AbstractBuilder<T, B> {

    }

    public static class Builder extends AbstractBuilder<InternalSegment, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected InternalSegment newBuildingInstance() {
            return new InternalSegment();
        }

    }
}
