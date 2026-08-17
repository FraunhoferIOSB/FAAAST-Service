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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute;

import static java.util.Optional.ofNullable;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.EvaluationContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.StringValue;
import java.util.Objects;


/**
 * An attribute that resolves a value from a claim in the evaluation context.
 */
public class ClaimAttribute implements Attribute {

    private final String claim;

    /**
     * Creates a new claim attribute.
     *
     * @param claim the claim to resolve
     */
    public ClaimAttribute(String claim) {
        this.claim = claim;
    }


    /**
     * Validates this claim attribute.
     *
     * @throws IllegalArgumentException if the claim is null
     */
    public void validate() throws IllegalArgumentException {
        if (claim == null) {
            throw new IllegalArgumentException("Operand to claim attribute is null");
        }
    }


    public String getClaim() {
        return claim;
    }


    @Override
    public boolean isClaim() {
        return true;
    }


    @Override
    public ClaimAttribute asClaim() {
        return this;
    }


    @Override
    public StringValue evaluatePartially(EvaluationContext evaluationContext) {
        return ofNullable(evaluationContext.getClaim(claim))
                .map(StringValue::new)
                .orElseThrow(() -> new IllegalStateException(String.format("Claim %s not present in context", claim)));
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClaimAttribute that = (ClaimAttribute) o;
        return Objects.equals(claim, that.claim);
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(claim);
    }
}
