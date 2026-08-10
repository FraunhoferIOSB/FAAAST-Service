package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.DateTimeLiteral;


public class ClientNow extends GlobalAttribute {
    private static final String ISSUED_AT_CLAIM = "iat";


    @Override
    public DateTimeLiteral evaluatePartially(EvaluationContext evaluationContext) {
        String iat = evaluationContext.getClaim(ISSUED_AT_CLAIM);
        if (iat == null) {
            throw new IllegalArgumentException("Help");
        }
        return DateTimeLiteral.parse(iat);
    }
}
