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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common;

import io.adminshell.aas.v3.model.builder.ExtendableBuilder;


public abstract class AbstractMultiFormatValueProviderConfig implements MultiFormatValueProviderConfig {

    protected String format;
    protected String template;
    protected String query;

    @Override
    public String getFormat() {
        return format;
    }


    @Override
    public void setFormat(String format) {
        this.format = format;
    }


    @Override
    public String getTemplate() {
        return template;
    }


    @Override
    public void setTemplate(String template) {
        this.template = template;
    }


    @Override
    public String getQuery() {
        return query;
    }


    @Override
    public void setQuery(String query) {
        this.query = query;
    }

    protected abstract static class AbstractBuilder<T extends AbstractMultiFormatValueProviderConfig, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B query(String value) {
            getBuildingInstance().setQuery(value);
            return getSelf();
        }


        public B format(String value) {
            getBuildingInstance().setFormat(value);
            return getSelf();
        }


        public B template(String value) {
            getBuildingInstance().setTemplate(value);
            return getSelf();
        }
    }
}
