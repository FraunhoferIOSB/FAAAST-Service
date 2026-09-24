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
 * Multiple persistence operations. All operations executed with the same transaction either
 * all take effect or none of them do.
 *
 */
public interface Transaction extends AutoCloseable {

    /**
     * Indicates whether the transaction has been committed or rolled back.
     *
     * @return true if the transaction is still active, false otherwise
     */
    public boolean isActive();


    /**
     * Makes all changes performed within this transaction permanent and ends the transaction.
     *
     * @throws IllegalStateException if the transaction is no longer active
     * @throws de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException if committing fails
     */
    public void commit();


    /**
     * Discards all changes performed within this transaction and ends the transaction.
     *
     * @throws IllegalStateException if the transaction is no longer active
     * @throws de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException if rolling back fails
     */
    public void rollback();


    /**
     * Releases all resources held by this transaction, rolling it back if it is still active.
     */
    @Override
    public void close();
}
