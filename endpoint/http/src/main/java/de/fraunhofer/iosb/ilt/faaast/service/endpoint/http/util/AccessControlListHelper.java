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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Acl;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Defacl;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Defattribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Defformula;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Defobject;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.ObjectItem;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


public class AccessControlListHelper {
    private AccessControlListHelper() {}


    public static Acl getAcl(AccessPermissionRule rule, AllAccessPermissionRules allAccess) {
        if (rule.getAcl() != null) {
            return rule.getAcl();
        }
        else if (rule.getUseacl() != null) {
            Optional<Defacl> acl = allAccess.getDefacls().stream()
                    .filter(a -> Objects.equals(a.getName(), rule.getUseacl()))
                    .findAny();
            if (acl.isPresent()) {
                return acl.get().getAcl();
            }
            else {
                throw new IllegalArgumentException("DEFACL not found: " + rule.getUseacl());
            }
        }
        else {
            throw new IllegalArgumentException("invalid rule: ACL or USEACL must be specified");
        }
    }


    public static List<AttributeItem> getAttributes(Acl acl, AllAccessPermissionRules allAccess) {
        if ((acl.getAttributes() != null) && (!acl.getAttributes().isEmpty())) {
            return acl.getAttributes();
        }
        else if (acl.getUseattributes() != null) {
            Optional<Defattribute> attribute = allAccess.getDefattributes().stream()
                    .filter(a -> Objects.equals(a.getName(), acl.getUseattributes()))
                    .findAny();
            if (attribute.isPresent()) {
                return attribute.get().getAttributes();
            }
            else {
                throw new IllegalArgumentException("DEFATTRIBUTES not found: " + acl.getUseattributes());
            }
        }
        else {
            throw new IllegalArgumentException("invalid rule: ATTRIBUTES or USEATTRIBUTES must be specified");
        }
    }


    public static LogicalExpression getFormula(AccessPermissionRule rule, AllAccessPermissionRules allAccess) {
        if (rule.getFormula() != null) {
            return rule.getFormula();
        }
        else if (rule.getUseformula() != null) {
            Optional<Defformula> formula = allAccess.getDefformulas().stream()
                    .filter(a -> Objects.equals(a.getName(), rule.getUseformula()))
                    .findAny();
            if (formula.isPresent()) {
                return formula.get().getFormula();
            }
            else {
                throw new IllegalArgumentException("DEFFORMULA not found: " + rule.getUseformula());
            }
        }
        else {
            throw new IllegalArgumentException("invalid rule: FORMULA or USEFORMULA must be specified");
        }
    }


    public static List<ObjectItem> getObjects(AccessPermissionRule rule, AllAccessPermissionRules allAccess) {
        if ((rule.getObjects() != null) && (!rule.getObjects().isEmpty())) {
            return rule.getObjects();
        }
        else if (rule.getUseobjects() != null) {
            // We must collect all Defobjects in all Useobjects
            List<Defobject> objectList = allAccess.getDefobjects().stream()
                    .filter(a -> rule.getUseobjects().contains(a.getName()))
                    .toList();
            if (objectList.isEmpty()) {
                throw new IllegalArgumentException("DEFOBJECTS not found: " + rule.getUseobjects());
            }
            else {
                Set<ObjectItem> retval = new HashSet<>();
                for (Defobject item: objectList) {
                    retval.addAll(item.getObjects());
                }
                return retval.stream().toList();
            }
        }
        else {
            throw new IllegalArgumentException("Invalid rule: OBJECTS or USEOBJECTS must be specified");
        }
    }
}
