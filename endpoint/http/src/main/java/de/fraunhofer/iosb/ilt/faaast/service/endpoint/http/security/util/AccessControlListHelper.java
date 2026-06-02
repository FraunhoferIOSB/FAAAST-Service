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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util;

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


/**
 * Helper class for working with AAS ACL.
 */
public class AccessControlListHelper {
    private static final String DEFACL = "DEFACL";
    private static final Object USEACL = "USEACL";
    private static final String DEFATTRIBUTES = "DEFATTRIBUTES";
    private static final Object USEATTRIBUTES = "USEATTRIBUTES";
    private static final String DEFFORMULA = "DEFFORUMLA";
    private static final Object USEFORUMLA = "USEFORUMLA";
    private static final String DEFOBJECTS = "DEFOBJECTS";
    private static final Object USEOBJECTS = "USEOBJECTS";

    private AccessControlListHelper() {}


    /**
     * returns the ACL definition of the AccessPermissionRule. If ACL is not directly defined, returns the DEFACL defined by
     * USEACL.
     *
     * @param rule Rule to get ACL from
     * @param allAccess Rule environment
     * @return The ACL.
     */
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
                throw new IllegalArgumentException(String.format("%s not found for %s: %s", DEFACL, USEACL, rule.getUseacl()));
            }
        }
        else {
            throw new IllegalArgumentException(String.format("Invalid rule: ACL or %s must be specified", USEACL));
        }
    }


    /**
     * returns the ATTRIBUTES of the AccessPermissionRule. If ATTRIBUTES are not directly defined, returns the DEFATTRIBUTES
     * defined by USEATTRIBUTES.
     *
     * @param acl ACL to get ATTRIBUTES from
     * @param allAccess Rule environment
     * @return The ATTRIBUTES.
     */
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
                throw new IllegalArgumentException(String.format("%s not found for %s: %s", DEFATTRIBUTES, USEATTRIBUTES, acl.getUseattributes()));
            }
        }
        else {
            throw new IllegalArgumentException(String.format("Invalid rule: ATTRIBUTES or %s must be specified", USEATTRIBUTES));
        }
    }


    /**
     * returns the FORMULA of the AccessPermissionRule. If FORMULA is not directly defined, returns the DEFFORMULA defined
     * by USEFORMULA.
     *
     * @param rule Rule to get FORMULA from
     * @param allAccess Rule environment
     * @return The FORMULA.
     */
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
                throw new IllegalArgumentException(String.format("%s not found for %s: %s", DEFFORMULA, USEFORUMLA, rule.getUseformula()));
            }
        }
        else {
            throw new IllegalArgumentException(String.format("Invalid rule: FORMULA or %s must be specified", USEFORUMLA));
        }
    }


    /**
     * returns the OBJECTS of the AccessPermissionRule. If OBJECTS is not directly defined, returns the DEFOBJECTS defined
     * by USEOBJECTS.
     *
     * @param rule Rule to get ObjectItems from
     * @param allAccess Rule environment
     * @return The OBJECTS.
     */
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
                throw new IllegalArgumentException(String.format("%s not found for %s: %s", DEFOBJECTS, USEOBJECTS, rule.getUseobjects()));
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
            throw new IllegalArgumentException(String.format("Invalid rule: OBJECTS or %s must be specified", USEOBJECTS));
        }
    }
}
