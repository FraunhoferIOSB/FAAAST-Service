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
package de.fraunhofer.iosb.ilt.faaast.service.model.visitor;

import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Collects a map of all referables and their corresponding references.
 */
public class ReferenceCollector extends AssetAdministrationShellElementWalker {

    private Map<Reference, List<Reference>> aasContext;
    private Reference parent;
    private Map<Reference, Referable> result;

    private void init() {
        aasContext = new HashMap<>();
        parent = null;
        result = new HashMap<>();
    }


    /**
     * Collects a map of all referables and their corresponding references.
     *
     * @param obj the object to search, e.g., an null {@link org.eclipse.digitaltwin.aas4j.v3.model.Environment},
     *            {@link org.eclipse.digitaltwin.aas4j.v3.model.Referable}, or
     *            {@link org.eclipse.digitaltwin.aas4j.v3.model.Identifiable}
     * @return a map of all referables contained in any depth with their corresponding reference
     */
    public static Map<Reference, Referable> collect(Object obj) {
        ReferenceCollector collector = new ReferenceCollector();
        collector.walk(obj);
        return collector.result;
    }


    public ReferenceCollector() {
        this.after = new DefaultAssetAdministrationShellElementVisitor() {
            @Override
            public void visit(Referable referable) {
                if (isContainerElement(referable)) {
                    parent = ReferenceHelper.getParent(parent);
                }
            }
        };
        this.visitor = new DefaultAssetAdministrationShellElementVisitor() {

            @Override
            public void visit(AssetAdministrationShell aas) {
                aasContext.put(ReferenceBuilder.forAas(aas), aas.getSubmodels());
                DefaultAssetAdministrationShellElementVisitor.super.visit(aas);
            }


            @Override
            public void visit(Referable referable) {
                if (referable == null) {
                    return;
                }
                String id = referable.getIdShort();
                if (Identifiable.class.isAssignableFrom(referable.getClass())) {
                    id = ((Identifiable) referable).getId();
                }
                if (Objects.nonNull(parent) && parent.getKeys().get(parent.getKeys().size() - 1).getType() == KeyTypes.SUBMODEL_ELEMENT_LIST) {
                    id = Integer.toString(((SubmodelElementList) result.get(parent)).getValue().indexOf(referable));
                }

                Reference reference = ReferenceHelper.combine(
                        parent,
                        new ReferenceBuilder()
                                .element(id, referable.getClass())
                                .build());
                result.put(reference, referable);
                Key root = ReferenceHelper.getRoot(reference);
                if (Objects.nonNull(root) && root.getType() == KeyTypes.SUBMODEL) {
                    result.putAll(aasContext.entrySet().stream()
                            .filter(x -> x.getValue().stream().anyMatch(y -> ReferenceHelper.equals(y, ReferenceHelper.fromKeys(root))))
                            .collect(Collectors.toMap(
                                    x -> ReferenceHelper.combine(x.getKey(), reference),
                                    x -> referable)));
                }
                if (isContainerElement(referable)) {
                    parent = reference;
                }
            }
        };
    }


    @Override
    public void visit(AnnotatedRelationshipElement element) {
        visitBefore(element);
        visitAfter(element);
    }


    @Override
    public void visit(AssetInformation element) {
        visitBefore(element);
        visitAfter(element);
    }


    @Override
    public void visit(Operation element) {
        visitBefore(element);
        visitAfter(element);
    }


    @Override
    public void visit(Entity element) {
        visitBefore(element);
        visitAfter(element);
    }


    @Override
    public void walk(Object obj) {
        init();
        super.walk(obj);
    }


    private static boolean isContainerElement(Referable referable) {
        if (referable == null) {
            return false;
        }
        return Environment.class.isAssignableFrom(referable.getClass())
                || AssetAdministrationShell.class.isAssignableFrom(referable.getClass())
                || Submodel.class.isAssignableFrom(referable.getClass())
                || SubmodelElementCollection.class.isAssignableFrom(referable.getClass())
                || SubmodelElementList.class.isAssignableFrom(referable.getClass());
    }

}
