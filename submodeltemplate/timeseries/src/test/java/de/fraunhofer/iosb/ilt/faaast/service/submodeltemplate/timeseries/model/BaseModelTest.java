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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model;

import static de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.TimeSeriesData.FIELD_1;
import static de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.TimeSeriesData.FIELD_2;
import static org.hamcrest.MatcherAssert.assertThat;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.TimeSeriesData;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.Assert;


public abstract class BaseModelTest {

    protected static final ZonedDateTime TIME = ZonedDateTime.parse("2021-01-01T00:00:00Z", DateTimeFormatter.ISO_ZONED_DATE_TIME);

    protected static final Property PROPERTY_FIELD1 = new DefaultProperty.Builder()
            .idShort(FIELD_1)
            .valueType(Datatype.INT.getName())
            .value("0")
            .build();

    protected static final Property PROPERTY_FIELD2 = new DefaultProperty.Builder()
            .idShort(FIELD_2)
            .valueType(Datatype.DOUBLE.getName())
            .value("0.1")
            .build();

    protected static final Property PROPERTY_TIME = new DefaultProperty.Builder()
            .semanticId(ReferenceHelper.globalReference(Constants.TIME_UTC))
            .valueType(Datatype.DATE_TIME.getName())
            .value(DateTimeFormatter.ISO_ZONED_DATE_TIME.format(TIME))
            .idShort(Constants.RECORD_TIME_ID_SHORT)
            .build();

    protected static final SubmodelElement ADDITIONAL_ELEMENT = new DefaultSubmodelElementCollection.Builder()
            .idShort("additional")
            .build();

    protected static final InternalSegment INTERNAL_SEGMENT_WITH_TIMES = InternalSegment.builder()
            .start(ZonedDateTime.parse("2021-01-01T00:00:00Z"))
            .end(ZonedDateTime.parse("2021-01-04T00:00:00Z"))
            .records(TimeSeriesData.RECORDS)
            .build();

    protected static final InternalSegment INTERNAL_SEGMENT_WITHOUT_TIMES = InternalSegment.builder()
            .records(TimeSeriesData.RECORDS)
            .build();

    protected static final LinkedSegment LINKED_SEGMENT = LinkedSegment.builder()
            .semanticId(ReferenceHelper.globalReference(Constants.LINKED_SEGMENT_SEMANTIC_ID))
            .endpoint("host")
            .query("query")
            .build();

    protected <T extends Referable> void assertAASEquals(T expected, T actual) {
        Assert.assertEquals(
                DeepCopyHelper.deepCopy(expected, Referable.class),
                DeepCopyHelper.deepCopy(actual, Referable.class));
    }


    protected <T extends Referable> void assertAASNotEquals(T expected, T actual) {
        Assert.assertNotEquals(
                DeepCopyHelper.deepCopy(expected, Referable.class),
                DeepCopyHelper.deepCopy(actual, Referable.class));
    }


    protected <T extends Referable> void assertAASElements(Collection<T> actual, SubmodelElement... elements) {
        Collection<Referable> actualCopy = DeepCopyHelper.deepCopy(actual, Referable.class);
        SubmodelElement[] expected = Stream.of(elements).map(x -> DeepCopyHelper.deepCopy(x, SubmodelElement.class)).toArray(SubmodelElement[]::new);
        assertThat(actualCopy, Matchers.containsInAnyOrder(expected));
    }


    protected void assertAASElements(SubmodelElementCollection actual, SubmodelElement... elements) {
        assertAASElements(actual.getValues(), elements);
    }


    protected <T extends Referable> void assertAASHasElements(Collection<T> actual, SubmodelElement... elements) {
        Collection<Referable> actualCopy = DeepCopyHelper.deepCopy(actual, Referable.class);
        SubmodelElement[] expected = Stream.of(elements).map(x -> DeepCopyHelper.deepCopy(x, SubmodelElement.class)).toArray(SubmodelElement[]::new);
        assertThat(actualCopy, Matchers.hasItems(expected));
    }


    protected void assertAASHasElements(SubmodelElementCollection actual, SubmodelElement... elements) {
        assertAASHasElements(actual.getValues(), elements);
    }
}
