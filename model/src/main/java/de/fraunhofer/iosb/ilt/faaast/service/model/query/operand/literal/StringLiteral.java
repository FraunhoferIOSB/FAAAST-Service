package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal;

public record StringLiteral(String value) implements Literal {

    public static StringLiteral parse(String value) {
        return new StringLiteral(value);
    }


    @Override
    public boolean isString() {
        return true;
    }


    @Override
    public StringLiteral asString() {
        return this;
    }
}
