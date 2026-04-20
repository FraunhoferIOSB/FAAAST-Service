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
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;


public class AssetConnectionModelSimple {
    public static final String INITIAL_VALUE = "initial value";
    public static final Property PROPERTY_SOURCE_1 = new DefaultProperty.Builder()
            .idShort("source1")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final Property PROPERTY_SOURCE_2 = new DefaultProperty.Builder()
            .idShort("source2")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final Property PROPERTY_SOURCE_3 = new DefaultProperty.Builder()
            .idShort("source3")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final Property PROPERTY_SOURCE_4 = new DefaultProperty.Builder()
            .idShort("source4")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final Submodel SUBMODEL_SOURCE = new DefaultSubmodel.Builder()
            .idShort("SubmodelSource")
            .id("http://example.org/submodel/source")
            .submodelElements(PROPERTY_SOURCE_1)
            .submodelElements(PROPERTY_SOURCE_2)
            .submodelElements(PROPERTY_SOURCE_3)
            .submodelElements(PROPERTY_SOURCE_4)
            .build();
    public static final Property PROPERTY_1 = new DefaultProperty.Builder()
            .idShort("property1")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final Property PROPERTY_2 = new DefaultProperty.Builder()
            .idShort("property2")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final Property PROPERTY_3 = new DefaultProperty.Builder()
            .idShort("property3")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final Property PROPERTY_4 = new DefaultProperty.Builder()
            .idShort("property4")
            .value(INITIAL_VALUE)
            .valueType(DataTypeDefXsd.STRING)
            .build();
    public static final Submodel SUBMODEL = new DefaultSubmodel.Builder()
            .idShort("submodel")
            .id("http://example.org/submodel")
            .submodelElements(PROPERTY_1)
            .submodelElements(PROPERTY_2)
            .submodelElements(PROPERTY_3)
            .submodelElements(PROPERTY_4)
            .build();
    public static final Reference REFERENCE_SUBMODEL_SOURCE = ReferenceBuilder.forSubmodel(SUBMODEL_SOURCE);
    public static final Reference REFERENCE_SUBMODEL = ReferenceBuilder.forSubmodel(SUBMODEL);
    public static final Reference REFERENCE_PROPERTY_SOURCE_1 = ReferenceBuilder.forSubmodel(SUBMODEL_SOURCE, PROPERTY_SOURCE_1);
    public static final Reference REFERENCE_PROPERTY_SOURCE_2 = ReferenceBuilder.forSubmodel(SUBMODEL_SOURCE, PROPERTY_SOURCE_2);
    public static final Reference REFERENCE_PROPERTY_SOURCE_3 = ReferenceBuilder.forSubmodel(SUBMODEL_SOURCE, PROPERTY_SOURCE_3);
    public static final Reference REFERENCE_PROPERTY_SOURCE_4 = ReferenceBuilder.forSubmodel(SUBMODEL_SOURCE, PROPERTY_SOURCE_4);
    public static final Reference REFERENCE_PROPERTY_1 = ReferenceBuilder.forSubmodel(SUBMODEL, PROPERTY_1);
    public static final Reference REFERENCE_PROPERTY_2 = ReferenceBuilder.forSubmodel(SUBMODEL, PROPERTY_2);
    public static final Reference REFERENCE_PROPERTY_3 = ReferenceBuilder.forSubmodel(SUBMODEL, PROPERTY_3);
    public static final Reference REFERENCE_PROPERTY_4 = ReferenceBuilder.forSubmodel(SUBMODEL, PROPERTY_4);
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

    // nodeIds   
    public static final String NODE_ID_SOURCE_1 = "ns=3;s=1.Value";
    public static final String NODE_ID_SOURCE_2 = "ns=3;s=2.Value";
    public static final String NODE_ID_SOURCE_3 = "ns=3;s=3.Value";
    public static final String NODE_ID_SOURCE_4 = "ns=3;s=4.Value";

    private AssetConnectionModelSimple() {}

}
