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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import java.util.Map;


/**
 * Utility class for working with paging.
 */
public class PagingHelper {

    private static final String QUERY_PARAMETER_CURSOR = "cursor";
    private static final String QUERY_PARAMETER_LIMIT = "limit";

    private PagingHelper() {}


    /**
     * Parses paging information from query paramters.
     *
     * @param queryParameters the query paramters of the HTTP request
     * @return the parsed {@code PagingInfo}
     * @throws InvalidRequestException if paging information cannot be parsed, e.g. because provided values are no valid
     *             positive numbers
     */
    public static PagingInfo parsePagingInfo(Map<String, String> queryParameters) throws InvalidRequestException {
        PagingInfo.Builder builder = PagingInfo.builder();
        if (queryParameters.containsKey(QUERY_PARAMETER_CURSOR)) {
            builder.cursor(EncodingHelper.base64UrlDecode(queryParameters.get(QUERY_PARAMETER_CURSOR)));
        }
        if (queryParameters.containsKey(QUERY_PARAMETER_LIMIT)) {
            String errorMessage = String.format(
                    "invalid value for query parameter '%s' - must be a positive number (value: %s)",
                    QUERY_PARAMETER_LIMIT,
                    queryParameters.get(QUERY_PARAMETER_LIMIT));
            try {
                long limit = Long.parseLong(queryParameters.get(QUERY_PARAMETER_LIMIT));
                if (limit <= 0) {
                    throw new InvalidRequestException(errorMessage);
                }
                builder.limit(limit);
            }
            catch (NumberFormatException e) {
                throw new InvalidRequestException(errorMessage, e);
            }
        }
        return builder.build();
    }

}
