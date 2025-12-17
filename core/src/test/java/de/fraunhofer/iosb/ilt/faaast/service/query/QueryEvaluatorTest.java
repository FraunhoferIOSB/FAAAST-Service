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
package de.fraunhofer.iosb.ilt.faaast.service.query;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Query;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
import org.junit.Test;


/**
 * Unit tests for {@link QueryEvaluator}.
 */
public class QueryEvaluatorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Environment createTestEnvironmentForSimpleEq(boolean matching) {
        Submodel submodel = new DefaultSubmodel.Builder()
                .id("https://example.com/submodel/1")
                .idShort("TestSubmodel")
                .build();

        AssetAdministrationShell aas = new DefaultAssetAdministrationShell.Builder()
                .id("https://example.com/aas/1")
                .idShort(matching ? "TestType" : "NonMatching")
                .assetInformation(new DefaultAssetInformation.Builder()
                        .assetKind(AssetKind.INSTANCE)
                        .assetType("TestType")
                        .build())
                .build();

        return new DefaultEnvironment.Builder()
                .assetAdministrationShells(aas)
                .submodels(submodel)
                .build();
    }


    private Environment createTestEnvironmentForDocumentsMatch(boolean matching) {
        List<SubmodelElement> documentsItems = new ArrayList<>();
        SubmodelElementCollection docItem = new DefaultSubmodelElementCollection.Builder()
                .idShort("Doc1")
                .value(new DefaultSubmodelElementCollection.Builder()
                        .idShort("DocumentClassification")
                        .value(new DefaultProperty.Builder()
                                .idShort("Class")
                                .value(matching ? "03-01" : "NonMatching")
                                .valueType(DataTypeDefXsd.STRING)
                                .build())
                        .build())
                .value(new DefaultSubmodelElementCollection.Builder()
                        .idShort("DocumentVersion")
                        .value(new DefaultMultiLanguageProperty.Builder()
                                .idShort("SMLLanguages")
                                .value(new DefaultLangStringTextType.Builder()
                                        .language("nl")
                                        .text("Dutch text")
                                        .build())
                                .build())
                        .build())
                .build();
        documentsItems.add(docItem);

        SubmodelElementList documents = new DefaultSubmodelElementList.Builder()
                .idShort("Documents")
                .value(documentsItems)
                .build();

        Submodel submodel = new DefaultSubmodel.Builder()
                .id("https://example.com/submodel/2")
                .idShort("TestSubmodel")
                .submodelElements(documents)
                .build();

        AssetAdministrationShell aas = new DefaultAssetAdministrationShell.Builder()
                .id("https://example.com/aas/2")
                .idShort("TestAAS")
                .assetInformation(new DefaultAssetInformation.Builder()
                        .assetKind(AssetKind.INSTANCE)
                        .build())
                .build();

        return new DefaultEnvironment.Builder()
                .assetAdministrationShells(aas)
                .submodels(submodel)
                .build();
    }


    private Environment createTestEnvironmentForAndMatch(boolean matching) {
        List<SubmodelElement> productClassItems = new ArrayList<>();
        Property productClassId = new DefaultProperty.Builder()
                .idShort("ProductClassId")
                .value(matching ? "27-37-09-05" : "NonMatching")
                .valueType(DataTypeDefXsd.STRING)
                .build();
        productClassItems.add(productClassId);

        SubmodelElementList productClassifications = new DefaultSubmodelElementList.Builder()
                .idShort("ProductClassifications")
                .value(productClassItems)
                .build();

        Property someProperty = new DefaultProperty.Builder()
                .idShort("SomeProperty")
                .semanticId(new DefaultReference.Builder()
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.GLOBAL_REFERENCE)
                                .value("0173-1#02-BAF016#006")
                                .build())
                        .build())
                .value(matching ? "50" : "150") // For < 100
                .valueType(DataTypeDefXsd.INT)
                .build();

        Submodel submodel = new DefaultSubmodel.Builder()
                .id("https://example.com/submodel/3")
                .idShort("TechnicalData")
                .submodelElements(productClassifications)
                .submodelElements(someProperty)
                .build();

        AssetAdministrationShell aas = new DefaultAssetAdministrationShell.Builder()
                .id("https://example.com/aas/3")
                .idShort("TestAAS")
                .assetInformation(new DefaultAssetInformation.Builder()
                        .assetKind(AssetKind.INSTANCE)
                        .build())
                .build();

        return new DefaultEnvironment.Builder()
                .assetAdministrationShells(aas)
                .submodels(submodel)
                .build();
    }


    private Environment createTestEnvironmentForOrMatch(boolean matching) {
        List<SpecificAssetId> specificAssetIds = new ArrayList<>();
        specificAssetIds.add(new DefaultSpecificAssetId.Builder()
                .name("supplierId")
                .value(matching ? "aas-1" : "NonMatching")
                .build());
        specificAssetIds.add(new DefaultSpecificAssetId.Builder()
                .name("customerId")
                .value(matching ? "aas-2" : "NonMatching")
                .build());

        AssetAdministrationShell aas = new DefaultAssetAdministrationShell.Builder()
                .id("https://example.com/aas/4")
                .idShort("TestAAS")
                .assetInformation(new DefaultAssetInformation.Builder()
                        .assetKind(AssetKind.INSTANCE)
                        .specificAssetIds(specificAssetIds)
                        .build())
                .build();

        Submodel submodel = new DefaultSubmodel.Builder()
                .id("https://example.com/submodel/4")
                .idShort("TestSubmodel")
                .build();

        return new DefaultEnvironment.Builder()
                .assetAdministrationShells(aas)
                .submodels(submodel)
                .build();
    }


    /* ------------------------------------------------------------------ */
    @Test
    public void simpleEq_withMatchingFields() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$eq": [
                      { "$field": "$aas#idShort" },
                      {
                        "$field":
                            "$aas#assetInformation.assetType"
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(
                json, new TypeReference<>() {});

        Environment env = createTestEnvironmentForSimpleEq(true);
        QueryEvaluator evaluator = new QueryEvaluator();
        AssetAdministrationShell aas = env.getAssetAdministrationShells().get(0);
        boolean result = evaluator.matches(query.get$condition(), aas);
        assertTrue(result);
    }


    /* ------------------------------------------------------------------ */
    @Test
    public void simpleEq_withNonMatchingFields() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$eq": [
                      { "$field": "$aas#idShort" },
                      {
                        "$field":
                            "$aas#assetInformation.assetType"
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(
                json, new TypeReference<>() {});

        Environment env = createTestEnvironmentForSimpleEq(false);
        QueryEvaluator evaluator = new QueryEvaluator();
        AssetAdministrationShell aas = env.getAssetAdministrationShells().get(0);
        boolean result = evaluator.matches(query.get$condition(), aas);
        assertFalse(result);
    }


    /* ------------------------------------------------------------------ */
    @Test
    public void documentsMatch_withMatchingValues() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$match": [
                      { "$eq": [
                          { "$field": "$sme.Documents[].DocumentClassification.Class#value" },
                          { "$strVal": "03-01" }
                        ]
                      },
                      { "$eq": [
                          { "$field": "$sme.Documents[].DocumentVersion.SMLLanguages#language" },
                          { "$strVal": "nl" }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(
                json, new TypeReference<>() {});

        Environment env = createTestEnvironmentForDocumentsMatch(true);
        QueryEvaluator evaluator = new QueryEvaluator();
        Submodel submodel = env.getSubmodels().get(0);
        boolean result = evaluator.matches(query.get$condition(), submodel);
        assertTrue(result);
    }


    /* ------------------------------------------------------------------ */
    @Test
    public void documentsMatch_withNonMatchingValues() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$match": [
                      { "$eq": [
                          { "$field": "$sme.Documents[].DocumentClassification.Class#value" },
                          { "$strVal": "03-01" }
                        ]
                      },
                      { "$eq": [
                          { "$field": "$sme.Documents[].DocumentVersion.SMLLanguages#language" },
                          { "$strVal": "nl" }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(
                json, new TypeReference<>() {});

        Environment env = createTestEnvironmentForDocumentsMatch(false);
        QueryEvaluator evaluator = new QueryEvaluator();
        Submodel submodel = env.getSubmodels().get(0);
        boolean result = evaluator.matches(query.get$condition(), submodel);
        assertFalse(result);
    }


    /* ------------------------------------------------------------------ */
    @Test
    public void andMatch_withMatchingConditions() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$and": [
                      { "$match": [
                          { "$eq": [
                              { "$field": "$sm#idShort" },
                              { "$strVal": "TechnicalData" }
                            ]
                          },
                          { "$eq": [
                              {
                                "$field":
                                "$sme.ProductClassifications[].ProductClassId#value"
                              },
                              { "$strVal": "27-37-09-05" }
                            ]
                          }
                        ]
                      },
                      { "$match": [
                          {
                            "$eq": [
                              { "$field": "$sm#idShort" },
                              { "$strVal": "TechnicalData" }
                            ]
                          },
                          {
                            "$eq": [
                              { "$field": "$sme#semanticId" },
                              { "$strVal": "0173-1#02-BAF016#006" }
                            ]
                          },
                          {
                            "$lt": [
                              { "$field": "$sme#value" },
                              { "$numVal": 100 }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(
                json, new TypeReference<>() {});

        Environment env = createTestEnvironmentForAndMatch(true);
        QueryEvaluator evaluator = new QueryEvaluator();
        Submodel submodel = env.getSubmodels().get(0);
        boolean result = evaluator.matches(query.get$condition(), submodel);
        assertTrue(result);
    }


    /* ------------------------------------------------------------------ */
    @Test
    public void andMatch_withNonMatchingConditions() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$and": [
                      { "$match": [
                          { "$eq": [
                              { "$field": "$sm#idShort" },
                              { "$strVal": "TechnicalData" }
                            ]
                          },
                          { "$eq": [
                              {
                                "$field":
                                "$sme.ProductClassifications[].ProductClassId#value"
                              },
                              { "$strVal": "27-37-09-05" }
                            ]
                          }
                        ]
                      },
                      { "$match": [
                          {
                            "$eq": [
                              { "$field": "$sm#idShort" },
                              { "$strVal": "TechnicalData" }
                            ]
                          },
                          {
                            "$eq": [
                              { "$field": "$sme#semanticId" },
                              { "$strVal": "0173-1#02-BAF016#006" }
                            ]
                          },
                          {
                            "$lt": [
                              { "$field": "$sme#value" },
                              { "$numVal": 100 }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(
                json, new TypeReference<>() {});

        Environment env = createTestEnvironmentForAndMatch(false);
        QueryEvaluator evaluator = new QueryEvaluator();
        Submodel submodel = env.getSubmodels().get(0);
        boolean result = evaluator.matches(query.get$condition(), submodel);
        assertFalse(result);
    }


    /* ------------------------------------------------------------------ */
    @Test
    public void orMatch_withMatchingSpecificAssetIds() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$or": [
                      {
                        "$match": [
                          { "$eq": [
                              { "$field": "$aas#assetInformation.specificAssetIds[].name" },
                              { "$strVal": "supplierId" }
                            ]
                          },
                          { "$eq": [
                              { "$field": "$aas#assetInformation.specificAssetIds[].value" },
                              { "$strVal": "aas-1" }
                            ]
                          }
                        ]
                      },
                      {
                        "$match": [
                          {
                            "$eq": [
                              { "$field": "$aas#assetInformation.specificAssetIds[].name" },
                              { "$strVal": "customerId" }
                            ]
                          },
                          {
                            "$eq": [
                              { "$field": "$aas#assetInformation.specificAssetIds[].value" },
                              { "$strVal": "aas-2" }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(
                json, new TypeReference<>() {});

        Environment env = createTestEnvironmentForOrMatch(true);
        QueryEvaluator evaluator = new QueryEvaluator();
        AssetAdministrationShell aas = env.getAssetAdministrationShells().get(0);
        boolean result = evaluator.matches(query.get$condition(), aas);
        assertTrue(result);
    }


    /* ------------------------------------------------------------------ */
    @Test
    public void orMatch_withNonMatchingSpecificAssetIds() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$or": [
                      {
                        "$match": [
                          { "$eq": [
                              { "$field": "$aas#assetInformation.specificAssetIds[].name" },
                              { "$strVal": "supplierId" }
                            ]
                          },
                          { "$eq": [
                              { "$field": "$aas#assetInformation.specificAssetIds[].value" },
                              { "$strVal": "aas-1" }
                            ]
                          }
                        ]
                      },
                      {
                        "$match": [
                          {
                            "$eq": [
                              { "$field": "$aas#assetInformation.specificAssetIds[].name" },
                              { "$strVal": "customerId" }
                            ]
                          },
                          {
                            "$eq": [
                              { "$field": "$aas#assetInformation.specificAssetIds[].value" },
                              { "$strVal": "aas-2" }
                            ]
                          }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(
                json, new TypeReference<>() {});

        Environment env = createTestEnvironmentForOrMatch(false);
        QueryEvaluator evaluator = new QueryEvaluator();
        AssetAdministrationShell aas = env.getAssetAdministrationShells().get(0);
        boolean result = evaluator.matches(query.get$condition(), aas);
        assertFalse(result);
    }

}
