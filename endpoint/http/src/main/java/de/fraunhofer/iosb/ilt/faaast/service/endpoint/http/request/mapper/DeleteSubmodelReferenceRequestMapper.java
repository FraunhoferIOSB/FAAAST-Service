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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.DeleteSubmodelReferenceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import io.adminshell.aas.v3.dataformat.core.ReflectionHelper;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.Reference;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;


/**
 * class to map HTTP-DELETE-Request path:
 * shells/{aasIdentifier}/aas/submodels/{submodelIdentifier}
 */
public class DeleteSubmodelReferenceRequestMapper extends AbstractRequestMapper {

    private static final String AAS_ID = RegExHelper.uniqueGroupName();
    private static final String SUBMODEL_ID = RegExHelper.uniqueGroupName();
    private static final String PATTERN = String.format("shells/(?<%s>.*)/aas/submodels/(?<%s>.*)", AAS_ID, SUBMODEL_ID);

    public DeleteSubmodelReferenceRequestMapper(ServiceContext serviceContext) {
        super(serviceContext, HttpMethod.DELETE, PATTERN);
    }


    @Override
    public Request doParse(HttpRequest httpRequest, Map<String, String> urlParameters) {
        return DeleteSubmodelReferenceRequest.builder()
                .id(IdentifierHelper.parseIdentifier(EncodingHelper.base64Decode(urlParameters.get(AAS_ID))))
                .submodelRef(toReference(EncodingHelper.base64Decode(urlParameters.get(SUBMODEL_ID))))
                .build();
    }


    private Reference toReference(String id) {
        Reference result;
        try {
            result = ReflectionHelper.getDefaultImplementation(Reference.class).getConstructor().newInstance();
            Key key = ReflectionHelper.getDefaultImplementation(Key.class).getConstructor().newInstance();
            key.setIdType(IdentifierHelper.guessKeyType(id));
            key.setType(KeyElements.SUBMODEL);
            key.setValue(id);
            result.setKeys(Arrays.asList(key));
            return result;

        }
        catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            throw new IllegalArgumentException("error parsing reference from id", e);
        }
    }
}
