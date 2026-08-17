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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.dpp.request;

import static de.fraunhofer.iosb.ilt.faaast.service.model.DPP.DPP_1;
import static org.mockito.Mockito.mock;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.dpp.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.dpp.serialization.HttpJsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp.ReadDppByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp.ReadDppByProductIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.dpp.ReadDppIdsByProductIdsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DppSerializationMode;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;


public class RequestMappingManagerTest {

    private final RequestMappingManager mappingManager;
    private final HttpJsonApiSerializer serializer;
    private final ServiceContext serviceContext;

    public RequestMappingManagerTest() {
        serializer = new HttpJsonApiSerializer();
        serviceContext = mock(ServiceContext.class);
        mappingManager = new RequestMappingManager(serviceContext);
    }


    @Test
    public void testReadDPPById() throws InvalidRequestException {
        String dppId = DPP_1.getAAS().getId();
        Request expected = ReadDppByIdRequest.builder()
                .id(dppId)
                .dppSerializationMode(DppSerializationMode.DEFAULT)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("v1/dpps/" + EncodingHelper.base64UrlEncode(dppId))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testReadDPPByProductId() throws InvalidRequestException {
        String productId = "http://example.org/productId";
        Request expected = ReadDppByProductIdRequest.builder()
                .id(productId)
                .dppSerializationMode(DppSerializationMode.DEFAULT)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.GET)
                .path("v1/dppsByProductId/" + EncodingHelper.base64UrlEncode(productId))
                .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testReadDPPIdsByProductIds() throws InvalidRequestException, SerializationException {
        List<String> productIds = List.of("http://example.org/productId1", "http://example.org/productId2");
        Request expected = ReadDppIdsByProductIdsRequest.builder()
                .productIds(productIds)
                .build();
        Request actual = mappingManager.map(HttpRequest.builder()
                .method(HttpMethod.POST)
                .path("v1/dppsByProductIds")
                .body(serializer.write(productIds))
                .build());
        Assert.assertEquals(expected, actual);
    }
}
