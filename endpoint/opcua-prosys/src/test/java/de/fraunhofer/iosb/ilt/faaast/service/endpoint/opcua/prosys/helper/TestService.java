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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys.helper;

import static org.mockito.Mockito.mock;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.Endpoint;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternal;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A AAS Test service.
 *
 * @author Tino Bischoff
 */
public class TestService extends Service {

    private static final Logger logger = LoggerFactory.getLogger(TestService.class);

    private MessageBusInternal messageBus;
    private boolean useFullExample;

    /**
     * Constructs a new TestService
     *
     * @param endpoint
     * @param full True if the full example is requested, otherwise the simple
     *            is used
     */
    public TestService(Endpoint endpoint, boolean full) throws ConfigurationException {
        super(
                CoreConfig.builder().build(),
                full ? AASFull.ENVIRONMENT : AASSimple.ENVIRONMENT,
                mock(Persistence.class),
                new MessageBusInternal(),
                List.of(endpoint),
                List.of());
        useFullExample = full;
    }
}
