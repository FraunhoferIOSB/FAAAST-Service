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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.RandomStringUtils;


/**
 * Utility class helping with regular expressions.
 */
public class RegExHelper {

    private static final Set<String> ACTIVE_GROUP_NAMES = new HashSet<>();
    private static final String PATTERN_LINE_START = "^";
    private static final String PATTERN_LINE_END = "$";
    private static final Pattern PATTERN_NAMED_GROUP = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

    private RegExHelper() {}


    /**
     * Generate a random unique group name based on UUID without special characters.
     *
     * @return random unique group name.
     */
    public static String uniqueGroupName() {
        String result;
        do {
            result = RandomStringUtils.randomAlphabetic(10);
        } while (ACTIVE_GROUP_NAMES.contains(result));
        ACTIVE_GROUP_NAMES.add(result);
        return result;
    }


    /**
     * Checks if the pattern is doing an exact line match, i.e. starts with '^' and ends with '$'.
     *
     * @param pattern the pattern to check
     * @return true if pattern does exact line match, false otherwise
     */
    public static boolean hasLineMatch(String pattern) {
        return pattern.startsWith(PATTERN_LINE_START) && pattern.endsWith(PATTERN_LINE_END);
    }


    /**
     * Ensures that the pattern does start with '^' and end with '$'.
     *
     * @param pattern the pattern
     * @return the updated pattern
     */
    public static String ensureLineMatch(String pattern) {
        String result = pattern;
        if (!result.startsWith(PATTERN_LINE_START)) {
            result = PATTERN_LINE_START + result;
        }
        if (!result.endsWith(PATTERN_LINE_END)) {
            result += PATTERN_LINE_END;
        }
        return result;
    }


    /**
     * Removes exact line match, i.e. if the pattern starts with '^' and ends with '$' these are removed, otherwise
     * input is return unmodified.
     *
     * @param pattern the pattern to process
     * @return the modified pattern
     */
    public static String removeLineMatch(String pattern) {
        return hasLineMatch(pattern)
                ? pattern.substring(0, pattern.length() - 1)
                : pattern;
    }


    /**
     * Finds a named groups (in the form of {@literal '(?<[group name]>)')} in the pattern and extracts the
     * corresponding values from the input. If pattern does not contain named groups or input does not match pattern and
     * empty list is return.
     *
     * @param pattern the pattern to evaluate
     * @param input the value to evaludate the pattern against
     * @return group names and their corresponding value
     */
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


    /**
     * Removes named groups from a regular expression, e.g. the regex {@literal a+(?<mygroup>b+)c+} becomes
     * {@literal a+(b+)c+}
     *
     * @param pattern the input pattern
     * @return the processed pattern
     */
    public static String removeGroupNames(String pattern) {
        return pattern.replaceAll("(\\?<[^>]*>)", "");
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
