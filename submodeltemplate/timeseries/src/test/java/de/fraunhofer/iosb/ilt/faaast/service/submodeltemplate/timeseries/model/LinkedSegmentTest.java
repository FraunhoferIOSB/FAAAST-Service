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

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.junit.Assert;
import org.junit.Test;


public class LinkedSegmentTest extends BaseModelTest {

    @Test
    public void testConversionRoundTrip() {
        LinkedSegment expected = LINKED_SEGMENT;
        LinkedSegment actual = LinkedSegment.of(expected);
        assertAASEquals(expected, actual);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testWithAdditionalProperties() {
        LinkedSegment expected = LinkedSegment.builder()
                .idShort("idShort")
                .category("category")
                .description(new DefaultLangStringTextType.Builder()
                        .language("en")
                        .text("foo")
                        .build())
                .description(new DefaultLangStringTextType.Builder()
                        .language("de")
                        .text("bar")
                        .build())
                .semanticId(ReferenceBuilder.global(Constants.LINKED_SEGMENT_SEMANTIC_ID))
                .endpoint("host")
                .query("query")
                .build();
        LinkedSegment actual = LinkedSegment.of(expected);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testParseWithAdditionalElement() throws ValueFormatException {
        SubmodelElementCollection expected = new DefaultSubmodelElementCollection.Builder()
                .semanticId(ReferenceBuilder.global(Constants.LINKED_SEGMENT_SEMANTIC_ID))
                .value(new DefaultProperty.Builder()
                        .idShort(Constants.LINKED_SEGMENT_QUERY_ID_SHORT)
                        .valueType(Datatype.STRING.getAas4jDatatype())
                        .value(LINKED_SEGMENT.getQuery())
                        .build())
                .value(ADDITIONAL_ELEMENT)
                .build();
        Record actual = Record.of(expected);
        assertAASEquals(expected, actual);
    }


    @Test
    public void testAddAdditionalElement() throws ValueFormatException {
        LinkedSegment expected = LinkedSegment.builder()
                .value(ADDITIONAL_ELEMENT)
                .build();
        LinkedSegment actual = LinkedSegment.of(expected);
        assertAASHasElements(actual, ADDITIONAL_ELEMENT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testWithUpdatingElements() throws ValueFormatException {

        //        SubmodelElementCollection emptyRecords = new DefaultSubmodelElementCollection.Builder()
        //                .idShort(Constants.INTERNAL_SEGMENT_RECORDS_ID_SHORT)
        //                .build();
        Property emptyEndpoint = new DefaultProperty.Builder()
                .idShort(Constants.LINKED_SEGMENT_ENDPOINT_ID_SHORT)
                .valueType(Datatype.STRING.getAas4jDatatype())
                .value(null)
                .build();

        Property endpoint = new DefaultProperty.Builder()
                .idShort(Constants.LINKED_SEGMENT_ENDPOINT_ID_SHORT)
                .valueType(Datatype.STRING.getAas4jDatatype())
                .value(LINKED_SEGMENT.getEndpoint())
                .build();

        Property emptyQuery = new DefaultProperty.Builder()
                .idShort(Constants.LINKED_SEGMENT_QUERY_ID_SHORT)
                .valueType(Datatype.STRING.getAas4jDatatype())
                .value(null)
                .build();

        Property query = new DefaultProperty.Builder()
                .idShort(Constants.LINKED_SEGMENT_QUERY_ID_SHORT)
                .valueType(Datatype.STRING.getAas4jDatatype())
                .value(LINKED_SEGMENT.getQuery())
                .build();

        LinkedSegment segment = new LinkedSegment();
        assertAASElements(segment, emptyEndpoint, emptyQuery);

        segment.setQuery(query.getValue());
        assertAASElements(segment, emptyEndpoint, query);

        segment.setEndpoint(endpoint.getValue());
        assertAASElements(segment, endpoint, query);

        segment.setQuery(null);
        assertAASElements(segment, endpoint, emptyQuery);

        segment.setEndpoint(null);
        assertAASElements(segment, emptyEndpoint, emptyQuery);
    }
}
