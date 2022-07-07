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
package de.fraunhofer.iosb.ilt.faaast.service.model.value;

import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class MultiLanguagePropertyValue extends DataElementValue {

    private Set<LangString> langStringSet;

    public MultiLanguagePropertyValue() {
        this.langStringSet = new HashSet<>();
    }


    public MultiLanguagePropertyValue(Set<LangString> langStringSet) {
        this.langStringSet = langStringSet;
    }


    public MultiLanguagePropertyValue(LangString... langStringSet) {
        this.langStringSet = Stream.of(langStringSet).collect(Collectors.toSet());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MultiLanguagePropertyValue that = (MultiLanguagePropertyValue) o;
        return Objects.equals(langStringSet, that.langStringSet);
    }


    public Set<LangString> getLangStringSet() {
        return langStringSet;
    }


    public void setLangStringSet(Set<LangString> langStringSet) {
        this.langStringSet = langStringSet;
    }


    @Override
    public int hashCode() {
        return Objects.hash(langStringSet);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends MultiLanguagePropertyValue, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B values(Set<LangString> value) {
            getBuildingInstance().setLangStringSet(value);
            return getSelf();
        }


        public B value(LangString value) {
            getBuildingInstance().getLangStringSet().add(value);
            return getSelf();
        }


        public B value(String language, String value) {
            LangString langString = new LangString();
            langString.setLanguage(language);
            langString.setValue(value);
            getBuildingInstance().getLangStringSet().add(langString);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<MultiLanguagePropertyValue, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected MultiLanguagePropertyValue newBuildingInstance() {
            return new MultiLanguagePropertyValue();
        }
    }
}
