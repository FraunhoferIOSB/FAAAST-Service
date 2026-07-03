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
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Keeps an in-memory version of the aclFolder's rules. When access control rules are added/deleted/modified, updates
 * its own state accordingly.
 *
 * @param <T> Identifier type.
 */
public abstract class AbstractAclRepository<T> implements AclRepository {

    protected final Map<T, AllAccessPermissionRules> allAccessPermissionRules;

    private List<AccessPermissionRule> resolved;

    /**
     * Class constructor.
     */
    protected AbstractAclRepository() {
        this.allAccessPermissionRules = new HashMap<>();
        this.resolved = new ArrayList<>();
    }


    @Override
    public List<AccessPermissionRule> getAccessPermissionRules() {
        return new ArrayList<>(resolved.stream().map(rule -> DeepCopyHelper.deepCopyAny(rule, AccessPermissionRule.class)).toList());
    }


    /**
     * Add an environment to the current ACL and resolve all DEF* into the AccessPermissionRule list.
     *
     * @param identifier Identifier of rule to add.
     * @param acl Rule environment to add.
     */
    public final void add(T identifier, AllAccessPermissionRules acl) {
        allAccessPermissionRules.put(identifier, acl);
        resolve(allAccessPermissionRules.values());
    }


    /**
     * Remove an environment from the current ACL along with its DEF*.
     *
     * @param identifier Identifier of rule to remove.
     */
    public final void remove(T identifier) {
        if (allAccessPermissionRules.remove(identifier) != null) {
            resolve(allAccessPermissionRules.values());
        }
    }


    private void resolve(Collection<AllAccessPermissionRules> unresolved) {
        AllAccessPermissionRules unresolvedMerged = merge(unresolved);
        List<AccessPermissionRule> resolvedRules = new ArrayList<>();
        for (AccessPermissionRule rule: unresolvedMerged.getRules()) {
            AccessPermissionRule resolvedRule = new AccessPermissionRule();
            Acl acl = new Acl();
            acl.setAccess(rule.getAcl().getAccess());
            acl.setRights(rule.getAcl().getRights());
            acl.setAttributes(getAttributes(getAcl(rule, unresolvedMerged), unresolvedMerged));
            resolvedRule.setAcl(acl);
            resolvedRule.setObjects(getObjects(rule, unresolvedMerged));
            resolvedRule.setFormula(getFormula(rule, unresolvedMerged));
            resolvedRule.setFilter(getFilter(rule, unresolvedMerged));
            resolvedRules.add(resolvedRule);
        }

        resolved = resolvedRules;
    }


    private AllAccessPermissionRules merge(Collection<AllAccessPermissionRules> rulesList) {
        AllAccessPermissionRules merged = new AllAccessPermissionRules();
        merged.setRules(rulesList.stream().map(AllAccessPermissionRules::getRules).flatMap(Collection::stream).toList());
        merged.setDefobjects(rulesList.stream().map(AllAccessPermissionRules::getDefobjects).flatMap(Collection::stream).toList());
        merged.setDefformulas(rulesList.stream().map(AllAccessPermissionRules::getDefformulas).flatMap(Collection::stream).toList());
        merged.setDefacls(rulesList.stream().map(AllAccessPermissionRules::getDefacls).flatMap(Collection::stream).toList());
        merged.setDefattributes(rulesList.stream().map(AllAccessPermissionRules::getDefattributes).flatMap(Collection::stream).toList());
        return merged;
    }
}
