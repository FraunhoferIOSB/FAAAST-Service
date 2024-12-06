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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.LambdaOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.LambdaSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider.LambdaValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EndpointException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PutSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.InvokeOperationSyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class LambdaAssetConnectionTest {

    private Service service;
    private Persistence persistence;

    @Before
    public void init() throws ConfigurationInitializationException, ConfigurationException, AssetConnectionException, MessageBusException, EndpointException {
        persistence = mock(Persistence.class);
        FileStorage fileStorage = mock(FileStorage.class);
        MessageBus messageBus = mock(MessageBus.class);
        service = new Service(CoreConfig.DEFAULT, persistence, fileStorage, messageBus, List.of(), List.of());
    }


    @Test
    public void testValueProvider() throws MessageBusException, EndpointException, ResourceNotFoundException, PersistenceException {
        final String submodelId = "submodel";
        final String propertyId = "property";
        final int initialValueAAS = 0;
        final int initialValueAsset = 1;
        final int newValue = 2;

        final Property property = new DefaultProperty.Builder()
                .idShort(propertyId)
                .value(Integer.toString(initialValueAAS))
                .valueType(DataTypeDefXsd.INT)
                .build();

        final Reference propertyRef = new ReferenceBuilder()
                .submodel(submodelId)
                .element(propertyId)
                .build();

        when(persistence.getSubmodelElement(eq(propertyRef), any())).thenReturn(property);

        AtomicInteger value = new AtomicInteger(initialValueAsset);
        service.getAssetConnectionManager().registerLambdaValueProvider(
                propertyRef,
                LambdaValueProvider.builder()
                        .read(() -> {
                            try {
                                return PropertyValue.of(Datatype.INT, Integer.toString(value.get()));
                            }
                            catch (ValueFormatException e) {
                                Assert.fail();
                                throw new RuntimeException();
                            }
                        })
                        .write(x -> {
                            if (x instanceof PropertyValue propertyValue) {
                                value.set(Integer.parseInt(propertyValue.getValue().asString()));
                            }
                            else {
                                Assert.fail();
                            }
                        })
                        .build());
        service.start();

        // read
        Property actual = readProperty(propertyRef);
        assertEquals(initialValueAsset, Integer.parseInt(actual.getValue()));

        // write
        service.execute(PutSubmodelElementByPathRequest.builder()
                .submodelId(submodelId)
                .path(propertyId)
                .submodelElement(new DefaultProperty.Builder()
                        .idShort(propertyId)
                        .value(Integer.toString(newValue))
                        .valueType(DataTypeDefXsd.INT)
                        .build())
                .build());

        actual = readProperty(propertyRef);
        assertEquals(newValue, Integer.parseInt(actual.getValue()));
    }


    @Test
    public void testSubscriptionProvider()
            throws ConfigurationInitializationException, ConfigurationException, AssetConnectionException, MessageBusException, EndpointException, ResourceNotFoundException,
            InterruptedException, PersistenceException {
        final String submodelId = "submodel";
        final String propertyId = "property";
        final int initialValueAAS = 0;

        final Property property = new DefaultProperty.Builder()
                .idShort(propertyId)
                .value(Integer.toString(initialValueAAS))
                .valueType(DataTypeDefXsd.INT)
                .build();

        final Reference propertyRef = new ReferenceBuilder()
                .submodel(submodelId)
                .element(propertyId)
                .build();

        when(persistence.getSubmodelElement(eq(propertyRef), any())).thenReturn(property);

        List<Integer> values = List.of(1, 2, 3, 4);
        // need two locks: canUpdate, updated
        Semaphore canUpdate = new Semaphore(1);
        Semaphore updated = new Semaphore(1);
        canUpdate.acquire();
        updated.acquire();
        service.getAssetConnectionManager().registerLambdaSubscriptionProvider(
                propertyRef,
                LambdaSubscriptionProvider.builder()
                        .generate(x -> {
                            Thread thread = new Thread() {
                                public void run() {
                                    int i = 0;
                                    do {
                                        int value = values.get(i);
                                        try {
                                            canUpdate.acquire();
                                            if (Objects.nonNull(x)) {
                                                x.newDataReceived(PropertyValue.of(Datatype.INT, Integer.toString(value)));
                                            }
                                        }
                                        catch (InterruptedException | ValueFormatException e) {
                                            Assert.fail();
                                        }
                                        finally {
                                            i++;
                                            updated.release();
                                        }
                                    } while (i < values.size());
                                }
                            };

                            {
                                thread.start();
                            }
                        })
                        .build());
        service.start();

        assertEquals(initialValueAAS, Integer.parseInt(readProperty(propertyRef).getValue()));
        canUpdate.release();
        for (int value: values) {
            updated.acquire();
            assertEquals(value, Integer.parseInt(readProperty(propertyRef).getValue()));
            canUpdate.release();
        }
    }


    @Test
    public void testOperationProvider()
            throws MessageBusException, EndpointException, ResourceNotFoundException, PersistenceException {
        final String submodelId = "submodel";
        final String operationId = "property";
        final String input1Id = "in1";
        final String input2Id = "in2";
        final String outputId = "result";

        final Operation operation = new DefaultOperation.Builder()
                .idShort(operationId)
                .inputVariables(new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort(input1Id)
                                .valueType(DataTypeDefXsd.INT)
                                .build())
                        .build())
                .inputVariables(new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort(input2Id)
                                .valueType(DataTypeDefXsd.INT)
                                .build())
                        .build())
                .outputVariables(new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort(outputId)
                                .valueType(DataTypeDefXsd.INT)
                                .build())
                        .build())
                .build();

        final Reference reference = new ReferenceBuilder()
                .submodel(submodelId)
                .element(operationId)
                .build();

        when(persistence.getSubmodelElement(eq(reference), any(), eq(Operation.class))).thenReturn(operation);
        service.getAssetConnectionManager().registerLambdaOperationProvider(
                reference,
                LambdaOperationProvider.builder()
                        .handle((input, inoutput) -> {
                            int in1 = Integer.parseInt(((Property) input[0].getValue()).getValue());
                            int in2 = Integer.parseInt(((Property) input[1].getValue()).getValue());
                            return new OperationVariable[] {
                                    new DefaultOperationVariable.Builder()
                                            .value(new DefaultProperty.Builder()
                                                    .idShort(outputId)
                                                    .value(Integer.toString(in1 + in2))
                                                    .valueType(DataTypeDefXsd.INT)
                                                    .build())
                                            .build()
                            };
                        })
                        .build());
        service.start();

        Response response = service.execute(InvokeOperationSyncRequest.builder()
                .submodelId(ReferenceHelper.findFirstKeyType(reference, KeyTypes.SUBMODEL))
                .path(ReferenceHelper.findFirstKeyType(reference, KeyTypes.SUBMODEL_ELEMENT))
                .inputArgument(new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort(input1Id)
                                .value("1")
                                .valueType(DataTypeDefXsd.INT)
                                .build())
                        .build())
                .inputArgument(new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort(input2Id)
                                .value("2")
                                .valueType(DataTypeDefXsd.INT)
                                .build())
                        .build())
                .build());
        if (!response.getStatusCode().isSuccess()
                || !InvokeOperationSyncResponse.class.isAssignableFrom(response.getClass())) {
            throw new RuntimeException();
        }
        OperationResult result = ((InvokeOperationSyncResponse) response).getPayload();
        assertTrue(result.getSuccess());
        assertEquals(1, result.getOutputArguments().size());
        assertTrue(Property.class.isAssignableFrom(result.getOutputArguments().get(0).getValue().getClass()));
        Property resultProperty = (Property) result.getOutputArguments().get(0).getValue();
        assertEquals(3, Integer.parseInt(resultProperty.getValue()));
    }


    private Property readProperty(Reference reference) {
        Response response = service.execute(GetSubmodelElementByPathRequest.builder()
                .submodelId(ReferenceHelper.findFirstKeyType(reference, KeyTypes.SUBMODEL))
                .path(ReferenceHelper.findFirstKeyType(reference, KeyTypes.SUBMODEL_ELEMENT))
                .build());
        if (!response.getStatusCode().isSuccess()
                || !GetSubmodelElementByPathResponse.class.isAssignableFrom(response.getClass())
                || !Property.class.isAssignableFrom(((GetSubmodelElementByPathResponse) response).getPayload().getClass())) {
            throw new RuntimeException();
        }
        return (Property) ((GetSubmodelElementByPathResponse) response).getPayload();
    }

}
