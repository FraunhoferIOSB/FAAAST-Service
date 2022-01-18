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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllAssetAdministrationShellsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.InvokeOperationAsyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.GetAllAssetAdministrationShellsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.InvokeOperationAsyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.InvokeOperationSyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import io.adminshell.aas.v3.dataformat.core.AASFull;
import io.adminshell.aas.v3.dataformat.core.AASSimple;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.OperationVariable;
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


    @Test
    public void testInvokeOperationAsyncRequest() throws ConfigurationException, InterruptedException {
        CoreConfig coreConfig = CoreConfig.builder().build();
        AssetAdministrationShellEnvironment environment = AASFull.createEnvironment();
        Persistence persistence = mock(Persistence.class);
        MessageBus messageBus = mock(MessageBus.class);
        AssetConnectionManager assetConnectionManager = mock(AssetConnectionManager.class);
        AssetOperationProvider assetOperationProvider = mock(AssetOperationProvider.class);
        RequestHandlerManager manager = new RequestHandlerManager(coreConfig, persistence, messageBus, assetConnectionManager);

        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel";
        String SUBMODEL_ELEMENT_IDSHORT = "ExampleOperation";
        Operation operation = (Operation) environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_IDSHORT))
                .findFirst().get();

        OperationHandle operationHandle = new OperationHandle.Builder().handleId("1").requestId("1").build();
        when(persistence.putOperationContext(any(), any(), any())).thenReturn(operationHandle);
        when(persistence.getOperationResult(any())).thenReturn(new OperationResult.Builder().requestId("1").build());
        when(assetConnectionManager.hasOperationProvider(any())).thenReturn(true);
        when(assetConnectionManager.getOperationProvider(any())).thenReturn(assetOperationProvider);

        InvokeOperationAsyncRequest invokeOperationAsyncRequest = new InvokeOperationAsyncRequest();
        invokeOperationAsyncRequest.setRequestId("1");
        invokeOperationAsyncRequest.setInoutputArguments(operation.getInoutputVariables());
        invokeOperationAsyncRequest.setInputArguments(operation.getInputVariables());

        InvokeOperationAsyncResponse response = manager.execute(invokeOperationAsyncRequest);
        Assert.assertEquals(operationHandle, response.getPayload());

    }


    @Test
    public void testInvokeOperationSyncRequest() throws ConfigurationException, InterruptedException, AssetConnectionException {
        CoreConfig coreConfig = CoreConfig.builder().build();
        AssetAdministrationShellEnvironment environment = AASFull.createEnvironment();
        Persistence persistence = mock(Persistence.class);
        MessageBus messageBus = mock(MessageBus.class);
        AssetConnectionManager assetConnectionManager = mock(AssetConnectionManager.class);
        AssetOperationProvider assetOperationProvider = mock(AssetOperationProvider.class);
        RequestHandlerManager manager = new RequestHandlerManager(coreConfig, persistence, messageBus, assetConnectionManager);

        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel";
        String SUBMODEL_ELEMENT_IDSHORT = "ExampleOperation";
        Operation operation = (Operation) environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_IDSHORT))
                .findFirst().get();

        OperationHandle operationHandle = new OperationHandle.Builder().handleId("1").requestId("1").build();
        when(persistence.putOperationContext(any(), any(), any())).thenReturn(operationHandle);
        when(persistence.getOperationResult(any())).thenReturn(new OperationResult.Builder().requestId("1").build());
        when(assetConnectionManager.hasOperationProvider(any())).thenReturn(true);
        when(assetConnectionManager.getOperationProvider(any())).thenReturn(assetOperationProvider);
        when(assetOperationProvider.invoke(any(), any())).thenReturn(operation.getOutputVariables().toArray(new OperationVariable[0]));

        InvokeOperationSyncRequest invokeOperationSyncRequest = new InvokeOperationSyncRequest();
        invokeOperationSyncRequest.setRequestId("1");
        invokeOperationSyncRequest.setInoutputArguments(operation.getInoutputVariables());
        invokeOperationSyncRequest.setInputArguments(operation.getInputVariables());

        InvokeOperationSyncResponse response = manager.execute(invokeOperationSyncRequest);
        Assert.assertEquals(StatusCode.Success, response.getStatusCode());
        Assert.assertNotNull(response.getPayload());
        Assert.assertEquals(operation.getOutputVariables().get(0).getValue(), ((OperationResult) response.getPayload()).getOutputArguments().get(0).getValue());

    }
}
