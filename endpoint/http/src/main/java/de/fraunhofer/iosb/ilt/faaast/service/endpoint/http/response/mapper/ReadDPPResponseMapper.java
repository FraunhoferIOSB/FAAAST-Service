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

import static de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content.VALUE;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.dpp.DppSerializationMode;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.dpp.JsonDppSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp.AbstractDPPRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.dpp.AbstractDPPResponse;
import jakarta.servlet.http.HttpServletResponse;


public class ReadDPPResponseMapper extends ResponseWithPayloadResponseMapper<AbstractDPPResponse, AbstractDPPRequest<AbstractDPPResponse>> {
    public ReadDPPResponseMapper(ServiceContext serviceContext) {
        super(serviceContext);
    }


    @Override
    public void map(AbstractDPPRequest<AbstractDPPResponse> dppRequest, AbstractDPPResponse dppResponse, HttpServletResponse httpResponse) throws Exception {
        HttpHelper.sendJson(httpResponse,
                dppResponse.getStatusCode(),
                new JsonDppSerializer().write(
                        dppResponse.getPayload(),
                        VALUE == dppRequest.getOutputModifier().getContent() ? DppSerializationMode.EXPANDED : DppSerializationMode.COMPRESSED));
    }
}
