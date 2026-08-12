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


public interface GlobalAttribute extends Attribute {

    @Override
    default boolean isGlobal() {
        return true;
    }


    @Override
    default GlobalAttribute asGlobal() {
        return this;
    }


    default boolean isAnonymous() {
        return false;
    }


    default Anonymous asAnonymous() {
        throw new UnsupportedOperationException("%s is not Anonymous");
    }


    default boolean isClientNow() {
        return false;
    }


    default ClientNow asClientNow() {
        throw new UnsupportedOperationException("%s is not ClientNow");
    }


    default boolean isLocalNow() {
        return false;
    }


    default LocalNow asLocalNow() {
        throw new UnsupportedOperationException("%s is not LocalNow");
    }


    default boolean isUtcNow() {
        return false;
    }


    default UtcNow asUtcNow() {
        throw new UnsupportedOperationException("%s is not UtcNow");
    }

}
