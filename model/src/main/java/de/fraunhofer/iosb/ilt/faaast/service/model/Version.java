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

import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Class representing basic version information.
 */
public class Version {

    public static final Version V3_0 = builder()
            .major(3)
            .minor(0)
            .build();

    public static final Version V3_1 = builder()
            .major(3)
            .minor(1)
            .build();

    private Integer major;
    private Integer minor;
    private Integer patch;
    private String suffix;

    public Version() {
        major = 0;
        minor = null;
        patch = null;
        suffix = null;
    }


    public int getMajor() {
        return major;
    }


    public void setMajor(int major) {
        this.major = major;
    }


    public int getMinor() {
        return minor;
    }


    public void setMinor(int minor) {
        this.minor = minor;
    }


    public int getPatch() {
        return patch;
    }


    public void setPatch(int patch) {
        this.patch = patch;
    }


    public String getSuffix() {
        return suffix;
    }


    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Version that = (Version) o;
        return Objects.equals(major, that.major)
                && Objects.equals(minor, that.minor)
                && Objects.equals(patch, that.patch)
                && Objects.equals(suffix, that.suffix);
    }


    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, suffix);
    }


    @Override
    public String toString() {
        StringBuilder result = new StringBuilder()
                .append("v")
                .append(major);
        if (Objects.nonNull(minor)) {
            result.append(".")
                    .append(minor);
        }
        if (Objects.nonNull(patch)) {
            result.append(".")
                    .append(patch);
        }
        if (patch != null) {
            result.append(".").append(patch);
        }
        if (suffix != null) {
            result.append("-")
                    .append(suffix);
        }
        return result.toString();
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends ExtendableBuilder<Version, Builder> {

        public Builder major(int value) {
            getBuildingInstance().setMajor(value);
            return getSelf();
        }


        public Builder minor(int value) {
            getBuildingInstance().setMinor(value);
            return getSelf();
        }


        public Builder patch(int value) {
            getBuildingInstance().setPatch(value);
            return getSelf();
        }


        public Builder suffix(String value) {
            getBuildingInstance().setSuffix(value);
            return getSelf();
        }


        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected Version newBuildingInstance() {
            return new Version();
        }
    }
}
