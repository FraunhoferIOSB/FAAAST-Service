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
package de.fraunhofer.iosb.ilt.faaast.service.model;

import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import org.eclipse.digitaltwin.aas4j.v3.model.*;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.*;


public class AASDeep {
    private static final String SUBMODEL1_ID = "https://acplt.org/Test_Submodel_Deep";

    private static AssetAdministrationShell createAAS1() {
        return new DefaultAssetAdministrationShell.Builder()
                .idShort("TestAssetAdministrationShell")
                .id("https://acplt.org/Test_AssetAdministrationShell")
                .administration(new DefaultAdministrativeInformation.Builder()
                        .version("0")
                        .revision("9")
                        .build())
                .assetInformation(new DefaultAssetInformation.Builder()
                        .assetKind(AssetKind.INSTANCE)
                        .specificAssetIds(new DefaultSpecificAssetID.Builder()
                                .name("Test Asset")
                                .value("https://acplt.org/Test_Asset")
                                .build()
                        )
                        .build())
                .submodels(new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.SUBMODEL)
                                .value(SUBMODEL1_ID)
                                .build())
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .build())
                .build();
    }

    private static Submodel createSubmodel1() {
        return new DefaultSubmodel.Builder()
                .idShort("TestSubmodel1")
                .id(SUBMODEL1_ID)
                .semanticID(new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("http://acplt.org/SubmodelTemplates/ExampleSubmodel")
                                .build())
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .build())
                .submodelElements(new DefaultProperty.Builder()
                        .idShort("DecimalProperty1")
                        .category("PARAMETER")
                        .value("123456")
                        .valueType(DataTypeDefXSD.DECIMAL)
                        .build())
                .submodelElements(new DefaultSubmodelElementList.Builder()
                        .idShort("ExampleSubmodelElementListOrdered")
                        .category("PARAMETER")
                        .orderRelevant(true)
                        .typeValueListElement(AASSubmodelElements.SUBMODEL_ELEMENT)
                        .semanticID(new DefaultReference.Builder()
                                .keys(new DefaultKey.Builder()
                                        .type(KeyTypes.GLOBAL_REFERENCE)
                                        .value("http://acplt.org/SubmodelElementLists/ExampleSubmodelElementListOrdered")
                                        .build())
                                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                .build())
                        .value(new DefaultProperty.Builder()
                                .idShort("DecimalProperty2")
                                .category("PARAMETER")
                                .value("123456")
                                .valueType(DataTypeDefXSD.DECIMAL)
                                .build())
                        .value(new DefaultSubmodelElementList.Builder()
                                .idShort("ExampleSubmodelElementListNested")
                                .category("PARAMETER")
                                .typeValueListElement(AASSubmodelElements.SUBMODEL_ELEMENT)
                                .semanticID(new DefaultReference.Builder()
                                        .keys(new DefaultKey.Builder()
                                                .type(KeyTypes.GLOBAL_REFERENCE)
                                                .value("http://acplt.org/SubmodelElementLists/ExampleSubmodelElementListOrdered")
                                                .build())
                                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                                        .build())
                                .value(new DefaultProperty.Builder()
                                        .idShort("DecimalProperty3")
                                        .category("PARAMETER")
                                        .value("123456")
                                        .valueType(DataTypeDefXSD.DECIMAL)
                                        .build())
                                .value(new DefaultProperty.Builder()
                                        .idShort("DecimalProperty4")
                                        .category("PARAMETER")
                                        .value("123456")
                                        .valueType(DataTypeDefXSD.DECIMAL)
                                        .build())
                                .build())
                        .build())
                .build();
    }


    public static Environment createEnvironment() {
        return new DefaultEnvironment.Builder()
                .assetAdministrationShells(createAAS1())
                .submodels(createSubmodel1())
                .build();
    }
}
