package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.path.segment;

import java.util.Objects;
import javax.annotation.Nonnull;


public record PropertySegment(String name) implements FieldPathSegment {
    public PropertySegment {
        Objects.requireNonNull(name, "name must be non-null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
    }


    @Override
    public @Nonnull String toString() {
        return name;
    }
}
