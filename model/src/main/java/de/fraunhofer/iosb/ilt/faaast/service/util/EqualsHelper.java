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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import org.json.JSONException;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;


/**
 * Helper class to compare objects that do not properly implement equals.
 */
public class EqualsHelper {

    private EqualsHelper() {}


    /**
     * Compares two {@link com.github.fge.jsonpatch.mergepatch.JsonMergePatch} instances by serializing them to Java.
     *
     * @param a instance a
     * @param b instance b
     * @return if both inputs are equal, otherwise false
     */
    public static boolean equals(JsonMergePatch a, JsonMergePatch b) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return JSONCompare.compareJSON(mapper.writeValueAsString(a), mapper.writeValueAsString(b), JSONCompareMode.STRICT).passed();
        }
        catch (JsonProcessingException | JSONException e) {
            return false;
        }
    }
}
