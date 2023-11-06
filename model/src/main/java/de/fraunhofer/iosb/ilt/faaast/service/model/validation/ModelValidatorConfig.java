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
package de.fraunhofer.iosb.ilt.faaast.service.model.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.AbstractBuilder;


/**
 * Configuration for {@link ModelValidator}.
 */
public class ModelValidatorConfig {

    public static final ModelValidatorConfig DEFAULT = ModelValidatorConfig.builder().build();
    public static final ModelValidatorConfig ALL = ModelValidatorConfig.builder()
            .validateConstraints(true)
            .validateIdShortUniqueness(true)
            .validateIdentifierUniqueness(true)
            .build();
    public static final ModelValidatorConfig NONE = ModelValidatorConfig.builder()
            .validateConstraints(false)
            .validateIdShortUniqueness(false)
            .validateIdentifierUniqueness(false)
            .build();

    // TODO currently deactived because not present in AAS4j
    private static final boolean DEFAULT_VALIDATE_CONSTRAINTS = false;
    private static final boolean DEFAULT_VALIDATE_ID_SHORT_UNIQUENESS = true;
    private static final boolean DEFAULT_VALIDATE_IDENTIFIER_UNIQUENESS = true;

    private boolean validateConstraints;
    private boolean validateIdShortUniqueness;
    private boolean validateIdentifierUniqueness;

    public ModelValidatorConfig() {
        validateConstraints = DEFAULT_VALIDATE_CONSTRAINTS;
        validateIdShortUniqueness = DEFAULT_VALIDATE_ID_SHORT_UNIQUENESS;
        validateIdentifierUniqueness = DEFAULT_VALIDATE_IDENTIFIER_UNIQUENESS;
    }


    public boolean getValidateConstraints() {
        return validateConstraints;
    }


    @JsonIgnore
    public boolean isEnabled() {
        return validateConstraints || validateIdShortUniqueness || validateIdentifierUniqueness;
    }


    public void setValidateConstraints(boolean validateConstraints) {
        this.validateConstraints = validateConstraints;
    }


    public boolean getIdShortUniqueness() {
        return validateIdShortUniqueness;
    }


    public void setValidateIdShortUniqueness(boolean validateIdShortUniqueness) {
        this.validateIdShortUniqueness = validateIdShortUniqueness;
    }


    public boolean getIdentifierUniqueness() {
        return validateIdentifierUniqueness;
    }


    public void setValidateIdentifierUniqueness(boolean validateIdentifierUniqueness) {
        this.validateIdentifierUniqueness = validateIdentifierUniqueness;
    }


    @Override
    public int hashCode() {
        return Objects.hash(validateConstraints,
                validateIdShortUniqueness,
                validateIdentifierUniqueness);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ModelValidatorConfig other = (ModelValidatorConfig) obj;
        return Objects.equals(this.validateConstraints, other.validateConstraints)
                && Objects.equals(this.validateIdShortUniqueness, other.validateIdShortUniqueness)
                && Objects.equals(this.validateIdentifierUniqueness, other.validateIdentifierUniqueness);
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<ModelValidatorConfig> {

        public Builder validateConstraints(boolean value) {
            getBuildingInstance().setValidateConstraints(value);
            return this;
        }


        public Builder validateIdShortUniqueness(boolean value) {
            getBuildingInstance().setValidateIdShortUniqueness(value);
            return this;
        }


        public Builder validateIdentifierUniqueness(boolean value) {
            getBuildingInstance().setValidateIdentifierUniqueness(value);
            return this;
        }


        @Override
        protected ModelValidatorConfig newBuildingInstance() {
            return new ModelValidatorConfig();
        }
    }
}
