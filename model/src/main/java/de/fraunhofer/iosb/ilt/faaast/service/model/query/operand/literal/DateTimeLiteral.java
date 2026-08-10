package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;


public record DateTimeLiteral(ZonedDateTime value) implements Literal {

    public static DateTimeLiteral parse(String value) {
        return Optional.of(tryParseZonedDateTime(value))
                .orElse(tryParseInstant(value).map(i -> i.atZone(ZoneId.systemDefault())))
                .map(DateTimeLiteral::new)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Cannot parse %s to DateTime", value)));
    }


    private static Optional<ZonedDateTime> tryParseZonedDateTime(String value) {
        try {
            return Optional.of(ZonedDateTime.parse(value));
        }
        catch (DateTimeParseException dateTimeParseException) {
            return Optional.empty();
        }
    }


    private static Optional<Instant> tryParseInstant(String value) {
        try {
            return Optional.of(Instant.parse(value));
        }
        catch (DateTimeParseException dateTimeParseException) {
            return Optional.empty();
        }
    }


    @Override
    public boolean isDateTime() {
        return true;
    }


    @Override
    public DateTimeLiteral asDateTime() {
        return this;
    }
}
