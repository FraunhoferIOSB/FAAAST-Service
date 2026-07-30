/*
 * Copyright 2026 Fraunhofer IOSB.
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

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonFieldNames;
import de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DigitalProductPassport;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.io.File;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultFile;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;


public class DppExamples {
    private static final String RESOURCE_PATH = "src/test/resources/dpp";

    public static final File DPP_1_COMPRESSED_FILE = new File(RESOURCE_PATH + "/dpp-1.json");

    public static final DigitalProductPassport DPP_1 = DigitalProductPassport.builder()
            .aas(new DefaultAssetAdministrationShell.Builder()
                    .id("http://example.org/dpp/1")
                    .idShort("testShell")
                    .assetInformation(new DefaultAssetInformation.Builder()
                            .assetKind(AssetKind.INSTANCE)
                            .globalAssetId("http://example.org/product/1")
                            .build())
                    .submodels(ReferenceBuilder.forSubmodel("http://example.org/dpp/1/metadata"))
                    .submodels(ReferenceBuilder.forSubmodel("http://example.org/dpp/1/pcf"))
                    .build())
            .metadata(new DefaultSubmodel.Builder()
                    .id("http://example.org/dpp/1/metadata")
                    .submodelElements(new DefaultProperty.Builder()
                            .idShort(JsonFieldNames.DPP_DPP_ID)
                            .value("http://example.org/dpp/1")
                            .valueType(DataTypeDefXsd.STRING)
                            .build())
                    .submodelElements(new DefaultProperty.Builder()
                            .idShort(JsonFieldNames.DPP_PRODUCT_ID)
                            .value("http://example.org/product/1")
                            .valueType(DataTypeDefXsd.STRING)
                            .build())
                    .submodelElements(new DefaultProperty.Builder()
                            .idShort(JsonFieldNames.DPP_GRANULARITY)
                            .value("Item")
                            .valueType(DataTypeDefXsd.STRING)
                            .build())
                    .submodelElements(new DefaultProperty.Builder()
                            .idShort(JsonFieldNames.DPP_SCHEMA_VERSION)
                            .value("ENXXX:v1.0")
                            .valueType(DataTypeDefXsd.STRING)
                            .build())
                    .submodelElements(new DefaultProperty.Builder()
                            .idShort(JsonFieldNames.DPP_STATUS)
                            .value("Active")
                            .valueType(DataTypeDefXsd.STRING)
                            .build())
                    .submodelElements(new DefaultProperty.Builder()
                            .idShort(JsonFieldNames.DPP_LAST_UPDATE)
                            .value("2025-08-22T03:12:00Z")
                            .valueType(DataTypeDefXsd.DATE_TIME)
                            .build())
                    .submodelElements(new DefaultProperty.Builder()
                            .idShort(JsonFieldNames.DPP_ECONOMIC_OPERATOR_ID)
                            .value("gxx:ppp456789")
                            .valueType(DataTypeDefXsd.STRING)
                            .build())
                    .submodelElements(new DefaultProperty.Builder()
                            .idShort(JsonFieldNames.DPP_FACILITY_ID)
                            .value("gxx:xxx987654")
                            .valueType(DataTypeDefXsd.STRING)
                            .build())
                    .submodelElements(new DefaultSubmodelElementList.Builder()
                            .idShort(JsonFieldNames.DPP_CONTENT_SPECIFICATION_IDS)
                            .value(new DefaultProperty.Builder()
                                    .value("https://admin-shell.io/idta/CarbonFootprint/CarbonFootprint/1/0")
                                    .valueType(DataTypeDefXsd.STRING)
                                    .build())
                            .value(new DefaultProperty.Builder()
                                    .value("http://example.org/smts/custom/1")
                                    .valueType(DataTypeDefXsd.STRING)
                                    .build())
                            .build())
                    .build())
            .content(new DefaultSubmodel.Builder()
                    .id("http://example.org/dpp/1/pcf")
                    .idShort("CarbonFootprint")
                    .semanticId(ReferenceBuilder.global("https://admin-shell.io/idta/CarbonFootprint/CarbonFootprint/1/0"))
                    .submodelElements(new DefaultSubmodelElementList.Builder()
                            .idShort("ProductCarbonFootprints")
                            .semanticId(ReferenceBuilder.global("https://admin-shell.io/idta/CarbonFootprint/ProductCarbonFootprints/1/0"))
                            .value(new DefaultSubmodelElementCollection.Builder()
                                    .semanticId(ReferenceBuilder.global("https://admin-shell.io/idta/CarbonFootprint/ProductCarbonFootprint/1/0"))
                                    .value(new DefaultSubmodelElementList.Builder()
                                            .idShort("PcfCalculationMethod")
                                            .semanticId(ReferenceBuilder.global("https://admin-shell.io/idta/CarbonFootprint/PcfCalculationMethods/1/0"))
                                            .value(new DefaultProperty.Builder()
                                                    .value("ISO 14067")
                                                    .valueType(DataTypeDefXsd.STRING)
                                                    .build())
                                            .build())
                                    .value(new DefaultProperty.Builder()
                                            .idShort("PcfCO2eq")
                                            .value("17.2")
                                            .valueType(DataTypeDefXsd.DECIMAL)
                                            .build())
                                    .value(new DefaultProperty.Builder()
                                            .idShort("ReferenceImpactUnitForCalculation")
                                            .value("piece")
                                            .valueType(DataTypeDefXsd.STRING)
                                            .build())
                                    .value(new DefaultProperty.Builder()
                                            .idShort("QuantityOfMeasureForCalculation")
                                            .value("5.0")
                                            .valueType(DataTypeDefXsd.DOUBLE)
                                            .build())
                                    .value(new DefaultSubmodelElementList.Builder()
                                            .idShort("LifeCyclePhases")
                                            .semanticId(ReferenceBuilder.global("https://admin-shell.io/idta/CarbonFootprint/LifeCyclePhases/1/0"))
                                            .value(new DefaultProperty.Builder()
                                                    .idShort("PublicationDate")
                                                    .value("2025-08-22T03:12:00Z")
                                                    .valueType(DataTypeDefXsd.DATE_TIME)
                                                    .build())
                                            .build())
                                    .value(new DefaultFile.Builder()
                                            .idShort("ExplanatoryStatement")
                                            .value("Statement.pdf")
                                            .contentType("application/pdf")
                                            .build())
                                    .value(new DefaultSubmodelElementCollection.Builder()
                                            .idShort("GoodsHandoverAddress")
                                            .build())
                                    .build())
                            .build())
                    .build())
            .content(new DefaultSubmodel.Builder()
                    .id("http://example.org/foo")
                    .idShort("CustomSMT")
                    .semanticId(ReferenceBuilder.global("http://example.org/smts/custom/1"))
                    .submodelElements(new DefaultMultiLanguageProperty.Builder()
                            .idShort("foo")
                            .value(new DefaultLangStringTextType.Builder()
                                    .language("en")
                                    .text("english text")
                                    .build())
                            .value(new DefaultLangStringTextType.Builder()
                                    .language("de")
                                    .text("deutscher text")
                                    .build())
                            .build())
                    .submodelElements(new DefaultFile.Builder()
                            .idShort("bar")
                            .value("SomeFile.pdf")
                            .contentType("application/pdf")
                            .build())
                    .build())
            .build();
}
