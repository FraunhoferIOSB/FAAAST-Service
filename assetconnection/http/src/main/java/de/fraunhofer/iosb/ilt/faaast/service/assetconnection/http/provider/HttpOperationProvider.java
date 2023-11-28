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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider.MultiFormatOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.HttpAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.UnaryOperator;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Provides the capability to execute operation via HTTP.
 */
public class HttpOperationProvider extends MultiFormatOperationProvider<HttpOperationProviderConfig> {

    public static final String DEFAULT_EXECUTE_METHOD = "POST";
    private final ServiceContext serviceContext;
    private final Reference reference;
    private final HttpClient client;
    private final HttpAssetConnectionConfig connectionConfig;

    public HttpOperationProvider(ServiceContext serviceContext,
            Reference reference,
            HttpClient client,
            HttpAssetConnectionConfig connectionConfig,
            HttpOperationProviderConfig config) {
        super(config);
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(client, "client must be non-null");
        Ensure.requireNonNull(connectionConfig, "connectionConfig must be non-null");
        this.client = client;
        this.serviceContext = serviceContext;
        this.reference = reference;
        this.connectionConfig = connectionConfig;
    }


    @Override
    protected OperationVariable[] getOutputParameters() {
        try {
            return serviceContext.getOperationOutputVariables(reference);
        }
        catch (ResourceNotFoundException e) {
            throw new IllegalStateException(
                    String.format(
                            "operation not defined in AAS model (reference: %s)",
                            ReferenceHelper.toString(reference)),
                    e);
        }
    }


    @Override
    protected byte[] invoke(byte[] input, UnaryOperator<String> variableReplacer) throws AssetConnectionException {
        try {
            HttpResponse<byte[]> response = HttpHelper.execute(
                    client,
                    connectionConfig.getBaseUrl(),
                    variableReplacer.apply(config.getPath()),
                    config.getFormat(),
                    StringUtils.isBlank(config.getMethod())
                            ? DEFAULT_EXECUTE_METHOD
                            : config.getMethod(),
                    HttpRequest.BodyPublishers.ofByteArray(input),
                    HttpResponse.BodyHandlers.ofByteArray(),
                    HttpHelper.mergeHeaders(connectionConfig.getHeaders(), config.getHeaders()));
            if (!HttpHelper.is2xxSuccessful(response)) {
                throw new AssetConnectionException(String.format("executing operation via HTTP asset connection failed (reference: %s)", ReferenceHelper.toString(reference)));
            }
            return response.body();
        }
        catch (IOException | URISyntaxException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format("executing operation via HTTP asset connection failed (reference: %s)", ReferenceHelper.toString(reference)), e);
        }
    }
}
