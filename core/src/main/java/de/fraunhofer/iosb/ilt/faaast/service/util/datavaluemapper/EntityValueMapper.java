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

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.DataElementValueMapper;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.ArrayList;
import java.util.List;


public class EntityValueMapper extends DataValueMapper<Entity, EntityValue> {

    @Override
    public EntityValue toDataElementValue(Entity submodelElement) {
        if (submodelElement == null) {
            return null;
        }
        EntityValue entityValue = new EntityValue();
        entityValue.setEntityType(submodelElement.getEntityType());

        //TODO: Is type List<ElementValue> of statements in entityValue correct?
        List<ElementValue> elementValueList = new ArrayList<>();
        submodelElement.getStatements().forEach(x -> elementValueList.add(DataElementValueMapper.toDataElement(x)));
        entityValue.setStatements(elementValueList);
        entityValue.setGlobalAssetId(submodelElement.getGlobalAssetId() != null ? submodelElement.getGlobalAssetId().getKeys() : null);
        return entityValue;
    }


    @Override
    public Entity setDataElementValue(Entity submodelElement, EntityValue value) {
        if (submodelElement == null || value == null) {
            return null;
        }
        submodelElement.setEntityType(value.getEntityType());
        //TODO: Is type List<ElementValue> of statements in entityValue correct?
        //submodelElement.setStatements(value.getStatements());
        submodelElement.setGlobalAssetId(value.getGlobalAssetId() != null ? new DefaultReference.Builder().keys(value.getGlobalAssetId()).build() : null);
        return submodelElement;
    }
}
