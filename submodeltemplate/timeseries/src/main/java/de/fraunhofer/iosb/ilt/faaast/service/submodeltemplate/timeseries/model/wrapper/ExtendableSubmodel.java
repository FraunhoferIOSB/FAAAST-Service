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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.fraunhofer.iosb.ilt.faaast.service.util.AasHelper;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.builder.SubmodelBuilder;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * This class allows to create custom subclasses of {@link io.adminshell.aas.v3.model.impl.DefaultSubmodel} containing
 * additional properties which are mapped to AAS elements inside this submodel.
 */
public class ExtendableSubmodel extends DefaultSubmodel {

    @JsonIgnore
    protected List<Wrapper> additionalValues;

    public ExtendableSubmodel(Wrapper... additionalValues) {
        this.additionalValues = new ArrayList<>(Arrays.asList(additionalValues));
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
            ExtendableSubmodel other = (ExtendableSubmodel) obj;
            return super.equals(other)
                    && Objects.equals(this.additionalValues, other.additionalValues);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), additionalValues);
    }


    /**
     * Generic method to parse properties mapped to AAS.
     *
     * @param <T> the concrete type
     * @param result the result instance to modify; this element will be modified!
     * @param submodel the submodel to parse
     * @return the modified {@code result} with parsed values from {@code submodel}
     */
    protected static <T extends ExtendableSubmodel> T genericOf(T result, Submodel submodel) {
        if (Objects.isNull(submodel) || Objects.isNull(submodel.getSubmodelElements())) {
            return result;
        }
        AasHelper.applyBasicProperties(submodel, result);
        for (var sme: submodel.getSubmodelElements()) {
            List<Wrapper> matchingAdditionalValues = result.additionalValues.stream()
                    .filter(x -> x.getAASType().isAssignableFrom(sme.getClass()))
                    .filter(x -> x.canParse(sme))
                    .collect(Collectors.toList());
            if (matchingAdditionalValues.size() > 1) {
                throw new IllegalArgumentException("ambiguous mapping definition");
            }
            if (matchingAdditionalValues.size() == 1) {
                matchingAdditionalValues.get(0).parse(sme);
            }
            else {
                result.submodelElements.add(sme);
            }
        }
        return result;
    }


    public List<Wrapper> getAdditionalValues() {
        return additionalValues;
    }


    public void setAdditionalValues(List<Wrapper> additionalValues) {
        this.additionalValues = additionalValues;
    }


    /**
     * Registers additional {@link Wrapper} values to be handled with this class.
     *
     * @param additionalValues additional wrapper values
     */
    public void withAdditionalValues(Wrapper... additionalValues) {
        this.additionalValues.addAll(Arrays.asList(additionalValues));
    }

    public abstract static class AbstractBuilder<T extends ExtendableSubmodel, B extends AbstractBuilder<T, B>> extends SubmodelBuilder<T, B> {

        public B additionalValue(Wrapper value) {
            getBuildingInstance().getAdditionalValues().add(value);
            return getSelf();
        }


        public B additionalValues(Wrapper... value) {
            getBuildingInstance().getAdditionalValues().addAll(Arrays.asList(value));
            return getSelf();
        }


        public B additionalValues(Collection<Wrapper> value) {
            getBuildingInstance().getAdditionalValues().addAll(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<ExtendableSubmodel, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ExtendableSubmodel newBuildingInstance() {
            return new ExtendableSubmodel();
        }

    }

}
