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
package de.fraunhofer.iosb.ilt.faaast.service.eventlistener.mqtt.actions;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.eventlistener.mqtt.HttpProvider;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EventListenerException;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Calls an operation.
 */
public class CallOperation implements Action {
    private static final Logger LOGGER = LoggerFactory.getLogger(Action.class);
    private final Reference reference;

    public CallOperation(Reference reference) {
        this.reference = reference;
    }


    @Override
    public void execute(HttpProvider httpProvider) throws EventListenerException {
        try {
            String inputArguments = "{}";
            LOGGER.debug("Event listener will execute action: call operation");
            String submodelId = ReferenceHelper.getParent(reference).getKeys().get(0).getValue();
            //@TODO do no hard-code port
            HttpResponse response = HttpHelper.execute(
                    httpProvider.getHttpClient(),
                    URI.create("https://" + httpProvider.getBaseUrl()
                            + ":8191/api/v3.0/submodels/"
                            + Base64.getEncoder().encodeToString(submodelId.getBytes())
                            + "/submodel/submodel-elements/").toURL(),
                    ReferenceHelper.toPath(reference)
                            + "/invoke",
                    "JSON",
                    "POST",
                    HttpRequest.BodyPublishers.ofByteArray(inputArguments.getBytes()),
                    HttpResponse.BodyHandlers.ofByteArray(), null);
            LOGGER.debug("Event listener successfully executed the action.");
        }
        catch (Exception e) {
            throw new EventListenerException(e);
        }
    }

}
