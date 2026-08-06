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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.dpp.request.mapper.dpp;

import static de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DppSerializationMode.DEFAULT;
import static java.util.Optional.ofNullable;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.dpp.request.mapper.AbstractRequestMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DppSerializationMode;
import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import javax.annotation.Nonnull;


/**
 * abstract class for DPP request mappers which may contain a serialization mode in the query parameter.
 */
public abstract class AbstractDppRequestMapperWithSerializationMode extends AbstractRequestMapper {

    protected AbstractDppRequestMapperWithSerializationMode(ServiceContext serviceContext, HttpMethod method, String urlPattern) {
        super(serviceContext, method, urlPattern);
    }


    /**
     * Returns a DppSerialization mode for a representation parameter string, or the default mode for no parameter.
     * 
     * @param representationParameter Representation parameter of the DPP request.
     * @return DppSerializationMode corresponding to the representation parameter.
     * @throws IllegalArgumentException if the representation parameter is unknown.
     */
    protected final @Nonnull DppSerializationMode parseSerializationMode(String representationParameter) {
        return ofNullable(representationParameter)
                .map(DppSerializationMode::parse)
                .orElse(DEFAULT);
    }
}
