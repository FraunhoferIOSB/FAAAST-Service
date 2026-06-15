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

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider.MultiFormatOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.HttpAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.AsyncOperationMode;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Provides the capability to execute operation via HTTP.
 */
public class HttpOperationProvider extends MultiFormatOperationProvider<HttpOperationProviderConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpOperationProvider.class);

    public static final String DEFAULT_EXECUTE_METHOD = "POST";
    private static final int HTTP_ACCEPTED = 202;
    private static final String LOCATION_HEADER = "location";
    private static final String STATUS_INITIATED = "Initiated";
    private static final String STATUS_RUNNING = "Running";
    private static final String STATUS_COMPLETED = "Completed";
    private static final String EXECUTION_STATE_QUERY = "$.executionState";
    private static final String EMPTY = "[empty]";
    private static final long ASYNC_POLL_INTERVAL = 500;
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
        if (reference == null) {
            throw new IllegalArgumentException("reference must be non-null");
        }
        try {
            SubmodelElement element = serviceContext.getPersistence().getSubmodelElement(reference, QueryModifier.DEFAULT);
            if (element == null) {
                throw new ResourceNotFoundException(String.format("reference could not be resolved (reference: %s)", ReferenceHelper.toString(reference)));
            }
            if (!Operation.class.isAssignableFrom(element.getClass())) {
                throw new IllegalArgumentException(String.format("reference points to invalid type (reference: %s, expected type: Operation, actual type: %s)",
                        ReferenceHelper.toString(reference),
                        element.getClass()));
            }
            return ((Operation) element).getOutputVariables().toArray(new OperationVariable[0]);
        }
        catch (ResourceNotFoundException | PersistenceException e) {
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
            String path = variableReplacer.apply(config.getPath());
            String method = StringUtils.isBlank(config.getMethod())
                    ? DEFAULT_EXECUTE_METHOD
                    : config.getMethod();
            Map<String, String> headers = HttpHelper.mergeHeaders(connectionConfig.getHeaders(), config.getHeaders());
            headers = headers.entrySet().stream().collect(Collectors.toMap(Entry::getKey, x -> variableReplacer.apply(x.getValue())));
            LOGGER.trace("Sending HTTP request to asset (baseUrl: {}, path: {}, method: {}, headers: {}, body: {})",
                    connectionConfig.getBaseUrl(),
                    config.getPath(),
                    method,
                    headers,
                    Objects.nonNull(input) ? new String(input) : "");
            byte[] retval = null;
            if (config.getMode() == AsyncOperationMode.DIRECT) {
                HttpResponse<byte[]> response = callOperationAsync(path, method, input, headers);
                if (!HttpHelper.is2xxSuccessful(response)) {
                    throw new AssetConnectionException(String.format("executing operation via HTTP asset connection failed (reference: %s)", ReferenceHelper.toString(reference)));
                }
                retval = response.body();
            }
            if (config.getMode() == AsyncOperationMode.ASYNC_AAS) {
                retval = invokeAsyncAas(path, method, input, headers);
            }

            return retval;
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format("executing operation via HTTP asset connection failed (reference: %s)", ReferenceHelper.toString(reference)), e);
        }
        catch (IOException | URISyntaxException e) {
            throw new AssetConnectionException(String.format("executing operation via HTTP asset connection failed (reference: %s)", ReferenceHelper.toString(reference)), e);
        }
    }


    private byte[] invokeAsyncAas(String path, String method, byte[] input, Map<String, String> headers)
            throws InterruptedException, URISyntaxException, IOException, AssetConnectionException {
        byte[] retval = null;
        HttpResponse<byte[]> responseCall = callOperationAsync(path, method, input, headers);
        if (responseCall.statusCode() == HTTP_ACCEPTED) {
            // extract location header
            if (responseCall.headers().map().containsKey(LOCATION_HEADER) && (!responseCall.headers().map().get(LOCATION_HEADER).isEmpty())) {
                URI locationUri = extractLocationUri(responseCall, path);

                boolean running = true;
                while (running) {
                    HttpResponse<byte[]> responseStatus = callOperationStatus(locationUri,
                            headers);
                    if (responseStatus.body() != null) {
                        String bodyTxt = new String(responseStatus.body());
                        List<Object> jsonPathResult = JsonPath
                                .using(Configuration.defaultConfiguration().addOptions(Option.ALWAYS_RETURN_LIST))
                                .parse(bodyTxt)
                                .read(EXECUTION_STATE_QUERY);
                        if (!jsonPathResult.isEmpty()) {
                            String state = jsonPathResult.get(0).toString();
                            if (state.equalsIgnoreCase(STATUS_INITIATED) || state.equalsIgnoreCase(STATUS_RUNNING)) {
                                running = true;
                            }
                            else {
                                running = false;
                                if (state.equalsIgnoreCase(STATUS_COMPLETED)) {
                                    retval = responseStatus.body();
                                }
                                else {
                                    throw new AssetConnectionException(
                                            String.format("executing operation via HTTP asset connection failed (reference: %s): executionState: %s",
                                                    ReferenceHelper.toString(reference), state));
                                }
                            }
                        }
                        else {
                            throw new AssetConnectionException(String.format(
                                    "executing operation via HTTP asset connection failed: executionState not found (reference: %s)", ReferenceHelper.toString(reference)));
                        }
                    }
                    else {
                        throw new AssetConnectionException(
                                String.format("executing operation via HTTP asset connection failed (reference: %s)", ReferenceHelper.toString(reference)));
                    }
                    Thread.sleep(ASYNC_POLL_INTERVAL);
                }
            }
            else {
                throw new AssetConnectionException(
                        String.format("executing operation via HTTP asset connection failed (reference: %s)", ReferenceHelper.toString(reference)));
            }
        }
        else {
            throw new AssetConnectionException(String.format("executing operation via HTTP asset connection failed (reference: %s)", ReferenceHelper.toString(reference)));
        }
        return retval;
    }


    private HttpResponse<byte[]> callOperationAsync(String path, String method, byte[] input, Map<String, String> headers)
            throws IOException, URISyntaxException, InterruptedException {
        HttpResponse<byte[]> responseCall = HttpHelper.execute(
                client,
                connectionConfig.getBaseUrl(),
                path,
                config.getFormat(),
                method,
                HttpRequest.BodyPublishers.ofByteArray(input),
                HttpResponse.BodyHandlers.ofByteArray(),
                headers);
        LOGGER.trace("Response from asset (status code: {}, headers: {}, body: {})",
                responseCall.statusCode(),
                responseCall.headers().map(),
                responseCall.body() != null ? new String(responseCall.body()) : EMPTY);
        return responseCall;
    }


    private HttpResponse<byte[]> callOperationStatus(URI locationUri, Map<String, String> headers)
            throws AssetConnectionException, IOException, InterruptedException, URISyntaxException {
        HttpResponse<byte[]> responseStatus = HttpHelper.execute(
                client,
                locationUri.toURL(),
                "",
                config.getFormat(),
                "GET",
                HttpRequest.BodyPublishers.noBody(),
                HttpResponse.BodyHandlers.ofByteArray(),
                headers);
        LOGGER.trace("Response from asset status (status code: {}, headers: {}, body: {})",
                responseStatus.statusCode(),
                responseStatus.headers().map(),
                responseStatus.body() != null ? new String(responseStatus.body()) : EMPTY);
        if (!HttpHelper.is2xxSuccessful(responseStatus)) {
            throw new AssetConnectionException(
                    String.format("executing operation via HTTP asset connection failed (reference: %s)", ReferenceHelper.toString(reference)));
        }
        return responseStatus;
    }


    private URI extractLocationUri(HttpResponse<byte[]> responseCall, String path) throws URISyntaxException, MalformedURLException {
        String urlTxt = responseCall.headers().map().get(LOCATION_HEADER).get(0);
        URI locationUri = new URI(urlTxt);
        if (!locationUri.isAbsolute()) {
            // make relative URL absolute
            locationUri = new URL(connectionConfig.getBaseUrl(), path).toURI().resolve(locationUri.toString());
        }
        return locationUri;
    }

}
