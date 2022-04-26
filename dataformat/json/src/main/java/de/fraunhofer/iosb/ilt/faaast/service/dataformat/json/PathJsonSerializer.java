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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.json.JsonMapper;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
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


public class PathJsonSerializer {

    public static final String ID_SHORT_PATH_SEPARATOR = ".";
    private final SerializerWrapper wrapper;

    public PathJsonSerializer() {
        this.wrapper = new SerializerWrapper(this::modifyMapper);
    }


    public JsonMapper getMapper() {
        return wrapper.getMapper();
    }


    public String write(Object obj) throws SerializationException {
        return write(obj, Level.DEFAULT);
    }


    private boolean isContainerElement(Referable referable) {
        if (referable == null) {
            return false;
        }
        return AssetAdministrationShell.class.isAssignableFrom(referable.getClass())
                || Submodel.class.isAssignableFrom(referable.getClass())
                || SubmodelElementCollection.class.isAssignableFrom(referable.getClass());
    }


    public String write(Object obj, Level level) throws SerializationException {
        final List<String> idShorts = new ArrayList<>();
        final Deque<Referable> hierarchy = new ArrayDeque<>();
        final int maxDepth = level == Level.CORE ? 2 : Integer.MAX_VALUE;
        new IdShortPathElementWalker.Builder()
                .before(new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(Referable referable) {
                        if (isContainerElement(referable)) {
                            hierarchy.push(referable);
                        }
                    }
                })
                .after(new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(Referable referable) {
                        if (isContainerElement(referable)) {
                            hierarchy.pop();
                        }
                    }
                })
                .visitor(new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(Referable referable) {
                        if (hierarchy.isEmpty()) {
                            idShorts.add(referable.getIdShort());
                            return;
                        }
                        String path = hierarchy.stream().map(Referable::getIdShort).collect(Collectors.joining(ID_SHORT_PATH_SEPARATOR));
                        if (!isContainerElement(referable) && hierarchy.size() < maxDepth) {
                            idShorts.add(String.format("%s%s%s", path, ID_SHORT_PATH_SEPARATOR, referable.getIdShort()));
                            return;
                        }
                        if (isContainerElement(referable) && hierarchy.size() <= maxDepth) {
                            idShorts.add(path);
                        }
                    }
                })
                .build()
                .walk(obj);
        try {
            return wrapper.getMapper().writeValueAsString(idShorts);
        }
        catch (JsonProcessingException e) {
            throw new SerializationException("serialization failed", e);
        }
    }


    protected JsonMapper modifyMapper(JsonMapper mapper) {
        return mapper;
    }

    public static class IdShortPathElementWalker extends AssetAdministrationShellElementWalker {

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

        public static class Builder extends AssetAdministrationShellElementWalker.AbstractBuilder<IdShortPathElementWalker, Builder> {

            @Override
            protected Builder getSelf() {
                return this;
            }


            @Override
            protected IdShortPathElementWalker newBuildingInstance() {
                return new IdShortPathElementWalker();
            }
        }
    }
}
