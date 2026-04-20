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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.impl;

import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.AbstractCloudEventMapperTest;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.CloudEventMapper;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.CloudEventMapperConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import io.cloudevents.CloudEvent;
import java.util.function.Function;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.junit.Assert;
import org.junit.Test;


public class ValueChangeCloudEventMapperTest extends AbstractCloudEventMapperTest {
    private final String valueChanged = "valueChanged";

    @Override
    protected CloudEventMapper getCloudEventMapper(String callbackAddress, String dataSchemaPrefix, String eventTypePrefix, boolean slimEvents,
                                                   Function<Reference, Referable> referableSupplier) {
        return new ValueChangedCloudEventMapper(new CloudEventMapperConfig(callbackAddress, dataSchemaPrefix, eventTypePrefix, slimEvents), referableSupplier);
    }


    @Test
    public void testValueChangeMappingValid() throws Exception {
        String submodelId = "hello-world";
        Property property = new DefaultProperty.Builder()
                .idShort("test")
                .semanticId(new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .value("my-semantic-id")
                                .build())
                        .build())
                .idShort("ExampleProperty")
                .valueType(DataTypeDefXsd.STRING)
                .value("bar")
                .build();

        CloudEvent expected = expectedFrom(submodelId, property, "Property", valueChanged);

        Function<Reference, Referable> referableSupplier = r -> property;

        CloudEventMapper mapper = getCloudEventMapper(referableSupplier);

        var fastMessage = ValueChangeEventMessage.builder()
                .element(asReference(submodelId, property))
                .build();

        Assert.assertTrue(mapper.canHandle(fastMessage));

        CloudEvent actual = mapper.createCloudEvent(fastMessage);

        assertCloudEvent(expected, actual);
    }


    @Test
    public void testSlimEventsOmitsOnlyData() throws Exception {
        String submodelId = "hello-world";
        Property property = new DefaultProperty.Builder()
                .idShort("test")
                .semanticId(new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .value("my-semantic-id")
                                .build())
                        .build())
                .idShort("ExampleProperty")
                .valueType(DataTypeDefXsd.STRING)
                .value("bar")
                .build();

        CloudEvent expected = expectedFrom(submodelId, property, "Property", valueChanged);

        Function<Reference, Referable> referableSupplier = r -> property;

        CloudEventMapper mapper = getCloudEventMapper(true, referableSupplier);

        ValueChangeEventMessage fastMessage = ValueChangeEventMessage.builder()
                .element(asReference(submodelId, property))
                .build();

        Assert.assertTrue(mapper.canHandle(fastMessage));

        CloudEvent actual = mapper.createCloudEvent(fastMessage);

        assertCloudEvent(expected, actual, true);
    }
}
