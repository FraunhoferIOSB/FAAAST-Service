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
package de.fraunhofer.iosb.ilt.faaast.service.model;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.DeleteAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByAssetIdReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByAssetIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByIdShortReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAssetAdministrationShellByIdReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.PostAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.PutAssetAdministrationShellByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasserialization.GenerateSerializationByIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.DeleteConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsByDataSpecificationReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsByIsCaseOfRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.PostConceptDescriptionRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.PutConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.description.GetSelfDescriptionRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.proprietary.ImportRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.proprietary.ResetRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.DeleteFileByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.DeleteSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetAllSubmodelElementsPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetAllSubmodelElementsReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetAllSubmodelElementsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetAllSubmodelElementsValueRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetFileByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetOperationAsyncResultRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetOperationAsyncStatusRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationAsyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationRequest;
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
import java.util.Arrays;
import java.util.List;


/**
 * List of supported service profiles according to specification.
 */
public enum ServiceSpecificationProfile {
    AAS_FULL(
            "https://admin-shell.io/aas/API/3/0/AssetAdministrationShellServiceSpecification/SSP-001",
            List.of(
                    Interface.AAS,
                    Interface.SUBMODEL,
                    Interface.SERIALIZATION,
                    Interface.DESCRIPTION),
            List.of(
                    DeleteSubmodelReferenceRequest.class,
                    DeleteThumbnailRequest.class,
                    GetAllSubmodelReferencesRequest.class,
                    GetAssetAdministrationShellReferenceRequest.class,
                    GetAssetAdministrationShellRequest.class,
                    GetAssetInformationRequest.class,
                    GetThumbnailRequest.class,
                    PostSubmodelReferenceRequest.class,
                    PutAssetAdministrationShellRequest.class,
                    PutAssetInformationRequest.class,
                    PutThumbnailRequest.class,
                    DeleteFileByPathRequest.class,
                    DeleteSubmodelElementByPathRequest.class,
                    GetAllSubmodelElementsPathRequest.class,
                    GetAllSubmodelElementsReferenceRequest.class,
                    GetAllSubmodelElementsRequest.class,
                    GetAllSubmodelElementsValueRequest.class,
                    GetFileByPathRequest.class,
                    GetOperationAsyncResultRequest.class,
                    GetOperationAsyncStatusRequest.class,
                    GetSubmodelElementByPathReferenceRequest.class,
                    GetSubmodelElementByPathRequest.class,
                    GetSubmodelReferenceRequest.class,
                    GetSubmodelRequest.class,
                    InvokeOperationAsyncRequest.class,
                    InvokeOperationRequest.class,
                    InvokeOperationSyncRequest.class,
                    PatchSubmodelElementByPathRequest.class,
                    PatchSubmodelElementValueByPathRequest.class,
                    PatchSubmodelRequest.class,
                    PostSubmodelElementByPathRequest.class,
                    PostSubmodelElementRequest.class,
                    PutFileByPathRequest.class,
                    PutSubmodelElementByPathRequest.class,
                    PutSubmodelRequest.class,
                    GenerateSerializationByIdsRequest.class,
                    GetSelfDescriptionRequest.class)),
    AAS_READ(
            "https://admin-shell.io/aas/API/3/0/AssetAdministrationShellServiceSpecification/SSP-002",
            List.of(
                    Interface.AAS,
                    Interface.SUBMODEL,
                    Interface.DESCRIPTION),
            List.of(
                    GetAllSubmodelReferencesRequest.class,
                    GetAssetAdministrationShellReferenceRequest.class,
                    GetAssetAdministrationShellRequest.class,
                    GetAssetInformationRequest.class,
                    GetThumbnailRequest.class,
                    GetAllSubmodelElementsPathRequest.class,
                    GetAllSubmodelElementsReferenceRequest.class,
                    GetAllSubmodelElementsRequest.class,
                    GetAllSubmodelElementsValueRequest.class,
                    GetFileByPathRequest.class,
                    GetSubmodelElementByPathReferenceRequest.class,
                    GetSubmodelElementByPathRequest.class,
                    GetSubmodelReferenceRequest.class,
                    GetSubmodelRequest.class,
                    GetSelfDescriptionRequest.class)),
    SUBMODEL_FULL(
            "https://admin-shell.io/aas/API/3/0/SubmodelServiceSpecification/SSP-001",
            List.of(
                    Interface.SUBMODEL,
                    Interface.SERIALIZATION,
                    Interface.DESCRIPTION),
            List.of(
                    DeleteFileByPathRequest.class,
                    DeleteSubmodelElementByPathRequest.class,
                    GetAllSubmodelElementsPathRequest.class,
                    GetAllSubmodelElementsReferenceRequest.class,
                    GetAllSubmodelElementsRequest.class,
                    GetAllSubmodelElementsValueRequest.class,
                    GetFileByPathRequest.class,
                    GetOperationAsyncResultRequest.class,
                    GetOperationAsyncStatusRequest.class,
                    GetSubmodelElementByPathReferenceRequest.class,
                    GetSubmodelElementByPathRequest.class,
                    GetSubmodelReferenceRequest.class,
                    GetSubmodelRequest.class,
                    InvokeOperationAsyncRequest.class,
                    InvokeOperationRequest.class,
                    InvokeOperationSyncRequest.class,
                    PatchSubmodelElementByPathRequest.class,
                    PatchSubmodelElementValueByPathRequest.class,
                    PatchSubmodelRequest.class,
                    PostSubmodelElementByPathRequest.class,
                    PostSubmodelElementRequest.class,
                    PutFileByPathRequest.class,
                    PutSubmodelElementByPathRequest.class,
                    PutSubmodelRequest.class,
                    GenerateSerializationByIdsRequest.class,
                    GetSelfDescriptionRequest.class)),
    SUBMODEL_READ(
            "https://admin-shell.io/aas/API/3/0/SubmodelServiceSpecification/SSP-002",
            List.of(
                    Interface.SUBMODEL,
                    Interface.SERIALIZATION,
                    Interface.DESCRIPTION),
            List.of(
                    GetAllSubmodelElementsPathRequest.class,
                    GetAllSubmodelElementsReferenceRequest.class,
                    GetAllSubmodelElementsRequest.class,
                    GetAllSubmodelElementsValueRequest.class,
                    GetFileByPathRequest.class,
                    GetSubmodelElementByPathReferenceRequest.class,
                    GetSubmodelElementByPathRequest.class,
                    GetSubmodelReferenceRequest.class,
                    GetSubmodelRequest.class,
                    GenerateSerializationByIdsRequest.class,
                    GetSelfDescriptionRequest.class)),
    SUBMODEL_VALUE(
            "https://admin-shell.io/aas/API/3/0/SubmodelServiceSpecification/SSP-003",
            List.of(
                    Interface.SUBMODEL,
                    Interface.DESCRIPTION),
            List.of(
                    GetSubmodelRequest.class,
                    InvokeOperationSyncRequest.class,
                    GetSelfDescriptionRequest.class)),
    AASX_FILE_SERVER_FULL(
            "https://admin-shell.io/aas/API/3/0/AasxFileServerServiceSpecification/SSP-001",
            List.of(
                    Interface.AASX_FILE_SERVER,
                    Interface.DESCRIPTION),
            List.of(
                    GetSelfDescriptionRequest.class)),
    AAS_REGISTRY_FULL(
            "https://admin-shell.io/aas/API/3/0/AssetAdministrationShellRegistryServiceSpecification/SSP-001",
            List.of(),
            List.of()),
    AAS_REGISTRY_READ(
            "https://admin-shell.io/aas/API/3/0/AssetAdministrationShellRegistryServiceSpecification/SSP-002",
            List.of(),
            List.of()),
    SUBMODEL_REGISTRY_FULL(
            "https://admin-shell.io/aas/API/3/0/SubmodelRegistryServiceSpecification/SSP-001",
            List.of(),
            List.of()),
    SUBMODEL_REGISTRY_READ(
            "https://admin-shell.io/aas/API/3/0/SubmodelRegistryServiceSpecification/SSP-002",
            List.of(),
            List.of()),
    DISCOVERY_FULL(
            "https://admin-shell.io/aas/API/3/0/DiscoveryServiceSpecification/SSP-001",
            List.of(
                    Interface.AAS_BASIC_DISCOVERY,
                    Interface.DESCRIPTION),
            List.of(
                    DeleteAllAssetLinksByIdRequest.class,
                    GetAllAssetAdministrationShellIdsByAssetLinkRequest.class,
                    GetAllAssetLinksByIdRequest.class,
                    PostAllAssetLinksByIdRequest.class,
                    GetSelfDescriptionRequest.class)),
    AAS_REPOSITORY_FULL(
            "https://admin-shell.io/aas/API/3/0/AssetAdministrationShellRepositoryServiceSpecification/SSP-001",
            List.of(
                    Interface.AAS_REPOSITORY,
                    Interface.AAS,
                    Interface.SUBMODEL_REPOSITORY,
                    Interface.SUBMODEL,
                    Interface.SERIALIZATION,
                    Interface.DESCRIPTION),
            List.of(
                    DeleteAssetAdministrationShellByIdRequest.class,
                    GetAllAssetAdministrationShellsByAssetIdReferenceRequest.class,
                    GetAllAssetAdministrationShellsByAssetIdRequest.class,
                    GetAllAssetAdministrationShellsByIdShortReferenceRequest.class,
                    GetAllAssetAdministrationShellsByIdShortRequest.class,
                    GetAllAssetAdministrationShellsReferenceRequest.class,
                    GetAllAssetAdministrationShellsRequest.class,
                    GetAssetAdministrationShellByIdReferenceRequest.class,
                    GetAssetAdministrationShellByIdRequest.class,
                    PostAssetAdministrationShellRequest.class,
                    PutAssetAdministrationShellByIdRequest.class,
                    DeleteSubmodelReferenceRequest.class,
                    DeleteThumbnailRequest.class,
                    GetAllSubmodelReferencesRequest.class,
                    GetAssetAdministrationShellReferenceRequest.class,
                    GetAssetAdministrationShellRequest.class,
                    GetAssetInformationRequest.class,
                    GetThumbnailRequest.class,
                    PostSubmodelReferenceRequest.class,
                    PutAssetAdministrationShellRequest.class,
                    PutAssetInformationRequest.class,
                    PutThumbnailRequest.class,
                    DeleteSubmodelByIdRequest.class,
                    GetAllSubmodelsByIdShortRequest.class,
                    GetAllSubmodelsBySemanticIdRequest.class,
                    GetAllSubmodelsReferenceRequest.class,
                    GetAllSubmodelsRequest.class,
                    GetSubmodelByIdReferenceRequest.class,
                    GetSubmodelByIdRequest.class,
                    PatchSubmodelByIdRequest.class,
                    PostSubmodelRequest.class,
                    PutSubmodelByIdRequest.class,
                    DeleteFileByPathRequest.class,
                    DeleteSubmodelElementByPathRequest.class,
                    GetAllSubmodelElementsPathRequest.class,
                    GetAllSubmodelElementsReferenceRequest.class,
                    GetAllSubmodelElementsRequest.class,
                    GetAllSubmodelElementsValueRequest.class,
                    GetFileByPathRequest.class,
                    GetOperationAsyncResultRequest.class,
                    GetOperationAsyncStatusRequest.class,
                    GetSubmodelElementByPathReferenceRequest.class,
                    GetSubmodelElementByPathRequest.class,
                    GetSubmodelReferenceRequest.class,
                    GetSubmodelRequest.class,
                    InvokeOperationAsyncRequest.class,
                    InvokeOperationRequest.class,
                    InvokeOperationSyncRequest.class,
                    PatchSubmodelElementByPathRequest.class,
                    PatchSubmodelElementValueByPathRequest.class,
                    PatchSubmodelRequest.class,
                    PostSubmodelElementByPathRequest.class,
                    PostSubmodelElementRequest.class,
                    PutFileByPathRequest.class,
                    PutSubmodelElementByPathRequest.class,
                    PutSubmodelRequest.class,
                    GenerateSerializationByIdsRequest.class,
                    GetSelfDescriptionRequest.class)),
    AAS_REPOSITORY_READ(
            "https://admin-shell.io/aas/API/3/0/AssetAdministrationShellRepositoryServiceSpecification/SSP-002",
            List.of(
                    Interface.AAS_REPOSITORY,
                    Interface.AAS,
                    Interface.SUBMODEL_REPOSITORY,
                    Interface.SUBMODEL,
                    Interface.SERIALIZATION,
                    Interface.DESCRIPTION),
            List.of(
                    GetAllAssetAdministrationShellsByAssetIdReferenceRequest.class,
                    GetAllAssetAdministrationShellsByAssetIdRequest.class,
                    GetAllAssetAdministrationShellsByIdShortReferenceRequest.class,
                    GetAllAssetAdministrationShellsByIdShortRequest.class,
                    GetAllAssetAdministrationShellsReferenceRequest.class,
                    GetAllAssetAdministrationShellsRequest.class,
                    GetAssetAdministrationShellByIdReferenceRequest.class,
                    GetAssetAdministrationShellByIdRequest.class,
                    GetAllSubmodelReferencesRequest.class,
                    GetAssetAdministrationShellReferenceRequest.class,
                    GetAssetAdministrationShellRequest.class,
                    GetAssetInformationRequest.class,
                    GetThumbnailRequest.class,
                    GetAllSubmodelsByIdShortRequest.class,
                    GetAllSubmodelsBySemanticIdRequest.class,
                    GetAllSubmodelsReferenceRequest.class,
                    GetAllSubmodelsRequest.class,
                    GetSubmodelByIdReferenceRequest.class,
                    GetSubmodelByIdRequest.class,
                    GetAllSubmodelElementsPathRequest.class,
                    GetAllSubmodelElementsReferenceRequest.class,
                    GetAllSubmodelElementsRequest.class,
                    GetAllSubmodelElementsValueRequest.class,
                    GetFileByPathRequest.class,
                    GetSubmodelElementByPathReferenceRequest.class,
                    GetSubmodelElementByPathRequest.class,
                    GetSubmodelReferenceRequest.class,
                    GetSubmodelRequest.class,
                    GenerateSerializationByIdsRequest.class,
                    GetSelfDescriptionRequest.class)),
    SUBMODEL_REPOSITORY_FULL(
            "https://admin-shell.io/aas/API/3/0/SubmodelRepositoryServiceSpecification/SSP-001",
            List.of(
                    Interface.SUBMODEL_REPOSITORY,
                    Interface.SUBMODEL,
                    Interface.SERIALIZATION,
                    Interface.DESCRIPTION),
            List.of(
                    DeleteSubmodelByIdRequest.class,
                    GetAllSubmodelsByIdShortRequest.class,
                    GetAllSubmodelsBySemanticIdRequest.class,
                    GetAllSubmodelsReferenceRequest.class,
                    GetAllSubmodelsRequest.class,
                    GetSubmodelByIdReferenceRequest.class,
                    GetSubmodelByIdRequest.class,
                    PatchSubmodelByIdRequest.class,
                    PostSubmodelRequest.class,
                    PutSubmodelByIdRequest.class,
                    DeleteFileByPathRequest.class,
                    DeleteSubmodelElementByPathRequest.class,
                    GetAllSubmodelElementsPathRequest.class,
                    GetAllSubmodelElementsReferenceRequest.class,
                    GetAllSubmodelElementsRequest.class,
                    GetAllSubmodelElementsValueRequest.class,
                    GetFileByPathRequest.class,
                    GetOperationAsyncResultRequest.class,
                    GetOperationAsyncStatusRequest.class,
                    GetSubmodelElementByPathReferenceRequest.class,
                    GetSubmodelElementByPathRequest.class,
                    GetSubmodelReferenceRequest.class,
                    GetSubmodelRequest.class,
                    InvokeOperationAsyncRequest.class,
                    InvokeOperationRequest.class,
                    InvokeOperationSyncRequest.class,
                    PatchSubmodelElementByPathRequest.class,
                    PatchSubmodelElementValueByPathRequest.class,
                    PatchSubmodelRequest.class,
                    PostSubmodelElementByPathRequest.class,
                    PostSubmodelElementRequest.class,
                    PutFileByPathRequest.class,
                    PutSubmodelElementByPathRequest.class,
                    PutSubmodelRequest.class,
                    GenerateSerializationByIdsRequest.class,
                    GetSelfDescriptionRequest.class)),
    SUBMODEL_REPOSITORY_READ(
            "https://admin-shell.io/aas/API/3/0/SubmodelRepositoryServiceSpecification/SSP-002",
            List.of(
                    Interface.SUBMODEL_REPOSITORY,
                    Interface.SUBMODEL,
                    Interface.SERIALIZATION,
                    Interface.DESCRIPTION),
            List.of(
                    GetAllSubmodelsByIdShortRequest.class,
                    GetAllSubmodelsBySemanticIdRequest.class,
                    GetAllSubmodelsReferenceRequest.class,
                    GetAllSubmodelsRequest.class,
                    GetSubmodelByIdReferenceRequest.class,
                    GetSubmodelByIdRequest.class,
                    GetAllSubmodelElementsPathRequest.class,
                    GetAllSubmodelElementsReferenceRequest.class,
                    GetAllSubmodelElementsRequest.class,
                    GetAllSubmodelElementsValueRequest.class,
                    GetFileByPathRequest.class,
                    GetSubmodelElementByPathReferenceRequest.class,
                    GetSubmodelElementByPathRequest.class,
                    GetSubmodelReferenceRequest.class,
                    GetSubmodelRequest.class,
                    GenerateSerializationByIdsRequest.class,
                    GetSelfDescriptionRequest.class)),
    CONCEPT_DESCRIPTION_FULL(
            "https://admin-shell.io/aas/API/3/0/ConceptDescriptionServiceSpecification/SSP-001",
            List.of(
                    Interface.CONCEPT_DESCRIPTION_REPOSITORY,
                    Interface.SERIALIZATION,
                    Interface.DESCRIPTION),
            List.of(
                    DeleteConceptDescriptionByIdRequest.class,
                    GetAllConceptDescriptionsByDataSpecificationReferenceRequest.class,
                    GetAllConceptDescriptionsByIdShortRequest.class,
                    GetAllConceptDescriptionsByIsCaseOfRequest.class,
                    GetAllConceptDescriptionsRequest.class,
                    GetConceptDescriptionByIdRequest.class,
                    PostConceptDescriptionRequest.class,
                    PutConceptDescriptionByIdRequest.class,
                    GenerateSerializationByIdsRequest.class,
                    GetSelfDescriptionRequest.class)),
    FAAAST_IMPORT(
            "https://github.com/FraunhoferIOSB/FAAAST-Service/API/1/3/Import",
            List.of(),
            List.of(ImportRequest.class)),
    FAAAST_RESET(
            "https://github.com/FraunhoferIOSB/FAAAST-Service/API/1/3/Reset",
            List.of(),
            List.of(ResetRequest.class));

    public static List<ServiceSpecificationProfile> ALL = Arrays.asList(ServiceSpecificationProfile.values());

    private final String id;
    private final List<Interface> interfaces;
    private final List<Class<? extends Request>> supportedRequests;

    private ServiceSpecificationProfile(String id, List<Interface> interfaces, List<Class<? extends Request>> supportedRequests) {
        this.id = id;
        this.interfaces = interfaces;
        this.supportedRequests = supportedRequests;
    }


    public String getId() {
        return id;
    }


    public List<Interface> getInterfaces() {
        return interfaces;
    }


    public List<Class<? extends Request>> getSupportedRequests() {
        return supportedRequests;
    }
}
