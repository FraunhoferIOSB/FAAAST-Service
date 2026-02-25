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
package de.fraunhofer.iosb.ilt.faaast.service.registry;

import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.net.http.HttpRequest;


/**
 * Adds a constant header to all registry synchronization requests.
 */
public class StaticHeaderRegistryRequestDecorator implements RegistryRequestDecorator {

    private final String headerName;
    private final String headerValue;

    public StaticHeaderRegistryRequestDecorator(String headerName, String headerValue) {
        Ensure.requireNonNull(headerName, "headerName must be non-null");
        Ensure.requireNonNull(headerValue, "headerValue must be non-null");
        this.headerName = headerName;
        this.headerValue = headerValue;
    }


    @Override
    public void decorate(HttpRequest.Builder builder) {
        builder.header(headerName, headerValue);
    }
}
