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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp.AbstractDppRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.dpp.AbstractDPPResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DigitalProductPassport;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.HasSemantics;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Base class for handling DPP requests with content.
 *
 * @param <T> type of the DPP request
 * @param <U> type of the corresponding DPP response
 */
public abstract class AbstractDppRequestHandler<T extends AbstractDppRequest<U>, U extends AbstractDPPResponse> extends AbstractRequestHandler<T, U> {

    private static final Reference DPP_METADATA_SUBMODEL_SEMANTIC_ID = ReferenceBuilder.global("https://admin-shell.io/idta/cds/dppMetadata/1");
    private static final Reference CONTENT_SPECIFICATION_IDS_SEMANTIC_ID = ReferenceBuilder.global("https://admin-shell.io/idta/cds/contentSpecificationIds/1");

    /**
     * Get and build a DPP from an AAS as source, fetching its metadata and content submodels.
     *
     * @param shell The DPP shell.
     * @param persistence The persistence where the DPP submodels are stored.
     * @return A complete DigitalProductPassport with resolved metadata and contents.
     * @throws ResourceNotFoundException Any of the submodels were not found.
     * @throws PersistenceException Requesting the persistence failed.
     */
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

        List<Reference> contentSpecificationIds = getContentSpecificationIdsAsReferences(metadata);

        // Remove submodels where semantic ID does not appear in contentSpecificationId list
        submodels.removeIf(sm -> sm.getSemanticId() == null || contentSpecificationIds.stream().noneMatch(contentId -> hasCorrectSemanticId(sm, contentId)));

        return DigitalProductPassport.builder()
                .aas(shell)
                .metadata(metadata)
                .contents(submodels)
                .build();
    }


    private Submodel getMetadataSubmodel(List<Submodel> submodels) throws ResourceNotFoundException {
        return submodels.stream()
                .filter(Objects::nonNull)
                .filter(elem -> hasCorrectSemanticId(elem, DPP_METADATA_SUBMODEL_SEMANTIC_ID))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("DPP Metadata Submodel not found"));
    }


    private List<Reference> getContentSpecificationIdsAsReferences(Submodel metadataSubmodel) {
        return metadataSubmodel.getSubmodelElements()
                .stream()
                // find the contentSpecificationId SML
                .filter(SubmodelElementList.class::isInstance)
                .filter(elem -> hasCorrectSemanticId(elem, CONTENT_SPECIFICATION_IDS_SEMANTIC_ID))
                .map(SubmodelElementList.class::cast)
                .map(SubmodelElementList::getValue)
                .findFirst()
                // find all correctly formed content specification ids
                .map(s -> s.stream()
                        .filter(Property.class::isInstance)
                        .map(Property.class::cast)
                        .map(Property::getValue)
                        .map(ReferenceBuilder::global)
                        .toList())
                .orElse(List.of());
    }


    private boolean hasCorrectSemanticId(HasSemantics hasSemantics, Reference toMatch) {
        Reference semanticId = hasSemantics.getSemanticId();
        return semanticId != null && ReferenceHelper.equals(semanticId, toMatch);
    }


    /**
     * For a read DPP, Shell and Submodel read events need to be emitted.
     *
     * @param dpp The DPP that was read during a request.
     * @param messageBus The message bus used to distribute events.
     * @throws MessageBusException Distributing events failed.
     */
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
