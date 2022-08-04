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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.SetSubmodelElementValueByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.SetSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValueParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementPathHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * class to map HTTP-PUT-Request path:
 * submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}
 * <br>
 * shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}/submodel/submodel-elements/{idShortPath}
 */
public class SetSubmodelElementValueByPathRequestMapper extends SubmodelInterfaceRequestMapper<SetSubmodelElementValueByPathRequest<?>, SetSubmodelElementValueByPathResponse> {

    private static final String SUBMODEL_ELEMENT_PATH = "submodelElementPath";
    private static final String PATTERN = String.format("submodel-elements/(?<%s>.*)", SUBMODEL_ELEMENT_PATH);
    private static final String QUERY_PARAMETER_CONTENT = "content";
    private static final String QUERY_PARAMETER_CONTENT_VALUE = "value";

    public SetSubmodelElementValueByPathRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.PUT, PATTERN);
    }


    @Override
    public boolean matches(HttpRequest httpRequest) {
        return super.matches(httpRequest)
                && httpRequest.hasQueryParameter(QUERY_PARAMETER_CONTENT)
                && Objects.equals(httpRequest.getQueryParameter(QUERY_PARAMETER_CONTENT), QUERY_PARAMETER_CONTENT_VALUE);
    }


    @Override
    public SetSubmodelElementValueByPathRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier) throws InvalidRequestException {
        final List<Key> path = ElementPathHelper.toKeys(EncodingHelper.urlDecode(urlParameters.get(SUBMODEL_ELEMENT_PATH)));
        final Identifier identifier = IdentifierHelper.parseIdentifier(EncodingHelper.base64Decode(urlParameters.get(SUBMODEL_ID)));
        return SetSubmodelElementValueByPathRequest.builder()
                .path(path)
                .value(httpRequest.getBody())
                .valueParser(new ElementValueParser<Object>() {
                    @Override
                    public <U extends ElementValue> U parse(Object raw, Class<U> type) throws DeserializationException {
                        if (ElementValue.class.isAssignableFrom(type)) {
                            return deserializer.readValue(raw.toString(), serviceContext.getTypeInfo(ReferenceHelper.toReference(path, identifier, Submodel.class)));
                        }
                        else if (SubmodelElement.class.isAssignableFrom(type)) {
                            SubmodelElement submodelElement = (SubmodelElement) deserializer.read(raw.toString(), type);
                            try {
                                return ElementValueMapper.toValue(submodelElement);
                            }
                            catch (ValueMappingException e) {
                                throw new DeserializationException("error mapping submodel element to value object", e);
                            }
                        }
                        throw new DeserializationException(
                                String.format("error deserializing payload - invalid type '%s' (must be either instance of ElementValue or SubmodelElement",
                                        type.getSimpleName()));
                    }
                })
                .build();
    }

}
