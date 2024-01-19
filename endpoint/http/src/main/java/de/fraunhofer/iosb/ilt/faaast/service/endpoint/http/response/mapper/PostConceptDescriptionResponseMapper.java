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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.PostConceptDescriptionRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.conceptdescription.PostConceptDescriptionResponse;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;


/**
 * Response mapper for {@link PostConceptDescriptionResponse}.
 */
public class PostConceptDescriptionResponseMapper extends AbstractPostResponseWithLocationHeaderMapper<PostConceptDescriptionResponse, PostConceptDescriptionRequest> {

    public PostConceptDescriptionResponseMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    protected String computeLocationHeader(PostConceptDescriptionRequest apiRequest, PostConceptDescriptionResponse apiResponse) {
        return String.format("/%s", EncodingHelper.base64UrlEncode(apiResponse.getPayload().getId()));
    }
}
