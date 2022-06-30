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
package de.fraunhofer.iosb.ilt.faaast.service.model.elementvaluemapper;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import io.adminshell.aas.v3.model.EntityType;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultEntity;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;


public class EntityValueTest {

    @Test
    public void testSetValueMapping() throws ValueFormatException {
        SubmodelElement actual = new DefaultEntity.Builder()
                .statement(new DefaultProperty.Builder()
                        .idShort("property")
                        .build())
                .build();
        EntityValue value = createEntityValue();
        SubmodelElement expected = createEntity(value.getGlobalAssetId(), value.getStatements().keySet().iterator().next(), value.getEntityType());
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testEntitySetValueMappingWithNull() throws ValueFormatException {
        SubmodelElement actual = new DefaultEntity.Builder()
                .statement(new DefaultProperty.Builder()
                        .idShort("property")
                        .valueType(null)
                        .value(null)
                        .build())
                .entityType(null)
                .build();
        EntityValue value = EntityValue.builder()
                .statement("property", PropertyValue.of(Datatype.STRING, "foo"))
                .globalAssetId(null)
                .build();
        SubmodelElement expected = new DefaultEntity.Builder()
                .statement(new DefaultProperty.Builder()
                        .idShort("property")
                        .valueType(Datatype.STRING.getName())
                        .value("foo")
                        .build())
                .entityType(null)
                .build();
        ElementValueMapper.setValue(actual, value);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testToValueMapping() throws ValueFormatException, ValueMappingException {
        EntityValue expected = createEntityValue();
        SubmodelElement input = createEntity(expected.getGlobalAssetId(), expected.getStatements().keySet().iterator().next(), expected.getEntityType());
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testToValueMappingWithNull() throws ValueFormatException, ValueMappingException {
        EntityValue expected = EntityValue.builder()
                .build();
        SubmodelElement input = new DefaultEntity.Builder()
                .statement(null)
                .entityType(null)
                .build();
        ElementValue actual = ElementValueMapper.toValue(input);
        Assert.assertEquals(expected, actual);
    }


    private DefaultEntity createEntity(List<Key> globalAssetId, String idShort, EntityType entityType) {
        return new DefaultEntity.Builder()
                .statement(new DefaultProperty.Builder()
                        .idShort(idShort)
                        .valueType(Datatype.STRING.getName())
                        .value("foo")
                        .build())
                .entityType(entityType)
                .globalAssetId(new DefaultReference.Builder()
                        .keys(globalAssetId)
                        .build())
                .build();
    }


    private EntityValue createEntityValue() throws ValueFormatException {
        return EntityValue.builder()
                .statement("property", PropertyValue.of(Datatype.STRING, "foo"))
                .entityType(EntityType.SELF_MANAGED_ENTITY)
                .globalAssetId(List.of(new DefaultKey.Builder()
                        .idType(KeyType.IRI)
                        .type(KeyElements.SUBMODEL)
                        .value("http://example.org/submodel/1")
                        .build(),
                        new DefaultKey.Builder()
                                .idType(KeyType.ID_SHORT)
                                .type(KeyElements.PROPERTY)
                                .value("property1")
                                .build()))
                .build();
    }

}
