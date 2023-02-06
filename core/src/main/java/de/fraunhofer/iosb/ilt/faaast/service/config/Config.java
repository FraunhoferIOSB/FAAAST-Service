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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.reflect.TypeToken;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInstantiationException;


/**
 * Superclass of all config classes that are coupled with a concrete implementation class (via generics).
 *
 * @param <T> type of the implementation class configured by this configuration
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, visible = true)
public abstract class Config<T extends Configurable> {

    /**
     * Utility method to get the concrete type of the corresponding implementation.
     *
     * @return the type of the corresponding implementation
     */
    protected Class<T> getImplementationType() {
        return (Class<T>) TypeToken.of(getClass()).resolveType(Config.class.getTypeParameters()[0]).getRawType();
    }


    /**
     * Creates a new instance of the implementation class that is initialized with this configuration.
     *
     * @param coreConfig the coreConfig to initialize the implementation class with
     * @param context context information about the service
     * @return a new instance of the implementation class that is initialized with this configuration
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInstantiationException when creating a new
     *             instance fails
     */
    public T newInstance(CoreConfig coreConfig, ServiceContext context) throws ConfigurationException {
        try {
            T result = getImplementationType().newInstance();
            result.init(coreConfig, this, context);
            return result;
        }
        catch (IllegalAccessException | InstantiationException e) {
            throw new ConfigurationInstantiationException("error instantiating configuration implementation class", e);
        }
    }
}
