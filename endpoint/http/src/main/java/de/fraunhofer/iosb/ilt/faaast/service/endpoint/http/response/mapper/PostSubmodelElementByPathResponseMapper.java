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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractRequestWithModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.PostSubmodelElementByPathRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.PostSubmodelElementByPathResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Response mapper for {@link PostSubmodelElementByPathResponse}.
 */
public class PostSubmodelElementByPathResponseMapper extends AbstractResponseMapper<PostSubmodelElementByPathResponse> {

    public PostSubmodelElementByPathResponseMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    public void map(Request<PostSubmodelElementByPathResponse> apiRequest, PostSubmodelElementByPathResponse apiResponse, HttpServletResponse httpResponse) {
        PostSubmodelElementByPathRequest request = ((PostSubmodelElementByPathRequest) apiRequest);
        IdShortPath path = IdShortPath.parse(request.getPath());
        if (path.isEmpty()) {
            httpResponse.addHeader("Location", String.format("/%s", apiResponse.getPayload().getIdShort()));
        }
        else {
            Reference parentReference = new ReferenceBuilder()
                    .submodel(request.getSubmodelId())
                    .idShortPath(path)
                    .build();
            try {
                SubmodelElement parent = EnvironmentHelper.resolve(parentReference, serviceContext.getAASEnvironment(), SubmodelElement.class);
                if (SubmodelElementList.class.isAssignableFrom(parent.getClass())) {
                    httpResponse.addHeader("Location", String.format(".%d", ((SubmodelElementList) parent).getValue().size() - 1));
                }
                else {
                    httpResponse.addHeader("Location", String.format(".%s", apiResponse.getPayload().getIdShort()));
                }
            }
            catch (ResourceNotFoundException ex) {
                httpResponse.addHeader("Location", String.format("/%s", apiResponse.getPayload().getIdShort()));
            }
        }

        try {
            HttpHelper.sendJson(httpResponse,
                    apiResponse.getStatusCode(),
                    new HttpJsonApiSerializer().write(
                            apiResponse.getPayload(),
                            AbstractRequestWithModifier.class.isAssignableFrom(apiRequest.getClass())
                                    ? ((AbstractRequestWithModifier) apiRequest).getOutputModifier()
                                    : OutputModifier.DEFAULT));
        }
        catch (SerializationException e) {
            HttpHelper.send(httpResponse, StatusCode.SERVER_INTERNAL_ERROR, Result.exception(e.getMessage()));
        }
    }

}
