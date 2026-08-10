package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal;

public record NumberLiteral(Number value) implements Literal {

    public static NumberLiteral parse(String value) {
        return new NumberLiteral(Double.parseDouble(value));
    }


    @Override
    public boolean isNumber() {
        return true;
    }


    @Override
    public NumberLiteral asNumber() {
        return this;
    }
}
