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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Allows defining constaints on which {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier}
 * are supported/accepted. By default, 'extent' is supported while 'level' is not supported.
 */
public class OutputModifierConstraints {

    private boolean supportsExtent;
    private boolean supportsLevel;
    private Set<Content> supportedContentModifiers;
    public static final OutputModifierConstraints DEFAULT = new OutputModifierConstraints(true, false, Content.values());
    public static final OutputModifierConstraints NONE = new OutputModifierConstraints(false, false);
    public static final OutputModifierConstraints SUBMODEL = new OutputModifierConstraints(true, true, Content.NORMAL, Content.VALUE, Content.PATH,
            Content.METADATA);
    public static final OutputModifierConstraints SUBMODEL_ELEMENT = new OutputModifierConstraints(true, true, Content.NORMAL, Content.VALUE, Content.PATH,
            Content.METADATA);
    public static final OutputModifierConstraints ASSET_ADMINISTRATION_SHELL = new OutputModifierConstraints(false, false, Content.NORMAL);

    public OutputModifierConstraints() {
        this.supportsExtent = true;
        this.supportsLevel = false;
        this.supportedContentModifiers = new HashSet<>();
    }


    private OutputModifierConstraints(boolean supportsExtent, boolean supportsLevel, Content... supportedContentModifiers) {
        this.supportsExtent = supportsExtent;
        this.supportsLevel = supportsLevel;
        this.supportedContentModifiers = Objects.nonNull(supportedContentModifiers) && supportedContentModifiers.length > 0
                ? new HashSet<>(Arrays.asList(supportedContentModifiers))
                : new HashSet<>(List.of(Content.DEFAULT));
    }


    public boolean getSupportsExtent() {
        return supportsExtent;
    }


    public boolean getSupportsLevel() {
        return supportsLevel;
    }


    public Set<Content> getSupportedContentModifiers() {
        return supportedContentModifiers;
    }


    public void setSupportsExtent(boolean supportsExtent) {
        this.supportsExtent = supportsExtent;
    }


    public void setSupportsLevel(boolean supportsLevel) {
        this.supportsLevel = supportsLevel;
    }


    public void setSupportedContentModifiers(Set<Content> supportedContentModifiers) {
        this.supportedContentModifiers = supportedContentModifiers;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OutputModifierConstraints that = (OutputModifierConstraints) o;
        return Objects.equals(supportedContentModifiers, that.supportedContentModifiers)
                && Objects.equals(supportsExtent, that.supportsExtent)
                && Objects.equals(supportsLevel, that.supportsLevel);
    }


    @Override
    public int hashCode() {
        return Objects.hash(supportedContentModifiers, supportsExtent, supportsLevel);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends OutputModifierConstraints, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B supportsExtent(boolean value) {
            getBuildingInstance().setSupportsExtent(value);
            return getSelf();
        }


        public B supportsLevel(boolean value) {
            getBuildingInstance().setSupportsLevel(value);
            return getSelf();
        }


        public B supportsContentModifier(Content value) {
            getBuildingInstance().getSupportedContentModifiers().add(value);
            return getSelf();
        }


        public B supportedContentModifiers(Content... value) {
            getBuildingInstance().setSupportedContentModifiers(value != null
                    ? new HashSet<>(Arrays.asList(value))
                    : new HashSet<>());
            return getSelf();
        }


        public B supportedContentModifiers(Set<Content> value) {
            getBuildingInstance().setSupportedContentModifiers(value != null
                    ? value
                    : new HashSet<>());
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<OutputModifierConstraints, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected OutputModifierConstraints newBuildingInstance() {
            return new OutputModifierConstraints();
        }

    }

}
