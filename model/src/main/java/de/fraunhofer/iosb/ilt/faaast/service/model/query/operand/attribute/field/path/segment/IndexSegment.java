package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.path.segment;

import javax.annotation.Nonnull;


/**
 * May be null -> existence check (wildcard)
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
