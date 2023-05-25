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
package de.fraunhofer.iosb.ilt.faaast.service.model.request;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.PutAssetAdministrationShellResponse;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Request class for PutAssetAdministrationShell requests.
 */
public class PutAssetAdministrationShellRequest implements Request<PutAssetAdministrationShellResponse> {

    private String id;
    private AssetAdministrationShell aas;

    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    public AssetAdministrationShell getAas() {
        return aas;
    }


    public void setAas(AssetAdministrationShell aas) {
        this.aas = aas;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PutAssetAdministrationShellRequest that = (PutAssetAdministrationShellRequest) o;
        return Objects.equals(aas, that.aas);
    }


    @Override
    public int hashCode() {
        return Objects.hash(id, aas);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends PutAssetAdministrationShellRequest, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B aas(AssetAdministrationShell value) {
            getBuildingInstance().setAas(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<PutAssetAdministrationShellRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PutAssetAdministrationShellRequest newBuildingInstance() {
            return new PutAssetAdministrationShellRequest();
        }
    }

}
