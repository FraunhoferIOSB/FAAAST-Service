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

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint;
import de.fraunhofer.iosb.ilt.faaast.service.starter.util.ParameterConstants;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import org.junit.*;
import picocli.CommandLine;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;


public class AppTest {

    private static final String MODEL_RESOURCE_PATH = "/AASMinimal.json"; // Path of model resource from core dependency
    private static final String CONFIG = "src/test/resources/config-minimal.json";
    private App application;
    private CommandLine cmd;
    private Path modelPath;

    @Before
    public void prepareResources() throws IOException {
        modelPath = Paths.get("." + MODEL_RESOURCE_PATH);
        InputStream modelResourceAsStream = AppTest.class.getResourceAsStream(MODEL_RESOURCE_PATH);

        Files.copy(modelResourceAsStream, modelPath);
    }


    @Before
    public void initCmd() throws IOException {
        application = new App();
        application.dryRun = true;
        cmd = new CommandLine(application)
                .setCaseInsensitiveEnumValuesAllowed(true);
        cmd.setOut(new PrintWriter(new StringWriter()));
    }


    @After
    public void cleanUpResources() throws IOException {
        Files.deleteIfExists(modelPath);
    }


    private EnvironmentVariables withEnv(Map<String, String> variables) {
        return withEnv(variables.entrySet().stream()
                .map(x -> new String[] {
                        x.getKey(),
                        x.getValue()
                })
                .flatMap(x -> Stream.of(x))
                .toArray(String[]::new));
    }


    private EnvironmentVariables withEnv(String... variables) {
        Ensure.requireNonNull(variables, "variables must be non-null");
        Ensure.require(variables.length >= 2, "variables must contain at least one element");
        Ensure.require(variables.length % 2 == 0, "variables must contain an even number of elements");

        EnvironmentVariables result = null;
        for (int i = 0; i < variables.length; i += 2) {
            String key = variables[i];
            if (!Objects.equals(App.ENV_CONFIG_FILE_PATH, key) && !Objects.equals(App.ENV_MODEL_FILE_PATH, key)) {
                key = key.startsWith(App.ENV_CONFIG_EXTENSION_PREFIX)
                        ? key
                        : String.format("%s%s", App.ENV_CONFIG_EXTENSION_PREFIX, key);
            }
            String value = variables[i + 1];
            result = result == null
                    ? new EnvironmentVariables(key, value)
                    : result.and(key, value);
        }
        return result;
    }


    @Test
    public void testGetConfigOverrides() throws Exception {
        Map<String, String> cliProperties = new HashMap<>();
        cliProperties.put(ParameterConstants.REQUEST_HANDLER_THREAD_POOL_SIZE, "3");
        cliProperties.put(ParameterConstants.ENDPOINT_0_CLASS, HttpEndpoint.class.getCanonicalName());
        Map<String, String> envProperties = new HashMap<>();
        envProperties.put(ParameterConstants.REQUEST_HANDLER_THREAD_POOL_SIZE, "4");
        envProperties.put(ParameterConstants.ENDPOINT_0_PORT, "1337");
        Map<String, String> expected = new HashMap<>(envProperties);
        expected.putAll(cliProperties);
        String[] args = cliProperties.entrySet().stream()
                .map(x -> String.format("%s=%s", x.getKey(), x.getValue()))
                .toArray(String[]::new);
        Map<String, String> actual = withEnv(envProperties).execute(() -> {
            new CommandLine(application).execute(args);
            return application.getConfigOverrides();
        });
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testConfigFileCLI() {
        cmd.execute("-c", CONFIG);
        Assert.assertEquals(new File(CONFIG), application.configFile);
    }


    @Test
    public void testConfigFileCLIDefault() {
        cmd.execute();
        Assert.assertEquals(new File(App.CONFIG_FILENAME_DEFAULT), application.configFile);
    }


    @Test
    public void testConfigFileENV() throws Exception {
        File actual = withEnv(App.ENV_CONFIG_FILE_PATH, CONFIG)
                .execute(() -> {
                    new CommandLine(application).execute();
                    return application.configFile;
                });
        Assert.assertEquals(new File(CONFIG), actual);
    }


    @Test
    public void testModelFileCLI() {
        cmd.execute("-m", modelPath.toString());
        Assert.assertEquals(modelPath.toFile(), application.modelFile);
    }


    @Test
    public void testModelFileENV() throws Exception {
        File actual = withEnv(App.ENV_MODEL_FILE_PATH, modelPath.toString())
                .execute(() -> {
                    new CommandLine(application).execute();
                    return application.modelFile;
                });
        Assert.assertEquals(modelPath.toFile(), actual);
    }


    @Test
    public void testModelFilePrio() throws Exception {
        File actual = withEnv(App.ENV_MODEL_FILE_PATH, "env.json")
                .execute(() -> {
                    new CommandLine(application).execute("-m", modelPath.toString());
                    return application.modelFile;
                });
        Assert.assertEquals(modelPath.toFile(), actual);
    }


    @Test
    public void testUseEmptyModelCLI() {
        cmd.execute("--emptyModel");
        Assert.assertEquals(true, application.useEmptyModel);
    }


    @Test
    public void testUseEmptyModelCLIDefault() {
        cmd.execute();
        Assert.assertEquals(false, application.useEmptyModel);
    }


    @Test
    public void testAutoCompleteConfigurationCLI() {
        cmd.execute("--no-autoCompleteConfig");
        Assert.assertEquals(false, application.autoCompleteConfiguration);
    }


    @Test
    public void testAutoCompleteConfigurationCLIDefault() {
        cmd.execute();
        Assert.assertEquals(true, application.autoCompleteConfiguration);
    }


    @Test
    public void testModelValidationCLI() {
        cmd.execute("--no-modelValidation");
        Assert.assertEquals(false, application.validateModel);
    }


    @Test
    public void testModelValidationCLIDefault() {
        cmd.execute("-m", modelPath.toString());
        Assert.assertEquals(true, application.validateModel);
    }


    @Test
    public void testEndpointsCLI() {
        var expected = List.of(EndpointType.HTTP, EndpointType.OPCUA);

        cmd.execute("--endpoint", "http", "--endpoint", "opcua");
        Assert.assertEquals(expected, application.endpoints);

        cmd.execute("--endpoint", "http,opcua");
        Assert.assertEquals(expected, application.endpoints);
    }
}
