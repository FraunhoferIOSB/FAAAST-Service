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
package de.fraunhofer.iosb.ilt.faaast.service.exception;

import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;


/**
 * Indicates that a desired resource cannot be found.
 */
public class ResourceNotFoundException extends Exception {

    public ResourceNotFoundException(String message) {
        super(message);
    }


    public ResourceNotFoundException(Reference reference, Throwable cause) {
        this("Resource not found with ID Path: " + AasUtils.asString(reference), cause);
    }


    public ResourceNotFoundException(Reference reference) {
        this("Resource not found with ID Path: " + AasUtils.asString(reference));
    }


    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }


    public ResourceNotFoundException(Throwable cause) {
        super(cause);
    }

}
