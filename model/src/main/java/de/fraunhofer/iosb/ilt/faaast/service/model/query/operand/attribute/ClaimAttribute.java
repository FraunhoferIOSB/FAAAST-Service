package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.literal.StringLiteral;

import static java.util.Optional.ofNullable;


public class ClaimAttribute extends Attribute {

    private final String claim;


    public ClaimAttribute(String claim) {this.claim = claim;}


    public void validate() throws IllegalArgumentException {
        if (claim == null) {
            throw new IllegalArgumentException("Operand to claim attribute is null");
        }
    }


    @Override
    public StringLiteral evaluatePartially(EvaluationContext evaluationContext) {
        return ofNullable(evaluationContext.getClaim(claim))
                .map(StringLiteral::parse)
                .orElseThrow(() -> new IllegalStateException(String.format("Claim %s not present in context", claim)));
    }
}
