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
package de.fraunhofer.iosb.ilt.faaast.service.util.datavaluemapper;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueMapper;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.List;
import java.util.stream.Collectors;


public class EntityValueMapper extends DataValueMapper<Entity, EntityValue> {

    @Override
    public EntityValue toValue(Entity submodelElement) {
        if (submodelElement == null) {
            return null;
        }
        return EntityValue.builder()
                .entityType(submodelElement.getEntityType())
                .statements(submodelElement.getStatements().stream().collect(Collectors.toMap(x -> x.getIdShort(), x -> ElementValueMapper.toValue(x))))
                .globalAssetId(submodelElement.getGlobalAssetId() != null ? submodelElement.getGlobalAssetId().getKeys() : List.of())
                .build();
    }


    @Override
    public Entity setValue(Entity submodelElement, EntityValue value) {
        if (submodelElement == null || value == null) {
            return null;
        }
        for (SubmodelElement statement: submodelElement.getStatements()) {
            if (value.getStatements().containsKey(statement.getIdShort())) {
                ElementValueMapper.setValue(statement, value.getStatements().get(statement.getIdShort()));
            }
        }
        submodelElement.setEntityType(value.getEntityType());
        submodelElement.setGlobalAssetId(value.getGlobalAssetId() != null ? new DefaultReference.Builder().keys(value.getGlobalAssetId()).build() : null);
        return submodelElement;
    }
}
