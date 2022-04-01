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

import com.github.stefanbirkner.systemlambda.SystemLambda;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint;
import de.fraunhofer.iosb.ilt.faaast.service.starter.util.ConfigParameter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;


public class AppTest {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AppTest.class);
    private App application;
    private CommandLine cmd;

    @Before
    public void initCmd() throws IOException {
        application = new App();
        cmd = new CommandLine(application)
                .setCaseInsensitiveEnumValuesAllowed(true);
        cmd.setOut(new PrintWriter(new StringWriter()));
    }


    private SystemLambda.WithEnvironmentVariables withEnv(Map<String, String> variables) {
        return withEnv(variables.entrySet().stream()
                .map(x -> new String[] {
                        x.getKey(),
                        x.getValue()
                })
                .flatMap(x -> Stream.of(x))
                .toArray(String[]::new));
    }


    private SystemLambda.WithEnvironmentVariables withEnv(String... variables) {
        if (variables == null) {
            throw new IllegalArgumentException("variables must contain at least one element");
        }
        SystemLambda.WithEnvironmentVariables result = null;
        for (int i = 0; i < variables.length; i += 2) {
            String key = variables[i];
            if (!Objects.equals(App.ENV_CONFIG_FILE_PATH, key) && !Objects.equals(App.ENV_MODEL_FILE_PATH, key)) {
                key = key.startsWith(App.ENV_CONFIG_EXTENSION_PREFIX)
                        ? key
                        : String.format("%s%s", App.ENV_CONFIG_EXTENSION_PREFIX, key);
            }
            String value = variables[i + 1];
            result = result == null
                    ? SystemLambda.withEnvironmentVariable(key, value)
                    : result.and(key, value);
        }
        return result;
    }


    @Test
    public void testGetConfigOverrides() throws IOException, Exception {
        Map<String, String> cliProperties = new HashMap<>();
        cliProperties.put(ConfigParameter.REQUEST_HANDLER_THREAD_POOL_SIZE, "3");
        cliProperties.put(ConfigParameter.ENDPOINT_0_CLASS, HttpEndpoint.class.getCanonicalName());
        Map<String, String> envProperties = new HashMap<>();
        envProperties.put(ConfigParameter.REQUEST_HANDLER_THREAD_POOL_SIZE, "4");
        envProperties.put(ConfigParameter.ENDPOINT_0_PORT, "1337");
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
    public void testConfigFile_CLI() {
        cmd.execute("-c", "myConfig.json");
        Assert.assertEquals(new File("myConfig.json"), application.configFile);
    }


    @Test
    public void testConfigFile_CLI_Default() {
        cmd.execute();
        Assert.assertEquals(new File(App.CONFIG_FILENAME_DEFAULT), application.configFile);
    }


    @Test
    public void testConfigFile_ENV() throws Exception {
        File actual = withEnv(App.ENV_CONFIG_FILE_PATH, "myConfig.json")
                .execute(() -> {
                    new CommandLine(application).execute();
                    return application.configFile;
                });
        Assert.assertEquals(new File("myConfig.json"), actual);
    }


    @Test
    public void testModelFile_CLI() {
        cmd.execute("-m", "myAAS.json");
        Assert.assertEquals(new File("myAAS.json"), application.modelFile);
    }


    @Test
    public void testModelFile_ENV() throws Exception {
        File actual = withEnv(App.ENV_MODEL_FILE_PATH, "myAAS.json")
                .execute(() -> {
                    new CommandLine(application).execute();
                    return application.modelFile;
                });
        Assert.assertEquals(new File("myAAS.json"), actual);
    }


    @Test
    public void testModelFile_Prio() throws Exception {
        File actual = withEnv(App.ENV_MODEL_FILE_PATH, "env.json")
                .execute(() -> {
                    new CommandLine(application).execute("-m", "cli.json");
                    return application.modelFile;
                });
        Assert.assertEquals(new File("cli.json"), actual);
    }


    @Test
    public void testUseEmptyModel_CLI() {
        cmd.execute("--emptyModel");
        Assert.assertEquals(true, application.useEmptyModel);
    }


    @Test
    public void testUseEmptyModel_CLI_Default() {
        cmd.execute();
        Assert.assertEquals(false, application.useEmptyModel);
    }


    @Test
    public void testAutoCompleteConfiguration_CLI() {
        cmd.execute("--no-autoCompleteConfig");
        Assert.assertEquals(false, application.autoCompleteConfiguration);
    }


    @Test
    public void testAutoCompleteConfiguration_CLI_Default() {
        cmd.execute();
        Assert.assertEquals(true, application.autoCompleteConfiguration);
    }


    @Test
    public void testModelValidation_CLI() {
        cmd.execute("--no-modelValidation");
        Assert.assertEquals(false, application.validateModel);
    }


    @Test
    public void testModelValidation_CLI_Default() {
        cmd.execute();
        Assert.assertEquals(true, application.validateModel);
    }


    @Test
    public void testEndpoints_CLI() {
        var expected = List.of(EndpointType.HTTP, EndpointType.OPCUA);

        cmd.execute("--endpoint", "http", "--endpoint", "opcua");
        Assert.assertEquals(expected, application.endpoints);

        cmd.execute("--endpoint", "http,opcua");
        Assert.assertEquals(expected, application.endpoints);
    }
}
