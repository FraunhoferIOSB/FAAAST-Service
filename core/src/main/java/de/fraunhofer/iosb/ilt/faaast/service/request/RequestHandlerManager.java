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
package de.fraunhofer.iosb.ilt.faaast.service.request;

import com.google.common.reflect.TypeToken;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.MessageType;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.TypeInstantiationException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestHandler;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Finds available RequestHandlers and handles execution (sync or async)
 */
public class RequestHandlerManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestHandlerManager.class);
    private Map<Class<? extends Request>, ? extends RequestHandler> handlers;
    private ExecutorService requestHandlerExecutorService;
    private final CoreConfig coreConfig;
    private final Persistence persistence;
    private final MessageBus messageBus;
    private final AssetConnectionManager assetConnectionManager;

    public RequestHandlerManager(CoreConfig coreConfig, Persistence persistence, MessageBus messageBus, AssetConnectionManager assetConnectionManager) {
        this.coreConfig = coreConfig;
        this.persistence = persistence;
        this.messageBus = messageBus;
        this.assetConnectionManager = assetConnectionManager;
        init();
    }


    private void init() {
        // TODO implement build-time scan to improve performance (see https://github.com/classgraph/classgraph/wiki/Build-Time-Scanning)
        final Object[] constructorArgs = new Object[] {
                persistence,
                messageBus,
                assetConnectionManager
        };
        final Class<?>[] constructorArgTypes = RequestHandler.class.getConstructors()[0].getParameterTypes();
        try (ScanResult scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(getClass().getPackageName())
                .scan()) {
            // TODO change approach for RequestHandler from abstract class to interface 
            // (either with init method or pass all arguments with handle method)
            handlers = scanResult.getSubclasses(RequestHandler.class).loadClasses().stream()
                    .map(x -> (Class<? extends RequestHandler>) x)
                    .collect(Collectors.toMap(
                            x -> {
                                return (Class<? extends Request>) TypeToken.of(x).resolveType(RequestHandler.class.getTypeParameters()[0]).getRawType();
                            },
                            x -> {
                                try {
                                    Constructor<? extends RequestHandler> constructor = x.getConstructor(constructorArgTypes);
                                    return constructor.newInstance(constructorArgs);
                                }
                                catch (NoSuchMethodException | SecurityException e) {
                                    LOGGER.warn("request handler implementation could not be loaded, "
                                            + "reason: missing constructor (implementation class: {}, required constructor signature: {}",
                                            x.getName(),
                                            constructorArgTypes,
                                            e);
                                }
                                catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                                    LOGGER.warn("request handler implementation could not be loaded, "
                                            + "reason: calling constructor failed (implementation class: {}, constructor arguments: {}",
                                            x.getName(),
                                            constructorArgs,
                                            e);
                                }
                                return null;
                            }));
        }
        // filter out null values from handlers that could not be instantiated so that later we don't need to check for null on each access

        // create request handler executor service 
        requestHandlerExecutorService = Executors.newFixedThreadPool(
                coreConfig.getRequestHandlerThreadPoolSize(),
                new BasicThreadFactory.Builder()
                        .namingPattern("RequestHandler" + "-%d")
                        .build());
    }


    /**
     * Properly shuts down this instance and releases all resources. Do not call
     * any methods on this instance after calling this method.
     */
    public void shutdown() {
        requestHandlerExecutorService.shutdown();
        try {
            if (requestHandlerExecutorService.awaitTermination(2, TimeUnit.SECONDS)) {
                return;
            }
        }
        catch (InterruptedException e) {
            LOGGER.error("Interrupted while waiting for shutdown.", e);
            Thread.currentThread().interrupt();
        }
        LOGGER.warn("RequestHandlerManager stopped with {} unfinished requests.",
                requestHandlerExecutorService.shutdownNow().size());
    }


    /**
     * Executes a request synchroniously.
     *
     * @param <I> type of request/input
     * @param <O> type of response/output
     * @param request the request to execute
     * @return the reponse to this request
     * @throws TypeInstantiationException if response class could not be
     *             instantiated
     * @throws IllegalArgumentException if request is null
     */
    public <I extends Request<O>, O extends Response> O execute(I request) {
        if (request == null) {
            throw new IllegalArgumentException("request must be non-null");
        }
        if (!handlers.containsKey(request.getClass())) {
            return createResponse(request, StatusCode.SERVER_INTERNAL_ERROR, MessageType.EXCEPTION, "no handler defined for this request");
        }
        try {
            return (O) handlers.get(request.getClass()).process(request);
        }
        catch (ResourceNotFoundException e) {
            return createResponse(request, StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND, MessageType.ERROR, e);
        }
        catch (Exception e) {
            return createResponse(request, StatusCode.SERVER_INTERNAL_ERROR, MessageType.EXCEPTION, e);
        }
    }


    private static <I extends Request<O>, O extends Response> O createResponse(I request, StatusCode statusCode, MessageType messageType, Exception e) {
        return createResponse(request, statusCode, messageType, e.getMessage());
    }


    private static <I extends Request<O>, O extends Response> O createResponse(I request, StatusCode statusCode, MessageType messageType, String message) {
        try {
            O response = (O) TypeToken.of(request.getClass()).resolveType(Request.class.getTypeParameters()[0]).getRawType().getConstructor().newInstance();
            response.setStatusCode(statusCode);
            response.getResult().setSuccess(false);
            response.getResult().setMessages(List.of(
                    new Message.Builder()
                            .text(message)
                            .messageType(messageType)
                            .build()));
            return response;
        }
        catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            throw new TypeInstantiationException("executing request failed and failure could not be properly handled", ex);
        }
    }


    /**
     * Executes a request asynchroniously.
     *
     * @param <I> type of request/input
     * @param <O> type of response/output
     * @param request the request to execute
     * @param callback callback handler which is called with the response once
     *            the request has been executed
     */
    public <I extends Request<O>, O extends Response> void executeAsync(I request, Consumer<O> callback) {
        if (request == null) {
            throw new IllegalArgumentException("request must be non-null");
        }
        if (callback == null) {
            throw new IllegalArgumentException("callback must be non-null");
        }
        requestHandlerExecutorService.submit(() -> callback.accept(execute(request)));
    }
}
