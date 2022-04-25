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
package de.fraunhofer.iosb.ilt.faaast.service.test.util;

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultIdentifierKeyValuePair;
import java.util.Map;
import java.util.stream.Collectors;


public class ApiPaths {

    private final String host;
    private final int port;

    public ApiPaths(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public String root() {
        return String.format("%s:%d", host, port);
    }


    public AASRespositoryInterface aasRepository() {
        return new AASRespositoryInterface();
    }


    public AASBasicDiscovery aasBasicDiscovery() {
        return new AASBasicDiscovery();
    }


    public SubmodelRespositoryInterface submodelRepository() {
        return new SubmodelRespositoryInterface();
    }


    public ConceptDesccriptionRepositoryInterface conceptDescriptionRepository() {
        return new ConceptDesccriptionRepositoryInterface();
    }


    public AASInterface aasInterface(String identifier) {
        return new AASInterface(identifier);
    }


    public AASInterface aasInterface(AssetAdministrationShell aas) {
        return new AASInterface(aas.getIdentification().getIdentifier());
    }

    public class AASRespositoryInterface {

        public String assetAdministrationShells() {
            return String.format("%s/shells", ApiPaths.this.root());
        }


        public String assetAdministrationShell(String identifier) {
            return String.format("%s/%s",
                    assetAdministrationShells(),
                    EncodingHelper.base64UrlEncode(identifier));
        }


        public String assetAdministrationShell(AssetAdministrationShell aas) {
            return assetAdministrationShell(aas.getIdentification().getIdentifier());
        }
    }

    public class SubmodelRespositoryInterface {

        public String submodels() {
            return String.format("%s/submodels", ApiPaths.this.root());
        }


        public String submodel(String identifier) {
            return String.format("%s/%s",
                    submodels(),
                    EncodingHelper.base64UrlEncode(identifier));
        }


        public String submodel(Submodel submodel) {
            return submodel(submodel.getIdentification().getIdentifier());
        }


        public SubmodelInterface submodelInterface(Submodel submodel) {
            return submodelInterface(submodel.getIdentification().getIdentifier());
        }


        public SubmodelInterface submodelInterface(String identifier) {
            return new SubmodelInterface(submodel(identifier));
        }
    }

    public class ConceptDesccriptionRepositoryInterface {

        public String conceptDescriptions() {
            return String.format("%s/concept-descriptions", ApiPaths.this.root());
        }


        public String conceptDescription(String identifier) {
            return String.format("%s/%s",
                    conceptDescriptions(),
                    EncodingHelper.base64UrlEncode(identifier));
        }


        public String conceptDescription(ConceptDescription conceptDescription) {
            return conceptDescription(conceptDescription.getIdentification().getIdentifier());
        }
    }

    public class AASBasicDiscovery {

        public String assetAdministrationShells() {
            return String.format("%s/lookup/shells", ApiPaths.this.root());
        }


        public String assetAdministrationShells(Map<String, String> assetIds) throws SerializationException {
            return String.format("%s?assetIds=%s",
                    assetAdministrationShells(),
                    EncodingHelper.base64UrlEncode(new HttpJsonSerializer().write(
                            assetIds.entrySet().stream()
                                    .map(x -> new DefaultIdentifierKeyValuePair.Builder()
                                            .key(x.getKey())
                                            .value(x.getValue())
                                            .build())
                                    .collect(Collectors.toList()))));
        }


        public String assetAdministrationShell(String identifier) {
            return String.format("%s/%s",
                    assetAdministrationShells(),
                    EncodingHelper.base64UrlEncode(identifier));
        }


        public String assetAdministrationShell(AssetAdministrationShell aas) {
            return assetAdministrationShell(aas.getIdentification().getIdentifier());
        }
    }

    public class AASInterface {

        private final String identifier;

        private AASInterface(String identifier) {
            this.identifier = identifier;
        }


        public String assetAdministrationShell() {
            return String.format("%s/shells/%s/aas",
                    root(),
                    EncodingHelper.base64UrlEncode(identifier));
        }


        public String assetInformation() {
            return String.format("%s/asset-information", assetAdministrationShell());
        }


        public String submodels() {
            return String.format("%s/submodels", assetAdministrationShell());
        }


        public String submodel(Submodel submodel) {
            return submodel(submodel.getIdentification().getIdentifier());
        }


        public String submodel(Reference reference) {
            return submodel(reference.getKeys().get(0).getValue());
        }


        public String submodel(String identifier) {
            return String.format("%s/submodels/%s",
                    assetAdministrationShell(),
                    EncodingHelper.base64UrlEncode(identifier));
        }


        public SubmodelInterface submodelInterface(Submodel submodel) {
            return new SubmodelInterface(submodel.getIdentification().getIdentifier());
        }


        public SubmodelInterface submodelInterface(String identifier) {
            return new SubmodelInterface(submodel(identifier));
        }
    }

    public class SubmodelInterface {

        private final String root;

        private SubmodelInterface(String root) {
            this.root = root;
        }


        public String submodel() {
            return String.format("%s/submodel", root);
        }


        public String submodel(Level level) {
            return String.format("%s/submodel?level=%s", root, level.name().toLowerCase());
        }


        public String submodel(Level level, Content content) {
            return String.format("%s/submodel?level=%s&content=%s", root, level.name().toLowerCase(), content.name().toLowerCase());
        }


        public String submodel(Content content) {
            return String.format("%s/submodel?content=%s", root, content.name().toLowerCase());
        }


        public String submodelElements() {
            return String.format("%s/submodel-elements",
                    submodel());
        }


        public String submodelElement(String idShortPath) {
            return String.format("%s/%s",
                    submodelElements(),
                    idShortPath);
        }


        public String submodelElement(SubmodelElement submodelElement) {
            return submodelElement(submodelElement.getIdShort());
        }


        public String invoke(String idShortPath) {
            return String.format("%s/invoke",
                    submodelElement(idShortPath));
        }


        public String operationResult(String idShortPath, String handleId) {
            return String.format("%s/operation-results/%s",
                    submodelElement(idShortPath),
                    handleId);
        }
    }
}
