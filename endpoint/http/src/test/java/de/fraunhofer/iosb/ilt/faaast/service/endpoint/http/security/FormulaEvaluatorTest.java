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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.LogicalExpression;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;


/**
 * Unit tests for {@link FormulaEvaluator}.
 */
public class FormulaEvaluatorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /* ------------------------------------------------------------------ */
    @Test
    public void complexFormula_withMatchingClaims() throws Exception {
        String json = """
                {
                  "$and": [
                    {
                      "$or": [
                        {
                          "$eq": [
                            { "$field": "$sm#semanticId" },
                            { "$strVal": "SemanticID-Nameplate" }
                          ]
                        },
                        {
                          "$eq": [
                            { "$field": "$sm#semanticId" },
                            { "$strVal": "SemanticID-TechnicalData" }
                          ]
                        }
                      ]
                    },
                    {
                      "$or": [
                        {
                          "$eq": [
                            { "$attribute": { "CLAIM": "email" } },
                            { "$strVal": "user1@company1.com" }
                          ]
                        },
                        {
                          "$eq": [
                            { "$attribute": { "CLAIM": "email" } },
                            { "$strVal": "user2@company2.com" }
                          ]
                        }
                      ]
                    }
                  ]
                }
                """;

        LogicalExpression formula = MAPPER.readValue(
                json, new TypeReference<>() {});

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("$sm#semanticId", "SemanticID-TechnicalData"); // matches 2nd $eq
        ctx.put("CLAIM:email", "user2@company2.com"); // matches 2nd $eq
        boolean result = FormulaEvaluator.evaluate(formula, ctx);
        assertTrue(result);
    }


    /* ------------------------------------------------------------------ */
    @Test
    public void regexFormula_withNonMatchingEmail() throws Exception {
        String json = """
                {
                  "$and": [
                    {
                      "$or": [
                        {
                          "$eq": [
                            { "$field": "$sm#semanticId" },
                            { "$strVal": "SemanticID-Nameplate" }
                          ]
                        },
                        {
                          "$eq": [
                            { "$field": "$sm#semanticId" },
                            { "$strVal": "SemanticID-TechnicalData" }
                          ]
                        }
                      ]
                    },
                    {
                      "$regex": [
                        { "$attribute": { "CLAIM": "email" } },
                        { "$strVal": "[\\\\w\\\\.]+'@company\\\\.com" }
                      ]
                    }
                  ]
                }
                """;

        LogicalExpression formula = MAPPER.readValue(
                json, new TypeReference<>() {});
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("$sm#semanticId", "SemanticID-TechnicalData");
        ctx.put("CLAIM:email", "other.user@other-company.org"); // does NOT match
        assertFalse(FormulaEvaluator.evaluate(formula, ctx));
    }


    /* ------------------------------------------------------------------ */
    @Test
    public void fullFormula_allConditionsMet() throws Exception {
        String json = """
                {
                  "$and": [
                    {
                      "$or": [
                        {
                          "$eq": [
                            { "$field": "$sm#semanticId" },
                            { "$strVal": "SemanticID-Nameplate" }
                          ]
                        },
                        {
                          "$eq": [
                            { "$field": "$sm#semanticId" },
                            { "$strVal": "SemanticID-TechnicalData" }
                          ]
                        }
                      ]
                    },
                    {
                      "$eq": [
                        { "$attribute": { "CLAIM": "companyName" } },
                        { "$strVal": "company1-name" }
                      ]
                    },
                    {
                      "$regex": [
                        { "$attribute": { "REFERENCE": "(Submodel)*#Id" } },
                        { "$strVal": "^https://company1.com/.*$" }
                      ]
                    },
                    {
                      "$ge": [
                        { "$attribute": { "GLOBAL": "UTCNOW" } },
                        { "$timeVal": "09:00" }
                      ]
                    },
                    {
                      "$le": [
                        { "$attribute": { "GLOBAL": "UTCNOW" } },
                        { "$timeVal": "17:00" }
                      ]
                    }
                  ]
                }
                """;

        LogicalExpression formula = MAPPER.readValue(
                json, new TypeReference<>() {});
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("$sm#semanticId", "SemanticID-TechnicalData");
        ctx.put("CLAIM:companyName", "company1-name");
        ctx.put("REF:(Submodel)*#Id", "https://company1.com/id-0815");
        ctx.put("UTCNOW", LocalTime.of(10, 30)); // between 09:00 and 17:00
        assertTrue(FormulaEvaluator.evaluate(formula, ctx));
    }


    @Test
    public void testFormula_ConditionsNotMet() throws JsonProcessingException {
        String json = """
                {
                                    "$and": [
                                        {
                                            "$or": [
                                                {
                                                    "$eq": [
                                                        {
                                                            "$attribute": {
                                                                "CLAIM": "organization"
                                                            }
                                                        },
                                                        {
                                                            "$strVal": "[MyCompany]"
                                                        }
                                                    ]
                                                },
                                                {
                                                    "$eq": [
                                                        {
                                                            "$attribute": {
                                                                "CLAIM": "organization"
                                                            }
                                                        },
                                                        {
                                                            "$strVal": "Company2"
                                                        }
                                                    ]
                                                }
                                            ]
                                        },
                                        {
                                            "$or": [
                                                {
                                                    "$eq": [
                                                        {
                                                            "$attribute": {
                                                                "CLAIM": "email"
                                                            }
                                                        },
                                                        {
                                                            "$strVal": "bob@example.com"
                                                        }
                                                    ]
                                                },
                                                {
                                                    "$eq": [
                                                        {
                                                            "$attribute": {
                                                                "CLAIM": "email"
                                                            }
                                                        },
                                                        {
                                                            "$strVal": "user2@company2.com"
                                                        }
                                                    ]
                                                }
                                            ]
                                        }
                                    ]
                                }
                """;
        LogicalExpression formula = MAPPER.readValue(
                json, new TypeReference<>() {});
        Map<String, Object> ctx = new HashMap<>();
        ctx.put("CLAIM:organization", "Company2");
        //ctx.put("CLAIM:email", "user2@company2.com");
        assertFalse(FormulaEvaluator.evaluate(formula, ctx));
    }

}
