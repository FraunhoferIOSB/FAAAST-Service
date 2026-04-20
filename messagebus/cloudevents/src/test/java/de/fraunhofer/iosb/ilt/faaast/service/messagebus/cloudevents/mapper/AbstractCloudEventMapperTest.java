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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonFormat;
import java.net.URI;
import java.util.UUID;
import java.util.function.Function;
import org.eclipse.digitaltwin.aas4j.v3.model.HasSemantics;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.junit.Assert;


public abstract class AbstractCloudEventMapperTest {
    private final String eventTypePrefix = "my.prefix.test.";
    private final String callbackAddress = "https://localhost:12345/api/v3.0";
    private final String dataSchemaPrefix = "https://my-data-schema-prefix/path#";
    protected final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY)
            .registerModule(JsonFormat.getCloudEventJacksonModule());

    protected CloudEventMapper getCloudEventMapper(Function<Reference, Referable> referableSupplier) {
        return getCloudEventMapper(false, referableSupplier);
    }


    protected CloudEventMapper getCloudEventMapper(boolean slimEvents, Function<Reference, Referable> referableSupplier) {
        return getCloudEventMapper(callbackAddress, dataSchemaPrefix, eventTypePrefix, slimEvents, referableSupplier);
    }


    protected abstract CloudEventMapper getCloudEventMapper(String callbackAddress, String dataSchemaPrefix, String eventTypePrefix, boolean slimEvents,
                                                            Function<Reference, Referable> referableSupplier);


    protected CloudEvent expectedFrom(String identifiableId, Referable referable, String dataSchemaSuffix, String eventTypeSuffix) throws JsonProcessingException {
        CloudEventBuilder builder = CloudEventBuilder.v1()
                .withSource(URI.create(String.format("%s/submodels/%s/submodel-elements/%s", callbackAddress, EncodingHelper.base64UrlEncode(identifiableId),
                        referable.getIdShort())))
                .withId(UUID.randomUUID().toString())
                .withType(eventTypePrefix + eventTypeSuffix)
                .withDataSchema(URI.create(dataSchemaPrefix + dataSchemaSuffix))
                .withDataContentType("application/json")
                .withData(mapper.writeValueAsBytes(referable));

        if (referable instanceof HasSemantics && ((HasSemantics) referable).getSemanticId() != null) {
            builder.withExtension("semanticid", ((HasSemantics) referable).getSemanticId().getKeys().get(0).getValue());
        }

        return builder.build();
    }


    protected Reference asReference(String submodelId, SubmodelElement referable) {
        return ReferenceBuilder.forSubmodel(submodelId, referable);
    }


    protected void assertCloudEvent(CloudEvent expected, CloudEvent actual) {
        assertCloudEvent(expected, actual, false);
    }


    protected void assertCloudEvent(CloudEvent expected, CloudEvent actual, boolean isSlim) {
        Assert.assertEquals(expected.getSpecVersion(), actual.getSpecVersion());
        Assert.assertEquals(expected.getSource(), actual.getSource());
        Assert.assertEquals(expected.getType(), actual.getType());
        Assert.assertEquals(expected.getDataSchema(), actual.getDataSchema());
        Assert.assertEquals(expected.getExtension("semanticid"), actual.getExtension("semanticid"));
        Assert.assertEquals(expected.getDataContentType(), actual.getDataContentType());
        if (isSlim) {
            Assert.assertNull(actual.getData());
        }
        else {
            Assert.assertEquals(expected.getData(), actual.getData());
        }
    }

}
