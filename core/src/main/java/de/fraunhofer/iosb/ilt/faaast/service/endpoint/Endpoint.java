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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.Configurable;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;


/**
 * An Endpoint is an implementation of the interfaces and methods described by Part 2 of the AAS specification. It is
 * also often called upper DT interface.
 * 
 * @param <T> type of the corresponding configuration class
 */
public interface Endpoint<T extends EndpointConfig> extends Configurable<T> {

    /**
     * Initialize the endpoint with a matching configuration.
     * 
     * @param core the core config
     * @param config the corresponding config
     */
    public void init(CoreConfig core, T config, ServiceContext context);


    /**
     * Starts the endpoint.
     */
    public void start() throws Exception;


    /**
     * Stops the endpoint.
     */
    public void stop();

}
