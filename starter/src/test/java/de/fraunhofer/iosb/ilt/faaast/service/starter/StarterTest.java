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

import static com.github.stefanbirkner.systemlambda.SystemLambda.withEnvironmentVariable;

import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.starter.util.AASEnvironmentHelper;
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;


public class StarterTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(StarterTest.class);
    private final ConfigFactory configFactory = new ConfigFactory();
    private Application application;
    private CommandLine cmd;

    @Before
    public void initCmd() throws IOException {
        application = new Application();
        cmd = new CommandLine(application);
        cmd.setOut(new PrintWriter(new StringWriter()));
    }


    @BeforeClass
    public static void initialize() throws IOException {
        Files.copy(Paths.get("src/test/resources/AASFull.json"), Paths.get("aasenvironment.json"), StandardCopyOption.REPLACE_EXISTING);
    }


    @AfterClass
    public static void cleanup() {
        try {
            Files.deleteIfExists(Paths.get("aasenvironment.json"));
        }
        catch (IOException ex) {
            LOGGER.warn("error removing temp file 'aasenvironment.json'");
        }
    }


    private ServiceConfig getExpectedDefaultServiceConfig() {
        return new ServiceConfig.Builder()
                .core(new CoreConfig.Builder().requestHandlerThreadPoolSize(2).build())
                .endpoints(List.of(new HttpEndpointConfig()))
                .persistence(new PersistenceInMemoryConfig())
                .messageBus(new MessageBusInternalConfig())
                .build();
    }


    @Test
    public void testCreateConfig() throws IOException, Exception {
        ServiceConfig expected = getExpectedDefaultServiceConfig();
        ServiceConfig actual = configFactory.toServiceConfig("src/test/resources/test-config-expected.json");

        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testCreateConfigWithProperties() throws IOException, Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("core.requestHandlerThreadPoolSize", 2);
        properties.put("endpoints.0.@class", "de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint");
        ServiceConfig expected = getExpectedDefaultServiceConfig();
        ServiceConfig actual = configFactory.toServiceConfig("src/test/resources/test-config.json", true, properties, null);

        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetDefaultConfig() throws IOException, Exception {
        ServiceConfig expected = getExpectedDefaultServiceConfig();
        ServiceConfig actual = configFactory.getDefaultServiceConfig();

        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetDefaultConfigWithProperties() throws IOException, Exception {
        Map<String, Object> properties = new HashMap<>();
        properties.put("core.requestHandlerThreadPoolSize", 10);

        ServiceConfig expected = getExpectedDefaultServiceConfig();
        CoreConfig coreConfig = expected.getCore();
        coreConfig.setRequestHandlerThreadPoolSize(10);

        ServiceConfig actual = configFactory.getDefaultServiceConfig(properties);

        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testGetAASEnvironmentDefault() {
        AssetAdministrationShellEnvironment expected = new DefaultAssetAdministrationShellEnvironment();
        AssetAdministrationShellEnvironment actual = AASEnvironmentHelper.newEmpty();
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
    @Ignore("Not Yet")
    public void testGetAASEnvironmentFromFileAML() throws IOException, DeserializationException, Exception {
        String filePath = "src/test/resources/AASFull.aml";
        testAASEnvironment(filePath, new AmlDeserializer());
    }


    @Test
    @Ignore("Not yet")
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
        AssetAdministrationShellEnvironment actual = AASEnvironmentHelper.fromFile(new File(filePath));
        Assert.assertEquals(expected, actual);
    }


    @Test(expected = DeserializationException.class)
    public void testGetAASEnvironmentFail() throws IOException, DeserializationException, Exception {
        String filePath = "src/test/resources/AASSimple.xmasl";
        AASEnvironmentHelper.fromFile(new File(filePath));
    }


    @Test
    public void testCMDConfigFile() {
        cmd.execute("-c", "myConfig.json");
        Assert.assertEquals("myConfig.json", application.configFilePath);

        cmd.execute();
        Assert.assertEquals(Application.DEFAULT_CONFIG_PATH, application.configFilePath);
    }


    @Test
    public void testCMDConfigFileThroughEnvironmentVariable() throws Exception {
        String actual = withEnvironmentVariable(application.CONFIG_FILE_PATH_ENVIRONMENT_VARIABLE, "myConfig.json")
                .execute(() -> {
                    new CommandLine(application).execute();
                    return application.configFilePath;
                });

        Assert.assertEquals("myConfig.json", actual);
    }


    @Test
    public void testCMDConfigParameterThroughEnvironmentVariable() throws Exception {
        Map<String, Object> actual = withEnvironmentVariable("faaast.configParameter.core.requestHandlerThreadPoolSize", "42")
                .and("faaast.configParameter.endpoints.0.port", "9999")
                .execute(() -> {
                    new CommandLine(application).execute();
                    return application.properties;
                });

        Assert.assertEquals("42", actual.get("core.requestHandlerThreadPoolSize"));
        Assert.assertEquals("9999", actual.get("endpoints.0.port"));
    }


    @Test
    public void testCMDaasEnv() {
        cmd.execute("-e", "myAAS.json");
        Assert.assertEquals("myAAS.json", application.aasEnvironmentFile.getName());

        cmd.execute();
        Assert.assertTrue(application.aasEnvironmentFile.exists());

        cmd.execute("--emptyEnvironment");
        Assert.assertEquals(true, application.useEmptyAASEnvironment);
    }


    @Test
    public void testCMDaasEnvThroughEnvironmentVariable() throws Exception {
        String actual = withEnvironmentVariable(application.AASENV_FILE_PATH_ENVIRONMENT_VARIABLE, "myAAS.json")
                .execute(() -> {
                    new CommandLine(application).execute();
                    return application.aasEnvironmentFile.getName();
                });

        Assert.assertEquals("myAAS.json", actual);
    }


    @Test
    public void testCMDPriority() throws Exception {
        String actual = withEnvironmentVariable(application.AASENV_FILE_PATH_ENVIRONMENT_VARIABLE, "myAAS.json")
                .execute(() -> {
                    new CommandLine(application).execute("-e", "AAS.json");
                    return application.aasEnvironmentFile.getName();
                });

        Assert.assertEquals("myAAS.json", actual);

        actual = withEnvironmentVariable(application.CONFIG_FILE_PATH_ENVIRONMENT_VARIABLE, "myConfig.json")
                .execute(() -> {
                    new CommandLine(application).execute("-c", "con.json");
                    return application.configFilePath;
                });

        Assert.assertEquals("myConfig.json", actual);
    }


    @Test
    public void testCMDautoComplete() {
        cmd.execute("--no-autoCompleteConfig");
        Assert.assertEquals(false, application.autoCompleteConfiguration);

        cmd.execute();
        Assert.assertEquals(true, application.autoCompleteConfiguration);
    }


    @Test
    public void testCMDvalidation() {
        cmd.execute("--no-modelValidation");
        Assert.assertEquals(false, application.validateAASEnv);

        cmd.execute();
        Assert.assertEquals(true, application.validateAASEnv);
    }


    @Test
    public void testCMDproperties() {
        cmd.execute("-Dcore.requestHandlerThreadPoolSize=42");
        Assert.assertEquals("42", application.properties.get("core.requestHandlerThreadPoolSize"));
    }


    @Test
    public void testCMDendpoints() {
        cmd.execute("--endpoints", "http", "opcua");
        Assert.assertTrue(application.endpoints.containsAll(List.of("http", "opcua")));
    }

}
