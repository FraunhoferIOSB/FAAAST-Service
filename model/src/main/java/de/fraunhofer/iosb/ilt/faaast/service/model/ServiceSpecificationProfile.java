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

/**
 * List of supported service profiles according to specification.
 */
public enum ServiceSpecificationProfile {
    AAS_FULL("https://admin-shell.io/aas/API/3/0/AssetAdministrationShellServiceSpecification/SSP-001"),
    AAS_READ("https://admin-shell.io/aas/API/3/0/AssetAdministrationShellServiceSpecification/SSP-002"),
    SUBMODEL_FULL("https://admin-shell.io/aas/API/3/0/SubmodelServiceSpecification/SSP-001"),
    SUBMODEL_VALUE("https://admin-shell.io/aas/API/3/0/SubmodelServiceSpecification/SSP-002"),
    SUBMODEL_READ("https://admin-shell.io/aas/API/3/0/SubmodelServiceSpecification/SSP-003"),
    AASX_FILE_SERVER_FULL("https://admin-shell.io/aas/API/3/0/AasxFileServerServiceSpecification/SSP-001"),
    AAS_REGISTRY_FULL("https://admin-shell.io/aas/API/3/0/AasxFileServerServiceSpecification/SSP-001"),
    AAS_REGISTRY_READ("https://admin-shell.io/aas/API/3/0/AssetAdministrationShellRegistryServiceSpecification/SSP-002"),
    SUBMODEL_REGISTRY_FULL("https://admin-shell.io/aas/API/3/0/SubmodelRegistryServiceSpecification/SSP-001"),
    SUBMODEL_REGISTRY_READ("https://admin-shell.io/aas/API/3/0/SubmodelRegistryServiceSpecification/SSP-002"),
    DISCOVERY_FULL("https://admin-shell.io/aas/API/3/0/DiscoveryServiceSpecification/SSP-001"),
    AAS_REPOSITORY_FULL("https://admin-shell.io/aas/API/3/0/AssetAdministrationShellRepositoryServiceSpecification/SSP-001"),
    AAS_REPOSITORY_READ("https://admin-shell.io/aas/API/3/0/AssetAdministrationShellRepositoryServiceSpecification/SSP-002"),
    SUBMODEL_REPOSITORY_FULL("https://admin-shell.io/aas/API/3/0/SubmodelRepositoryServiceSpecification/SSP-001"),
    SUBMODEL_REPOSITORY_READ("https://admin-shell.io/aas/API/3/0/SubmodelRepositoryServiceSpecification/SSP-002"),
    SUBMODEL_REPOSITORY_TEMPLATE("https://admin-shell.io/aas/API/3/0/SubmodelRepositoryServiceSpecification/SSP-003"),
    SUBMODEL_REPOSITORY_TEMPLATE_READ("https://admin-shell.io/aas/API/3/0/SubmodelRepositoryServiceSpecification/SSP-004"),
    CONCEPT_DESCRIPTION_FULL("https://admin-shell.io/aas/API/3/0/ConceptDescriptionServiceSpecification/SSP-001");

    private final String name;

    private ServiceSpecificationProfile(String name) {
        this.name = name;
    }


    public String getName() {
        return name;
    }
}
