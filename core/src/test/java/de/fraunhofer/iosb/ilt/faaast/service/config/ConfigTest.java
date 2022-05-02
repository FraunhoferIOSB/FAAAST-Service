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
package de.fraunhofer.iosb.ilt.faaast.service.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.fraunhofer.iosb.ilt.faaast.service.config.fixtures.DummyAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.fixtures.DummyNodeBasedProviderConfig;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;


public class ConfigTest {

    private static final File CONFIG_FILE = new File("src/test/resources/dummy-config.json");
    private static ServiceConfig config;
    private static ObjectMapper mapper;

    @BeforeClass
    public static void init() {
        DummyAssetConnectionConfig assetConnection = new DummyAssetConnectionConfig();
        assetConnection.setHost("tcp://localhost");
        assetConnection.setPort(1234);
        DummyNodeBasedProviderConfig valueProvider = new DummyNodeBasedProviderConfig();
        valueProvider.setNodeId("some.opc.ua.node.id");
        // TODO change ID_SHORT to IdShort once dataformat-core 1.2.1 hotfix is released
        assetConnection.getValueProviders().put(AasUtils.parseReference("(Property)[ID_SHORT]Temperature"), valueProvider);
        config = ServiceConfig.builder()
                .core(CoreConfig.builder()
                        .requestHandlerThreadPoolSize(2)
                        .build())
                .assetConnection(assetConnection)
                .build();
        mapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }


    @Test
    public void testSerialization() throws JsonProcessingException, IOException, JSONException {
        String expected = Files.readString(CONFIG_FILE.toPath());
        String actual = mapper.writeValueAsString(config);
        System.out.println(actual);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }


    @Test
    public void testDeserialization() throws JsonProcessingException, IOException {
        ServiceConfig actual = mapper.readValue(CONFIG_FILE, ServiceConfig.class);
        Assert.assertEquals(config, actual);
    }
}
