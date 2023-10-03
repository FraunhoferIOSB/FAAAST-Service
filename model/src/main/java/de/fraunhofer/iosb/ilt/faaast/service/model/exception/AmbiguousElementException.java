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
package de.fraunhofer.iosb.ilt.faaast.service.model.exception;

import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Indicates that an element is ambiguous, i.e. cannot be uniquely identified.
 */
public class AmbiguousElementException extends Exception {

    private static final String BASE_MSG = "Ambiguous element";

    public AmbiguousElementException(Referable referable, List<Reference> references) {
        super(String.format("%s (referable: %s, found matching references: %s)",
                BASE_MSG,
                referable,
                references.stream()
                        .map(ReferenceHelper::toString)
                        .collect(Collectors.joining(", "))));
    }

}
