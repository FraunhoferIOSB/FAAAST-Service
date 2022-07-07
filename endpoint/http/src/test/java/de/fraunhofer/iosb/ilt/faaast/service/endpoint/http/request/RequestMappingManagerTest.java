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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteAASXPackageByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteAllAssetLinksByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteSubmodelReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAASXByPackageIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAASXPackageIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAssetAdministrationShellIdsByAssetLinkRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAssetAdministrationShellsByAssetIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAssetAdministrationShellsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAssetAdministrationShellsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAssetLinksByIdRequest;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetOperationAsyncResultRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationAsyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PostAASXPackageRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PostAllAssetLinksByIdRequest;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeExtractor;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementPathHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultIdentifierKeyValuePair;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


public class RequestMappingManagerTest {

    private static final AssetAdministrationShell AAS = AASFull.AAS_1;
    private static final String AASX_PACKAGE_ID = "examplePackageId";
    private static final List<IdentifierKeyValuePair> ASSET_IDENTIFIERS = Arrays.asList(
            new DefaultIdentifierKeyValuePair.Builder()
                    .key("globalAssetId")
                    .value("http://example.org")
                    .build(),
            new DefaultIdentifierKeyValuePair.Builder()
                    .key("foo")
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
    private final HttpJsonSerializer serializer;
    private final ServiceContext serviceContext;

    public RequestMappingManagerTest() {
        serializer = new HttpJsonSerializer();
        serviceContext = mock(ServiceContext.class);
        mappingManager = new RequestMappingManager(serviceContext);
    }


    @Test
    public void testDeleteAASXPackageByIdRequest() throws InvalidRequestException {
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
    public void testDeleteAllAssetLinksByIdRequest() throws InvalidRequestException {
        Request expected = DeleteAllAssetLinksByIdRequest.builder()
                .id(AAS.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("lookup/shells/" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteAssetAdministrationShellById() throws InvalidRequestException {
        Request expected = DeleteAssetAdministrationShellByIdRequest.builder()
                .id(AAS.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteConceptDescriptionById() throws InvalidRequestException {
        Request expected = DeleteConceptDescriptionByIdRequest.builder()
                .id(CONCEPT_DESCRIPTION.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("concept-descriptions/" + EncodingHelper.base64UrlEncode(CONCEPT_DESCRIPTION.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteSubmodelById() throws InvalidRequestException {
        Request expected = DeleteSubmodelByIdRequest.builder()
                .id(SUBMODEL.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteSubmodelElementByPath() throws SerializationException, InvalidRequestException {
        Request expected = DeleteSubmodelElementByPathRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ReferenceHelper.toKeys(SUBMODEL_ELEMENT_REF))
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathHelper.toElementPath(SUBMODEL_ELEMENT_REF))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteSubmodelReference() throws InvalidRequestException {
        Reference submodelRef = AasUtils.toReference(SUBMODEL);
        Request expected = DeleteSubmodelReferenceRequest.builder()
                .id(AAS.getIdentification())
                .submodelRef(submodelRef)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()) + "/aas/submodels/"
                        + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAASXByPackageIdRequest() throws InvalidRequestException {
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
    public void testGetAllAASXPackageIdsRequest() throws InvalidRequestException {
        Request expected = GetAllAASXPackageIdsRequest.builder()
                .aasId(AAS.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("packages")
                .query("aasId=" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllAssetAdministrationShellIdsByAssetLinkRequest() throws SerializationException, InvalidRequestException {
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
    public void testGetAllAssetAdministrationShellsByAssetId() throws SerializationException, InvalidRequestException {
        List<IdentifierKeyValuePair> assetIds = Arrays.asList(new DefaultIdentifierKeyValuePair.Builder()
                .key(GLOBAL_ASSET_ID)
                .value(AAS.getAssetInformation().getGlobalAssetId().getKeys().get(0).getValue())
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
    public void testGetAllAssetAdministrationShellsByIdShort() throws InvalidRequestException {
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
    public void testGetAllAssetAdministrationShellsRequest() throws InvalidRequestException {
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
    public void testGetAllAssetLinksByIdRequest() throws InvalidRequestException {
        Request expected = GetAllAssetLinksByIdRequest.builder()
                .id(AAS.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("lookup/shells/" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllConceptDescriptions() throws InvalidRequestException {
        Request expected = GetAllConceptDescriptionsRequest.builder()
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("concept-descriptions")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllConceptDescriptionsByDataSpecificationReference() throws InvalidRequestException {
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
    public void testGetAllConceptDescriptionsByIdShort() throws InvalidRequestException {
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
    public void testGetAllConceptDescriptionsByIsCaseOf() throws InvalidRequestException {
        Reference isCaseOf = CONCEPT_DESCRIPTION.getIsCaseOfs().get(0);
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
    public void testGetAllSubmodelElements() throws InvalidRequestException {
        Request expected = GetAllSubmodelElementsRequest.builder()
                .id(SUBMODEL.getIdentification())
                .outputModifier(new OutputModifier.Builder()
                        .level(Level.DEEP)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements")
                .query("level=deep")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodels() throws InvalidRequestException {
        Request expected = GetAllSubmodelsRequest.builder()
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelsByIdShort() throws InvalidRequestException {
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
    public void testGetAllSubmodelsBySemanticId() throws InvalidRequestException, SerializationException {
        Request expected = GetAllSubmodelsBySemanticIdRequest.builder()
                .semanticId(SUBMODEL.getSemanticId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels")
                .query("semanticId=" + EncodingHelper.base64UrlEncode(serializer.write(SUBMODEL.getSemanticId())))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAssetAdministrationShell() throws InvalidRequestException {
        Request expected = GetAssetAdministrationShellRequest.builder()
                .id(AAS.getIdentification())
                .outputModifier(new OutputModifier.Builder()
                        .content(Content.NORMAL)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()) + "/aas")
                .query("content=normal")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAssetAdministrationShellById() throws InvalidRequestException {
        Request expected = GetAssetAdministrationShellByIdRequest.builder()
                .id(AAS.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAssetInformationRequest() throws InvalidRequestException {
        Request expected = GetAssetInformationRequest.builder()
                .id(AAS.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()) + "/aas/asset-information")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetConceptDescriptionById() throws InvalidRequestException {
        Request expected = GetConceptDescriptionByIdRequest.builder()
                .id(CONCEPT_DESCRIPTION.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("concept-descriptions/" + EncodingHelper.base64UrlEncode(CONCEPT_DESCRIPTION.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetOperationAsyncResult() throws InvalidRequestException {
        final String handleId = UUID.randomUUID().toString();
        Request expected = GetOperationAsyncResultRequest.builder()
                .handleId(handleId)
                .path(ReferenceHelper.toKeys(OPERATION_REF))
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathHelper.toElementPath(OPERATION_REF)
                        + "/operation-results/" + EncodingHelper.base64UrlEncode(handleId))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetSubmodel() throws InvalidRequestException {
        Request expected = GetSubmodelRequest.builder()
                .id(SUBMODEL.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetSubmodelByIdRequest() throws InvalidRequestException {
        Request expected = GetSubmodelByIdRequest.builder()
                .id(SUBMODEL.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetSubmodelElementByPath() throws InvalidRequestException {
        Request expected = GetSubmodelElementByPathRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ReferenceHelper.toKeys(SUBMODEL_ELEMENT_REF))
                .outputModifier(new OutputModifier.Builder()
                        .level(Level.DEEP)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathHelper.toElementPath(SUBMODEL_ELEMENT_REF))
                .query("level=deep")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testInvokeOperationAsync() throws IOException {
        //        Reference submodelElementRef = AasUtils.toReference(AasUtils.toReference(AASFull.SUBMODEL_3), AASFull.SUBMODEL_3.getSubmodelElements().get(2));
        //        File example = new File("src/test/resources/example-invoke.json");
        //        execute(HttpMethod.POST,
        //                "submodels/bXktYWFzLXRlc3QtaWRlbnRpZmllcg==/submodel/submodel-elements/MySubmodelElementStruct.MySubSubmodelElementList%5B1%5D/invoke",
        //                "submodels/" + EncodingUtils.base64UrlEncode(AASFull.SUBMODEL_1.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
        //                + ElementPathUtils.toElementPath(submodelElementRef) + "/invoke", // does url encode happen automatically?
        //                "async=true",
        //                Files.readString(example.toPath()),
        //                InvokeOperationAsyncRequest.class);
        Assert.assertTrue(true);
    }


    @Test
    public void testInvokeOperationAsyncContentNormal() throws SerializationException, InvalidRequestException {
        Request expected = InvokeOperationAsyncRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ReferenceHelper.toKeys(OPERATION_REF))
                .inputArguments(OPERATION.getInputVariables())
                .inoutputArguments(OPERATION.getInoutputVariables())
                .content(Content.NORMAL)
                .build();

        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathHelper.toElementPath(OPERATION_REF)
                        + "/invoke")
                .query("async=true")
                .body(serializer.write(expected))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testInvokeOperationAsyncContentValue() throws SerializationException, InvalidRequestException {
        Request expected = InvokeOperationAsyncRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ReferenceHelper.toKeys(OPERATION_REF))
                .inputArguments(OPERATION.getInputVariables())
                .inoutputArguments(OPERATION.getInoutputVariables())
                .content(Content.VALUE)
                .build();

        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathHelper.toElementPath(OPERATION_REF)
                        + "/invoke")
                .query("content=value&async=true")
                .body(serializer.write(expected))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testInvokeOperationSyncContentNormal() throws SerializationException, InvalidRequestException {
        Request expected = InvokeOperationSyncRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ReferenceHelper.toKeys(OPERATION_REF))
                .inputArguments(OPERATION.getInputVariables())
                .inoutputArguments(OPERATION.getInoutputVariables())
                .content(Content.NORMAL)
                .build();

        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathHelper.toElementPath(OPERATION_REF)
                        + "/invoke")
                .body(serializer.write(expected))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testInvokeOperationSyncContentValue() throws SerializationException, InvalidRequestException {
        Request expected = InvokeOperationSyncRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ReferenceHelper.toKeys(OPERATION_REF))
                .inputArguments(OPERATION.getInputVariables())
                .inoutputArguments(OPERATION.getInoutputVariables())
                .content(Content.VALUE)
                .build();

        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathHelper.toElementPath(OPERATION_REF)
                        + "/invoke")
                .query("content=value")
                .body(serializer.write(expected))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testMappingGetAllSubmodelReferences() throws InvalidRequestException {
        Request expected = GetAllSubmodelReferencesRequest.builder()
                .id(AAS.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()) + "/aas/submodels")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    @Ignore("AASX not implemented yet")
    public void testPostAASXPackageRequest() throws IOException, InvalidRequestException {
        Assert.fail("not implemented (multipart HTTP message");
        Request expected = PostAASXPackageRequest.builder()
                .aasId(AAS.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("packages")
                .query("aasId=" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);

        //        File example = new File("src/test/resources/example-packages.json");
        //        execute(HttpMethod.POST, "packages",
        //                "",
        //                Files.readString(example.toPath()),
        //                PostAASXPackageRequest.class);
    }


    @Test
    public void testPostAllAssetLinksByIdRequest() throws SerializationException, InvalidRequestException {
        Request expected = PostAllAssetLinksByIdRequest.builder()
                .id(AAS.getIdentification())
                .assetLinks(ASSET_IDENTIFIERS)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("lookup/shells/" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .body(serializer.write(ASSET_IDENTIFIERS))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPostAssetAdministrationShell() throws SerializationException, InvalidRequestException {
        Request expected = PostAssetAdministrationShellRequest.builder()
                .aas(AAS)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("shells")
                .body(serializer.write(AAS))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPostConceptDescription() throws SerializationException, InvalidRequestException {
        Request expected = PostConceptDescriptionRequest.builder()
                .conceptDescription(CONCEPT_DESCRIPTION)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("concept-descriptions")
                .body(serializer.write(CONCEPT_DESCRIPTION))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPostSubmodel() throws SerializationException, InvalidRequestException {
        Request expected = PostSubmodelRequest.builder()
                .submodel(SUBMODEL)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels")
                .body(serializer.write(SUBMODEL))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPostSubmodelElement() throws SerializationException, InvalidRequestException {
        Request expected = PostSubmodelElementRequest.builder()
                .id(SUBMODEL.getIdentification())
                .submodelElement(SUBMODEL_ELEMENT)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements")
                .body(serializer.write(SUBMODEL_ELEMENT))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPostSubmodelElementByPath() throws SerializationException, InvalidRequestException {
        Request expected = PostSubmodelElementByPathRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ReferenceHelper.toKeys(SUBMODEL_ELEMENT_REF))
                .submodelElement(SUBMODEL_ELEMENT)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathHelper.toElementPath(SUBMODEL_ELEMENT_REF))
                .body(serializer.write(SUBMODEL_ELEMENT))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPostSubmodelReference() throws SerializationException, InvalidRequestException {
        Reference submodelRef = AasUtils.toReference(SUBMODEL);
        Request expected = PostSubmodelReferenceRequest.builder()
                .id(AAS.getIdentification())
                .submodelRef(submodelRef)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()) + "/aas/submodels")
                .body(serializer.write(submodelRef))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    @Ignore("AASX not implemented yet")
    public void testPutAASXPackageByIdRequest() throws IOException, InvalidRequestException {
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
    public void testPutAssetAdministrationShell() throws SerializationException, InvalidRequestException {
        Request expected = PutAssetAdministrationShellRequest.builder()
                .id(AAS.getIdentification())
                .aas(AAS)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()) + "/aas")
                .body(serializer.write(AAS))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutAssetAdministrationShellById() throws SerializationException, InvalidRequestException {
        Request expected = PutAssetAdministrationShellByIdRequest.builder()
                .id(AAS.getIdentification())
                .aas(AAS)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .body(serializer.write(AAS))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutAssetInformationRequest() throws SerializationException, InvalidRequestException {
        Request expected = PutAssetInformationRequest.builder()
                .id(AAS.getIdentification())
                .assetInformation(AAS.getAssetInformation())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()) + "/aas/asset-information")
                .body(serializer.write(AAS.getAssetInformation()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutConceptDescriptionById() throws SerializationException, InvalidRequestException {
        Request expected = PutConceptDescriptionByIdRequest.builder()
                .id(CONCEPT_DESCRIPTION.getIdentification())
                .conceptDescription(CONCEPT_DESCRIPTION)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("concept-descriptions/" + EncodingHelper.base64UrlEncode(CONCEPT_DESCRIPTION.getIdentification().getIdentifier()))
                .body(serializer.write(CONCEPT_DESCRIPTION))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutSubmodel() throws SerializationException, InvalidRequestException {
        Request expected = PutSubmodelRequest.builder()
                .id(SUBMODEL.getIdentification())
                .submodel(SUBMODEL)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel")
                .body(serializer.write(SUBMODEL))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutSubmodelById() throws SerializationException, InvalidRequestException {
        Request expected = PutSubmodelByIdRequest.builder()
                .id(SUBMODEL.getIdentification())
                .submodel(SUBMODEL)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()))
                .body(serializer.write(SUBMODEL))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutSubmodelElementByPath() throws SerializationException, InvalidRequestException {
        Request expected = PutSubmodelElementByPathRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ReferenceHelper.toKeys(SUBMODEL_ELEMENT_REF))
                .submodelElement(SUBMODEL_ELEMENT)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathHelper.toElementPath(SUBMODEL_ELEMENT_REF))
                .body(serializer.write(SUBMODEL_ELEMENT))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testSetSubmodelElementValueByPathContentNormal() throws SerializationException, InvalidRequestException, Exception {
        SetSubmodelElementValueByPathRequest expected = SetSubmodelElementValueByPathRequest.<String> builder()
                .id(SUBMODEL.getIdentification())
                .path(ReferenceHelper.toKeys(SUBMODEL_ELEMENT_REF))
                .build();
        when(serviceContext.getTypeInfo(any())).thenReturn(TypeExtractor.extractTypeInfo(SUBMODEL_ELEMENT));
        Request temp = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathHelper.toElementPath(SUBMODEL_ELEMENT_REF))
                .query("content=value")
                .body(serializer.write(SUBMODEL_ELEMENT))
                .build());
        SetSubmodelElementValueByPathRequest actual = (SetSubmodelElementValueByPathRequest) temp;
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getPath(), actual.getPath());
        Assert.assertEquals(ElementValueMapper.toValue(SUBMODEL_ELEMENT), actual.getValueParser().parse(actual.getRawValue(), SubmodelElement.class));
    }


    @Test
    public void testSetSubmodelElementValueByPathContentValue() throws SerializationException, InvalidRequestException, Exception {
        SetSubmodelElementValueByPathRequest expected = SetSubmodelElementValueByPathRequest.<String> builder()
                .id(SUBMODEL.getIdentification())
                .path(ReferenceHelper.toKeys(SUBMODEL_ELEMENT_REF))
                .build();
        when(serviceContext.getTypeInfo(any())).thenReturn(TypeExtractor.extractTypeInfo(SUBMODEL_ELEMENT));
        Request temp = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("submodels/" + EncodingHelper.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathHelper.toElementPath(SUBMODEL_ELEMENT_REF))
                .query("content=value")
                .body(serializer.write(ElementValueMapper.toValue(SUBMODEL_ELEMENT)))
                .build());
        SetSubmodelElementValueByPathRequest actual = (SetSubmodelElementValueByPathRequest) temp;
        Assert.assertEquals(expected.getId(), actual.getId());
        Assert.assertEquals(expected.getPath(), actual.getPath());
        Assert.assertEquals(ElementValueMapper.toValue(SUBMODEL_ELEMENT), actual.getValueParser().parse(actual.getRawValue(), ElementValue.class));
    }


    @Test(expected = InvalidRequestException.class)
    public void testUnknownPath() throws InvalidRequestException {
        mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("foo")
                .build());
    }


    @Test(expected = InvalidRequestException.class)
    public void testInvalidSubURL() throws InvalidRequestException {
        mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingHelper.base64UrlEncode(AAS.getIdentification().getIdentifier()) + "/bogus")
                .build());
    }
}
