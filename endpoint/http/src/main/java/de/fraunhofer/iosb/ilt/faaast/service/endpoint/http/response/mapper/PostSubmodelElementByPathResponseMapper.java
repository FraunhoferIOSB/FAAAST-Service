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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.response.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PostSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PostSubmodelElementByPathResponse;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Response mapper for {@link PostSubmodelElementByPathResponse}.
 */
public class PostSubmodelElementByPathResponseMapper extends AbstractPostResponseWithLocationHeaderMapper<PostSubmodelElementByPathResponse, PostSubmodelElementByPathRequest> {

    public PostSubmodelElementByPathResponseMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    protected String computeLocationHeader(PostSubmodelElementByPathRequest apiRequest, PostSubmodelElementByPathResponse apiResponse) {
        IdShortPath path = IdShortPath.parse(apiRequest.getPath());
        if (path.isEmpty()) {
            return apiResponse.getPayload().getIdShort();
        }
        GetSubmodelElementByPathResponse serviceResponse = serviceContext.execute(GetSubmodelElementByPathRequest.builder()
                .submodelId(apiRequest.getSubmodelId())
                .path(apiRequest.getPath())
                .build());
        SubmodelElement parent = serviceResponse.getPayload();
        if (SubmodelElementList.class.isAssignableFrom(parent.getClass())) {
            return String.format(".%d", ((SubmodelElementList) parent).getValue().size() - 1);
        }
        return String.format(".%s", apiResponse.getPayload().getIdShort());
    }
}
