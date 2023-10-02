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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.GetAllAssetAdministrationShellsByAssetIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.GetAllAssetAdministrationShellsByAssetIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import java.util.Map;


/**
 * class to map HTTP-GET-Request path: shells.
 */
public class GetAllAssetAdministrationShellsByAssetIdRequestMapper
        extends AbstractRequestMapperWithOutputModifier<GetAllAssetAdministrationShellsByAssetIdRequest, GetAllAssetAdministrationShellsByAssetIdResponse> {

    private static final String PATTERN = "shells";

    public GetAllAssetAdministrationShellsByAssetIdRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.GET, PATTERN);
    }


    @Override
    public boolean matchesUrl(HttpRequest httpRequest) {
        return super.matchesUrl(httpRequest) && httpRequest.hasQueryParameter(QueryParameters.ASSET_IDS);
    }


    @Override
    public GetAllAssetAdministrationShellsByAssetIdRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier)
            throws InvalidRequestException {
        try {
            return GetAllAssetAdministrationShellsByAssetIdRequest.builder()
                    .assetIds(deserializer.readList(EncodingHelper.base64UrlDecode(httpRequest.getQueryParameter(QueryParameters.ASSET_IDS)),
                            IdentifierKeyValuePair.class))
                    .build();
        }
        catch (DeserializationException e) {
            throw new InvalidRequestException(
                    String.format("error deserializing %s (value: %s)", QueryParameters.ASSET_IDS, httpRequest.getQueryParameter(QueryParameters.ASSET_IDS)), e);
        }
    }
}