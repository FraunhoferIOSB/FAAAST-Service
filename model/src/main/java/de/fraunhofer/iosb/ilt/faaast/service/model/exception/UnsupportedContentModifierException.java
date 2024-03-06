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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;


/**
 * Indicates usage of unsupported content modifier.
 */
public class UnsupportedContentModifierException extends UnsupportedModifierException {

    public UnsupportedContentModifierException(Content contentModifier, Content... supportedContentModifiers) {
        this(contentModifier, Arrays.asList(supportedContentModifiers));
    }


    public UnsupportedContentModifierException(String contentModifier, Content... supportedContentModifiers) {
        this(contentModifier, Arrays.asList(supportedContentModifiers));
    }


    public UnsupportedContentModifierException(Content contentModifier, Collection<Content> supportedContentModifiers) {
        this(contentModifier.name(), supportedContentModifiers);
    }


    public UnsupportedContentModifierException(String contentModifier, Collection<Content> supportedContentModifiers) {
        super(String.format("unsupported content modifier '%s' (supported content modifiers: %s)",
                contentModifier,
                supportedContentModifiers.stream()
                        .map(Enum::name)
                        .map(String::toLowerCase)
                        .distinct()
                        .sorted()
                        .collect(Collectors.joining(", "))));
    }
}
