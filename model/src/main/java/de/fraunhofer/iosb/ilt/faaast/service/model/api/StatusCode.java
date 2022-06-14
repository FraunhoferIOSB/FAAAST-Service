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
package de.fraunhofer.iosb.ilt.faaast.service.model.api;

/**
 * Model class for defined status codes for {@link Request}.
 */
public enum StatusCode {
    SUCCESS(true),
    SUCCESS_CREATED(true),
    SUCCESS_NO_CONTENT(true),
    CLIENT_FORBIDDEN(false),
    CLIENT_ERROR_BAD_REQUEST(false),
    CLIENT_METHOD_NOT_ALLOWED(false),
    CLIENT_ERROR_RESOURCE_NOT_FOUND(false),
    SERVER_INTERNAL_ERROR(false),
    SERVER_ERROR_BAD_GATEWAY(false);

    private boolean success;

    private StatusCode(boolean success) {
        this.success = success;
    }


    public boolean isSuccess() {
        return success;
    }
}
