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
package de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.referable;

import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.AccessObject;


public abstract class ReferableObject implements AccessObject {
    private static final String WILDCARD = "(\"*\")";
    private final String identifier;

    protected ReferableObject(String input) {
        this.identifier = input.substring(getNotation().length() + 1, input.lastIndexOf(")") + 1);
    }


    public String getIdentifier() {
        return identifier;
    }


    public boolean isWildcard() {
        return identifier.equals(WILDCARD);
    }


    public abstract String getNotation();
}
