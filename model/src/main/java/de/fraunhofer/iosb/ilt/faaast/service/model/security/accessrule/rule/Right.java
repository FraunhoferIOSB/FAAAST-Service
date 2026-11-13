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
package de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.rule;

import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.GET;
import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.PATCH;
import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.POST;
import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.PUT;

import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Enumeration of the possible access rights.
 */
public enum Right {
    CREATE(POST, PUT),
    CREATE_UPDATE(POST, PATCH, PUT),
    READ(GET),
    UPDATE(PATCH, PUT),
    DELETE(HttpMethod.DELETE),
    EXECUTE(),
    VIEW(GET),
    ALL();

    private final Set<HttpMethod> httpMethods;

    Right(HttpMethod... httpMethods) {
        this.httpMethods = new HashSet<>(Arrays.asList(httpMethods));
    }


    /**
     * Returns whether this Right maps to this HttpMethod.
     * 
     * @param method the http method
     * @return True if right maps to the http method.
     */
    public boolean mapsTo(HttpMethod method) {
        return this == ALL || httpMethods.contains(method);
    }


    /**
     * Returns list of Rights that may map to the given http method.
     * 
     * @param method The http method.
     * @return The list of applying rights.
     */
    public static List<Right> from(HttpMethod method) {
        return Arrays.stream(Right.values()).filter(right -> right.mapsTo(method)).toList();
    }
}
