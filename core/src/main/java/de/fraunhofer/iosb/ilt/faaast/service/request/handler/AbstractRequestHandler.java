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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import com.google.common.reflect.TypeToken;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.DeserializationException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;


/**
 * Base class for implementing code to execute a given Request.
 *
 * @param <I> type of the request
 * @param <O> type of the corresponding response
 */
public abstract class AbstractRequestHandler<I extends Request<O>, O extends Response> {

    /**
     * Creates a empty response object.
     *
     * @return new empty response object
     * @throws NoSuchMethodException if response type does not implement a
     *             parameterless constructor
     * @throws InstantiationException if response type is abstract
     * @throws InvocationTargetException if parameterless constructor of
     *             response type throws an exception
     * @throws IllegalAccessException if parameterless constructor of response
     *             type is inaccessible
     */
    public O newResponse() throws NoSuchMethodException, InstantiationException, InvocationTargetException, IllegalAccessException {
        return (O) ConstructorUtils.invokeConstructor(
                TypeToken.of(getClass())
                        .resolveType(AbstractRequestHandler.class.getTypeParameters()[1])
                        .getRawType());
    }


    /**
     * Processes a request and returns the resulting response.
     *
     * @param request the request
     * @param context the execution context
     * @return the response
     * @throws Exception if processing the request fails
     */
    public abstract O process(I request, RequestExecutionContext context) throws Exception;


    /**
     * Creates an updated element based on a JSON merge patch.
     *
     * @param <T> the type of the element to update
     * @param patch the JSON merge patch containing the changes to apply
     * @param targetBean the original element to apply the update to
     * @param type the type information
     * @return the updated element
     * @throws
     * de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException
     *             if applying the merge patch fails
     */
    protected <T> T applyMergePatch(JsonMergePatch patch, T targetBean, Class<T> type) throws InvalidRequestException {
        try {
            JsonNode json = new JsonSerializer().toNode(targetBean);
            JsonNode updatedJson = patch.apply(json);
            return new JsonDeserializer().read(updatedJson, type);
        }
        catch (JsonPatchException | IllegalArgumentException | DeserializationException e) {
            throw new InvalidRequestException("Error applying JSON merge patch", e);
        }
    }


    /**
     * Parses a {@code SpecificAssetId} as {@code AssetIdentification}.
     *
     * @param specificAssetId the input to parse
     * @return the parsed output
     */
    protected AssetIdentification parseSpecificAssetId(SpecificAssetId specificAssetId) {
        if (Objects.isNull(specificAssetId)) {
            return null;
        }
        if (specificAssetId.getName().equalsIgnoreCase(FaaastConstants.KEY_GLOBAL_ASSET_ID)) {
            return new GlobalAssetIdentification.Builder()
                    .value(specificAssetId.getValue())
                    .build();
        }
        return new SpecificAssetIdentification.Builder()
                .value(specificAssetId.getValue())
                .key(specificAssetId.getName())
                .build();
    }


    /**
     * Parses a list of {@code SpecificAssetId} as {@code AssetIdentification}.
     *
     * @param specificAssetIds the input to parse
     * @return the parsed output
     */
    protected List<AssetIdentification> parseSpecificAssetIds(List<SpecificAssetId> specificAssetIds) {
        if (Objects.isNull(specificAssetIds)) {
            return List.of();
        }
        return specificAssetIds.stream()
                .map(this::parseSpecificAssetId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
