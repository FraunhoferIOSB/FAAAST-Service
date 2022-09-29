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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import java.util.Objects;


/**
 * Helper class for working with AAS responses.
 */
public class ResponseHelper {

    private ResponseHelper() {}


    /**
     * Compares if two responses are equal when ignoring the timestamps of the included messages.
     *
     * @param r1 the first response
     * @param r2 the second response
     * @return true if inputs are equal ignoring the message timestamps, false otherweise
     */
    public static boolean equalsIgnoringTime(Response r1, Response r2) {
        removeTimeFromMessages(r1);
        removeTimeFromMessages(r2);
        return Objects.equals(r1, r2);
    }


    /**
     * Compares if two {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.Result} objects are equal when ignoring
     * the timestamps of the included messages.
     *
     * @param r1 the first result
     * @param r2 the second result
     * @return true if inputs are equal ignoring the message timestamps, false otherweise
     */
    public static boolean equalsIgnoringTime(Result r1, Result r2) {
        removeTimeFromMessages(r1);
        removeTimeFromMessages(r2);
        return Objects.equals(r1, r2);
    }


    /**
     * Removes timestamps from messages (by setting the value to null).
     *
     * @param response the response to process
     */
    public static void removeTimeFromMessages(Response response) {
        if (response != null) {
            removeTimeFromMessages(response.getResult());
        }
    }


    /**
     * Removes timestamps from messages (by setting the value to null).
     *
     * @param result the result to process
     */
    public static void removeTimeFromMessages(Result result) {
        if (result != null && result.getMessages() != null) {
            result.getMessages().forEach(x -> x.setTimestamp(null));
        }
    }
}
