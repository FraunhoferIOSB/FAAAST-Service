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
import java.util.Optional;


public class EvaluationContext {

    private final Map<String, String> claims;
    private final String route;

    public EvaluationContext(Map<String, String> claims, String route) {
        this.claims = claims;
        this.route = route;
    }


    public EvaluationContext(Map<String, String> claims) {
        this.claims = claims;
        this.route = null;
    }


    public String getClaim(String claimName) {
        return claims.getOrDefault(claimName, "does not exist :-D");
    }


    public Optional<String> getRoute() {
        return Optional.ofNullable(route);
    }


    public boolean isAnonymous() {
        return claims.isEmpty();
    }

}
