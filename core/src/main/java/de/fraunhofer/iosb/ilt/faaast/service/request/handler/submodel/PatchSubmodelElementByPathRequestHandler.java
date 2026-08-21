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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PatchSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PatchSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidator;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * Class to handle a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PatchSubmodelElementByPathRequest}.
 */
public class PatchSubmodelElementByPathRequestHandler extends AbstractSubmodelInterfaceRequestHandler<PatchSubmodelElementByPathRequest, PatchSubmodelElementByPathResponse> {

    private record PatchOutcome(SubmodelElement oldSubmodelElement, SubmodelElement newSubmodelElement) {}

    @Override
    public PatchSubmodelElementByPathResponse doProcess(PatchSubmodelElementByPathRequest request, RequestExecutionContext context)
            throws Exception {
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        PatchOutcome outcome = context.getPersistence().inTransaction(tx -> {
            Submodel current = tx.getSubmodel(request.getSubmodelId(), QueryModifier.DEFAULT);
            Submodel updated = applyMergePatch(request.getChanges(), current, Submodel.class);
            tx.save(updated);
            SubmodelElement oldSubmodelElement = tx.getSubmodelElement(reference, QueryModifier.DEFAULT);
            SubmodelElement newSubmodelElement = applyMergePatch(request.getChanges(), oldSubmodelElement, SubmodelElement.class);
            ModelValidator.validate(newSubmodelElement, context.getCoreConfig().getValidationOnUpdate());
            tx.update(reference, newSubmodelElement);
            return new PatchOutcome(oldSubmodelElement, newSubmodelElement);
        });
        context.getAssetConnectionManager().cleanupDanglingConnectionsAfterModify(reference);
        context.getAssetConnectionManager().syncValueProvidersOnWrite(
                reference, outcome.oldSubmodelElement(), outcome.newSubmodelElement(), !request.isInternal());
        if (!request.isInternal()) {
            context.getMessageBus().publish(ElementUpdateEventMessage.builder()
                    .element(reference)
                    .value(outcome.newSubmodelElement())
                    .build());
        }
        return PatchSubmodelElementByPathResponse.builder()
                .statusCode(StatusCode.SUCCESS_NO_CONTENT)
                .build();
    }
}
