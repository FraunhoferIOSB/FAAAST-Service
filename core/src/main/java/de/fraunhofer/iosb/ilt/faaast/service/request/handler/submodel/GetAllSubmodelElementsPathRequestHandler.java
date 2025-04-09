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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.submodel;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetAllSubmodelElementsPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetAllSubmodelElementsPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.ReferenceCollector;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetAllSubmodelElementsRequest} in the service
 * and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetAllSubmodelElementsResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class GetAllSubmodelElementsPathRequestHandler extends AbstractSubmodelInterfaceRequestHandler<GetAllSubmodelElementsPathRequest, GetAllSubmodelElementsPathResponse> {

    private static <T> Page<T> preparePagedResult(Stream<T> input, PagingInfo paging) {
        Stream<T> result = input;
        if (Objects.nonNull(paging.getCursor())) {
            result = result.skip(readCursor(paging.getCursor()));
        }
        if (paging.hasLimit()) {
            result = result.limit(paging.getLimit() + 1);
        }
        List<T> temp = result.collect(Collectors.toList());
        return Page.<T> builder()
                .result(temp.stream()
                        .limit(paging.hasLimit() ? paging.getLimit() : temp.size())
                        .collect(Collectors.toList()))
                .metadata(PagingMetadata.builder()
                        .cursor(nextCursor(paging, temp.size()))
                        .build())
                .build();
    }


    private static long readCursor(String cursor) {
        return Long.parseLong(cursor);
    }


    private static String writeCursor(long index) {
        return Long.toString(index);
    }


    private static String nextCursor(PagingInfo paging, boolean hasMoreData) {
        if (!hasMoreData) {
            return null;
        }
        if (!paging.hasLimit()) {
            throw new IllegalStateException("unable to generate next cursor for paging - there should not be more data available if previous request did not have a limit set");
        }
        if (Objects.isNull(paging.getCursor())) {
            return writeCursor(paging.getLimit());
        }
        return writeCursor(readCursor(paging.getCursor()) + paging.getLimit());
    }


    private static String nextCursor(PagingInfo paging, int resultCount) {
        return nextCursor(paging, paging.hasLimit() && resultCount > paging.getLimit());
    }


    @Override
    public GetAllSubmodelElementsPathResponse doProcess(GetAllSubmodelElementsPathRequest request, RequestExecutionContext context)
            throws AssetConnectionException, ValueMappingException, ResourceNotFoundException, MessageBusException, ResourceNotAContainerElementException, PersistenceException {
        Reference reference = ReferenceBuilder.forSubmodel(request.getSubmodelId());
        Page<SubmodelElement> submodelElements = context.getPersistence().getSubmodelElements(reference, request.getOutputModifier(), PagingInfo.ALL);
        Page<IdShortPath> page;
        page = preparePagedResult(submodelElements.getContent().stream()
                .flatMap(x -> ReferenceCollector.collect(x).keySet().stream()
                        .map(y -> IdShortPath.combine(
                                IdShortPath.builder().idShort(x.getIdShort()).build(),
                                IdShortPath.fromReference(y))))
                .sorted((x, y) -> x.toString().compareTo(y.toString())),
                request.getPagingInfo());
        if (!request.isInternal() && Objects.nonNull(submodelElements.getContent())) {
            submodelElements.getContent().forEach(LambdaExceptionHelper.rethrowConsumer(
                    x -> context.getMessageBus().publish(ElementReadEventMessage.builder()
                            .element(AasUtils.toReference(reference, x))
                            .value(x)
                            .build())));
        }
        return GetAllSubmodelElementsPathResponse.builder()
                .payload(page)
                .success()
                .build();
    }

}
