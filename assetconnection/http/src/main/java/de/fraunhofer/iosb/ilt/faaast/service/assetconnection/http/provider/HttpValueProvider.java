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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.content.ContentDeserializerFactory;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.content.ContentSerializerFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class HttpValueProvider implements AssetValueProvider {

    private final HttpValueProviderConfig providerConfig;
    private final Reference reference;
    private final ServiceContext serviceContext;
    private final String serverUri;
    private final HttpClient client;

    /**
     * Creates new instance.
     *
     * @param serverUri serverUri to use, must be non-null
     * @param providerConfig configuration, must be non-null
     * @throws IllegalArgumentException if client is null
     * @throws IllegalArgumentException if providerConfig is null
     */
    public HttpValueProvider(HttpClient client, ServiceContext serviceContext, Reference reference, String serverUri, HttpValueProviderConfig providerConfig) {
        if (serverUri == null) {
            throw new IllegalArgumentException("serverUri must be non-null");
        }
        if (providerConfig == null) {
            throw new IllegalArgumentException("providerConfig must be non-null");
        }
        this.serverUri = serverUri;
        this.providerConfig = providerConfig;
        this.serviceContext = serviceContext;
        this.reference = reference;
        this.client = client;
    }


    @Override
    public DataElementValue getValue() throws AssetConnectionException {
        try {
            HttpRequest request = null;
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(serverUri + providerConfig.getPath()))
                    .header("Content-Type", "application/" + providerConfig.getContentFormat());

            switch (providerConfig.getMethod()) {
                case "GET":
                    request = requestBuilder.GET().build();
                    break;
                case "POST":
                    request = requestBuilder.POST(HttpRequest.BodyPublishers.noBody()).build();
                    break;
                case "PUT":
                    request = requestBuilder.PUT(HttpRequest.BodyPublishers.noBody()).build();
                    break;
            }

            if (request == null) {
                throw new AssetConnectionException(String.format("error reading value from asset conenction (reference: %s)", AasUtils.asString(reference)));
            }
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() > 226) {
                throw new AssetConnectionException(String.format("error reading value from asset conenction (reference: %s)", AasUtils.asString(reference)));
            }
            return ContentDeserializerFactory
                    .create(providerConfig.getContentFormat())
                    .read(response.body(),
                            providerConfig.getQuery(),
                            serviceContext.getTypeInfo(reference));
        }
        catch (IOException | URISyntaxException | InterruptedException e) {
            throw new AssetConnectionException(String.format("error reading value from asset conenction (reference: %s)", AasUtils.asString(reference)), e);
        }
    }


    @Override
    public void setValue(DataElementValue value) throws AssetConnectionException {
        try {
            if (!(value instanceof PropertyValue)) {
                throw new AssetConnectionException(String.format("unsupported value (%s)", value.getClass().getSimpleName()));
            }
            HttpRequest request = null;
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(new URI(serverUri + providerConfig.getPath()))
                    .header("Content-Type", "application/" + providerConfig.getContentFormat());
            String body = ContentSerializerFactory
                    .create(providerConfig.getContentFormat())
                    .write(value, providerConfig.getQuery());

            switch (providerConfig.getMethod()) {
                case "GET":
                    request = requestBuilder.GET().build();
                    break;
                case "POST":
                    request = requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body)).build();
                    break;
                case "PUT":
                    request = requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body)).build();
                    break;
            }

            if (request == null) {
                throw new AssetConnectionException(String.format("error reading value from asset connection (reference: %s)", AasUtils.asString(reference)));
            }
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() > 226) {
                throw new AssetConnectionException(String.format("error reading value from asset connection (reference: %s)", AasUtils.asString(reference)));
            }
        }
        catch (IOException | URISyntaxException | InterruptedException e) {
            throw new AssetConnectionException("writing value via HTTP asset connection failed", e);
        }
    }
}
