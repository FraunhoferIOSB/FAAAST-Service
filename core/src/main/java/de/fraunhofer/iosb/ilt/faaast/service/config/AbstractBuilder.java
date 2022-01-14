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
package de.fraunhofer.iosb.ilt.faaast.service.config;

/**
 * Utility class for realizing builder pattern with inheritance.
 * The builder works by creating an internal instance of the targte class upon creation and modifying that instance
 * witht the builder. Upon a call to build() the instance is returned.
 * After calling build() once the builder should not be used any further as additional calls/modifications will also
 * modify the previously return object which developers may not expect.
 * 
 * @param <T> type to build with the builder
 */
public abstract class AbstractBuilder<T> {

    private final T buildingInstance;

    protected AbstractBuilder() {
        buildingInstance = newBuildingInstance();
    }


    /**
     * Returns the instance of the object to create.
     * 
     * @return the instance of the object to create.
     */
    public T build() {
        return buildingInstance;
    }


    /**
     * Returns the current internal instance used by the builder.
     * 
     * @return the current internal instance used by the builder.
     */
    protected T getBuildingInstance() {
        return buildingInstance;
    }


    /**
     * Creates a new/empty instance of the object to build.
     * 
     * @return a new/empty instance of the object to build.
     */
    protected abstract T newBuildingInstance();
}
