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
package de.fraunhofer.iosb.ilt.faaast.service.model.registry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.io.Serializable;
import java.util.Objects;


/**
 * Registry Descriptor for Description.
 */
public class DescriptionDescriptor implements Serializable {

    @JsonIgnore
    private String id;
    private String language;
    private String text;

    public DescriptionDescriptor() {
        id = null;
        language = null;
        text = null;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public String getLanguage() {
        return language;
    }


    public void setLanguage(String language) {
        this.language = language;
    }


    public String getText() {
        return text;
    }


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
        DescriptionDescriptor that = (DescriptionDescriptor) o;
        return Objects.equals(id, that.id)
                && Objects.equals(language, that.language)
                && Objects.equals(text, that.text);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, language, text);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends DescriptionDescriptor, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


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

    public static class Builder extends AbstractBuilder<DescriptionDescriptor, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected DescriptionDescriptor newBuildingInstance() {
            return new DescriptionDescriptor();
        }
    }
}
