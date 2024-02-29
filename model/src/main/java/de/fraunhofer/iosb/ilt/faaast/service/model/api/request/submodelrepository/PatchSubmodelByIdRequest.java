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
package de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository;

import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodelrepository.PatchSubmodelByIdResponse;
import de.fraunhofer.iosb.ilt.faaast.service.util.EqualsHelper;
import java.util.Objects;


/**
 * Request class for PatchSubmodelById requests.
 */
public class PatchSubmodelByIdRequest extends Request<PatchSubmodelByIdResponse> {

    private String id;
    private JsonMergePatch changes;

    public JsonMergePatch getChanges() {
        return changes;
    }


    public void setChanges(JsonMergePatch changes) {
        this.changes = changes;
    }


    public String getId() {
        return id;
    }


    public void setId(String id) {
        this.id = id;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PatchSubmodelByIdRequest that = (PatchSubmodelByIdRequest) o;
        return super.equals(that)
                && Objects.equals(id, that.id)
                && EqualsHelper.equals(changes, that.changes);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, changes);
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends PatchSubmodelByIdRequest, B extends AbstractBuilder<T, B>> extends Request.AbstractBuilder<T, B> {

        public B id(String value) {
            getBuildingInstance().setId(value);
            return getSelf();
        }


        public B changes(JsonMergePatch value) {
            getBuildingInstance().setChanges(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<PatchSubmodelByIdRequest, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected PatchSubmodelByIdRequest newBuildingInstance() {
            return new PatchSubmodelByIdRequest();
        }
    }
}
