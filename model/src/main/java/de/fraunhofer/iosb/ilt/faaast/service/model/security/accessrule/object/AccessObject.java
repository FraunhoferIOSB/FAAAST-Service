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
package de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object;

import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessRuleEntity;


/**
 * Interface describing an access object.
 */
public interface AccessObject extends AccessRuleEntity<AccessObject> {
    @Override
    default AccessObject getInstance() {
        return this;
    }


    /**
     * Returns whether this object is a route object.
     *
     * @return true if this object is a route object, otherwise false
     */
    default boolean isRoute() {
        return false;
    }


    /**
     * Returns this object as a {@link RouteObject}.
     *
     * @return the route object
     */
    default RouteObject asRoute() {
        throw new UnsupportedOperationException(String.format("Cannot convert %s to Route", this.getClass().getSimpleName()));
    }
}
