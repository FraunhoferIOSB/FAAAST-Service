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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import io.adminshell.aas.v3.dataformat.DeserializationException;
import io.adminshell.aas.v3.dataformat.Deserializer;
import io.adminshell.aas.v3.dataformat.aml.AmlDeserializer;
import io.adminshell.aas.v3.dataformat.i4aas.I4AASDeserializer;
import io.adminshell.aas.v3.dataformat.json.JsonDeserializer;
import io.adminshell.aas.v3.dataformat.xml.XmlDeserializer;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.impl.DefaultAssetAdministrationShellEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


public class StarterTest {

    ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    ConfigFactory configFactory = new ConfigFactory();
    AASEnvironmentFactory environmentFactory = new AASEnvironmentFactory();

    @Test
    public void testCreateConfig() throws IOException, Exception {
        ServiceConfig expected = mapper.readValue(new File("src/test/resources/test-config-expected.json"), ServiceConfig.class);
        ServiceConfig actual = configFactory.toServiceConfig("src/test/resources/test-config-expected.json");

        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testCreateConfigWithProperties() throws IOException, Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("core.requestHandlerThreadPoolSize", 2);
        properties.put("endpoints.0.@class", "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.http.HttpEndpoint");
        ServiceConfig expected = mapper.readValue(new File("src/test/resources/test-config-expected.json"), ServiceConfig.class);
        ServiceConfig actual = configFactory.toServiceConfig("src/test/resources/test-config.json", true, properties, null);

        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetDefaultConfig() throws IOException, Exception {
        ServiceConfig expected = mapper.readValue(new File("src/main/resources/default-config.json"), ServiceConfig.class);
        ServiceConfig actual = configFactory.getDefaultServiceConfig();

        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetDefaultConfigWithProperties() throws IOException, Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("core.requestHandlerThreadPoolSize", 10);

        ServiceConfig expected = mapper.readValue(new File("src/main/resources/default-config.json"), ServiceConfig.class);
        CoreConfig coreConfig = expected.getCore();
        coreConfig.setRequestHandlerThreadPoolSize(10);

        ServiceConfig actual = configFactory.getDefaultServiceConfig(properties);

        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAASEnvironmentDefault() {
        AssetAdministrationShellEnvironment expected = new DefaultAssetAdministrationShellEnvironment();
        AssetAdministrationShellEnvironment actual = environmentFactory.getEmptyAASEnvironment();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAASEnvironmentFromFileJSON() throws IOException, DeserializationException, Exception {
        String filePath = "src/test/resources/AASFull.json";
        testAASEnvironment(filePath, new JsonDeserializer());
    }


    @Test
    public void testGetAASEnvironmentFromFileXML() throws IOException, DeserializationException, Exception {
        String filePath = "src/test/resources/AASFull.xml";
        testAASEnvironment(filePath, new XmlDeserializer());
    }


    @Test
    @Ignore
    public void testGetAASEnvironmentFromFileAML() throws IOException, DeserializationException, Exception {
        String filePath = "src/test/resources/AASFull.aml";
        testAASEnvironment(filePath, new AmlDeserializer());
    }


    @Test
    @Ignore
    public void testGetAASEnvironmentFromFileOPCUA() throws IOException, DeserializationException, Exception {
        String filePath = "src/test/resources/AASSimple.xml";
        testAASEnvironment(filePath, new I4AASDeserializer());
    }


    @Test
    public void testGetAASEnvironmentFromFileRDF() throws IOException, DeserializationException, Exception {
        String filePath = "src/test/resources/AASFull.rdf";
        testAASEnvironment(filePath, new io.adminshell.aas.v3.dataformat.rdf.Serializer());
    }


    private void testAASEnvironment(String filePath, Deserializer deserializer) throws Exception, FileNotFoundException, DeserializationException {
        AssetAdministrationShellEnvironment expected = deserializer.read(new File(filePath));
        AssetAdministrationShellEnvironment actual = environmentFactory.getAASEnvironment(filePath);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAASEnvironmentFail() throws IOException, DeserializationException, Exception {
        String filePath = "src/test/resources/AASSimple.xmasl";
        try {
            environmentFactory.getAASEnvironment(filePath);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
        Assert.assertThrows(Exception.class, () -> environmentFactory.getAASEnvironment(filePath));
    }

}
