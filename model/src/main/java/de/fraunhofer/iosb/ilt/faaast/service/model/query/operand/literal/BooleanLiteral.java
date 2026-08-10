package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal;

/**
 * Note: This class is not defined in the QueryLanguage
 */
public record BooleanLiteral(Boolean value) implements Literal {

    public static BooleanLiteral parse(String value) {
        return new BooleanLiteral(Boolean.parseBoolean(value));
    }


    @Override
    public boolean isBoolean() {
        return true;
    }


    @Override
    public BooleanLiteral asBoolean() {
        return this;
    }
}
