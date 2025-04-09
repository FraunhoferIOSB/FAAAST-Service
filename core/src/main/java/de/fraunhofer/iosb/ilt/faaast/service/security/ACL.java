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
package de.fraunhofer.iosb.ilt.faaast.service.security;

import de.fraunhofer.iosb.ilt.faaast.service.security.attributes.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.security.attributes.ClaimAttribute;
import java.util.List;


/**
 * Describes the access control list.
 */
public class ACL {
    private List<Attribute> attributes;
    private List<Right> rights;
    private AccessType accessType;

    /**
     * Set the attributes.
     *
     * @param list
     */
    public void setAttributes(List<ClaimAttribute> list) {}


    /**
     * Set the rights.
     *
     * @param rights
     */
    public void setRights(List<Right> rights) {}


    /**
     * Set the access type.
     *
     * @param accessType
     */
    public void setAccessType(AccessType accessType) {}

    // Constructors, getters, and setters
}
