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
package de.fraunhofer.iosb.ilt.faaast.service.model.messagebus;

import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Holds informations about a subscription.
 */
public class SubscriptionInfo {

    private static final Predicate<Reference> DEFAULT_FILTER = x -> true;
    private Set<Class<?>> subscribedEvents;
    private Consumer<EventMessage> handler;
    private Predicate<Reference> filter;

    /**
     * Static method to create a Subscription Info.
     *
     * @param eventMessageClass which should be subscribed to
     * @param handler which should be accepted in the subscription
     * @param <T> concrete type of the EventMessage
     * @return a Subscription Info
     */
    public static <T extends EventMessage> SubscriptionInfo create(Class<T> eventMessageClass, Consumer<T> handler) {
        SubscriptionInfo subscriptionInfo = new SubscriptionInfo((Consumer<EventMessage>) handler);
        subscriptionInfo.setHandler((Consumer<EventMessage>) handler);
        subscriptionInfo.subscribedEvents = Collections.singleton(eventMessageClass);
        return subscriptionInfo;
    }


    /**
     * Static method to create a Subscription Info.
     *
     * @param eventMessageClass which should be subscribed to
     * @param handler which should be accepted in the subscription
     * @param keyElements of the references which should be subscribed to
     * @param <T> concrete type of the EventMessage
     * @return a Subscriptio nInfo
     */
    public static <T extends EventMessage> SubscriptionInfo create(Class<T> eventMessageClass, Consumer<T> handler, KeyTypes keyElements) {
        SubscriptionInfo subscriptionInfo = create(eventMessageClass, handler);
        subscriptionInfo.setFilter(x -> x != null
                && x.getKeys() != null
                && !x.getKeys().isEmpty()
                && x.getKeys().get(x.getKeys().size() - 1).getType().equals(keyElements));
        return subscriptionInfo;
    }


    /**
     * Static method to create a Subscription Info.
     *
     * @param eventMessageClass which should be subscribed to
     * @param handler which should be accepted in the subscription
     * @param reference which should be subscribed to
     * @param <T> concrete type of the EventMessage
     * @return a Subscriptio nInfo
     */
    public static <T extends EventMessage> SubscriptionInfo create(Class<T> eventMessageClass, Consumer<T> handler, Reference reference) {
        SubscriptionInfo subscriptionInfo = create(eventMessageClass, handler);
        subscriptionInfo.setFilter(x -> x.equals(reference));
        return subscriptionInfo;
    }


    /**
     * Static method to create a Subscription Info.
     *
     * @param eventMessageClass which should be subscribed to
     * @param handler which should be accepted in the subscription
     * @param filter of references which should be subscribed to
     * @param <T> concrete type of the EventMessage
     * @return a Subscriptio nInfo
     */
    public static <T extends EventMessage> SubscriptionInfo create(Class<T> eventMessageClass, Consumer<T> handler, Predicate<Reference> filter) {
        SubscriptionInfo subscriptionInfo = create(eventMessageClass, handler);
        subscriptionInfo.setFilter(filter);
        return subscriptionInfo;
    }


    public SubscriptionInfo(Consumer<EventMessage> handler) {
        setHandler(handler);
        this.filter = DEFAULT_FILTER;
        this.subscribedEvents = new HashSet<>(Arrays.asList(EventMessage.class));
    }


    public Set<Class<?>> getSubscribedEvents() {
        return subscribedEvents;
    }


    /**
     * Sets subscribed events.
     *
     * @param subscribedEvents the subscribed events
     */
    public void setSubscribedEvents(Set<Class<?>> subscribedEvents) {
        if (subscribedEvents != null) {
            this.subscribedEvents = subscribedEvents.stream()
                    .filter(EventMessage.class::isAssignableFrom)
                    .collect(Collectors.toSet());
        }
        else {
            this.subscribedEvents = null;
        }
    }


    public Consumer<EventMessage> getHandler() {
        return handler;
    }


    /**
     * Sets the handler.
     *
     * @param handler the handler to set
     */
    public void setHandler(Consumer<EventMessage> handler) {
        Ensure.requireNonNull(handler, "handler must be non-null");
        this.handler = handler;
    }


    public Predicate<Reference> getFilter() {
        return filter;
    }


    /**
     * Sets the filter.
     *
     * @param filter the filter to set
     */
    public void setFilter(Predicate<Reference> filter) {
        if (filter != null) {
            this.filter = filter;
        }
        else {
            this.filter = DEFAULT_FILTER;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubscriptionInfo that = (SubscriptionInfo) o;
        return Objects.equals(subscribedEvents, that.subscribedEvents) && Objects.equals(handler, that.handler) && Objects.equals(filter, that.filter);
    }


    @Override
    public int hashCode() {
        return Objects.hash(subscribedEvents, handler, filter);
    }
}
