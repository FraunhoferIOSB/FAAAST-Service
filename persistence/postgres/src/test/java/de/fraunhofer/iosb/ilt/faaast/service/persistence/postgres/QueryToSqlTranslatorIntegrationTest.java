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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.postgres;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASSimple;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Query;
import de.fraunhofer.iosb.ilt.faaast.service.query.QueryEvaluator;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultExtension;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultMultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;


public class QueryToSqlTranslatorIntegrationTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static EmbeddedPostgres embeddedPostgres;

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        embeddedPostgres = EmbeddedPostgres.start();
        embeddedPostgres.getJdbcUrl("postgres", "postgres");

        try (Connection conn = embeddedPostgres.getPostgresDatabase().getConnection()) {
            DatabaseSchema.createTables(conn);
        }
    }


    @AfterClass
    public static void tearDownDatabase() throws Exception {
        if (embeddedPostgres != null) {
            embeddedPostgres.close();
        }
    }


    private Connection getConnection() throws SQLException {
        return embeddedPostgres.getPostgresDatabase().getConnection();
    }


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
        Property classProperty = new DefaultProperty.Builder()
                .idShort("Class")
                .value(matching ? "03-01" : "NonMatching")
                .valueType(DataTypeDefXsd.STRING)
                .build();

        MultiLanguageProperty smlLanguages = new DefaultMultiLanguageProperty.Builder()
                .idShort("SMLLanguages")
                .value(new DefaultLangStringTextType.Builder()
                        .language("nl")
                        .text("Dutch text")
                        .build())
                .build();

        List<SubmodelElement> documentsItems = new ArrayList<>();
        documentsItems.add(classProperty);
        documentsItems.add(smlLanguages);

        SubmodelElementList documentsList = new DefaultSubmodelElementList.Builder()
                .idShort("Documents")
                .value(documentsItems)
                .build();

        Submodel submodel = new DefaultSubmodel.Builder()
                .id("https://example.com/submodel/2")
                .idShort("TestSubmodel")
                .submodelElements(documentsList)
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
                .value(matching ? "050" : "150")
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


    private void insertEnvironment(Environment env) throws SQLException, JsonProcessingException {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            if (env.getAssetAdministrationShells() != null) {
                for (AssetAdministrationShell aas: env.getAssetAdministrationShells()) {
                    String json = MAPPER.writeValueAsString(aas);
                    stmt.execute("INSERT INTO " + DatabaseSchema.TABLE_AAS + " (id, id_short, content) VALUES ('" +
                            aas.getId() + "', '" + (aas.getIdShort() != null ? aas.getIdShort() : "") + "', '" +
                            json.replace("'", "''") + "') ON CONFLICT (id) DO UPDATE SET content = EXCLUDED.content");
                }
            }
            if (env.getSubmodels() != null) {
                for (Submodel sm: env.getSubmodels()) {
                    String json = MAPPER.writeValueAsString(sm);
                    String semanticId = "";
                    if (sm.getSemanticId() != null && sm.getSemanticId().getKeys() != null && !sm.getSemanticId().getKeys().isEmpty()) {
                        semanticId = sm.getSemanticId().getKeys().get(0).getValue();
                    }
                    stmt.execute("INSERT INTO " + DatabaseSchema.TABLE_SUBMODEL + " (id, id_short, semantic_id, content) VALUES ('" +
                            sm.getId() + "', '" + (sm.getIdShort() != null ? sm.getIdShort() : "") + "', '" + semanticId + "', '" +
                            json.replace("'", "''") + "') ON CONFLICT (id) DO UPDATE SET content = EXCLUDED.content");
                }
            }
        }
    }


    private void clearDatabase() throws SQLException {
        try (Connection conn = getConnection();
                Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM " + DatabaseSchema.TABLE_SUBMODEL);
            stmt.execute("DELETE FROM " + DatabaseSchema.TABLE_AAS);
        }
    }


    private boolean executeSqlQuery(String sql) throws SQLException {
        System.out.println("Generated SQL: " + sql);
        try (Connection conn = getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            return rs.next();
        }
    }


    @Test
    public void simpleEq_withMatchingFields() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$eq": [
                      { "$field": "$aas#idShort" },
                      { "$strVal": "TestType" }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        Environment env = createTestEnvironmentForSimpleEq(true);

        clearDatabase();
        insertEnvironment(env);

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_AAS);

        assertFalse(result.isEmpty());
        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_AAS + " WHERE " + result.getSql()));

        QueryEvaluator evaluator = new QueryEvaluator();
        AssetAdministrationShell aas = env.getAssetAdministrationShells().get(0);
        Assert.assertTrue(evaluator.matches(query.get$condition(), aas));
    }


    @Test
    public void simpleEq_withNonMatchingFields() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$eq": [
                      { "$field": "$aas#idShort" },
                      { "$strVal": "NonMatching" }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        Environment env = createTestEnvironmentForSimpleEq(false);

        clearDatabase();
        insertEnvironment(env);

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_AAS);

        assertFalse(result.isEmpty());
        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_AAS + " WHERE " + result.getSql()));

        QueryEvaluator evaluator = new QueryEvaluator();
        AssetAdministrationShell aas = env.getAssetAdministrationShells().get(0);
        Assert.assertTrue(evaluator.matches(query.get$condition(), aas));
    }


    @Test
    public void documentsMatch_withMatchingValues() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$match": [
                      { "$eq": [
                          { "$field": "$sme.Documents[].Class#value" },
                          { "$strVal": "03-01" }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        Environment env = createTestEnvironmentForDocumentsMatch(true);

        clearDatabase();
        insertEnvironment(env);

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT content FROM " + DatabaseSchema.TABLE_SUBMODEL);
        }

        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE " + result.getSql()));

        QueryEvaluator evaluator = new QueryEvaluator();
        Submodel submodel = env.getSubmodels().get(0);
        Assert.assertTrue(evaluator.matches(query.get$condition(), submodel));
    }


    @Test
    public void documentsMatch_withNonMatchingValues() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$match": [
                      { "$eq": [
                          { "$field": "$sme.Documents[].Class#value" },
                          { "$strVal": "NonMatching" }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        Environment env = createTestEnvironmentForDocumentsMatch(false);

        clearDatabase();
        insertEnvironment(env);

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());
        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE " + result.getSql()));

        QueryEvaluator evaluator = new QueryEvaluator();
        Submodel submodel = env.getSubmodels().get(0);
        Assert.assertTrue(evaluator.matches(query.get$condition(), submodel));
    }


    @Test
    public void andMatch_onlySmIdShort() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$and": [
                      { "$eq": [
                          { "$field": "$sm#idShort" },
                          { "$strVal": "TechnicalData" }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        Environment env = createTestEnvironmentForAndMatch(true);

        clearDatabase();
        insertEnvironment(env);

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());
        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE " + result.getSql()));
    }


    @Test
    public void andMatch_onlyProductClassifications() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$and": [
                      { "$eq": [
                          { "$field": "$sme.ProductClassifications[].ProductClassId#value" },
                          { "$strVal": "27-37-09-05" }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        Environment env = createTestEnvironmentForAndMatch(true);

        clearDatabase();
        insertEnvironment(env);

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());
        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE " + result.getSql()));
    }


    @Test
    public void andMatch_onlySmeSemanticIdAndValue() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$and": [
                      { "$eq": [
                          { "$field": "$sme#semanticId" },
                          { "$strVal": "0173-1#02-BAF016#006" }
                        ]
                      },
                      { "$lt": [
                          { "$field": "$sme#value" },
                          { "$numVal": 100 }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        Environment env = createTestEnvironmentForAndMatch(true);

        clearDatabase();
        insertEnvironment(env);

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());
        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE " + result.getSql()));
    }


    @Test
    public void andMatch_smIdShortAndProductClassifications() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$and": [
                      { "$eq": [
                          { "$field": "$sm#idShort" },
                          { "$strVal": "TechnicalData" }
                        ]
                      },
                      { "$eq": [
                          { "$field": "$sme.ProductClassifications[].ProductClassId#value" },
                          { "$strVal": "27-37-09-05" }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        Environment env = createTestEnvironmentForAndMatch(true);

        clearDatabase();
        insertEnvironment(env);

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());
        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE " + result.getSql()));
    }


    @Test
    public void andMatch_smIdShortAndSmeConditions() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$and": [
                      { "$eq": [
                          { "$field": "$sm#idShort" },
                          { "$strVal": "TechnicalData" }
                        ]
                      },
                      { "$eq": [
                          { "$field": "$sme#semanticId" },
                          { "$strVal": "0173-1#02-BAF016#006" }
                        ]
                      },
                      { "$lt": [
                          { "$field": "$sme#value" },
                          { "$numVal": 100 }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        Environment env = createTestEnvironmentForAndMatch(true);

        clearDatabase();
        insertEnvironment(env);

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());
        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE " + result.getSql()));
    }


    @Test
    public void andMatch_withMatchingConditions() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$and": [
                      { "$eq": [
                          { "$field": "$sm#idShort" },
                          { "$strVal": "TechnicalData" }
                        ]
                      },
                      { "$eq": [
                          { "$field": "$sme.ProductClassifications[].ProductClassId#value" },
                          { "$strVal": "27-37-09-05" }
                        ]
                      },
                      { "$match": [
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

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        Environment env = createTestEnvironmentForAndMatch(true);

        clearDatabase();
        insertEnvironment(env);

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());
        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE " + result.getSql()));
    }


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

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        Environment env = createTestEnvironmentForOrMatch(true);

        clearDatabase();
        insertEnvironment(env);

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_AAS);

        assertFalse(result.isEmpty());
        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_AAS + " WHERE " + result.getSql()));

        QueryEvaluator evaluator = new QueryEvaluator();
        AssetAdministrationShell aas = env.getAssetAdministrationShells().get(0);
        Assert.assertTrue(evaluator.matches(query.get$condition(), aas));
    }


    @Test
    public void submodelSemanticIdQuery() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$eq": [
                      { "$field": "$sm#semanticId" },
                      { "$strVal": "https://example.com/semanticId" }
                    ]
                  }
                }
                """;

        Environment env = AASSimple.createEnvironment();

        clearDatabase();
        insertEnvironment(env);

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());
    }


    @Test
    public void regexQuery() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$regex": [
                      { "$field": "$sm#idShort" },
                      { "$strVal": ".*" }
                    ]
                  }
                }
                """;

        Environment env = AASSimple.createEnvironment();

        clearDatabase();
        insertEnvironment(env);

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());
        assertTrue(result.getSql().contains("~"));
    }


    @Test
    public void containsQuery() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$contains": [
                      { "$field": "$sm#idShort" },
                      { "$strVal": "Test" }
                    ]
                  }
                }
                """;

        Environment env = AASSimple.createEnvironment();

        clearDatabase();
        insertEnvironment(env);

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());
    }


    @Test
    public void comparisonQuery() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$lt": [
                      { "$field": "$sme#value" },
                      { "$numVal": 100 }
                    ]
                  }
                }
                """;

        Environment env = AASFull.createEnvironment();

        clearDatabase();
        insertEnvironment(env);

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());
        assertTrue(result.getSql().contains("<"));
    }


    @Test
    public void nullConditionReturnsTrue() throws Exception {
        clearDatabase();
        insertEnvironment(AASSimple.createEnvironment());

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(null, DatabaseSchema.TABLE_SUBMODEL);

        assertTrue(result.isEmpty());
        assertTrue(result.getSql().equals("TRUE"));
        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE " + result.getSql()));
    }


    private Environment createTestEnvironmentForExtensions() {
        Extension versionExt = new DefaultExtension.Builder()
                .name("Version")
                .value("1.0.0")
                .valueType(DataTypeDefXsd.STRING)
                .build();

        Extension dateExt = new DefaultExtension.Builder()
                .name("Date")
                .value("2024-01-15")
                .valueType(DataTypeDefXsd.STRING)
                .build();

        Submodel submodel = new DefaultSubmodel.Builder()
                .id("https://example.com/submodel/extensions")
                .idShort("TestSubmodel")
                .extensions(Arrays.asList(versionExt, dateExt))
                .build();

        AssetAdministrationShell aas = new DefaultAssetAdministrationShell.Builder()
                .id("https://example.com/aas/extensions")
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


    @Test
    public void nestedMatches_withMultipleConditions() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$and": [
                      { "$eq": [
                          { "$field": "$sm#idShort" },
                          { "$strVal": "TechnicalData" }
                        ]
                      },
                      { "$eq": [
                          { "$field": "$sme#idShort" },
                          { "$strVal": "ProductClassifications" }
                        ]
                      },
                      { "$eq": [
                          { "$field": "$sme.ProductClassifications[].ProductClassId#value" },
                          { "$strVal": "27-37-09-05" }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        Environment env = createTestEnvironmentForAndMatch(true);

        clearDatabase();
        insertEnvironment(env);

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());
        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE " + result.getSql()));
    }


    @Test
    public void nestedMatches_withSimpleSmeCondition() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$and": [
                      { "$eq": [
                          { "$field": "$sme#idShort" },
                          { "$strVal": "ProductClassId" }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        Environment env = createTestEnvironmentForAndMatch(true);

        clearDatabase();
        insertEnvironment(env);

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());
        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE " + result.getSql()));
    }


    @Test
    public void nestedMatches_withSmAndSmeConditions() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$and": [
                      { "$eq": [
                          { "$field": "$sm#idShort" },
                          { "$strVal": "TechnicalData" }
                        ]
                      },
                      { "$eq": [
                          { "$field": "$sme#idShort" },
                          { "$strVal": "ProductClassifications" }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        Environment env = createTestEnvironmentForAndMatch(true);

        clearDatabase();
        insertEnvironment(env);

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());
        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE " + result.getSql()));
    }


    @Test
    public void matchInArray_withExtensions() throws Exception {
        String json = """
                {
                  "$condition": {
                    "$match": [
                      { "$eq": [
                          { "$field": "$sm#extensions[].name" },
                          { "$strVal": "Version" }
                        ]
                      }
                    ]
                  }
                }
                """;

        Query query = MAPPER.readValue(json, new TypeReference<>() {});
        Environment env = createTestEnvironmentForExtensions();

        clearDatabase();
        insertEnvironment(env);

        QueryToSqlTranslator translator = new QueryToSqlTranslator();
        QueryToSqlTranslator.TranslationResult result = translator.translate(query.get$condition(), DatabaseSchema.TABLE_SUBMODEL);

        assertFalse(result.isEmpty());
        assertTrue(executeSqlQuery("SELECT * FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE " + result.getSql()));
    }
}
