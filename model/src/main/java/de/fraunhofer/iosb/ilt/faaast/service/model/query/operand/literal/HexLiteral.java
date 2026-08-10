package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal;

import java.util.HexFormat;


public record HexLiteral(Long value) implements Literal {

    public static HexLiteral parse(String value) {
        return new HexLiteral(HexFormat.fromHexDigitsToLong(value));
    }


    @Override
    public boolean isHex() {
        return true;
    }


    @Override
    public HexLiteral asHex() {
        return this;
    }
}
