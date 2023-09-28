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
package de.fraunhofer.iosb.ilt.faaast.service.serialization.json.fixture;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ElementReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationFinishEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.OperationInvokeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.access.ValueReadEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.error.ErrorEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.error.ErrorLevel;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.FileValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.RelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.SubmodelElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXSD;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;


public class EventValues {

    private static final String RESOURCE_PATH = "src/test/resources";

    public static final String ELEMENT_READ_EVENT_FILE = RESOURCE_PATH + "/eventmessage-elementread.json";
    public static final String OPERATION_FINISH_EVENT_FILE = RESOURCE_PATH + "/eventmessage-operationfinish.json";
    public static final String OPERATION_INVOKE_EVENT_FILE = RESOURCE_PATH + "/eventmessage-operationinvoke.json";
    public static final String VALUE_READ_EVENT_FILE = RESOURCE_PATH + "/eventmessage-valueread.json";

    public static final String ELEMENT_CREATE_EVENT_FILE = RESOURCE_PATH + "/eventmessage-elementcreate.json";
    public static final String ELEMENT_DELETE_EVENT_FILE = RESOURCE_PATH + "/eventmessage-elementdelete.json";
    public static final String ELEMENT_UPDATE_EVENT_FILE = RESOURCE_PATH + "/eventmessage-elementupdate.json";
    public static final String VALUE_CHANGE_EVENT_FILE = RESOURCE_PATH + "/eventmessage-valuechange.json";

    public static final String ERROR_EVENT_FILE = RESOURCE_PATH + "/eventmessage-error.json";

    private static final Reference REFERENCE = new DefaultReference.Builder()
            .type(ReferenceTypes.MODEL_REFERENCE)
            .keys(new DefaultKey.Builder()
                    .type(KeyTypes.SUBMODEL)
                    .value("http://example.org/submodel")
                    .build())
            .keys(new DefaultKey.Builder()
                    .type(KeyTypes.PROPERTY)
                    .value("property")
                    .build())
            .build();

    private static final Property PROPERTY_INT = new DefaultProperty.Builder()
            .idShort("PROPERTY_INT")
            .valueType(DataTypeDefXSD.INT)
            .value("1")
            .build();
    private static final Property PROPERTY_DOUBLE = new DefaultProperty.Builder()
            .idShort("PROPERTY_DOUBLE")
            .valueType(DataTypeDefXSD.DOUBLE)
            .value("3.14")
            .build();

    private static final Property PROPERTY_STRING = new DefaultProperty.Builder()
            .idShort("PROPERTY_STRING")
            .valueType(DataTypeDefXSD.STRING)
            .value("example value")
            .build();

    private static final PropertyValue PROPERTY_VALUE_INT;
    private static final PropertyValue PROPERTY_VALUE_DOUBLE;
    private static final PropertyValue PROPERTY_VALUE_STRING;
    private static final RangeValue RANGE_VALUE;
    private static final RelationshipElementValue RELATIONSHIP_ELEMENT_VALUE;
    private static final AnnotatedRelationshipElementValue ANNOTATED_RELATIONSHIP_ELEMENT_VALUE;
    private static final MultiLanguagePropertyValue MULTILANGUAGE_PROPERTY_VALUE;
    private static final FileValue FILE_VALUE;
    private static final ReferenceElementValue REFERENCE_ELEMENT_VALUE;
    private static final SubmodelElementCollectionValue SUBMODEL_ELEMENT_COLLECTION_VALUE;
    private static final List<ElementValue> ALL_ELEMENT_VALUE_TYPES;

    static {
        try {
            PROPERTY_VALUE_INT = ElementValueMapper.toValue(PROPERTY_INT);
            PROPERTY_VALUE_DOUBLE = ElementValueMapper.toValue(PROPERTY_DOUBLE);
            PROPERTY_VALUE_STRING = ElementValueMapper.toValue(PROPERTY_STRING);
            RANGE_VALUE = RangeValue.builder()
                    .min(TypedValueFactory.create(Datatype.DOUBLE, "0.1"))
                    .max(TypedValueFactory.create(Datatype.DOUBLE, "0.2"))
                    .build();
            RELATIONSHIP_ELEMENT_VALUE = RelationshipElementValue.builder()
                    .first(REFERENCE)
                    .second(REFERENCE)
                    .build();
            ANNOTATED_RELATIONSHIP_ELEMENT_VALUE = new AnnotatedRelationshipElementValue.Builder()
                    .first(REFERENCE)
                    .second(REFERENCE)
                    .annotation("exampleAnnotation", PROPERTY_VALUE_INT)
                    .build();
            MULTILANGUAGE_PROPERTY_VALUE = MultiLanguagePropertyValue.builder()
                    .value("de", "deutsch")
                    .value("en", "english")
                    .build();
            FILE_VALUE = FileValue.builder()
                    .mimeType("mimeType")
                    .value("/example.txt")
                    .build();
            REFERENCE_ELEMENT_VALUE = ReferenceElementValue.builder()
                    .value(REFERENCE)
                    .build();
            SUBMODEL_ELEMENT_COLLECTION_VALUE = SubmodelElementCollectionValue.builder()
                    .value("property_int", PROPERTY_VALUE_INT)
                    .value("property_double", PROPERTY_VALUE_DOUBLE)
                    .value("property_string", PROPERTY_VALUE_STRING)
                    .value("nested_collection", SubmodelElementCollectionValue.builder()
                            .value("nested_property_int", PROPERTY_VALUE_INT)
                            .value("nested_property_double", PROPERTY_VALUE_DOUBLE)
                            .value("nested_property_string", PROPERTY_VALUE_STRING)
                            .build())
                    .build();
            ALL_ELEMENT_VALUE_TYPES = List.of(
                    PROPERTY_VALUE_INT,
                    PROPERTY_VALUE_DOUBLE,
                    PROPERTY_VALUE_STRING,
                    RANGE_VALUE,
                    RELATIONSHIP_ELEMENT_VALUE,
                    ANNOTATED_RELATIONSHIP_ELEMENT_VALUE,
                    MULTILANGUAGE_PROPERTY_VALUE,
                    FILE_VALUE,
                    REFERENCE_ELEMENT_VALUE,
                    SUBMODEL_ELEMENT_COLLECTION_VALUE);

        }
        catch (ValueFormatException | ValueMappingException e) {
            throw new IllegalStateException("Error initializing data strcutures for test", e);
        }
    }

    // access events
    public static final ElementReadEventMessage ELEMENT_READ_EVENT = ElementReadEventMessage.builder()
            .element(REFERENCE)
            .value(PROPERTY_STRING)
            .build();

    public static OperationInvokeEventMessage OPERATION_INVOKE_EVENT = OperationInvokeEventMessage.builder()
            .element(REFERENCE)
            .input(ALL_ELEMENT_VALUE_TYPES)
            .inoutput(ALL_ELEMENT_VALUE_TYPES)
            .build();

    public static OperationFinishEventMessage OPERATION_FINISH_EVENT = OperationFinishEventMessage.builder()
            .element(REFERENCE)
            .output(ALL_ELEMENT_VALUE_TYPES)
            .inoutput(ALL_ELEMENT_VALUE_TYPES)
            .build();

    public static ValueReadEventMessage VALUE_READ_EVENT = ValueReadEventMessage.builder()
            .element(REFERENCE)
            .value(PROPERTY_VALUE_STRING)
            .build();

    // change events
    public static ElementCreateEventMessage ELEMENT_CREATE_EVENT = ElementCreateEventMessage.builder()
            .element(REFERENCE)
            .value(PROPERTY_INT)
            .build();

    public static ElementDeleteEventMessage ELEMENT_DELETE_EVENT = ElementDeleteEventMessage.builder()
            .element(REFERENCE)
            .value(PROPERTY_INT)
            .build();

    public static ElementUpdateEventMessage ELEMENT_UPDATE_EVENT = ElementUpdateEventMessage.builder()
            .element(REFERENCE)
            .value(PROPERTY_INT)
            .build();

    public static ValueChangeEventMessage VALUE_CHANGE_EVENT = ValueChangeEventMessage.builder()
            .element(REFERENCE)
            .oldValue(PROPERTY_VALUE_INT)
            .newValue(PROPERTY_VALUE_DOUBLE)
            .build();

    // error events
    public static ErrorEventMessage ERROR_EVENT = ErrorEventMessage.builder()
            .element(REFERENCE)
            .level(ErrorLevel.WARN)
            .message("This is a warning")
            .build();

    private EventValues() {

    }
}