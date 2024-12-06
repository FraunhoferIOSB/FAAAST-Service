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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http;

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.MethodNotAllowedException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.util.MostSpecificClassComparator;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import jakarta.servlet.ServletException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.Result;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultResult;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes.Type;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * HTTP handler that is called to render error pages.
 */
public class HttpErrorHandler extends ErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpErrorHandler.class);
    private static final Map<Class<?>, StatusCode> exceptionToStatusCode = Map.of(
            MethodNotAllowedException.class, StatusCode.CLIENT_METHOD_NOT_ALLOWED,
            InvalidRequestException.class, StatusCode.CLIENT_ERROR_BAD_REQUEST,
            ResourceNotFoundException.class, StatusCode.CLIENT_ERROR_RESOURCE_NOT_FOUND);
    private final HttpEndpointConfig config;

    public HttpErrorHandler(HttpEndpointConfig config) {
        this.config = config;
    }


    private static Throwable findRealCause(Throwable cause) {
        if (Objects.isNull(cause)) {
            return null;
        }
        if (isWellKnown(cause)) {
            return cause;
        }
        if (cause instanceof ServletException
                && Objects.nonNull(cause.getCause())
                && isWellKnown(cause.getCause())) {
            return cause.getCause();
        }
        return cause;
    }


    private static boolean isWellKnown(Throwable cause) {
        return getStatus(cause).isPresent();
    }


    private static Optional<StatusCode> getStatus(Throwable cause) {
        Optional<Class<?>> key = exceptionToStatusCode.keySet().stream()
                .filter(x -> x.isAssignableFrom(cause.getClass()))
                .sorted(Comparator.comparing(x -> x, new MostSpecificClassComparator()))
                .findFirst();
        if (key.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(exceptionToStatusCode.get(key.get()));
    }


    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        LOGGER.debug("handle error (request: {}, response: {}, callback: {})", request, response, callback);
        Throwable cause = findRealCause((Throwable) request.getAttribute(ERROR_EXCEPTION));
        StatusCode statusCode = StatusCode.SERVER_INTERNAL_ERROR;
        if (Objects.nonNull(cause) && isWellKnown(cause)) {
            statusCode = getStatus(cause).get();
        }
        send(response, statusCode, cause, callback);
        return true;
    }


    private void send(Response response, StatusCode statusCode, Throwable cause, Callback callback) {
        Result result = new DefaultResult.Builder()
                .messages(Message.builder()
                        .messageType(HttpHelper.messageTypeFromstatusCode(statusCode))
                        .text(Objects.nonNull(cause) && !StringHelper.isEmpty(cause.getMessage())
                                ? cause.getMessage()
                                : HttpStatus.getMessage(HttpHelper.toHttpStatusCode(statusCode)))
                        .build())
                .build();
        if (config.isIncludeErrorDetails() && Objects.nonNull(cause)) {
            result.getMessages().add(Message.builder()
                    .messageType(MessageTypeEnum.EXCEPTION)
                    .text(getStacktrace(
                            cause))
                    .build());
        }
        response.setStatus(HttpHelper.toHttpStatusCode(statusCode));
        try {
            sendJson(response, new HttpJsonApiSerializer().write(result), callback);
        }
        catch (Exception e) {
            sendJson(response, getFallbackResponseJson(), callback);
        }
    }


    private void sendJson(Response response, String json, Callback callback) {
        byte[] payload = json.getBytes(StandardCharsets.UTF_8);
        response.getHeaders().put(HttpHeader.CONTENT_TYPE, Type.APPLICATION_JSON_UTF_8.asString());
        response.getHeaders().put(HttpHeader.CONTENT_LENGTH, payload.length);
        response.write(true, ByteBuffer.wrap(payload), callback);
        callback.succeeded();
    }


    private static String getStacktrace(Throwable cause) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        cause.printStackTrace(printWriter);
        return stringWriter.toString();
    }


    private static String getFallbackResponseJson() {
        return String.format(
                """
                        {
                          "messages" : [ {
                            "messageType" : "Error",
                            "text" : "Server Error",
                            "timestamp" : "%s"
                          } ]
                        }
                        """,
                DateTimeFormatter.ISO_DATE_TIME.format(LocalDate.now()));
    }

}
