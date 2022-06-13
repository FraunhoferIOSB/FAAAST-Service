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
package de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


public class EntityValueMapper implements DataValueMapper<Entity, EntityValue> {

    @Override
    public EntityValue toValue(Entity submodelElement) throws ValueMappingException {
        if (submodelElement == null) {
            return null;
        }
        EntityValue value = EntityValue.builder().build();
        value.setEntityType(submodelElement.getEntityType());
        if (submodelElement.getStatements() != null && submodelElement.getStatements().stream().noneMatch(Objects::isNull)) {
            value.setStatements(submodelElement.getStatements().stream()
                    .collect(Collectors.toMap(
                            x -> x != null ? x.getIdShort() : null,
                            LambdaExceptionHelper.rethrowFunction(x -> x != null ? ElementValueMapper.toValue(x) : null))));
        }
        value.setGlobalAssetId(submodelElement.getGlobalAssetId() != null ? submodelElement.getGlobalAssetId().getKeys() : List.of());
        return value;
    }


    @Override
    public Entity setValue(Entity submodelElement, EntityValue value) {
        DataValueMapper.super.setValue(submodelElement, value);
        if (value != null) {
            for (SubmodelElement statement: submodelElement.getStatements()) {
                if (statement != null
                        && value.getStatements() != null
                        && value.getStatements().containsKey(statement.getIdShort())) {
                    ElementValueMapper.setValue(statement, value.getStatements().get(statement.getIdShort()));

                }
            }

            submodelElement.setEntityType(value.getEntityType());
            if (value.getGlobalAssetId() != null && value.getGlobalAssetId().stream().noneMatch(Objects::isNull)) {
                submodelElement.setGlobalAssetId(new DefaultReference.Builder().keys(value.getGlobalAssetId()).build());
            }
            else {
                submodelElement.setGlobalAssetId(null);
            }
        }
        return submodelElement;
    }
}
