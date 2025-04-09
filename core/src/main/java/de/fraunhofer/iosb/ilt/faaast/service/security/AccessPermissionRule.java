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

import de.fraunhofer.iosb.ilt.faaast.service.security.objects.AccessObject;
import de.fraunhofer.iosb.ilt.faaast.service.security.objects.IdentifiableObject;
import java.util.List;


/**
 * Describes an access permissiom rule.
 */
public class AccessPermissionRule {
    private ACL acl;
    private List<AccessObject> objects;
    private Condition formula;
    private Filter filter; // Optional

    /**
     * Set the ACL.
     *
     * @param acl
     */
    public void setAcl(ACL acl) {}


    /**
     * Set the objects.
     *
     * @param list
     */
    public void setObjects(List<IdentifiableObject> list) {}


    /**
     * Set the formula.
     *
     * @param condition
     */
    public void setFormula(Condition condition) {}

    // Constructors, getters, and setters
}
