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
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Base class for request that suppport
 * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier}.
 *
 * @param <T> actual type of the request
 */
public abstract class RequestWithModifier<T extends Response> extends BaseRequest<T> {

    private OutputModifierConstraints outputModifierConstraints;
    protected OutputModifier outputModifier;

    protected RequestWithModifier() {
        this(OutputModifierConstraints.DEFAULT);
    }


    protected RequestWithModifier(OutputModifierConstraints outputModifierConstraints) {
        this.outputModifier = OutputModifier.DEFAULT;
        this.outputModifierConstraints = outputModifierConstraints == null
                ? OutputModifierConstraints.DEFAULT
                : outputModifierConstraints;
    }


    public OutputModifier getOutputModifier() {
        return outputModifier;
    }


    public void setOutputModifier(OutputModifier outputModifier) {
        this.outputModifier = outputModifier;
    }


    public OutputModifierConstraints getOutputModifierConstraints() {
        return outputModifierConstraints;
    }


    public void setOutputModifierConstraints(OutputModifierConstraints outputModifierConstraints) {
        this.outputModifierConstraints = outputModifierConstraints;
    }


    /**
     * Checks if a given content modifier is supported for this type
     *
     * @param content the content modifier to check
     * @throws IllegalArgumentException if provided content modifier is not
     *             supported
     */
    public void checkContenModifierValid(Content content) {
        if (content != null && !outputModifierConstraints.getSupportedContentModifiers().contains(content)) {
            throw new IllegalArgumentException(String.format("unsupported value for outputModifier.content (actual: %s, supported: %s)",
                    content,
                    outputModifierConstraints.getSupportedContentModifiers().stream()
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
        if (!outputModifierConstraints.getSupportsLevel() && level != null) {
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
        if (!outputModifierConstraints.getSupportsExtent() && extent != null) {
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
                && Objects.equals(outputModifierConstraints, that.outputModifierConstraints);
    }


    @Override
    public int hashCode() {
        return Objects.hash(outputModifier, outputModifierConstraints);
    }

    public abstract static class AbstractBuilder<T extends RequestWithModifier, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B outputModifier(OutputModifier value) {
            getBuildingInstance().setOutputModifier(value);
            return getSelf();
        }


        public B outputModifierConstraints(OutputModifierConstraints value) {
            getBuildingInstance().setOutputModifierConstraints(value);
            return getSelf();
        }
    }

}
