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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.FileStorage;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import java.util.Objects;


/**
 * Immutable wrapper class containing access to all relevant information of a Service to execute a
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.Request}.
 */
public class RequestExecutionContext {

    private final CoreConfig coreConfig;
    private final Persistence persistence;
    private final MessageBus messageBus;
    private final AssetConnectionManager assetConnectionManager;
    private final FileStorage fileStorage;

    public RequestExecutionContext(CoreConfig coreConfig,
            Persistence persistence,
            FileStorage fileStorage,
            MessageBus messageBus,
            AssetConnectionManager assetConnectionManager) {
        this.coreConfig = coreConfig;
        this.persistence = persistence;
        this.fileStorage = fileStorage;
        this.messageBus = messageBus;
        this.assetConnectionManager = assetConnectionManager;
    }


    public CoreConfig getCoreConfig() {
        return coreConfig;
    }


    public Persistence<?> getPersistence() {
        return persistence;
    }


    public MessageBus getMessageBus() {
        return messageBus;
    }


    public AssetConnectionManager getAssetConnectionManager() {
        return assetConnectionManager;
    }


    public FileStorage getFileStorage() {
        return fileStorage;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestExecutionContext that = (RequestExecutionContext) o;
        return Objects.equals(coreConfig, that.coreConfig)
                && Objects.equals(persistence, that.persistence)
                && Objects.equals(messageBus, that.messageBus)
                && Objects.equals(assetConnectionManager, that.assetConnectionManager)
                && Objects.equals(fileStorage, that.fileStorage);
    }


    @Override
    public int hashCode() {
        return Objects.hash(coreConfig, persistence, messageBus, assetConnectionManager, fileStorage);
    }
}
