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

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class for resolving host names. Based on
 * https://github.com/eclipse/milo/blob/v0.6.9/opc-ua-sdk/sdk-server/src/main/java/org/eclipse/milo/opcua/sdk/server/util/HostnameUtil.java.
 */
public class HostnameUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HostnameUtil.class);

    /**
     * @return the local hostname, if possible. Failure results in "localhost".
     */
    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            return "localhost";
        }
    }


    /**
     * Given an address resolve it to as many unique addresses or hostnames as can be found.
     *
     * @param address the address to resolve.
     * @return the addresses and hostnames that were resolved from {@code address}.
     */
    public static Set<String> getHostnames(String address) {
        return getHostnames(address, true);
    }


    /**
     * Given an address resolve it to as many unique addresses or hostnames as can be found.
     *
     * @param address the address to resolve.
     * @param includeLoopback if {@code true} loopback addresses will be included in the returned set.
     * @return the addresses and hostnames that were resolved from {@code address}.
     */
    public static Set<String> getHostnames(String address, boolean includeLoopback) {
        Set<String> hostnames = new HashSet<>();

        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            if (inetAddress.isAnyLocalAddress()) {
                try {
                    Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
                    for (NetworkInterface ni: Collections.list(nis)) {
                        Collections.list(ni.getInetAddresses()).forEach(ia -> {
                            if (ia instanceof Inet4Address) {
                                if (includeLoopback || !ia.isLoopbackAddress()) {
                                    hostnames.add(ia.getHostName());
                                    hostnames.add(ia.getHostAddress());
                                    hostnames.add(ia.getCanonicalHostName());
                                }
                            }
                        });
                    }
                }
                catch (SocketException e) {
                    LOGGER.warn("Failed to NetworkInterfaces for bind address: {}", address, e);
                }
            }
            else {
                if (includeLoopback || !inetAddress.isLoopbackAddress()) {
                    hostnames.add(inetAddress.getHostName());
                    hostnames.add(inetAddress.getHostAddress());
                    hostnames.add(inetAddress.getCanonicalHostName());
                }
            }
        }
        catch (UnknownHostException e) {
            LOGGER.warn("Failed to get InetAddress for bind address: {}", address, e);
        }

        return hostnames;
    }

}
