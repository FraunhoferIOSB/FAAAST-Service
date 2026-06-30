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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.acl.repository;

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util.AccessControlListHelper.getAcl;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util.AccessControlListHelper.getAttributes;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util.AccessControlListHelper.getFilter;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util.AccessControlListHelper.getFormula;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.util.AccessControlListHelper.getObjects;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Acl;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import java.util.ArrayList;
import java.util.List;


/**
 * Keeps an in-memory version of the aclFolder's rules. When access control rules are added/deleted/modified, updates
 * its own state accordingly.
 */
public abstract class AbstractAclRepository implements AclRepository {

    private AllAccessPermissionRules allAccessPermissionRules;

    /**
     * Class constructor.
     */
    protected AbstractAclRepository() {
        this.allAccessPermissionRules = new AllAccessPermissionRules();
    }


    @Override
    public List<AccessPermissionRule> getAccessPermissionRules() {
        return allAccessPermissionRules.getRules();
    }


    @Override
    public final void addAndResolve(AllAccessPermissionRules acl) {
        allAccessPermissionRules.getRules().addAll(acl.getRules());
        allAccessPermissionRules.getDefacls().addAll(acl.getDefacls());
        allAccessPermissionRules.getDefattributes().addAll(acl.getDefattributes());
        allAccessPermissionRules.getDefformulas().addAll(acl.getDefformulas());
        allAccessPermissionRules.getDefobjects().addAll(acl.getDefobjects());

        allAccessPermissionRules = resolve(allAccessPermissionRules);
    }


    @Override
    public final void remove(AllAccessPermissionRules acl) {
        allAccessPermissionRules.getRules().removeAll(acl.getRules());
        allAccessPermissionRules.getDefacls().removeAll(acl.getDefacls());
        allAccessPermissionRules.getDefattributes().removeAll(acl.getDefattributes());
        allAccessPermissionRules.getDefformulas().removeAll(acl.getDefformulas());
        allAccessPermissionRules.getDefobjects().removeAll(acl.getDefobjects());

        allAccessPermissionRules = resolve(allAccessPermissionRules);
    }


    private AllAccessPermissionRules resolve(AllAccessPermissionRules unresolved) {
        AllAccessPermissionRules resolved = new AllAccessPermissionRules();
        List<AccessPermissionRule> resolvedRules = new ArrayList<>();
        for (AccessPermissionRule rule: unresolved.getRules()) {
            AccessPermissionRule resolvedRule = new AccessPermissionRule();
            Acl acl = new Acl();
            acl.setAccess(rule.getAcl().getAccess());
            acl.setRights(rule.getAcl().getRights());
            acl.setAttributes(getAttributes(getAcl(rule, unresolved), unresolved));
            resolvedRule.setAcl(acl);
            resolvedRule.setObjects(getObjects(rule, unresolved));
            resolvedRule.setFormula(getFormula(rule, unresolved));
            resolvedRule.setFilter(getFilter(rule, unresolved));
            resolvedRules.add(resolvedRule);
        }

        resolved.setRules(resolvedRules);
        return resolved;
    }
}
