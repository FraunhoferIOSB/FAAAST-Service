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

import java.io.IOException;
import java.net.ServerSocket;


/**
 * Helps finding a random free port.
 */
public class PortHelper {

    private static final String ERROR_MSG_FREE_PORT = "error finding random free port";

    private PortHelper() {}


    /**
     * Returns a random free port.
     *
     * @return a free port
     * @throws IllegalArgumentException if fails
     */
    public static int findFreePort() {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            Ensure.requireNonNull(serverSocket);
            Ensure.require(serverSocket.getLocalPort() > 0, ERROR_MSG_FREE_PORT);
            return serverSocket.getLocalPort();
        }
        catch (IOException e) {
            throw new IllegalArgumentException(ERROR_MSG_FREE_PORT, e);
        }
    }
}
