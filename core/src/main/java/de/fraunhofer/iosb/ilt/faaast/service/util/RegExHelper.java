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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegExHelper {

    private static final Pattern PATTERN_NAMED_GROUP = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

    private RegExHelper() {

    }


    public static Map<String, String> getGroupValues(String pattern, String input) {
        Map<String, String> result = new HashMap<>();
        Matcher matcher = Pattern.compile(pattern).matcher(input);
        if (matcher.matches()) {
            getNamedGroupCandidates(pattern).forEach(group -> {
                try {
                    result.put(group, matcher.group(group));
                }
                catch (IllegalArgumentException e) {
                    // ignore
                }
            });
        }
        return result;
    }


    private static Set<String> getNamedGroupCandidates(String pattern) {
        Set<String> result = new TreeSet<>();
        Matcher m = PATTERN_NAMED_GROUP.matcher(pattern);
        while (m.find()) {
            result.add(m.group(1));
        }
        return result;
    }
}
