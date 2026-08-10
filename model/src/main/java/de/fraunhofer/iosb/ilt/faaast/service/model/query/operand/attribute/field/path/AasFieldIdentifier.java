package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.path;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.FieldIdentifierAttribute;


public class AasFieldIdentifier extends FieldIdentifierAttribute {
    public AasFieldIdentifier(FieldPath fieldPath) {
        super(fieldPath);
    }


    @Override
    protected String getScopeSyntax() {
        return "$aas";
    }
}
