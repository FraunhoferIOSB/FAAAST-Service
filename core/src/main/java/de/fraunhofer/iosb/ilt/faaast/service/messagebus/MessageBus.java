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

import de.fraunhofer.iosb.ilt.faaast.service.config.Configurable;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;


public interface MessageBus<T extends MessageBusConfig> extends Configurable<T> {

    /**
     * Publish a new EventMessage to the message bus<br>
     *
     * @param message which should be published<br>
     */
    public void publish(EventMessage message) throws Exception;


    /**
     * Subscribe to event messages published in the message bus.<br>
     * The Subscription Info determines which event messages are considered in detail.<br>
     *
     * @param subscriptionInfo to determine which event messages should be considered<br>
     * @return the id of the created subscription in the message bus.<br>
     *         The id can be used to update/unsubscribe this subscription.
     */
    public SubscriptionId subscribe(SubscriptionInfo subscriptionInfo);


    /**
     * Unsubscribe from a specific subscription by id<br>
     *
     * @param id of the subscription which should be deleted
     */
    public void unsubscribe(SubscriptionId id);


    /**
     * Starts the MessageBus
     */
    public void start();


    /**
     * Stops the MessageBus
     */
    public void stop();

}
