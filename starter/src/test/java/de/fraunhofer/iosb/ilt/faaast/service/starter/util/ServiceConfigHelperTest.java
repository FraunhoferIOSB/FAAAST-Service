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
package de.fraunhofer.iosb.ilt.faaast.service.starter.util;

import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.EndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;


public class ServiceConfigHelperTest {

    private static ServiceConfig getConfigWithHttpEndpoint() {
        return new ServiceConfig.Builder()
                .core(new CoreConfig.Builder().requestHandlerThreadPoolSize(2).build())
                .endpoints(List.of(new HttpEndpointConfig.Builder()
                        .port(8080)
                        .build()))
                .persistence(new PersistenceInMemoryConfig())
                .messageBus(new MessageBusInternalConfig())
                .build();
    }


    @Test
    public void testAutoComplete() throws IOException, Exception {
        ServiceConfig expected = getConfigWithHttpEndpoint();
        ServiceConfig actual = ServiceConfigHelper.load(getClass().getResourceAsStream("/config-partial.json"));
        ServiceConfigHelper.autoComplete(actual);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testWithProperties() throws IOException, Exception {
        ServiceConfig input = new ServiceConfig.Builder()
                .core(new CoreConfig.Builder()
                        .requestHandlerThreadPoolSize(3)
                        .build())
                .endpoints(List.of(new EndpointConfig() {
                    public int getPort() {
                        return 1337;
                    }
                }))
                .persistence(new PersistenceInMemoryConfig())
                .messageBus(new MessageBusInternalConfig())
                .build();
        ServiceConfig expected = getConfigWithHttpEndpoint();
        ServiceConfig actual = ServiceConfigHelper.withProperties(input,
                Map.of(ParameterConstants.REQUEST_HANDLER_THREAD_POOL_SIZE, expected.getCore().getRequestHandlerThreadPoolSize(),
                        ParameterConstants.ENDPOINT_0_CLASS, HttpEndpoint.class.getCanonicalName(),
                        ParameterConstants.ENDPOINT_0_PORT, 8080));
        Assert.assertEquals(expected, actual);
    }

}
