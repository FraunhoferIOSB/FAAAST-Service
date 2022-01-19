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

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementCollectionValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.FileValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.RelationshipElementValue;
import io.adminshell.aas.v3.model.EntityType;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import java.io.File;
import java.util.Arrays;

public class PropertyValues {

    private PropertyValues() {

    }

    public static final File PROPERTY_VALUE_FILE = new File("src/test/resources/property-value.json");
    public static final PropertyValue PROPERTY_VALUE = PropertyValue.builder()
            .value("foo")
            .build();

    public static final File MULTI_LANGUAGE_PROPERTY_VALUE_FILE = new File("src/test/resources/multilanguage-property-value.json");
    public static final MultiLanguagePropertyValue MULTI_LANGUAGE_PROPERTY_VALUE = MultiLanguagePropertyValue.builder()
            .value("de", "foo")
            .value("en", "bar")
            .build();

    public static final File RANGE_VALUE_FILE = new File("src/test/resources/range-value.json");
    public static final RangeValue RANGE_VALUE = RangeValue.builder()
            .min(3)
            .max(5)
            .build();

    public static final File REFERENCE_ELEMENT_VALUE_GLOBAL_FILE = new File("src/test/resources/reference-element-value-global.json");
    public static final ReferenceElementValue REFERENCE_ELEMENT_GLOBAL_VALUE = ReferenceElementValue.builder()
            .key(KeyType.IRI, KeyElements.GLOBAL_REFERENCE, "http://customer.com/demo/aas/1/1/1234859590")
            .key(KeyType.IRI, KeyElements.GLOBAL_REFERENCE, "http://customer.com/demo/aas/1/2/4567895050")
            .build();

    public static final File REFERENCE_ELEMENT_VALUE_MODEL_FILE = new File("src/test/resources/reference-element-value-model.json");
    public static final ReferenceElementValue REFERENCE_ELEMENT_MODEL_VALUE = ReferenceElementValue.builder()
            .key(KeyType.IRI, KeyElements.SUBMODEL, "http://customer.com/demo/aas/1/1/1234859590")
            .key(KeyType.ID_SHORT, KeyElements.PROPERTY, "MaxRotationSpeed")
            .build();

    public static final File FILE_VALUE_FILE = new File("src/test/resources/file-value.json");
    public static final FileValue FILE_VALUE = FileValue.builder()
            .mimeType("application/pdf")
            .value("SafetyInstructions.pdf")
            .build();

    public static final File BLOB_VALUE_FILE_WITH_BLOB = new File("src/test/resources/blob-value-withblob.json");
    public static final File BLOB_VALUE_FILE_WITHOUT_BLOB = new File("src/test/resources/blob-value-withoutblob.json");
    public static final BlobValue BLOB_VALUE = BlobValue.builder()
            .mimeType("application/octet-stream")
            .value("example-data")
            .build();

    public static final File RELATIONSHIP_ELEMENT_VALUE_FILE = new File("src/test/resources/relationship-element-value.json");
    public static final RelationshipElementValue RELATIONSHIP_ELEMENT_GLOBAL_VALUE = RelationshipElementValue.builder()
            .first(REFERENCE_ELEMENT_GLOBAL_VALUE.getKeys())
            .second(REFERENCE_ELEMENT_MODEL_VALUE.getKeys())
            .build();

    public static final File ANNOTATED_RELATIONSHIP_ELEMENT_VALUE_FILE = new File("src/test/resources/annotated-relationship-element-value.json");
    public static final AnnotatedRelationshipElementValue ANNOTATED_RELATIONSHIP_ELEMENT_GLOBAL_VALUE = new AnnotatedRelationshipElementValue.Builder()
            .first(REFERENCE_ELEMENT_GLOBAL_VALUE.getKeys())
            .second(REFERENCE_ELEMENT_MODEL_VALUE.getKeys())
            .annotation("AppliedRule", PropertyValue.builder()
                    .value("TechnicalCurrentFlowDirection")
                    .build())
            .build();

    public static final File ENTITY_VALUE_FILE = new File("src/test/resources/entity-value.json");
    public static final EntityValue ENTITY_ELEMENT_VALUE = new EntityValue.Builder()
            .entityType(EntityType.SELF_MANAGED_ENTITY)
            .statement("MaxRotationSpeed", PropertyValue.builder()
                    .value("5000")
                    .build())
            .globalAssetId(Arrays.asList(new DefaultKey.Builder()
                    .idType(KeyType.IRI)
                    .type(KeyElements.GLOBAL_REFERENCE)
                    .value("http://customer.com/demo/asset/1/1/MySubAsset")
                    .build()))
            .build();

    public static final File ELEMENT_COLLECTION_VALUE_FILE = new File("src/test/resources/element-collection-value.json");
    public static final ElementCollectionValue ELEMENT_COLLECTION_VALUE = new ElementCollectionValue.Builder()
            .value("exampleProperty", PROPERTY_VALUE)
            .value("exampleRange", RANGE_VALUE)
            .value("exampleEntity", ENTITY_ELEMENT_VALUE)
            .build();

}
