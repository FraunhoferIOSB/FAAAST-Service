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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AbstractAssetOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.ArgumentValidationMode;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.InMemoryFile;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.TypedInMemoryFile;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.PatchSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.DeleteSubmodelReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.DeleteThumbnailRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.GetAllSubmodelReferencesRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.GetAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.GetAssetInformationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.GetThumbnailRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.PostSubmodelReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.PutAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.PutAssetInformationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.PutThumbnailRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.DeleteAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByAssetIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.PostAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.PutAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.DeleteConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsByDataSpecificationReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsByIsCaseOfRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.PostConceptDescriptionRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.PutConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.proprietary.ImportRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.proprietary.ResetRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.DeleteSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetAllSubmodelElementsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetFileByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationAsyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PostSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PostSubmodelElementRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PutFileByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PutSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PutSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.DeleteSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.GetAllSubmodelsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.GetAllSubmodelsBySemanticIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.GetAllSubmodelsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.GetSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.PostSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.PutSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PatchSubmodelElementValueByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.DeleteSubmodelReferenceResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.DeleteThumbnailResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.GetAllSubmodelReferencesResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.GetAssetAdministrationShellResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.GetAssetInformationResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.GetThumbnailResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.PostSubmodelReferenceResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.PutAssetAdministrationShellResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.PutAssetInformationResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aas.PutThumbnailResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.DeleteAssetAdministrationShellByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.GetAllAssetAdministrationShellsByAssetIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.GetAllAssetAdministrationShellsByIdShortResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.GetAllAssetAdministrationShellsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.GetAssetAdministrationShellByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.PostAssetAdministrationShellResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.PutAssetAdministrationShellByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.DeleteConceptDescriptionByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.GetAllConceptDescriptionsByDataSpecificationReferenceResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.GetAllConceptDescriptionsByIdShortResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.GetAllConceptDescriptionsByIsCaseOfResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.GetAllConceptDescriptionsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.GetConceptDescriptionByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.PostConceptDescriptionResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.PutConceptDescriptionByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.proprietary.ImportResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.proprietary.ResetResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.DeleteSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetAllSubmodelElementsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetFileByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.InvokeOperationAsyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.InvokeOperationSyncResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PostSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PostSubmodelElementResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PutFileByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PutSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PutSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.DeleteSubmodelByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.GetAllSubmodelsByIdShortResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.GetAllSubmodelsBySemanticIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.GetAllSubmodelsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.GetSubmodelByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.PostSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.PutSubmodelByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValueParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.StringValue;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.ConceptDescriptionSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.request.RequestHandlerManager;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.submodelrepository.DeleteSubmodelByIdRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ResponseHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;
import org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultFile;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultRange;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultResource;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultResult;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


public class RequestHandlerManagerTest {

    private static final long DEFAULT_TIMEOUT = 1000;
    private static final AssetAdministrationShell AAS = AASFull.AAS_1;
    private static final Submodel SUBMODEL = AASFull.SUBMODEL_1;
    private static final SubmodelElement SUBMODEL_ELEMENT = AASFull.SUBMODEL_1.getSubmodelElements().get(0);
    private static final Reference SUBMODEL_ELEMENT_REF = ReferenceBuilder.forSubmodel(SUBMODEL, SUBMODEL_ELEMENT);

    private static final CoreConfig coreConfigWithConstraintValidation = CoreConfig.builder()
            .validateConstraints(true)
            .build();
    private static CoreConfig coreConfig;
    private static Environment environment;
    private static MessageBus messageBus;
    private static Persistence persistence;
    private static AssetConnectionManager assetConnectionManager;
    private static FileStorage fileStorage;
    private static RequestHandlerManager manager;
    private static AssetValueProvider assetValueProvider;
    private static Service service;
    private static StaticRequestExecutionContext context;

    @Before
    public void createRequestHandlerManager() throws ConfigurationException, AssetConnectionException {
        environment = AASFull.createEnvironment();
        coreConfig = CoreConfig.DEFAULT;
        messageBus = mock(MessageBus.class);
        persistence = spy(Persistence.class);
        service = mock(Service.class);
        assetConnectionManager = spy(new AssetConnectionManager(coreConfig, List.of(), service));
        fileStorage = mock(FileStorage.class);
        context = new StaticRequestExecutionContext(
                coreConfig,
                persistence,
                fileStorage,
                messageBus,
                assetConnectionManager);
        manager = new RequestHandlerManager(coreConfig);
        assetValueProvider = mock(AssetValueProvider.class);
        when(assetConnectionManager.getValueProvider(any())).thenReturn(assetValueProvider);
    }


    @Test
    public void testGetAllAssetAdministrationShellRequest() throws Exception {
        when(persistence.findAssetAdministrationShells(any(), any(), any()))
                .thenReturn(Page.of(environment.getAssetAdministrationShells()));
        GetAllAssetAdministrationShellsRequest request = new GetAllAssetAdministrationShellsRequest();
        GetAllAssetAdministrationShellsResponse actual = manager.execute(request, context);
        GetAllAssetAdministrationShellsResponse expected = new GetAllAssetAdministrationShellsResponse.Builder()
                .payload(Page.of(environment.getAssetAdministrationShells()))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllAssetAdministrationShellsByAssetIdRequest() throws Exception {
        GlobalAssetIdentification globalAssetIdentification = new GlobalAssetIdentification.Builder()
                .value("TestValue")
                .build();
        SpecificAssetIdentification specificAssetIdentification = new SpecificAssetIdentification.Builder()
                .value("TestValue")
                .key("TestKey")
                .build();

        when(persistence.findAssetAdministrationShells(eq(
                AssetAdministrationShellSearchCriteria.builder()
                        .assetIds(List.of(globalAssetIdentification, specificAssetIdentification))
                        .build()),
                any(),
                any()))
                .thenReturn(Page.of(
                        environment.getAssetAdministrationShells().get(0),
                        environment.getAssetAdministrationShells().get(1)));

        List<SpecificAssetId> assetIds = List.of(
                new DefaultSpecificAssetId.Builder()
                        .name("globalAssetId")
                        .value("TestValue")
                        .externalSubjectId(new DefaultReference.Builder().build())
                        .build(),
                new DefaultSpecificAssetId.Builder()
                        .name("TestKey")
                        .value("TestValue")
                        .build());
        GetAllAssetAdministrationShellsByAssetIdRequest request = new GetAllAssetAdministrationShellsByAssetIdRequest.Builder()
                .assetIds(assetIds)
                .build();
        GetAllAssetAdministrationShellsByAssetIdResponse actual = manager.execute(request, context);
        GetAllAssetAdministrationShellsByAssetIdResponse expected = new GetAllAssetAdministrationShellsByAssetIdResponse.Builder()
                .payload(Page.of(environment.getAssetAdministrationShells().get(0), environment.getAssetAdministrationShells().get(1)))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllAssetAdministrationShellsByIdShortRequest() throws Exception {
        when(persistence.findAssetAdministrationShells(eq(
                AssetAdministrationShellSearchCriteria.builder()
                        .idShort("Test")
                        .build()),
                any(),
                any()))
                .thenReturn(Page.of(environment.getAssetAdministrationShells()));

        GetAllAssetAdministrationShellsByIdShortRequest request = new GetAllAssetAdministrationShellsByIdShortRequest.Builder()
                .idShort("Test")
                .build();
        GetAllAssetAdministrationShellsByIdShortResponse actual = manager.execute(request, context);
        GetAllAssetAdministrationShellsByIdShortResponse expected = new GetAllAssetAdministrationShellsByIdShortResponse.Builder()
                .payload(Page.of(environment.getAssetAdministrationShells()))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPostAssetAdministrationShellRequest() throws Exception {
        PostAssetAdministrationShellRequest request = new PostAssetAdministrationShellRequest.Builder()
                .aas(environment.getAssetAdministrationShells().get(0))
                .build();
        PostAssetAdministrationShellResponse actual = manager.execute(request, context);
        PostAssetAdministrationShellResponse expected = new PostAssetAdministrationShellResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0))
                .statusCode(StatusCode.SUCCESS_CREATED)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence, times(1)).save(environment.getAssetAdministrationShells().get(0));
    }


    @Test
    @Ignore("Currently not working because AAS4j does not provide validation which is required to produce the expected error")
    public void testPostAssetAdministrationShellRequestEmptyAas() throws Exception {
        PostAssetAdministrationShellResponse actual = manager.execute(
                new PostAssetAdministrationShellRequest.Builder()
                        .aas(new DefaultAssetAdministrationShell.Builder()
                                .build())
                        .build(),
                new StaticRequestExecutionContext(coreConfigWithConstraintValidation, persistence, fileStorage, messageBus, assetConnectionManager));
        Assert.assertEquals(StatusCode.CLIENT_ERROR_BAD_REQUEST, actual.getStatusCode());
    }


    @Test
    public void testGetAssetAdministrationShellByIdRequest() throws ResourceNotFoundException, Exception {
        when(persistence.getAssetAdministrationShell(eq(environment.getAssetAdministrationShells().get(0).getId()), any()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        GetAssetAdministrationShellByIdRequest request = GetAssetAdministrationShellByIdRequest.builder()
                .id(environment.getAssetAdministrationShells().get(0).getId())
                .outputModifier(OutputModifier.DEFAULT)
                .build();
        GetAssetAdministrationShellByIdResponse actual = manager.execute(request, context);
        GetAssetAdministrationShellByIdResponse expected = new GetAssetAdministrationShellByIdResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPutAssetAdministrationShellByIdRequest() throws Exception {
        PutAssetAdministrationShellByIdRequest request = new PutAssetAdministrationShellByIdRequest.Builder()
                .aas(environment.getAssetAdministrationShells().get(0))
                .build();
        PutAssetAdministrationShellByIdResponse actual = manager.execute(request, context);
        PutAssetAdministrationShellByIdResponse expected = new PutAssetAdministrationShellByIdResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence, times(1)).save(environment.getAssetAdministrationShells().get(0));
    }


    @Test
    public void testDeleteAssetAdministrationShellByIdRequest() throws ResourceNotFoundException, Exception {
        when(persistence.getAssetAdministrationShell(eq(environment.getAssetAdministrationShells().get(0).getId()), any()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        DeleteAssetAdministrationShellByIdRequest request = DeleteAssetAdministrationShellByIdRequest.builder()
                .id(environment.getAssetAdministrationShells().get(0).getId())
                .build();
        DeleteAssetAdministrationShellByIdResponse actual = manager.execute(request, context);
        DeleteAssetAdministrationShellByIdResponse expected = DeleteAssetAdministrationShellByIdResponse.builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).deleteAssetAdministrationShell(environment.getAssetAdministrationShells().get(0).getId());
    }


    @Test
    public void testGetAssetAdministrationShellRequest() throws ResourceNotFoundException, Exception {
        when(persistence.getAssetAdministrationShell(eq(environment.getAssetAdministrationShells().get(0).getId()), any()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        GetAssetAdministrationShellRequest request = new GetAssetAdministrationShellRequest.Builder()
                .id(AAS.getId())
                .build();
        GetAssetAdministrationShellResponse actual = manager.execute(request, context);
        GetAssetAdministrationShellResponse expected = new GetAssetAdministrationShellResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPutAssetAdministrationShellRequest() throws ResourceNotFoundException, Exception {
        // @TODO: open/unclear
        // expected Identifiable
        PutAssetAdministrationShellRequest request = new PutAssetAdministrationShellRequest.Builder()
                .aas(environment.getAssetAdministrationShells().get(0))
                .id(AAS.getId())
                .build();
        PutAssetAdministrationShellResponse actual = manager.execute(request, context);
        PutAssetAdministrationShellResponse expected = new PutAssetAdministrationShellResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).save(environment.getAssetAdministrationShells().get(0));
    }


    @Test
    @Ignore("Currently not working because AAS4j does not provide validation which is required to produce the expected error")
    public void testPutAssetAdministrationShellRequestEmptyAas() throws ResourceNotFoundException, Exception {
        PutAssetAdministrationShellResponse actual = manager.execute(
                new PutAssetAdministrationShellRequest.Builder()
                        .aas(new DefaultAssetAdministrationShell.Builder().build())
                        .build(),
                new StaticRequestExecutionContext(coreConfigWithConstraintValidation, persistence, fileStorage, messageBus, assetConnectionManager));
        Assert.assertEquals(StatusCode.CLIENT_ERROR_BAD_REQUEST, actual.getStatusCode());
    }


    @Test
    public void testGetAssetInformationRequest() throws ResourceNotFoundException, Exception {
        when(persistence.getAssetAdministrationShell(eq(environment.getAssetAdministrationShells().get(0).getId()), any()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        GetAssetInformationRequest request = new GetAssetInformationRequest.Builder()
                .id(environment.getAssetAdministrationShells().get(0).getId())
                .build();
        GetAssetInformationResponse actual = manager.execute(request, context);
        GetAssetInformationResponse expected = new GetAssetInformationResponse.Builder()
                .payload(environment.getAssetAdministrationShells().get(0).getAssetInformation())
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetThumbnailRequest() throws ResourceNotFoundException, Exception {
        TypedInMemoryFile file = new TypedInMemoryFile.Builder()
                .path("my/path/image.png")
                .content("foo".getBytes())
                .build();
        String aasId = "aasid";
        when(persistence.getAssetAdministrationShell(eq(aasId), any()))
                .thenReturn(new DefaultAssetAdministrationShell.Builder()
                        .id(aasId)
                        .assetInformation(new DefaultAssetInformation.Builder()
                                .defaultThumbnail(new DefaultResource.Builder()
                                        .path(file.getPath())
                                        .build())
                                .build())
                        .build());
        when(fileStorage.get(file.getPath())).thenReturn(
                file.getContent());
        GetThumbnailRequest request = new GetThumbnailRequest.Builder()
                .id(aasId)
                .build();
        GetThumbnailResponse actual = manager.execute(request, context);
        GetThumbnailResponse expected = new GetThumbnailResponse.Builder()
                .payload(file)
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testDeleteThumbnailRequest() throws ResourceNotFoundException, Exception {
        InMemoryFile file = InMemoryFile.builder()
                .path("my/path/image.png")
                .content("foo".getBytes())
                .build();
        String aasId = "aasid";
        when(persistence.getAssetAdministrationShell(eq(aasId), any()))
                .thenReturn(new DefaultAssetAdministrationShell.Builder()
                        .id(aasId)
                        .assetInformation(new DefaultAssetInformation.Builder()
                                .defaultThumbnail(new DefaultResource.Builder()
                                        .path(file.getPath())
                                        .build())
                                .build())
                        .build());
        when(fileStorage.get(file.getPath())).thenReturn(
                file.getContent());
        PutThumbnailRequest putThumbnailRequestRequest = new PutThumbnailRequest.Builder()
                .id(aasId)
                .content(new TypedInMemoryFile.Builder().path(file.getPath()).content(file.getContent()).contentType("image/png").build())
                .build();
        GetThumbnailRequest request = new GetThumbnailRequest.Builder()
                .id(aasId)
                .build();
        PutThumbnailResponse send = manager.execute(putThumbnailRequestRequest, context);
        Assert.assertTrue(send.getResult().getMessages().isEmpty());
        GetThumbnailResponse actual = manager.execute(request, context);
        GetThumbnailResponse expected = new GetThumbnailResponse.Builder()
                .payload(new TypedInMemoryFile.Builder()
                        .content(file.getContent())
                        .contentType("image/png")
                        .path(file.getPath())
                        .build())
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        DeleteThumbnailRequest deleteThumbnailRequest = new DeleteThumbnailRequest.Builder()
                .id(aasId)
                .build();
        DeleteThumbnailResponse deleted = manager.execute(deleteThumbnailRequest, context);
        Assert.assertTrue(deleted.getResult().getMessages().isEmpty());
        GetThumbnailResponse fail = manager.execute(request, context);
        Assert.assertFalse(fail.getResult().getMessages().isEmpty());
    }


    @Test
    public void testPutFileRequest() throws ResourceNotFoundException, Exception {
        TypedInMemoryFile expectedFile = new TypedInMemoryFile.Builder()
                .path("file:///TestFile.pdf")
                .content("foo".getBytes())
                .contentType("application/pdf")
                .build();
        SubmodelElement file = new DefaultFile.Builder()
                .idShort("ExampleFile")
                .value("file://TestFile.pdf")
                .build();
        when(persistence.getSubmodelElement((SubmodelElementIdentifier) any(), any()))
                .thenReturn(file);
        PutFileByPathRequest putFileByPathRequest = new PutFileByPathRequest.Builder()
                .submodelId(environment.getSubmodels().get(0).getId())
                .path(file.getIdShort())
                .content(expectedFile)
                .build();
        PutFileByPathResponse putFileByPathResponse = manager.execute(putFileByPathRequest, context);
        PutFileByPathResponse putFileByPathResponseExpected = PutFileByPathResponse.builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertEquals(putFileByPathResponseExpected, putFileByPathResponse);
        Assert.assertTrue(putFileByPathResponse.getResult().getMessages().isEmpty());
        GetFileByPathRequest request = new GetFileByPathRequest.Builder()
                .submodelId(environment.getSubmodels().get(0).getId())
                .path(file.getIdShort())
                .build();
        when(fileStorage.get(expectedFile.getPath()))
                .thenReturn(expectedFile.getContent());
        GetFileByPathResponse actual = manager.execute(request, context);
        GetFileByPathResponse expected = new GetFileByPathResponse.Builder()
                .payload(expectedFile)
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPutAssetInformationRequest() throws ResourceNotFoundException, Exception {
        when(persistence.getAssetAdministrationShell(eq(environment.getAssetAdministrationShells().get(0).getId()), any()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        PutAssetInformationRequest request = new PutAssetInformationRequest.Builder()
                .id(environment.getAssetAdministrationShells().get(0).getId())
                .assetInformation(environment.getAssetAdministrationShells().get(0).getAssetInformation())
                .build();
        PutAssetInformationResponse actual = manager.execute(request, context);
        PutAssetInformationResponse expected = new PutAssetInformationResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).save(environment.getAssetAdministrationShells().get(0));
    }


    @Test
    public void testGetAllSubmodelReferencesRequest() throws ResourceNotFoundException, Exception {
        when(persistence.getSubmodelRefs(eq(environment.getAssetAdministrationShells().get(0).getId()), any()))
                .thenReturn(Page.of(environment.getAssetAdministrationShells().get(0).getSubmodels()));
        when(persistence.getAssetAdministrationShell(eq(environment.getAssetAdministrationShells().get(0).getId()), any()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        GetAllSubmodelReferencesRequest request = new GetAllSubmodelReferencesRequest.Builder()
                .id(environment.getAssetAdministrationShells().get(0).getId())
                .build();
        GetAllSubmodelReferencesResponse actual = manager.execute(request, context);
        GetAllSubmodelReferencesResponse expected = new GetAllSubmodelReferencesResponse.Builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(Page.of(environment.getAssetAdministrationShells().get(0).getSubmodels()))
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPostSubmodelReferenceRequest() throws ResourceNotFoundException, Exception {
        when(persistence.getAssetAdministrationShell(eq(environment.getAssetAdministrationShells().get(0).getId()), any()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));
        PostSubmodelReferenceRequest request = new PostSubmodelReferenceRequest.Builder()
                .id(environment.getAssetAdministrationShells().get(0).getId())
                .submodelRef(SUBMODEL_ELEMENT_REF)
                .build();
        PostSubmodelReferenceResponse actual = manager.execute(request, context);
        PostSubmodelReferenceResponse expected = new PostSubmodelReferenceResponse.Builder()
                .statusCode(StatusCode.SUCCESS_CREATED)
                .payload(SUBMODEL_ELEMENT_REF)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).save(environment.getAssetAdministrationShells().get(0));
    }


    @Test
    public void testDeleteSubmodelReferenceRequest() throws ResourceNotFoundException, Exception {
        when(persistence.getAssetAdministrationShell(eq(environment.getAssetAdministrationShells().get(0).getId()), any()))
                .thenReturn(environment.getAssetAdministrationShells().get(0));

        DeleteSubmodelReferenceRequest request = new DeleteSubmodelReferenceRequest.Builder()
                .id(environment.getAssetAdministrationShells().get(0).getId())
                .submodelRef(environment.getAssetAdministrationShells().get(0).getSubmodels().get(0))
                .build();
        DeleteSubmodelReferenceResponse actual = manager.execute(request, context);
        DeleteSubmodelReferenceResponse expected = new DeleteSubmodelReferenceResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllSubmodelsRequest() throws ResourceNotFoundException, Exception {
        when(persistence.findSubmodels(
                eq(SubmodelSearchCriteria.NONE),
                any(),
                any()))
                .thenReturn(Page.of(environment.getSubmodels()));

        GetAllSubmodelsRequest request = new GetAllSubmodelsRequest.Builder()
                .outputModifier(OutputModifier.DEFAULT)
                .build();
        GetAllSubmodelsResponse actual = manager.execute(request, context);
        GetAllSubmodelsResponse expected = new GetAllSubmodelsResponse.Builder()
                .payload(Page.of(environment.getSubmodels()))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllSubmodelsBySemanticIdRequest() throws ResourceNotFoundException, Exception {
        when(persistence.findSubmodels(
                eq(SubmodelSearchCriteria.builder()
                        .semanticId(SUBMODEL_ELEMENT_REF)
                        .build()),
                any(),
                any()))
                .thenReturn(Page.of(environment.getSubmodels()));
        GetAllSubmodelsBySemanticIdRequest request = new GetAllSubmodelsBySemanticIdRequest.Builder()
                .semanticId(SUBMODEL_ELEMENT_REF)
                .outputModifier(OutputModifier.DEFAULT)
                .build();
        GetAllSubmodelsBySemanticIdResponse actual = manager.execute(request, context);
        GetAllSubmodelsBySemanticIdResponse expected = new GetAllSubmodelsBySemanticIdResponse.Builder()
                .payload(Page.of(environment.getSubmodels()))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllSubmodelsByIdShortRequest() throws ResourceNotFoundException, Exception {
        when(persistence.findSubmodels(
                eq(SubmodelSearchCriteria.builder()
                        .idShort("Test")
                        .build()),
                any(),
                any()))
                .thenReturn(Page.of(environment.getSubmodels()));
        GetAllSubmodelsByIdShortRequest request = new GetAllSubmodelsByIdShortRequest.Builder()
                .idShort("Test")
                .outputModifier(OutputModifier.DEFAULT)
                .build();
        GetAllSubmodelsByIdShortResponse actual = manager.execute(request, context);
        GetAllSubmodelsByIdShortResponse expected = new GetAllSubmodelsByIdShortResponse.Builder()
                .payload(Page.of(environment.getSubmodels()))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPostSubmodelRequest() throws ResourceNotFoundException, Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        PostSubmodelRequest request = new PostSubmodelRequest.Builder()
                .submodel(submodel)
                .build();
        PostSubmodelResponse actual = manager.execute(request, context);
        PostSubmodelResponse expected = new PostSubmodelResponse.Builder()
                .payload(submodel)
                .statusCode(StatusCode.SUCCESS_CREATED)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).save(submodel);
    }


    @Test
    public void testPostSubmodelRequestAlreadyExists() throws ResourceNotFoundException, Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        PostSubmodelRequest request = new PostSubmodelRequest.Builder()
                .submodel(submodel)
                .build();
        when(persistence.submodelExists(submodel.getId()))
                .thenReturn(true);
        PostSubmodelResponse actual = manager.execute(request, context);
        Assert.assertEquals(StatusCode.CLIENT_RESOURCE_CONFLICT, actual.getStatusCode());
        verify(persistence, times(0)).save((Submodel) any());
    }


    public void testPostSubmodelRequestDuplicateIdShort() throws ResourceNotFoundException, Exception {
        PostSubmodelResponse actual = manager.execute(
                new PostSubmodelRequest.Builder()
                        .submodel(new DefaultSubmodel.Builder()
                                .submodelElements(new DefaultProperty.Builder()
                                        .idShort("foo")
                                        .build())
                                .submodelElements(new DefaultProperty.Builder()
                                        .idShort("foo")
                                        .build())
                                .build())
                        .build(),
                context);
        Assert.assertEquals(StatusCode.CLIENT_ERROR_BAD_REQUEST, actual.getStatusCode());
    }


    @Test
    public void testGetSubmodelByIdRequest() throws ResourceNotFoundException, Exception {
        when(persistence.getSubmodel(eq(environment.getSubmodels().get(0).getId()), any()))
                .thenReturn(environment.getSubmodels().get(0));
        GetSubmodelByIdRequest request = new GetSubmodelByIdRequest.Builder()
                .id(environment.getSubmodels().get(0).getId())
                .outputModifier(OutputModifier.DEFAULT)
                .build();
        GetSubmodelByIdResponse actual = manager.execute(request, context);
        GetSubmodelByIdResponse expected = new GetSubmodelByIdResponse.Builder()
                .payload(environment.getSubmodels().get(0))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPutSubmodelByIdRequest() throws ResourceNotFoundException, Exception {
        PutSubmodelByIdRequest request = new PutSubmodelByIdRequest.Builder()
                .id(environment.getSubmodels().get(0).getId())
                .submodel(environment.getSubmodels().get(0))
                .build();
        PutSubmodelByIdResponse actual = manager.execute(request, context);
        PutSubmodelByIdResponse expected = new PutSubmodelByIdResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).save(environment.getSubmodels().get(0));
    }


    @Test
    public void testDeleteSubmodelByIdRequest() throws ResourceNotFoundException, Exception {
        when(persistence.getSubmodel(eq(environment.getSubmodels().get(0).getId()), any()))
                .thenReturn(environment.getSubmodels().get(0));
        DeleteSubmodelByIdRequest request = new DeleteSubmodelByIdRequest.Builder()
                .submodelId(environment.getSubmodels().get(0).getId())
                .build();
        DeleteSubmodelByIdResponse actual = manager.execute(request, context);
        DeleteSubmodelByIdResponse expected = new DeleteSubmodelByIdResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).deleteSubmodel(environment.getSubmodels().get(0).getId());
    }


    @Test
    public void testGetSubmodelRequest() throws ResourceNotFoundException, Exception {
        when(persistence.getSubmodel(eq(environment.getSubmodels().get(0).getId()), any()))
                .thenReturn(environment.getSubmodels().get(0));
        GetSubmodelRequest request = new GetSubmodelRequest.Builder()
                .submodelId(environment.getSubmodels().get(0).getId())
                .outputModifier(OutputModifier.DEFAULT)
                .build();
        GetSubmodelResponse actual = manager.execute(request, context);
        GetSubmodelResponse expected = new GetSubmodelResponse.Builder()
                .payload(environment.getSubmodels().get(0))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPutSubmodelRequest() throws ResourceNotFoundException, Exception {
        PutSubmodelRequest request = new PutSubmodelRequest.Builder()
                .submodelId(environment.getSubmodels().get(0).getId())
                .submodel(environment.getSubmodels().get(0))
                .outputModifier(OutputModifier.DEFAULT)
                .submodel(environment.getSubmodels().get(0))
                .build();
        PutSubmodelResponse actual = manager.execute(request, context);
        PutSubmodelResponse expected = new PutSubmodelResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).save(environment.getSubmodels().get(0));
    }


    @Test
    public void testGetAllSubmodelElementsRequest() throws ResourceNotFoundException, Exception {
        Reference reference = ReferenceBuilder.forSubmodel(environment.getSubmodels().get(0));
        when(persistence.getSubmodelElements(eq(SubmodelElementIdentifier.fromReference(reference)), any(), any()))
                .thenReturn(Page.of(environment.getSubmodels().get(0).getSubmodelElements()));
        GetAllSubmodelElementsRequest request = new GetAllSubmodelElementsRequest.Builder()
                .submodelId(environment.getSubmodels().get(0).getId())
                .outputModifier(OutputModifier.DEFAULT)
                .build();
        GetAllSubmodelElementsResponse actual = manager.execute(request, context);
        GetAllSubmodelElementsResponse expected = new GetAllSubmodelElementsResponse.Builder()
                .payload(Page.of(environment.getSubmodels().get(0).getSubmodelElements()))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPostSubmodelElementRequest() throws ResourceNotFoundException, Exception {
        Reference reference = ReferenceBuilder.forSubmodel(environment.getSubmodels().get(0));
        PostSubmodelElementRequest request = new PostSubmodelElementRequest.Builder()
                .submodelId(environment.getSubmodels().get(0).getId())
                .submodelElement(environment.getSubmodels().get(0).getSubmodelElements().get(0))
                .build();
        PostSubmodelElementResponse actual = manager.execute(request, context);
        PostSubmodelElementResponse expected = new PostSubmodelElementResponse.Builder()
                .statusCode(StatusCode.SUCCESS_CREATED)
                .payload(environment.getSubmodels().get(0).getSubmodelElements().get(0))
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).insert(SubmodelElementIdentifier.fromReference(reference), environment.getSubmodels().get(0).getSubmodelElements().get(0));
    }


    @Test
    public void testPostSubmodelElementRequestAlreadyExists() throws ResourceNotFoundException, Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        SubmodelElement submodelElement = environment.getSubmodels().get(0).getSubmodelElements().get(0);
        PostSubmodelElementRequest request = new PostSubmodelElementRequest.Builder()
                .submodelId(submodel.getId())
                .submodelElement(submodelElement)
                .build();
        Reference referenceToNewElement = new ReferenceBuilder()
                .submodel(submodel)
                .element(submodelElement.getIdShort())
                .build();
        when(persistence.submodelElementExists(referenceToNewElement))
                .thenReturn(true);
        PostSubmodelElementResponse actual = manager.execute(request, context);
        Assert.assertEquals(StatusCode.CLIENT_RESOURCE_CONFLICT, actual.getStatusCode());
    }


    @Test
    public void testGetSubmodelElementByPathRequest() throws ResourceNotFoundException, AssetConnectionException, Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        SubmodelElement cur_submodelElement = new DefaultProperty.Builder()
                .idShort("testIdShort")
                .value("testValue")
                .build();
        PropertyValue propertyValue = new PropertyValue.Builder().value(new StringValue("test")).build();
        when(persistence.getSubmodelElement((SubmodelElementIdentifier) any(), eq(OutputModifier.DEFAULT)))
                .thenReturn(cur_submodelElement);
        when(assetConnectionManager.hasValueProvider(any())).thenReturn(true);
        when(assetValueProvider.getValue()).thenReturn(propertyValue);

        GetSubmodelElementByPathRequest request = new GetSubmodelElementByPathRequest.Builder()
                .submodelId(submodel.getId())
                .outputModifier(OutputModifier.DEFAULT)
                .path(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .build();
        GetSubmodelElementByPathResponse actual = manager.execute(request, context);

        SubmodelElement expected_submodelElement = new DefaultProperty.Builder()
                .idShort("testIdShort")
                .value("test")
                .valueType(DataTypeDefXsd.STRING)
                .build();
        GetSubmodelElementByPathResponse expected = new GetSubmodelElementByPathResponse.Builder()
                .payload(expected_submodelElement)
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPostSubmodelElementByPathRequest() throws ResourceNotFoundException, Exception {
        Property property1 = new DefaultProperty.Builder()
                .valueType(DataTypeDefXsd.STRING)
                .value("first")
                .build();
        SubmodelElementList list = new DefaultSubmodelElementList.Builder()
                .idShort("list")
                .value(property1)
                .build();
        Submodel submodel = new DefaultSubmodel.Builder()
                .id("submodel")
                .submodelElements(list)
                .build();
        IdShortPath listPath = IdShortPath.builder()
                .path(list.getIdShort())
                .build();
        SubmodelElementIdentifier listIdentifier = SubmodelElementIdentifier.builder()
                .submodelId(submodel.getId())
                .idShortPath(listPath)
                .build();

        when(persistence.getSubmodelElement(eq(listIdentifier), any()))
                .thenReturn(list);
        Reference refNewElement = new ReferenceBuilder()
                .submodel(submodel)
                .element(list)
                .index(1)
                .build();
        when(persistence.submodelElementExists(refNewElement))
                .thenReturn(false);
        Property newProperty = new DefaultProperty.Builder()
                .valueType(DataTypeDefXsd.STRING)
                .value("new")
                .build();
        PostSubmodelElementByPathRequest request = new PostSubmodelElementByPathRequest.Builder()
                .submodelId(submodel.getId())
                .submodelElement(newProperty)
                .path(listPath.toString())
                .build();
        PostSubmodelElementByPathResponse actual = manager.execute(request, context);
        PostSubmodelElementByPathResponse expected = new PostSubmodelElementByPathResponse.Builder()
                .payload(newProperty)
                .statusCode(StatusCode.SUCCESS_CREATED)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).insert(listIdentifier, newProperty);
    }


    @Test
    public void testPostSubmodelElementByPathRequestAlreadyExists() throws ResourceNotFoundException, Exception {
        Property property1 = new DefaultProperty.Builder()
                .valueType(DataTypeDefXsd.STRING)
                .value("first")
                .build();
        SubmodelElementList list = new DefaultSubmodelElementList.Builder()
                .idShort("list")
                .value(property1)
                .build();
        Submodel submodel = new DefaultSubmodel.Builder()
                .id("submodel")
                .submodelElements(list)
                .build();
        IdShortPath listPath = IdShortPath.builder()
                .path(list.getIdShort())
                .build();
        when(persistence.getSubmodelElement((SubmodelElementIdentifier) any(), eq(QueryModifier.DEFAULT)))
                .thenReturn(list);
        Property newProperty = new DefaultProperty.Builder()
                .valueType(DataTypeDefXsd.STRING)
                .value("new")
                .build();

        Reference referenceToNewElement = new ReferenceBuilder()
                .submodel(submodel)
                .elements(listPath.getElements())
                .element(newProperty)
                .build();
        when(persistence.submodelElementExists(referenceToNewElement))
                .thenReturn(true);
        PostSubmodelElementByPathRequest request = new PostSubmodelElementByPathRequest.Builder()
                .submodelId(submodel.getId())
                .submodelElement(newProperty)
                .path(listPath.toString())
                .build();
        PostSubmodelElementByPathResponse actual = manager.execute(request, context);
        Assert.assertEquals(StatusCode.CLIENT_RESOURCE_CONFLICT, actual.getStatusCode());
    }


    @Test
    public void testPutSubmodelElementByPathRequest() throws ResourceNotFoundException, AssetConnectionException, ValueMappingException, Exception {
        SubmodelElement currentSubmodelElement = new DefaultProperty.Builder()
                .idShort("TestIdshort")
                .valueType(DataTypeDefXsd.STRING)
                .value("TestValue")
                .build();
        SubmodelElement newSubmodelElement = new DefaultProperty.Builder()
                .idShort("TestIdshort")
                .valueType(DataTypeDefXsd.STRING)
                .value("NewTestValue")
                .build();
        when(persistence.getSubmodelElement((SubmodelElementIdentifier) any(), any()))
                .thenReturn(currentSubmodelElement);
        when(assetConnectionManager.hasValueProvider(any())).thenReturn(true);

        PutSubmodelElementByPathRequest request = new PutSubmodelElementByPathRequest.Builder()
                .submodelId(environment.getSubmodels().get(0).getId())
                .path(currentSubmodelElement.getIdShort())
                .submodelElement(newSubmodelElement)
                .build();
        PutSubmodelElementByPathResponse actual = manager.execute(request, context);
        PutSubmodelElementByPathResponse expected = new PutSubmodelElementByPathResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(assetValueProvider).setValue(ElementValueMapper.toValue(newSubmodelElement, DataElementValue.class));
        verify(persistence).update(ReferenceBuilder.forSubmodel(request.getSubmodelId(), request.getSubmodelElement().getIdShort()), newSubmodelElement);
    }


    @Test
    public void testPatchSubmodelElementValueByPathRequest() throws ResourceNotFoundException, AssetConnectionException, Exception {
        when(persistence.getSubmodelElement((SubmodelElementIdentifier) any(), any()))
                .thenReturn(environment.getSubmodels().get(0).getSubmodelElements().get(0));
        when(assetConnectionManager.hasValueProvider(any())).thenReturn(true);
        PropertyValue propertyValue = new PropertyValue.Builder()
                .value(new StringValue("Test"))
                .build();
        PatchSubmodelElementValueByPathRequest request = new PatchSubmodelElementValueByPathRequest.Builder<ElementValue>()
                .submodelId(environment.getSubmodels().get(0).getId())
                .value(propertyValue)
                .valueParser(new ElementValueParser<ElementValue>() {
                    @Override
                    public <U extends ElementValue> U parse(ElementValue raw, Class<U> type) {
                        return (U) raw;
                    }
                })
                .path(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .build();

        Response actual = manager.execute(request, context);
        PatchSubmodelElementValueByPathResponse expected = new PatchSubmodelElementValueByPathResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(assetValueProvider).setValue(propertyValue);
    }


    @Test
    public void testDeleteSubmodelElementByPathRequest() throws ResourceNotFoundException, Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        Reference reference = new ReferenceBuilder()
                .submodel(submodel)
                .idShortPath(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .build();
        when(persistence.getSubmodelElement(reference, QueryModifier.DEFAULT))
                .thenReturn(environment.getSubmodels().get(0).getSubmodelElements().get(0));
        DeleteSubmodelElementByPathRequest request = new DeleteSubmodelElementByPathRequest.Builder()
                .submodelId(submodel.getId())
                .path(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .build();
        DeleteSubmodelElementByPathResponse actual = manager.execute(request, context);
        DeleteSubmodelElementByPathResponse expected = new DeleteSubmodelElementByPathResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).deleteSubmodelElement(SubmodelElementIdentifier.fromReference(reference));
    }


    private Operation getTestOperation() {
        return new DefaultOperation.Builder()
                .category("Test")
                .idShort("TestOperation")
                .inoutputVariables(new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort("TestProp")
                                .value("TestValue")
                                .build())
                        .build())
                .outputVariables(new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort("TestPropOutput")
                                .value("TestValue")
                                .build())
                        .build())
                .inputVariables(new DefaultOperationVariable.Builder()
                        .value(new DefaultProperty.Builder()
                                .idShort("TestPropInput")
                                .value("TestValue")
                                .build())
                        .build())
                .build();
    }


    @Test
    public void testInvokeOperationAsyncRequest() throws Exception {
        String submodelId = "http://example.org";
        CoreConfig coreConfig = CoreConfig.builder().build();
        Persistence persistence = mock(Persistence.class);
        MessageBus messageBus = mock(MessageBus.class);
        AssetConnectionManager assetConnectionManager = mock(AssetConnectionManager.class);
        AssetOperationProvider assetOperationProvider = mock(AssetOperationProvider.class);
        FileStorage fileStorage = mock(FileStorage.class);
        StaticRequestExecutionContext context = new StaticRequestExecutionContext(coreConfig, persistence, fileStorage, messageBus, assetConnectionManager);
        Operation operation = getTestOperation();

        when(persistence.getOperationResult(any())).thenReturn(new DefaultOperationResult.Builder().build());
        when(assetConnectionManager.hasOperationProvider(any())).thenReturn(true);
        when(assetConnectionManager.getOperationProvider(any())).thenReturn(assetOperationProvider);

        when(persistence.getSubmodelElement(ReferenceBuilder.forSubmodel(submodelId, operation.getIdShort()), QueryModifier.MINIMAL, Operation.class))
                .thenReturn(operation);
        when(assetOperationProvider.getConfig()).thenReturn(new AbstractAssetOperationProviderConfig() {});

        InvokeOperationAsyncRequest invokeOperationAsyncRequest = new InvokeOperationAsyncRequest.Builder()
                .submodelId(submodelId)
                .path(operation.getIdShort())
                .inoutputArguments(operation.getInoutputVariables())
                .inputArguments(operation.getInputVariables())
                .build();

        InvokeOperationAsyncResponse response = manager.execute(invokeOperationAsyncRequest, context);
        OperationHandle handle = response.getPayload();
        verify(persistence).save(
                eq(handle),
                any());
    }


    @Test
    public void testInvokeOperationSyncRequest() throws Exception {
        String submodelId = "http://example.org";
        CoreConfig coreConfig = CoreConfig.builder().build();
        Persistence persistence = mock(Persistence.class);
        MessageBus messageBus = mock(MessageBus.class);
        AssetConnectionManager assetConnectionManager = mock(AssetConnectionManager.class);
        FileStorage fileStorage = mock(FileStorage.class);
        StaticRequestExecutionContext context = new StaticRequestExecutionContext(coreConfig, persistence, fileStorage, messageBus, assetConnectionManager);
        when(assetConnectionManager.hasOperationProvider(any())).thenReturn(true);
        when(assetConnectionManager.getOperationProvider(any())).thenAnswer(x -> new CustomAssetOperationProvider());

        Operation operation = getTestOperation();

        when(persistence.getSubmodelElement(ReferenceBuilder.forSubmodel(submodelId, operation.getIdShort()), QueryModifier.MINIMAL, Operation.class))
                .thenReturn(operation);

        InvokeOperationSyncRequest invokeOperationSyncRequest = new InvokeOperationSyncRequest.Builder()
                .inoutputArguments(operation.getInoutputVariables())
                .inputArguments(operation.getInputVariables())
                .submodelId(submodelId)
                .path(operation.getIdShort())
                .build();

        InvokeOperationSyncResponse actual = manager.execute(invokeOperationSyncRequest, context);
        InvokeOperationSyncResponse expected = new InvokeOperationSyncResponse.Builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(new DefaultOperationResult.Builder()
                        .inoutputArguments(List.of(new DefaultOperationVariable.Builder()
                                .value(new DefaultProperty.Builder()
                                        .idShort("TestProp")
                                        .value("TestOutput")
                                        .build())
                                .build()))
                        .outputArguments(operation.getOutputVariables())
                        .executionState(ExecutionState.COMPLETED)
                        .success(true)
                        .build())
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test(expected = InvalidRequestException.class)
    public void testInvokeOperationSyncRequestMissingInputArgument() throws Exception {
        String submodelId = "http://example.org";
        CoreConfig coreConfig = CoreConfig.builder().build();
        Persistence persistence = mock(Persistence.class);
        MessageBus messageBus = mock(MessageBus.class);
        AssetConnectionManager assetConnectionManager = mock(AssetConnectionManager.class);
        FileStorage fileStorage = mock(FileStorage.class);
        StaticRequestExecutionContext context = new StaticRequestExecutionContext(coreConfig, persistence, fileStorage, messageBus, assetConnectionManager);
        when(assetConnectionManager.hasOperationProvider(any())).thenReturn(true);
        when(assetConnectionManager.getOperationProvider(any())).thenAnswer(x -> new CustomAssetOperationProvider(
                CustomAssetOperationProviderConfig.builder()
                        .inputValidationMode(ArgumentValidationMode.REQUIRE_PRESENT)
                        .build()));

        Operation operation = getTestOperation();

        when(persistence.getSubmodelElement(ReferenceBuilder.forSubmodel(submodelId, operation.getIdShort()), QueryModifier.MINIMAL, Operation.class))
                .thenReturn(operation);

        InvokeOperationSyncRequest invokeOperationSyncRequest = new InvokeOperationSyncRequest.Builder()
                .inoutputArguments(operation.getInoutputVariables())
                .inputArguments(List.of())
                .submodelId(submodelId)
                .path(operation.getIdShort())
                .build();

        manager.execute(invokeOperationSyncRequest, context);
    }


    @Test
    public void testInvokeOperationSyncRequestWithDefaultInputArgument() throws Exception {
        String submodelId = "http://example.org";
        CoreConfig coreConfig = CoreConfig.builder().build();
        Persistence persistence = mock(Persistence.class);
        MessageBus messageBus = mock(MessageBus.class);
        AssetConnectionManager assetConnectionManager = mock(AssetConnectionManager.class);
        FileStorage fileStorage = mock(FileStorage.class);
        StaticRequestExecutionContext context = new StaticRequestExecutionContext(coreConfig, persistence, fileStorage, messageBus, assetConnectionManager);
        when(assetConnectionManager.hasOperationProvider(any())).thenReturn(true);
        when(assetConnectionManager.getOperationProvider(any())).thenAnswer(x -> new CustomAssetOperationProvider(
                CustomAssetOperationProviderConfig.builder()
                        .inputValidationMode(ArgumentValidationMode.REQUIRE_PRESENT_OR_DEFAULT)
                        .build()));
        Operation operation = getTestOperation();

        when(persistence.getSubmodelElement(ReferenceBuilder.forSubmodel(submodelId, operation.getIdShort()), QueryModifier.MINIMAL, Operation.class))
                .thenReturn(operation);

        InvokeOperationSyncRequest invokeOperationSyncRequest = new InvokeOperationSyncRequest.Builder()
                .inoutputArguments(operation.getInoutputVariables())
                .inputArguments(List.of())
                .submodelId(submodelId)
                .path(operation.getIdShort())
                .build();

        InvokeOperationSyncResponse actual = manager.execute(invokeOperationSyncRequest, context);
        InvokeOperationSyncResponse expected = new InvokeOperationSyncResponse.Builder()
                .statusCode(StatusCode.SUCCESS)
                .payload(new DefaultOperationResult.Builder()
                        .inoutputArguments(List.of(new DefaultOperationVariable.Builder()
                                .value(new DefaultProperty.Builder()
                                        .idShort("TestProp")
                                        .value("TestOutput")
                                        .build())
                                .build()))
                        .outputArguments(operation.getOutputVariables())
                        .executionState(ExecutionState.COMPLETED)
                        .success(true)
                        .build())
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }

    static class CustomAssetOperationProviderConfig extends AbstractAssetOperationProviderConfig {

        public static Builder builder() {
            return new Builder();
        }

        public abstract static class AbstractBuilder<T extends CustomAssetOperationProviderConfig, B extends AbstractBuilder<T, B>>
                extends AbstractAssetOperationProviderConfig.AbstractBuilder<T, B> {

        }

        public static class Builder extends AbstractBuilder<CustomAssetOperationProviderConfig, Builder> {

            @Override
            protected Builder getSelf() {
                return this;
            }


            @Override
            protected CustomAssetOperationProviderConfig newBuildingInstance() {
                return new CustomAssetOperationProviderConfig();
            }

        }
    }

    static class CustomAssetOperationProvider implements AssetOperationProvider<CustomAssetOperationProviderConfig> {

        private final CustomAssetOperationProviderConfig config;

        public CustomAssetOperationProvider() {
            config = new CustomAssetOperationProviderConfig();
        }


        public CustomAssetOperationProvider(CustomAssetOperationProviderConfig config) {
            this.config = config;
        }


        @Override
        public CustomAssetOperationProviderConfig getConfig() {
            return config;
        }


        @Override
        public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
            Property property = (Property) inoutput[0].getValue();
            property.setValue("TestOutput");
            return new OperationVariable[] {
                    new DefaultOperationVariable.Builder()
                            .value(new DefaultProperty.Builder()
                                    .idShort("TestPropOutput")
                                    .value("TestValue")
                                    .build())
                            .build()
            };
        }


        @Override
        public void invokeAsync(OperationVariable[] input, OperationVariable[] inoutput, BiConsumer<OperationVariable[], OperationVariable[]> callbackSuccess,
                                Consumer<Throwable> callbackFailure)
                throws AssetConnectionException {
            // intentionally left empty
        }
    }

    @Test
    public void testGetAllConceptDescriptionsRequest() throws ResourceNotFoundException, Exception {
        when(persistence.findConceptDescriptions(
                eq(ConceptDescriptionSearchCriteria.NONE),
                any(),
                any()))
                .thenReturn(Page.of(environment.getConceptDescriptions()));
        GetAllConceptDescriptionsRequest request = new GetAllConceptDescriptionsRequest.Builder()
                .outputModifier(OutputModifier.DEFAULT)
                .build();
        GetAllConceptDescriptionsResponse actual = manager.execute(request, context);
        GetAllConceptDescriptionsResponse expected = new GetAllConceptDescriptionsResponse.Builder()
                .payload(Page.of(environment.getConceptDescriptions()))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllConceptDescriptionsByIdShortRequest() throws ResourceNotFoundException, Exception {
        when(persistence.findConceptDescriptions(
                eq(ConceptDescriptionSearchCriteria.builder()
                        .idShort(environment.getConceptDescriptions().get(0).getIdShort())
                        .build()),
                any(),
                any()))
                .thenReturn(Page.of(environment.getConceptDescriptions()));
        GetAllConceptDescriptionsByIdShortRequest request = new GetAllConceptDescriptionsByIdShortRequest.Builder()
                .outputModifier(OutputModifier.DEFAULT)
                .idShort(environment.getConceptDescriptions().get(0).getIdShort())
                .build();
        GetAllConceptDescriptionsByIdShortResponse actual = manager.execute(request, context);
        GetAllConceptDescriptionsByIdShortResponse expected = new GetAllConceptDescriptionsByIdShortResponse.Builder()
                .payload(Page.of(environment.getConceptDescriptions()))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllConceptDescriptionsByIsCaseOfRequest() throws ResourceNotFoundException, Exception {
        Reference reference = ReferenceBuilder.forConceptDescription(environment.getConceptDescriptions().get(0));
        when(persistence.findConceptDescriptions(
                eq(ConceptDescriptionSearchCriteria.builder()
                        .isCaseOf(reference)
                        .build()),
                any(),
                any()))
                .thenReturn(Page.of(environment.getConceptDescriptions()));
        GetAllConceptDescriptionsByIsCaseOfRequest request = new GetAllConceptDescriptionsByIsCaseOfRequest.Builder()
                .outputModifier(OutputModifier.DEFAULT)
                .isCaseOf(reference)
                .build();
        GetAllConceptDescriptionsByIsCaseOfResponse actual = manager.execute(request, context);
        GetAllConceptDescriptionsByIsCaseOfResponse expected = new GetAllConceptDescriptionsByIsCaseOfResponse.Builder()
                .payload(Page.of(environment.getConceptDescriptions()))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetAllConceptDescriptionsByDataSpecificationReferenceRequest() throws ResourceNotFoundException, Exception {
        Reference reference = ReferenceBuilder.forConceptDescription(environment.getConceptDescriptions().get(0));
        when(persistence.findConceptDescriptions(
                eq(ConceptDescriptionSearchCriteria.builder()
                        .dataSpecification(reference)
                        .build()),
                any(),
                any()))
                .thenReturn(Page.of(environment.getConceptDescriptions()));
        GetAllConceptDescriptionsByDataSpecificationReferenceRequest request = new GetAllConceptDescriptionsByDataSpecificationReferenceRequest.Builder()
                .outputModifier(OutputModifier.DEFAULT)
                .dataSpecification(reference)
                .build();
        GetAllConceptDescriptionsByDataSpecificationReferenceResponse actual = manager.execute(request, context);
        GetAllConceptDescriptionsByDataSpecificationReferenceResponse expected = new GetAllConceptDescriptionsByDataSpecificationReferenceResponse.Builder()
                .payload(Page.of(environment.getConceptDescriptions()))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPostConceptDescriptionRequest() throws ResourceNotFoundException, Exception {
        PostConceptDescriptionRequest request = new PostConceptDescriptionRequest.Builder()
                .conceptDescription(environment.getConceptDescriptions().get(0))
                .build();
        PostConceptDescriptionResponse actual = manager.execute(request, context);
        PostConceptDescriptionResponse expected = new PostConceptDescriptionResponse.Builder()
                .payload(environment.getConceptDescriptions().get(0))
                .statusCode(StatusCode.SUCCESS_CREATED)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).save(environment.getConceptDescriptions().get(0));
    }


    @Test
    public void testPostConceptDescriptionRequestAlreadyExists() throws ResourceNotFoundException, Exception {
        ConceptDescription conceptDescription = environment.getConceptDescriptions().get(0);
        when(persistence.conceptDescriptionExists(conceptDescription.getId()))
                .thenReturn(true);
        PostConceptDescriptionRequest request = new PostConceptDescriptionRequest.Builder()
                .conceptDescription(conceptDescription)
                .build();
        PostConceptDescriptionResponse actual = manager.execute(request, context);
        Assert.assertEquals(StatusCode.CLIENT_RESOURCE_CONFLICT, actual.getStatusCode());
        verify(persistence, times(0)).save((ConceptDescription) any());
    }


    @Test
    @Ignore("Currently not working because AAS4j does not provide validation which is required to produce the expected error")
    public void testPostConceptDescriptionRequestEmptyConceptDescription() throws ResourceNotFoundException, Exception {
        PostConceptDescriptionResponse actual = manager.execute(
                new PostConceptDescriptionRequest.Builder()
                        .conceptDescription(new DefaultConceptDescription.Builder().build())
                        .build(),
                new StaticRequestExecutionContext(coreConfigWithConstraintValidation, persistence, fileStorage, messageBus, assetConnectionManager));
        Assert.assertEquals(StatusCode.CLIENT_ERROR_BAD_REQUEST, actual.getStatusCode());
    }


    @Test
    public void testGetConceptDescriptionByIdRequest() throws ResourceNotFoundException, Exception {
        when(persistence.getConceptDescription(eq(environment.getConceptDescriptions().get(0).getId()), any()))
                .thenReturn(environment.getConceptDescriptions().get(0));
        GetConceptDescriptionByIdRequest request = new GetConceptDescriptionByIdRequest.Builder()
                .outputModifier(OutputModifier.DEFAULT)
                .id(environment.getConceptDescriptions().get(0).getId())
                .build();
        GetConceptDescriptionByIdResponse actual = manager.execute(request, context);
        GetConceptDescriptionByIdResponse expected = new GetConceptDescriptionByIdResponse.Builder()
                .payload(environment.getConceptDescriptions().get(0))
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPutConceptDescriptionByIdRequest() throws ResourceNotFoundException, Exception {
        PutConceptDescriptionByIdRequest request = new PutConceptDescriptionByIdRequest.Builder()
                .conceptDescription(environment.getConceptDescriptions().get(0))
                .build();
        PutConceptDescriptionByIdResponse actual = manager.execute(request, context);
        PutConceptDescriptionByIdResponse expected = new PutConceptDescriptionByIdResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).save(environment.getConceptDescriptions().get(0));
    }


    @Test
    public void testDeleteConceptDescriptionByIdRequest() throws ResourceNotFoundException, Exception {
        when(persistence.getConceptDescription(eq(environment.getConceptDescriptions().get(0).getId()), any()))
                .thenReturn(environment.getConceptDescriptions().get(0));
        DeleteConceptDescriptionByIdRequest request = new DeleteConceptDescriptionByIdRequest.Builder()
                .id(environment.getConceptDescriptions().get(0).getId())
                .build();
        DeleteConceptDescriptionByIdResponse actual = manager.execute(request, context);
        DeleteConceptDescriptionByIdResponse expected = new DeleteConceptDescriptionByIdResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).deleteConceptDescription(environment.getConceptDescriptions().get(0).getId());
    }


    @Test
    public void testGetIdentifiableWithInvalidIdRequest() throws ResourceNotFoundException, Exception {
        when(persistence.getSubmodel(any(), any()))
                .thenThrow(new ResourceNotFoundException("Resource not found with id"));
        GetSubmodelByIdRequest request = new GetSubmodelByIdRequest.Builder().build();
        GetSubmodelByIdResponse actual = manager.execute(request, context);
        GetSubmodelByIdResponse expected = new GetSubmodelByIdResponse.Builder()
                .result(new DefaultResult.Builder()
                        .messages(Message.builder()
                                .messageType(MessageTypeEnum.ERROR)
                                .text("Resource not found with id")
                                .build())
                        .build())
                .statusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetReferableWithInvalidIdRequest() throws ResourceNotFoundException, Exception {
        when(persistence.getSubmodelElement((SubmodelElementIdentifier) any(), any()))
                .thenThrow(new ResourceNotFoundException("Resource not found with id"));
        GetSubmodelElementByPathRequest request = getExampleGetSubmodelElementByPathRequest();
        GetSubmodelElementByPathResponse actual = manager.execute(request, context);
        GetSubmodelElementByPathResponse expected = new GetSubmodelElementByPathResponse.Builder()
                .result(new DefaultResult.Builder()
                        .messages(Message.builder()
                                .messageType(MessageTypeEnum.ERROR)
                                .text("Resource not found with id")
                                .build())
                        .build())
                .statusCode(StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetReferableWithMessageBusExceptionRequest() throws ResourceNotFoundException, MessageBusException, Exception {
        when(persistence.getSubmodelElement((SubmodelElementIdentifier) any(), any()))
                .thenReturn(new DefaultProperty());
        doThrow(new MessageBusException("Invalid Messagbus Call")).when(messageBus).publish(any());
        GetSubmodelElementByPathRequest request = getExampleGetSubmodelElementByPathRequest();
        MessageBusException exception = Assert.assertThrows(MessageBusException.class, () -> manager.execute(request, context));
        Assert.assertEquals("Invalid Messagbus Call", exception.getMessage());
    }


    @Test
    public void testGetValueWithInvalidAssetConnectionRequest() throws ResourceNotFoundException, AssetConnectionException, Exception {
        when(persistence.getSubmodelElement((SubmodelElementIdentifier) any(), any()))
                .thenReturn(new DefaultProperty());
        AssetValueProvider assetValueProvider = mock(AssetValueProvider.class);
        when(assetConnectionManager.hasValueProvider(any())).thenReturn(true);
        when(assetConnectionManager.getValueProvider(any())).thenReturn(assetValueProvider);
        when(assetValueProvider.getValue()).thenThrow(new AssetConnectionException("Invalid Assetconnection"));
        GetSubmodelElementByPathRequest request = getExampleGetSubmodelElementByPathRequest();
        AssetConnectionException exception = Assert.assertThrows(AssetConnectionException.class, () -> manager.execute(request, context));
        Assert.assertEquals("Invalid Assetconnection", exception.getMessage());
    }


    private GetSubmodelElementByPathRequest getExampleGetSubmodelElementByPathRequest() {
        return new GetSubmodelElementByPathRequest.Builder()
                .path("testProperty")
                .submodelId("test")
                .build();
    }


    @Test
    public void testGetAllAssetAdministrationShellRequestAsync() throws InterruptedException, PersistenceException {
        when(persistence.findAssetAdministrationShells(eq(AssetAdministrationShellSearchCriteria.NONE), any(), any()))
                .thenReturn(Page.of(environment.getAssetAdministrationShells()));
        GetAllAssetAdministrationShellsRequest request = new GetAllAssetAdministrationShellsRequest();
        final AtomicReference<GetAllAssetAdministrationShellsResponse> response = new AtomicReference<>();
        CountDownLatch condition = new CountDownLatch(1);
        manager.executeAsync(request, x -> response.set(x), context);
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(environment.getAssetAdministrationShells(), response.get().getPayload().getContent());
    }


    @Test
    public void testReadValueFromAssetConnectionAndUpdatePersistence()
            throws AssetConnectionException, ResourceNotFoundException, ValueMappingException, MessageBusException, ResourceNotAContainerElementException, PersistenceException {
        AbstractRequestHandler requestHandler = new DeleteSubmodelByIdRequestHandler();
        Reference parentRef = ReferenceBuilder.forSubmodel("sub");
        SubmodelElement propertyUpdated = new DefaultProperty.Builder()
                .idShort("propertyUpdated")
                .value("test")
                .valueType(DataTypeDefXsd.STRING)
                .build();
        SubmodelElement rangeUpdated = new DefaultRange.Builder()
                .idShort("rangeUpdated")
                .max("1.0")
                .min("0.0")
                .valueType(DataTypeDefXsd.DOUBLE)
                .build();
        SubmodelElement propertyStatic = new DefaultProperty.Builder()
                .idShort("propertyStatic")
                .value("test")
                .valueType(DataTypeDefXsd.STRING)
                .build();
        SubmodelElementCollection collection = new DefaultSubmodelElementCollection.Builder()
                .idShort("col1")
                .value(propertyStatic)
                .build();

        SubmodelElement propertyExpected = new DefaultProperty.Builder()
                .idShort("propertyUpdated")
                .value("testNew")
                .valueType(DataTypeDefXsd.STRING)
                .build();
        SubmodelElement rangeExpected = new DefaultRange.Builder()
                .idShort("rangeUpdated")
                .max("2.0")
                .min("0.0")
                .valueType(DataTypeDefXsd.DOUBLE)
                .build();

        Reference propertyUpdatedRef = AasUtils.toReference(parentRef, propertyUpdated);
        Reference propertyStaticRef = AasUtils.toReference(AasUtils.toReference(parentRef, collection), propertyStatic);
        Reference rangeUpdatedRef = AasUtils.toReference(parentRef, rangeUpdated);
        List<SubmodelElement> submodelElements = new ArrayList<>(List.of(propertyUpdated, rangeUpdated, collection));
        AssetValueProvider propertyUpdatedProvider = mock(AssetValueProvider.class);
        AssetValueProvider propertyStaticProvider = mock(AssetValueProvider.class);
        AssetValueProvider rangeUpdatedProvider = mock(AssetValueProvider.class);
        when(assetConnectionManager.hasValueProvider(propertyUpdatedRef)).thenReturn(true);
        when(assetConnectionManager.hasValueProvider(propertyStaticRef)).thenReturn(true);
        when(assetConnectionManager.hasValueProvider(rangeUpdatedRef)).thenReturn(true);

        when(assetConnectionManager.getValueProvider(propertyUpdatedRef)).thenReturn(propertyUpdatedProvider);
        when(propertyUpdatedProvider.getValue()).thenReturn(ElementValueMapper.toValue(propertyExpected, DataElementValue.class));

        when(assetConnectionManager.getValueProvider(propertyStaticRef)).thenReturn(propertyStaticProvider);
        when(propertyStaticProvider.getValue()).thenReturn(ElementValueMapper.toValue(propertyStatic, DataElementValue.class));

        when(assetConnectionManager.getValueProvider(rangeUpdatedRef)).thenReturn(rangeUpdatedProvider);
        when(rangeUpdatedProvider.getValue()).thenReturn(ElementValueMapper.toValue(rangeExpected, DataElementValue.class));

        // TODO fix
        //when(persistence.put(null, propertyUpdatedRef, propertyExpected)).thenReturn(propertyExpected);
        //when(persistence.put(null, propertyStaticRef, propertyStatic)).thenReturn(propertyStatic);
        //when(persistence.put(null, rangeUpdatedRef, rangeExpected)).thenReturn(rangeExpected);
        requestHandler.syncWithAsset(
                parentRef,
                submodelElements,
                true,
                new StaticRequestExecutionContext(coreConfig, persistence, fileStorage, messageBus, assetConnectionManager));
        verify(persistence).update(propertyUpdatedRef, propertyExpected);
        verify(persistence).update(rangeUpdatedRef, rangeExpected);
        verify(persistence, times(0)).update(parentRef, propertyStatic);
        List<SubmodelElement> expectedSubmodelElements = List.of(propertyExpected, rangeExpected, collection);
        Assert.assertTrue(expectedSubmodelElements.size() == submodelElements.size()
                && expectedSubmodelElements.containsAll(submodelElements)
                && submodelElements.containsAll(expectedSubmodelElements));
    }


    public void testImport() throws Exception {
        ImportRequest request = new ImportRequest.Builder()
                .content("{}".getBytes())
                .contentType("application/json")
                .build();
        ImportResponse actual = manager.execute(request, context);
        ImportResponse expected = new ImportResponse.Builder()
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    public void testReset() throws Exception {
        ResetRequest request = new ResetRequest.Builder().build();
        ResetResponse actual = manager.execute(request, context);
        ResetResponse expected = new ResetResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }
}
