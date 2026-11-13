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

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.Operand;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.GlobalAttribute;


/**
 * An attribute used as an operand in a query expression.
 */
public interface Attribute extends Operand {
    /**
     * Returns whether this attribute is a global attribute.
     *
     * @return true if this attribute is a global attribute, otherwise false
     */
    default boolean isGlobal() {
        return false;
    }


    /**
     * Returns this attribute as a global attribute.
     *
     * @return this attribute as a global attribute
     */
    default GlobalAttribute asGlobal() {
        throw new UnsupportedOperationException("%s is not a global attribute");
    }


    /**
     * Returns whether this attribute is a claim attribute.
     *
     * @return true if this attribute is a claim attribute, otherwise false
     */
    default boolean isClaim() {
        return false;
    }


    /**
     * Returns this attribute as a claim attribute.
     *
     * @return this attribute as a claim attribute
     */
    default ClaimAttribute asClaim() {
        throw new UnsupportedOperationException("%s is not a claim attribute");

    }
}
