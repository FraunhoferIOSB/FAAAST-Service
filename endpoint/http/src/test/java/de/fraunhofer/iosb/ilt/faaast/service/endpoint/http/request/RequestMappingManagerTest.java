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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.MethodNotAllowedException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpConstants;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.TypedInMemoryFile;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.SetSubmodelElementValueByPathRequest;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.DeleteAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByAssetIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAssetAdministrationShellByIdReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.PostAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.PutAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasxfileserver.DeleteAASXPackageByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasxfileserver.GetAASXByPackageIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasxfileserver.GetAllAASXPackageIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasxfileserver.PostAASXPackageRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.DeleteConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsByDataSpecificationReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsByIsCaseOfRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.PostConceptDescriptionRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.PutConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.description.GetSelfDescriptionRequest;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.GetSubmodelByIdReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.GetSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.PatchSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.PostSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.PutSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import jakarta.json.Json;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetID;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetID;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


public class RequestMappingManagerTest {

    private static final AssetAdministrationShell AAS = AASFull.AAS_1;
    private static final String AASX_PACKAGE_ID = "examplePackageId";
    private static final List<SpecificAssetID> ASSET_IDENTIFIERS = Arrays.asList(
            new DefaultSpecificAssetID.Builder()
                    .name("globalAssetId")
                    .value("http://example.org")
                    .build(),
            new DefaultSpecificAssetID.Builder()
                    .name("foo")
                    .value("bar")
                    .build());
    private static final ConceptDescription CONCEPT_DESCRIPTION = AASFull.CONCEPT_DESCRIPTION_4;
    private static final String GLOBAL_ASSET_ID = "globalAssetId";
    private static final Submodel SUBMODEL = AASFull.SUBMODEL_3;
    private static final Operation OPERATION = (Operation) SUBMODEL.getSubmodelElements().stream().filter(x -> Operation.class.isAssignableFrom(x.getClass())).findFirst().get();
    private static final Reference OPERATION_REF = AasUtils.toReference(AasUtils.toReference(SUBMODEL), OPERATION);
    private static final SubmodelElement SUBMODEL_ELEMENT = AASFull.SUBMODEL_3.getSubmodelElements().get(0);
    private static final Reference SUBMODEL_ELEMENT_REF = AasUtils.toReference(AasUtils.toReference(SUBMODEL), SUBMODEL_ELEMENT);
    private final RequestMappingManager mappingManager;
    private final HttpJsonApiSerializer serializer;
    private final ServiceContext serviceContext;

    public RequestMappingManagerTest() {
        serializer = new HttpJsonApiSerializer();
        serviceContext = mock(ServiceContext.class);
        mappingManager = new RequestMappingManager(serviceContext);
    }


    @Test
    public void testDeleteAASXPackageById() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = DeleteAASXPackageByIdRequest.builder()
                .packageId(AASX_PACKAGE_ID)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("packages/" + EncodingHelper.base64UrlEncode(AASX_PACKAGE_ID))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteAllAssetLinksById() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = DeleteAllAssetLinksByIdRequest.builder()
                .id(AAS.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("lookup/shells/" + EncodingHelper.base64UrlEncode(AAS.getId()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteAssetAdministrationShellById() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = DeleteAssetAdministrationShellByIdRequest.builder()
                .id(AAS.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteConceptDescriptionById() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = DeleteConceptDescriptionByIdRequest.builder()
                .id(CONCEPT_DESCRIPTION.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("concept-descriptions/" + EncodingHelper.base64UrlEncode(CONCEPT_DESCRIPTION.getId()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteFileByPath() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = DeleteFileByPathRequest.builder()
                .submodelId(SUBMODEL.getId())
                .path(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements/"
                        + ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF) + "/attachment")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteSubmodelById() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = DeleteSubmodelByIdRequest.builder()
                .id(SUBMODEL.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteSubmodelElementByPath() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = DeleteSubmodelElementByPathRequest.builder()
                .submodelId(SUBMODEL.getId())
                .path(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements/"
                        + ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteSubmodelReference() throws InvalidRequestException, MethodNotAllowedException {
        Reference submodelRef = AasUtils.toReference(SUBMODEL);
        Request expected = DeleteSubmodelReferenceRequest.builder()
                .id(AAS.getId())
                .submodelRef(submodelRef)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()) + "/aas/submodels/"
                        + EncodingHelper.base64UrlEncode(SUBMODEL.getId()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteThumbnail() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = DeleteThumbnailRequest.builder()
                .id(AAS.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()) + "/aas/asset-information/thumbnail")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAASXByPackageId() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAASXByPackageIdRequest.builder()
                .packageId(AASX_PACKAGE_ID)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("packages/" + EncodingHelper.base64UrlEncode(AASX_PACKAGE_ID))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllAASXPackageIds() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAllAASXPackageIdsRequest.builder()
                .aasId(AAS.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("packages")
                .query("aasId=" + EncodingHelper.base64UrlEncode(AAS.getId()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllAssetAdministrationShellIdsByAssetLink() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAllAssetAdministrationShellIdsByAssetLinkRequest.builder()
                .assetIdentifierPairs(ASSET_IDENTIFIERS)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("lookup/shells")
                .query("assetIds=" + EncodingHelper.base64UrlEncode(serializer.write(ASSET_IDENTIFIERS)))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllAssetAdministrationShells() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAllAssetAdministrationShellsRequest.builder()
                .outputModifier(OutputModifier.DEFAULT)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllAssetAdministrationShellsReference() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAllAssetAdministrationShellsReferenceRequest.builder()
                .outputModifier(new OutputModifier.Builder()
                        .content(Content.REFERENCE)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/$reference")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllAssetAdministrationShellsByAssetId() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        List<SpecificAssetID> assetIds = Arrays.asList(new DefaultSpecificAssetID.Builder()
                .name(GLOBAL_ASSET_ID)
                .value(AAS.getAssetInformation().getGlobalAssetID())
                .build());

        Request expected = GetAllAssetAdministrationShellsByAssetIdRequest.builder()
                .outputModifier(OutputModifier.DEFAULT)
                .assetIds(assetIds)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells")
                .query("assetIds=" + EncodingHelper.base64UrlEncode(serializer.write(assetIds)))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllAssetAdministrationShellsByIdShort() throws InvalidRequestException, MethodNotAllowedException {
        String idShort = AAS.getIdShort();
        Request expected = GetAllAssetAdministrationShellsByIdShortRequest.builder()
                .outputModifier(OutputModifier.DEFAULT)
                .idShort(idShort)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells")
                .query("idShort=" + idShort)
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllAssetLinksById() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAllAssetLinksByIdRequest.builder()
                .id(AAS.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("lookup/shells/" + EncodingHelper.base64UrlEncode(AAS.getId()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllConceptDescriptions() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAllConceptDescriptionsRequest.builder()
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("concept-descriptions")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllConceptDescriptionsByDataSpecificationReference() throws InvalidRequestException, MethodNotAllowedException {
        Reference dataSpecificationRef = AasUtils.toReference(CONCEPT_DESCRIPTION);
        Request expected = GetAllConceptDescriptionsByDataSpecificationReferenceRequest.builder()
                .dataSpecification(dataSpecificationRef)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("concept-descriptions")
                .query("dataSpecificationRef=" + EncodingHelper.base64UrlEncode(AasUtils.asString(dataSpecificationRef)))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllConceptDescriptionsByIdShort() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAllConceptDescriptionsByIdShortRequest.builder()
                .idShort(CONCEPT_DESCRIPTION.getIdShort())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("concept-descriptions")
                .query("idShort=" + CONCEPT_DESCRIPTION.getIdShort())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllConceptDescriptionsByIsCaseOf() throws InvalidRequestException, MethodNotAllowedException {
        Reference isCaseOf = CONCEPT_DESCRIPTION.getIsCaseOf().get(0);
        Request expected = GetAllConceptDescriptionsByIsCaseOfRequest.builder()
                .isCaseOf(isCaseOf)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("concept-descriptions")
                .query("isCaseOf=" + EncodingHelper.base64UrlEncode(AasUtils.asString(isCaseOf)))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelElements() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAllSubmodelElementsRequest.builder()
                .submodelId(SUBMODEL.getId())
                .outputModifier(new OutputModifier.Builder()
                        .level(Level.DEEP)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements")
                .query("level=deep")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelElementsReference() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAllSubmodelElementsReferenceRequest.builder()
                .submodelId(SUBMODEL.getId())
                .outputModifier(new OutputModifier.Builder()
                        .content(Content.REFERENCE)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements/$reference")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelReferences() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAllSubmodelReferencesRequest.builder()
                .id(AAS.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()) + "/aas/submodels")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodels() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAllSubmodelsRequest.builder()
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelsReference() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAllSubmodelsReferenceRequest.builder()
                .outputModifier(new OutputModifier.Builder()
                        .content(Content.REFERENCE)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/$reference")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelsByIdShort() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAllSubmodelsByIdShortRequest.builder()
                .idShort(SUBMODEL.getIdShort())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels")
                .query("idShort=" + SUBMODEL.getIdShort())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelsBySemanticId() throws InvalidRequestException, MethodNotAllowedException, SerializationException {
        Request expected = GetAllSubmodelsBySemanticIdRequest.builder()
                .semanticId(SUBMODEL.getSemanticID())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels")
                .query("semanticId=" + EncodingHelper.base64UrlEncode(serializer.write(SUBMODEL.getSemanticID())))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAssetAdministrationShell() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAssetAdministrationShellRequest.builder()
                .id(AAS.getId())
                .outputModifier(new OutputModifier.Builder()
                        .content(Content.NORMAL)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()) + "/aas")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAssetAdministrationShellReference() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAssetAdministrationShellReferenceRequest.builder()
                .id(AAS.getId())
                .outputModifier(new OutputModifier.Builder()
                        .content(Content.REFERENCE)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()) + "/aas/$reference")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAssetAdministrationShellById() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAssetAdministrationShellByIdRequest.builder()
                .id(AAS.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAssetAdministrationShellByIdReference() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAssetAdministrationShellByIdReferenceRequest.builder()
                .id(AAS.getId())
                .outputModifier(new OutputModifier.Builder()
                        .content(Content.REFERENCE)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()) + "/$reference")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAssetInformation() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetAssetInformationRequest.builder()
                .id(AAS.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()) + "/aas/asset-information")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetConceptDescriptionById() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetConceptDescriptionByIdRequest.builder()
                .id(CONCEPT_DESCRIPTION.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("concept-descriptions/" + EncodingHelper.base64UrlEncode(CONCEPT_DESCRIPTION.getId()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetFileByPath() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetFileByPathRequest.builder()
                .submodelId(SUBMODEL.getId())
                .path(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements/"
                        + ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF) + "/attachment")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetOperationAsyncResult() throws InvalidRequestException, MethodNotAllowedException {
        final String handleId = UUID.randomUUID().toString();
        Request expected = GetOperationAsyncResultRequest.builder()
                .handle(OperationHandle.builder()
                        .handleId(handleId)
                        .build())
                .path(ReferenceHelper.toPath(OPERATION_REF))
                .submodelId(SUBMODEL.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements/"
                        + ReferenceHelper.toPath(OPERATION_REF)
                        + "/operation-results/" + EncodingHelper.base64UrlEncode(handleId))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetSelfDescription() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetSelfDescriptionRequest.builder()
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("description")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetSubmodel() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetSubmodelRequest.builder()
                .submodelId(SUBMODEL.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetSubmodelReference() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetSubmodelReferenceRequest.builder()
                .submodelId(SUBMODEL.getId())
                .outputModifier(new OutputModifier.Builder()
                        .content(Content.REFERENCE)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/$reference")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetSubmodelById() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetSubmodelByIdRequest.builder()
                .id(SUBMODEL.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetSubmodelByIdReference() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetSubmodelByIdReferenceRequest.builder()
                .id(SUBMODEL.getId())
                .outputModifier(new OutputModifier.Builder()
                        .content(Content.REFERENCE)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/$reference")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetSubmodelElementByPath() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetSubmodelElementByPathRequest.builder()
                .submodelId(SUBMODEL.getId())
                .path(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .outputModifier(new OutputModifier.Builder()
                        .level(Level.DEEP)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements/"
                        + ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .query("level=deep")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetSubmodelElementByPathReference() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetSubmodelElementByPathReferenceRequest.builder()
                .submodelId(SUBMODEL.getId())
                .path(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .outputModifier(new OutputModifier.Builder()
                        .content(Content.REFERENCE)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId())
                        + "/submodel/submodel-elements/" + ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF)
                        + "/$reference")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetThumbnail() throws InvalidRequestException, MethodNotAllowedException {
        Request expected = GetThumbnailRequest.builder()
                .id(AAS.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()) + "/aas/asset-information/thumbnail")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testInvalidSubURL() {
        Assert.assertThrows(InvalidRequestException.class, () -> mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()) + "/bogus")
                .build()));
    }


    @Test
    public void testInvokeOperationAsyncContentNormal() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = InvokeOperationAsyncRequest.builder()
                .submodelId(SUBMODEL.getId())
                .path(ReferenceHelper.toPath(OPERATION_REF))
                .inputArguments(OPERATION.getInputVariables())
                .inoutputArguments(OPERATION.getInoutputVariables())
                .content(Content.NORMAL)
                .build();

        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements/"
                        + ReferenceHelper.toPath(OPERATION_REF)
                        + "/invoke-async")
                .body(serializer.write(expected).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testInvokeOperationAsyncContentValue() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = InvokeOperationAsyncRequest.builder()
                .submodelId(SUBMODEL.getId())
                .path(ReferenceHelper.toPath(OPERATION_REF))
                .inputArguments(OPERATION.getInputVariables())
                .inoutputArguments(OPERATION.getInoutputVariables())
                .content(Content.VALUE)
                .build();

        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements/"
                        + ReferenceHelper.toPath(OPERATION_REF)
                        + "/invoke-async/$value")
                .body(serializer.write(expected).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testInvokeOperationSyncContentNormal() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = InvokeOperationSyncRequest.builder()
                .submodelId(SUBMODEL.getId())
                .path(ReferenceHelper.toPath(OPERATION_REF))
                .inputArguments(OPERATION.getInputVariables())
                .inoutputArguments(OPERATION.getInoutputVariables())
                .content(Content.NORMAL)
                .build();

        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements/"
                        + ReferenceHelper.toPath(OPERATION_REF)
                        + "/invoke")
                .body(serializer.write(expected).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testInvokeOperationSyncContentValue() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = InvokeOperationSyncRequest.builder()
                .submodelId(SUBMODEL.getId())
                .path(ReferenceHelper.toPath(OPERATION_REF))
                .inputArguments(OPERATION.getInputVariables())
                .inoutputArguments(OPERATION.getInoutputVariables())
                .content(Content.VALUE)
                .build();

        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements/"
                        + ReferenceHelper.toPath(OPERATION_REF)
                        + "/invoke/$value")
                .body(serializer.write(expected).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPatchSubmodel() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PatchSubmodelRequest.builder()
                .submodelId(SUBMODEL.getId())
                .changes(Json.createMergePatch(Json.createObjectBuilder()
                        .add("category", "NewCategory")
                        .build()))
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PATCH)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel")
                .body("{\"category\": \"NewCategory\"}".getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPatchSubmodelById() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PatchSubmodelByIdRequest.builder()
                .id(SUBMODEL.getId())
                .changes(Json.createMergePatch(Json.createObjectBuilder()
                        .add("category", "NewCategory")
                        .build()))
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PATCH)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()))
                .body("{\"category\": \"NewCategory\"}".getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPatchSubmodelElementByPath() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PatchSubmodelElementByPathRequest.builder()
                .submodelId(SUBMODEL.getId())
                .path(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .changes(Json.createMergePatch(Json.createObjectBuilder()
                        .add("category", "NewCategory")
                        .build()))
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PATCH)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements/"
                        + ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .body("{\"category\": \"NewCategory\"}".getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    @Ignore("AASX not implemented yet")
    public void testPostAASXPackage() throws IOException, InvalidRequestException, MethodNotAllowedException {
        Assert.fail("not implemented (multipart HTTP message");
        Request expected = PostAASXPackageRequest.builder()
                .aasId(AAS.getId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("packages")
                .query("aasId=" + EncodingHelper.base64UrlEncode(AAS.getId()))
                .build());
        Assert.assertEquals(expected, actual);

        //        File example = new File("src/test/resources/example-packages.json");
        //        execute(HttpMethod.POST, "packages",
        //                "",
        //                Files.readString(example.toPath()),
        //                PostAASXPackageRequest.class);
    }


    @Test
    public void testPostAllAssetLinksById() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PostAllAssetLinksByIdRequest.builder()
                .id(AAS.getId())
                .assetLinks(ASSET_IDENTIFIERS)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("lookup/shells/" + EncodingHelper.base64UrlEncode(AAS.getId()))
                .body(serializer.write(ASSET_IDENTIFIERS).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPostAssetAdministrationShell() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PostAssetAdministrationShellRequest.builder()
                .aas(AAS)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("shells")
                .body(serializer.write(AAS).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPostConceptDescription() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PostConceptDescriptionRequest.builder()
                .conceptDescription(CONCEPT_DESCRIPTION)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("concept-descriptions")
                .body(serializer.write(CONCEPT_DESCRIPTION).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPostSubmodel() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PostSubmodelRequest.builder()
                .submodel(SUBMODEL)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels")
                .body(serializer.write(SUBMODEL).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPostSubmodelElement() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PostSubmodelElementRequest.builder()
                .submodelId(SUBMODEL.getId())
                .submodelElement(SUBMODEL_ELEMENT)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements")
                .body(serializer.write(SUBMODEL_ELEMENT).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPostSubmodelElementByPath() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PostSubmodelElementByPathRequest.builder()
                .submodelId(SUBMODEL.getId())
                .path(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .submodelElement(SUBMODEL_ELEMENT)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements/"
                        + ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .body(serializer.write(SUBMODEL_ELEMENT).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPostSubmodelReference() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Reference submodelRef = AasUtils.toReference(SUBMODEL);
        Request expected = PostSubmodelReferenceRequest.builder()
                .id(AAS.getId())
                .submodelRef(submodelRef)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()) + "/aas/submodels")
                .body(serializer.write(submodelRef).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    @Ignore("AASX not implemented yet")
    public void testPutAASXPackageById() throws IOException, InvalidRequestException, MethodNotAllowedException {
        Assert.fail("not implemented (requires multipart HTTP)");
        Request expected = GetAASXByPackageIdRequest.builder()
                .packageId(AASX_PACKAGE_ID)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("packages/" + EncodingHelper.base64UrlEncode(AASX_PACKAGE_ID))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutAssetAdministrationShell() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PutAssetAdministrationShellRequest.builder()
                .id(AAS.getId())
                .aas(AAS)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()) + "/aas")
                .body(serializer.write(AAS).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutAssetAdministrationShellById() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PutAssetAdministrationShellByIdRequest.builder()
                .id(AAS.getId())
                .aas(AAS)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()))
                .body(serializer.write(AAS).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutAssetInformation() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PutAssetInformationRequest.builder()
                .id(AAS.getId())
                .assetInformation(AAS.getAssetInformation())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()) + "/aas/asset-information")
                .body(serializer.write(AAS.getAssetInformation()).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutConceptDescriptionById() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PutConceptDescriptionByIdRequest.builder()
                .id(CONCEPT_DESCRIPTION.getId())
                .conceptDescription(CONCEPT_DESCRIPTION)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("concept-descriptions/" + EncodingHelper.base64UrlEncode(CONCEPT_DESCRIPTION.getId()))
                .body(serializer.write(CONCEPT_DESCRIPTION).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    private byte[] generateMultipartBodyRandomFile(byte[] content, String fileName, ContentType contentType) throws IOException {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addTextBody("fileName", fileName, ContentType.TEXT_PLAIN);
        builder.addBinaryBody("file", content, contentType, fileName);
        builder.setBoundary("boundary");
        byte[] multipart = null;
        multipart = EntityUtils.toByteArray(builder.build());
        return multipart;
    }


    @Test
    public void testPutFileByPath() throws InvalidRequestException, IOException {
        byte[] content = new byte[20];
        new Random().nextBytes(content);
        byte[] multipart = generateMultipartBodyRandomFile(content, "test.pdf", ContentType.APPLICATION_PDF);
        String contentType = "multipart/form-data; boundary=boundary";
        Request expected = PutFileByPathRequest.builder()
                .submodelId(SUBMODEL.getId())
                .path(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .content(new TypedInMemoryFile.Builder()
                        .path("test.pdf")
                        .content(content)
                        .contentType(ContentType.APPLICATION_PDF.getMimeType())
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements/"
                        + ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF) + "/attachment")
                .header(HttpConstants.HEADER_CONTENT_TYPE, contentType)
                .body(multipart)
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutSubmodel() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PutSubmodelRequest.builder()
                .submodelId(SUBMODEL.getId())
                .submodel(SUBMODEL)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel")
                .body(serializer.write(SUBMODEL).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutSubmodelById() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PutSubmodelByIdRequest.builder()
                .id(SUBMODEL.getId())
                .submodel(SUBMODEL)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()))
                .body(serializer.write(SUBMODEL).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutSubmodelElementByPath() throws SerializationException, InvalidRequestException, MethodNotAllowedException {
        Request expected = PutSubmodelElementByPathRequest.builder()
                .submodelId(SUBMODEL.getId())
                .path(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .submodelElement(SUBMODEL_ELEMENT)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId()) + "/submodel/submodel-elements/"
                        + ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .body(serializer.write(SUBMODEL_ELEMENT).getBytes())
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutThumbnail() throws SerializationException, InvalidRequestException, MethodNotAllowedException, IOException {
        byte[] content = new byte[20];
        new Random().nextBytes(content);
        byte[] generated = generateMultipartBodyRandomFile(content, "test.png", ContentType.IMAGE_PNG);
        String contentType = "multipart/form-data; boundary=boundary";
        Request expected = PutThumbnailRequest.builder()
                .id(AAS.getId())
                .content(new TypedInMemoryFile.Builder()
                        .path("test.png")
                        .content(content)
                        .contentType(ContentType.IMAGE_PNG.getMimeType())
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getId()) + "/aas/asset-information/thumbnail")
                .body(generated)
                .header(HttpConstants.HEADER_CONTENT_TYPE, contentType)
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSetSubmodelElementValueByPathContentNormal() throws Exception {
        SetSubmodelElementValueByPathRequest expected = SetSubmodelElementValueByPathRequest.<String> builder()
                .submodelId(SUBMODEL.getId())
                .path(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .build();
        when(serviceContext.getTypeInfo(any())).thenReturn(TypeExtractor.extractTypeInfo(SUBMODEL_ELEMENT));
        Request temp = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PATCH)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId())
                        + "/submodel/submodel-elements/" + ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF)
                        + "/$value")
                .body(serializer.write(SUBMODEL_ELEMENT).getBytes())
                .build());
        SetSubmodelElementValueByPathRequest actual = (SetSubmodelElementValueByPathRequest) temp;
        Assert.assertEquals(expected.getSubmodelId(), actual.getSubmodelId());
        Assert.assertEquals(expected.getPath(), actual.getPath());
        Assert.assertEquals(ElementValueMapper.toValue(SUBMODEL_ELEMENT), actual.getValueParser().parse(actual.getRawValue(), ElementValue.class));
    }


    @Test
    public void testSetSubmodelElementValueByPathContentValue() throws Exception {
        SetSubmodelElementValueByPathRequest expected = SetSubmodelElementValueByPathRequest.<String> builder()
                .submodelId(SUBMODEL.getId())
                .path(ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF))
                .build();
        when(serviceContext.getTypeInfo(any())).thenReturn(TypeExtractor.extractTypeInfo(SUBMODEL_ELEMENT));
        Request temp = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PATCH)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getId())
                        + "/submodel/submodel-elements/" + ReferenceHelper.toPath(SUBMODEL_ELEMENT_REF)
                        + "/$value")
                .body(serializer.write(ElementValueMapper.toValue(SUBMODEL_ELEMENT)).getBytes())
                .build());
        SetSubmodelElementValueByPathRequest actual = (SetSubmodelElementValueByPathRequest) temp;
        Assert.assertEquals(expected.getSubmodelId(), actual.getSubmodelId());
        Assert.assertEquals(expected.getPath(), actual.getPath());
        Assert.assertEquals(ElementValueMapper.toValue(SUBMODEL_ELEMENT), actual.getValueParser().parse(actual.getRawValue(), ElementValue.class));
    }


    @Test
    public void testUnknownPath() {
        Assert.assertThrows(InvalidRequestException.class, () -> mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("foo")
                .build()));
    }


    @Test
    public void testUnsupportedOperation() {
        Assert.assertThrows(MethodNotAllowedException.class, () -> mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PATCH)
                .path("shells")
                .build()));
    }

}
