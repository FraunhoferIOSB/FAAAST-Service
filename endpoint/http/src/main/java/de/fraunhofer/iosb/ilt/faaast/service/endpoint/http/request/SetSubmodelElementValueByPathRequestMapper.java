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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.SetSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.valuedata.ElementValueParser;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.core.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementPathUtils;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingUtils;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdUtils;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.List;
import java.util.Objects;


/**
 * class to map HTTP-PUT-Request path:
 * submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}
 */
public class SetSubmodelElementValueByPathRequestMapper extends RequestMapper {

    private static final HttpMethod HTTP_METHOD = HttpMethod.PUT;
    private static final String PATTERN = "^submodels/(.*?)/submodel/submodel-elements/(.*)$";
    private static final String QUERYPARAM1 = "content";
    private static final String QUERYVALUE1 = "value";

    public SetSubmodelElementValueByPathRequestMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    public Request parse(HttpRequest httpRequest) {
        final List<Key> path = ElementPathUtils.toKeys(EncodingUtils.urlDecode(httpRequest.getPathElements().get(4)));
        return SetSubmodelElementValueByPathRequest.builder()
                .id(IdUtils.parseIdentifier(EncodingUtils.base64Decode(httpRequest.getPathElements().get(1))))
                .path(path)
                .value(httpRequest.getBody())
                .valueParser(new ElementValueParser<Object>() {
                    @Override
                    public <U extends ElementValue> U parse(Object raw, Class<U> type) throws DeserializationException {
                        if (ElementValue.class.isAssignableFrom(type)) {
                            return deserializer.readValue(raw.toString(), serviceContext.getTypeInfo(ElementPathUtils.toReference(path)));
                        }
                        else if (SubmodelElement.class.isAssignableFrom(type)) {
                            SubmodelElement submodelElement = (SubmodelElement) deserializer.read(raw.toString(), type);
                            return ElementValueMapper.toValue(submodelElement);
                        }
                        throw new DeserializationException(
                                String.format("error deserializing payload - invalid type '%s' (must be either instance of ElementValue or SubmodelElement",
                                        type.getSimpleName()));
                    }
                })
                .build();
    }


    @Override
    public boolean matches(HttpRequest httpRequest) {
        return httpRequest.getMethod().equals(HTTP_METHOD)
                && httpRequest.getPath().matches(PATTERN)
                && (httpRequest.getQueryParameters().containsKey(QUERYPARAM1) && Objects.equals(httpRequest.getQueryParameters().get(QUERYPARAM1), QUERYVALUE1));
    }
}
