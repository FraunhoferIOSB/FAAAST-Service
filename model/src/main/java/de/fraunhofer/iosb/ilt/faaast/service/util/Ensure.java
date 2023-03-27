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

import java.util.function.Supplier;


/**
 * Utility class to ensure validity of parameter values.
 */
public class Ensure {

    private Ensure() {}


    /**
     * Checks that the specified object reference is not {@code null}. This method is designed primarily for doing
     * parameter validation in methods and constructors, as demonstrated below:
     * <blockquote>
     *
     * <pre>
     * public Foo(Bar bar) {
     *     this.bar = Ensure.requireNonNull(bar);
     * }
     * </pre>
     *
     * </blockquote>
     *
     * @param obj the object reference to check for nullity
     * @param <T> the type of the reference
     * @return {@code obj} if not {@code null}
     * @throws IllegalArgumentException if {@code obj} is {@code null}
     * @see java.util.Objects#requireNonNull(Object)
     */
    public static <T> T requireNonNull(T obj) {
        if (obj == null) {
            throw new IllegalArgumentException();
        }
        return obj;
    }


    /**
     * Checks that the specified object reference is not {@code null} and throws a customized
     * {@link IllegalArgumentException} if it is. This method is designed primarily for doing parameter validation in
     * methods and constructors with multiple parameters, as demonstrated below:
     * <blockquote>
     *
     * <pre>
     * public Foo(Bar bar, Baz baz) {
     *     this.bar = Ensure.requireNonNull(bar, "bar must not be null");
     *     this.baz = Ensure.requireNonNull(baz, "baz must not be null");
     * }
     * </pre>
     *
     * </blockquote>
     *
     * @param obj the object reference to check for nullity
     * @param message detail message to be used in the event that a {@code
     *                IllegalArgumentException} is thrown
     * @param <T> the type of the reference
     * @return {@code obj} if not {@code null}
     * @throws IllegalArgumentException if {@code obj} is {@code null}
     * @see java.util.Objects#requireNonNull(Object, String)
     */
    public static <T> T requireNonNull(T obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
        return obj;
    }


    /**
     * Validates that a given condition is met, otherwise throws custom {@code IllegalArgumentException}.
     *
     * @param condition condition to check
     * @param message message for exception
     * @throws IllegalArgumentException if condition is not fulfilled
     */
    public static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }


    /**
     * Validates that a given condition is met, otherwise throws exception.
     *
     * @param <E> type of the exception to throw
     * @param condition condition to check
     * @param exception the exception
     * @throws E if condition is not satisfied
     */
    public static <E extends Throwable> void require(boolean condition, E exception) throws E {
        if (!condition) {
            throw exception;
        }
    }


    /**
     * Checks that the specified object reference is not {@code null} and throws a customized
     * {@link IllegalArgumentException} if it is.
     *
     * @param <T> the type of the reference
     * @param obj the object to check
     * @param messageSupplier the message supplier
     * @return {@code obj} if not {@code null}
     * @throws IllegalArgumentException if {@code obj} is {@code null}
     * @see java.util.Objects#requireNonNull(Object, Supplier)
     */
    public static <T> T requireNonNull(T obj, Supplier<String> messageSupplier) {
        if (obj == null) {
            throw new IllegalArgumentException(messageSupplier == null
                    ? null
                    : messageSupplier.get());
        }
        return obj;
    }


    /**
     * Checks that the specified object reference is not {@code null} and throws a customized {@link RuntimeException}
     * if it is.
     *
     * @param <T> the type of the reference
     * @param obj the object to check
     * @param exception the exception to throw
     * @return {@code obj} if not {@code null}
     * @throws RuntimeException if {@code obj} is {@code null}
     */
    public static <T> T requireNonNull(T obj, RuntimeException exception) {
        if (obj == null) {
            throw exception;
        }
        return obj;
    }


    /**
     * Checks that the specified object reference is not {@code null} and throws custom exception if it is.
     *
     * @param <T> the type of the reference
     * @param <E> type of the exception to throw
     * @param obj the object to check
     * @param exception the exception to throw
     * @return {@code obj} if not {@code null}
     * @throws E if {@code obj} is {@code null}
     */
    public static <T, E extends Throwable> T requireNonNull(T obj, E exception) throws E {
        if (obj == null) {
            throw exception;
        }
        return obj;
    }
}
