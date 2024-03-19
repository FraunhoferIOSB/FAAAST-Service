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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.path;

import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.AssetAdministrationShellElementWalker;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.DefaultAssetAdministrationShellElementVisitor;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Walks an AAS element recursively finding all idShort paths for serialization with content=path.
 */
public class IdShortPathElementWalker extends AssetAdministrationShellElementWalker {

    private Deque<Referable> hierarchy;
    private IdShortPath path;
    private List<IdShortPath> idShortPaths;
    private Map<Referable, Long> submodelElementListChildCounters = new HashMap<>();
    private Level level;
    private int maxDepth;

    private void init() {
        maxDepth = level == Level.CORE ? 2 : Integer.MAX_VALUE;
        hierarchy = new ArrayDeque<>();
        path = IdShortPath.EMPTY;
        idShortPaths = new ArrayList<>();
    }


    public List<IdShortPath> getIdShortPaths() {
        return idShortPaths;
    }


    private static boolean isContainerElement(Referable referable) {
        if (referable == null) {
            return false;
        }
        return AssetAdministrationShell.class.isAssignableFrom(referable.getClass())
                || Submodel.class.isAssignableFrom(referable.getClass())
                || SubmodelElementCollection.class.isAssignableFrom(referable.getClass())
                || SubmodelElementList.class.isAssignableFrom(referable.getClass());
    }


    private void addPath(Referable referable) {
        if (path.isEmpty()) {
            path = IdShortPath.builder()
                    .from(path)
                    .idShort(referable.getIdShort())
                    .build();
        }
        else {
            // add path segment
            Referable parent = hierarchy.peekLast();
            if (Objects.nonNull(parent) && SubmodelElementList.class.isAssignableFrom(parent.getClass())) {
                if (!submodelElementListChildCounters.containsKey(parent)) {
                    submodelElementListChildCounters.put(parent, 0L);
                }
                else {
                    submodelElementListChildCounters.put(parent, submodelElementListChildCounters.get(parent) + 1);
                }
                path = IdShortPath.builder()
                        .from(path)
                        .index(submodelElementListChildCounters.get(parent))
                        .build();
            }
            else {
                path = IdShortPath.builder()
                        .from(path)
                        .idShort(referable.getIdShort())
                        .build();
            }
        }
        if (hierarchy.size() >= maxDepth) {
            return;
        }
        idShortPaths.add(path);
    }


    public IdShortPathElementWalker(Level level) {
        this.level = level;
        this.after = new DefaultAssetAdministrationShellElementVisitor() {
            @Override
            public void visit(Referable referable) {
                if (isContainerElement(referable)) {
                    hierarchy.pollLast();
                }
                path = path.getParent();
            }
        };
        this.visitor = new DefaultAssetAdministrationShellElementVisitor() {
            @Override
            public void visit(Referable referable) {
                if (Objects.isNull(referable) || SubmodelElement.class.isAssignableFrom(referable.getClass())) {
                    return;
                }
                if (isContainerElement(referable)) {
                    hierarchy.add(referable);
                }
            }


            @Override
            public void visit(SubmodelElement element) {
                if (Objects.isNull(element)) {
                    return;
                }
                addPath(element);
                if (isContainerElement(element)) {
                    hierarchy.add(element);
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


    /**
     * Visits a list by visiting all children.
     *
     * @param <T> content-type of the list
     * @param list the list
     */
    public <T> void visit(List<T> list) {
        list.forEach(this::visit);
    }


    @Override
    public void walk(Object obj) {
        init();
        super.walk(obj);
    }

}
