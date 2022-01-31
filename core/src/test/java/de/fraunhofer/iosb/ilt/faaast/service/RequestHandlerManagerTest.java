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
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.SpecificAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.*;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.*;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.response.*;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValueParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.values.StringValue;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.requesthandlers.Util;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementPathUtils;
import io.adminshell.aas.v3.dataformat.core.AASFull;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.*;
import io.adminshell.aas.v3.model.impl.DefaultIdentifierKeyValuePair;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultOperation;
import io.adminshell.aas.v3.model.impl.DefaultOperationVariable;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultReference;
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
    private static final String GLOBAL_ASSET_ID = "globalAssetId";
    private static final Submodel SUBMODEL = AASFull.SUBMODEL_1;
    private static final SubmodelElement SUBMODEL_ELEMENT = AASFull.SUBMODEL_1.getSubmodelElements().get(0);
    private static final Reference SUBMODEL_ELEMENT_REF = AasUtils.toReference(AasUtils.toReference(SUBMODEL), SUBMODEL_ELEMENT);

    private static CoreConfig coreConfig;
    private static AssetAdministrationShellEnvironment environment;
    private static MessageBus messageBus;
    private static Persistence persistence;
    private static AssetConnectionManager assetConnectionManager;
    private static RequestHandlerManager manager;

    @Before
    public void createRequestHandlerManager() {
        environment = AASFull.createEnvironment();
        coreConfig = CoreConfig.builder().build();
        messageBus = mock(MessageBus.class);
        persistence = mock(Persistence.class);
        assetConnectionManager = mock(AssetConnectionManager.class);
        manager = new RequestHandlerManager(coreConfig, persistence, messageBus, assetConnectionManager);
    }


    @Test
    public void testGetAllAssetAdministrationShellRequest() {
        when(persistence.get(any(), argThat((List<AssetIdentification> t) -> true), any()))
                .thenReturn(environment.getAssetAdministrationShells());
        GetAllAssetAdministrationShellsRequest request = new GetAllAssetAdministrationShellsRequest();
        GetAllAssetAdministrationShellsResponse response = manager.execute(request);
        GetAllAssetAdministrationShellsResponse expected = new GetAllAssetAdministrationShellsResponse.Builder()
                .payload(environment.getAssetAdministrationShells())
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
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
        GetAllAssetAdministrationShellsByAssetIdResponse response = manager.execute(request);
        GetAllAssetAdministrationShellsByAssetIdResponse expected = new GetAllAssetAdministrationShellsByAssetIdResponse.Builder()
                .payload(List.of(environment.getAssetAdministrationShells().get(0), environment.getAssetAdministrationShells().get(1)))
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testGetAllAssetAdministrationShellsByIdShortRequest() {
        when(persistence.get(eq("Test"), argThat((List<AssetIdentification> t) -> true), any()))
                .thenReturn(environment.getAssetAdministrationShells());
        GetAllAssetAdministrationShellsByIdShortRequest request = new GetAllAssetAdministrationShellsByIdShortRequest.Builder()
                .idShort("Test")
                .build();
        GetAllAssetAdministrationShellsByIdShortResponse response = manager.execute(request);
        GetAllAssetAdministrationShellsByIdShortResponse expected = new GetAllAssetAdministrationShellsByIdShortResponse.Builder()
                .payload(environment.getAssetAdministrationShells())
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testPostAssetAdministrationShellRequest() {
        when(persistence.put(environment.getAssetAdministrationShells().get(0)))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        PostAssetAdministrationShellRequest request = new PostAssetAdministrationShellRequest.Builder()
                .aas(environment.getAssetAdministrationShells().get(0))
                .build();
        PostAssetAdministrationShellResponse response = manager.execute(request);
        PostAssetAdministrationShellResponse expected = new PostAssetAdministrationShellResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0))
                .statusCode(StatusCode.SuccessCreated)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testGetAssetAdministrationShellByIdRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), new OutputModifier()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        GetAssetAdministrationShellByIdRequest request = GetAssetAdministrationShellByIdRequest.builder()
                .id(environment.getAssetAdministrationShells().get(0).getIdentification())
                .outputModifier(new OutputModifier())
                .build();
        GetAssetAdministrationShellByIdResponse response = manager.execute(request);
        GetAssetAdministrationShellByIdResponse expected = new GetAssetAdministrationShellByIdResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0))
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testPutAssetAdministrationShellByIdRequest() {
        when(persistence.put(environment.getAssetAdministrationShells().get(0)))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        PutAssetAdministrationShellByIdRequest request = new PutAssetAdministrationShellByIdRequest.Builder()
                .aas(environment.getAssetAdministrationShells().get(0))
                .build();
        PutAssetAdministrationShellByIdResponse response = manager.execute(request);
        PutAssetAdministrationShellByIdResponse expected = new PutAssetAdministrationShellByIdResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0))
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testDeleteAssetAdministrationShellByIdRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), new QueryModifier()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        DeleteAssetAdministrationShellByIdRequest request = new DeleteAssetAdministrationShellByIdRequest().builder()
                .id(environment.getAssetAdministrationShells().get(0).getIdentification())
                .build();
        DeleteAssetAdministrationShellByIdResponse response = manager.execute(request);
        DeleteAssetAdministrationShellByIdResponse expected = new DeleteAssetAdministrationShellByIdResponse.Builder()
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
        verify(persistence).remove(environment.getAssetAdministrationShells().get(0).getIdentification());
    }


    @Test
    public void testGetAssetAdministrationShellRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), new OutputModifier()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        GetAssetAdministrationShellRequest request = new GetAssetAdministrationShellRequest.Builder()
                .id(AAS.getIdentification())
                .build();
        GetAssetAdministrationShellResponse response = manager.execute(request);
        GetAssetAdministrationShellResponse expected = new GetAssetAdministrationShellResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0))
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
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
        PutAssetAdministrationShellResponse response = manager.execute(request);
        PutAssetAdministrationShellResponse expected = new PutAssetAdministrationShellResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0))
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testGetAssetInformationRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), new QueryModifier()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        GetAssetInformationRequest request = new GetAssetInformationRequest.Builder()
                .id(environment.getAssetAdministrationShells().get(0).getIdentification())
                .build();
        GetAssetInformationResponse response = manager.execute(request);
        GetAssetInformationResponse expected = new GetAssetInformationResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0).getAssetInformation())
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
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
        PutAssetInformationResponse response = manager.execute(request);
        PutAssetInformationResponse expected = new PutAssetInformationResponse.Builder()
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testGetAllSubmodelReferencesRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), new OutputModifier()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        GetAllSubmodelReferencesRequest request = new GetAllSubmodelReferencesRequest.Builder()
                .id(environment.getAssetAdministrationShells().get(0).getIdentification())
                .outputModifier(new OutputModifier())
                .build();
        GetAllSubmodelReferencesResponse response = manager.execute(request);
        GetAllSubmodelReferencesResponse expected = new GetAllSubmodelReferencesResponse.Builder()
                .statusCode(StatusCode.Success)
                .payload(environment.getAssetAdministrationShells().get(0).getSubmodels())
                .build();
        Assert.assertEquals(expected, response);
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
        PostSubmodelReferenceResponse response = manager.execute(request);
        PostSubmodelReferenceResponse expected = new PostSubmodelReferenceResponse.Builder()
                .statusCode(StatusCode.SuccessCreated)
                .payload(SUBMODEL_ELEMENT_REF)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testDeleteSubmodelReferenceRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), new QueryModifier()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        DeleteSubmodelReferenceRequest request = new DeleteSubmodelReferenceRequest.Builder()
                .id(environment.getAssetAdministrationShells().get(0).getIdentification())
                .submodelRef(SUBMODEL_ELEMENT_REF)
                .build();
        DeleteSubmodelReferenceResponse response = manager.execute(request);
        DeleteSubmodelReferenceResponse expected = new DeleteSubmodelReferenceResponse.Builder()
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
        //verify(persistence).remove(environment.getAssetAdministrationShells().get(0).getIdentification());
    }


    @Test
    public void testGetAllSubmodelsRequest() throws ResourceNotFoundException {
        when(persistence.get(null, (Reference) null, new OutputModifier()))
                .thenReturn(environment.getSubmodels());
        GetAllSubmodelsRequest request = new GetAllSubmodelsRequest.Builder()
                .outputModifier(new OutputModifier())
                .build();
        GetAllSubmodelsResponse response = manager.execute(request);
        GetAllSubmodelsResponse expected = new GetAllSubmodelsResponse.Builder()
                .payload(environment.getSubmodels())
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testGetAllSubmodelsBySemanticIdRequest() throws ResourceNotFoundException {
        when(persistence.get(null, SUBMODEL_ELEMENT_REF, new OutputModifier()))
                .thenReturn(environment.getSubmodels());
        GetAllSubmodelsBySemanticIdRequest request = new GetAllSubmodelsBySemanticIdRequest.Builder()
                .semanticId(SUBMODEL_ELEMENT_REF)
                .outputModifier(new OutputModifier())
                .build();
        GetAllSubmodelsBySemanticIdResponse response = manager.execute(request);
        GetAllSubmodelsBySemanticIdResponse expected = new GetAllSubmodelsBySemanticIdResponse.Builder()
                .payload(environment.getSubmodels())
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testGetAllSubmodelsByIdShortRequest() throws ResourceNotFoundException {
        when(persistence.get("Test", (Reference) null, new OutputModifier()))
                .thenReturn(environment.getSubmodels());
        GetAllSubmodelsByIdShortRequest request = new GetAllSubmodelsByIdShortRequest.Builder()
                .idShort("Test")
                .outputModifier(new OutputModifier())
                .build();
        GetAllSubmodelsByIdShortResponse response = manager.execute(request);
        GetAllSubmodelsByIdShortResponse expected = new GetAllSubmodelsByIdShortResponse.Builder()
                .payload(environment.getSubmodels())
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testPostSubmodelRequest() throws ResourceNotFoundException {
        when(persistence.put(environment.getSubmodels().get(0)))
                .thenReturn(environment.getSubmodels().get(0));
        PostSubmodelRequest request = new PostSubmodelRequest.Builder()
                .submodel(environment.getSubmodels().get(0))
                .build();
        PostSubmodelResponse response = manager.execute(request);
        PostSubmodelResponse expected = new PostSubmodelResponse.Builder()
                .payload(environment.getSubmodels().get(0))
                .statusCode(StatusCode.SuccessCreated)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testGetSubmodelByIdRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getSubmodels().get(0).getIdentification(), new OutputModifier()))
                .thenReturn(environment.getSubmodels().get(0));
        GetSubmodelByIdRequest request = new GetSubmodelByIdRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .outputModifier(new OutputModifier())
                .build();
        GetSubmodelByIdResponse response = manager.execute(request);
        GetSubmodelByIdResponse expected = new GetSubmodelByIdResponse.Builder()
                .payload(environment.getSubmodels().get(0))
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testPutSubmodelByIdRequest() throws ResourceNotFoundException {
        when(persistence.put(environment.getSubmodels().get(0)))
                .thenReturn(environment.getSubmodels().get(0));
        PutSubmodelByIdRequest request = new PutSubmodelByIdRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .submodel(environment.getSubmodels().get(0))
                .build();
        PutSubmodelByIdResponse response = manager.execute(request);
        PutSubmodelByIdResponse expected = new PutSubmodelByIdResponse.Builder()
                .payload(environment.getSubmodels().get(0))
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testDeleteSubmodelByIdRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getSubmodels().get(0).getIdentification(), new QueryModifier()))
                .thenReturn(environment.getSubmodels().get(0));
        DeleteSubmodelByIdRequest request = new DeleteSubmodelByIdRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .build();
        DeleteSubmodelByIdResponse response = manager.execute(request);
        DeleteSubmodelByIdResponse expected = new DeleteSubmodelByIdResponse.Builder()
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
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
        GetSubmodelResponse response = manager.execute(request);
        GetSubmodelResponse expected = new GetSubmodelResponse.Builder()
                .payload(environment.getSubmodels().get(0))
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
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
        PutSubmodelResponse response = manager.execute(request);
        PutSubmodelResponse expected = new PutSubmodelResponse.Builder()
                .payload(environment.getSubmodels().get(0))
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testGetAllSubmodelElementsRequest() throws ResourceNotFoundException {
        Reference reference = Util.toReference(environment.getSubmodels().get(0).getIdentification(), Submodel.class);
        when(persistence.getSubmodelElements(reference, (Reference) null, new OutputModifier()))
                .thenReturn(environment.getSubmodels().get(0).getSubmodelElements());
        GetAllSubmodelElementsRequest request = new GetAllSubmodelElementsRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .outputModifier(new OutputModifier())
                .build();
        GetAllSubmodelElementsResponse response = manager.execute(request);
        GetAllSubmodelElementsResponse expected = new GetAllSubmodelElementsResponse.Builder()
                .payload(environment.getSubmodels().get(0).getSubmodelElements())
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testPostSubmodelElementRequest() throws ResourceNotFoundException {
        Reference reference = Util.toReference(environment.getSubmodels().get(0).getIdentification(), Submodel.class);
        when(persistence.put(reference, (Reference) null, environment.getSubmodels().get(0).getSubmodelElements().get(0)))
                .thenReturn(environment.getSubmodels().get(0).getSubmodelElements().get(0));
        PostSubmodelElementRequest request = new PostSubmodelElementRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .submodelElement(environment.getSubmodels().get(0).getSubmodelElements().get(0))
                .build();
        PostSubmodelElementResponse response = manager.execute(request);
        PostSubmodelElementResponse expected = new PostSubmodelElementResponse.Builder()
                .statusCode(StatusCode.SuccessCreated)
                .payload(environment.getSubmodels().get(0).getSubmodelElements().get(0))
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testGetSubmodelElementByPathRequest() throws ResourceNotFoundException {
        when(persistence.get(Util.toReference(ElementPathUtils.extractElementPath(SUBMODEL_ELEMENT_REF)), new OutputModifier()))
                .thenReturn(environment.getSubmodels().get(0).getSubmodelElements().get(0));
        GetSubmodelElementByPathRequest request = new GetSubmodelElementByPathRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .outputModifier(new OutputModifier())
                .path(ElementPathUtils.extractElementPath(SUBMODEL_ELEMENT_REF))
                .build();
        GetSubmodelElementByPathResponse response = manager.execute(request);
        GetSubmodelElementByPathResponse expected = new GetSubmodelElementByPathResponse.Builder()
                .payload(environment.getSubmodels().get(0).getSubmodelElements().get(0))
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testPostSubmodelElementByPathRequest() throws ResourceNotFoundException {
        when(persistence.put(any(), argThat((Reference t) -> true), any()))
                .thenReturn(environment.getSubmodels().get(0).getSubmodelElements().get(0));
        PostSubmodelElementByPathRequest request = new PostSubmodelElementByPathRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .path(ElementPathUtils.extractElementPath(SUBMODEL_ELEMENT_REF))
                .build();
        PostSubmodelElementByPathResponse response = manager.execute(request);
        PostSubmodelElementByPathResponse expected = new PostSubmodelElementByPathResponse.Builder()
                .payload(environment.getSubmodels().get(0).getSubmodelElements().get(0))
                .statusCode(StatusCode.SuccessCreated)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testPutSubmodelElementByPathRequest() throws ResourceNotFoundException {
        when(persistence.put(any(), argThat((Reference t) -> true), any()))
                .thenReturn(environment.getSubmodels().get(0).getSubmodelElements().get(0));
        when(persistence.get(argThat((Reference t) -> true), any()))
                .thenReturn(environment.getSubmodels().get(0).getSubmodelElements().get(0));
        PutSubmodelElementByPathRequest request = new PutSubmodelElementByPathRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .submodelElement(environment.getSubmodels().get(0).getSubmodelElements().get(0))
                .path(ElementPathUtils.extractElementPath(SUBMODEL_ELEMENT_REF))
                .build();
        PutSubmodelElementByPathResponse response = manager.execute(request);
        PutSubmodelElementByPathResponse expected = new PutSubmodelElementByPathResponse.Builder()
                .payload(environment.getSubmodels().get(0).getSubmodelElements().get(0))
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testSetSubmodelElementValueByPathRequest() throws ResourceNotFoundException {
        when(persistence.get((Reference) any(), any()))
                .thenReturn(environment.getSubmodels().get(0).getSubmodelElements().get(0));
        SetSubmodelElementValueByPathRequest request = new SetSubmodelElementValueByPathRequest.Builder<ElementValue>()
                .id(environment.getSubmodels().get(0).getIdentification())
                .value(new PropertyValue.Builder()
                        .value(new StringValue("Test"))
                        .build())
                .valueParser(new ElementValueParser<ElementValue>() {
                    @Override
                    public <U extends ElementValue> U parse(ElementValue raw, Class<U> type) {
                        return (U) raw;
                    }
                })
                .path(ElementPathUtils.extractElementPath(SUBMODEL_ELEMENT_REF))
                .build();

        Response response = manager.execute(request);
        SetSubmodelElementValueByPathResponse expected = new SetSubmodelElementValueByPathResponse.Builder()
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testDeleteSubmodelElementByPathRequest() throws ResourceNotFoundException {
        when(persistence.get(Util.toReference(ElementPathUtils.extractElementPath(SUBMODEL_ELEMENT_REF)), new QueryModifier()))
                .thenReturn(environment.getSubmodels().get(0).getSubmodelElements().get(0));
        DeleteSubmodelElementByPathRequest request = new DeleteSubmodelElementByPathRequest.Builder()
                .id(environment.getSubmodels().get(0).getIdentification())
                .path(ElementPathUtils.extractElementPath(SUBMODEL_ELEMENT_REF))
                .build();
        DeleteSubmodelElementByPathResponse response = manager.execute(request);
        DeleteSubmodelElementByPathResponse expected = new DeleteSubmodelElementByPathResponse.Builder()
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
        //verify(persistence).remove(environment.getSubmodels().get(0).getIdentification());
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

    @Test
    public void testGetAllConceptDescriptionsRequest() throws ResourceNotFoundException {
        when(persistence.get(null, null, null, new OutputModifier()))
                .thenReturn(environment.getConceptDescriptions());
        GetAllConceptDescriptionsRequest request = new GetAllConceptDescriptionsRequest.Builder()
                .outputModifier(new OutputModifier())
                .build();
        GetAllConceptDescriptionsResponse response = manager.execute(request);
        GetAllConceptDescriptionsResponse expected = new GetAllConceptDescriptionsResponse.Builder()
                .payload(environment.getConceptDescriptions())
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testGetAllConceptDescriptionsByIdShortRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getConceptDescriptions().get(0).getIdShort(), null, null, new OutputModifier()))
                .thenReturn(environment.getConceptDescriptions());
        GetAllConceptDescriptionsByIdShortRequest request = new GetAllConceptDescriptionsByIdShortRequest.Builder()
                .outputModifier(new OutputModifier())
                .idShort(environment.getConceptDescriptions().get(0).getIdShort())
                .build();
        GetAllConceptDescriptionsByIdShortResponse response = manager.execute(request);
        GetAllConceptDescriptionsByIdShortResponse expected = new GetAllConceptDescriptionsByIdShortResponse.Builder()
                .payload(environment.getConceptDescriptions())
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testGetAllConceptDescriptionsByIsCaseOfRequest() throws ResourceNotFoundException {
        Reference reference = Util.toReference(environment.getConceptDescriptions().get(0).getIdentification(), ConceptDescription.class);
        when(persistence.get(null, reference, null, new OutputModifier()))
                .thenReturn(environment.getConceptDescriptions());
        GetAllConceptDescriptionsByIsCaseOfRequest request = new GetAllConceptDescriptionsByIsCaseOfRequest.Builder()
                .outputModifier(new OutputModifier())
                .isCaseOf(reference)
                .build();
        GetAllConceptDescriptionsByIsCaseOfResponse response = manager.execute(request);
        GetAllConceptDescriptionsByIsCaseOfResponse expected = new GetAllConceptDescriptionsByIsCaseOfResponse.Builder()
                .payload(environment.getConceptDescriptions())
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testGetAllConceptDescriptionsByDataSpecificationReferenceRequest() throws ResourceNotFoundException {
        Reference reference = Util.toReference(environment.getConceptDescriptions().get(0).getIdentification(), ConceptDescription.class);
        when(persistence.get(null, null, reference, new OutputModifier()))
                .thenReturn(environment.getConceptDescriptions());
        GetAllConceptDescriptionsByDataSpecificationReferenceRequest request = new GetAllConceptDescriptionsByDataSpecificationReferenceRequest.Builder()
                .outputModifier(new OutputModifier())
                .dataSpecification(reference)
                .build();
        GetAllConceptDescriptionsByDataSpecificationReferenceResponse response = manager.execute(request);
        GetAllConceptDescriptionsByDataSpecificationReferenceResponse expected = new GetAllConceptDescriptionsByDataSpecificationReferenceResponse.Builder()
                .payload(environment.getConceptDescriptions())
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testPostConceptDescriptionRequest() throws ResourceNotFoundException {
        when(persistence.put(environment.getConceptDescriptions().get(0)))
                .thenReturn(environment.getConceptDescriptions().get(0));
        PostConceptDescriptionRequest request = new PostConceptDescriptionRequest.Builder()
                .conceptDescription(environment.getConceptDescriptions().get(0))
                .build();
        PostConceptDescriptionResponse response = manager.execute(request);
        PostConceptDescriptionResponse expected = new PostConceptDescriptionResponse.Builder()
                .payload(environment.getConceptDescriptions().get(0))
                .statusCode(StatusCode.SuccessCreated)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testGetConceptDescriptionByIdRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getConceptDescriptions().get(0).getIdentification(), new OutputModifier()))
                .thenReturn(environment.getConceptDescriptions().get(0));
        GetConceptDescriptionByIdRequest request = new GetConceptDescriptionByIdRequest.Builder()
                .outputModifier(new OutputModifier())
                .id(environment.getConceptDescriptions().get(0).getIdentification())
                .build();
        GetConceptDescriptionByIdResponse response = manager.execute(request);
        GetConceptDescriptionByIdResponse expected = new GetConceptDescriptionByIdResponse.Builder()
                .payload(environment.getConceptDescriptions().get(0))
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testPutConceptDescriptionByIdRequest() throws ResourceNotFoundException {
        when(persistence.put(environment.getConceptDescriptions().get(0)))
                .thenReturn(environment.getConceptDescriptions().get(0));
        PutConceptDescriptionByIdRequest request = new PutConceptDescriptionByIdRequest.Builder()
                .conceptDescription(environment.getConceptDescriptions().get(0))
                .build();
        PutConceptDescriptionByIdResponse response = manager.execute(request);
        PutConceptDescriptionByIdResponse expected = new PutConceptDescriptionByIdResponse.Builder()
                .payload(environment.getConceptDescriptions().get(0))
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
    }


    @Test
    public void testDeleteConceptDescriptionByIdRequest() throws ResourceNotFoundException {
        when(persistence.get(environment.getConceptDescriptions().get(0).getIdentification(), new QueryModifier()))
                .thenReturn(environment.getConceptDescriptions().get(0));
        DeleteConceptDescriptionByIdRequest request = new DeleteConceptDescriptionByIdRequest.Builder()
                .id(environment.getConceptDescriptions().get(0).getIdentification())
                .build();
        DeleteConceptDescriptionByIdResponse response = manager.execute(request);
        DeleteConceptDescriptionByIdResponse expected = new DeleteConceptDescriptionByIdResponse.Builder()
                .statusCode(StatusCode.Success)
                .build();
        Assert.assertEquals(expected, response);
        verify(persistence).remove(environment.getConceptDescriptions().get(0).getIdentification());
    }

    //---------------------------------------------- Async?


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
}
