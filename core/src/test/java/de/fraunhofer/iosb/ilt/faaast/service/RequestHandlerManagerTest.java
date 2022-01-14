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
package de.fraunhofer.iosb.ilt.faaast.service;

import static org.mockito.Mockito.*;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllAssetAdministrationShellsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.GetAllAssetAdministrationShellsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import io.adminshell.aas.v3.dataformat.core.AASSimple;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Assert;
import org.junit.Test;


public class RequestHandlerManagerTest {
    private static final long DEFAULT_TIMEOUT = 1000;

    @Test
    public void testGetAllAssetAdministrationShellRequest() throws ConfigurationException {
        CoreConfig coreConfig = CoreConfig.builder().build();
        AssetAdministrationShellEnvironment environment = AASSimple.createEnvironment();
        Persistence persistence = mock(Persistence.class);
        MessageBus messageBus = mock(MessageBus.class);
        AssetConnectionManager assetConnectionManager = mock(AssetConnectionManager.class);
        RequestHandlerManager manager = new RequestHandlerManager(coreConfig, persistence, messageBus, assetConnectionManager);

        when(persistence.get(any(), argThat((AssetIdentification t) -> true), any()))
                .thenReturn(environment.getAssetAdministrationShells());
        GetAllAssetAdministrationShellsRequest request = new GetAllAssetAdministrationShellsRequest();
        GetAllAssetAdministrationShellsResponse response = manager.execute(request);
        Assert.assertEquals(environment.getAssetAdministrationShells(), response.getPayload());
    }


    @Test
    public void testGetAllAssetAdministrationShellRequestAsync() throws ConfigurationException, InterruptedException {
        CoreConfig coreConfig = CoreConfig.builder().build();
        AssetAdministrationShellEnvironment environment = AASSimple.createEnvironment();
        Persistence persistence = mock(Persistence.class);
        MessageBus messageBus = mock(MessageBus.class);
        AssetConnectionManager assetConnectionManager = mock(AssetConnectionManager.class);
        RequestHandlerManager manager = new RequestHandlerManager(coreConfig, persistence, messageBus, assetConnectionManager);

        when(persistence.get(any(), argThat((AssetIdentification t) -> true), any()))
                .thenReturn(environment.getAssetAdministrationShells());

        CountDownLatch condition = new CountDownLatch(1);
        final AtomicReference<GetAllAssetAdministrationShellsResponse> response = new AtomicReference<>();

        GetAllAssetAdministrationShellsRequest request = new GetAllAssetAdministrationShellsRequest();
        manager.executeAsync(request, x -> response.set(x));
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(environment.getAssetAdministrationShells(), response.get().getPayload());
    }
}
