package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.path.FieldPath;

import java.util.Objects;


public abstract class FieldIdentifierAttribute extends Attribute {
    protected final FieldPath fieldPath;


    public FieldIdentifierAttribute(FieldPath fieldPath) {this.fieldPath = fieldPath;}


    protected abstract String getScopeSyntax();


    @Override
    public Operand evaluatePartially(EvaluationContext evaluationContext) {
        return this;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        FieldIdentifierAttribute other = (FieldIdentifierAttribute) obj;
        return Objects.equals(fieldPath, other.fieldPath);
    }


    @Override
    public int hashCode() {
        return Objects.hash(getClass(), fieldPath);
    }


    @Override
    public String toString() {
        return getScopeSyntax() + "#" + fieldPath;
    }
}
