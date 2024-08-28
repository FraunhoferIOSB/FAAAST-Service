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
package de.fraunhofer.iosb.ilt.faaast.service.model.exception;

import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Indicates that errors with the storage occured.
 */
public class StorageException extends Exception {

    private static final String BASE_MSG = "Storage error.";

    public StorageException(String message) {
        super(message);
    }


    public StorageException(Reference reference, Throwable cause) {
        this(String.format("%s (reference: %s)", BASE_MSG, ReferenceHelper.toString(reference)), cause);
    }


    public StorageException(Reference reference) {
        this(String.format("%s (reference: %s)", BASE_MSG, ReferenceHelper.toString(reference)));
    }


    public StorageException(String id, Class<? extends Identifiable> type, Throwable cause) {
        this(String.format("%s (id: %s, type: %s)", BASE_MSG, id, type), cause);
    }


    public StorageException(String id, Class<? extends Identifiable> type) {
        this(String.format("%s (id: %s, type: %s)", BASE_MSG, id, type));
    }


    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }


    public StorageException(Throwable cause) {
        super(cause);
    }

}
