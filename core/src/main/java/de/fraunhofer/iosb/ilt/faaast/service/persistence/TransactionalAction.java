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
 * An action within a transaction, returning a result.
 *
 * @param <R> type of the result
 */
@FunctionalInterface
public interface TransactionalAction<R> {

    /**
     * Executes the action.
     *
     * @param persistence the persistence to use for all operations within this action. Using any other instance runs
     *            outside the transaction.
     * @return the result of the action
     * @throws Exception if the action fails; causes the transaction to be rolled back
     */
    public R execute(Persistence<?> persistence) throws Exception;
}
