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
package de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata;

import io.adminshell.aas.v3.model.EntityType;
import io.adminshell.aas.v3.model.Key;
import java.util.List;
import java.util.Objects;


public class EntityValue extends DataElementValue {
    private List<ElementValue> statements;
    private EntityType entityType;
    private List<Key> globalAssetId;

    public List<ElementValue> getStatements() {
        return statements;
    }


    public void setStatements(List<ElementValue> statements) {
        this.statements = statements;
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


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EntityValue that = (EntityValue) o;
        return Objects.equals(statements, that.statements) && entityType == that.entityType && Objects.equals(globalAssetId, that.globalAssetId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(statements, entityType, globalAssetId);
    }
}
