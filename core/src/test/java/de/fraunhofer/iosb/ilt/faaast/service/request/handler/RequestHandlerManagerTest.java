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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.ExecutionState;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.DeleteAssetAdministrationShellByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.DeleteConceptDescriptionByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.DeleteSubmodelByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.DeleteSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.DeleteSubmodelReferenceResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAssetAdministrationShellsByAssetIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAssetAdministrationShellsByIdShortResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAssetAdministrationShellsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllConceptDescriptionsByDataSpecificationReferenceResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllConceptDescriptionsByIdShortResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllConceptDescriptionsByIsCaseOfResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllConceptDescriptionsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllSubmodelElementsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllSubmodelReferencesResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllSubmodelsByIdShortResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllSubmodelsBySemanticIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllSubmodelsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAssetAdministrationShellByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAssetAdministrationShellResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAssetInformationResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetConceptDescriptionByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetSubmodelByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.InvokeOperationAsyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.InvokeOperationSyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostAssetAdministrationShellResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostConceptDescriptionResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostSubmodelElementResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostSubmodelReferenceResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutAssetAdministrationShellByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutAssetAdministrationShellResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutAssetInformationResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutConceptDescriptionByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutSubmodelByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.SetSubmodelElementValueByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteSubmodelReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAssetAdministrationShellsByAssetIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAssetAdministrationShellsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAssetAdministrationShellsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllConceptDescriptionsByDataSpecificationReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllConceptDescriptionsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllConceptDescriptionsByIsCaseOfRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllConceptDescriptionsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllSubmodelElementsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllSubmodelReferencesRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllSubmodelsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllSubmodelsBySemanticIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllSubmodelsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAssetInformationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationAsyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PostAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PostConceptDescriptionRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PostSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PostSubmodelElementRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PostSubmodelReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PostSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PutAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PutAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PutAssetInformationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PutConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PutSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PutSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PutSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.SetSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValueParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.StringValue;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.request.RequestHandlerManager;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ResponseHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultIdentifierKeyValuePair;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultOperation;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultRange;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
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
    private static final AssetAdministrationShell AAS = AASFull.AAS_1;
    private static final Submodel SUBMODEL = AASFull.SUBMODEL_1;
    private static final SubmodelElement SUBMODEL_ELEMENT = AASFull.SUBMODEL_1.getSubmodelElements().get(0);
    private static final Reference SUBMODEL_ELEMENT_REF = AasUtils.toReference(AasUtils.toReference(SUBMODEL), SUBMODEL_ELEMENT);

    private static CoreConfig coreConfig;
    private static AssetAdministrationShellEnvironment environment;
    private static MessageBus messageBus;
    private static Persistence persistence;
    private static AssetConnectionManager assetConnectionManager;
    private static RequestHandlerManager manager;
    private static AssetValueProvider assetValueProvider;
    private static ServiceContext serviceContext;

    @Before
    public void createRequestHandlerManager() throws ConfigurationException, AssetConnectionException {
        environment = AASFull.createEnvironment();
        coreConfig = CoreConfig.builder().build();
        messageBus = mock(MessageBus.class);
        persistence = mock(Persistence.class);
        serviceContext = mock(ServiceContext.class);
        assetConnectionManager = spy(new AssetConnectionManager(coreConfig, List.of(), serviceContext));
        manager = new RequestHandlerManager(coreConfig, persistence, messageBus, assetConnectionManager);
        assetValueProvider = mock(AssetValueProvider.class);
        when(assetConnectionManager.getValueProvider(any())).thenReturn(assetValueProvider);
    }


    @Test
    public void testGetAllAssetAdministrationShellRequest() {
        when(persistence.get(any(), argThat((List<AssetIdentification> t) -> true), any()))
                .thenReturn(environment.getAssetAdministrationShells());
        GetAllAssetAdministrationShellsRequest request = new GetAllAssetAdministrationShellsRequest();
        GetAllAssetAdministrationShellsResponse actual = manager.execute(request);
        GetAllAssetAdministrationShellsResponse expected = new GetAllAssetAdministrationShellsResponse.Builder()
                .payload(environment.getAssetAdministrationShells())
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllAssetAdministrationShellsByAssetIdRequest() {
        GlobalAssetIdentification globalAssetIdentification = new GlobalAssetIdentification.Builder()
                .reference(new DefaultReference.Builder().key(new DefaultKey.Builder()
                        .idType(KeyType.IRI)
                        .type(KeyElements.GLOBAL_REFERENCE)
                        .value("TestValue")
                        .build())
                        .build())
                .build();
        SpecificAssetIdentification specificAssetIdentification = new SpecificAssetIdentification.Builder()
                .value("TestValue")
                .key("TestKey")
                .build();
        when(persistence.get(eq(null), eq(List.of(globalAssetIdentification, specificAssetIdentification)), any()))
                .thenReturn(List.of(environment.getAssetAdministrationShells().get(0), environment.getAssetAdministrationShells().get(1)));

        List<IdentifierKeyValuePair> assetIds = List.of(new DefaultIdentifierKeyValuePair.Builder()
                .key("globalAssetId")
                .value("TestValue")
                .externalSubjectId(new DefaultReference.Builder().build())
                .build(),
                new DefaultIdentifierKeyValuePair.Builder()
                        .key("TestKey")
                        .value("TestValue")
                        .build());
        GetAllAssetAdministrationShellsByAssetIdRequest request = new GetAllAssetAdministrationShellsByAssetIdRequest.Builder()
                .assetIds(assetIds)
                .build();
        GetAllAssetAdministrationShellsByAssetIdResponse actual = manager.execute(request);
        GetAllAssetAdministrationShellsByAssetIdResponse expected = new GetAllAssetAdministrationShellsByAssetIdResponse.Builder()
                .payload(List.of(environment.getAssetAdministrationShells().get(0), environment.getAssetAdministrationShells().get(1)))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllAssetAdministrationShellsByIdShortRequest() {
        when(persistence.get(eq("Test"), argThat((List<AssetIdentification> t) -> true), any()))
                .thenReturn(environment.getAssetAdministrationShells());
        GetAllAssetAdministrationShellsByIdShortRequest request = new GetAllAssetAdministrationShellsByIdShortRequest.Builder()
                .idShort("Test")
                .build();
        GetAllAssetAdministrationShellsByIdShortResponse actual = manager.execute(request);
        GetAllAssetAdministrationShellsByIdShortResponse expected = new GetAllAssetAdministrationShellsByIdShortResponse.Builder()
                .payload(environment.getAssetAdministrationShells())
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPostAssetAdministrationShellRequest() {
        when(persistence.put(environment.getAssetAdministrationShells().get(0)))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        PostAssetAdministrationShellRequest request = new PostAssetAdministrationShellRequest.Builder()
                .aas(environment.getAssetAdministrationShells().get(0))
                .build();
        PostAssetAdministrationShellResponse actual = manager.execute(request);
        PostAssetAdministrationShellResponse expected = new PostAssetAdministrationShellResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0))
                .statusCode(StatusCode.SUCCESS_CREATED)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAssetAdministrationShellByIdRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), new OutputModifier()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        GetAssetAdministrationShellByIdRequest request = GetAssetAdministrationShellByIdRequest.builder()
                .id(environment.getAssetAdministrationShells().get(0).getIdentification())
                .outputModifier(new OutputModifier())
                .build();
        GetAssetAdministrationShellByIdResponse actual = manager.execute(request);
        GetAssetAdministrationShellByIdResponse expected = new GetAssetAdministrationShellByIdResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPutAssetAdministrationShellByIdRequest() {
        when(persistence.put(environment.getAssetAdministrationShells().get(0)))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        PutAssetAdministrationShellByIdRequest request = new PutAssetAdministrationShellByIdRequest.Builder()
                .aas(environment.getAssetAdministrationShells().get(0))
                .build();
        PutAssetAdministrationShellByIdResponse actual = manager.execute(request);
        PutAssetAdministrationShellByIdResponse expected = new PutAssetAdministrationShellByIdResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testDeleteAssetAdministrationShellByIdRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), new QueryModifier()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        DeleteAssetAdministrationShellByIdRequest request = new DeleteAssetAdministrationShellByIdRequest().builder()
                .id(environment.getAssetAdministrationShells().get(0).getIdentification())
                .build();
        DeleteAssetAdministrationShellByIdResponse actual = manager.execute(request);
        DeleteAssetAdministrationShellByIdResponse expected = new DeleteAssetAdministrationShellByIdResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).remove(environment.getAssetAdministrationShells().get(0).getIdentification());
    }


    @Test
    public void testGetAssetAdministrationShellRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), new OutputModifier()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        GetAssetAdministrationShellRequest request = new GetAssetAdministrationShellRequest.Builder()
                .id(AAS.getIdentification())
                .build();
        GetAssetAdministrationShellResponse actual = manager.execute(request);
        GetAssetAdministrationShellResponse expected = new GetAssetAdministrationShellResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPutAssetAdministrationShellRequest() throws ResourceNotFoundException {
        // @TODO: open/unclear
        // expected Identifiable
        when(persistence.put(environment.getAssetAdministrationShells().get(0)))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        PutAssetAdministrationShellRequest request = new PutAssetAdministrationShellRequest.Builder()
                .aas(environment.getAssetAdministrationShells().get(0))
                .id(AAS.getIdentification())
                .build();
        PutAssetAdministrationShellResponse actual = manager.execute(request);
        PutAssetAdministrationShellResponse expected = new PutAssetAdministrationShellResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAssetInformationRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), new QueryModifier()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        GetAssetInformationRequest request = new GetAssetInformationRequest.Builder()
                .id(environment.getAssetAdministrationShells().get(0).getIdentification())
                .build();
        GetAssetInformationResponse actual = manager.execute(request);
        GetAssetInformationResponse expected = new GetAssetInformationResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0).getAssetInformation())
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPutAssetInformationRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), new QueryModifier()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        when(persistence.put(environment.getAssetAdministrationShells().get(0)))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        PutAssetInformationRequest request = new PutAssetInformationRequest.Builder()
                .id(environment.getAssetAdministrationShells().get(0).getIdentification())
                .assetInformation(environment.getAssetAdministrationShells().get(0).getAssetInformation())
                .build();
        PutAssetInformationResponse actual = manager.execute(request);
        PutAssetInformationResponse expected = new PutAssetInformationResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllSubmodelReferencesRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), new OutputModifier()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        GetAllSubmodelReferencesRequest request = new GetAllSubmodelReferencesRequest.Builder()
                .id(environment.getAssetAdministrationShells().get(0).getIdentification())
                .outputModifier(new OutputModifier())
                .build();
        GetAllSubmodelReferencesResponse actual = manager.execute(request);
        GetAllSubmodelReferencesResponse expected = new GetAllSubmodelReferencesResponse.Builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(environment.getAssetAdministrationShells().get(0).getSubmodels())
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPostSubmodelReferenceRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), new QueryModifier()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        when(persistence.put(environment.getSubmodels().get(0)))
                .thenReturn(environment.getSubmodels().get(0));
        PostSubmodelReferenceRequest request = new PostSubmodelReferenceRequest.Builder()
                .id(environment.getAssetAdministrationShells().get(0).getIdentification())
                .submodelRef(SUBMODEL_ELEMENT_REF)
                .build();
        PostSubmodelReferenceResponse actual = manager.execute(request);
        PostSubmodelReferenceResponse expected = new PostSubmodelReferenceResponse.Builder()
                .statusCode(StatusCode.SUCCESS_CREATED)
                .payload(SUBMODEL_ELEMENT_REF)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testDeleteSubmodelReferenceRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), new QueryModifier()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        DeleteSubmodelReferenceRequest request = new DeleteSubmodelReferenceRequest.Builder()
                .id(environment.getAssetAdministrationShells().get(0).getIdentification())
                .submodelRef(SUBMODEL_ELEMENT_REF)
                .build();
        DeleteSubmodelReferenceResponse actual = manager.execute(request);
        DeleteSubmodelReferenceResponse expected = new DeleteSubmodelReferenceResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllSubmodelsRequest() throws ResourceNotFoundException {
        when(persistence.get(null, (Reference) null, new OutputModifier()))
                .thenReturn(environment.getSubmodels());

        GetAllSubmodelsRequest request = new GetAllSubmodelsRequest.Builder()
                .outputModifier(new OutputModifier())
                .build();
        GetAllSubmodelsResponse actual = manager.execute(request);
        GetAllSubmodelsResponse expected = new GetAllSubmodelsResponse.Builder()
                .payload(environment.getSubmodels())
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllSubmodelsBySemanticIdRequest() throws ResourceNotFoundException {
        when(persistence.get(null, SUBMODEL_ELEMENT_REF, new OutputModifier()))
                .thenReturn(environment.getSubmodels());
        GetAllSubmodelsBySemanticIdRequest request = new GetAllSubmodelsBySemanticIdRequest.Builder()
                .semanticId(SUBMODEL_ELEMENT_REF)
                .outputModifier(new OutputModifier())
                .build();
        GetAllSubmodelsBySemanticIdResponse actual = manager.execute(request);
        GetAllSubmodelsBySemanticIdResponse expected = new GetAllSubmodelsBySemanticIdResponse.Builder()
                .payload(environment.getSubmodels())
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllSubmodelsByIdShortRequest() throws ResourceNotFoundException {
        when(persistence.get("Test", (Reference) null, new OutputModifier()))
                .thenReturn(environment.getSubmodels());
        GetAllSubmodelsByIdShortRequest request = new GetAllSubmodelsByIdShortRequest.Builder()
                .idShort("Test")
                .outputModifier(new OutputModifier())
                .build();
        GetAllSubmodelsByIdShortResponse actual = manager.execute(request);
        GetAllSubmodelsByIdShortResponse expected = new GetAllSubmodelsByIdShortResponse.Builder()
                .payload(environment.getSubmodels())
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPostSubmodelRequest() throws ResourceNotFoundException {
        when(persistence.put(environment.getSubmodels().get(0)))
                .thenReturn(environment.getSubmodels().get(0));
        PostSubmodelRequest request = new PostSubmodelRequest.Builder()
                .submodel(environment.getSubmodels().get(0))
                .build();
        PostSubmodelResponse actual = manager.execute(request);
        PostSubmodelResponse expected = new PostSubmodelResponse.Builder()
                .payload(environment.getSubmodels().get(0))
                .statusCode(StatusCode.SUCCESS_CREATED)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetSubmodelByIdRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getSubmodels().get(0).getIdentification(), new OutputModifier()))
                .thenReturn(environment.getSubmodels().get(0));
        GetSubmodelByIdRequest request = new GetSubmodelByIdRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .outputModifier(new OutputModifier())
                .build();
        GetSubmodelByIdResponse actual = manager.execute(request);
        GetSubmodelByIdResponse expected = new GetSubmodelByIdResponse.Builder()
                .payload(environment.getSubmodels().get(0))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPutSubmodelByIdRequest() throws ResourceNotFoundException {
        when(persistence.put(environment.getSubmodels().get(0)))
                .thenReturn(environment.getSubmodels().get(0));
        PutSubmodelByIdRequest request = new PutSubmodelByIdRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .submodel(environment.getSubmodels().get(0))
                .build();
        PutSubmodelByIdResponse actual = manager.execute(request);
        PutSubmodelByIdResponse expected = new PutSubmodelByIdResponse.Builder()
                .payload(environment.getSubmodels().get(0))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testDeleteSubmodelByIdRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getSubmodels().get(0).getIdentification(), new QueryModifier()))
                .thenReturn(environment.getSubmodels().get(0));
        DeleteSubmodelByIdRequest request = new DeleteSubmodelByIdRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .build();
        DeleteSubmodelByIdResponse actual = manager.execute(request);
        DeleteSubmodelByIdResponse expected = new DeleteSubmodelByIdResponse.Builder()
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).remove(environment.getSubmodels().get(0).getIdentification());
    }


    @Test
    public void testGetSubmodelRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getSubmodels().get(0).getIdentification(), new OutputModifier()))
                .thenReturn(environment.getSubmodels().get(0));
        GetSubmodelRequest request = new GetSubmodelRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .outputModifier(new OutputModifier())
                .build();
        GetSubmodelResponse actual = manager.execute(request);
        GetSubmodelResponse expected = new GetSubmodelResponse.Builder()
                .payload(environment.getSubmodels().get(0))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPutSubmodelRequest() throws ResourceNotFoundException {
        when(persistence.put(environment.getSubmodels().get(0)))
                .thenReturn(environment.getSubmodels().get(0));
        PutSubmodelRequest request = new PutSubmodelRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .submodel(environment.getSubmodels().get(0))
                .outputModifier(new OutputModifier())
                .submodel(environment.getSubmodels().get(0))
                .build();
        PutSubmodelResponse actual = manager.execute(request);
        PutSubmodelResponse expected = new PutSubmodelResponse.Builder()
                .payload(environment.getSubmodels().get(0))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllSubmodelElementsRequest() throws ResourceNotFoundException {
        Reference reference = ReferenceHelper.toReference(environment.getSubmodels().get(0).getIdentification(), Submodel.class);
        when(persistence.getSubmodelElements(reference, (Reference) null, new OutputModifier()))
                .thenReturn(environment.getSubmodels().get(0).getSubmodelElements());
        GetAllSubmodelElementsRequest request = new GetAllSubmodelElementsRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .outputModifier(new OutputModifier())
                .build();
        GetAllSubmodelElementsResponse actual = manager.execute(request);
        GetAllSubmodelElementsResponse expected = new GetAllSubmodelElementsResponse.Builder()
                .payload(environment.getSubmodels().get(0).getSubmodelElements())
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPostSubmodelElementRequest() throws ResourceNotFoundException {
        Reference reference = ReferenceHelper.toReference(environment.getSubmodels().get(0).getIdentification(), Submodel.class);
        when(persistence.put(reference, (Reference) null, environment.getSubmodels().get(0).getSubmodelElements().get(0)))
                .thenReturn(environment.getSubmodels().get(0).getSubmodelElements().get(0));
        PostSubmodelElementRequest request = new PostSubmodelElementRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .submodelElement(environment.getSubmodels().get(0).getSubmodelElements().get(0))
                .build();
        PostSubmodelElementResponse actual = manager.execute(request);
        PostSubmodelElementResponse expected = new PostSubmodelElementResponse.Builder()
                .statusCode(StatusCode.SUCCESS_CREATED)
                .payload(environment.getSubmodels().get(0).getSubmodelElements().get(0))
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetSubmodelElementByPathRequest() throws ResourceNotFoundException, AssetConnectionException {
        Submodel submodel = environment.getSubmodels().get(0);
        SubmodelElement cur_submodelElement = new DefaultProperty.Builder()
                .idShort("testIdShort")
                .value("testValue")
                .build();
        PropertyValue propertyValue = new PropertyValue.Builder().value(new StringValue("test")).build();
        when(persistence.get(argThat((Reference t) -> true), eq(new OutputModifier())))
                .thenReturn(cur_submodelElement);
        when(assetConnectionManager.hasValueProvider(any())).thenReturn(true);
        when(assetValueProvider.getValue()).thenReturn(propertyValue);

        GetSubmodelElementByPathRequest request = new GetSubmodelElementByPathRequest.Builder()
                .id(submodel.getIdentification())
                .outputModifier(new OutputModifier())
                .path(ReferenceHelper.toKeys(SUBMODEL_ELEMENT_REF))
                .build();
        GetSubmodelElementByPathResponse actual = manager.execute(request);

        SubmodelElement expected_submodelElement = new DefaultProperty.Builder()
                .idShort("testIdShort")
                .value("test")
                .valueType("string")
                .build();
        GetSubmodelElementByPathResponse expected = new GetSubmodelElementByPathResponse.Builder()
                .payload(expected_submodelElement)
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPostSubmodelElementByPathRequest() throws ResourceNotFoundException {
        when(persistence.put(any(), argThat((Reference t) -> true), any()))
                .thenReturn(environment.getSubmodels().get(0).getSubmodelElements().get(0));
        PostSubmodelElementByPathRequest request = new PostSubmodelElementByPathRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .path(ReferenceHelper.toKeys(SUBMODEL_ELEMENT_REF))
                .build();
        PostSubmodelElementByPathResponse actual = manager.execute(request);
        PostSubmodelElementByPathResponse expected = new PostSubmodelElementByPathResponse.Builder()
                .payload(environment.getSubmodels().get(0).getSubmodelElements().get(0))
                .statusCode(StatusCode.SUCCESS_CREATED)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPutSubmodelElementByPathRequest() throws ResourceNotFoundException, AssetConnectionException, ValueMappingException {
        SubmodelElement currentSubmodelElement = new DefaultProperty.Builder()
                .idShort("TestIdshort")
                .valueType("string")
                .value("TestValue")
                .build();
        SubmodelElement newSubmodelElement = new DefaultProperty.Builder()
                .idShort("TestIdshort")
                .valueType("string")
                .value("NewTestValue")
                .build();
        when(persistence.get(argThat((Reference t) -> true), any()))
                .thenReturn(currentSubmodelElement);
        when(persistence.put(any(), argThat((Reference t) -> true), any()))
                .thenReturn(newSubmodelElement);
        when(assetConnectionManager.hasValueProvider(any())).thenReturn(true);

        PutSubmodelElementByPathRequest request = new PutSubmodelElementByPathRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .submodelElement(newSubmodelElement)
                .build();
        PutSubmodelElementByPathResponse actual = manager.execute(request);
        PutSubmodelElementByPathResponse expected = new PutSubmodelElementByPathResponse.Builder()
                .payload(newSubmodelElement)
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(assetValueProvider).setValue(ElementValueMapper.toValue(newSubmodelElement));
    }


    @Test
    public void testSetSubmodelElementValueByPathRequest() throws ResourceNotFoundException, AssetConnectionException {
        when(persistence.get((Reference) any(), any()))
                .thenReturn(environment.getSubmodels().get(0).getSubmodelElements().get(0));
        when(assetConnectionManager.hasValueProvider(any())).thenReturn(true);
        PropertyValue propertyValue = new PropertyValue.Builder()
                .value(new StringValue("Test"))
                .build();
        SetSubmodelElementValueByPathRequest request = new SetSubmodelElementValueByPathRequest.Builder<ElementValue>()
                .id(environment.getSubmodels().get(0).getIdentification())
                .value(propertyValue)
                .valueParser(new ElementValueParser<ElementValue>() {
                    @Override
                    public <U extends ElementValue> U parse(ElementValue raw, Class<U> type) {
                        return (U) raw;
                    }
                })
                .path(ReferenceHelper.toKeys(SUBMODEL_ELEMENT_REF))
                .build();

        Response actual = manager.execute(request);
        SetSubmodelElementValueByPathResponse expected = new SetSubmodelElementValueByPathResponse.Builder()
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(assetValueProvider).setValue(propertyValue);
    }


    @Test
    public void testDeleteSubmodelElementByPathRequest() throws ResourceNotFoundException {
        Submodel submodel = environment.getSubmodels().get(0);
        Reference reference = ReferenceHelper.toReference(ReferenceHelper.toKeys(SUBMODEL_ELEMENT_REF),
                submodel.getIdentification(),
                Submodel.class);
        when(persistence.get(reference, new QueryModifier()))
                .thenReturn(environment.getSubmodels().get(0).getSubmodelElements().get(0));
        DeleteSubmodelElementByPathRequest request = new DeleteSubmodelElementByPathRequest.Builder()
                .id(submodel.getIdentification())
                .path(ReferenceHelper.toKeys(SUBMODEL_ELEMENT_REF))
                .build();
        DeleteSubmodelElementByPathResponse actual = manager.execute(request);
        DeleteSubmodelElementByPathResponse expected = new DeleteSubmodelElementByPathResponse.Builder()
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).remove(reference);
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
                .id(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.IRI)
                        .identifier("http://example.org")
                        .build())
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
                .id(new DefaultIdentifier.Builder()
                        .idType(IdentifierType.IRI)
                        .identifier("http://example.org")
                        .build())
                .build();

        InvokeOperationSyncResponse actual = manager.execute(invokeOperationSyncRequest);
        InvokeOperationSyncResponse expected = new InvokeOperationSyncResponse.Builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(new OperationResult.Builder()
                        .requestId("1")
                        .inoutputArguments(List.of(new DefaultOperationVariable.Builder()
                                .value(new DefaultProperty.Builder()
                                        .idShort("TestProp")
                                        .value("TestOutput")
                                        .build())
                                .build()))
                        .outputArguments(operation.getInputVariables())
                        .executionState(ExecutionState.COMPLETED)
                        .build())
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
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
            // intentionally left empty
        }
    }

    @Test
    public void testGetAllConceptDescriptionsRequest() throws ResourceNotFoundException {
        when(persistence.get(null, null, null, new OutputModifier()))
                .thenReturn(environment.getConceptDescriptions());
        GetAllConceptDescriptionsRequest request = new GetAllConceptDescriptionsRequest.Builder()
                .outputModifier(new OutputModifier())
                .build();
        GetAllConceptDescriptionsResponse actual = manager.execute(request);
        GetAllConceptDescriptionsResponse expected = new GetAllConceptDescriptionsResponse.Builder()
                .payload(environment.getConceptDescriptions())
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllConceptDescriptionsByIdShortRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getConceptDescriptions().get(0).getIdShort(), null, null, new OutputModifier()))
                .thenReturn(environment.getConceptDescriptions());
        GetAllConceptDescriptionsByIdShortRequest request = new GetAllConceptDescriptionsByIdShortRequest.Builder()
                .outputModifier(new OutputModifier())
                .idShort(environment.getConceptDescriptions().get(0).getIdShort())
                .build();
        GetAllConceptDescriptionsByIdShortResponse actual = manager.execute(request);
        GetAllConceptDescriptionsByIdShortResponse expected = new GetAllConceptDescriptionsByIdShortResponse.Builder()
                .payload(environment.getConceptDescriptions())
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllConceptDescriptionsByIsCaseOfRequest() throws ResourceNotFoundException {
        Reference reference = ReferenceHelper.toReference(environment.getConceptDescriptions().get(0).getIdentification(), ConceptDescription.class);
        when(persistence.get(null, reference, null, new OutputModifier()))
                .thenReturn(environment.getConceptDescriptions());
        GetAllConceptDescriptionsByIsCaseOfRequest request = new GetAllConceptDescriptionsByIsCaseOfRequest.Builder()
                .outputModifier(new OutputModifier())
                .isCaseOf(reference)
                .build();
        GetAllConceptDescriptionsByIsCaseOfResponse actual = manager.execute(request);
        GetAllConceptDescriptionsByIsCaseOfResponse expected = new GetAllConceptDescriptionsByIsCaseOfResponse.Builder()
                .payload(environment.getConceptDescriptions())
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllConceptDescriptionsByDataSpecificationReferenceRequest() throws ResourceNotFoundException {
        Reference reference = ReferenceHelper.toReference(environment.getConceptDescriptions().get(0).getIdentification(), ConceptDescription.class);
        when(persistence.get(null, null, reference, new OutputModifier()))
                .thenReturn(environment.getConceptDescriptions());
        GetAllConceptDescriptionsByDataSpecificationReferenceRequest request = new GetAllConceptDescriptionsByDataSpecificationReferenceRequest.Builder()
                .outputModifier(new OutputModifier())
                .dataSpecification(reference)
                .build();
        GetAllConceptDescriptionsByDataSpecificationReferenceResponse actual = manager.execute(request);
        GetAllConceptDescriptionsByDataSpecificationReferenceResponse expected = new GetAllConceptDescriptionsByDataSpecificationReferenceResponse.Builder()
                .payload(environment.getConceptDescriptions())
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPostConceptDescriptionRequest() throws ResourceNotFoundException {
        when(persistence.put(environment.getConceptDescriptions().get(0)))
                .thenReturn(environment.getConceptDescriptions().get(0));
        PostConceptDescriptionRequest request = new PostConceptDescriptionRequest.Builder()
                .conceptDescription(environment.getConceptDescriptions().get(0))
                .build();
        PostConceptDescriptionResponse actual = manager.execute(request);
        PostConceptDescriptionResponse expected = new PostConceptDescriptionResponse.Builder()
                .payload(environment.getConceptDescriptions().get(0))
                .statusCode(StatusCode.SUCCESS_CREATED)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetConceptDescriptionByIdRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getConceptDescriptions().get(0).getIdentification(), new OutputModifier()))
                .thenReturn(environment.getConceptDescriptions().get(0));
        GetConceptDescriptionByIdRequest request = new GetConceptDescriptionByIdRequest.Builder()
                .outputModifier(new OutputModifier())
                .id(environment.getConceptDescriptions().get(0).getIdentification())
                .build();
        GetConceptDescriptionByIdResponse actual = manager.execute(request);
        GetConceptDescriptionByIdResponse expected = new GetConceptDescriptionByIdResponse.Builder()
                .payload(environment.getConceptDescriptions().get(0))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPutConceptDescriptionByIdRequest() throws ResourceNotFoundException {
        when(persistence.put(environment.getConceptDescriptions().get(0)))
                .thenReturn(environment.getConceptDescriptions().get(0));
        PutConceptDescriptionByIdRequest request = new PutConceptDescriptionByIdRequest.Builder()
                .conceptDescription(environment.getConceptDescriptions().get(0))
                .build();
        PutConceptDescriptionByIdResponse actual = manager.execute(request);
        PutConceptDescriptionByIdResponse expected = new PutConceptDescriptionByIdResponse.Builder()
                .payload(environment.getConceptDescriptions().get(0))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testDeleteConceptDescriptionByIdRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getConceptDescriptions().get(0).getIdentification(), new QueryModifier()))
                .thenReturn(environment.getConceptDescriptions().get(0));
        DeleteConceptDescriptionByIdRequest request = new DeleteConceptDescriptionByIdRequest.Builder()
                .id(environment.getConceptDescriptions().get(0).getIdentification())
                .build();
        DeleteConceptDescriptionByIdResponse actual = manager.execute(request);
        DeleteConceptDescriptionByIdResponse expected = new DeleteConceptDescriptionByIdResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).remove(environment.getConceptDescriptions().get(0).getIdentification());
    }


    @Test
    public void testGetIdentifiableWithInvalidIdRequest() throws ResourceNotFoundException {
        when(persistence.get(argThat((Identifier t) -> true), any()))
                .thenThrow(new ResourceNotFoundException("Resource not found with id"));
        GetSubmodelByIdRequest request = new GetSubmodelByIdRequest.Builder().build();
        GetSubmodelByIdResponse actual = manager.execute(request);
        GetSubmodelByIdResponse expected = new GetSubmodelByIdResponse.Builder()
                .result(Result.error("Resource not found with id"))
                .statusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetReferableWithInvalidIdRequest() throws ResourceNotFoundException {
        when(persistence.get(argThat((Reference r) -> true), any()))
                .thenThrow(new ResourceNotFoundException("Resource not found with id"));
        GetSubmodelElementByPathRequest request = getExampleGetSubmodelElementByPathRequest();
        GetSubmodelElementByPathResponse actual = manager.execute(request);
        GetSubmodelElementByPathResponse expected = new GetSubmodelElementByPathResponse.Builder()
                .result(Result.error("Resource not found with id"))
                .statusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetReferableWithMessageBusExceptionRequest() throws ResourceNotFoundException, MessageBusException {
        when(persistence.get(argThat((Reference r) -> true), any()))
                .thenReturn(new DefaultProperty());
        doThrow(new MessageBusException("Invalid Messagbus Call")).when(messageBus).publish(any());
        GetSubmodelElementByPathRequest request = getExampleGetSubmodelElementByPathRequest();
        GetSubmodelElementByPathResponse actual = manager.execute(request);
        GetSubmodelElementByPathResponse expected = new GetSubmodelElementByPathResponse.Builder()
                .result(Result.exception("Invalid Messagbus Call"))
                .statusCode(StatusCode.SERVER_INTERNAL_ERROR)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetValueWithInvalidAssetConnectionRequest() throws ResourceNotFoundException, AssetConnectionException {
        when(persistence.get(argThat((Reference r) -> true), any()))
                .thenReturn(new DefaultProperty());
        AssetValueProvider assetValueProvider = mock(AssetValueProvider.class);
        when(assetConnectionManager.hasValueProvider(any())).thenReturn(true);
        when(assetConnectionManager.getValueProvider(any())).thenReturn(assetValueProvider);
        when(assetValueProvider.getValue()).thenThrow(new AssetConnectionException("Invalid Assetconnection"));
        GetSubmodelElementByPathRequest request = getExampleGetSubmodelElementByPathRequest();
        GetSubmodelElementByPathResponse actual = manager.execute(request);
        GetSubmodelElementByPathResponse expected = new GetSubmodelElementByPathResponse.Builder()
                .result(Result.exception("Invalid Assetconnection"))
                .statusCode(StatusCode.SERVER_INTERNAL_ERROR)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    private GetSubmodelElementByPathRequest getExampleGetSubmodelElementByPathRequest() {
        return new GetSubmodelElementByPathRequest.Builder()
                .path(List.of(new DefaultKey.Builder()
                        .value("testProperty")
                        .type(KeyElements.PROPERTY)
                        .idType(KeyType.ID_SHORT)
                        .build()))
                .id(new DefaultIdentifier.Builder().identifier("test").idType(IdentifierType.IRI).build())
                .build();
    }


    @Test
    public void testGetAllAssetAdministrationShellRequestAsync() throws InterruptedException {
        when(persistence.get(any(), argThat((List<AssetIdentification> t) -> true), any()))
                .thenReturn(environment.getAssetAdministrationShells());
        GetAllAssetAdministrationShellsRequest request = new GetAllAssetAdministrationShellsRequest();
        final AtomicReference<GetAllAssetAdministrationShellsResponse> response = new AtomicReference<>();
        CountDownLatch condition = new CountDownLatch(1);
        manager.executeAsync(request, x -> response.set(x));
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(environment.getAssetAdministrationShells(), response.get().getPayload());
    }


    @Test
    public void testReadValueFromAssetConnectionAndUpdatePersistence() throws AssetConnectionException, ResourceNotFoundException, ValueMappingException, MessageBusException {
        RequestHandler requestHandler = new DeleteSubmodelByIdRequestHandler(persistence, messageBus, assetConnectionManager);
        Reference parentRef = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .value("sub")
                        .idType(KeyType.IRI)
                        .type(KeyElements.SUBMODEL)
                        .build())
                .build();
        SubmodelElement prop1 = new DefaultProperty.Builder()
                .idShort("prop1")
                .value("test")
                .valueType("string")
                .build();
        SubmodelElement range = new DefaultRange.Builder()
                .idShort("range1")
                .max("1.0")
                .min("0.0")
                .valueType("double")
                .build();
        SubmodelElement prop2 = new DefaultProperty.Builder()
                .idShort("prop2")
                .value("test")
                .valueType("string")
                .build();
        SubmodelElementCollection collection = new DefaultSubmodelElementCollection.Builder()
                .idShort("col1")
                .value(prop2)
                .build();

        SubmodelElement prop1Expected = new DefaultProperty.Builder()
                .idShort("prop1")
                .value("testNew")
                .valueType("string")
                .build();
        SubmodelElement rangeExpected = new DefaultRange.Builder()
                .idShort("range1")
                .max("1.0")
                .min("0.0")
                .valueType("double")
                .build();
        SubmodelElement prop2Expected = new DefaultProperty.Builder()
                .idShort("prop2")
                .value("testNew")
                .valueType("string")
                .build();
        Reference prop1Ref = AasUtils.toReference(parentRef, prop1);
        Reference prop2Ref = AasUtils.toReference(AasUtils.toReference(parentRef, collection), prop2);
        Reference rangeRef = AasUtils.toReference(parentRef, range);
        List<SubmodelElement> submodelElements = List.of(prop1, range, collection);
        AssetValueProvider prop1Provider = mock(AssetValueProvider.class);
        AssetValueProvider prop2Provider = mock(AssetValueProvider.class);
        AssetValueProvider rangeProvider = mock(AssetValueProvider.class);
        when(assetConnectionManager.hasValueProvider(prop1Ref)).thenReturn(true);
        when(assetConnectionManager.hasValueProvider(prop2Ref)).thenReturn(true);
        when(assetConnectionManager.hasValueProvider(rangeRef)).thenReturn(true);

        when(assetConnectionManager.getValueProvider(prop1Ref)).thenReturn(prop1Provider);
        when(prop1Provider.getValue()).thenReturn(ElementValueMapper.toValue(prop1Expected));

        when(assetConnectionManager.getValueProvider(prop2Ref)).thenReturn(prop2Provider);
        when(prop2Provider.getValue()).thenReturn(ElementValueMapper.toValue(prop2Expected));

        when(assetConnectionManager.getValueProvider(rangeRef)).thenReturn(rangeProvider);
        when(rangeProvider.getValue()).thenReturn(ElementValueMapper.toValue(rangeExpected));

        requestHandler.syncWithAsset(parentRef, submodelElements);
        verify(persistence).put(null, prop1Ref, prop1Expected);
        verify(persistence).put(null, prop2Ref, prop2Expected);
        verify(persistence).put(null, rangeRef, rangeExpected);
        Assert.assertEquals(prop1Expected, prop1);
        Assert.assertEquals(prop2Expected, prop2);
        Assert.assertEquals(rangeExpected, range);
    }

}
