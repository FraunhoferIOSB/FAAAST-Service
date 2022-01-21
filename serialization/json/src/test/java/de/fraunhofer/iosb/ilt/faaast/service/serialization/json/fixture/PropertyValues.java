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

import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.Blob;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.EntityType;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.MultiLanguageProperty;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.ReferenceElement;
import io.adminshell.aas.v3.model.RelationshipElement;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultAnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.impl.DefaultBlob;
import io.adminshell.aas.v3.model.impl.DefaultEntity;
import io.adminshell.aas.v3.model.impl.DefaultFile;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultMultiLanguageProperty;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultRange;
import io.adminshell.aas.v3.model.impl.DefaultReferenceElement;
import io.adminshell.aas.v3.model.impl.DefaultRelationshipElement;
import io.adminshell.aas.v3.model.impl.DefaultSubmodel;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.io.File;


public class PropertyValues {

    private PropertyValues() {

    }

    public static final File PROPERTY_FILE = new File("src/test/resources/property.json");
    public static final Property PROPERTY = new DefaultProperty.Builder()
            .category("category")
            .idShort("prop1")
            .kind(ModelingKind.INSTANCE)
            .value("foo")
            .build();

    public static final File MULTI_LANGUAGE_PROPERTY_FILE = new File("src/test/resources/multilanguage-property.json");
    public static final MultiLanguageProperty MULTI_LANGUAGE_PROPERTY = new DefaultMultiLanguageProperty.Builder()
            .idShort("multiLanguageProp1")
            .kind(ModelingKind.INSTANCE)
            .value(new LangString("foo", "de"))
            .value(new LangString("bar", "en"))
            .build();

    public static final File RANGE_FILE = new File("src/test/resources/range.json");
    public static final Range RANGE = new DefaultRange.Builder()
            .idShort("range1")
            .kind(ModelingKind.INSTANCE)
            .min("3.0")
            .max("5.0")
            .build();

    public static final File REFERENCE_ELEMENT_GLOBAL_FILE = new File("src/test/resources/reference-element-global.json");
    public static final ReferenceElement REFERENCE_ELEMENT_GLOBAL = new DefaultReferenceElement.Builder()
            .idShort("referenceGlobal")
            .value(AasUtils.parseReference("(GlobalReference)[IRI]http://customer.com/demo/aas/1/1/1234859590,(GlobalReference)[IRI]http://customer.com/demo/aas/1/2/4567895050"))
            .build();

    public static final File REFERENCE_ELEMENT_MODEL_FILE = new File("src/test/resources/reference-element-model.json");
    public static final ReferenceElement REFERENCE_ELEMENT_MODEL = new DefaultReferenceElement.Builder()
            .idShort("referenceModel")
            .value(AasUtils.parseReference("(Submodel)[IRI]http://customer.com/demo/aas/1/1/1234859590,(Property)[ID_SHORT]MaxRotationSpeed"))
            .build();

    public static final File FILE_FILE = new File("src/test/resources/file.json");
    public static final io.adminshell.aas.v3.model.File FILE = new DefaultFile.Builder()
            .idShort("file1")
            .kind(ModelingKind.INSTANCE)
            .mimeType("application/pdf")
            .value("SafetyInstructions.pdf")
            .build();

    public static final File BLOB_FILE_WITH_BLOB = new File("src/test/resources/blob-withblob.json");
    public static final File BLOB_FILE_WITHOUT_BLOB = new File("src/test/resources/blob-withoutblob.json");
    public static final Blob BLOB = new DefaultBlob.Builder()
            .idShort("blob1")
            .kind(ModelingKind.INSTANCE)
            .mimeType("application/octet-stream")
            .value("example-data".getBytes())
            .build();

    public static final File RELATIONSHIP_ELEMENT_FILE = new File("src/test/resources/relationship-element.json");
    public static final RelationshipElement RELATIONSHIP_ELEMENT = new DefaultRelationshipElement.Builder()
            .idShort("relationship1")
            .kind(ModelingKind.INSTANCE)
            .first(REFERENCE_ELEMENT_GLOBAL.getValue())
            .second(REFERENCE_ELEMENT_MODEL.getValue())
            .build();

    public static final File ANNOTATED_RELATIONSHIP_ELEMENT_FILE = new File("src/test/resources/annotated-relationship-element.json");
    public static final AnnotatedRelationshipElement ANNOTATED_RELATIONSHIP_ELEMENT = new DefaultAnnotatedRelationshipElement.Builder()
            .idShort("annotatedRelationship1")
            .kind(ModelingKind.INSTANCE)
            .first(REFERENCE_ELEMENT_GLOBAL.getValue())
            .second(REFERENCE_ELEMENT_MODEL.getValue())
            .annotation(new DefaultProperty.Builder()
                    .idShort("AppliedRule")
                    .value("TechnicalCurrentFlowDirection")
                    .build())
            .build();

    public static final File ENTITY_FILE = new File("src/test/resources/entity.json");
    public static final Entity ENTITY = new DefaultEntity.Builder()
            .idShort("entity1")
            .kind(ModelingKind.INSTANCE)
            .entityType(EntityType.SELF_MANAGED_ENTITY)
            .statement(new DefaultProperty.Builder()
                    .idShort("MaxRotationSpeed")
                    .value("5000")
                    .build())
            .globalAssetId(AasUtils.parseReference("(GlobalReference)[IRI]http://customer.com/demo/asset/1/1/MySubAsset"))
            .build();

    public static final File ELEMENT_COLLECTION_FILE = new File("src/test/resources/element-collection.json");
    public static final SubmodelElementCollection ELEMENT_COLLECTION = new DefaultSubmodelElementCollection.Builder()
            .idShort("collection1")
            .kind(ModelingKind.INSTANCE)
            .value(PROPERTY)
            .value(RANGE)
            .value(ENTITY)
            .build();

    public static final File SUBMODEL_FILE = new File("src/test/resources/submodel.json");
    public static final Submodel SUBMODEL = new DefaultSubmodel.Builder()
            .category("category")
            .idShort("submodel1")
            .kind(ModelingKind.INSTANCE)
            .identification(new DefaultIdentifier.Builder()
                    .idType(IdentifierType.IRI)
                    .identifier("http://example.org/test")
                    .build())
            .submodelElement(PROPERTY)
            .submodelElement(RANGE)
            .submodelElement(ELEMENT_COLLECTION)
            .build();
}
