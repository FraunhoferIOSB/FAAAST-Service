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

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Paging information.
 */
public class PagingInfo {

    public static final PagingInfo ALL = new PagingInfo();
    public static final long DEFAULT_LIMIT = -1;
    private String cursor;
    private long limit;

    private PagingInfo() {
        this.cursor = null;
        this.limit = DEFAULT_LIMIT;
    }


    public String getCursor() {
        return cursor;
    }


    protected void setCursor(String cursor) {
        this.cursor = cursor;
    }


    public long getLimit() {
        return limit;
    }


    protected void setLimit(long limit) {
        this.limit = limit;
    }


    /**
     * Returns whether a limit is set of no.
     *
     * @return true is limit is set, false otherwise
     */
    public boolean hasLimit() {
        return !Objects.equals(DEFAULT_LIMIT, limit);
    }


    /**
     * Creates a new instance with the provided values.
     *
     * @param cursor the cursor value
     * @param limit the limit value
     * @return a new instance with the provided values
     */
    public static PagingInfo of(String cursor, long limit) {
        return builder()
                .cursor(cursor)
                .limit(limit)
                .build();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PagingInfo other = (PagingInfo) o;
        return Objects.equals(cursor, other.cursor)
                && Objects.equals(limit, other.limit);
    }


    @Override
    public int hashCode() {
        return Objects.hash(cursor, limit);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends PagingInfo, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B cursor(String value) {
            getBuildingInstance().setCursor(value);
            return getSelf();
        }


        public B limit(long value) {
            getBuildingInstance().setLimit(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<PagingInfo, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PagingInfo newBuildingInstance() {
            return new PagingInfo();
        }
    }
}
