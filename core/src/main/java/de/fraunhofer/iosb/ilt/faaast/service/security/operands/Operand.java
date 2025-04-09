package de.fraunhofer.iosb.ilt.faaast.service.security.operands;

import de.fraunhofer.iosb.ilt.faaast.service.security.utils.EvaluationContext;

public interface Operand {
    Object getValue(EvaluationContext context);
}
