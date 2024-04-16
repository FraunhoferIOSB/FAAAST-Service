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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasbasicdiscovery;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractRequestWithId;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.aasbasicdiscovery.PostAllAssetLinksByIdResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;


/**
 * Request class for PostAllAssetLinksById requests.
 */
public class PostAllAssetLinksByIdRequest extends AbstractRequestWithId<PostAllAssetLinksByIdResponse> {

    private List<SpecificAssetId> assetLinks;

    public PostAllAssetLinksByIdRequest() {
        this.assetLinks = new ArrayList<>();
    }


    public List<SpecificAssetId> getAssetLinks() {
        return assetLinks;
    }


    public void setAssetLinks(List<SpecificAssetId> assetLinks) {
        this.assetLinks = assetLinks;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PostAllAssetLinksByIdRequest that = (PostAllAssetLinksByIdRequest) o;
        return super.equals(that)
                && Objects.equals(assetLinks, that.assetLinks);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), assetLinks);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends PostAllAssetLinksByIdRequest, B extends AbstractBuilder<T, B>> extends AbstractRequestWithId.AbstractBuilder<T, B> {

        public B assetLink(SpecificAssetId value) {
            getBuildingInstance().getAssetLinks().add(value);
            return getSelf();
        }


        public B assetLinks(List<SpecificAssetId> value) {
            getBuildingInstance().setAssetLinks(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<PostAllAssetLinksByIdRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PostAllAssetLinksByIdRequest newBuildingInstance() {
            return new PostAllAssetLinksByIdRequest();
        }
    }
}
