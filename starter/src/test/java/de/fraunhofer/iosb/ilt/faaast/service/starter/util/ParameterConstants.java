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
package de.fraunhofer.iosb.ilt.faaast.service.starter.util;

public class ParameterConstants {

    public static final String REQUEST_HANDLER_THREAD_POOL_SIZE = "core.requestHandlerThreadPoolSize";
    public static final String ENDPOINT_0_CLASS = "endpoints[0].@class";
    public static final String ENDPOINT_0_PORT = "endpoints[0].port";

    public static final String MESSAGEBUS_NO_UNDERSCORE_BEFORE = "messageBus_ab";
    public static final String MESSAGEBUS_NO_UNDERSCORE_AFTER = "messageBus.ab";
    public static final String MESSAGEBUS_UNDERSCORE_BEFORE = "messageBus_c_d";
    public static final String MESSAGEBUS_UNDERSCORE_AFTER = "messageBus.c_d";
    public static final String MESSAGEBUS_NESTED_NO_UNDERSCORE_BEFORE = "messageBus_nes_ted_ef";
    public static final String MESSAGEBUS_NESTED_NO_UNDERSCORE_AFTER = "messageBus.nes_ted.ef";
    public static final String MESSAGEBUS_NESTED_UNDERSCORE_BEFORE = "messageBus_nes_ted_g_h";
    public static final String MESSAGEBUS_NESTED_UNDERSCORE_AFTER = "messageBus.nes_ted.g_h";
    public static final String MESSAGEBUS_PREFIX_BEFORE = "messageBus_c_d";
    public static final String MESSAGEBUS_PREFIX_AFTER = "messageBus.c_d";
    public static final String MESSAGEBUS_AMBIGUITY_BEFORE = "messageBus_ambi_guity";

    private ParameterConstants() {}
}
