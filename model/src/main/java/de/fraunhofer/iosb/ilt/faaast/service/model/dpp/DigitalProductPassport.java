/*
 * Copyright 2026 Fraunhofer IOSB.
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

package de.fraunhofer.iosb.ilt.faaast.service.model.dpp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * This class represents a DPP as defined by CEN/CENELEC JTC24 and DIN EN 18221 but represented via AAS elements.
 */
public class DigitalProductPassport {
    private AssetAdministrationShell aas;
    private Submodel metadata;
    private List<Submodel> contents;

    public DigitalProductPassport() {
        this.contents = new ArrayList<>();
    }


    public AssetAdministrationShell getAAS() {
        return aas;
    }


    public void setAAS(AssetAdministrationShell aas) {
        this.aas = aas;
    }


    public Submodel getMetadata() {
        return metadata;
    }


    public void setMetadata(Submodel metadata) {
        this.metadata = metadata;
    }


    public List<Submodel> getContents() {
        return contents;
    }


    public void setContents(List<Submodel> contents) {
        this.contents = contents;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DigitalProductPassport other = (DigitalProductPassport) o;
        return Objects.equals(aas, other.aas)
                && Objects.equals(metadata, other.metadata)
                && Objects.equals(contents, other.contents);
    }


    @Override
    public int hashCode() {
        return Objects.hash(aas, metadata, contents);
    }


    @Override
    public String toString() {
        return "DigitalProductPassport{" +
                "aas=" + aas +
                ", metadata=" + metadata +
                ", contents=" + contents +
                '}';
    }


    public static Builder builder() {
        return new Builder();

    }

    public abstract static class AbstractBuilder<T extends DigitalProductPassport, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B aas(AssetAdministrationShell value) {
            getBuildingInstance().setAAS(value);
            return getSelf();
        }


        public B metadata(Submodel value) {
            getBuildingInstance().setMetadata(value);
            return getSelf();
        }


        public B content(Submodel value) {
            getBuildingInstance().getContents().add(value);
            return getSelf();
        }


        public B contents(List<Submodel> value) {
            getBuildingInstance().setContents(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<DigitalProductPassport, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected DigitalProductPassport newBuildingInstance() {
            return new DigitalProductPassport();
        }

    }

}
