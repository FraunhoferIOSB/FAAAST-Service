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

import de.fraunhofer.iosb.ilt.faaast.service.util.ObjectHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultLangStringTextType;


/**
 * Value class for MultiLanguageProperty.
 */
public class MultiLanguagePropertyValue extends DataElementValue {

    private List<LangStringTextType> langStringSet;

    public MultiLanguagePropertyValue() {
        this.langStringSet = new ArrayList<>();
    }


    public MultiLanguagePropertyValue(List<LangStringTextType> langStringSet) {
        this.langStringSet = langStringSet;
    }


    public MultiLanguagePropertyValue(LangStringTextType... langStringSet) {
        this.langStringSet = Stream.of(langStringSet).collect(Collectors.toList());
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
        return ObjectHelper.equalsIgnoreOrder(langStringSet, that.langStringSet);
    }


    public List<LangStringTextType> getLangStringSet() {
        return langStringSet;
    }


    public void setLangStringSet(List<LangStringTextType> langStringSet) {
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

        public B values(List<LangStringTextType> value) {
            getBuildingInstance().setLangStringSet(value);
            return getSelf();
        }


        public B value(LangStringTextType value) {
            getBuildingInstance().getLangStringSet().add(value);
            return getSelf();
        }


        public B value(String language, String value) {
            LangStringTextType langString = new DefaultLangStringTextType();
            langString.setLanguage(language);
            langString.setText(value);
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
