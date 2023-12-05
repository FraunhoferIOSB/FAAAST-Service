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
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Model class for query modifier.
 */
public class QueryModifier {

    public static final QueryModifier DEFAULT = new QueryModifier();
    public static final QueryModifier MINIMAL = new Builder()
            .level(Level.CORE)
            .extend(Extent.WITHOUT_BLOB_VALUE)
            .build();
    protected Level level;
    protected Extent extent;

    protected QueryModifier() {
        this.level = Level.DEEP;
        this.extent = Extent.WITHOUT_BLOB_VALUE;
    }


    public Level getLevel() {
        return level;
    }


    public Extent getExtent() {
        return extent;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueryModifier that = (QueryModifier) o;
        return Objects.equals(level, that.level)
                && Objects.equals(extent, that.extent);
    }


    @Override
    public int hashCode() {
        return Objects.hash(level, extent);
    }

    public abstract static class AbstractBuilder<T extends QueryModifier, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B level(Level value) {
            getBuildingInstance().level = value;
            return getSelf();
        }


        public B extend(Extent value) {
            getBuildingInstance().extent = value;
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<QueryModifier, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected QueryModifier newBuildingInstance() {
            return new QueryModifier();
        }
    }
}
