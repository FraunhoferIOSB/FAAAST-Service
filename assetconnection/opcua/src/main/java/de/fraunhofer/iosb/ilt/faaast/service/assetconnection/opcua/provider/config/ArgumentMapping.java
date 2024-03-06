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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config;

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Helper class for mapping IdShort to argument names.
 */
public class ArgumentMapping {

    private String idShort;
    private String argumentName;

    public String getIdShort() {
        return idShort;
    }


    public void setIdShort(String idShort) {
        this.idShort = idShort;
    }


    public String getArgumentName() {
        return argumentName;
    }


    public void setArgumentName(String argumentName) {
        this.argumentName = argumentName;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ArgumentMapping that = (ArgumentMapping) o;
        return Objects.equals(idShort, that.idShort)
                && Objects.equals(argumentName, that.argumentName);
    }


    @Override
    public int hashCode() {
        return Objects.hash(idShort, argumentName);
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ExtendableBuilder<ArgumentMapping, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected ArgumentMapping newBuildingInstance() {
            return new ArgumentMapping();
        }


        public Builder idShort(String value) {
            getBuildingInstance().setIdShort(value);
            return getSelf();
        }


        public Builder argumentName(String value) {
            getBuildingInstance().setArgumentName(value);
            return getSelf();
        }

    }
}
