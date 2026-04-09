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

import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.ANY_URI;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.BASE64BINARY;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.BOOLEAN;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.BYTE;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.DATE;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.DATE_TIME;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.DECIMAL;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.DOUBLE;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.DURATION;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.FLOAT;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.GDAY;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.GMONTH;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.GMONTH_DAY;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.GYEAR;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.GYEAR_MONTH;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.HEX_BINARY;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INT;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.INTEGER;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.LONG;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.NEGATIVE_INTEGER;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.NON_NEGATIVE_INTEGER;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.NON_POSITIVE_INTEGER;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.POSITIVE_INTEGER;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.SHORT;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.STRING;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.TIME;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.UNSIGNED_BYTE;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.UNSIGNED_INT;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.UNSIGNED_LONG;
import static org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd.UNSIGNED_SHORT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.ArgumentValidationMode;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.PostAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.DeleteConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsByDataSpecificationReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsByIsCaseOfRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.PostConceptDescriptionRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.PutConceptDescriptionByIdRequest;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.PostSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.AbstractResponseWithPayload;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.PostAssetAdministrationShellResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.DeleteConceptDescriptionByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.GetAllConceptDescriptionsByDataSpecificationReferenceResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.GetAllConceptDescriptionsByIdShortResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.GetAllConceptDescriptionsByIsCaseOfResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.GetAllConceptDescriptionsResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.GetConceptDescriptionByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.PostConceptDescriptionResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.PutConceptDescriptionByIdResponse;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.PostSubmodelResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.StringValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.ReferenceCollector;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.ConceptDescriptionSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.request.RequestHandlerManager;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ResponseHelper;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultFile;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultResource;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultResult;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentMatcher;


public class RequestHandlerManagerTest {

    private static final long DEFAULT_TIMEOUT = 1000;
    private static final Random RANDOM = new Random();
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
        doReturn(persistence)
                .when(service)
                .getPersistence();
        doReturn(messageBus)
                .when(service)
                .getMessageBus();
        doReturn(true)
                .when(assetConnectionManager)
                .hasOperationProvider(any());
        doReturn(Optional.of(ArgumentValidationMode.REQUIRE_PRESENT_OR_DEFAULT))
                .when(assetConnectionManager)
                .getOperationInputValidationMode(any());
        doReturn(Optional.of(ArgumentValidationMode.REQUIRE_PRESENT_OR_DEFAULT))
                .when(assetConnectionManager)
                .getOperationInoutputValidationMode(any());
        doReturn(Optional.of(ArgumentValidationMode.REQUIRE_PRESENT_OR_DEFAULT))
                .when(assetConnectionManager)
                .getOperationOutputValidationMode(any());
        doAnswer(call -> {
            OperationVariable[] input = call.getArgument(1);
            OperationVariable[] inoutput = call.getArgument(2);
            return Optional.of(invokeMockOperation(input, inoutput));
        }).when(assetConnectionManager).invoke(any(), any(), any());
    }


    @Test
    public void testGetAllAssetAdministrationShellRequest() throws Exception {
        doReturn(Page.of(environment.getAssetAdministrationShells()))
                .when(persistence)
                .findAssetAdministrationShells(any(), any(), any());
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

        doReturn(Page.of(
                environment.getAssetAdministrationShells().get(0),
                environment.getAssetAdministrationShells().get(1)))
                .when(persistence)
                .findAssetAdministrationShells(eq(
                        AssetAdministrationShellSearchCriteria.builder()
                                .assetIds(List.of(globalAssetIdentification, specificAssetIdentification))
                                .build()),
                        any(),
                        any());

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
        doReturn(Page.of(environment.getAssetAdministrationShells()))
                .when(persistence)
                .findAssetAdministrationShells(
                        eq(AssetAdministrationShellSearchCriteria.builder()
                                .idShort("Test")
                                .build()),
                        any(),
                        any());

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
    public void testDeleteAssetAdministrationShellByIdRequest() throws ResourceNotFoundException, Exception {
        doReturn(environment.getAssetAdministrationShells().get(0))
                .when(persistence)
                .getAssetAdministrationShell(
                        eq(environment.getAssetAdministrationShells().get(0).getId()),
                        any());

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
        doReturn(environment.getAssetAdministrationShells().get(0))
                .when(persistence)
                .getAssetAdministrationShell(
                        eq(environment.getAssetAdministrationShells().get(0).getId()),
                        any());

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
        doReturn(environment.getAssetAdministrationShells().get(0))
                .when(persistence)
                .getAssetAdministrationShell(
                        eq(environment.getAssetAdministrationShells().get(0).getId()),
                        any());

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
        doReturn(new DefaultAssetAdministrationShell.Builder()
                .id(aasId)
                .assetInformation(new DefaultAssetInformation.Builder()
                        .defaultThumbnail(new DefaultResource.Builder()
                                .path(file.getPath())
                                .build())
                        .build())
                .build())
                .when(persistence)
                .getAssetAdministrationShell(eq(aasId), any());
        doReturn(file.getContent())
                .when(fileStorage)
                .get(file.getPath());

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
        doReturn(new DefaultAssetAdministrationShell.Builder()
                .id(aasId)
                .assetInformation(new DefaultAssetInformation.Builder()
                        .defaultThumbnail(new DefaultResource.Builder()
                                .path(file.getPath())
                                .build())
                        .build())
                .build())
                .when(persistence)
                .getAssetAdministrationShell(eq(aasId), any());
        doReturn(file.getContent())
                .when(fileStorage)
                .get(file.getPath());

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
        doReturn(file)
                .when(persistence)
                .getSubmodelElement(any(SubmodelElementIdentifier.class), any());
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
        doReturn(expectedFile.getContent())
                .when(fileStorage)
                .get(expectedFile.getPath());
        GetFileByPathResponse actual = manager.execute(request, context);
        GetFileByPathResponse expected = new GetFileByPathResponse.Builder()
                .payload(expectedFile)
                .statusCode(StatusCode.SUCCESS)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testPutAssetInformationRequest() throws ResourceNotFoundException, Exception {
        doReturn(environment.getAssetAdministrationShells().get(0))
                .when(persistence)
                .getAssetAdministrationShell(eq(environment.getAssetAdministrationShells().get(0).getId()), any());

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
        doReturn(Page.of(environment.getAssetAdministrationShells().get(0).getSubmodels()))
                .when(persistence)
                .getSubmodelRefs(eq(environment.getAssetAdministrationShells().get(0).getId()), any());
        doReturn(environment.getAssetAdministrationShells().get(0))
                .when(persistence)
                .getAssetAdministrationShell(eq(environment.getAssetAdministrationShells().get(0).getId()), any());

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
        doReturn(environment.getAssetAdministrationShells().get(0))
                .when(persistence)
                .getAssetAdministrationShell(eq(environment.getAssetAdministrationShells().get(0).getId()), any());

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
        doReturn(environment.getAssetAdministrationShells().get(0))
                .when(persistence)
                .getAssetAdministrationShell(eq(environment.getAssetAdministrationShells().get(0).getId()), any());

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
        doReturn(Page.of(environment.getSubmodels()))
                .when(persistence)
                .findSubmodels(eq(SubmodelSearchCriteria.NONE), any(), any());

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
    public void testGetAllSubmodelsRequestWithAssetConnection() throws ResourceNotFoundException, Exception {
        Page<Submodel> data = Page.<Submodel> of(environment.getSubmodels());
        doReturn(data)
                .when(persistence)
                .findSubmodels(eq(SubmodelSearchCriteria.NONE), any(), any());

        assertReadWithAssetConnection(
                data,
                null,
                TypeFactory.defaultInstance().constructParametricType(Page.class, Submodel.class),
                new GetAllSubmodelsRequest.Builder()
                        .outputModifier(OutputModifier.DEFAULT)
                        .build(),
                new GetAllSubmodelsResponse());
    }


    @Test
    public void testGetAllSubmodelsBySemanticIdRequest() throws ResourceNotFoundException, Exception {
        doReturn(Page.of(environment.getSubmodels()))
                .when(persistence)
                .findSubmodels(
                        eq(SubmodelSearchCriteria.builder()
                                .semanticId(SUBMODEL_ELEMENT_REF)
                                .build()),
                        any(),
                        any());

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
    public void testGetAllSubmodelsBySemanticIdRequestWithAssetConnection() throws ResourceNotFoundException, Exception {
        Page<Submodel> data = Page.<Submodel> of(environment.getSubmodels());
        doReturn(data)
                .when(persistence)
                .findSubmodels(
                        eq(SubmodelSearchCriteria.builder()
                                .semanticId(SUBMODEL_ELEMENT_REF)
                                .build()),
                        any(),
                        any());

        assertReadWithAssetConnection(
                data,
                null,
                TypeFactory.defaultInstance().constructParametricType(Page.class, Submodel.class),
                new GetAllSubmodelsBySemanticIdRequest.Builder()
                        .semanticId(SUBMODEL_ELEMENT_REF)
                        .outputModifier(OutputModifier.DEFAULT)
                        .build(),
                new GetAllSubmodelsBySemanticIdResponse());
    }


    @Test
    public void testGetAllSubmodelsByIdShortRequest() throws ResourceNotFoundException, Exception {
        doReturn(Page.of(environment.getSubmodels()))
                .when(persistence)
                .findSubmodels(
                        eq(SubmodelSearchCriteria.builder()
                                .idShort("Test")
                                .build()),
                        any(),
                        any());

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
    public void testGetAllSubmodelsByIdShortRequestWithAssetConnection() throws ResourceNotFoundException, Exception {
        Page<Submodel> data = Page.<Submodel> of(environment.getSubmodels());
        doReturn(data)
                .when(persistence)
                .findSubmodels(
                        eq(SubmodelSearchCriteria.builder()
                                .idShort("Test")
                                .build()),
                        any(),
                        any());

        assertReadWithAssetConnection(
                data,
                null,
                TypeFactory.defaultInstance().constructParametricType(Page.class, Submodel.class),
                new GetAllSubmodelsByIdShortRequest.Builder()
                        .idShort("Test")
                        .outputModifier(OutputModifier.DEFAULT)
                        .build(),
                new GetAllSubmodelsByIdShortResponse());
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
    public void testPostSubmodelRequestWithAssetConnection() throws ResourceNotFoundException, Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        assertWriteWithAssetConnection(
                submodel,
                null,
                TypeFactory.defaultInstance().constructType(Submodel.class),
                x -> new PostSubmodelRequest.Builder()
                        .submodel(x)
                        .build(),
                x -> new PostSubmodelResponse.Builder()
                        .payload(x)
                        .statusCode(StatusCode.SUCCESS_CREATED)
                        .build());
    }


    @Test
    public void testPostSubmodelRequestAlreadyExists() throws ResourceNotFoundException, Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        PostSubmodelRequest request = new PostSubmodelRequest.Builder()
                .submodel(submodel)
                .build();
        doReturn(true)
                .when(persistence)
                .submodelExists(submodel.getId());
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
    public void testPutSubmodelRequest() throws ResourceNotFoundException, Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        doReturn(submodel)
                .when(persistence)
                .getSubmodel(eq(submodel.getId()), any());
        PutSubmodelRequest request = new PutSubmodelRequest.Builder()
                .submodelId(submodel.getId())
                .submodel(submodel)
                .build();
        PutSubmodelResponse actual = manager.execute(request, context);
        PutSubmodelResponse expected = new PutSubmodelResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).save(submodel);
    }


    @Test
    public void testPutSubmodelRequestWithAssetConnection() throws ResourceNotFoundException, Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        doReturn(submodel)
                .when(persistence)
                .getSubmodel(eq(submodel.getId()), any());
        assertWriteWithAssetConnection(
                submodel,
                null,
                TypeFactory.defaultInstance().constructType(Submodel.class),
                x -> new PutSubmodelRequest.Builder()
                        .submodelId(submodel.getId())
                        .submodel(x)
                        .build(),
                x -> new PutSubmodelResponse.Builder()
                        .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                        .build());
    }


    @Test
    public void testDeleteSubmodelByIdRequest() throws ResourceNotFoundException, Exception {
        doReturn(environment.getSubmodels().get(0))
                .when(persistence)
                .getSubmodel(eq(environment.getSubmodels().get(0).getId()), any());

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
        doReturn(environment.getSubmodels().get(0))
                .when(persistence)
                .getSubmodel(eq(environment.getSubmodels().get(0).getId()), any());

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
    public void testGetSubmodelRequestWithAssetConnection() throws ResourceNotFoundException, Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        doReturn(submodel)
                .when(persistence)
                .getSubmodel(eq(submodel.getId()), any());

        assertReadWithAssetConnection(
                submodel,
                null,
                TypeFactory.defaultInstance().constructType(Submodel.class),
                new GetSubmodelRequest.Builder()
                        .submodelId(submodel.getId())
                        .outputModifier(OutputModifier.DEFAULT)
                        .build(),
                new GetSubmodelResponse());
    }


    @Test
    public void testGetAllSubmodelElementsRequest() throws ResourceNotFoundException, Exception {
        Reference reference = ReferenceBuilder.forSubmodel(environment.getSubmodels().get(0));
        doReturn(Page.of(environment.getSubmodels().get(0).getSubmodelElements()))
                .when(persistence)
                .getSubmodelElements(eq(SubmodelElementIdentifier.fromReference(reference)), any(), any());

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
    public void testGetAllSubmodelElementsRequestWithAssetConnection() throws ResourceNotFoundException, Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        Page<SubmodelElement> data = Page.<SubmodelElement> of(submodel.getSubmodelElements());
        doReturn(data)
                .when(persistence)
                .getSubmodelElements(
                        eq(SubmodelElementIdentifier.builder()
                                .submodelId(submodel.getId())
                                .build()),
                        any(),
                        any());

        assertReadWithAssetConnection(
                data,
                ReferenceBuilder.forSubmodel(submodel),
                TypeFactory.defaultInstance().constructParametricType(Page.class, SubmodelElement.class),
                new GetAllSubmodelElementsRequest.Builder()
                        .submodelId(submodel.getId())
                        .outputModifier(OutputModifier.DEFAULT)
                        .build(),
                new GetAllSubmodelElementsResponse());
    }


    @Test
    public void testPostSubmodelElementRequest() throws ResourceNotFoundException, Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        Reference reference = ReferenceBuilder.forSubmodel(submodel);
        SubmodelElement submodelElement = DeepCopyHelper.deepCopy(environment.getSubmodels().get(0).getSubmodelElements().get(0));
        PostSubmodelElementRequest request = new PostSubmodelElementRequest.Builder()
                .submodelId(submodel.getId())
                .submodelElement(submodelElement)
                .build();
        PostSubmodelElementResponse actual = manager.execute(request, context);
        PostSubmodelElementResponse expected = new PostSubmodelElementResponse.Builder()
                .statusCode(StatusCode.SUCCESS_CREATED)
                .payload(submodelElement)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        verify(persistence).insert(SubmodelElementIdentifier.fromReference(reference), submodelElement);
    }


    @Test
    public void testPostSubmodelElementsRequestWithAssetConnection() throws ResourceNotFoundException, Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        SubmodelElement submodelElement = new DefaultProperty.Builder()
                .idShort("newProperty")
                .valueType(STRING)
                .build();
        assertWriteWithAssetConnection(
                submodelElement,
                ReferenceBuilder.forSubmodel(submodel, submodelElement),
                TypeFactory.defaultInstance().constructType(SubmodelElement.class),
                x -> new PostSubmodelElementRequest.Builder()
                        .submodelId(submodel.getId())
                        .submodelElement(x)
                        .build(),
                x -> new PostSubmodelElementResponse.Builder()
                        .statusCode(StatusCode.SUCCESS_CREATED)
                        .payload(x)
                        .build());
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
        doReturn(true)
                .when(persistence)
                .submodelElementExists(referenceToNewElement);

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
        doReturn(cur_submodelElement)
                .when(persistence)
                .getSubmodelElement((SubmodelElementIdentifier) any(), eq(OutputModifier.DEFAULT));
        doReturn(true)
                .when(assetConnectionManager)
                .hasValueProvider(any());
        doReturn(Optional.of(propertyValue))
                .when(assetConnectionManager)
                .readValue(any());

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
        assetConnectionManager.stop();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
    }


    @Test
    public void testGetSubmodelElementByPathRequestWithAssetConnection() throws ResourceNotFoundException, Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        SubmodelElement submodelElement = submodel.getSubmodelElements().stream().filter(Property.class::isInstance).findFirst().get();
        doReturn(submodelElement)
                .when(persistence)
                .getSubmodelElement((SubmodelElementIdentifier) any(), eq(OutputModifier.DEFAULT));

        assertReadWithAssetConnection(
                submodelElement,
                ReferenceBuilder.forSubmodel(submodel, submodelElement),
                TypeFactory.defaultInstance().constructType(SubmodelElement.class),
                new GetSubmodelElementByPathRequest.Builder()
                        .submodelId(submodel.getId())
                        .outputModifier(OutputModifier.DEFAULT)
                        .path(submodelElement.getIdShort())
                        .build(),
                new GetSubmodelElementByPathResponse());
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
        doReturn(list)
                .when(persistence)
                .getSubmodelElement(eq(listIdentifier), any());
        Reference refNewElement = new ReferenceBuilder()
                .submodel(submodel)
                .element(list)
                .index(1)
                .build();
        doReturn(false)
                .when(persistence)
                .submodelElementExists(refNewElement);
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
    public void testPostSubmodelElementByPathRequestWithAssetConnection() throws ResourceNotFoundException, Exception {
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
        doReturn(list)
                .when(persistence)
                .getSubmodelElement(eq(listIdentifier), any());
        Reference refNewElement = new ReferenceBuilder()
                .submodel(submodel)
                .element(list)
                .index(1)
                .build();
        doReturn(false)
                .when(persistence)
                .submodelElementExists(refNewElement);
        Property newProperty = new DefaultProperty.Builder()
                .valueType(DataTypeDefXsd.STRING)
                .value("new")
                .build();
        assertWriteWithAssetConnection(
                newProperty,
                refNewElement,
                TypeFactory.defaultInstance().constructType(SubmodelElement.class),
                x -> new PostSubmodelElementByPathRequest.Builder()
                        .submodelId(submodel.getId())
                        .submodelElement(x)
                        .path(listPath.toString())
                        .build(),
                x -> new PostSubmodelElementByPathResponse.Builder()
                        .payload(x)
                        .statusCode(StatusCode.SUCCESS_CREATED)
                        .build());
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
        doReturn(list)
                .when(persistence)
                .getSubmodelElement((SubmodelElementIdentifier) any(), eq(QueryModifier.DEFAULT));
        Property newProperty = new DefaultProperty.Builder()
                .valueType(DataTypeDefXsd.STRING)
                .value("new")
                .build();

        Reference referenceToNewElement = new ReferenceBuilder()
                .submodel(submodel)
                .elements(listPath.getElements())
                .element(newProperty)
                .build();
        doReturn(true)
                .when(persistence)
                .submodelElementExists(referenceToNewElement);
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
        Property originalProperty = new DefaultProperty.Builder()
                .valueType(DataTypeDefXsd.STRING)
                .value("original value")
                .build();
        Property newProperty = new DefaultProperty.Builder()
                .valueType(DataTypeDefXsd.STRING)
                .value("new value")
                .build();
        SubmodelElementList list = new DefaultSubmodelElementList.Builder()
                .idShort("list")
                .value(originalProperty)
                .build();
        Submodel submodel = new DefaultSubmodel.Builder()
                .id("submodel")
                .submodelElements(list)
                .build();
        IdShortPath propertyPath = IdShortPath.builder()
                .path(list.getIdShort())
                .index(0)
                .build();
        SubmodelElementIdentifier propertyIdentifier = SubmodelElementIdentifier.builder()
                .submodelId(submodel.getId())
                .idShortPath(propertyPath)
                .build();

        Reference propertyReference = new ReferenceBuilder()
                .submodel(submodel)
                .element(list)
                .index(0, KeyTypes.PROPERTY)
                .build();

        doReturn(originalProperty)
                .when(persistence)
                .getSubmodelElement(eq(propertyIdentifier), any());
        doReturn(true)
                .when(assetConnectionManager)
                .hasValueProvider(any());
        doNothing()
                .when(assetConnectionManager)
                .setValue(any(), any());

        PutSubmodelElementByPathRequest request = new PutSubmodelElementByPathRequest.Builder()
                .submodelId(submodel.getId())
                .path(propertyPath.toString())
                .submodelElement(newProperty)
                .build();
        PutSubmodelElementByPathResponse actual = manager.execute(request, context);
        PutSubmodelElementByPathResponse expected = new PutSubmodelElementByPathResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));

        verify(assetConnectionManager).setValue(
                argThat(sameAs(propertyReference)),
                eq(ElementValueMapper.toValue(newProperty, DataElementValue.class)));
        verify(persistence).update(
                argThat(sameAs(propertyReference)),
                eq(newProperty));
    }


    @Test
    public void testPatchSubmodelElementValueByPathRequest() throws ResourceNotFoundException, AssetConnectionException, Exception {
        doReturn(environment.getSubmodels().get(0).getSubmodelElements().get(0))
                .when(persistence)
                .getSubmodelElement((SubmodelElementIdentifier) any(), any());
        doReturn(true)
                .when(assetConnectionManager)
                .hasValueProvider(any());
        doNothing()
                .when(assetConnectionManager)
                .setValue(any(), any());
        PropertyValue propertyValue = new PropertyValue.Builder()
                .value(new StringValue("Test"))
                .build();
        PatchSubmodelElementValueByPathRequest request = new PatchSubmodelElementValueByPathRequest.Builder<ElementValue>()
                .submodelId(environment.getSubmodels().get(0).getId())
                .value(propertyValue)
                .path(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .build();

        Response actual = manager.execute(request, context);
        PatchSubmodelElementValueByPathResponse expected = new PatchSubmodelElementValueByPathResponse.Builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        assetConnectionManager.stop();
        verify(assetConnectionManager).setValue(
                any(),
                eq(propertyValue));
    }


    @Test
    public void testDeleteSubmodelElementByPathRequest() throws ResourceNotFoundException, Exception {
        Submodel submodel = environment.getSubmodels().get(0);
        Reference reference = new ReferenceBuilder()
                .submodel(submodel)
                .idShortPath(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .build();
        doReturn(environment.getSubmodels().get(0).getSubmodelElements().get(0))
                .when(persistence)
                .getSubmodelElement(reference, QueryModifier.DEFAULT);

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


    @Test
    public void testInvokeOperationAsyncRequest() throws Exception {
        String submodelId = "http://example.org";
        Operation operation = getTestOperation();

        doReturn(new DefaultOperationResult.Builder().build())
                .when(persistence)
                .getOperationResult(any());
        doReturn(operation)
                .when(persistence)
                .getSubmodelElement(
                        ReferenceBuilder.forSubmodel(submodelId, operation.getIdShort()),
                        QueryModifier.MINIMAL,
                        Operation.class);
        doNothing().when(assetConnectionManager)
                .invokeAsync(any(), any(), any(), any(), any());

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
        Operation operation = getTestOperation();
        doReturn(operation)
                .when(persistence)
                .getSubmodelElement(
                        ReferenceBuilder.forSubmodel(submodelId, operation.getIdShort()),
                        QueryModifier.MINIMAL,
                        Operation.class);

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
        Operation operation = getTestOperation();
        doReturn(Optional.of(ArgumentValidationMode.REQUIRE_PRESENT))
                .when(assetConnectionManager)
                .getOperationInputValidationMode(any());
        doReturn(operation)
                .when(persistence)
                .getSubmodelElement(
                        ReferenceBuilder.forSubmodel(submodelId, operation.getIdShort()),
                        QueryModifier.MINIMAL,
                        Operation.class);

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
        Operation operation = getTestOperation();
        doReturn(operation)
                .when(persistence)
                .getSubmodelElement(
                        ReferenceBuilder.forSubmodel(submodelId, operation.getIdShort()),
                        QueryModifier.MINIMAL,
                        Operation.class);

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


    @Test
    public void testGetAllConceptDescriptionsRequest() throws ResourceNotFoundException, Exception {
        doReturn(Page.of(environment.getConceptDescriptions()))
                .when(persistence)
                .findConceptDescriptions(eq(ConceptDescriptionSearchCriteria.NONE), any(), any());

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
        doReturn(Page.of(environment.getConceptDescriptions()))
                .when(persistence)
                .findConceptDescriptions(
                        eq(ConceptDescriptionSearchCriteria.builder()
                                .idShort(environment.getConceptDescriptions().get(0).getIdShort())
                                .build()),
                        any(),
                        any());

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
        doReturn(Page.of(environment.getConceptDescriptions()))
                .when(persistence)
                .findConceptDescriptions(
                        eq(ConceptDescriptionSearchCriteria.builder()
                                .isCaseOf(reference)
                                .build()),
                        any(),
                        any());

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
        doReturn(Page.of(environment.getConceptDescriptions()))
                .when(persistence)
                .findConceptDescriptions(
                        eq(ConceptDescriptionSearchCriteria.builder()
                                .dataSpecification(reference)
                                .build()),
                        any(),
                        any());

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
        doReturn(true)
                .when(persistence)
                .conceptDescriptionExists(conceptDescription.getId());

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
        doReturn(environment.getConceptDescriptions().get(0))
                .when(persistence)
                .getConceptDescription(eq(environment.getConceptDescriptions().get(0).getId()), any());

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
        doReturn(environment.getConceptDescriptions().get(0))
                .when(persistence)
                .getConceptDescription(eq(environment.getConceptDescriptions().get(0).getId()), any());

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
        doThrow(new ResourceNotFoundException("Resource not found with id"))
                .when(persistence)
                .getSubmodel(any(), any());

        GetSubmodelRequest request = new GetSubmodelRequest.Builder()
                .submodelId("foo")
                .build();
        GetSubmodelResponse actual = manager.execute(request, context);
        GetSubmodelResponse expected = new GetSubmodelResponse.Builder()
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
        doThrow(new ResourceNotFoundException("Resource not found with id"))
                .when(persistence)
                .getSubmodelElement(any(SubmodelElementIdentifier.class), any());

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
        doReturn(new DefaultProperty())
                .when(persistence)
                .getSubmodelElement(any(SubmodelElementIdentifier.class), any());
        doThrow(new MessageBusException("Invalid Messagbus Call"))
                .when(messageBus)
                .publish(any());

        GetSubmodelElementByPathRequest request = getExampleGetSubmodelElementByPathRequest();
        MessageBusException exception = Assert.assertThrows(MessageBusException.class, () -> manager.execute(request, context));
        Assert.assertEquals("Invalid Messagbus Call", exception.getMessage());
    }


    @Test
    public void testGetAllAssetAdministrationShellRequestAsync() throws InterruptedException, PersistenceException {
        doReturn(Page.of(environment.getAssetAdministrationShells()))
                .when(persistence)
                .findAssetAdministrationShells(eq(AssetAdministrationShellSearchCriteria.NONE), any(), any());

        GetAllAssetAdministrationShellsRequest request = new GetAllAssetAdministrationShellsRequest();
        final AtomicReference<GetAllAssetAdministrationShellsResponse> response = new AtomicReference<>();
        CountDownLatch condition = new CountDownLatch(1);
        manager.executeAsync(request, x -> response.set(x), context);
        condition.await(DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        Assert.assertEquals(environment.getAssetAdministrationShells(), response.get().getPayload().getContent());
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


    private GetSubmodelElementByPathRequest getExampleGetSubmodelElementByPathRequest() {
        return new GetSubmodelElementByPathRequest.Builder()
                .path("testProperty")
                .submodelId("test")
                .build();
    }


    private OperationVariable[] invokeMockOperation(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
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


    private <T> void assertReadWithAssetConnection(T originalData,
                                                   Reference baseReference,
                                                   JavaType type,
                                                   Request request,
                                                   AbstractResponseWithPayload<T> expected)
            throws Exception {
        Map<Reference, Property> updatedProperties = new HashMap<>();
        T expectedData = randomizePropertyValues(
                originalData,
                baseReference,
                type,
                LambdaExceptionHelper.rethrowBiConsumer((reference, property) -> {
                    doReturn(Optional.of(ElementValueMapper.toValue(property, DataElementValue.class)))
                            .when(assetConnectionManager)
                            .readValue(reference);
                    updatedProperties.put(reference, property);
                }));
        expected.setPayload(expectedData);
        expected.setStatusCode(StatusCode.SUCCESS);
        Response actual = manager.execute(request, context);
        assetConnectionManager.stop();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        assertPersistenceUpdateCalled(updatedProperties);
        assertValueChangeEventsSent(updatedProperties);
    }


    private <T> void assertWriteWithAssetConnection(T originalData,
                                                    Reference baseReference,
                                                    JavaType type,
                                                    Function<T, Request> requestBuilder,
                                                    Function<T, Response> expectedResponseBuilder)
            throws Exception {
        Map<Reference, Property> updatedProperties = new HashMap<>();
        // when claled from POST, the newly added element is not found here - maybe have seperate methods for POST and PUT?
        T expectedData = randomizePropertyValues(
                originalData,
                baseReference,
                type,
                LambdaExceptionHelper.rethrowBiConsumer((reference, property) -> {
                    doNothing()
                            .when(assetConnectionManager)
                            .setValue(eq(reference), any());
                    doReturn(true)
                            .when(assetConnectionManager)
                            .hasValueProvider(reference);
                    doReturn(Optional.of(ElementValueMapper.toValue(property, DataElementValue.class)))
                            .when(assetConnectionManager)
                            .readValue(reference);
                    updatedProperties.put(reference, property);
                }));
        Request request = requestBuilder.apply(expectedData);

        Response expected = expectedResponseBuilder.apply(expectedData);
        Response actual = manager.execute(request, context);
        assetConnectionManager.stop();
        Assert.assertTrue(ResponseHelper.equalsIgnoringTime(expected, actual));
        assertValueProviderSetValueCalled(updatedProperties);
        assertValueChangeEventsSent(updatedProperties);
    }


    private void assertPersistenceUpdateCalled(Map<Reference, Property> updatedProperties) throws Exception {
        updatedProperties.entrySet().stream().forEach(LambdaExceptionHelper.rethrowConsumer(
                x -> verify(persistence).update(x.getKey(), x.getValue())));
    }


    private void assertValueChangeEventsSent(Map<Reference, Property> updatedProperties) throws Exception {
        updatedProperties.entrySet().stream().forEach(LambdaExceptionHelper.rethrowConsumer(
                x -> verify(messageBus).publish(argThat(msg -> {
                    if (!(msg instanceof ValueChangeEventMessage)) {
                        return false;
                    }
                    ValueChangeEventMessage event = (ValueChangeEventMessage) msg;
                    try {
                        return ReferenceHelper.equals(x.getKey(), event.getElement())
                                && Objects.equals(ElementValueMapper.toValue(x.getValue()), event.getNewValue());
                    }
                    catch (ValueMappingException e) {
                        Assert.fail();
                        return false;
                    }
                }))));
    }


    private void assertValueProviderSetValueCalled(Map<Reference, Property> updatedProperties) throws Exception {
        updatedProperties.entrySet().stream().forEach(LambdaExceptionHelper.rethrowConsumer(
                x -> verify(assetConnectionManager).setValue(x.getKey(), ElementValueMapper.toValue(x.getValue()))));
    }


    private <T> T randomizePropertyValues(T element, Reference baseReference, JavaType type, BiConsumer<Reference, Property> handler) throws Exception {
        T result = DeepCopyHelper.deepCopyAny(element, type);
        ReferenceCollector.collect(result, baseReference).entrySet().stream()
                .filter(x -> Property.class.isInstance(x.getValue()))
                .forEach(x -> {
                    Property property = ((Property) x.getValue());
                    property.setValue(randomValue(property.getValueType()));
                    if (Objects.nonNull(handler)) {
                        handler.accept(x.getKey(), property);
                    }
                });
        return result;
    }


    private String randomValue(DataTypeDefXsd datatype) {
        return switch (datatype) {
            case STRING -> UUID.randomUUID().toString();
            case DECIMAL, DOUBLE -> Double.toString(RANDOM.nextDouble());
            case ANY_URI -> "http://example.org/" + UUID.randomUUID();
            case BASE64BINARY -> {
                byte[] bytes = new byte[10];
                RANDOM.nextBytes(bytes);
                yield Base64.getEncoder().encodeToString(bytes);
            }
            case BOOLEAN -> Boolean.toString(RANDOM.nextBoolean());
            case BYTE -> Byte.toString((byte) RANDOM.nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE + 1));
            case DATE -> LocalDate.of(RANDOM.nextInt(1970, 2030),
                    RANDOM.nextInt(1, 13),
                    RANDOM.nextInt(1, 28))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE);
            case DATE_TIME -> LocalDateTime.of(
                    LocalDate.of(RANDOM.nextInt(1970, 2030),
                            RANDOM.nextInt(1, 13),
                            RANDOM.nextInt(1, 28)),
                    LocalTime.of(RANDOM.nextInt(0, 24),
                            RANDOM.nextInt(0, 60),
                            RANDOM.nextInt(0, 60)))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            case DURATION -> String.format("P%dY%dM%dDT%dH%dM%dS",
                    RANDOM.nextInt(5),
                    RANDOM.nextInt(12),
                    RANDOM.nextInt(28),
                    RANDOM.nextInt(24),
                    RANDOM.nextInt(60),
                    RANDOM.nextInt(60));
            case FLOAT -> Float.toString(RANDOM.nextFloat());
            case GDAY -> "---" + String.format("%02d", RANDOM.nextInt(1, 32));
            case GMONTH -> "--" + String.format("%02d", RANDOM.nextInt(1, 13));
            case GMONTH_DAY -> "--" + String.format("%02d-%02d", RANDOM.nextInt(1, 13), RANDOM.nextInt(1, 32));
            case GYEAR -> String.valueOf(RANDOM.nextInt(1970, 2030));
            case GYEAR_MONTH -> String.format("%04d-%02d", RANDOM.nextInt(1970, 2030), RANDOM.nextInt(1, 13));
            case HEX_BINARY -> {
                byte[] bytes = new byte[8];
                RANDOM.nextBytes(bytes);
                StringBuilder sb = new StringBuilder();
                for (byte b: bytes) {
                    sb.append(String.format("%02X", b));
                }
                yield sb.toString();
            }
            case INT, INTEGER -> Integer.toString(RANDOM.nextInt());
            case LONG -> Long.toString(RANDOM.nextLong());
            case NEGATIVE_INTEGER -> Integer.toString(-RANDOM.nextInt(Integer.MAX_VALUE));
            case NON_NEGATIVE_INTEGER -> Integer.toString(RANDOM.nextInt(Integer.MAX_VALUE + 1));
            case NON_POSITIVE_INTEGER -> Integer.toString(-RANDOM.nextInt(Integer.MAX_VALUE + 1));
            case POSITIVE_INTEGER -> Integer.toString(RANDOM.nextInt(1, Integer.MAX_VALUE));
            case SHORT -> Short.toString((short) RANDOM.nextInt(Short.MIN_VALUE, Short.MAX_VALUE + 1));
            case TIME -> LocalTime.of(RANDOM.nextInt(0, 24),
                    RANDOM.nextInt(0, 60),
                    RANDOM.nextInt(0, 60))
                    .format(DateTimeFormatter.ISO_LOCAL_TIME);
            case UNSIGNED_BYTE -> Integer.toString(RANDOM.nextInt(0, Byte.MAX_VALUE));
            case UNSIGNED_INT -> Integer.toString(RANDOM.nextInt(0, Integer.MAX_VALUE));
            case UNSIGNED_LONG -> Long.toString(RANDOM.nextLong(0, Long.MAX_VALUE));
            case UNSIGNED_SHORT -> Integer.toString(RANDOM.nextInt(0, Short.MAX_VALUE));
            default -> throw new IllegalStateException("Unsupported datatype");
        };
    }


    private static ArgumentMatcher<Reference> sameAs(Reference reference) {
        return new ArgumentMatcher<Reference>() {
            @Override
            public boolean matches(Reference x) {
                return ReferenceHelper.equals(reference, x);
            }


            @Override
            public String toString() {
                return "[ReferenceMatcher for " + reference + "]";
            }
        };
    }
}
