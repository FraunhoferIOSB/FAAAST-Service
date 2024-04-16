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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PatchSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PatchSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValidationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidator;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PatchSubmodelElementByPathRequest}.
 */
public class PatchSubmodelElementByPathRequestHandler extends AbstractSubmodelInterfaceRequestHandler<PatchSubmodelElementByPathRequest, PatchSubmodelElementByPathResponse> {

    public PatchSubmodelElementByPathRequestHandler(RequestExecutionContext context) {
        super(context);
    }


    @Override
    public PatchSubmodelElementByPathResponse doProcess(PatchSubmodelElementByPathRequest request)
            throws ResourceNotFoundException, ValueMappingException, AssetConnectionException, MessageBusException, ValidationException, ResourceNotAContainerElementException,
            InvalidRequestException {
        Submodel current = context.getPersistence().getSubmodel(request.getSubmodelId(), QueryModifier.DEFAULT);
        Submodel updated = applyMergePatch(request.getChanges(), current, Submodel.class);
        context.getPersistence().save(updated);
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        SubmodelElement oldSubmodelElement = context.getPersistence().getSubmodelElement(reference, QueryModifier.DEFAULT);
        SubmodelElement newSubmodelElement = applyMergePatch(request.getChanges(), oldSubmodelElement, SubmodelElement.class);
        ModelValidator.validate(newSubmodelElement, context.getCoreConfig().getValidationOnUpdate());
        context.getPersistence().update(reference, newSubmodelElement);
        cleanupDanglingAssetConnectionsForParent(reference, context.getPersistence());
        if (!request.isInternal() && Objects.isNull(oldSubmodelElement)) {
            context.getMessageBus().publish(ElementCreateEventMessage.builder()
                    .element(reference)
                    .value(newSubmodelElement)
                    .build());
        }
        else if (Objects.equals(oldSubmodelElement.getClass(), newSubmodelElement.getClass())
                && ElementValueHelper.isSerializableAsValue(oldSubmodelElement.getClass())) {
            ElementValue oldValue = ElementValueMapper.toValue(oldSubmodelElement);
            ElementValue newValue = ElementValueMapper.toValue(newSubmodelElement);
            if (!Objects.equals(oldValue, newValue)) {
                context.getAssetConnectionManager().setValue(reference, newValue);
                if (!request.isInternal()) {
                    context.getMessageBus().publish(ValueChangeEventMessage.builder()
                            .element(reference)
                            .oldValue(oldValue)
                            .newValue(newValue)
                            .build());
                }
            }
        }
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementUpdateEventMessage.builder()
                    .element(reference)
                    .value(newSubmodelElement)
                    .build());
        }
        return PatchSubmodelElementByPathResponse.builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
    }
}
