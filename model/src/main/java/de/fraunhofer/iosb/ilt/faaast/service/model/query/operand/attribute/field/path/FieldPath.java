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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.path;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.path.segment.FieldPathSegment;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.path.segment.PropertySegment;

import java.util.List;
import java.util.Objects;


public record FieldPath(List<FieldPathSegment> segments) {
    public FieldPath(List<FieldPathSegment> segments) {
        Objects.requireNonNull(segments, "segments must be non-null");
        if (segments.isEmpty()) {
            throw new IllegalArgumentException("segments must not be empty");
        }
        if (segments.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("segments must not contain null");
        }
        if (!(segments.get(0) instanceof PropertySegment)) {
            throw new IllegalArgumentException("path must start with a property segment");
        }
        this.segments = List.copyOf(segments);
    }

}
