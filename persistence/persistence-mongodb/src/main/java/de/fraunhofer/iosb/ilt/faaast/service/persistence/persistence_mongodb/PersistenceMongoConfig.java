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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.persistence_mongodb;

import de.fraunhofer.iosb.ilt.faaast.service.persistence.PersistenceConfig;


/**
 * lol.
 */
public class PersistenceMongoConfig extends PersistenceConfig<PersistenceMongo> {
    private String databaseName;
    private String collectionName;
    private String modelId;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends PersistenceMongoConfig, B extends AbstractBuilder<T, B>>
            extends PersistenceConfig.AbstractBuilder<PersistenceMongo, T, B> {
        public B databaseName(String value) {
            getBuildingInstance().setDatabaseName(value);
            return getSelf();
        }

        public B collectionName(String value) {
            getBuildingInstance().setCollectionName(value);
            return getSelf();
        }

        public B modelId(String value) {
            getBuildingInstance().setModelId(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<PersistenceMongoConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PersistenceMongoConfig newBuildingInstance() {
            return new PersistenceMongoConfig();
        }
    }
}
