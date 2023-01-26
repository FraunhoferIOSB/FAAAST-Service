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
package de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.impl;

import de.fraunhofer.iosb.ilt.faaast.service.model.descriptor.DescriptionDescriptor;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.Objects;


/**
 * Registry Descriptor default implementation for Description.
 */
public class DefaultDescriptionDescriptor implements DescriptionDescriptor {

    private String language;
    private String text;

    public DefaultDescriptionDescriptor() {
        language = null;
        text = null;
    }


    public DefaultDescriptionDescriptor(DescriptionDescriptor source) {
        language = source.getLanguage();
        text = source.getText();
    }


    @Override
    public String getLanguage() {
        return language;
    }


    @Override
    public void setLanguage(String language) {
        this.language = language;
    }


    @Override
    public String getText() {
        return text;
    }


    @Override
    public void setText(String text) {
        this.text = text;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DefaultDescriptionDescriptor that = (DefaultDescriptionDescriptor) o;
        return Objects.equals(language, that.language)
                && Objects.equals(text, that.text);
    }


    @Override
    public int hashCode() {
        return Objects.hash(language, text);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends DefaultDescriptionDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B language(String value) {
            getBuildingInstance().setLanguage(value);
            return getSelf();
        }


        public B text(String value) {
            getBuildingInstance().setText(value);
            return getSelf();
        }


        public B from(LangString langString) {
            if (langString != null) {
                getBuildingInstance().setLanguage(langString.getLanguage());
                getBuildingInstance().setText(langString.getValue());
            }
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<DefaultDescriptionDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected DefaultDescriptionDescriptor newBuildingInstance() {
            return new DefaultDescriptionDescriptor();
        }
    }
}
