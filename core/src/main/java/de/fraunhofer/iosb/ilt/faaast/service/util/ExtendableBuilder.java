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

/**
 * Utility class for realizing the builder pattern with support for inheritance.
 * 
 * @param <T> type of the class to build
 * @param <B> type of the actual builder
 */
public abstract class ExtendableBuilder<T, B extends ExtendableBuilder<T, B>> extends AbstractBuilder<T> {

    /**
     * Returns the current instance of the builder.
     * 
     * @return the current instance of the builder.
     */
    protected abstract B getSelf();
}
