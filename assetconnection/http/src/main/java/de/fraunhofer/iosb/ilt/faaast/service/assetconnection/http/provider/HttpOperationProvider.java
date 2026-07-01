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

import static org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState.CANCELED;
import static org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState.COMPLETED;
import static org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState.FAILED;
import static org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState.TIMEOUT;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.common.provider.MultiFormatOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.HttpAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.AsyncOperationMode;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider.config.HttpOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.util.HttpConstants;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.response.submodel.GetOperationAsyncStatusResponse;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.OperationProviderHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;
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

    private static final String INVOKE_OPERATION_ASYNC_AAS_MSG = "Invoking HTTP asset operation provider with ASYNC AAS pattern";
    public static final String DEFAULT_EXECUTE_METHOD = HttpConstants.METHOD_POST;
    private final ServiceContext serviceContext;
    private final Reference reference;
    private final HttpClient client;
    private final HttpAssetConnectionConfig connectionConfig;
    private final ScheduledExecutorService asyncExecutor = Executors.newScheduledThreadPool(1, x -> {
        Thread t = new Thread(x, "operation-provider-async-executor");
        t.setDaemon(true);
        return t;
    });

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
    public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
        if (config.getMode() == AsyncOperationMode.ASYNC_AAS) {
            throw new AssetConnectionException("Operation provider with mode ASYNC_AAS can only be invoked asynchronuously");
        }
        return super.invoke(input, inoutput);
    }


    @Override
    protected byte[] invoke(byte[] input, UnaryOperator<String> variableReplacer, Consumer<Message> callbackProgress) throws AssetConnectionException {
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
            return switch (config.getMode()) {
                case DIRECT -> invokeDirect(path, method, input, headers);
                case ASYNC_AAS -> invokeAsyncAas(path, method, input, headers, callbackProgress);
                default -> throw new IllegalArgumentException("unsupported HTTP operation invocation mode: " + config.getMode());
            };
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException(String.format("executing operation via HTTP asset connection failed (reference: %s)", ReferenceHelper.toString(reference)), e);
        }
        catch (IOException | URISyntaxException e) {
            throw new AssetConnectionException(String.format("executing operation via HTTP asset connection failed (reference: %s)", ReferenceHelper.toString(reference)), e);
        }
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


    private byte[] invokeDirect(String path, String method, byte[] input, Map<String, String> headers)
            throws AssetConnectionException, IOException, URISyntaxException, InterruptedException {
        HttpResponse<byte[]> response = HttpHelper.execute(
                client,
                connectionConfig.getBaseUrl(),
                path,
                config.getFormat(),
                method,
                HttpRequest.BodyPublishers.ofByteArray(input),
                HttpResponse.BodyHandlers.ofByteArray(),
                headers);
        LOGGER.trace("Response from asset (status code: {}, headers: {}, body: {})",
                response.statusCode(),
                response.headers().map(),
                response.body());
        if (!HttpHelper.is2xxSuccessful(response)) {
            throw new AssetConnectionException(String.format("executing operation via HTTP asset connection failed (reference: %s)", ReferenceHelper.toString(reference)));
        }
        return response.body();
    }


    private byte[] invokeAsyncAas(String path, String method, byte[] input, Map<String, String> headers, Consumer<Message> callbackProgress)
            throws InterruptedException, URISyntaxException, IOException, AssetConnectionException {
        URI statusUri = invokeAsyncAasCall(path, method, input, headers);
        URI resultUri = invokeAsyncAasStatus(statusUri, headers, callbackProgress);
        return invokeAsyncAasResult(resultUri, headers);
    }


    private URI invokeAsyncAasCall(String path, String method, byte[] input, Map<String, String> headers)
            throws IOException, AssetConnectionException {
        HttpResponse<String> response = null;
        try {
            response = HttpHelper.execute(
                    client,
                    connectionConfig.getBaseUrl(),
                    path,
                    config.getFormat(),
                    method,
                    HttpRequest.BodyPublishers.ofByteArray(input),
                    HttpResponse.BodyHandlers.ofString(),
                    headers);
        }
        catch (URISyntaxException | InterruptedException e) {
            throw new AssetConnectionException(String.format(
                    "%s - failed to invoke asset (reference: %s, reason: %s)",
                    INVOKE_OPERATION_ASYNC_AAS_MSG,
                    ReferenceHelper.toString(reference),
                    e.getMessage()),
                    e);
        }
        LOGGER.trace("{} - response from asset upon invoke (status code: {}, headers: {}, body: {})",
                INVOKE_OPERATION_ASYNC_AAS_MSG,
                response.statusCode(),
                response.headers().map(),
                response.body());
        if (response.statusCode() != HttpConstants.STATUS_ACCEPTED) {
            throw new AssetConnectionException(String.format(
                    "%s - asset returned invalid status code upon invoke (expected: %d, actual: %d, reference: %s, body: %s)",
                    INVOKE_OPERATION_ASYNC_AAS_MSG,
                    HttpConstants.STATUS_ACCEPTED,
                    response.statusCode(),
                    ReferenceHelper.toString(reference),
                    response.body()));
        }
        if (!response.headers().map().containsKey(HttpConstants.HEADER_LOCATION) || response.headers().map().get(HttpConstants.HEADER_LOCATION).isEmpty()) {
            throw new AssetConnectionException(String.format(
                    "%s - asset did not return location header upon invoke (reference: %s, status code: %d, body: %s)",
                    INVOKE_OPERATION_ASYNC_AAS_MSG,
                    HttpConstants.STATUS_ACCEPTED,
                    response.statusCode(),
                    ReferenceHelper.toString(reference),
                    response.body()));
        }
        return extractLocationUri(response);
    }


    private URI invokeAsyncAasStatus(URI statusUri, Map<String, String> headers, Consumer<Message> callbackProgress) throws AssetConnectionException, InterruptedException {
        CompletableFuture<URI> future = new CompletableFuture<>();
        AtomicReference<ExecutionState> lastKnownState = new AtomicReference<>(ExecutionState.INITIATED);

        ScheduledFuture<?> statusQuery = asyncExecutor.scheduleWithFixedDelay(() -> {
            try {
                HttpResponse<String> response = callOperationStatus(statusUri, headers);
                if (response.statusCode() == HttpConstants.STATUS_FOUND) {
                    URI resultUri = extractLocationUri(response);
                    LOGGER.trace("async operation returned 302 indicated it is finished (reference: {}, result URI: {})",
                            ReferenceHelper.asString(reference),
                            resultUri);
                    future.complete(resultUri);
                    return;
                }
                GetOperationAsyncStatusResponse status = new JsonApiDeserializer().read(response.body(), GetOperationAsyncStatusResponse.class);
                ExecutionState currentState = status.getPayload().getExecutionState();
                if (lastKnownState.get() != currentState) {
                    LOGGER.trace("async operation state changed from {} to {} (reference: {})",
                            lastKnownState.get(), currentState, ReferenceHelper.asString(reference));
                    lastKnownState.set(currentState);
                }
                switch (currentState) {
                    case CANCELED, FAILED, TIMEOUT ->
                        future.completeExceptionally(new AssetConnectionException(String.format(
                                "executing operation via HTTP asset connection failed (reference: %s): executionState: %s",
                                ReferenceHelper.toString(reference),
                                currentState)));
                    case COMPLETED -> {
                        // keep pulling until there is a redirect
                    }
                    case RUNNING -> {
                        status.getPayload().getMessages().stream()
                                .filter(OperationProviderHelper::isProgressMessage)
                                .forEach(x -> callbackProgress.accept(Message.of(x)));
                    }
                    default -> {
                        // do nothing
                    }
                }
            }
            catch (Exception e) {
                future.completeExceptionally(e);
            }
        }, 0, config.getAsyncPollInterval(), TimeUnit.MILLISECONDS);
        future.whenComplete((result, e) -> statusQuery.cancel(false));

        try {
            return future.get();
        }
        catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof AssetConnectionException) {
                throw (AssetConnectionException) cause;
            }
            throw new AssetConnectionException(String.format(
                    "%s - fetching status failed (reference: %s, reason: %s)",
                    INVOKE_OPERATION_ASYNC_AAS_MSG,
                    ReferenceHelper.toString(reference),
                    cause.getMessage()),
                    cause);
        }
    }


    private byte[] invokeAsyncAasResult(URI resultUri, Map<String, String> headers) throws AssetConnectionException {
        HttpResponse<byte[]> responseResult;
        try {
            responseResult = HttpHelper.execute(
                    client,
                    resultUri.toURL(),
                    "",
                    config.getFormat(),
                    "GET",
                    HttpRequest.BodyPublishers.noBody(),
                    HttpResponse.BodyHandlers.ofByteArray(),
                    headers);
        }
        catch (IOException | URISyntaxException | InterruptedException e) {
            throw new AssetConnectionException(String.format(
                    "%s - failed to fetch asset result (reference: %s, URI: %s, reason: %s)",
                    INVOKE_OPERATION_ASYNC_AAS_MSG,
                    ReferenceHelper.toString(reference),
                    resultUri,
                    e.getMessage()),
                    e);
        }
        LOGGER.trace("Response from asset result (status code: {}, headers: {}, body: {})",
                responseResult.statusCode(),
                responseResult.headers().map(),
                responseResult.body());

        if (responseResult.statusCode() != HttpConstants.STATUS_OK) {
            throw new AssetConnectionException(String.format(
                    "executing operation via HTTP asset connection failed (reference: %s)",
                    ReferenceHelper.toString(reference)));
        }
        return responseResult.body();
    }


    private HttpResponse<String> callOperationStatus(URI locationUri, Map<String, String> headers)
            throws AssetConnectionException, IOException, InterruptedException, URISyntaxException {
        HttpResponse<String> response = HttpHelper.execute(
                client,
                locationUri.toURL(),
                "",
                config.getFormat(),
                "GET",
                HttpRequest.BodyPublishers.noBody(),
                HttpResponse.BodyHandlers.ofString(),
                headers);
        LOGGER.trace("Response from asset status (status code: {}, headers: {}, body: {})",
                response.statusCode(),
                response.headers().map(),
                response.body());
        if (!HttpHelper.is2xxSuccessful(response)) {
            throw new AssetConnectionException(
                    String.format("executing operation via HTTP asset connection failed (reference: %s)", ReferenceHelper.toString(reference)));
        }
        return response;
    }


    private URI extractLocationUri(HttpResponse<?> response) throws AssetConnectionException {
        try {
            String locationHeader = response.headers().map().get(HttpConstants.HEADER_LOCATION).get(0);
            URI locationUri = new URI(locationHeader);
            if (!locationUri.isAbsolute()) {
                // make relative URL absolute
                locationUri = response.request().uri().resolve(locationUri.toString());
            }
            return locationUri;
        }
        catch (URISyntaxException e) {
            throw new AssetConnectionException(String.format("failed to extract location header (reason: %s)", e.getMessage()), e);
        }
    }

}
