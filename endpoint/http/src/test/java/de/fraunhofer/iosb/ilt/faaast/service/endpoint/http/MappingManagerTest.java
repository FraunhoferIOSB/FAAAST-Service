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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http;

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.http.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.utils.ElementPathUtils;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.utils.EncodingUtils;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.DeleteAASXPackageByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.DeleteAllAssetLinksByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.DeleteAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.DeleteConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.DeleteSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.DeleteSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.DeleteSubmodelReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAASXByPackageIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllAASXPackageIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllAssetAdministrationShellIdsByAssetLinkRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllAssetAdministrationShellsByAssetIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllAssetAdministrationShellsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllAssetAdministrationShellsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllAssetLinksByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllConceptDescriptionsByDataSpecificationReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllConceptDescriptionsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllConceptDescriptionsByIsCaseOfRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllConceptDescriptionsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllSubmodelElementsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllSubmodelReferencesRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllSubmodelsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllSubmodelsBySemanticIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAllSubmodelsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetAssetInformationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetOperationAsyncResultRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.GetSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.InvokeOperationAsyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.PostAASXPackageRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.PostAllAssetLinksByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.PostAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.PostConceptDescriptionRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.PostSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.PostSubmodelElementRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.PostSubmodelReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.PostSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.PutAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.PutAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.PutAssetInformationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.PutConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.PutSubmodelByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.PutSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.PutSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.request.SetSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.SerializationException;
import io.adminshell.aas.v3.dataformat.core.AASFull;
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


public class MappingManagerTest {

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
    private final MappingManager mappingManager;
    private final HttpJsonSerializer serializer;

    public MappingManagerTest() {
        this.serializer = new HttpJsonSerializer();
        this.mappingManager = new MappingManager();
    }


    @Test
    public void testDeleteAASXPackageByIdRequest() {
        Request expected = DeleteAASXPackageByIdRequest.builder()
                .packageId(AASX_PACKAGE_ID)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("packages/" + EncodingUtils.base64UrlEncode(AASX_PACKAGE_ID))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteAllAssetLinksByIdRequest() {
        Request expected = DeleteAllAssetLinksByIdRequest.builder()
                .aasIdentifier(AAS.getIdentification().getIdentifier())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("lookup/shells/" + EncodingUtils.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteAssetAdministrationShellById() {
        Request expected = DeleteAssetAdministrationShellByIdRequest.builder()
                .id(AAS.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("shells/" + EncodingUtils.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteConceptDescriptionById() {
        Request expected = DeleteConceptDescriptionByIdRequest.builder()
                .id(CONCEPT_DESCRIPTION.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("concept-descriptions/" + EncodingUtils.base64UrlEncode(CONCEPT_DESCRIPTION.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteSubmodelById() {
        Request expected = DeleteSubmodelByIdRequest.builder()
                .id(SUBMODEL.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteSubmodelElementByPath() throws SerializationException {
        Request expected = DeleteSubmodelElementByPathRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ElementPathUtils.extractElementPath(SUBMODEL_ELEMENT_REF))
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathUtils.toElementPath(SUBMODEL_ELEMENT_REF))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testDeleteSubmodelReference() {
        Reference submodelRef = AasUtils.toReference(SUBMODEL);
        Request expected = DeleteSubmodelReferenceRequest.builder()
                .id(AAS.getIdentification())
                .submodelRef(submodelRef)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.DELETE)
                .path("shells/" + EncodingUtils.base64UrlEncode(AAS.getIdentification().getIdentifier()) + "/aas/submodels/"
                        + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAASXByPackageIdRequest() {
        Request expected = GetAASXByPackageIdRequest.builder()
                .packageId(AASX_PACKAGE_ID)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("packages/" + EncodingUtils.base64UrlEncode(AASX_PACKAGE_ID))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllAASXPackageIdsRequest() {
        Request expected = GetAllAASXPackageIdsRequest.builder()
                .aasId(AAS.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("packages")
                .query("aasId=" + EncodingUtils.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllAssetAdministrationShellIdsByAssetLinkRequest() throws SerializationException {
        Request expected = GetAllAssetAdministrationShellIdsByAssetLinkRequest.builder()
                .assetIdentifierPairs(ASSET_IDENTIFIERS)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("lookup/shells")
                .query("assetIds=" + EncodingUtils.base64UrlEncode(serializer.write(ASSET_IDENTIFIERS)))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllAssetAdministrationShellsByAssetId() throws SerializationException {
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
                .query("assetIds=" + EncodingUtils.base64UrlEncode(serializer.write(assetIds)))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllAssetAdministrationShellsByIdShort() {
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
    public void testGetAllAssetAdministrationShellsRequest() {
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
    public void testGetAllAssetLinksByIdRequest() {
        Request expected = GetAllAssetLinksByIdRequest.builder()
                .aasIdentifier(AAS.getIdentification().getIdentifier())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("lookup/shells/" + EncodingUtils.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllConceptDescriptions() {
        Request expected = GetAllConceptDescriptionsRequest.builder()
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("concept-descriptions")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllConceptDescriptionsByDataSpecificationReference() {
        Reference dataSpecificationRef = AasUtils.toReference(CONCEPT_DESCRIPTION);
        Request expected = GetAllConceptDescriptionsByDataSpecificationReferenceRequest.builder()
                .dataSpecification(dataSpecificationRef)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("concept-descriptions")
                .query("dataSpecificationRef=" + EncodingUtils.base64UrlEncode(AasUtils.asString(dataSpecificationRef)))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllConceptDescriptionsByIdShort() {
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
    public void testGetAllConceptDescriptionsByIsCaseOf() {
        Reference isCaseOf = CONCEPT_DESCRIPTION.getIsCaseOfs().get(0);
        Request expected = GetAllConceptDescriptionsByIsCaseOfRequest.builder()
                .isCaseOf(isCaseOf)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("concept-descriptions")
                .query("isCaseOf=" + EncodingUtils.base64UrlEncode(AasUtils.asString(isCaseOf)))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelElements() {
        Request expected = GetAllSubmodelElementsRequest.builder()
                .id(SUBMODEL.getIdentification())
                .outputModifier(new OutputModifier.Builder()
                        .level(Level.Deep)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements")
                .query("level=deep")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodels() {
        Request expected = GetAllSubmodelsRequest.builder()
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAllSubmodelsByIdShort() {
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
    public void testGetAllSubmodelsBySemanticId() {
        Request expected = GetAllSubmodelsBySemanticIdRequest.builder()
                .semanticId(SUBMODEL.getSemanticId())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels")
                .query("semanticId=" + EncodingUtils.base64UrlEncode(AasUtils.asString(SUBMODEL.getSemanticId())))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAssetAdministrationShell() {
        Request expected = GetAssetAdministrationShellRequest.builder()
                .id(AAS.getIdentification())
                .outputModifier(new OutputModifier.Builder()
                        .content(Content.Value)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingUtils.base64UrlEncode(AAS.getIdentification().getIdentifier()) + "/aas")
                .query("content=value")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAssetAdministrationShellById() {
        Request expected = GetAssetAdministrationShellByIdRequest.builder()
                .id(AAS.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingUtils.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAssetInformationRequest() {
        Request expected = GetAssetInformationRequest.builder()
                .id(AAS.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingUtils.base64UrlEncode(AAS.getIdentification().getIdentifier()) + "/aas/asset-information")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetConceptDescriptionById() {
        Request expected = GetConceptDescriptionByIdRequest.builder()
                .id(CONCEPT_DESCRIPTION.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("concept-descriptions/" + EncodingUtils.base64UrlEncode(CONCEPT_DESCRIPTION.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetOperationAsyncResult() {
        final String handleId = UUID.randomUUID().toString();
        Request expected = GetOperationAsyncResultRequest.builder()
                .handleId(handleId)
                .path(ElementPathUtils.extractElementPath(OPERATION_REF))
                .build();

        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathUtils.toElementPath(OPERATION_REF)
                        + "/operation-results/" + EncodingUtils.base64UrlEncode(handleId))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetSubmodel() {
        Request expected = GetSubmodelRequest.builder()
                .id(SUBMODEL.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetSubmodelByIdRequest() {
        Request expected = GetSubmodelByIdRequest.builder()
                .id(SUBMODEL.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetSubmodelElementByPath() {
        Request expected = GetSubmodelElementByPathRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ElementPathUtils.extractElementPath(SUBMODEL_ELEMENT_REF))
                .outputModifier(new OutputModifier.Builder()
                        .level(Level.Deep)
                        .build())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathUtils.toElementPath(SUBMODEL_ELEMENT_REF))
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
    }


    @Test
    public void testInvokeOperationAsync_Normal() throws SerializationException {
        Request expected = InvokeOperationAsyncRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ElementPathUtils.extractElementPath(OPERATION_REF))
                .inputArguments(OPERATION.getInputVariables())
                .inoutputArguments(OPERATION.getInoutputVariables())
                .content(Content.Normal)
                .build();

        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathUtils.toElementPath(OPERATION_REF)
                        + "/invoke")
                .query("async=true")
                .body(serializer.write(expected))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    @Ignore
    public void testInvokeOperationAsync_Value() throws SerializationException {
        Assert.fail("not implemented");
        Request expected = InvokeOperationAsyncRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ElementPathUtils.extractElementPath(OPERATION_REF))
                .inputArguments(OPERATION.getInputVariables())
                .inoutputArguments(OPERATION.getInoutputVariables())
                .content(Content.Value)
                .build();

        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathUtils.toElementPath(OPERATION_REF)
                        + "/invoke")
                .query("content=value&async=true")
                .body(serializer.write(expected))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testInvokeOperationSync_Normal() throws SerializationException {
        Request expected = InvokeOperationSyncRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ElementPathUtils.extractElementPath(OPERATION_REF))
                .inputArguments(OPERATION.getInputVariables())
                .inoutputArguments(OPERATION.getInoutputVariables())
                .content(Content.Normal)
                .build();

        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathUtils.toElementPath(OPERATION_REF)
                        + "/invoke")
                .body(serializer.write(expected))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    @Ignore
    public void testInvokeOperationSync_Value() throws SerializationException {
        Assert.fail("not implemented");
        Request expected = InvokeOperationSyncRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ElementPathUtils.extractElementPath(OPERATION_REF))
                .inputArguments(OPERATION.getInputVariables())
                .inoutputArguments(OPERATION.getInoutputVariables())
                .content(Content.Value)
                .build();

        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathUtils.toElementPath(OPERATION_REF)
                        + "/invoke")
                .query("content=value")
                .body(serializer.write(expected))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testMappingGetAllSubmodelReferences() {
        Request expected = GetAllSubmodelReferencesRequest.builder()
                .id(AAS.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("shells/" + EncodingUtils.base64UrlEncode(AAS.getIdentification().getIdentifier()) + "/aas/submodels")
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    @Ignore
    public void testPostAASXPackageRequest() throws IOException {
        Assert.fail("not implemented (multipart HTTP message");
        Request expected = PostAASXPackageRequest.builder()
                .aasId(AAS.getIdentification())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("packages")
                .query("aasId=" + EncodingUtils.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .build());
        Assert.assertEquals(expected, actual);

        //        File example = new File("src/test/resources/example-packages.json");
        //        execute(HttpMethod.POST, "packages",
        //                "",
        //                Files.readString(example.toPath()),
        //                PostAASXPackageRequest.class);
    }


    @Test
    public void testPostAllAssetLinksByIdRequest() throws SerializationException {
        Request expected = PostAllAssetLinksByIdRequest.builder()
                .aasIdentifier(AAS.getIdentification().getIdentifier())
                .assetLinks(ASSET_IDENTIFIERS)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("lookup/shells/" + EncodingUtils.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .body(serializer.write(ASSET_IDENTIFIERS))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPostAssetAdministrationShell() throws SerializationException {
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
    public void testPostConceptDescription() throws SerializationException {
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
    public void testPostSubmodel() throws SerializationException {
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
    public void testPostSubmodelElement() throws SerializationException {
        Request expected = PostSubmodelElementRequest.builder()
                .id(SUBMODEL.getIdentification())
                .submodelElement(SUBMODEL_ELEMENT)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements")
                .body(serializer.write(SUBMODEL_ELEMENT))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPostSubmodelElementByPath() throws SerializationException {
        Request expected = PostSubmodelElementByPathRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ElementPathUtils.extractElementPath(SUBMODEL_ELEMENT_REF))
                .submodelElement(SUBMODEL_ELEMENT)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathUtils.toElementPath(SUBMODEL_ELEMENT_REF))
                .body(serializer.write(SUBMODEL_ELEMENT))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPostSubmodelReference() throws SerializationException {
        Reference submodelRef = AasUtils.toReference(SUBMODEL);
        Request expected = PostSubmodelReferenceRequest.builder()
                .id(AAS.getIdentification())
                .submodelRef(submodelRef)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("shells/" + EncodingUtils.base64UrlEncode(AAS.getIdentification().getIdentifier()) + "/aas/submodels")
                .body(serializer.write(submodelRef))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    @Ignore
    public void testPutAASXPackageByIdRequest() throws IOException {
        Assert.fail("not implemented (requires multipart HTTP)");
        Request expected = GetAASXByPackageIdRequest.builder()
                .packageId(AASX_PACKAGE_ID)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("packages/" + EncodingUtils.base64UrlEncode(AASX_PACKAGE_ID))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutAssetAdministrationShell() throws SerializationException {
        Request expected = PutAssetAdministrationShellRequest.builder()
                .id(AAS.getIdentification())
                .aas(AAS)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("shells/" + EncodingUtils.base64UrlEncode(AAS.getIdentification().getIdentifier()) + "/aas")
                .body(serializer.write(AAS))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutAssetAdministrationShellById() throws SerializationException {
        Request expected = PutAssetAdministrationShellByIdRequest.builder()
                .id(AAS.getIdentification())
                .aas(AAS)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("shells/" + EncodingUtils.base64UrlEncode(AAS.getIdentification().getIdentifier()))
                .body(serializer.write(AAS))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutAssetInformationRequest() throws SerializationException {
        Request expected = PutAssetInformationRequest.builder()
                .id(AAS.getIdentification())
                .assetInformation(AAS.getAssetInformation())
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("shells/" + EncodingUtils.base64UrlEncode(AAS.getIdentification().getIdentifier()) + "/aas/asset-information")
                .body(serializer.write(AAS.getAssetInformation()))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutConceptDescriptionById() throws SerializationException {
        Request expected = PutConceptDescriptionByIdRequest.builder()
                .id(CONCEPT_DESCRIPTION.getIdentification())
                .conceptDescription(CONCEPT_DESCRIPTION)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("concept-descriptions/" + EncodingUtils.base64UrlEncode(CONCEPT_DESCRIPTION.getIdentification().getIdentifier()))
                .body(serializer.write(CONCEPT_DESCRIPTION))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutSubmodel() throws SerializationException {
        Request expected = PutSubmodelRequest.builder()
                .id(SUBMODEL.getIdentification())
                .submodel(SUBMODEL)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel")
                .body(serializer.write(SUBMODEL))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutSubmodelById() throws SerializationException {
        Request expected = PutSubmodelByIdRequest.builder()
                .id(SUBMODEL.getIdentification())
                .submodel(SUBMODEL)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()))
                .body(serializer.write(SUBMODEL))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testPutSubmodelElementByPath() throws SerializationException {
        Request expected = PutSubmodelElementByPathRequest.builder()
                .id(SUBMODEL.getIdentification())
                .path(ElementPathUtils.extractElementPath(SUBMODEL_ELEMENT_REF))
                .submodelElement(SUBMODEL_ELEMENT)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathUtils.toElementPath(SUBMODEL_ELEMENT_REF))
                .body(serializer.write(SUBMODEL_ELEMENT))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    @Ignore
    public void testSetSubmodelElementValueByPath() throws SerializationException {
        Request expected = SetSubmodelElementValueByPathRequest.<String> builder()
                .id(SUBMODEL.getIdentification())
                .path(ElementPathUtils.extractElementPath(SUBMODEL_ELEMENT_REF))
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.PUT)
                .path("submodels/" + EncodingUtils.base64UrlEncode(SUBMODEL.getIdentification().getIdentifier()) + "/submodel/submodel-elements/"
                        + ElementPathUtils.toElementPath(SUBMODEL_ELEMENT_REF))
                .query("content=value")
                .body(serializer.write(SUBMODEL_ELEMENT))
                .build());
        Assert.assertEquals(expected, actual);

        Assert.fail("not implemented");
        //        File example = new File("src/test/resources/example-submodel-element.json");
        //        execute(HttpMethod.PUT,
        //                "submodels/bXktYWFzLXRlc3QtaWRlbnRpZmllcg==/submodel/submodel-elements/MySubmodelElementStruct.MySubSubmodelElementList%5B1%5D",
        //                "content=value",
        //                Files.readString(example.toPath()),
        //                SetSubmodelElementValueByPathRequest.class);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testUnknownPath() {
        mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("foo")
                .build());
    }
}
