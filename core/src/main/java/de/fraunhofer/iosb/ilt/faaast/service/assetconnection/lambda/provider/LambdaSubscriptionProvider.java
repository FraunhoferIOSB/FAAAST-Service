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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.lambda.provider;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.NewDataListener;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Subscription provider that can be used from code with lambda expression.
 */
public class LambdaSubscriptionProvider implements AssetSubscriptionProvider, NewDataListener {

    private final Set<NewDataListener> listeners = new HashSet<>();
    private Consumer<NewDataListener> dataGenerator;

    private LambdaSubscriptionProvider() {}


    /**
     * Starts the provider.
     */
    public void start() {
        dataGenerator.accept(this);
    }


    @Override
    public void addNewDataListener(NewDataListener listener) throws AssetConnectionException {
        listeners.add(listener);
    }


    @Override
    public void removeNewDataListener(NewDataListener listener) throws AssetConnectionException {
        listeners.remove(listener);
    }


    @Override
    public void newDataReceived(DataElementValue data) {
        listeners.forEach(x -> x.newDataReceived(data));
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ExtendableBuilder<LambdaSubscriptionProvider, Builder> {

        public Builder generate(Consumer<NewDataListener> value) {
            getBuildingInstance().dataGenerator = value;
            return this;
        }


        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected LambdaSubscriptionProvider newBuildingInstance() {
            return new LambdaSubscriptionProvider();
        }

    }

}
