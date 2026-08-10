package de.fraunhofer.iosb.ilt.faaast.service.model.query.filter;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.FieldIdentifierAttribute;


public class QueryFilter {
    private FieldIdentifierAttribute fragment;
    private LogicalExpression condition;
}
