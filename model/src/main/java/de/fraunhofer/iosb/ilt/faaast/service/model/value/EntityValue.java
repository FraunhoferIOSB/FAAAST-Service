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
package de.fraunhofer.iosb.ilt.faaast.service.model.value;

import io.adminshell.aas.v3.model.EntityType;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class EntityValue extends ElementValue {

    private EntityType entityType;
    private List<Key> globalAssetId;
    private Map<String, ElementValue> statements;

    public EntityValue() {
        this.statements = new HashMap<>();
        this.globalAssetId = new ArrayList<>();
    }


    public EntityValue(Map<String, ElementValue> statements, EntityType entityType, List<Key> globalAssetId) {
        this.statements = statements;
        this.entityType = entityType;
        this.globalAssetId = globalAssetId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntityValue that = (EntityValue) o;
        return Objects.equals(statements, that.statements) && entityType == that.entityType && Objects.equals(globalAssetId, that.globalAssetId);
    }


    public EntityType getEntityType() {
        return entityType;
    }


    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }


    public List<Key> getGlobalAssetId() {
        return globalAssetId;
    }


    public void setGlobalAssetId(List<Key> globalAssetId) {
        this.globalAssetId = globalAssetId;
    }


    public Map<String, ElementValue> getStatements() {
        return statements;
    }


    public void setStatements(Map<String, ElementValue> statements) {
        this.statements = statements;
    }


    @Override
    public int hashCode() {
        return Objects.hash(statements, entityType, globalAssetId);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends EntityValue, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B statements(Map<String, ElementValue> value) {
            getBuildingInstance().setStatements(value);
            return getSelf();
        }


        public B statement(String name, ElementValue value) {
            getBuildingInstance().getStatements().put(name, value);
            return getSelf();
        }


        public B entityType(EntityType value) {
            getBuildingInstance().setEntityType(value);
            return getSelf();
        }


        public B globalAssetId(List<Key> value) {
            getBuildingInstance().setGlobalAssetId(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<EntityValue, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected EntityValue newBuildingInstance() {
            return new EntityValue();
        }
    }
}
