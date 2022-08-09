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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import org.apache.maven.shared.utils.StringUtils;


/**
 * Provides the capability to read and write values via HTTP
 */
public class HttpValueProvider extends MultiFormatValueProvider<HttpValueProviderConfig> {

    private static final String BASE_ERROR_MESSAGE = "error reading value from asset conenction (reference: %s)";
    public static final String DEFAULT_READ_METHOD = "GET";
    public static final String DEFAULT_WRITE_METHOD = "PUT";
    private final ServiceContext serviceContext;
    private final Reference reference;
    private final HttpClient client;
    private final URL baseUrl;

    public HttpValueProvider(ServiceContext serviceContext, Reference reference, HttpClient client, URL baseUrl, HttpValueProviderConfig config) {
        super(config);
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(client, "client must be non-null");
        Ensure.requireNonNull(baseUrl, "baseUrl must be non-null");
        this.serviceContext = serviceContext;
        this.reference = reference;
        this.client = client;
        this.baseUrl = baseUrl;
    }


    @Override
    public byte[] getRawValue() throws AssetConnectionException {
        try {
            HttpResponse<byte[]> response = HttpHelper.execute(
                    client,
                    baseUrl,
                    config.getPath(),
                    config.getFormat(),
                    DEFAULT_READ_METHOD,
                    BodyPublishers.noBody(),
                    BodyHandlers.ofByteArray());
            if (!HttpHelper.is2xxSuccessful(response)) {
                throw new AssetConnectionException(String.format(BASE_ERROR_MESSAGE, AasUtils.asString(reference)));
            }
            return response.body();
        }
        catch (IOException | InterruptedException | URISyntaxException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format(BASE_ERROR_MESSAGE, AasUtils.asString(reference)), e);
        }
    }


    @Override
    public void setRawValue(byte[] value) throws AssetConnectionException {
        try {
            HttpResponse<String> response = HttpHelper.execute(
                    client,
                    baseUrl,
                    config.getPath(),
                    config.getFormat(),
                    StringUtils.isBlank(config.getWriteMethod())
                            ? DEFAULT_WRITE_METHOD
                            : config.getWriteMethod(),
                    BodyPublishers.ofByteArray(value),
                    BodyHandlers.ofString());
            if (!HttpHelper.is2xxSuccessful(response)) {
                throw new AssetConnectionException(String.format(BASE_ERROR_MESSAGE, AasUtils.asString(reference)));
            }
        }
        catch (IOException | URISyntaxException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException("writing value via HTTP asset connection failed", e);
        }
    }


    @Override
    protected TypeInfo getTypeInfo() {
        return serviceContext.getTypeInfo(reference);
    }
}
