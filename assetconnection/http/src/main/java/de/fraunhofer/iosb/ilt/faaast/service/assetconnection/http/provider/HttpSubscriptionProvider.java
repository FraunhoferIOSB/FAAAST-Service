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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider.MultiFormatSubscriptionProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpSubscriptionProviderConfig;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides the capability to "subscribe" to en element via HTTP. This is done
 * via periodic polling.
 */
public class HttpSubscriptionProvider extends MultiFormatSubscriptionProvider<HttpSubscriptionProviderConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpSubscriptionProvider.class);
    public static final String DEFAULT_METHOD = "GET";
    public static final long MINIMUM_INTERVAL = 100;
    private final ServiceContext serviceContext;
    private final Reference reference;
    private final HttpClient client;
    private final URL baseUrl;
    private ScheduledExecutorService executor;
    private ScheduledFuture<?> executorHandler;

    public HttpSubscriptionProvider(ServiceContext serviceContext, Reference reference, HttpClient client, URL baseUrl, HttpSubscriptionProviderConfig config) {
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


    private byte[] readRawValue() throws AssetConnectionException {
        try {
            HttpResponse<byte[]> response = HttpHelper.execute(
                    client,
                    baseUrl,
                    config.getPath(),
                    config.getFormat(),
                    DEFAULT_METHOD,
                    StringUtils.isBlank(config.getPayload())
                            ? BodyPublishers.noBody()
                            : BodyPublishers.ofString(config.getPayload()),
                    BodyHandlers.ofByteArray());
            if (!HttpHelper.is2xxSuccessful(response)) {
                throw new AssetConnectionException(String.format("error reading value from asset conenction (reference: %s)", AasUtils.asString(reference)));
            }
            return response.body();
        }
        catch (IOException | InterruptedException | URISyntaxException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format("error reading value from asset conenction (reference: %s)", AasUtils.asString(reference)), e);
        }
    }


    @Override
    protected void subscribe() throws AssetConnectionException {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newScheduledThreadPool(0);
            executorHandler = executor.scheduleAtFixedRate(() -> {
                try {
                    fireNewDataReceived(readRawValue());
                }
                catch (AssetConnectionException e) {
                    LOGGER.error("error subscribing to asset connection (reference: {})", AasUtils.asString(reference), e);
                }
            }, 0, Math.max(MINIMUM_INTERVAL, config.getInterval()), TimeUnit.MILLISECONDS);
        }
    }


    @Override
    protected void unsubscribe() throws AssetConnectionException {
        if (executorHandler != null) {
            executorHandler.cancel(true);
        }
        if (executor != null) {
            executor.shutdown();
        }
    }


    @Override
    protected TypeInfo getTypeInfo() {
        return serviceContext.getTypeInfo(reference);
    }

}
