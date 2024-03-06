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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util;

import com.google.common.net.MediaType;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.MessageType;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class providing HTTP specific functionality.
 */
public class HttpHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHelper.class);

    private HttpHelper() {}


    /**
     * Convert protocol-agnostic status code to HTTP-specific status code.
     *
     * @param statusCode protocol-agnostic status code
     * @return HTTP-specific status code
     * @throws IllegalArgumentException if status code cannot be mapped to HTTP
     */
    public static int toHttpStatusCode(StatusCode statusCode) {
        switch (statusCode) {
            case SUCCESS:
                return HttpStatus.OK_200;
            case SUCCESS_CREATED:
                return HttpStatus.CREATED_201;
            case SUCCESS_ACCEPTED:
                return HttpStatus.ACCEPTED_202;
            case SUCCESS_NO_CONTENT:
                return HttpStatus.NO_CONTENT_204;
            case SUCCESS_FOUND:
                return HttpStatus.FOUND_302;
            case CLIENT_ERROR_BAD_REQUEST:
                return HttpStatus.BAD_REQUEST_400;
            case CLIENT_NOT_AUTHORIZED:
                return HttpStatus.UNAUTHORIZED_401;
            case CLIENT_FORBIDDEN:
                return HttpStatus.FORBIDDEN_403;
            case CLIENT_ERROR_RESOURCE_NOT_FOUND:
                return HttpStatus.NOT_FOUND_404;
            case CLIENT_METHOD_NOT_ALLOWED:
                return HttpStatus.METHOD_NOT_ALLOWED_405;
            case CLIENT_RESOURCE_CONFLICT:
                return HttpStatus.CONFLICT_409;
            case SERVER_INTERNAL_ERROR:
                return HttpStatus.INTERNAL_SERVER_ERROR_500;
            case SERVER_NOT_IMPLEMENTED:
                return HttpStatus.NOT_IMPLEMENTED_501;
            case SERVER_ERROR_BAD_GATEWAY:
                return HttpStatus.BAD_GATEWAY_502;
            default:
                throw new IllegalArgumentException(String.format("unsupported status code '%s'", statusCode.name()));
        }
    }


    /**
     * Parses a comma-separated list. Also trims white space on all entries.
     *
     * @param input the comma-separated list
     * @return the parsed list
     */
    public static List<String> parseCommaSeparatedList(String input) {
        return Stream.of(input.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }


    /**
     * Converts a {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode} to a
     * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.MessageType}.
     *
     * @param statusCode the input {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.StatusCode}
     * @return the resulting {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.MessageType}
     */
    public static MessageType messageTypeFromstatusCode(StatusCode statusCode) {
        if (statusCode.isError()) {
            return MessageType.ERROR;
        }
        if (statusCode.isException()) {
            return MessageType.EXCEPTION;
        }
        return MessageType.INFO;
    }


    /**
     * Sends a HTTP response with given statusCode and corresponding HTTP status code as message.
     *
     * @param response HTTP response object
     * @param statusCode the statusCode to send
     */
    public static void send(HttpServletResponse response, StatusCode statusCode) {
        send(response,
                statusCode,
                Result.builder()
                        .message(messageTypeFromstatusCode(statusCode), HttpStatus.getMessage(HttpHelper.toHttpStatusCode(statusCode)))
                        .build());
    }


    /**
     * Sends a HTTP response with given statusCode and result.
     *
     * @param response HTTP response object
     * @param statusCode statusCode to send
     * @param result the result to send
     */
    public static void send(HttpServletResponse response, StatusCode statusCode, Result result) {
        try {
            sendJson(response, statusCode, new HttpJsonApiSerializer().write(result));
        }
        catch (SerializationException e) {
            sendContent(response, StatusCode.SERVER_INTERNAL_ERROR, null, null);
        }
    }


    /**
     * Sends a HTTP response with given statusCode and JSON payload.
     *
     * @param response HTTP response object
     * @param statusCode statusCode to send
     * @param content JSON payload
     */
    public static void sendJson(HttpServletResponse response, StatusCode statusCode, String content) {
        sendContent(response,
                statusCode,
                content != null
                        ? content.getBytes(StandardCharsets.UTF_8)
                        : null,
                MediaType.JSON_UTF_8);
    }


    /**
     * Sends a HTTP response with given statusCode, payload and contentType.
     *
     * @param response HTTP response object
     * @param statusCode statusCode to send
     * @param content the content to send
     * @param contentType the contentType to use
     * @throws IllegalArgumentException if response is null
     * @throws IllegalArgumentException if statusCode is null
     */
    public static void sendContent(HttpServletResponse response, StatusCode statusCode, byte[] content, MediaType contentType) {
        sendContent(response, statusCode, content, contentType, null);
    }


    /**
     * Sends a HTTP response with given statusCode, payload and contentType.
     *
     * @param response HTTP response object
     * @param statusCode statusCode to send
     * @param content the content to send
     * @param contentType the contentType to use
     * @param headers headers to be added to the response
     * @throws IllegalArgumentException if response is null
     * @throws IllegalArgumentException if statusCode is null
     */
    public static void sendContent(HttpServletResponse response, StatusCode statusCode, byte[] content, MediaType contentType, Map<String, String> headers) {
        Ensure.requireNonNull(response, "response must be non-null");
        Ensure.requireNonNull(statusCode, "statusCode must be non-null");
        response.setStatus(toHttpStatusCode(statusCode));
        if (Objects.nonNull(headers)) {
            headers.forEach(response::addHeader);
        }
        if (statusCode != StatusCode.SUCCESS_NO_CONTENT) {
            if (contentType != null) {
                response.setContentType(contentType.toString());
                try {
                    if (contentType.charset().isPresent()) {
                        response.setCharacterEncoding(contentType.charset().get().toString());
                    }
                }
                catch (IllegalStateException | IllegalCharsetNameException | UnsupportedCharsetException e) {
                    LOGGER.warn("could not determine charset for contentType '{}'", contentType, e);
                }
            }
            if (content != null) {
                try {
                    response.getOutputStream().write(content);
                    response.getOutputStream().flush();
                    response.setContentLengthLong(content.length);
                }
                catch (IOException e) {
                    send(response,
                            StatusCode.SERVER_INTERNAL_ERROR,
                            Result.builder()
                                    .message(MessageType.EXCEPTION, e.getMessage())
                                    .build());
                }
            }
        }
    }


    /**
     * Sends an empty HTTP response with given statusCode and headers.
     *
     * @param response HTTP response object
     * @param statusCode statusCode to send
     * @param headers headers to be added to the response
     * @throws IllegalArgumentException if response is null
     * @throws IllegalArgumentException if statusCode is null
     */
    public static void sendEmpty(HttpServletResponse response, StatusCode statusCode, Map<String, String> headers) {
        Ensure.requireNonNull(response, "response must be non-null");
        Ensure.requireNonNull(statusCode, "statusCode must be non-null");
        response.setStatus(toHttpStatusCode(statusCode));
        if (Objects.nonNull(headers)) {
            headers.forEach(response::addHeader);
        }
    }
}
