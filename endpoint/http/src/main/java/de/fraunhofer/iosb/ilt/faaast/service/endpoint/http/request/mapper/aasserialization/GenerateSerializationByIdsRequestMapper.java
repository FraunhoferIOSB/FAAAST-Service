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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.aasserialization;

import com.google.common.net.MediaType;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractRequestMapper;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.QueryParameters;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpConstants;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasserialization.GenerateSerializationByIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


/**
 * class to map HTTP-GET-Request path: /serialization.
 */
public class GenerateSerializationByIdsRequestMapper extends AbstractRequestMapper {

    private static final String PATTERN = "serialization";

    public GenerateSerializationByIdsRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.GET, PATTERN);
    }


    @Override
    public Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException {
        GenerateSerializationByIdsRequest.Builder builder = GenerateSerializationByIdsRequest.builder();
        if (httpRequest.hasQueryParameter(QueryParameters.AAS_IDS)) {
            builder.aasIds(parseAndDecodeQueryParameter(httpRequest.getQueryParameter(QueryParameters.AAS_IDS)));
        }
        if (httpRequest.hasQueryParameter(QueryParameters.SUBMODEL_IDS)) {
            builder.submodelIds(parseAndDecodeQueryParameter(httpRequest.getQueryParameter(QueryParameters.SUBMODEL_IDS)));
        }
        if (httpRequest.hasQueryParameter(QueryParameters.INCLUDE_CONCEPT_DESCRIPTIONS)) {
            builder.includeConceptDescriptions(Boolean.valueOf(httpRequest.getQueryParameter(QueryParameters.INCLUDE_CONCEPT_DESCRIPTIONS)));
        }
        if (httpRequest.hasHeader(HttpConstants.HEADER_ACCEPT)) {
            builder.serializationFormat(determineDataFormat(httpRequest.getHeader(HttpConstants.HEADER_ACCEPT)));
        }
        return builder.build();
    }


    private static DataFormat determineDataFormat(String acceptHeaderValue) throws InvalidRequestException {
        List<MediaType> acceptedTypes = HttpHelper.parseCommaSeparatedList(acceptHeaderValue)
                .stream()
                .map(MediaType::parse)
                .map(MediaType::withoutParameters)
                .toList();
        for (MediaType type: acceptedTypes) {
            Optional<DataFormat> match = Stream.of(DataFormat.values())
                    .filter(x -> x.getContentType().withoutParameters().is(type))
                    .sorted(Comparator.comparingInt(DataFormat::getPriority))
                    .findFirst();
            if (match.isPresent()) {
                return match.get();
            }
        }
        throw new InvalidRequestException(String.format("requested data format not valid (%s)", acceptHeaderValue));
    }


    private List<String> parseAndDecodeQueryParameter(String input) {
        return HttpHelper.parseCommaSeparatedList(EncodingHelper.base64UrlDecode(input));
    }
}
