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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus;

import de.fraunhofer.iosb.ilt.faaast.service.config.Config;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Generic message bus configuration. When implementing a custom message bus inherit from this class to create a custom
 * configuration.
 *
 * @param <T> type of the message bus
 */
public class MessageBusConfig<T extends MessageBus> extends Config<T> {

    /**
     * Abstract builder class that should be used for builders of inheriting classes.
     *
     * @param <T> type of the message bus of the config to build
     * @param <C> type of the config to build
     * @param <B> type of this builder, needed for inheritance builder pattern
     */
    public abstract static class AbstractBuilder<T extends MessageBus, C extends MessageBusConfig<T>, B extends MessageBusConfig.AbstractBuilder<T, C, B>>
            extends ExtendableBuilder<C, B> {

    }

    /**
     * Builder for MessageBusConfig class.
     *
     * @param <T> type of the message bus of the config to build
     */
    public static class Builder<T extends MessageBus> extends MessageBusConfig.AbstractBuilder<T, MessageBusConfig<T>, MessageBusConfig.Builder<T>> {

        @Override
        protected MessageBusConfig<T> newBuildingInstance() {
            return new MessageBusConfig<>();
        }


        @Override
        protected Builder<T> getSelf() {
            return this;
        }
    }

}
