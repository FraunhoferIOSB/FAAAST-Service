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
package de.fraunhofer.iosb.ilt.faaast.service.persistence;

import java.util.Objects;


/**
 * Paging information.
 */
public class PagingInfo {

    public static final long DEFAULT_SKIP = -1;
    public static final long DEFAULT_LIMIT = -1;
    public static final PagingInfo ALL = new PagingInfo(DEFAULT_SKIP, DEFAULT_LIMIT);
    private long skip;
    private long limit;

    public PagingInfo(long skip, long limit) {
        this.skip = skip;
        this.limit = limit;
    }


    public long getSkip() {
        return skip;
    }


    public long getLimit() {
        return limit;
    }


    public boolean isSkipSet() {
        return !Objects.equals(DEFAULT_SKIP, skip);
    }


    public boolean isLimitSet() {
        return !Objects.equals(DEFAULT_LIMIT, limit);
    }
}
