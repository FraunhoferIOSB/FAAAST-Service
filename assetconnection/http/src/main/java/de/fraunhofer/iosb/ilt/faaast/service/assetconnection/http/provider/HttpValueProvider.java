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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider.MultiFormatValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.HttpAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides the capability to read and write values via HTTP.
 */
public class HttpValueProvider extends MultiFormatValueProvider<HttpValueProviderConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpValueProvider.class);
    private static final String BASE_ERROR_MESSAGE = "error reading value from asset conenction (reference: %s)";
    public static final String DEFAULT_READ_METHOD = "GET";
    public static final String DEFAULT_WRITE_METHOD = "PUT";
    private final ServiceContext serviceContext;
    private final Reference reference;
    private final HttpClient client;
    private final HttpAssetConnectionConfig connectionConfig;

    public HttpValueProvider(
            ServiceContext serviceContext,
            Reference reference,
            HttpClient client,
            HttpAssetConnectionConfig connectionConfig,
            HttpValueProviderConfig config) {
        super(config);
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(client, "client must be non-null");
        Ensure.requireNonNull(connectionConfig, "connectionConfig must be non-null");
        this.serviceContext = serviceContext;
        this.reference = reference;
        this.client = client;
        this.connectionConfig = connectionConfig;
    }


    @Override
    public byte[] getRawValue() throws AssetConnectionException {
        try {
            Map<String, String> headers = HttpHelper.mergeHeaders(connectionConfig.getHeaders(), config.getHeaders());
            LOGGER.trace("Sending HTTP read request to asset (baseUrl: {}, path: {}, method: {}, headers: {})",
                    connectionConfig.getBaseUrl(),
                    config.getPath(),
                    DEFAULT_READ_METHOD,
                    headers);
            HttpResponse<byte[]> response = HttpHelper.execute(
                    client,
                    connectionConfig.getBaseUrl(),
                    config.getPath(),
                    config.getFormat(),
                    DEFAULT_READ_METHOD,
                    BodyPublishers.noBody(),
                    BodyHandlers.ofByteArray(),
                    headers);
            LOGGER.trace("Response from asset (status code: {}, body{}, method: {}, headers: {})",
                    response.statusCode(),
                    response.body() != null ? new String(response.body()) : "[empty]",
                    DEFAULT_READ_METHOD,
                    headers);
            if (!HttpHelper.is2xxSuccessful(response)) {
                throw new AssetConnectionException(String.format(BASE_ERROR_MESSAGE, ReferenceHelper.toString(reference)));
            }
            return response.body();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format(BASE_ERROR_MESSAGE, ReferenceHelper.toString(reference)), e);
        }
        catch (IOException | URISyntaxException e) {
            throw new AssetConnectionException(String.format(BASE_ERROR_MESSAGE, ReferenceHelper.toString(reference)), e);
        }
    }


    @Override
    public void setRawValue(byte[] value) throws AssetConnectionException {
        try {
            Map<String, String> headers = HttpHelper.mergeHeaders(connectionConfig.getHeaders(), config.getHeaders());
            LOGGER.trace("Sending HTTP write request to asset (baseUrl: {}, path: {}, method: {}, headers: {})",
                    connectionConfig.getBaseUrl(),
                    config.getPath(),
                    DEFAULT_READ_METHOD,
                    headers);
            HttpResponse<String> response = HttpHelper.execute(
                    client,
                    connectionConfig.getBaseUrl(),
                    config.getPath(),
                    config.getFormat(),
                    StringUtils.isBlank(config.getWriteMethod())
                            ? DEFAULT_WRITE_METHOD
                            : config.getWriteMethod(),
                    BodyPublishers.ofByteArray(value),
                    BodyHandlers.ofString(),
                    headers);
            LOGGER.trace("Response from asset (status code: {}, body{}, method: {}, headers: {})",
                    response.statusCode(),
                    response.body() != null ? response.body() : "[empty]",
                    DEFAULT_READ_METHOD,
                    headers);
            if (!HttpHelper.is2xxSuccessful(response)) {
                throw new AssetConnectionException(String.format(BASE_ERROR_MESSAGE, ReferenceHelper.toString(reference)));
            }
        }
        catch (IOException | URISyntaxException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException("writing value via HTTP asset connection failed", e);
        }
    }


    @Override
    protected TypeInfo getTypeInfo() {
        try {
            return serviceContext.getTypeInfo(reference);
        }
        catch (ResourceNotFoundException e) {
            throw new IllegalStateException(String.format(
                    "HTTP value provider could not get typ info as resource does not exist - this should not be able to occur (reference: %s)",
                    ReferenceHelper.toString(reference)),
                    e);
        }
    }
}
