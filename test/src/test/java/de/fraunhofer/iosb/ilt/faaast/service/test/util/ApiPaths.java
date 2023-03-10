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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.QueryParameters;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultIdentifierKeyValuePair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;


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


    public AASSerializationInterface aasSerialization() {
        return new AASSerializationInterface();
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
                    EncodingHelper.base64UrlEncode(new HttpJsonApiSerializer().write(
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

    public class AASSerializationInterface {

        public String serialization() {
            return String.format("%s/serialization", ApiPaths.this.root());
        }


        public String serialization(List<AssetAdministrationShell> aasIds, List<Submodel> submodelIds, boolean includeConceptDescriptions) throws SerializationException {
            Map<String, String> queryElements = new HashMap<>();
            if (aasIds != null && !aasIds.isEmpty()) {
                queryElements.put(QueryParameters.AAS_IDS,
                        EncodingHelper.base64UrlEncode(
                                aasIds.stream()
                                        .map(x -> x.getIdentification().getIdentifier())
                                        .collect(Collectors.joining(","))));
            }
            if (submodelIds != null && !submodelIds.isEmpty()) {
                queryElements.put(QueryParameters.SUBMODEL_IDS,
                        EncodingHelper.base64UrlEncode(
                                submodelIds.stream()
                                        .map(x -> x.getIdentification().getIdentifier())
                                        .collect(Collectors.joining(","))));
            }
            queryElements.put(QueryParameters.INCLUDE_CONCEPT_DESCRIPTIONS, Boolean.toString(includeConceptDescriptions));
            return serialization() + buildQueryString(queryElements);
        }


        public String serializationFromStrings(List<String> aasIds, List<String> submodelIds, boolean includeConceptDescriptions) throws SerializationException {
            Map<String, String> queryElements = new HashMap<>();
            if (aasIds != null && !aasIds.isEmpty()) {
                queryElements.put(QueryParameters.AAS_IDS, EncodingHelper.base64UrlEncode(aasIds.stream().collect(Collectors.joining(","))));
            }
            if (submodelIds != null && !submodelIds.isEmpty()) {
                queryElements.put(QueryParameters.SUBMODEL_IDS, EncodingHelper.base64UrlEncode(submodelIds.stream().collect(Collectors.joining(","))));
            }
            queryElements.put(QueryParameters.INCLUDE_CONCEPT_DESCRIPTIONS, Boolean.toString(includeConceptDescriptions));
            return serialization() + buildQueryString(queryElements);
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
            return new SubmodelInterface(submodel(submodel));
        }


        public SubmodelInterface submodelInterface(String identifier) {
            return new SubmodelInterface(submodel(identifier));
        }
    }

    public class SubmodelInterface {

        private final String root;

        private String level(Level level) {
            return String.format("level=%s", level.name().toLowerCase());
        }


        private String content(Content content) {
            return String.format("content=%s", content.name().toLowerCase());
        }


        private SubmodelInterface(String root) {
            this.root = root;
        }


        public String submodel() {
            return String.format("%s/submodel", root);
        }


        public String submodel(Level level) {
            return String.format("%s/submodel?%s",
                    root,
                    level(level));
        }


        public String submodel(Level level, Content content) {
            return String.format("%s/submodel?%s&%s",
                    root,
                    level(level),
                    content(content));
        }


        public String submodel(Content content) {
            return String.format("%s/submodel?%s",
                    root,
                    content(content));
        }


        public String submodelElements() {
            return String.format("%s/submodel-elements",
                    submodel());
        }


        public String submodelElements(Level level) {
            return String.format("%s?%s",
                    submodelElements(),
                    level(level));
        }


        public String submodelElements(Level level, Content content) {
            return String.format("%s?%s&%s",
                    submodelElements(),
                    level(level),
                    content(content));
        }


        public String submodelElements(Content content) {
            return String.format("%s?%s",
                    submodelElements(),
                    content(content));
        }


        public String submodelElement(String idShortPath) {
            return String.format("%s/%s",
                    submodelElements(),
                    idShortPath);
        }


        public String submodelElement(String idShortPath, Level level) {
            return String.format("%s?%s",
                    submodelElement(idShortPath),
                    level(level));
        }


        public String submodelElement(String idShortPath, Content content) {
            return String.format("%s?%s",
                    submodelElement(idShortPath),
                    content(content));
        }


        public String submodelElement(String idShortPath, Level level, Content content) {
            return String.format("%s?%s&%s",
                    submodelElement(idShortPath),
                    level(level),
                    content(content));
        }


        public String submodelElement(SubmodelElement submodelElement) {
            return submodelElement(submodelElement.getIdShort());
        }


        public String submodelElement(SubmodelElement submodelElement, Level level) {
            return submodelElement(submodelElement.getIdShort(), level);
        }


        public String submodelElement(SubmodelElement submodelElement, Content content) {
            return submodelElement(submodelElement.getIdShort(), content);
        }


        public String submodelElement(SubmodelElement submodelElement, Level level, Content content) {
            return submodelElement(submodelElement.getIdShort(), level, content);
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

    private static String buildQueryString(Map<String, String> variableValues) {
        String result = variableValues.entrySet().stream()
                .filter(x -> StringUtils.isNotBlank(x.getValue()))
                .map(x -> String.format("%s=%s", x.getKey(), x.getValue()))
                .collect(Collectors.joining("&"));
        if (StringUtils.isNotBlank(result)) {
            return "?" + result;
        }
        return "";
    }
}
