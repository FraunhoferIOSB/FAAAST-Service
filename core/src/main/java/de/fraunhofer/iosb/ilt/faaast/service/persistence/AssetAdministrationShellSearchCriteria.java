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
package de.fraunhofer.iosb.ilt.faaast.service.persistence;

import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Search criteria for finding {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}.
 */
public class AssetAdministrationShellSearchCriteria {

    public static final AssetAdministrationShellSearchCriteria NONE = new AssetAdministrationShellSearchCriteria();

    private static final String DEFAULT_ID_SHORT = null;
    private static final List<AssetIdentification> DEFAULT_ASSET_IDS = new ArrayList<>();

    private String idShort;
    private List<AssetIdentification> assetIds;

    public AssetAdministrationShellSearchCriteria() {
        this.idShort = DEFAULT_ID_SHORT;
        this.assetIds = DEFAULT_ASSET_IDS;
    }


    public String getIdShort() {
        return idShort;
    }


    public void setIdShort(String idShort) {
        this.idShort = idShort;
    }


    public boolean isIdShortSet() {
        return !Objects.equals(idShort, DEFAULT_ID_SHORT);
    }


    public List<AssetIdentification> getAssetIds() {
        return assetIds;
    }


    public void setAssetIds(List<AssetIdentification> assetIds) {
        this.assetIds = assetIds;
    }


    public boolean isAssetIdsSet() {
        return !Objects.equals(assetIds, DEFAULT_ASSET_IDS);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AssetAdministrationShellSearchCriteria other = (AssetAdministrationShellSearchCriteria) o;
        return Objects.equals(idShort, other.idShort)
                && Objects.equals(assetIds, other.assetIds);
    }


    @Override
    public int hashCode() {
        return Objects.hash(idShort, assetIds);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends AssetAdministrationShellSearchCriteria, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B idShort(String value) {
            getBuildingInstance().setIdShort(value);
            return getSelf();
        }


        public B assetIds(List<AssetIdentification> value) {
            getBuildingInstance().setAssetIds(value);
            return getSelf();
        }


        public B assetId(AssetIdentification value) {
            getBuildingInstance().getAssetIds().add(value);
            return getSelf();
        }

    }

    public static class Builder extends AbstractBuilder<AssetAdministrationShellSearchCriteria, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected AssetAdministrationShellSearchCriteria newBuildingInstance() {
            return new AssetAdministrationShellSearchCriteria();
        }
    }
}
