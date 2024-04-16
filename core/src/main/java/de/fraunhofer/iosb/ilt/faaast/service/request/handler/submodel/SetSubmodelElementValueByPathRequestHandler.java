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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.SetSubmodelElementValueByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.SetSubmodelElementValueByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.SetSubmodelElementValueByPathRequest} in the service
 * and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.SetSubmodelElementValueByPathResponse}. Is
 * responsible for communication with the persistence and sends the corresponding events to the message bus.
 */
public class SetSubmodelElementValueByPathRequestHandler
        extends AbstractSubmodelInterfaceRequestHandler<SetSubmodelElementValueByPathRequest<?>, SetSubmodelElementValueByPathResponse> {

    public SetSubmodelElementValueByPathRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public SetSubmodelElementValueByPathResponse doProcess(SetSubmodelElementValueByPathRequest request) throws Exception {
        if (request == null || request.getValueParser() == null) {
            throw new IllegalArgumentException("value parser of request must be non-null");
        }
        SetSubmodelElementValueByPathResponse response = new SetSubmodelElementValueByPathResponse();
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        SubmodelElement submodelElement = context.getPersistence().getSubmodelElement(
                reference,
                new OutputModifier.Builder()
                        .extend(Extent.WITH_BLOB_VALUE)
                        .build());
        ElementValue oldValue = ElementValueMapper.toValue(submodelElement);
        ElementValue newValue = request.getValueParser().parse(request.getRawValue(), oldValue.getClass());
        ElementValueMapper.setValue(submodelElement, newValue);
        if (request.isSyncWithAsset()) {
            context.getAssetConnectionManager().setValue(reference, newValue);
        }
        try {
            context.getPersistence().update(reference, submodelElement);
        }
        catch (IllegalArgumentException e) {
            // empty on purpose
        }
        response.setStatusCode(StatusCode.SUCCESS_NO_CONTENT);
        if (!request.isInternal()) {
            context.getMessageBus().publish(ValueChangeEventMessage.builder()
                    .element(reference)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .build());
        }
        return response;
    }

}
