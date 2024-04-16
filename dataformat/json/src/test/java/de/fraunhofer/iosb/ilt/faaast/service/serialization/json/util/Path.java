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
package de.fraunhofer.iosb.ilt.faaast.service.serialization.json.util;

import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


public class Path {

    private String id = "";
    private List<Path> children = new ArrayList<>();
    private boolean isList = false;

    public List<String> getPaths() {
        List<String> result = new ArrayList<>();
        if (!StringHelper.isBlank(id)) {
            result.add(id);
        }
        result.addAll(isList ? computeBracetsPaths() : computeDotPaths());
        return result;
    }


    private List<String> computeDotPaths() {
        return children.stream().flatMap(x -> x.getPaths().stream()
                .map(y -> StringHelper.isBlank(id) ? y : String.format("%s.%s", id, y)))
                .collect(Collectors.toList());
    }


    private List<String> computeBracetsPaths() {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < children.size(); i++) {
            final int counter = i;
            final Path current = children.get(i);
            result.addAll(builder()
                    .from(current)
                    .id(StringHelper.isBlank(id) ? "[" + counter + "]" : String.format("%s[%d]", id, counter))
                    .build()
                    .getPaths());
        }
        return result;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public boolean getIsList() {
        return isList;
    }


    public void setIsList(boolean isList) {
        this.isList = isList;
    }


    public List<Path> getChildren() {
        return children;
    }


    public void setChildren(List<Path> children) {
        this.children = children;
    }


    public Path asCorePath() {
        return Path.builder()
                .id(id)
                .isList(isList)
                .children(children.stream()
                        .map(x -> Path.builder()
                                .id(x.getId())
                                .isList(x.getIsList())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }


    public static final Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends Path, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B from(Path original) {
            id(original.getId());
            isList(original.getIsList());
            children(original.getChildren().stream()
                    .map(x -> builder().from(x).build())
                    .collect(Collectors.toList()));
            return getSelf();
        }


        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B isList() {
            getBuildingInstance().setIsList(true);
            return getSelf();
        }


        public B isList(boolean value) {
            getBuildingInstance().setIsList(value);
            return getSelf();
        }


        public B child(Path value) {
            getBuildingInstance().getChildren().add(value);
            return getSelf();
        }


        public B child(String value) {
            getBuildingInstance().getChildren().add(Path.builder()
                    .id(value)
                    .build());
            return getSelf();
        }


        public B children(List<Path> value) {
            getBuildingInstance().setChildren(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<Path, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected Path newBuildingInstance() {
            return new Path();
        }

    }

}
