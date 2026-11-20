/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.service.model.query.visitor;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.EqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.GreaterThanEqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.GreaterThanOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.LessThanEqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.LessThanOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.NotEqualsOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string.ContainsComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string.EndsWithComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string.RegexComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.comparison.string.StartsWithComparison;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical.AndOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical.NotOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.logical.OrOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.match.MatchExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.ClaimAttribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.field.FieldIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.Anonymous;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.ClientNow;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.LocalNow;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.UtcNow;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.cast.Cast;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.temporal.TemporalOperation;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;


/**
 * Visitor for {@link de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression} nodes.
 *
 * <p>Each concrete expression type has a dedicated {@code visit} overload. The literal subclasses of
 * {@link TypedValue} share a single {@link #visit(TypedValue)} overload as they all share the same evaluation behavior.
 *
 * @param <R> the return type of the visit methods
 */
public interface LogicalExpressionVisitor<R> {

    /**
     * Visit a {@code $not} operation.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(NotOperation expr);


    /**
     * Visit a {@code $and} operation.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(AndOperation expr);


    /**
     * Visit a {@code $or} operation.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(OrOperation expr);


    /**
     * Visit an {@code $eq} comparison.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(EqualsOperation expr);


    /**
     * Visit a {@code $ne} comparison.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(NotEqualsOperation expr);


    /**
     * Visit a {@code $lt} comparison.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(LessThanOperation expr);


    /**
     * Visit a {@code $le} comparison.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(LessThanEqualsOperation expr);


    /**
     * Visit a {@code $gt} comparison.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(GreaterThanOperation expr);


    /**
     * Visit a {@code $ge} comparison.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(GreaterThanEqualsOperation expr);


    /**
     * Visit a {@code $starts-with} comparison.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(StartsWithComparison expr);


    /**
     * Visit a {@code $ends-with} comparison.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(EndsWithComparison expr);


    /**
     * Visit a {@code $contains} comparison.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(ContainsComparison expr);


    /**
     * Visit a {@code $regex} comparison.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(RegexComparison expr);


    /**
     * Visit a {@code $match} expression.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(MatchExpression expr);


    /**
     * Visit a claim attribute.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(ClaimAttribute expr);


    /**
     * Visit a {@code $utcNow} global attribute.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(UtcNow expr);


    /**
     * Visit a {@code $clientNow} global attribute.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(ClientNow expr);


    /**
     * Visit a {@code $localNow} global attribute.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(LocalNow expr);


    /**
     * Visit an {@code $anonymous} global attribute.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(Anonymous expr);


    /**
     * Visit a cast operation.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(Cast<?> expr);


    /**
     * Visit a temporal operation.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(TemporalOperation expr);


    /**
     * Visit a field identifier.
     *
     * @param expr the expression to visit
     * @return the visit result
     */
    R visit(FieldIdentifier expr);


    /**
     * Visit a typed value (literal). All subclasses of {@link TypedValue} share this single visit method.
     *
     * @param value the value to visit
     * @return the visit result
     */
    R visit(TypedValue<?> value);
}
