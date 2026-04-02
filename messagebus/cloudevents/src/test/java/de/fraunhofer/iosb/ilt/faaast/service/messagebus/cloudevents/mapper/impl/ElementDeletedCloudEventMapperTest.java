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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.CloudEventMapper;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.CloudEventMapperConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import io.cloudevents.CloudEvent;
import java.util.function.Function;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.junit.Assert;
import org.junit.Test;


public class ElementDeletedCloudEventMapperTest extends AbstractCloudEventMapperTest {

    private final String deleted = "deleted";

    @Test
    public void testElementDeletedMappingValid() throws Exception {
        String submodelId = "hello-world";
        // Note: omitting semantic id here since technically it is allowed
        Property property = new DefaultProperty.Builder()
                .idShort("test")
                .idShort("ExampleProperty")
                .valueType(DataTypeDefXsd.STRING)
                .value("bar")
                .build();

        CloudEvent expected = expectedFrom(submodelId, property, "Property", deleted);

        Function<Reference, Referable> referableSupplier = mock(Function.class);
        when(referableSupplier.apply(any())).thenReturn(null);

        CloudEventMapper mapper = getCloudEventMapper(referableSupplier);

        var fastMessage = ElementDeleteEventMessage.builder()
                .element(asReference(submodelId, property)).build();

        Assert.assertTrue(mapper.canHandle(fastMessage));

        CloudEvent actual = mapper.createCloudEvent(fastMessage);

        // We do not send the deleted element in the message
        assertDeletedCloudEvent(expected, actual);
    }


    @Override
    protected CloudEventMapper getCloudEventMapper(String callbackAddress, String dataSchemaPrefix, String eventTypePrefix, boolean slimEvents,
                                                   Function<Reference, Referable> referableSupplier) {
        return new ElementDeletedCloudEventMapper(new CloudEventMapperConfig(callbackAddress, dataSchemaPrefix, eventTypePrefix, slimEvents));
    }


    protected void assertDeletedCloudEvent(CloudEvent expected, CloudEvent actual) {
        Assert.assertEquals(expected.getSpecVersion(), actual.getSpecVersion());
        Assert.assertEquals(expected.getSource(), actual.getSource());
        Assert.assertEquals(expected.getType(), actual.getType());
        Assert.assertEquals(expected.getDataSchema(), actual.getDataSchema());
        Assert.assertEquals(expected.getExtension("semanticid"), actual.getExtension("semanticid"));
    }

}
