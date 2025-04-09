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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.security;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.memory.FileStorageInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.net.MalformedURLException;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.junit.Before;
import org.junit.Test;


public class ProcessorTest {

    private ServiceConfig config;

    @Before
    public void init() {

        Reference submodelRef = ReferenceBuilder.forSubmodel("https://example.com/ids/sm/AssetInterfacesDescription", "InterfaceHTTP");
        config = new ServiceConfig.Builder()
                .core(new CoreConfig.Builder().requestHandlerThreadPoolSize(2).build())
                .persistence(new PersistenceInMemoryConfig())
                .fileStorage(new FileStorageInMemoryConfig())
                .messageBus(new MessageBusInternalConfig())
                .submodelTemplateProcessors(List.of(new SecuritySubmodelTemplateProcessorConfig.Builder()
                        .interfaceConfiguration(submodelRef, new SecuritySubmodelTemplateProcessorConfigData.Builder().username("user1").password("pw1").build()).build()))
                .build();
    }


    @Test
    public void testSecurity() throws ConfigurationException, AssetConnectionException, PersistenceException, MessageBusException, MalformedURLException {
        /*
         * File initialModelFile = new File("src/test/resources/Test-Example.json");
         * config.getPersistence().setInitialModelFile(initialModelFile);
         * Service service = new Service(config);
         * Assert.assertNotNull(service);
         * AssetConnectionManager manager = service.getAssetConnectionManager();
         * Assert.assertNotNull(manager);
         * List<AssetConnection> assetConns = manager.getConnections();
         * Assert.assertEquals(2, assetConns.size());
         */
    }
}
