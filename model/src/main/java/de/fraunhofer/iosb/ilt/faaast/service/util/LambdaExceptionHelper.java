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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


/**
 * Helper class for handling exceptions in lambda expressions
 */
public class LambdaExceptionHelper {

    @FunctionalInterface
    public interface ConsumerWithExceptions<T, E extends Exception> {

        void accept(T t) throws E;
    }

    @FunctionalInterface
    public interface BiConsumerWithExceptions<T, U, E extends Exception> {

        void accept(T t, U u) throws E;
    }

    @FunctionalInterface
    public interface FunctionWithExceptions<T, R, E extends Exception> {

        R apply(T t) throws E;
    }

    @FunctionalInterface
    public interface SupplierWithExceptions<T, E extends Exception> {

        T get() throws E;
    }

    @FunctionalInterface
    public interface RunnableWithExceptions<E extends Exception> {

        void run() throws E;
    }

    /**
     * Wraps a {@link Consumer} throwing an Exception to be conveniently used in
     * functional expressions.
     *
     * @param <T> type of the consumer
     * @param <E> type of the potentially thrown exception
     * @param consumer the actual consumer
     * @return a wrapping consumer
     * @throws E if execution of underlying consumer throws given exception
     */
    public static <T, E extends Exception> Consumer<T> rethrowConsumer(ConsumerWithExceptions<T, E> consumer) throws E {
        return t -> {
            try {
                consumer.accept(t);
            }
            catch (Exception e) {
                throwAsUnchecked(e);
            }
        };
    }


    /**
     * Wraps a {@link BiConsumer} throwing an Exception to be conveniently used
     * in functional expressions.
     *
     * @param <T> first type of the biconsumer
     * @param <U> second type of the biconsumer
     * @param <E> type of the potentially thrown exception
     * @param biConsumer the actual biconsumer
     * @return a wrapping biconsumer
     * @throws E if execution of underlying biconsumer throws given exception
     */
    public static <T, U, E extends Exception> BiConsumer<T, U> rethrowBiConsumer(BiConsumerWithExceptions<T, U, E> biConsumer) throws E {
        return (t, u) -> {
            try {
                biConsumer.accept(t, u);
            }
            catch (Exception e) {
                throwAsUnchecked(e);
            }
        };
    }


    /**
     * Wraps a {@link Function} throwing an Exception to be conveniently used in
     * functional expressions.
     *
     * @param <T> input type of the function
     * @param <R> result type of the function
     * @param <E> type of the potentially thrown exception
     * @param function the actual function
     * @return a wrapping function
     * @throws E if execution of underlying function throws given exception
     */
    public static <T, R, E extends Exception> Function<T, R> rethrowFunction(FunctionWithExceptions<T, R, E> function) throws E {
        return t -> {
            try {
                return function.apply(t);
            }
            catch (Exception e) {
                throwAsUnchecked(e);
                return null;
            }
        };
    }


    /**
     * Wraps a {@link Supplier} throwing an Exception to be conveniently used in
     * functional expressions.
     *
     * @param <T> type of the supplier
     * @param <E> type of the potentially thrown exception
     * @param supplier the actual supplier
     * @return a wrapping supplier
     * @throws E if execution of underlying supplierthrows given exception
     */
    public static <T, E extends Exception> Supplier<T> rethrowSupplier(SupplierWithExceptions<T, E> supplier) throws E {
        return () -> {
            try {
                return supplier.get();
            }
            catch (Exception e) {
                throwAsUnchecked(e);
                return null;
            }
        };
    }


    /**
     * Wraps a Supplier interface and rethrows all exceptions as
     * RuntimeException.
     *
     * @param <T> result type of the supplier
     * @param supplier the supplier to wrap
     * @return wrapped supplier
     * @throws RuntimeException if calling the supplier fails
     */
    public static <T> Supplier<T> wrap(SupplierWithExceptions<T, Exception> supplier) {
        return () -> {
            try {
                return supplier.get();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }


    /**
     * Wraps a Consumer interface and rethrows all exceptions as
     * RuntimeException.
     *
     * @param <T> input type of the consumer
     * @param consumer the consumer to wrap
     * @return wrapped consumer
     * @throws RuntimeException if calling the consumer fails
     */
    public static <T> Consumer<T> wrap(ConsumerWithExceptions<T, Exception> consumer) {
        return arg -> {
            try {
                consumer.accept(arg);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }


    /**
     * Wraps a Runnable interface and rethrows all exceptions as
     * RuntimeException.
     *
     * @param runnable the runnable to wrap
     * @return wrapped runnable
     * @throws RuntimeException if calling the runnable fails
     */
    public static Runnable wrap(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }


    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwAsUnchecked(Exception e) throws E {
        throw (E) e;
    }
}
