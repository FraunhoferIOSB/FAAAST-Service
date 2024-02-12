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
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


public class CallOperation implements Action {
    private final Reference reference;

    public CallOperation(Reference reference) {
        this.reference = reference;
    }


    @Override
    public void execute(HttpProvider httpProvider) throws EventListenerException {
        try {
            HttpResponse response = HttpHelper.execute(
                    httpProvider.getHttpClient(),
                    URI.create("https://" + httpProvider.getBaseUrl() + ReferenceHelper.toPath(reference)).toURL(),
                    "path",
                    "format",
                    "method",
                    HttpRequest.BodyPublishers.noBody(),
                    HttpResponse.BodyHandlers.ofString(), null);
        }
        catch (Exception e) {
            throw new EventListenerException(e);
        }
    }

}
