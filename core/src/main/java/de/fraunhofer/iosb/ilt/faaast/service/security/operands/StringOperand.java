package de.fraunhofer.iosb.ilt.faaast.service.security.operands;

import de.fraunhofer.iosb.ilt.faaast.service.security.utils.EvaluationContext;

public class StringOperand implements Operand {
    private String value;

    public StringOperand(String value) {
        this.value = value;
    }

    @Override
    public Object getValue(EvaluationContext context) {
        // TODO Retrieve value based on context
        return value;
    }

    // Constructors, getters, and setters
}
