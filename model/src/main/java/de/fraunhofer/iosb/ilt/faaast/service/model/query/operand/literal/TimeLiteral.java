package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal;

import java.time.OffsetTime;


public record TimeLiteral(OffsetTime value) implements Literal {

    public static TimeLiteral parse(String value) {
        return new TimeLiteral(OffsetTime.parse(value));
    }


    @Override
    public boolean isTime() {
        return true;
    }


    @Override
    public TimeLiteral asTime() {
        return this;
    }
}
