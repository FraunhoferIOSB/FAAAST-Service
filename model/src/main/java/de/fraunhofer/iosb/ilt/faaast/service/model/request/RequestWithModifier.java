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
package de.fraunhofer.iosb.ilt.faaast.service.model.request;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.BaseRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public abstract class RequestWithModifier<T extends Response> extends BaseRequest<T> {

    public static final Set<Content> DEFAULT_SUPPORT_CONTENT_MODIFIERS = Set.of(Content.values());
    public static final boolean DEFAULT_SUPPORT_EXTENT = true;
    public static final boolean DEFAULT_SUPPORT_LEVEL = false;
    protected OutputModifier outputModifier;
    protected Set<Content> supportedContentModifiers;
    protected boolean supportsExtent;
    protected boolean supportsLevel;

    protected RequestWithModifier() {
        this(DEFAULT_SUPPORT_EXTENT, DEFAULT_SUPPORT_LEVEL, DEFAULT_SUPPORT_CONTENT_MODIFIERS);
    }


    protected RequestWithModifier(Content... supportedContentModifiers) {
        this(DEFAULT_SUPPORT_EXTENT, DEFAULT_SUPPORT_LEVEL, supportedContentModifiers);
    }


    protected RequestWithModifier(boolean supportsExtent, boolean supportsLevel) {
        this(supportsExtent, supportsLevel, DEFAULT_SUPPORT_CONTENT_MODIFIERS);
    }


    protected RequestWithModifier(boolean supportsExtent, boolean supportsLevel, Content... supportedContentModifiers) {
        this(supportsExtent, supportsLevel,
                supportedContentModifiers != null
                        ? new HashSet<>(Arrays.asList(supportedContentModifiers))
                        : new HashSet<>());
    }


    protected RequestWithModifier(boolean supportsExtent, boolean supportsLevel, Set<Content> supportedContentModifiers) {
        this.outputModifier = OutputModifier.DEFAULT;
        this.supportsExtent = supportsExtent;
        this.supportsLevel = supportsLevel;
        this.supportedContentModifiers = supportedContentModifiers != null
                ? supportedContentModifiers
                : new HashSet<>();
    }


    public OutputModifier getOutputModifier() {
        return outputModifier;
    }


    public void setOutputModifier(OutputModifier outputModifier) {
        this.outputModifier = outputModifier;
    }


    /**
     * Checks if a given content modifier is supported for this type
     *
     * @param content the content modifier to check
     * @throws IllegalArgumentException if provided content modifier is not
     *             supported
     */
    public void checkContenModifierValid(Content content) {
        if (content != null && !supportedContentModifiers.contains(content)) {
            throw new IllegalArgumentException(String.format("unsupported value for outputModifier.content (actual: %s, supported: %s)",
                    content,
                    supportedContentModifiers.stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(", "))));
        }
    }


    /**
     * Checks if a given level modifier is supported for this type
     *
     * @param level the level modifier to check
     * @throws IllegalArgumentException if provided level modifier is not
     *             supported
     */
    public void checkLevelModifierValid(Level level) {
        if (!supportsLevel && level != null) {
            throw new IllegalArgumentException("outputModifier.level not supported for this request");
        }

    }


    /**
     * Checks if a given extent modifier is supported for this type
     *
     * @param extent the extent modifier to check
     * @throws IllegalArgumentException if provided extent modifier is not
     *             supported
     */
    public void checkExtentModifierValid(Extent extent) {
        if (!supportsExtent && extent != null) {
            throw new IllegalArgumentException("outputModifier.extent not supported for this request");
        }

    }


    /**
     * Validates if current output modifier violates the constraints.
     * 
     * @throws IllegalArgumentException if there is any violation
     */
    public void validate() {
        checkContenModifierValid(outputModifier.getContent());
        checkLevelModifierValid(outputModifier.getLevel());
        checkExtentModifierValid(outputModifier.getExtent());
    }

    public abstract static class AbstractBuilder<T extends RequestWithModifier, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B outputModifier(OutputModifier value) {
            getBuildingInstance().setOutputModifier(value);
            return getSelf();
        }


        public B supportedContentModifiers(Content... value) {
            getBuildingInstance().supportedContentModifiers = value != null
                    ? new HashSet<>(Arrays.asList(value))
                    : new HashSet<>();
            return getSelf();
        }


        public B supportedContentModifiers(Set<Content> value) {
            getBuildingInstance().supportedContentModifiers = value != null
                    ? value
                    : new HashSet<>();
            return getSelf();
        }


        public B supportsExtent(boolean value) {
            getBuildingInstance().supportsExtent = value;
            return getSelf();
        }


        public B supportsLevel(boolean value) {
            getBuildingInstance().supportsLevel = value;
            return getSelf();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestWithModifier that = (RequestWithModifier) o;
        return Objects.equals(outputModifier, that.outputModifier)
                && Objects.equals(supportedContentModifiers, that.supportedContentModifiers)
                && Objects.equals(supportsExtent, that.supportsExtent)
                && Objects.equals(supportsLevel, that.supportsLevel);
    }


    @Override
    public int hashCode() {
        return Objects.hash(outputModifier, supportedContentModifiers, supportsExtent, supportsLevel);
    }
}
