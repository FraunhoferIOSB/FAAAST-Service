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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.file;

import de.fraunhofer.iosb.ilt.faaast.service.persistence.PersistenceConfig;
import java.nio.file.Path;
import java.util.Objects;


/**
 * Configuration class for {@link PersistenceFile}.
 */
public class PersistenceFileConfig extends PersistenceConfig<PersistenceFile> {

    private static final String DEFAULT_BASE_PATH = Path.of("").toAbsolutePath().toString();

    private String destination;

    private static final boolean DEFAULT_OVERRIDE_ORIGINAL_FILE = false;

    private boolean overrideOriginalModelFile;

    private static final boolean DEFAULT_LOAD_ORIGINAL_FILE = false;

    private boolean loadOriginalFileOnStartUp;

    public PersistenceFileConfig() {
        this.destination = DEFAULT_BASE_PATH;
        this.overrideOriginalModelFile = DEFAULT_OVERRIDE_ORIGINAL_FILE;
        this.loadOriginalFileOnStartUp = DEFAULT_LOAD_ORIGINAL_FILE;
    }


    public String getDestination() {
        return destination;
    }


    public void setDestination(String destination) {
        this.destination = destination;
    }


    public boolean isOverrideOriginalModelFile() {
        return overrideOriginalModelFile;
    }


    public void setOverrideOriginalModelFile(boolean overrideOriginalModelFile) {
        this.overrideOriginalModelFile = overrideOriginalModelFile;
    }


    public boolean isLoadOriginalFileOnStartUp() {
        return loadOriginalFileOnStartUp;
    }


    public void setLoadOriginalFileOnStartUp(boolean loadOriginalFileOnStartUp) {
        this.loadOriginalFileOnStartUp = loadOriginalFileOnStartUp;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PersistenceFileConfig other = (PersistenceFileConfig) obj;
        if (!Objects.equals(this.destination, other.destination)) {
            return false;
        }
        if (!Objects.equals(this.overrideOriginalModelFile, other.overrideOriginalModelFile)) {
            return false;
        }
        return Objects.equals(this.loadOriginalFileOnStartUp, other.loadOriginalFileOnStartUp);
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(this);
    }


    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends PersistenceFileConfig, B extends AbstractBuilder<T, B>>
            extends PersistenceConfig.AbstractBuilder<PersistenceFile, T, B> {

        public B overrideOriginalModelFile(boolean overrideOriginalModelFile) {
            getBuildingInstance().setOverrideOriginalModelFile(overrideOriginalModelFile);
            return getSelf();
        }


        public B destination(String destination) {
            getBuildingInstance().setDestination(destination);
            return getSelf();
        }


        public B loadOriginalFileOnStartup(boolean loadOriginalFileOnStartup) {
            getBuildingInstance().setLoadOriginalFileOnStartUp(loadOriginalFileOnStartup);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<PersistenceFileConfig, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PersistenceFileConfig newBuildingInstance() {
            return new PersistenceFileConfig();
        }
    }

}
