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
