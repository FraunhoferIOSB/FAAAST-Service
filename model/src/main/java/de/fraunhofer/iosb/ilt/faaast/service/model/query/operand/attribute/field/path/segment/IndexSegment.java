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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.path.segment;

import javax.annotation.Nonnull;


/**
 * May be null to indicate an existence check (wildcard).
 *
 * @param index Index of a field path.
 */
public record IndexSegment(Integer index) implements FieldPathSegment {
    public IndexSegment {
        if (index != null && index < 0) {
            throw new IllegalArgumentException("index must not be negative");
        }
    }


    /**
     * Creates a segment addressing any element.
     *
     * @return a wildcard segment
     */
    public static IndexSegment wildcard() {
        return new IndexSegment(null);
    }


    /**
     * Indicates whether this segment addresses any element rather than one.
     *
     * @return true if this is the wildcard
     */
    public boolean isWildcard() {
        return index == null;
    }


    @Override
    public @Nonnull String toString() {
        return isWildcard()
                ? "[]"
                : "[" + index + "]";
    }
}
