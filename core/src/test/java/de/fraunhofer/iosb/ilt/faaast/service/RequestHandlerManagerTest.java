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
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.*;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllAssetAdministrationShellsByAssetIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllAssetAdministrationShellsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.InvokeOperationAsyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.GetAllAssetAdministrationShellsByAssetIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.GetAllAssetAdministrationShellsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.InvokeOperationAsyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.InvokeOperationSyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import io.adminshell.aas.v3.dataformat.core.AASSimple;
import io.adminshell.aas.v3.model.*;
import io.adminshell.aas.v3.model.impl.DefaultOperation;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class RequestHandlerManagerTest {

    private static final long DEFAULT_TIMEOUT = 1000;

    private static CoreConfig coreConfig;
    private static AssetAdministrationShellEnvironment environment;
    private static MessageBus messageBus;
    private static Persistence persistence;
    private static AssetConnectionManager assetConnectionManager;
    private static RequestHandlerManager manager;

    @Before
    public void createRequestHandlerManager() {
        environment = AASSimple.createEnvironment();
        coreConfig = CoreConfig.builder().build();
        messageBus = mock(MessageBus.class);
        persistence = mock(Persistence.class);
        assetConnectionManager = mock(AssetConnectionManager.class);
        manager = new RequestHandlerManager(coreConfig, persistence, messageBus, assetConnectionManager);
    }


    @Test
    public void testGetAllAssetAdministrationShellRequest() {
        when(persistence.get(any(), argThat((AssetIdentification t) -> true), any()))
                .thenReturn(environment.getAssetAdministrationShells());
        GetAllAssetAdministrationShellsRequest request = new GetAllAssetAdministrationShellsRequest();
        GetAllAssetAdministrationShellsResponse response = manager.execute(request);
        Assert.assertEquals(environment.getAssetAdministrationShells(), response.getPayload());
    }


    @Test
    public void testGetAllAssetAdministrationShellsByAssetIdRequest() {
        when(persistence.get(any(), argThat((AssetIdentification t) -> true), any()))
                .thenReturn(environment.getAssetAdministrationShells());
        GetAllAssetAdministrationShellsByAssetIdRequest request = new GetAllAssetAdministrationShellsByAssetIdRequest();
        GetAllAssetAdministrationShellsByAssetIdResponse response = manager.execute(request);
        GetAllAssetAdministrationShellsByAssetIdResponse expected = new GetAllAssetAdministrationShellsByAssetIdResponse.Builder()
                .payload(environment.getAssetAdministrationShells())
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testGetAllAssetAdministrationShellRequestAsync() throws InterruptedException {
        when(persistence.get(any(), argThat((AssetIdentification t) -> true), any()))
                .thenReturn(environment.getAssetAdministrationShells());
        GetAllAssetAdministrationShellsRequest request = new GetAllAssetAdministrationShellsRequest();
        final AtomicReference<GetAllAssetAdministrationShellsResponse> response = new AtomicReference<>();
        CountDownLatch condition = new CountDownLatch(1);
        manager.executeAsync(request, x -> response.set(x));
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(environment.getAssetAdministrationShells(), response.get().getPayload());
    }


    private Operation getTestOperation() {
        return new DefaultOperation.Builder()
                .category("Test")
                .idShort("TestOperation")
                .inoutputVariable(new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort("TestProp")
                                .value("TestValue")
                                .build())
                        .build())
                .outputVariable(new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort("TestPropOutput")
                                .value("TestValue")
                                .build())
                        .build())
                .inputVariable(new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort("TestPropInput")
                                .value("TestValue")
                                .build())
                        .build())
                .build();
    }


    @Test
    public void testInvokeOperationAsyncRequest() {
        CoreConfig coreConfig = CoreConfig.builder().build();
        Persistence persistence = mock(Persistence.class);
        MessageBus messageBus = mock(MessageBus.class);
        AssetConnectionManager assetConnectionManager = mock(AssetConnectionManager.class);
        AssetOperationProvider assetOperationProvider = mock(AssetOperationProvider.class);
        RequestHandlerManager manager = new RequestHandlerManager(coreConfig, persistence, messageBus, assetConnectionManager);

        Operation operation = getTestOperation();

        OperationHandle expectedOperationHandle = new OperationHandle.Builder().handleId("1").requestId("1").build();
        when(persistence.putOperationContext(any(), any(), any())).thenReturn(expectedOperationHandle);
        when(persistence.getOperationResult(any())).thenReturn(new OperationResult.Builder().requestId("1").build());
        when(assetConnectionManager.hasOperationProvider(any())).thenReturn(true);
        when(assetConnectionManager.getOperationProvider(any())).thenReturn(assetOperationProvider);

        InvokeOperationAsyncRequest invokeOperationAsyncRequest = new InvokeOperationAsyncRequest.Builder()
                .requestId("1")
                .inoutputArguments(operation.getInoutputVariables())
                .inputArguments(operation.getInputVariables())
                .build();

        InvokeOperationAsyncResponse response = manager.execute(invokeOperationAsyncRequest);
        OperationHandle actualOperationHandle = response.getPayload();
        Assert.assertEquals(expectedOperationHandle, actualOperationHandle);
    }


    @Test
    public void testInvokeOperationSyncRequest() {
        CoreConfig coreConfig = CoreConfig.builder().build();
        Persistence persistence = mock(Persistence.class);
        MessageBus messageBus = mock(MessageBus.class);
        AssetConnectionManager assetConnectionManager = mock(AssetConnectionManager.class);
        when(assetConnectionManager.hasOperationProvider(any())).thenReturn(true);
        when(assetConnectionManager.getOperationProvider(any())).thenReturn(new CustomAssetOperationProvider());

        RequestHandlerManager manager = new RequestHandlerManager(coreConfig, persistence, messageBus, assetConnectionManager);
        Operation operation = getTestOperation();

        InvokeOperationSyncRequest invokeOperationSyncRequest = new InvokeOperationSyncRequest.Builder()
                .requestId("1")
                .inoutputArguments(operation.getInoutputVariables())
                .inputArguments(operation.getInputVariables())
                .build();

        InvokeOperationSyncResponse actualResponse = manager.execute(invokeOperationSyncRequest);
        InvokeOperationSyncResponse expectedResponse = new InvokeOperationSyncResponse.Builder()
                .statusCode(StatusCode.Success)
                .payload(new OperationResult.Builder()
                        .requestId("1")
                        .inoutputArguments(List.of(new DefaultOperationVariable.Builder()
                                .value(new DefaultProperty.Builder()
                                        .idShort("TestProp")
                                        .value("TestOutput")
                                        .build())
                                .build()))
                        .outputArguments(operation.getInputVariables())
                        .executionState(ExecutionState.Completed)
                        .build())
                .build();

        Assert.assertEquals(expectedResponse, actualResponse);
    }

    class CustomAssetOperationProvider implements AssetOperationProvider {

        @Override
        public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
            Property property = (Property) inoutput[0].getValue();
            property.setValue("TestOutput");
            return input;
        }


        @Override
        public void invokeAsync(OperationVariable[] input, OperationVariable[] inoutput, BiConsumer<OperationVariable[], OperationVariable[]> callback)
                throws AssetConnectionException {

        }
    }
}
