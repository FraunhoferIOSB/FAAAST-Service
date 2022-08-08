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

import com.google.common.reflect.TypeToken;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.RequestWithModifier;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;


/**
 * Base class for mapping HTTP requests including output modifier information.
 *
 * @param <T> type of request
 * @param <R> type of response to the request
 */
public abstract class RequestMapperWithOutputModifier<T extends RequestWithModifier<R>, R extends Response> extends RequestMapper {

    protected RequestMapperWithOutputModifier(ServiceContext serviceContext, HttpMethod method, String urlPattern) {
        super(serviceContext, method, urlPattern);
    }


    /**
     * Converts the HTTP request to protocol-agnostic request including output
     * modifier information
     *
     * @param httpRequest the HTTP request to convert
     * @param urlParameters map of named regex groups and their values
     * @param outputModifier output modifier for this request
     * @return the protocol-agnostic request
     * @throws InvalidRequestException if conversion fails
     */
    public abstract T doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) throws InvalidRequestException;


    @Override
    public T doParse(HttpRequest httpRequest, Map<String, String> urlParameters) throws InvalidRequestException {
        Class<RequestWithModifier<R>> rawType = (Class<RequestWithModifier<R>>) TypeToken.of(getClass()).resolveType(RequestMapperWithOutputModifier.class.getTypeParameters()[0])
                .getRawType();
        try {
            RequestWithModifier<R> request = rawType.getConstructor(null).newInstance(null);
            OutputModifier.Builder outputModifierBuilder = new OutputModifier.Builder();
            if (httpRequest.hasQueryParameter(QueryParameters.CONTENT)) {
                Content content = Content.fromString(httpRequest.getQueryParameter(QueryParameters.CONTENT));
                request.checkContenModifierValid(content);
                outputModifierBuilder.content(content);
            }
            if (httpRequest.hasQueryParameter(QueryParameters.LEVEL)) {
                Level level = Level.fromString(httpRequest.getQueryParameter(QueryParameters.LEVEL));
                request.checkLevelModifierValid(level);
                outputModifierBuilder.level(level);
            }
            if (httpRequest.hasQueryParameter(QueryParameters.EXTENT)) {
                Extent extent = Extent.fromString(httpRequest.getQueryParameter(QueryParameters.EXTENT));
                request.checkExtentModifierValid(extent);
                outputModifierBuilder.extend(extent);
            }
            OutputModifier outputModifier = outputModifierBuilder.build();
            T result = doParse(httpRequest, urlParameters, outputModifier);
            result.setOutputModifier(outputModifier);
            return result;
        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new InvalidRequestException("error resolving request class while trying to determine output modifier constraints", e);
        }
        catch (IllegalArgumentException e) {
            throw new InvalidRequestException("invalid output modifier", e);
        }
    }
}
