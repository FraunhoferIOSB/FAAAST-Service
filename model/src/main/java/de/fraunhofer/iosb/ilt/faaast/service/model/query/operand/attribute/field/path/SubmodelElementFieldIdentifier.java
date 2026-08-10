package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.path;

import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.FieldIdentifierAttribute;

import java.util.Objects;


public class SubmodelElementFieldIdentifier extends FieldIdentifierAttribute {

    private final IdShortPath idShortPath;


    public SubmodelElementFieldIdentifier(FieldPath fieldPath) {
        super(fieldPath);
        idShortPath = IdShortPath.EMPTY;
    }


    public SubmodelElementFieldIdentifier(FieldPath fieldPath, IdShortPath idShortPath) {
        super(fieldPath);
        this.idShortPath = idShortPath;
    }


    public IdShortPath getIdShortPath() {
        return idShortPath;
    }


    @Override
    protected String getScopeSyntax() {
        return idShortPath == null
                ? "$sme"
                : "$sme." + idShortPath;
    }


    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        SubmodelElementFieldIdentifier other = (SubmodelElementFieldIdentifier) obj;
        return Objects.equals(idShortPath, other.idShortPath);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idShortPath);
    }
}
