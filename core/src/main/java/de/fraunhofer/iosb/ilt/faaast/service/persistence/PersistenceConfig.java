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
package de.fraunhofer.iosb.ilt.faaast.service.persistence;

import de.fraunhofer.iosb.ilt.faaast.service.config.Config;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.io.File;
import java.util.Objects;


/**
 * Generic persistence configuration. When implementing a custom persistence
 * inherit from this class to create a custom configuration.
 *
 * @param <T> type of the persistence
 */
public class PersistenceConfig<T extends Persistence> extends Config<T> {

    private static final boolean DEFAULT_DECOUPLE_ENVIRONMENT = true;
    private File initialModel;
    private AssetAdministrationShellEnvironment environment;
    private boolean decoupleEnvironment;

    public PersistenceConfig(File initialModel) {
        this.initialModel = initialModel;
        decoupleEnvironment = DEFAULT_DECOUPLE_ENVIRONMENT;
    }


    public PersistenceConfig() {
        decoupleEnvironment = DEFAULT_DECOUPLE_ENVIRONMENT;
    }


    public File getInitialModel() {
        return initialModel;
    }


    /**
     * Sets model file containing initial model. Initial model is the model that
     * is loaded on first start.
     *
     * @param initialModel the model file
     */
    public void setInitialModel(File initialModel) {
        this.initialModel = initialModel;
    }


    public AssetAdministrationShellEnvironment getEnvironment() {
        return environment;
    }


    /**
     * Overwrites the AASEnvironment from model path
     *
     * @param environment the environment to set
     */
    public void setEnvironment(AssetAdministrationShellEnvironment environment) {
        this.environment = environment;
    }


    public boolean isDecoupleEnvironment() {
        return decoupleEnvironment;
    }


    /**
     * If true then a copied version of the environment is used
     *
     * @param decoupleEnvironment flag indicating whether to decouple the environment
     */
    public void setDecoupleEnvironment(boolean decoupleEnvironment) {
        this.decoupleEnvironment = decoupleEnvironment;
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
        final PersistenceConfig<?> other = (PersistenceConfig<?>) obj;
        return Objects.equals(this.initialModel, other.initialModel)
                && Objects.equals(this.decoupleEnvironment, other.decoupleEnvironment)
                && Objects.equals(this.environment, other.environment);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.initialModel, this.decoupleEnvironment, this.environment);
    }

    /**
     * Abstract builder class that should be used for builders of inheriting
     * classes.
     *
     * @param <T> type of the persistence of the config to build
     * @param <C> type of the config to build
     * @param <B> type of this builder, needed for inheritance builder pattern
     */
    public abstract static class AbstractBuilder<T extends Persistence, C extends PersistenceConfig<T>, B extends AbstractBuilder<T, C, B>> extends ExtendableBuilder<C, B> {

        public B initialModel(File value) {
            getBuildingInstance().setInitialModel(value);
            return getSelf();
        }


        public B environment(AssetAdministrationShellEnvironment value) {
            getBuildingInstance().setEnvironment(value);
            return getSelf();
        }


        public B decoupleEnvironment(boolean value) {
            getBuildingInstance().setDecoupleEnvironment(value);
            return getSelf();
        }

    }

    /**
     * Builder for PersistenceConfig class.
     *
     * @param <T> type of the persistence of the config to build
     */
    public static class Builder<T extends Persistence> extends AbstractBuilder<T, PersistenceConfig<T>, Builder<T>> {

        @Override
        protected Builder<T> getSelf() {
            return this;
        }


        @Override
        protected PersistenceConfig<T> newBuildingInstance() {
            return new PersistenceConfig<>();
        }

    }
}
