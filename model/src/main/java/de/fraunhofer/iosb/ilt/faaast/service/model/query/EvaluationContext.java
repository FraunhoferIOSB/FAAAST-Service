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
package de.fraunhofer.iosb.ilt.faaast.service.model.query;

import java.util.Map;
import javax.annotation.Nullable;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;


/**
 * Evaluation context to partially evaluate a query using claims and an identifiable object under test.
 */
public class EvaluationContext {

    private final Map<String, String> claims;
    private final Identifiable identifiable;

    public EvaluationContext(Map<String, String> claims, Identifiable identifiable) {
        this.claims = claims;
        this.identifiable = identifiable;
    }


    public EvaluationContext(Map<String, String> claims) {
        this.claims = claims;
        this.identifiable = null;
    }


    public EvaluationContext(Identifiable identifiable) {
        this.claims = Map.of();
        this.identifiable = identifiable;
    }


    /**
     * Get a claim from the context.
     *
     * @param claimName The name of the claim.
     * @return The claim or null if no claim was registered under the name.
     */
    public @Nullable String getClaim(String claimName) {
        return claims.get(claimName);
    }


    /**
     * Get the identifiable under test from the context.
     *
     * @return The identifiable or null if no identifiable is available.
     */
    public @Nullable Identifiable getIdentifiable() {
        return identifiable;
    }


    /**
     * Returns whether no claims are present.
     *
     * @return True if no claims are present in the context.
     */
    public boolean isAnonymous() {
        return claims.isEmpty();
    }

}
