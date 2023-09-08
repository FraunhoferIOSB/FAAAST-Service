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
package de.fraunhofer.iosb.ilt.faaast.service.model;

import java.util.Arrays;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Abstract base class for storing file content information.
 */
public abstract class AbstractFileContent {

    protected byte[] content;

    public String getPath() {
        return path;
    }


    public void setPath(String path) {
        this.path = path;
    }

    private String path;

    public byte[] getContent() {
        return content;
    }


    public void setContent(byte[] content) {
        this.content = content;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractFileContent that = (AbstractFileContent) o;
        return Arrays.equals(content, that.content);
    }


    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(content));
    }

    public abstract static class AbstractBuilder<T extends AbstractFileContent, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B content(byte[] value) {
            getBuildingInstance().setContent(value);
            return getSelf();
        }


        public B path(String value) {
            getBuildingInstance().setPath(value);
            return getSelf();
        }
    }
}
