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
package de.fraunhofer.iosb.ilt.faaast.service.eventlistener.mqtt;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.eventlistener.EventListener;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;


/**
 * Implementation of {@link de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage} for in memory storage.
 */
public class EventListenerMqtt implements EventListener<EventListenerMqttConfig> {

    private EventListenerMqttConfig config;
    private Environment environment;
    private String rule;

    public EventListenerMqtt() {

    }


    @Override
    public void init(CoreConfig coreConfig, EventListenerMqttConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
        Ensure.requireNonNull(config.getRule(), "rule must be non-null");
        this.rule = config.getRule();
        try {
            this.environment = config.loadInitialModelAndFiles().getEnvironment();
        }
        catch (DeserializationException | InvalidConfigurationException e) {
            throw new ConfigurationInitializationException("error initializing in-memory file storage", e);
        }
    }


    @Override
    public EventListenerMqttConfig asConfig() {
        return config;
    }


    @Override
    public void start() {

    }


    @Override
    public void stop() {

    }
}
