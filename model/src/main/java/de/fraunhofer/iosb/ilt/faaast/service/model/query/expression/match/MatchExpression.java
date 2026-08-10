package de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.match;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;

import java.util.List;


public class MatchExpression implements MatchElement {
    List<MatchElement> elements;


    @Override
    public LogicalExpression evaluatePartially(EvaluationContext evaluationContext) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
