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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationSyncRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetOperationAsyncResultResponse;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.io.File;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import javax.xml.datatype.DatatypeFactory;
import org.eclipse.digitaltwin.aas4j.v3.model.AasSubmodelElements;
import org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.BasicEventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Direction;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.EntityType;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultBasicEventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultBlob;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEntity;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultFile;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultRange;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultResult;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;


public class ValueOnlyExamples {

    private static final String RESOURCE_PATH = "src/test/resources/valueonly";

    public static final File ANNOTATED_RELATIONSHIP_ELEMENT_FILE = new File(RESOURCE_PATH + "/annotated-relationship-element.json");
    public static final File BASIC_EVENT_ELEMENT_FILE = new File(RESOURCE_PATH + "/basic-event-element.json");
    public static final File BLOB_FILE_WITHOUT_BLOB = new File(RESOURCE_PATH + "/blob-withoutblob.json");
    public static final File BLOB_FILE_WITH_BLOB = new File(RESOURCE_PATH + "/blob-withblob.json");
    public static final File ENTITY_FILE = new File(RESOURCE_PATH + "/entity.json");
    public static final File FILE_FILE = new File(RESOURCE_PATH + "/file.json");
    public static final File GET_OPERATION_ASYNC_RESULT_RESPONSE_FILE = new File(RESOURCE_PATH + "/get-operation-async-result-response.json");
    public static final File INVOKE_OPERATION_REQUEST_FILE = new File(RESOURCE_PATH + "/invoke-operation-request.json");
    public static final File MULTI_LANGUAGE_PROPERTY_FILE = new File(RESOURCE_PATH + "/multilanguage-property.json");
    public static final File PROPERTY_DATETIME_FILE = new File(RESOURCE_PATH + "/property-datetime.json");
    public static final File PROPERTY_DOUBLE_FILE = new File(RESOURCE_PATH + "/property-double.json");
    public static final File PROPERTY_GDAY_FILE = new File(RESOURCE_PATH + "/property-gday.json");
    public static final File PROPERTY_INT_FILE = new File(RESOURCE_PATH + "/property-int.json");
    public static final File PROPERTY_STRING_FILE = new File(RESOURCE_PATH + "/property-string.json");
    public static final File RANGE_DOUBLE_FILE = new File(RESOURCE_PATH + "/range-double.json");
    public static final File RANGE_INT_FILE = new File(RESOURCE_PATH + "/range-int.json");
    public static final File REFERENCE_ELEMENT_GLOBAL_FILE = new File(RESOURCE_PATH + "/reference-element-global.json");
    public static final File REFERENCE_ELEMENT_MODEL_FILE = new File(RESOURCE_PATH + "/reference-element-model.json");
    public static final File RELATIONSHIP_ELEMENT_FILE = new File(RESOURCE_PATH + "/relationship-element.json");
    public static final File SUBMODEL_ELEMENT_COLLECTION_FILE = new File(RESOURCE_PATH + "/submodel-element-collection.json");
    public static final File SUBMODEL_ELEMENT_LIST_FILE = new File(RESOURCE_PATH + "/submodel-element-list.json");
    public static final File SUBMODEL_ELEMENT_LIST_SIMPLE_FILE = new File(RESOURCE_PATH + "/submodel-element-list-simple.json");
    public static final File SUBMODEL_FILE = new File(RESOURCE_PATH + "/submodel.json");

    public static final Blob BLOB = new DefaultBlob.Builder()
            .idShort("blob1")
            .contentType("application/octet-stream")
            .value("example-data".getBytes())
            .build();

    public static final BasicEventElement BASIC_EVENT_ELEMENT = new DefaultBasicEventElement.Builder()
            .idShort("basicEventElement1")
            .direction(Direction.INPUT)
            .observed(ReferenceBuilder.forSubmodel("http://example.org/submodel/1", "http://example.org/element/1"))
            .build();

    public static final Operation CONTEXT_OPERATION_INVOKE = new DefaultOperation.Builder()
            .inputVariables(new DefaultOperationVariable.Builder()
                    .value(new DefaultProperty.Builder()
                            .idShort("inString")
                            .valueType(DataTypeDefXsd.STRING)
                            .build())
                    .build())
            .inputVariables(new DefaultOperationVariable.Builder()
                    .value(new DefaultProperty.Builder()
                            .idShort("inInt")
                            .valueType(DataTypeDefXsd.INT)
                            .build())
                    .build())
            .inputVariables(new DefaultOperationVariable.Builder()
                    .value(new DefaultProperty.Builder()
                            .idShort("inDouble")
                            .valueType(DataTypeDefXsd.DOUBLE)
                            .build())
                    .build())
            .inoutputVariables(new DefaultOperationVariable.Builder()
                    .value(new DefaultProperty.Builder()
                            .idShort("inoutString")
                            .valueType(DataTypeDefXsd.STRING)
                            .build())
                    .build())
            .inoutputVariables(new DefaultOperationVariable.Builder()
                    .value(new DefaultProperty.Builder()
                            .idShort("inoutInt")
                            .valueType(DataTypeDefXsd.INT)
                            .build())
                    .build())
            .inoutputVariables(new DefaultOperationVariable.Builder()
                    .value(new DefaultProperty.Builder()
                            .idShort("inoutDouble")
                            .valueType(DataTypeDefXsd.DOUBLE)
                            .build())
                    .build())
            .build();

    public static final Entity ENTITY = new DefaultEntity.Builder()
            .idShort("entity1")
            .entityType(EntityType.SELF_MANAGED_ENTITY)
            .statements(new DefaultProperty.Builder()
                    .idShort("MaxRotationSpeed")
                    .valueType(DataTypeDefXsd.INT)
                    .value("5000")
                    .build())
            .globalAssetId("http://customer.com/demo/asset/1/1/MySubAsset")
            .build();

    public static final org.eclipse.digitaltwin.aas4j.v3.model.File FILE = new DefaultFile.Builder()
            .idShort("file1")
            .contentType("application/pdf")
            .value("SafetyInstructions.pdf")
            .build();

    public static final GetOperationAsyncResultResponse GET_OPERATION_ASYNC_RESULT_RESPONSE = GetOperationAsyncResultResponse.builder()
            .success()
            .payload(new DefaultOperationResult.Builder()
                    .executionState(ExecutionState.COMPLETED)
                    .inoutputArguments(new DefaultOperationVariable.Builder()
                            .value(new DefaultProperty.Builder()
                                    .idShort("inoutString")
                                    .valueType(DataTypeDefXsd.STRING)
                                    .value("bar")
                                    .build())
                            .build())
                    .inoutputArguments(new DefaultOperationVariable.Builder()
                            .value(new DefaultProperty.Builder()
                                    .idShort("inoutInt")
                                    .valueType(DataTypeDefXsd.INT)
                                    .value("-42")
                                    .build())
                            .build())
                    .inoutputArguments(new DefaultOperationVariable.Builder()
                            .value(new DefaultProperty.Builder()
                                    .idShort("inoutDouble")
                                    .valueType(DataTypeDefXsd.DOUBLE)
                                    .value("17.42")
                                    .build())
                            .build())
                    .outputArguments(new DefaultOperationVariable.Builder()
                            .value(new DefaultProperty.Builder()
                                    .idShort("outString")
                                    .valueType(DataTypeDefXsd.STRING)
                                    .value("foo-bar")
                                    .build())
                            .build())
                    .outputArguments(new DefaultOperationVariable.Builder()
                            .value(new DefaultProperty.Builder()
                                    .idShort("outInt")
                                    .valueType(DataTypeDefXsd.INT)
                                    .value("-24")
                                    .build())
                            .build())
                    .outputArguments(new DefaultOperationVariable.Builder()
                            .value(new DefaultProperty.Builder()
                                    .idShort("outDouble")
                                    .valueType(DataTypeDefXsd.DOUBLE)
                                    .value("24.24")
                                    .build())
                            .build())
                    .build())
            .result(new DefaultResult.Builder()
                    .messages(Message.builder()
                            .messageType(MessageTypeEnum.INFO)
                            .text("some message text")
                            .timestamp("2024-01-01T00:00:00.000+00:00")
                            .build())
                    .build())
            .build();

    public static final InvokeOperationSyncRequest INVOKE_OPERATION_SYNC_REQUEST = InvokeOperationSyncRequest.builder()
            .submodelId("http://example.org/submodels/1")
            .path("my.test.operation")
            .timeout(DatatypeFactory.newDefaultInstance().newDuration("P1Y2M3DT1H2M3S"))
            .inputArgument(new DefaultOperationVariable.Builder()
                    .value(new DefaultProperty.Builder()
                            .idShort("inString")
                            .valueType(DataTypeDefXsd.STRING)
                            .value("foo")
                            .build())
                    .build())
            .inputArgument(new DefaultOperationVariable.Builder()
                    .value(new DefaultProperty.Builder()
                            .idShort("inInt")
                            .valueType(DataTypeDefXsd.INT)
                            .value("42")
                            .build())
                    .build())
            .inputArgument(new DefaultOperationVariable.Builder()
                    .value(new DefaultProperty.Builder()
                            .idShort("inDouble")
                            .valueType(DataTypeDefXsd.DOUBLE)
                            .value("42.17")
                            .build())
                    .build())
            .inoutputArgument(new DefaultOperationVariable.Builder()
                    .value(new DefaultProperty.Builder()
                            .idShort("inoutString")
                            .valueType(DataTypeDefXsd.STRING)
                            .value("bar")
                            .build())
                    .build())
            .inoutputArgument(new DefaultOperationVariable.Builder()
                    .value(new DefaultProperty.Builder()
                            .idShort("inoutInt")
                            .valueType(DataTypeDefXsd.INT)
                            .value("-42")
                            .build())
                    .build())
            .inoutputArgument(new DefaultOperationVariable.Builder()
                    .value(new DefaultProperty.Builder()
                            .idShort("inoutDouble")
                            .valueType(DataTypeDefXsd.DOUBLE)
                            .value("17.42")
                            .build())
                    .build())
            .build();

    public static final MultiLanguageProperty MULTI_LANGUAGE_PROPERTY = new DefaultMultiLanguageProperty.Builder()
            .idShort("multiLanguageProp1")
            .value(new DefaultLangStringTextType.Builder()
                    .language("de")
                    .text("foo")
                    .build())
            .value(new DefaultLangStringTextType.Builder()
                    .language("en")
                    .text("bar")
                    .build())
            .build();
    public static final Property PROPERTY_DATETIME = new DefaultProperty.Builder()
            .category("category")
            .idShort("propDateTime")
            .valueType(DataTypeDefXsd.DATE_TIME)
            .value(OffsetDateTime.of(2022, 7, 31, 17, 8, 51, 0, ZoneOffset.UTC).toString())
            .build();
    public static final Property PROPERTY_DOUBLE = new DefaultProperty.Builder()
            .category("category")
            .idShort("propDouble")
            .valueType(DataTypeDefXsd.DOUBLE)
            .value("42.17")
            .build();

    public static final Property PROPERTY_GDAY = new DefaultProperty.Builder()
            .category("category")
            .idShort("propGDay")
            .value("---15")
            .valueType(DataTypeDefXsd.GDAY)
            .build();

    public static final Property PROPERTY_INT = new DefaultProperty.Builder()
            .category("category")
            .idShort("propInt")
            .valueType(DataTypeDefXsd.INT)
            .value("42")
            .build();

    public static final Property PROPERTY_STRING = new DefaultProperty.Builder()
            .category("category")
            .idShort("propString")
            .value("foo")
            .build();

    public static final Range RANGE_DOUBLE = new DefaultRange.Builder()
            .idShort("rangeDouble")
            .valueType(DataTypeDefXsd.DOUBLE)
            .min("3.0")
            .max("5.0")
            .build();

    public static final Range RANGE_INT = new DefaultRange.Builder()
            .idShort("rangeInt")
            .valueType(DataTypeDefXsd.INT)
            .min("17")
            .max("42")
            .build();

    public static final ReferenceElement REFERENCE_ELEMENT_GLOBAL = new DefaultReferenceElement.Builder()
            .idShort("referenceGlobal")
            .value(new DefaultReference.Builder()
                    .type(ReferenceTypes.EXTERNAL_REFERENCE)
                    .keys(new DefaultKey.Builder()
                            .type(KeyTypes.GLOBAL_REFERENCE)
                            .value("http://customer.com/demo/aas/1/1/1234859590")
                            .build())
                    .build())
            .build();

    public static final ReferenceElement REFERENCE_ELEMENT_MODEL = new DefaultReferenceElement.Builder()
            .idShort("referenceModel")
            .value(new DefaultReference.Builder()
                    .type(ReferenceTypes.MODEL_REFERENCE)
                    .keys(new DefaultKey.Builder()
                            .type(KeyTypes.SUBMODEL)
                            .value("http://customer.com/demo/aas/1/1/1234859590")
                            .build())
                    .keys(new DefaultKey.Builder()
                            .type(KeyTypes.PROPERTY)
                            .value("MaxRotationSpeed")
                            .build())
                    .build())
            .build();

    public static final RelationshipElement RELATIONSHIP_ELEMENT = new DefaultRelationshipElement.Builder()
            .idShort("relationship1")
            .first(REFERENCE_ELEMENT_GLOBAL.getValue())
            .second(REFERENCE_ELEMENT_MODEL.getValue())
            .build();

    public static final SubmodelElementCollection SUBMODEL_ELEMENT_COLLECTION = new DefaultSubmodelElementCollection.Builder()
            .idShort("collection1")
            .value(PROPERTY_STRING)
            .value(RANGE_DOUBLE)
            .value(ENTITY)
            .build();
    public static final Submodel SUBMODEL = new DefaultSubmodel.Builder()
            .category("category")
            .idShort("submodel1")
            .id("http://example.org/test")
            .submodelElements(PROPERTY_STRING)
            .submodelElements(RANGE_DOUBLE)
            .submodelElements(SUBMODEL_ELEMENT_COLLECTION)
            .submodelElements(new DefaultOperation.Builder()
                    .idShort("operation1")
                    .build())
            .build();
    public static final SubmodelElementList SUBMODEL_ELEMENT_LIST = new DefaultSubmodelElementList.Builder()
            .idShort("listOfLists")
            .typeValueListElement(AasSubmodelElements.SUBMODEL_ELEMENT_LIST)
            .value(new DefaultSubmodelElementList.Builder()
                    .typeValueListElement(AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION)
                    .value(new DefaultSubmodelElementCollection.Builder()
                            .value(new DefaultProperty.Builder()
                                    .idShort("foo")
                                    .valueType(DataTypeDefXsd.STRING)
                                    .value("foo")
                                    .build())
                            .value(new DefaultProperty.Builder()
                                    .idShort("bar")
                                    .valueType(DataTypeDefXsd.INTEGER)
                                    .value("42")
                                    .build())
                            .build())
                    .value(new DefaultSubmodelElementCollection.Builder()
                            .value(new DefaultProperty.Builder()
                                    .idShort("foo")
                                    .valueType(DataTypeDefXsd.DOUBLE)
                                    .value("3.14")
                                    .build())
                            .value(new DefaultProperty.Builder()
                                    .idShort("bar")
                                    .valueType(DataTypeDefXsd.BOOLEAN)
                                    .value("true")
                                    .build())
                            .build())
                    .build())
            .value(new DefaultSubmodelElementList.Builder()
                    .typeValueListElement(AasSubmodelElements.SUBMODEL_ELEMENT_COLLECTION)
                    .value(new DefaultSubmodelElementCollection.Builder()
                            .value(new DefaultProperty.Builder()
                                    .idShort("foo")
                                    .valueType(DataTypeDefXsd.STRING)
                                    .value("bar")
                                    .build())
                            .value(new DefaultProperty.Builder()
                                    .idShort("bar")
                                    .valueType(DataTypeDefXsd.INTEGER)
                                    .value("17")
                                    .build())
                            .build())
                    .value(new DefaultSubmodelElementCollection.Builder()
                            .value(new DefaultProperty.Builder()
                                    .idShort("foo")
                                    .valueType(DataTypeDefXsd.DOUBLE)
                                    .value("0.01")
                                    .build())
                            .value(new DefaultProperty.Builder()
                                    .idShort("bar")
                                    .valueType(DataTypeDefXsd.BOOLEAN)
                                    .value("false")
                                    .build())
                            .build())
                    .build())
            .value(new DefaultSubmodelElementList.Builder()
                    .typeValueListElement(AasSubmodelElements.PROPERTY)
                    .valueTypeListElement(DataTypeDefXsd.INTEGER)
                    .value(new DefaultProperty.Builder()
                            .idShort("integer1")
                            .valueType(DataTypeDefXsd.INTEGER)
                            .value("1")
                            .build())
                    .value(new DefaultProperty.Builder()
                            .idShort("integer2")
                            .valueType(DataTypeDefXsd.INTEGER)
                            .value("2")
                            .build())
                    .build())
            .build();

    public static final SubmodelElementList SUBMODEL_ELEMENT_LIST_SIMPLE = new DefaultSubmodelElementList.Builder()
            .idShort("list")
            .typeValueListElement(AasSubmodelElements.PROPERTY)
            .valueTypeListElement(DataTypeDefXsd.INTEGER)
            .value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.INTEGER)
                    .value("1")
                    .build())
            .value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.INTEGER)
                    .value("2")
                    .build())
            .value(new DefaultProperty.Builder()
                    .valueType(DataTypeDefXsd.INTEGER)
                    .value("3")
                    .build())
            .build();

    public static final AnnotatedRelationshipElement ANNOTATED_RELATIONSHIP_ELEMENT = new DefaultAnnotatedRelationshipElement.Builder()
            .idShort("annotatedRelationship1")
            .first(REFERENCE_ELEMENT_GLOBAL.getValue())
            .second(REFERENCE_ELEMENT_MODEL.getValue())
            .annotations(new DefaultProperty.Builder()
                    .idShort("AppliedRule")
                    .value("TechnicalCurrentFlowDirection")
                    .build())
            .build();

    private ValueOnlyExamples() {

    }
}
