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
package de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object;

/**
 * An access object referencing a route.
 *
 * <p>A route is a URL path matching rule. It must not be null or blank. A lone {@code *} matches all routes. Wildcards
 * ({@code *}) may be used as prefix or suffix to match any number of path segments (e.g. {@code /shells/...} or
 * {@code /shells/12345/*}).
 *
 * @param route the route
 */
public record RouteObject(String route) implements AccessObject {

    public RouteObject {
        if (route == null || route.isBlank()) {
            throw new IllegalArgumentException("route must not be null or blank");
        }
        if (!route.startsWith("/") && !route.contains("*")) {
            throw new IllegalArgumentException("route must be a URL path (starting with '/') or contain a '*' wildcard, but was: " + route);
        }
    }


    @Override
    public boolean isRoute() {
        return true;
    }


    @Override
    public RouteObject asRoute() {
        return this;
    }
}
