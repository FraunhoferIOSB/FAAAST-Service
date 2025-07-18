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
package de.fraunhofer.iosb.ilt.faaast.service.model.security.json;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.HashMap;
import java.util.Map;


/**
 * Represents a flexible condition structure that can contain
 * any JSON operators (e.g., "$or", "$match", "$eq", etc.).
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public class Condition {

    // A catch-all map that stores any fields Jackson encounters
    // (e.g., "$or", "$match", "$eq", "$regex", etc.)
    private Map<String, Object> expression = new HashMap<>();

    /**
     * Set the expression.
     *
     * @param key key like eq
     * @param value value of claim
     */
    @JsonAnySetter
    public void setExpression(String key, Object value) {
        expression.put(key, value);
    }


    /**
     * Get the expression.
     *
     * @return expression
     */
    public Map<String, Object> getExpression() {
        return expression;
    }


    /**
     * Set the expression.
     *
     * @param expression full expression
     */
    public void setExpression(Map<String, Object> expression) {
        this.expression = expression;
    }
}
