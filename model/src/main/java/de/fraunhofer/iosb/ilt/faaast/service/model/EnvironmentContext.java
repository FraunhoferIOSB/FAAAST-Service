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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.aasx.InMemoryFile;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Wrapper class representing an {@link Environment} and the related files which make up an AASX
 * file.
 */
public class EnvironmentContext {

    private Environment environment;
    private List<InMemoryFile> files;

    public EnvironmentContext() {
        this.files = new ArrayList<>();
    }


    public Environment getEnvironment() {
        return environment;
    }


    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }


    public List<InMemoryFile> getFiles() {
        return files;
    }


    public void setFiles(List<InMemoryFile> files) {
        this.files = files;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EnvironmentContext that = (EnvironmentContext) o;
        return Objects.equals(environment, that.environment)
                && Objects.equals(files, that.files);
    }


    @Override
    public int hashCode() {
        return Objects.hash(environment, files);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends EnvironmentContext, B extends AbstractBuilder<T, B>>
            extends ExtendableBuilder<T, B> {

        public B environment(Environment value) {
            getBuildingInstance().setEnvironment(value);
            return getSelf();
        }


        public B files(InMemoryFile... value) {
            getBuildingInstance().setFiles(new ArrayList<>(Arrays.asList(value)));
            return getSelf();
        }


        public B files(List<InMemoryFile> value) {
            getBuildingInstance().setFiles(value);
            return getSelf();
        }


        public B file(InMemoryFile value) {
            getBuildingInstance().getFiles().add(value);
            return getSelf();
        }


        public B file(byte[] content, String path) {
            getBuildingInstance().getFiles().add(new InMemoryFile(content, path));
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<EnvironmentContext, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected EnvironmentContext newBuildingInstance() {
            return new EnvironmentContext();
        }
    }

}
