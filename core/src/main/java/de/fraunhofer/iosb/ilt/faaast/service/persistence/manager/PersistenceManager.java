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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.manager;

import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;


/**
 * Base class for implementing code to execute a request to the persistence.
 */
public abstract class PersistenceManager {

    protected static final String ERROR_MSG_RESOURCE_NOT_FOUND_BY_REF = "Resource not found by reference %s";
    protected static final String ERROR_MSG_RESOURCE_NOT_FOUND_BY_ID = "Resource not found by id %s";

    protected AssetAdministrationShellEnvironment aasEnvironment;

    public void setAasEnvironment(AssetAdministrationShellEnvironment aasEnvironment) {
        this.aasEnvironment = aasEnvironment;
    }


    /**
     * Helper method to ensure persistence manager is property initialized.
     *
     * @throws IllegalStateException if not properly initialized (aasEnvironment
     *             == null)
     */
    protected void ensureInitialized() {
        Ensure.requireNonNull(aasEnvironment, "aasEnvironment not properly initialized (must be non-null)");
    }

}
