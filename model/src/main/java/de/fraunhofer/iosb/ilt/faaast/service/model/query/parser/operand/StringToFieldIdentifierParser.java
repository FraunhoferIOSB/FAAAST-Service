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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.operand;

import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.AasFieldIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.ConceptDescriptionFieldIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.FieldIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.SubmodelElementFieldIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.SubmodelFieldIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.path.FieldPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.path.segment.FieldPathSegment;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.path.segment.IndexSegment;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.path.segment.PropertySegment;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.Parser;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Parses the {@code $field}/{@code FRAGMENT} string notation into a {@link FieldIdentifier}.
 *
 * <p>The notation has the form {@code <scope>[.<idShort>]*#<path>} where {@code path} is a dot-separated list of
 * property and index segments, e.g. {@code $sm#assetIds[0].value}.
 */
public class StringToFieldIdentifierParser implements Parser<String, FieldIdentifier> {

    private static final String ID_SHORT = "(?:[A-Za-z0-9_][A-Za-z0-9_.-]*)";
    private static final Pattern SCOPE_PATTERN = Pattern.compile(
            "^(\\$[a-z]+)(\\.[A-Za-z0-9_.-]+)?#(.+)$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PROPERTY_PATTERN = Pattern.compile(ID_SHORT);
    private static final Pattern INDEX_PATTERN = Pattern.compile("\\[(\\d*)\\]");

    /**
     * Parse a field path into its {@link FieldIdentifier}.
     *
     * @param value the field path string, e.g. {@code $sm#assetIds[0].value}
     * @return the corresponding field identifier attribute
     * @throws IllegalArgumentException if the value cannot be parsed or refers to an out-of-scope descriptor
     */
    @Override
    public FieldIdentifier parse(String value) {
        Matcher matcher = SCOPE_PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(String.format("Invalid field path: '%s'", value));
        }

        String scope = matcher.group(1).toLowerCase(Locale.ROOT);
        String idShort = matcher.group(2);
        String path = matcher.group(3);

        return createIdentifier(scope, idShort, parsePath(path));
    }


    private static FieldIdentifier createIdentifier(String scope, String idShort, List<FieldPathSegment> segments) {
        FieldPath fieldPath = new FieldPath(segments);
        return switch (scope) {
            case AasFieldIdentifier.NOTATION -> new AasFieldIdentifier(fieldPath);
            case SubmodelFieldIdentifier.NOTATION -> new SubmodelFieldIdentifier(fieldPath);
            case SubmodelElementFieldIdentifier.NOTATION -> new SubmodelElementFieldIdentifier(fieldPath, IdShortPath.parse(idShort));
            case ConceptDescriptionFieldIdentifier.NOTATION -> new ConceptDescriptionFieldIdentifier(fieldPath);
            default -> throw new UnsupportedOperationException(String.format("Unsupported field scope: '%s'", scope));
        };
    }


    private static List<FieldPathSegment> parsePath(String path) {
        List<FieldPathSegment> segments = new ArrayList<>();
        int position = 0;
        while (position < path.length()) {
            Matcher property = PROPERTY_PATTERN.matcher(path);
            if (property.find(position) && property.start() == position) {
                segments.add(new PropertySegment(property.group()));
                position = property.end();
            }
            else {
                Matcher index = INDEX_PATTERN.matcher(path);
                if (index.find(position) && index.start() == position) {
                    String token = index.group(1);
                    segments.add(token.isEmpty()
                            ? IndexSegment.wildcard()
                            : new IndexSegment(Integer.parseInt(token)));
                    position = index.end();
                }
                else {
                    throw new IllegalArgumentException(String.format("Invalid field path: '%s'", path));
                }
            }
        }
        if (segments.isEmpty()) {
            throw new IllegalArgumentException(String.format("Field path must not be empty: '%s'", path));
        }
        return segments;
    }
}
