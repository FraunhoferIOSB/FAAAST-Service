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
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility class for resolving host names. Based on
 * https://github.com/eclipse/milo/blob/v0.6.9/opc-ua-sdk/sdk-server/src/main/java/org/eclipse/milo/opcua/sdk/server/util/HostnameUtil.java.
 */
public class HostnameUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(HostnameUtil.class);
    public static final String LOCALHOST = "localhost";
    public static final String LOCALHOST_IP = "127.0.0.1";

    private HostnameUtil() {}


    /**
     * @return the local hostname, if possible. Failure results in "localhost".
     */
    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e) {
            return LOCALHOST;
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


    private static Set<String> getHostnames(NetworkInterface networkInterface, boolean includeLoopback) {
        Set<String> result = new HashSet<>();
        Collections.list(networkInterface.getInetAddresses()).forEach(ia -> {
            if (ia instanceof Inet4Address && (includeLoopback || !ia.isLoopbackAddress())) {
                result.add(ia.getHostName());
                result.add(ia.getHostAddress());
                result.add(ia.getCanonicalHostName());
            }
        });
        return result;
    }


    private static Set<String> getLocalHostnames(boolean includeLoopback) {
        try {
            return Collections.list(NetworkInterface.getNetworkInterfaces()).stream()
                    .flatMap(x -> getHostnames(x, includeLoopback).stream())
                    .collect(Collectors.toSet());
        }
        catch (SocketException e) {
            return Set.of();
        }
    }


    /**
     * Given an address resolve it to as many unique addresses or hostnames as can be found.
     *
     * @param address the address to resolve.
     * @param includeLoopback if {@code true} loopback addresses will be included in the returned set.
     * @return the addresses and hostnames that were resolved from {@code address}.
     */
    public static Set<String> getHostnames(String address, boolean includeLoopback) {
        try {
            InetAddress inetAddress = InetAddress.getByName(address);
            if (inetAddress.isAnyLocalAddress()) {
                return getLocalHostnames(includeLoopback);
            }
            if (includeLoopback || !inetAddress.isLoopbackAddress()) {
                return Set.of(
                        inetAddress.getHostName(),
                        inetAddress.getHostAddress(),
                        inetAddress.getCanonicalHostName());
            }
            return Set.of();
        }
        catch (UnknownHostException e) {
            LOGGER.warn("Failed to get InetAddress for bind address: {}", address, e);
            return Set.of();
        }
    }

}
