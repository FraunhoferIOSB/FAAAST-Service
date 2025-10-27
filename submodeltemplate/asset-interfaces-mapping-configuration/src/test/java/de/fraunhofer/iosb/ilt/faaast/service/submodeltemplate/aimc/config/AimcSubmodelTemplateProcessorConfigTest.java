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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonMapperFactory;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.SimpleAbstractTypeResolverFactory;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;


public class AimcSubmodelTemplateProcessorConfigTest {

    private static final File CONFIG_FILE = new File("src/test/resources/Example-config.json");
    private static final ObjectMapper MAPPER = new JsonMapperFactory()
            .create(new SimpleAbstractTypeResolverFactory().create())
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    private static final AimcSubmodelTemplateProcessorConfig CONFIG = new AimcSubmodelTemplateProcessorConfig.Builder()
            .connectionLevelCredentials(Map.of(
                    "http://plugfest.thingweb.io:8083",
                    List.of(
                            new BasicCredentials("user1", "pw1"),
                            new BasicCredentials("user2", "pw2"))))
            .build();

    @Test
    public void testConfigDeserialization() throws JsonProcessingException, IOException {
        AimcSubmodelTemplateProcessorConfig actual = MAPPER.readValue(CONFIG_FILE, AimcSubmodelTemplateProcessorConfig.class);
        Assert.assertEquals(CONFIG, actual);
    }


    @Test
    public void testConfigSerialization() throws IOException, JSONException {
        String expected = Files.readString(CONFIG_FILE.toPath());
        String actual = MAPPER.writeValueAsString(CONFIG);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }
}
