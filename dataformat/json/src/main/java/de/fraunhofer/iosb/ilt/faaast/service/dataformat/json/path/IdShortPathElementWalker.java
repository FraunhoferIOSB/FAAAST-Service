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
import io.adminshell.aas.v3.model.AccessControl;
import io.adminshell.aas.v3.model.AnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetInformation;
import io.adminshell.aas.v3.model.Certificate;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.ObjectAttributes;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.PolicyInformationPoints;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.SubjectAttributes;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Walks an AAS element recursively finding all idShort paths for serialization
 * with content=path
 */
public class IdShortPathElementWalker extends AssetAdministrationShellElementWalker {

    public static final String ID_SHORT_PATH_SEPARATOR = ".";
    private Deque<Referable> hierarchy;
    private List<String> idShortPaths;
    private Level level;

    private int maxDepth;

    private static boolean isContainerElement(Referable referable) {
        if (referable == null) {
            return false;
        }
        return AssetAdministrationShell.class.isAssignableFrom(referable.getClass())
                || Submodel.class.isAssignableFrom(referable.getClass())
                || SubmodelElementCollection.class.isAssignableFrom(referable.getClass());
    }


    public IdShortPathElementWalker(Level level) {
        this.level = level;
        this.before = new DefaultAssetAdministrationShellElementVisitor() {
            @Override
            public void visit(Referable referable) {
                if (isContainerElement(referable)) {
                    hierarchy.add(referable);
                }
            }
        };
        this.after = new DefaultAssetAdministrationShellElementVisitor() {
            @Override
            public void visit(Referable referable) {
                if (isContainerElement(referable)) {
                    hierarchy.pollLast();
                }
            }
        };
        this.visitor = new DefaultAssetAdministrationShellElementVisitor() {
            @Override
            public void visit(Referable referable) {
                if (referable == null) {
                    return;
                }
                if (hierarchy.isEmpty()) {
                    getIdShortPaths().add(referable.getIdShort());
                    return;
                }
                String path = hierarchy.stream().map(Referable::getIdShort).collect(Collectors.joining(ID_SHORT_PATH_SEPARATOR));
                if (!isContainerElement(referable) && hierarchy.size() < maxDepth) {
                    getIdShortPaths().add(String.format("%s%s%s", path, ID_SHORT_PATH_SEPARATOR, referable.getIdShort()));
                    return;
                }
                if (isContainerElement(referable) && hierarchy.size() <= maxDepth) {
                    getIdShortPaths().add(path);
                }
            }
        };
    }


    public List<String> getIdShortPaths() {
        return idShortPaths;
    }


    @Override
    public void visit(PolicyInformationPoints element) {
        // intentionally left empty
    }


    @Override
    public void visit(AccessControl element) {
        // intentionally left empty
    }


    @Override
    public void visit(SubjectAttributes element) {
        // intentionally left empty
    }


    @Override
    public void visit(ObjectAttributes element) {
        // intentionally left empty
    }


    @Override
    public void visit(Certificate element) {
        // intentionally left empty
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


    private void init() {
        maxDepth = level == Level.CORE ? 2 : Integer.MAX_VALUE;
        hierarchy = new ArrayDeque<>();
        idShortPaths = new ArrayList<>();
    }

}
