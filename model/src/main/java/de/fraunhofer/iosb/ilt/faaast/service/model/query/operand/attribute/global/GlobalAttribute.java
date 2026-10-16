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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.Attribute;


/**
 * A global attribute that can be referenced without a specific field scope.
 */
public interface GlobalAttribute extends Attribute {

    @Override
    default boolean isGlobal() {
        return true;
    }


    @Override
    default GlobalAttribute asGlobal() {
        return this;
    }


    /**
     * Returns whether this global attribute is the anonymous attribute.
     *
     * @return true if this global attribute is the anonymous attribute, otherwise false
     */
    default boolean isAnonymous() {
        return false;
    }


    /**
     * Returns this global attribute as an anonymous attribute.
     *
     * @return this global attribute as an anonymous attribute
     */
    default Anonymous asAnonymous() {
        throw new UnsupportedOperationException("%s is not Anonymous");
    }


    /**
     * Returns whether this global attribute is the client now attribute.
     *
     * @return true if this global attribute is the client now attribute, otherwise false
     */
    default boolean isClientNow() {
        return false;
    }


    /**
     * Returns this global attribute as a client now attribute.
     *
     * @return this global attribute as a client now attribute
     */
    default ClientNow asClientNow() {
        throw new UnsupportedOperationException("%s is not ClientNow");
    }


    /**
     * Returns whether this global attribute is the local now attribute.
     *
     * @return true if this global attribute is the local now attribute, otherwise false
     */
    default boolean isLocalNow() {
        return false;
    }


    /**
     * Returns this global attribute as a local now attribute.
     *
     * @return this global attribute as a local now attribute
     */
    default LocalNow asLocalNow() {
        throw new UnsupportedOperationException("%s is not LocalNow");
    }


    /**
     * Returns whether this global attribute is the UTC now attribute.
     *
     * @return true if this global attribute is the UTC now attribute, otherwise false
     */
    default boolean isUtcNow() {
        return false;
    }


    /**
     * Returns this global attribute as a UTC now attribute.
     *
     * @return this global attribute as a UTC now attribute
     */
    default UtcNow asUtcNow() {
        throw new UnsupportedOperationException("%s is not UtcNow");
    }

}
