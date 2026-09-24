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
package de.fraunhofer.iosb.ilt.faaast.service.persistence;

/**
 * A Transaction for persistence implementations that do not support grouping.
 * Rollback does nothing.
 */
public class NoopTransaction implements Transaction {

    private boolean active = true;

    @Override
    public boolean isActive() {
        return active;
    }


    @Override
    public void commit() {
        ensureActive();
        active = false;
    }


    @Override
    public void rollback() {
        ensureActive();
        active = false;
    }


    @Override
    public void close() {
        active = false;
    }


    private void ensureActive() {
        if (!active) {
            throw new IllegalStateException("transaction is no longer active");
        }
    }
}
