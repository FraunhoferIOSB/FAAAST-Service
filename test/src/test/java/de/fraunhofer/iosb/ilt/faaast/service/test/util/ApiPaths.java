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
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.UnsupportedModifierException;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;


public class ApiPaths {

    private static final String API_PREFIX = "/api/v3.0";
    private final String host;
    private final int port;

    public ApiPaths(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public String root() {
        return String.format("%s:%d%s", host, port, API_PREFIX);
    }


    private String content(Content content) {
        return String.format("/$%s", content.name().toLowerCase());
    }


    private static String appendQueryParameter(String url, String name, Object value) {
        return String.format("%s%s%s=%s",
                url,
                url.contains("?") ? "&" : "?",
                name,
                value);
    }


    private String paging(String url, String cursor, long limit) {
        String result = url;
        if (Objects.nonNull(cursor)) {
            result = appendQueryParameter(result, "cursor", EncodingHelper.base64UrlEncode(cursor));
        }
        return appendQueryParameter(result, "limit", limit);
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
        return new AASInterface(aas.getId());
    }


    public ProprietaryInterface proprietaryInterface() {
        return new ProprietaryInterface();
    }

    public class AASRespositoryInterface {

        public String assetAdministrationShells() {
            return String.format("%s/shells", ApiPaths.this.root());
        }


        public String assetAdministrationShells(Content content) {
            return String.format("%s%s", assetAdministrationShells(), content(content));
        }


        public String assetAdministrationShells(String cursor, long limit) {
            return paging(assetAdministrationShells(), cursor, limit);
        }


        public String assetAdministrationShell(String identifier) {
            return String.format("%s/%s",
                    assetAdministrationShells(),
                    EncodingHelper.base64UrlEncode(identifier));
        }


        public String assetAdministrationShell(String identifier, Content content) {
            return String.format("%s%s",
                    assetAdministrationShell(identifier),
                    content(content));
        }


        public String assetAdministrationShell(AssetAdministrationShell aas) {
            return assetAdministrationShell(aas.getId());
        }


        public String assetAdministrationShell(AssetAdministrationShell aas, Content content) {
            return assetAdministrationShell(aas.getId(), content);
        }


        public SubmodelRespositoryInterface submodelRepositoryInterface(AssetAdministrationShell aas) {
            return new SubmodelRespositoryInterface(assetAdministrationShell(aas));
        }


        public SubmodelRespositoryInterface submodelRepositoryInterface(String identifier) {
            return new SubmodelRespositoryInterface(assetAdministrationShell(identifier));
        }
    }

    public class SubmodelRespositoryInterface {

        private final String root;

        private SubmodelRespositoryInterface() {
            this.root = ApiPaths.this.root();
        }


        private SubmodelRespositoryInterface(String root) {
            this.root = root;
        }


        public String submodels() {
            return String.format("%s/submodels", root);
        }


        public String submodels(Content content) {
            return String.format("%s%s", submodels(), content(content));
        }


        public String submodels(String cursor, long limit) {
            return paging(submodels(), cursor, limit);
        }


        public String submodel(String identifier) {
            return String.format("%s/%s",
                    submodels(),
                    EncodingHelper.base64UrlEncode(identifier));
        }


        public String submodel(Submodel submodel) {
            return submodel(submodel.getId());
        }


        public String submodel(Submodel submodel, Content content) {
            return String.format("%s%s", submodel(submodel), content(content));
        }


        public SubmodelInterface submodelInterface(Submodel submodel) {
            return submodelInterface(submodel.getId());
        }


        public SubmodelInterface submodelInterface(String identifier) {
            return new SubmodelInterface(submodel(identifier));
        }
    }

    public class ConceptDesccriptionRepositoryInterface {

        public String conceptDescriptions() {
            return String.format("%s/concept-descriptions", ApiPaths.this.root());
        }


        public String conceptDescriptions(String cursor, long limit) {
            return paging(conceptDescriptions(), cursor, limit);
        }


        public String conceptDescription(String identifier) {
            return String.format("%s/%s",
                    conceptDescriptions(),
                    EncodingHelper.base64UrlEncode(identifier));
        }


        public String conceptDescription(ConceptDescription conceptDescription) {
            return conceptDescription(conceptDescription.getId());
        }
    }

    public class AASBasicDiscovery {

        public String assetAdministrationShells() {
            return String.format("%s/lookup/shells", ApiPaths.this.root());
        }


        public String assetAdministrationShells(String cursor, long limit) {
            return paging(assetAdministrationShells(), cursor, limit);
        }


        public String assetAdministrationShells(Map<String, String> assetIds) throws SerializationException, UnsupportedModifierException {
            return String.format("%s?assetIds=%s",
                    assetAdministrationShells(),
                    EncodingHelper.base64UrlEncode(new HttpJsonApiSerializer().write(
                            assetIds.entrySet().stream()
                                    .map(x -> new DefaultSpecificAssetId.Builder()
                                            .name(x.getKey())
                                            .value(x.getValue())
                                            .build())
                                    .collect(Collectors.toList()))));
        }


        public String assetAdministrationShells(Map<String, String> assetIds, String cursor, long limit) throws SerializationException, UnsupportedModifierException {
            return paging(assetAdministrationShells(assetIds), cursor, limit);
        }


        public String assetAdministrationShell(String identifier) {
            return String.format("%s/%s",
                    assetAdministrationShells(),
                    EncodingHelper.base64UrlEncode(identifier));
        }


        public String assetAdministrationShell(AssetAdministrationShell aas) {
            return assetAdministrationShell(aas.getId());
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
                                        .map(x -> x.getId())
                                        .collect(Collectors.joining(","))));
            }
            if (submodelIds != null && !submodelIds.isEmpty()) {
                queryElements.put(QueryParameters.SUBMODEL_IDS,
                        EncodingHelper.base64UrlEncode(
                                submodelIds.stream()
                                        .map(x -> x.getId())
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
            return String.format("%s/shells/%s",
                    root(),
                    EncodingHelper.base64UrlEncode(identifier));
        }


        public String assetAdministrationShell(Content content) {
            return String.format("%s%s",
                    assetAdministrationShell(),
                    content(content));
        }


        public String assetInformation() {
            return String.format("%s/asset-information", assetAdministrationShell());
        }


        public String submodels() {
            return String.format("%s/submodel-refs", assetAdministrationShell());
        }


        public String submodels(String cursor, long limit) {
            return paging(submodels(), cursor, limit);
        }


        public String submodel(Submodel submodel) {
            return submodel(submodel.getId());
        }


        public String submodel(Reference reference) {
            return submodel(reference.getKeys().get(0).getValue());
        }


        public String submodelRefs(Reference reference) {
            return String.format("%s/submodel-refs/%s",
                    assetAdministrationShell(),
                    EncodingHelper.base64UrlEncode(reference.getKeys().get(0).getValue()));
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


        private String extent(Extent extent) {
            return String.format("extent=%s", StringHelper.decapitalize(extent.getName()));
        }


        private SubmodelInterface(String root) {
            this.root = root;
        }


        public String submodel() {
            return String.format("%s", root);
        }


        public String submodel(Level level) {
            return String.format("%s?%s",
                    root,
                    level(level));
        }


        public String submodel(Level level, Content content) {
            return String.format("%s%s?%s",
                    root,
                    content(content),
                    level(level));
        }


        public String submodel(Content content) {
            return String.format("%s%s",
                    root,
                    content(content));
        }


        public String submodelElements() {
            return String.format("%s/submodel-elements",
                    submodel());
        }


        public String submodelElements(String cursor, long limit) {
            return paging(submodelElements(), cursor, limit);
        }


        public String submodelElements(Level level) {
            return String.format("%s?%s",
                    submodelElements(),
                    level(level));
        }


        public String submodelElements(Level level, String cursor, long limit) {
            return paging(submodelElements(level), cursor, limit);
        }


        public String submodelElements(Level level, Content content) {
            return String.format("%s%s?%s",
                    submodelElements(),
                    content(content),
                    level(level));
        }


        public String submodelElements(Level level, Content content, String cursor, long limit) {
            return paging(submodelElements(level, content), cursor, limit);
        }


        public String submodelElements(Content content) {
            return String.format("%s%s",
                    submodelElements(),
                    content(content));
        }


        public String submodelElements(Content content, String cursor, long limit) {
            return paging(submodelElements(content), cursor, limit);
        }


        public String submodelElement(String idShortPath) {
            return String.format("%s/%s",
                    submodelElements(),
                    URLEncoder.encode(idShortPath, StandardCharsets.UTF_8));
        }


        public String submodelElement(IdShortPath idShortPath) {
            return submodelElement(idShortPath.toString());
        }


        public String submodelElement(IdShortPath idShortPath, Extent extent) {
            return submodelElement(idShortPath.toString(), extent);
        }


        public String submodelElement(String idShortPath, Level level) {
            return String.format("%s?%s",
                    submodelElement(idShortPath),
                    level(level));
        }


        public String submodelElement(String idShortPath, Extent extent) {
            return String.format("%s?%s",
                    submodelElement(idShortPath),
                    extent(extent));
        }


        public String submodelElement(String idShortPath, Content content) {
            return String.format("%s%s",
                    submodelElement(idShortPath),
                    content(content));
        }


        public String submodelElement(String idShortPath, Content content, Extent extent) {
            return String.format("%s%s?%s",
                    submodelElement(idShortPath),
                    content(content),
                    extent(extent));
        }


        public String submodelElement(String idShortPath, Level level, Extent extent) {
            return String.format("%s?%s&%s",
                    submodelElement(idShortPath),
                    extent(extent),
                    level(level));
        }


        public String submodelElement(String idShortPath, Level level, Content content) {
            return String.format("%s%s?%s",
                    submodelElement(idShortPath),
                    content(content),
                    level(level));
        }


        public String submodelElement(String idShortPath, Level level, Content content, Extent extent) {
            return String.format("%s%s?%s&%s",
                    submodelElement(idShortPath),
                    content(content),
                    level(level),
                    extent(extent));
        }


        public String submodelElement(SubmodelElement submodelElement) {
            return submodelElement(submodelElement.getIdShort());
        }


        public String submodelElement(SubmodelElement submodelElement, Extent extent) {
            return submodelElement(submodelElement.getIdShort(), extent);
        }


        public String submodelElement(SubmodelElement submodelElement, Level level) {
            return submodelElement(submodelElement.getIdShort(), level);
        }


        public String submodelElement(SubmodelElement submodelElement, Level level, Extent extent) {
            return submodelElement(submodelElement.getIdShort(), level, extent);
        }


        public String submodelElement(SubmodelElement submodelElement, Content content) {
            return submodelElement(submodelElement.getIdShort(), content);
        }


        public String submodelElement(SubmodelElement submodelElement, Content content, Extent extent) {
            return submodelElement(submodelElement.getIdShort(), content, extent);
        }


        public String submodelElement(SubmodelElement submodelElement, Level level, Content content) {
            return submodelElement(submodelElement.getIdShort(), level, content);
        }


        public String submodelElement(SubmodelElement submodelElement, Level level, Content content, Extent extent) {
            return submodelElement(submodelElement.getIdShort(), level, content, extent);
        }


        public String invoke(IdShortPath idShortPath) {
            return invoke(idShortPath.toString());
        }


        public String invoke(String idShortPath) {
            return String.format("%s/invoke",
                    submodelElement(idShortPath));
        }


        public String invokeValueOnly(IdShortPath idShortPath) {
            return invokeValueOnly(idShortPath.toString());
        }


        public String invokeValueOnly(String idShortPath) {
            return String.format("%s/invoke/$value",
                    submodelElement(idShortPath));
        }


        public String invokeAsync(IdShortPath idShortPath) {
            return invokeAsync(idShortPath.toString());
        }


        public String invokeAsync(String idShortPath) {
            return String.format("%s/invoke-async",
                    submodelElement(idShortPath));
        }


        public String invokeAsyncValueOnly(IdShortPath idShortPath) {
            return invokeAsyncValueOnly(idShortPath.toString());
        }


        public String invokeAsyncValueOnly(String idShortPath) {
            return String.format("%s/invoke-async/$value",
                    submodelElement(idShortPath));
        }


        public String operationResult(String idShortPath, String handleId) {
            return String.format("%s/operation-results/%s",
                    submodelElement(idShortPath),
                    handleId);
        }


        public String operationStatus(String idShortPath, String handleId) {
            return String.format("%s/operation-status/%s",
                    submodelElement(idShortPath),
                    handleId);
        }
    }

    public class ProprietaryInterface {

        public String reset() {
            return String.format("%s/reset", ApiPaths.this.root());
        }


        public String importFile() {
            return String.format("%s/import", ApiPaths.this.root());
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
