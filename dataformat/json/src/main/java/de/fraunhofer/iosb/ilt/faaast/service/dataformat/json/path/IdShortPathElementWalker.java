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
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Walks an AAS element recursively finding all idShort paths for serialization with content=path.
 */
public class IdShortPathElementWalker extends AssetAdministrationShellElementWalker {

    public static final String ID_SHORT_PATH_SEPARATOR = ".";
    private Deque<Referable> hierarchy;
    private Deque<String> path;
    private List<String> idShortPaths;
    private Map<Referable, Integer> submodelElementListChildCounters = new HashMap<>();
    private Level level;
    private int maxDepth;

    private void init() {
        maxDepth = level == Level.CORE ? 2 : Integer.MAX_VALUE;
        hierarchy = new ArrayDeque<>();
        path = new ArrayDeque<>();
        idShortPaths = new ArrayList<>();
    }


    public List<String> getIdShortPaths() {
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
        String newPath;
        if (path.isEmpty()) {
            newPath = referable.getIdShort();
        }
        else {
            // add path segment
            Referable parent = hierarchy.peekLast();
            if (Objects.nonNull(parent) && SubmodelElementList.class.isAssignableFrom(parent.getClass())) {
                if (!submodelElementListChildCounters.containsKey(parent)) {
                    submodelElementListChildCounters.put(parent, 0);
                }
                else {
                    submodelElementListChildCounters.put(parent, submodelElementListChildCounters.get(parent) + 1);
                }
                newPath = "[" + submodelElementListChildCounters.get(parent) + "]";
            }
            else {
                newPath = ID_SHORT_PATH_SEPARATOR + referable.getIdShort();
            }
        }
        path.addLast(newPath);
        if (path.size() > maxDepth) {
            return;
        }
        idShortPaths.add(String.join("", path));
    }


    public IdShortPathElementWalker(Level level) {
        this.level = level;
        this.after = new DefaultAssetAdministrationShellElementVisitor() {
            @Override
            public void visit(Referable referable) {
                if (isContainerElement(referable)) {
                    hierarchy.pollLast();
                }
                path.pollLast();
            }
        };
        this.visitor = new DefaultAssetAdministrationShellElementVisitor() {
            @Override
            public void visit(Referable referable) {
                if (referable == null) {
                    return;
                }
                addPath(referable);
                if (isContainerElement(referable)) {
                    hierarchy.add(referable);
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

}
