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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier;

import java.util.Objects;


/**
 * Model class for search options.
 */
public class SearchOptions {

    private Depth depth;

    public SearchOptions() {
        this.depth = Depth.RECURSIVE;
    }


    public Depth getDepth() {
        return depth;
    }


    public void setDepth(Depth depth) {
        this.depth = depth;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SearchOptions that = (SearchOptions) o;
        return depth == that.depth;
    }


    @Override
    public int hashCode() {
        return Objects.hash(depth);
    }
}
