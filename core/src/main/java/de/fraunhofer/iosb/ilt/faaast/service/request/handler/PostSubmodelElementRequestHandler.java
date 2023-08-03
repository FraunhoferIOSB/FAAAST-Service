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

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostSubmodelElementResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.request.PostSubmodelElementRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidator;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * Class to handle a {@link de.fraunhofer.iosb.ilt.faaast.service.model.request.PostSubmodelElementRequest} in the
 * service and to send the corresponding response
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PostSubmodelElementResponse}. Is responsible for
 * communication with the persistence and sends the corresponding events to the message bus.
 */
public class PostSubmodelElementRequestHandler extends AbstractSubmodelInterfaceRequestHandler<PostSubmodelElementRequest, PostSubmodelElementResponse> {

    public PostSubmodelElementRequestHandler(CoreConfig coreConfig, Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        super(coreConfig, persistence, messageBus, assetConnectionManager);
    }


    @Override
    public PostSubmodelElementResponse doProcess(PostSubmodelElementRequest request) throws ResourceNotFoundException, ValueMappingException, Exception {
        ModelValidator.validate(request.getSubmodelElement(), coreConfig.getValidationOnCreate());
        Reference parentReference = ReferenceBuilder.forSubmodel(request.getSubmodelId());
        Reference childReference = AasUtils.toReference(parentReference, request.getSubmodelElement());
        SubmodelElement submodelElement = persistence.put(parentReference, null, request.getSubmodelElement());
        if (ElementValueHelper.isSerializableAsValue(submodelElement.getClass())) {
            assetConnectionManager.setValue(childReference, ElementValueMapper.toValue(submodelElement));
        }
        messageBus.publish(ElementCreateEventMessage.builder()
                .element(parentReference)
                .value(submodelElement)
                .build());
        return PostSubmodelElementResponse.builder()
                .payload(submodelElement)
                .created()
                .build();
    }
}
