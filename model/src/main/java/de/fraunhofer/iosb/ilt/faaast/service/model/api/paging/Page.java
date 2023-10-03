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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.paging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Holds a paged content set including information of further available results.
 */
public class Page<T> {

    private List<T> content;
    private PagingMetadata metadata;

    public Page() {
        this.content = new ArrayList<>();
    }


    public List<T> getContent() {
        return content;
    }


    public void setContent(List<T> content) {
        this.content = content;
    }


    public PagingMetadata getMetadata() {
        return metadata;
    }


    public void setMetadata(PagingMetadata metadata) {
        this.metadata = metadata;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Page<T> other = (Page<T>) o;
        return Objects.equals(content, other.content)
                && Objects.equals(metadata, other.metadata);
    }


    @Override
    public int hashCode() {
        return Objects.hash(content, metadata);
    }


    /**
     * Creates a new {@code Page} with given data but no nextPage.
     *
     * @param <T> type of the data
     * @param data the data
     * @return the new page
     */
    public static <T> Page<T> of(List<T> data) {
        Page<T> result = new Page<>();
        result.setContent(data);
        return result;
    }


    /**
     * Creates a new {@code Page} with given data and nextPage.
     *
     * @param <T> type of the data
     * @param data the data
     * @param metadata
     * @return the new page
     */
    public static <T> Page<T> of(List<T> data, PagingMetadata metadata) {
        Page<T> result = new Page<>();
        result.setContent(data);
        result.setMetadata(metadata);
        return result;
    }


    /**
     * Creates a new {@code Page} with given data but not nextPage.
     *
     * @param <T> type of the data
     * @param data the data
     * @return the new page
     */
    public static <T> Page<T> of(T... data) {
        Page<T> result = new Page<>();
        result.setContent(Arrays.asList(data));
        return result;
    }


    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    private abstract static class AbstractBuilder<C, T extends Page<C>, B extends AbstractBuilder<C, T, B>> extends ExtendableBuilder<T, B> {

        public B result(List<C> value) {
            getBuildingInstance().setContent(value);
            return getSelf();
        }


        public B result(C value) {
            getBuildingInstance().getContent().add(value);
            return getSelf();
        }


        public B metadata(PagingMetadata value) {
            getBuildingInstance().setMetadata(value);
            return getSelf();
        }

    }

    public static class Builder<T> extends AbstractBuilder<T, Page<T>, Builder<T>> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected Page<T> newBuildingInstance() {
            return new Page<>();
        }
    }
}
