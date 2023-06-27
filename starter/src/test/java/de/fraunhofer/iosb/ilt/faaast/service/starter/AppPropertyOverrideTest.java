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
package de.fraunhofer.iosb.ilt.faaast.service.starter;

import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.starter.fixtures.DummyMessageBusConfig;
import de.fraunhofer.iosb.ilt.faaast.service.starter.util.ParameterConstants;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;


public class AppPropertyOverrideTest extends AbstractAppTest {
    private static ServiceConfig config;

    @BeforeClass
    public static void init() {
        DummyMessageBusConfig messageBusConfig = new DummyMessageBusConfig();
        config = ServiceConfig.builder()
                .core(CoreConfig.builder()
                        .requestHandlerThreadPoolSize(2)
                        .build())
                .messageBus(messageBusConfig)
                .build();
    }


    @Test
    public void testSeperatorReplacement() throws Exception {
        Map<String, String> expected = new HashMap<>();
        expected.put(ParameterConstants.MESSAGEBUS_AFTER_AB, "1");
        expected.put(ParameterConstants.MESSAGEBUS_AFTER_CD, "1");

        Map<String, String> envVariables = new HashMap<>();
        envVariables.put(ParameterConstants.MESSAGEBUS_BEFORE_AB, "1");
        envVariables.put(ParameterConstants.MESSAGEBUS_BEFORE_CD, "1");

        Map<String, String> actual = application.removeSeparators(config, envVariables);
        Assert.assertEquals(expected, actual);
    }


    @Ignore
    @Test
    public void testNestedSeperatorReplacement() throws Exception {
        Map<String, String> expected = new HashMap<>();
        expected.put(ParameterConstants.MESSAGEBUS_AFTER_EF, "1");
        expected.put(ParameterConstants.MESSAGEBUS_AFTER_GH, "1");

        Map<String, String> envVariables = new HashMap<>();
        envVariables.put(ParameterConstants.MESSAGEBUS_BEFORE_EF, "1");
        envVariables.put(ParameterConstants.MESSAGEBUS_BEFORE_GH, "1");

        Map<String, String> actual = application.removeSeparators(config, envVariables);
        Assert.assertEquals(expected, actual);
    }
}
