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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.dpp.request;

import static de.fraunhofer.iosb.ilt.faaast.service.model.DPP.DPP_1;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.dpp.exception.MethodNotAllowedException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.dpp.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.dpp.serialization.HttpJsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.dpp.util.HttpConstants;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.TypedInMemoryFile;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.PatchSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.DeleteSubmodelReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.DeleteThumbnailRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.GetAllSubmodelReferencesRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.GetAssetAdministrationShellReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.GetAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.GetAssetInformationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.GetThumbnailRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.PostSubmodelReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.PutAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.PutAssetInformationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.PutThumbnailRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasbasicdiscovery.DeleteAllAssetLinksByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasbasicdiscovery.GetAllAssetAdministrationShellIdsByAssetLinkRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasbasicdiscovery.GetAllAssetLinksByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasbasicdiscovery.PostAllAssetLinksByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasbasicdiscovery.SearchAllAssetAdministrationShellIdsByAssetLinkRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.DeleteAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByAssetIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsReferenceRequest;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.description.GetSelfDescriptionRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp.ReadDppByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp.ReadDppByProductIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp.ReadDppIdsByProductIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.proprietary.DeleteOperationProviderByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.proprietary.ImportRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.proprietary.PostOperationProviderByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.proprietary.ResetRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.DeleteFileByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.DeleteSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetAllSubmodelElementsReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetAllSubmodelElementsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetFileByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetOperationAsyncResultRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationAsyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PatchSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PatchSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PostSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PostSubmodelElementRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PutFileByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PutSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PutSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.DeleteSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.GetAllSubmodelsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.GetAllSubmodelsBySemanticIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.GetAllSubmodelsReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.GetAllSubmodelsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.PostSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DppSerializationMode;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.xml.datatype.DatatypeFactory;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.junit.Assert;
import org.junit.Test;


public class RequestMappingManagerTest {

    private final RequestMappingManager mappingManager;
    private final HttpJsonApiSerializer serializer;
    private final ServiceContext serviceContext;

    public RequestMappingManagerTest() {
        serializer = new HttpJsonApiSerializer();
        serviceContext = mock(ServiceContext.class);
        mappingManager = new RequestMappingManager(serviceContext);
    }

    @Test
    public void testReadDPPById() throws InvalidRequestException {
        String dppId = DPP_1.getAAS().getId();
        Request expected = ReadDppByIdRequest.builder()
                .id(dppId)
                .dppSerializationMode(DppSerializationMode.DEFAULT)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("v1/dpps/" + EncodingHelper.base64UrlEncode(dppId))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testReadDPPByProductId() throws InvalidRequestException {
        String productId = "http://example.org/productId";
        Request expected = ReadDppByProductIdRequest.builder()
                .id(productId)
                .dppSerializationMode(DppSerializationMode.DEFAULT)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("v1/dppsByProductId/" + EncodingHelper.base64UrlEncode(productId))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testReadDPPIdsByProductIds() throws InvalidRequestException, SerializationException {
        List<String> productIds = List.of("http://example.org/productId1", "http://example.org/productId2");
        Request expected = ReadDppIdsByProductIdsRequest.builder()
                .productIds(productIds)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("v1/dppsByProductIds")
                .body(serializer.write(productIds))
                .build());
        Assert.assertEquals(expected, actual);
    }
}
