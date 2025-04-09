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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.submodel;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper.AbstractSubmodelInterfaceRequestMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.PatchSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PatchSubmodelElementValueByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValueParser;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * class to map HTTP-PATCH-Request path: submodels/{submodelIdentifier}/submodel-elements/{idShortPath},
 * shells/{aasIdentifier}/submodels/{submodelIdentifier}/submodel-elements/{idShortPath}.
 */
public class PatchSubmodelElementValueByPathRequestMapper
        extends AbstractSubmodelInterfaceRequestMapper<PatchSubmodelElementValueByPathRequest<?>, PatchSubmodelElementValueByPathResponse> {

    private static final String SUBMODEL_ELEMENT_PATH = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("submodel-elements/%s/\\$value", pathElement(SUBMODEL_ELEMENT_PATH));

    public PatchSubmodelElementValueByPathRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.PATCH, PATTERN);
    }


    @Override
    public PatchSubmodelElementValueByPathRequest doParse(HttpRequest httpRequest, Map<String, String> urlParameters, OutputModifier outputModifier)
            throws InvalidRequestException {
        final String path = EncodingHelper.urlDecode(urlParameters.get(SUBMODEL_ELEMENT_PATH));
        final String identifier = getParameterBase64UrlEncoded(urlParameters, SUBMODEL_ID);
        return PatchSubmodelElementValueByPathRequest.builder()
                .path(path)
                .value(httpRequest.getBodyAsString())
                .valueParser(new ElementValueParser<Object>() {
                    @Override
                    public <U extends ElementValue> U parse(Object raw, Class<U> type) throws DeserializationException {
                        String rawString;
                        if (raw.getClass().isAssignableFrom(byte[].class)) {
                            rawString = new String((byte[]) raw);
                        }
                        else {
                            rawString = raw.toString();
                        }
                        if (ElementValue.class.isAssignableFrom(type)) {
                            try {
                                return deserializer.readValue(
                                        rawString,
                                        serviceContext.getTypeInfo(
                                                new ReferenceBuilder()
                                                        .submodel(identifier)
                                                        .idShortPath(path)
                                                        .build()));
                            }
                            catch (ResourceNotFoundException | PersistenceException e) {
                                throw new DeserializationException("unable to obtain type information as resource does not exist or storage failed", e);
                            }
                        }
                        else if (SubmodelElement.class.isAssignableFrom(type)) {
                            SubmodelElement submodelElement = (SubmodelElement) deserializer.read(rawString, type);
                            try {
                                return ElementValueMapper.toValue(submodelElement, type);
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
