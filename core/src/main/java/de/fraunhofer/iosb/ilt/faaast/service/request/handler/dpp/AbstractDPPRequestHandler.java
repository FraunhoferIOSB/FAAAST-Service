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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.dpp;

import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp.AbstractDPPRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.dpp.AbstractDPPResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DigitalProductPassport;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;


public abstract class AbstractDPPRequestHandler<T extends AbstractDPPRequest<U>, U extends AbstractDPPResponse> extends AbstractRequestHandler<T, U> {

    protected DigitalProductPassport buildFrom(AssetAdministrationShell shell, Persistence<?> persistence) throws ResourceNotFoundException, PersistenceException {
        List<String> submodelIds = shell.getSubmodels().stream()
                .map(ReferenceHelper::getEffectiveKey).filter(Objects::nonNull)
                .map(Key::getValue)
                .toList();

        List<Submodel> submodels = new ArrayList<>();
        for (String id: submodelIds) {
            submodels.add(persistence.getSubmodel(id, QueryModifier.MAXIMAL));
        }
        Submodel metadata = getMetadataSubmodel(submodels);
        submodels.remove(metadata);

        return DigitalProductPassport.builder()
                .aas(shell)
                .metadata(metadata)
                .contents(submodels)
                .build();

    }


    private Submodel getMetadataSubmodel(List<Submodel> submodels) throws ResourceNotFoundException {
        return submodels.stream()
                .filter(sm -> sm.getSemanticId() != null)
                .filter(sm -> sm.getSemanticId().getKeys() != null)
                .filter(sm -> !sm.getSemanticId().getKeys().isEmpty())
                .filter(sm -> Objects.requireNonNull(ReferenceHelper.getEffectiveKey(sm.getSemanticId())).getValue().equals("DPP_SEMANTIC_ID"))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DPP Metadata Submodel not found"));
    }


    protected void distributeReadEvents(DigitalProductPassport dpp, MessageBus<?> messageBus) throws MessageBusException {
        messageBus.publish(ElementReadEventMessage.builder()
                .element(dpp.getAAS())
                .value(dpp.getAAS())
                .build());
        messageBus.publish(ElementReadEventMessage.builder()
                .element(dpp.getMetadata())
                .value(dpp.getMetadata())
                .build());
        for (Submodel sm: dpp.getContents()) {
            messageBus.publish(ElementReadEventMessage.builder()
                    .element(sm)
                    .value(sm)
                    .build());
        }
    }
}
