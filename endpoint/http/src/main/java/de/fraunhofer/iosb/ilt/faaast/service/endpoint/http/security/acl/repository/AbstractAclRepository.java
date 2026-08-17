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

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.acl.repository.def.DefEntityStore;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.expression.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.filter.QueryFilter;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.ClaimAttribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessRuleEntity;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.UnresolvedAccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.AccessObject;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.AccessRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.Rule;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule.UnresolvedAccessRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.parser.AccessPermissionRuleParser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Keeps an in-memory version of the aclFolder's rules. When access control rules are added/deleted/modified, updates
 * its own state accordingly.
 *
 * @param <T> Identifier type.
 */
public abstract class AbstractAclRepository<T> implements AclRepository {

    private final DefEntityStore<T> defEntityStore = new DefEntityStore<>();
    private final AccessPermissionRuleParser aclParser = new AccessPermissionRuleParser();
    protected final Map<T, List<UnresolvedAccessPermissionRule>> unresolved;
    // The active rule set is the set of rules that are fully resolved (no USE*) and active
    protected volatile List<AccessPermissionRule> activeRuleSet;

    /**
     * Class constructor.
     */
    protected AbstractAclRepository() {
        this.unresolved = new HashMap<>();
        this.activeRuleSet = new ArrayList<>();
    }


    @Override
    public List<AccessPermissionRule> getActiveRules(Set<String> claims) {
        // Don't return rules where the claims don't match.
        List<AccessPermissionRule> list = new ArrayList<>();
        for (AccessPermissionRule rule: activeRuleSet) {
            List<String> requiredClaims = getRequiredClaims(rule.rule().attributes());
            if (requiredClaims.isEmpty()
                    || claims.containsAll(requiredClaims)
                    || rule.rule().attributes().stream().anyMatch(attr -> attr.isGlobal() && attr.asGlobal().isAnonymous())) {
                list.add(rule);
            }
        }
        return list;
    }


    private List<String> getRequiredClaims(List<Attribute> attributes) {
        return attributes.stream()
                .filter(Attribute::isClaim)
                .map(Attribute::asClaim)
                .map(ClaimAttribute::getClaim)
                .toList();
    }


    /**
     * Add an environment to the current ACL and resolve all DEF* into the AccessPermissionRule list.
     *
     * @param identifier Identifier of rule to add.
     * @param acl Rule environment to add.
     */
    public final void add(T identifier, AllAccessPermissionRules acl) {
        defEntityStore.addDefAcls(identifier, acl.getDefacls());
        defEntityStore.addDefAttributes(identifier, acl.getDefattributes());
        defEntityStore.addDefObjects(identifier, acl.getDefobjects());
        defEntityStore.addDefFormulas(identifier, acl.getDefformulas());

        unresolved.put(identifier, acl.getRules().stream().map(aclParser::parse).toList());
        resolve();
    }


    /**
     * Remove an environment from the current ACL along with its DEF* and re-resolve active rules
     *
     * @param identifier Identifier of rule to remove.
     */
    public final void remove(T identifier) {
        remove(identifier, true);
    }


    /**
     * Remove an environment from the current ACL along with its DEF*.
     *
     * @param identifier Identifier of rule to remove.
     * @param resolve Whether the set of active rules are to be resolved again.
     */
    public final void remove(T identifier, boolean resolve) {
        defEntityStore.delete(identifier);
        if (unresolved.remove(identifier) != null && resolve) {
            resolve();
        }
    }


    private void resolve() {

        for (Map.Entry<T, List<UnresolvedAccessPermissionRule>> rules: unresolved.entrySet()) {
            activeRuleSet.addAll(rules.getValue().stream().map(this::resolve).filter(AccessPermissionRule::isEnabled).toList());
        }
    }


    private AccessPermissionRule resolve(UnresolvedAccessPermissionRule unresolved) {
        AccessRule rule = resolve(unresolved.rule());
        List<AccessObject> objects = resolve(unresolved.objects());
        LogicalExpression formula;
        if (unresolved.formula().isUse()) {
            formula = defEntityStore.getFormula(unresolved.formula().getUseName());
        }
        else {
            formula = unresolved.formula().getInstance();
        }

        List<QueryFilter> filters = unresolved.filters();

        return new AccessPermissionRule(rule, objects, formula, filters);
    }


    private List<AccessObject> resolve(List<AccessRuleEntity<AccessObject>> unresolved) {
        List<AccessObject> resolved = new ArrayList<>();
        for (AccessRuleEntity<AccessObject> object: unresolved) {
            if (object.isUse()) {
                resolved.addAll(defEntityStore.getObjects(object.getUseName()));
            }
            else {
                resolved.add(object.getInstance());
            }
        }
        return resolved;
    }


    private AccessRule resolve(AccessRuleEntity<Rule> unresolved) {
        Rule resolved = unresolved.getInstance();
        if (unresolved.isUse()) {
            resolved = defEntityStore.getAccessRule(unresolved.getUseName());
        }

        if (resolved instanceof UnresolvedAccessRule unresolvedAccessRule) {
            List<Attribute> attributes = unresolvedAccessRule.attributes().stream()
                    .map(attr -> attr.isUse() ? defEntityStore.getAttribute(attr.getUseName()) : List.of(attr.getInstance()))
                    .flatMap(Collection::stream)
                    .toList();
            resolved = unresolvedAccessRule.from(attributes);
        }

        return (AccessRule) resolved;
    }
}
