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
package de.fraunhofer.iosb.ilt.faaast.service.test.model;

import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEntity;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;


public class AssetConnectionModelRecursive {
    public static final String INITIAL_VALUE = "initial value";
    // source elements
    public static final Property PROPERTY_SOURCE = new DefaultProperty.Builder()
            .idShort("source1")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final Submodel SUBMODEL_SOURCE = new DefaultSubmodel.Builder()
            .idShort("SubmodelSource")
            .id("http://example.org/submodel/source")
            .submodelElements(PROPERTY_SOURCE)
            .build();
    // target elements
    public static final Property PROPERTY = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();

    public static final Property PROPERTY_COL = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final Property PROPERTY_COL_COL = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final SubmodelElementCollection COLLECTION_COL = new DefaultSubmodelElementCollection.Builder()
            .idShort("collection")
            .value(PROPERTY_COL_COL)
            .build();
    public static final Property PROPERTY_COL_LIST = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final SubmodelElementList LIST_COL = new DefaultSubmodelElementList.Builder()
            .idShort("list")
            .value(PROPERTY_COL_LIST)
            .build();
    public static final Property PROPERTY_COL_ENT = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final Entity ENTITY_COL = new DefaultEntity.Builder()
            .idShort("entity")
            .statements(PROPERTY_COL_ENT)
            .build();
    public static final Property PROPERTY_COL_REL = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final AnnotatedRelationshipElement RELATIONSHIP_COL = new DefaultAnnotatedRelationshipElement.Builder()
            .idShort("relationship")
            .annotations(PROPERTY_COL_REL)
            .build();

    public static final SubmodelElementCollection COLLECTION = new DefaultSubmodelElementCollection.Builder()
            .idShort("collection")
            .value(PROPERTY_COL)
            .value(COLLECTION_COL)
            .value(LIST_COL)
            .value(ENTITY_COL)
            .value(RELATIONSHIP_COL)
            .build();

    public static final Property PROPERTY_LIST = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final Property PROPERTY_LIST_COL = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final SubmodelElementCollection COLLECTION_LIST = new DefaultSubmodelElementCollection.Builder()
            .idShort("collection")
            .value(PROPERTY_LIST_COL)
            .build();
    public static final Property PROPERTY_LIST_LIST = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final SubmodelElementList LIST_LIST = new DefaultSubmodelElementList.Builder()
            .idShort("list")
            .value(PROPERTY_LIST_LIST)
            .build();
    public static final Property PROPERTY_LIST_ENT = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final Entity ENTITY_LIST = new DefaultEntity.Builder()
            .idShort("entity")
            .statements(PROPERTY_LIST_ENT)
            .build();
    public static final Property PROPERTY_LIST_REL = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final AnnotatedRelationshipElement RELATIONSHIP_LIST = new DefaultAnnotatedRelationshipElement.Builder()
            .idShort("relationship")
            .annotations(PROPERTY_LIST_REL)
            .build();

    public static final SubmodelElementList LIST = new DefaultSubmodelElementList.Builder()
            .idShort("list")
            .value(PROPERTY_LIST)
            .value(COLLECTION_LIST)
            .value(LIST_LIST)
            .value(ENTITY_LIST)
            .value(RELATIONSHIP_LIST)
            .build();

    public static final Property PROPERTY_ENT = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final Property PROPERTY_ENT_COL = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final SubmodelElementCollection COLLECTION_ENT = new DefaultSubmodelElementCollection.Builder()
            .idShort("collection")
            .value(PROPERTY_ENT_COL)
            .build();
    public static final Property PROPERTY_ENT_LIST = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final SubmodelElementList LIST_ENT = new DefaultSubmodelElementList.Builder()
            .idShort("list")
            .value(PROPERTY_ENT_LIST)
            .build();
    public static final Property PROPERTY_ENT_ENT = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final Entity ENTITY_ENT = new DefaultEntity.Builder()
            .idShort("entity")
            .statements(PROPERTY_ENT_ENT)
            .build();
    public static final Property PROPERTY_ENT_REL = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final AnnotatedRelationshipElement RELATIONSHIP_ENT = new DefaultAnnotatedRelationshipElement.Builder()
            .idShort("relationship")
            .annotations(PROPERTY_ENT_REL)
            .build();

    public static final Entity ENTITY = new DefaultEntity.Builder()
            .idShort("entity")
            .statements(PROPERTY_ENT)
            .statements(COLLECTION_ENT)
            .statements(LIST_ENT)
            .statements(ENTITY_ENT)
            .statements(RELATIONSHIP_ENT)
            .build();

    public static final Property PROPERTY_REL = new DefaultProperty.Builder()
            .idShort("property")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();

    public static final AnnotatedRelationshipElement RELATIONSHIP = new DefaultAnnotatedRelationshipElement.Builder()
            .idShort("relationship")
            .annotations(PROPERTY_REL)
            .build();

    public static final Submodel SUBMODEL = new DefaultSubmodel.Builder()
            .idShort("submodel")
            .id("http://example.org/submodel")
            .submodelElements(PROPERTY)
            .submodelElements(COLLECTION)
            .submodelElements(LIST)
            .submodelElements(ENTITY)
            .submodelElements(RELATIONSHIP)
            .build();

    // references
    public static final Reference REFERENCE_SUBMODEL_SOURCE = ReferenceBuilder.forSubmodel(SUBMODEL_SOURCE);
    public static final Reference REFERENCE_PROPERTY_SOURCE = ReferenceBuilder.forSubmodel(SUBMODEL_SOURCE, PROPERTY_SOURCE);

    public static final Reference REFERENCE_SUBMODEL = ReferenceBuilder.forSubmodel(SUBMODEL);
    public static final Reference REFERENCE_COLLECTION = ReferenceBuilder.forSubmodel(SUBMODEL, COLLECTION);
    public static final Reference REFERENCE_COLLECTION_COL = ReferenceBuilder.forSubmodel(SUBMODEL, COLLECTION, COLLECTION_COL);
    public static final Reference REFERENCE_COLLECTION_LIST = ReferenceBuilder.forSubmodel(SUBMODEL, COLLECTION, LIST_COL);
    public static final Reference REFERENCE_COLLECTION_ENT = ReferenceBuilder.forSubmodel(SUBMODEL, COLLECTION, ENTITY_COL);
    public static final Reference REFERENCE_COLLECTION_REL = ReferenceBuilder.forSubmodel(SUBMODEL, COLLECTION, RELATIONSHIP_COL);

    public static final Reference REFERENCE_LIST = ReferenceBuilder.forSubmodel(SUBMODEL, LIST);
    public static final Reference REFERENCE_LIST_COL = ReferenceBuilder.with(REFERENCE_LIST).index(1, KeyTypes.SUBMODEL_ELEMENT_COLLECTION).build();
    public static final Reference REFERENCE_LIST_LIST = ReferenceBuilder.with(REFERENCE_LIST).index(2, KeyTypes.SUBMODEL_ELEMENT_LIST).build();
    public static final Reference REFERENCE_LIST_ENT = ReferenceBuilder.with(REFERENCE_LIST).index(3, KeyTypes.ENTITY).build();
    public static final Reference REFERENCE_LIST_REL = ReferenceBuilder.with(REFERENCE_LIST).index(4, KeyTypes.ANNOTATED_RELATIONSHIP_ELEMENT).build();

    public static final Reference REFERENCE_ENTITY = ReferenceBuilder.forSubmodel(SUBMODEL, ENTITY);
    public static final Reference REFERENCE_ENTITY_COL = ReferenceBuilder.forSubmodel(SUBMODEL, ENTITY, ENTITY_ENT);
    public static final Reference REFERENCE_ENTITY_LIST = ReferenceBuilder.forSubmodel(SUBMODEL, ENTITY, LIST_ENT);
    public static final Reference REFERENCE_ENTITY_ENT = ReferenceBuilder.forSubmodel(SUBMODEL, ENTITY, ENTITY_ENT);
    public static final Reference REFERENCE_ENTITY_REL = ReferenceBuilder.forSubmodel(SUBMODEL, ENTITY, RELATIONSHIP_ENT);

    public static final Reference REFERENCE_RELATIONSHIP = ReferenceBuilder.forSubmodel(SUBMODEL, RELATIONSHIP);

    public static final Reference REFERENCE_PROPERTY = ReferenceBuilder.forSubmodel(SUBMODEL, PROPERTY);

    public static final Reference REFERENCE_PROPERTY_COL = ReferenceBuilder.forSubmodel(SUBMODEL, COLLECTION, PROPERTY_COL);
    public static final Reference REFERENCE_PROPERTY_COL_COL = ReferenceBuilder.forSubmodel(SUBMODEL, COLLECTION, COLLECTION_COL, PROPERTY_COL_COL);
    public static final Reference REFERENCE_PROPERTY_COL_LIST = ReferenceBuilder.with(REFERENCE_COLLECTION_LIST).index(0, KeyTypes.PROPERTY).build();
    public static final Reference REFERENCE_PROPERTY_COL_ENT = ReferenceBuilder.forSubmodel(SUBMODEL, COLLECTION, ENTITY_COL, PROPERTY_COL_ENT);
    public static final Reference REFERENCE_PROPERTY_COL_REL = ReferenceBuilder.forSubmodel(SUBMODEL, COLLECTION, RELATIONSHIP_COL, PROPERTY_COL_REL);

    public static final Reference REFERENCE_PROPERTY_LIST = ReferenceBuilder.with(REFERENCE_LIST).index(0, KeyTypes.PROPERTY).build();
    public static final Reference REFERENCE_PROPERTY_LIST_COL = ReferenceBuilder.with(REFERENCE_LIST_COL).element(PROPERTY_LIST_COL).build();
    public static final Reference REFERENCE_PROPERTY_LIST_LIST = ReferenceBuilder.with(REFERENCE_LIST_LIST).index(0, KeyTypes.PROPERTY).build();
    public static final Reference REFERENCE_PROPERTY_LIST_ENT = ReferenceBuilder.with(REFERENCE_LIST_ENT).element(PROPERTY_LIST_ENT).build();
    public static final Reference REFERENCE_PROPERTY_LIST_REL = ReferenceBuilder.with(REFERENCE_LIST_REL).element(PROPERTY_LIST_REL).build();

    public static final Reference REFERENCE_PROPERTY_ENT = ReferenceBuilder.forSubmodel(SUBMODEL, ENTITY, PROPERTY_ENT);
    public static final Reference REFERENCE_PROPERTY_ENT_COL = ReferenceBuilder.forSubmodel(SUBMODEL, ENTITY, COLLECTION_ENT, PROPERTY_ENT_COL);
    public static final Reference REFERENCE_PROPERTY_ENT_LIST = ReferenceBuilder.with(REFERENCE_ENTITY_LIST).index(0, KeyTypes.PROPERTY).build();
    public static final Reference REFERENCE_PROPERTY_ENT_ENT = ReferenceBuilder.forSubmodel(SUBMODEL, ENTITY, ENTITY_ENT, PROPERTY_ENT_ENT);
    public static final Reference REFERENCE_PROPERTY_ENT_REL = ReferenceBuilder.forSubmodel(SUBMODEL, ENTITY, RELATIONSHIP_ENT, PROPERTY_ENT_REL);

    public static final Reference REFERENCE_PROPERTY_REL = ReferenceBuilder.forSubmodel(SUBMODEL, RELATIONSHIP, PROPERTY_REL);

    public static final List<Reference> PROPERTY_REFERENCES = List.of(
            REFERENCE_PROPERTY,
            REFERENCE_PROPERTY_COL,
            REFERENCE_PROPERTY_COL_COL,
            REFERENCE_PROPERTY_COL_LIST,
            REFERENCE_PROPERTY_COL_ENT,
            REFERENCE_PROPERTY_COL_REL,
            REFERENCE_PROPERTY_LIST,
            REFERENCE_PROPERTY_LIST_COL,
            REFERENCE_PROPERTY_LIST_LIST,
            REFERENCE_PROPERTY_LIST_ENT,
            REFERENCE_PROPERTY_LIST_REL,
            REFERENCE_PROPERTY_ENT,
            REFERENCE_PROPERTY_ENT_COL,
            REFERENCE_PROPERTY_ENT_LIST,
            REFERENCE_PROPERTY_ENT_ENT,
            REFERENCE_PROPERTY_ENT_REL,
            REFERENCE_PROPERTY_REL);

    public static final List<Reference> CONTAINER_REFERENCES = List.of(
            REFERENCE_SUBMODEL,
            REFERENCE_COLLECTION,
            REFERENCE_PROPERTY_COL,
            REFERENCE_COLLECTION_COL,
            REFERENCE_COLLECTION_LIST,
            REFERENCE_COLLECTION_ENT,
            REFERENCE_COLLECTION_REL,
            REFERENCE_LIST,
            REFERENCE_LIST_COL,
            REFERENCE_LIST_LIST,
            REFERENCE_LIST_ENT,
            REFERENCE_LIST_REL,
            REFERENCE_ENTITY,
            REFERENCE_ENTITY_COL,
            REFERENCE_ENTITY_LIST,
            REFERENCE_ENTITY_ENT,
            REFERENCE_ENTITY_REL,
            REFERENCE_RELATIONSHIP);

    // environment
    public static final Environment ENVIRONMENT = new DefaultEnvironment.Builder()
            .assetAdministrationShells(new DefaultAssetAdministrationShell.Builder()
                    .idShort("AAS1")
                    .id("https://example.org/aas/1")
                    .submodels(REFERENCE_SUBMODEL_SOURCE)
                    .submodels(REFERENCE_SUBMODEL)
                    .build())
            .submodels(SUBMODEL_SOURCE)
            .submodels(SUBMODEL)
            .build();

    private AssetConnectionModelRecursive() {}

}
