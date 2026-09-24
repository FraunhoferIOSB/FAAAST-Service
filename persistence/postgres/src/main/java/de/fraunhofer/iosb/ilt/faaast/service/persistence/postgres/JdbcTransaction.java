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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.postgres;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Transaction;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A {@link Transaction} backed by a JDBC connection taken from the connection pool.
 *
 * <p>The connection is switched to manual commit for the lifetime of the transaction and returned to the pool on
 * {@link #close()}.
 */
public class JdbcTransaction implements Transaction {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdbcTransaction.class);

    private final Connection connection;
    private final Connection nonClosingConnection;
    private boolean active;
    private boolean closed;

    /**
     * Starts a transaction on the given connection.
     *
     * @param connection the connection to use, ownership is taken over by this transaction
     * @throws PersistenceException if the connection cannot be switched to manual commit
     */
    public JdbcTransaction(Connection connection) throws PersistenceException {
        this.connection = connection;
        this.nonClosingConnection = nonClosing(connection);
        try {
            connection.setAutoCommit(false);
        }
        catch (SQLException e) {
            closeConnection();
            throw new PersistenceException("Failed to start transaction", e);
        }
        this.active = true;
    }


    /**
     * Gets the connection of this transaction in a form that ignores {@code close()}, so that operations can safely
     * use it in try-with-resources blocks without ending the transaction.
     *
     * @return the connection of this transaction
     */
    public Connection nonClosingConnection() {
        return nonClosingConnection;
    }


    @Override
    public boolean isActive() {
        return active;
    }


    @Override
    public void commit() {
        ensureActive();
        active = false;
        try {
            connection.commit();
        }
        catch (SQLException e) {
            throw new PersistenceException("Failed to commit transaction", e);
        }
    }


    @Override
    public void rollback() {
        ensureActive();
        active = false;
        try {
            connection.rollback();
        }
        catch (SQLException e) {
            throw new PersistenceException("Failed to roll back transaction", e);
        }
    }


    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        try {
            if (active) {
                active = false;
                connection.rollback();
            }
        }
        catch (SQLException e) {
            LOGGER.warn("failed to roll back transaction while closing it", e);
        }
        finally {
            closeConnection();
        }
    }


    private void closeConnection() {
        try {
            connection.setAutoCommit(true);
        }
        catch (SQLException e) {
            LOGGER.debug("failed to reset autoCommit before returning connection to pool", e);
        }
        try {
            connection.close();
        }
        catch (SQLException e) {
            LOGGER.warn("failed to return connection to pool", e);
        }
    }


    private void ensureActive() {
        if (!active) {
            throw new IllegalStateException("transaction is no longer active");
        }
    }


    private static Connection nonClosing(Connection connection) {
        return (Connection) Proxy.newProxyInstance(
                JdbcTransaction.class.getClassLoader(),
                new Class<?>[] {
                        Connection.class
                },
                (proxy, method, args) -> {
                    if ("close".equals(method.getName()) && (args == null || args.length == 0)) {
                        return null;
                    }
                    try {
                        return method.invoke(connection, args);
                    }
                    catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }
}
