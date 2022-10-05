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

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;


/**
 * Superclass of all classes that can be created by a correspondig configuration class.
 *
 * @param <T> type of the corresponding configuration class
 */
public interface Configurable<T extends Config> {

    /**
     * Initializes this object with a generic coreConfig and an instance of the corresponding configuration class.
     *
     * @param coreConfig coreConfig
     * @param config an instance of the corresponding configuration class
     * @param serviceContext service context this element is running under
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException if initialization
     *             fails
     */
    public void init(CoreConfig coreConfig, T config, ServiceContext serviceContext) throws ConfigurationInitializationException;


    /**
     * Returns an instance of the corresponding configuration class representing the state this object.
     *
     * @return an instance of the corresponding configuration class representing the state this object.
     */
    public T asConfig();
}
