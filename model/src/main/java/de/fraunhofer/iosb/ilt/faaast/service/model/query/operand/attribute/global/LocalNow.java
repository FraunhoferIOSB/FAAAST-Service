package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.DateTimeLiteral;

import java.time.LocalDateTime;
import java.time.ZoneId;


public class LocalNow extends GlobalAttribute {
    @Override
    public DateTimeLiteral evaluatePartially(EvaluationContext evaluationContext) {
        return new DateTimeLiteral(LocalDateTime.now().atZone(ZoneId.systemDefault()));
    }
}
