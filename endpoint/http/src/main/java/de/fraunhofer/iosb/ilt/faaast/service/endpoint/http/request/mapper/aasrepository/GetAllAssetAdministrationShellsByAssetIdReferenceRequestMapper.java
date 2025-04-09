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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.aasrepository;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractRequestMapperWithOutputModifierAndPaging;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.QueryParameters;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByAssetIdReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasrepository.GetAllAssetAdministrationShellsByAssetIdReferenceResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;


/**
 * class to map HTTP-GET-Request path: shells/$reference?assetIds={}.
 */
public class GetAllAssetAdministrationShellsByAssetIdReferenceRequestMapper
        extends
        AbstractRequestMapperWithOutputModifierAndPaging<GetAllAssetAdministrationShellsByAssetIdReferenceRequest, GetAllAssetAdministrationShellsByAssetIdReferenceResponse> {

    private static final String PATTERN = "shells/\\$reference";

    public GetAllAssetAdministrationShellsByAssetIdReferenceRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.GET, PATTERN, Content.METADATA, Content.NORMAL, Content.PATH, Content.VALUE);
    }


    @Override
    public boolean matchesUrl(HttpRequest httpRequest) {
        return super.matchesUrl(httpRequest) && httpRequest.hasQueryParameter(QueryParameters.ASSET_IDS);
    }


    @Override
    public GetAllAssetAdministrationShellsByAssetIdReferenceRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier,
                                                                            PagingInfo pagingInfo)
            throws InvalidRequestException {
        try {
            return GetAllAssetAdministrationShellsByAssetIdReferenceRequest.builder()
                    .assetIds(deserializer.readList(
                            getParameterBase64UrlEncoded(httpRequest.getQueryParameters(), QueryParameters.ASSET_IDS),
                            SpecificAssetId.class))
                    .build();
        }
        catch (IllegalArgumentException e) {
            throw new InvalidRequestException(String.format(
                    "invalid query parameter - value not valid base64 (name: %s, value: %s)",
                    QueryParameters.ASSET_IDS,
                    httpRequest.getQueryParameter(QueryParameters.ASSET_IDS)),
                    e);
        }
        catch (DeserializationException e) {
            throw new InvalidRequestException(
                    String.format("error deserializing %s (value: %s)", QueryParameters.ASSET_IDS, httpRequest.getQueryParameter(QueryParameters.ASSET_IDS)), e);
        }
    }
}
