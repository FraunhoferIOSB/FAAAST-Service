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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.EntityType;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Value class for Entity.
 */
public class EntityValue extends ElementValue {

    private EntityType entityType;
    private String globalAssetId;
    private List<SpecificAssetId> specificAssetIds;
    private Map<String, ElementValue> statements;

    public EntityValue() {
        this.statements = new HashMap<>();
        this.specificAssetIds = new ArrayList<>();
    }


    public EntityValue(Map<String, ElementValue> statements, EntityType entityType, String globalAssetId, List<SpecificAssetId> specificAssetIds) {
        this.statements = statements;
        this.entityType = entityType;
        this.globalAssetId = globalAssetId;
        this.specificAssetIds = specificAssetIds;
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
        return Objects.equals(statements, that.statements)
                && entityType == that.entityType
                && Objects.equals(globalAssetId, that.globalAssetId)
                && Objects.equals(specificAssetIds, that.specificAssetIds);
    }


    public EntityType getEntityType() {
        return entityType;
    }


    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }


    public String getGlobalAssetId() {
        return globalAssetId;
    }


    public void setGlobalAssetId(String globalAssetId) {
        this.globalAssetId = globalAssetId;
    }


    public List<SpecificAssetId> getSpecificAssetIds() {
        return specificAssetIds;
    }


    public void setSpecificAssetIds(List<SpecificAssetId> specificAssetIds) {
        this.specificAssetIds = specificAssetIds;
    }


    public Map<String, ElementValue> getStatements() {
        return statements;
    }


    public void setStatements(Map<String, ElementValue> statements) {
        this.statements = statements;
    }


    @Override
    public int hashCode() {
        return Objects.hash(statements, entityType, globalAssetId, specificAssetIds);
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


        public B globalAssetId(String value) {
            getBuildingInstance().setGlobalAssetId(value);
            return getSelf();
        }


        public B specificAssetId(SpecificAssetId value) {
            getBuildingInstance().getSpecificAssetIds().add(value);
            return getSelf();
        }


        public B specificAssetIds(List<SpecificAssetId> value) {
            getBuildingInstance().setSpecificAssetIds(value);
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
