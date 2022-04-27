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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class Path {

    private String id;
    private List<Path> children = new ArrayList<>();

    public List<String> getPaths() {
        List<String> result = new ArrayList<>();
        result.add(id);
        result.addAll(
                children.stream().flatMap(x -> x.getPaths().stream()
                        .map(y -> String.format("%s.%s", id, y)))
                        .collect(Collectors.toList()));
        return result;
    }


    public Path asCorePath() {
        return Path.builder()
                .id(id)
                .children(children.stream()
                        .map(x -> Path.builder()
                                .id(x.getId())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }


    public static final Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Path instance = new Path();

        public Builder id(String value) {
            instance.id = value;
            return this;
        }


        public Builder child(Path value) {
            instance.getChildren().add(value);
            return this;
        }


        public Builder child(String value) {
            instance.getChildren().add(Path.builder()
                    .id(value)
                    .build());
            return this;
        }


        public Builder children(List<Path> value) {
            instance.children = value;
            return this;
        }


        public Path build() {
            return instance;
        }
    }

    public String getId() {
        return id;
    }


    public List<Path> getChildren() {
        return children;
    }
}
