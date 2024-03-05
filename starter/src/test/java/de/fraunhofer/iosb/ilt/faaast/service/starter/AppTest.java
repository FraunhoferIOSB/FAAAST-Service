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

import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpoint;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.starter.fixtures.DummyMessageBusConfig;
import de.fraunhofer.iosb.ilt.faaast.service.starter.model.ConfigOverride;
import de.fraunhofer.iosb.ilt.faaast.service.starter.model.ConfigOverrideSource;
import de.fraunhofer.iosb.ilt.faaast.service.starter.model.EndpointType;
import de.fraunhofer.iosb.ilt.faaast.service.starter.util.ParameterConstants;
import de.fraunhofer.iosb.ilt.faaast.service.starter.util.ServiceConfigHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import picocli.CommandLine;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;


public class AppTest {

    private static final String MODEL_RESOURCE_PATH = "/AASMinimal.json"; // Path of model resource from core dependency
    private static final String CONFIG = "src/test/resources/config-minimal.json";
    private App application;
    private CommandLine cmd;
    private Path modelPath;
    private static ServiceConfig dummyMessageBusConfig;

    @BeforeClass
    public static void init() {
        DummyMessageBusConfig messageBusConfig = new DummyMessageBusConfig();
        dummyMessageBusConfig = ServiceConfig.builder()
                .messageBus(messageBusConfig)
                .build();
    }


    @Before
    public void prepareResources() throws IOException {
        modelPath = Files.createTempFile("faaast-app-test-model", ".json");
        modelPath.toFile().deleteOnExit();
        InputStream modelResourceAsStream = AppTest.class.getResourceAsStream(MODEL_RESOURCE_PATH);
        Files.copy(modelResourceAsStream, modelPath, StandardCopyOption.REPLACE_EXISTING);
    }


    @Before
    public void initCmd() throws IOException {
        application = new App();
        application.dryRun = true;
        cmd = new CommandLine(application)
                .setCaseInsensitiveEnumValuesAllowed(true);
        cmd.setOut(new PrintWriter(new StringWriter()));
    }


    protected EnvironmentVariables withEnv(Map<String, String> variables) {
        return withEnv(variables.entrySet().stream()
                .map(x -> new String[] {
                        x.getKey(),
                        x.getValue()
                })
                .flatMap(x -> Stream.of(x))
                .toArray(String[]::new));
    }


    protected EnvironmentVariables withEnv(String... variables) {
        Ensure.requireNonNull(variables, "variables must be non-null");
        Ensure.require(variables.length >= 2, "variables must contain at least one element");
        Ensure.require(variables.length % 2 == 0, "variables must contain an even number of elements");

        EnvironmentVariables result = null;
        for (int i = 0; i < variables.length; i += 2) {
            String key = variables[i];
            if (!Objects.equals(App.ENV_PATH_CONFIG_FILE, key)
                    && !Objects.equals(App.ENV_PATH_MODEL_FILE, key)
                    && !Objects.equals(App.envPathWithAlternativeSeparator(App.ENV_PATH_CONFIG_FILE), key)
                    && !Objects.equals(App.envPathWithAlternativeSeparator(App.ENV_PATH_MODEL_FILE), key)
                    && !key.startsWith(App.ENV_PREFIX_CONFIG_EXTENSION)
                    && !key.startsWith(App.envPathWithAlternativeSeparator(App.ENV_PREFIX_CONFIG_EXTENSION))) {
                key = key.startsWith(App.ENV_PREFIX_CONFIG_EXTENSION)
                        ? key
                        : String.format("%s%s", App.ENV_PREFIX_CONFIG_EXTENSION, key);
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
        List<ConfigOverride> expected = List.of(
                ConfigOverride.builder()
                        .originalKey(ParameterConstants.ENDPOINT_0_CLASS)
                        .value(HttpEndpoint.class.getCanonicalName())
                        .source(ConfigOverrideSource.CLI)
                        .build(),
                ConfigOverride.builder()
                        .originalKey(ParameterConstants.REQUEST_HANDLER_THREAD_POOL_SIZE)
                        .value("3")
                        .source(ConfigOverrideSource.CLI)
                        .build(),
                ConfigOverride.builder()
                        .originalKey(ParameterConstants.ENDPOINT_0_PORT)
                        .value("1337")
                        .source(ConfigOverrideSource.ENV)
                        .build());
        List<ConfigOverride> input = new ArrayList<>(expected);
        input.add(ConfigOverride.builder()
                .originalKey(ParameterConstants.REQUEST_HANDLER_THREAD_POOL_SIZE)
                .value("4")
                .source(ConfigOverrideSource.ENV)
                .build());
        String[] args = input.stream()
                .filter(x -> x.getSource() == ConfigOverrideSource.CLI)
                .map(x -> String.format("%s=%s", x.getOriginalKey(), x.getValue()))
                .toArray(String[]::new);
        Map<String, String> env = input.stream()
                .filter(x -> x.getSource() == ConfigOverrideSource.ENV)
                .collect(Collectors.toMap(
                        x -> x.getOriginalKey(),
                        x -> x.getValue()));
        List<ConfigOverride> actual = withEnv(env).execute(() -> {
            new CommandLine(application).execute(args);
            return application.getConfigOverrides();
        });
        Assert.assertTrue(expected.containsAll(actual));
        Assert.assertTrue(actual.containsAll(expected));
    }


    @Test
    public void testSeparatorReplacement() throws Exception {
        List<ConfigOverride> expected = List.of(
                ConfigOverride.builder()
                        .originalKey(ParameterConstants.MESSAGEBUS_NO_UNDERSCORE_BEFORE)
                        .updatedKey(ParameterConstants.MESSAGEBUS_NO_UNDERSCORE_AFTER)
                        .value("1")
                        .source(ConfigOverrideSource.ENV)
                        .build(),
                ConfigOverride.builder()
                        .originalKey(ParameterConstants.MESSAGEBUS_UNDERSCORE_BEFORE)
                        .updatedKey(ParameterConstants.MESSAGEBUS_UNDERSCORE_AFTER)
                        .value("1")
                        .source(ConfigOverrideSource.ENV)
                        .build());
        Map<String, String> env = expected.stream()
                .filter(x -> x.getSource() == ConfigOverrideSource.ENV)
                .collect(Collectors.toMap(
                        x -> x.getOriginalKey(),
                        x -> x.getValue()));
        List<ConfigOverride> actual = withEnv(env).execute(() -> {
            return application.getConfigOverrides(dummyMessageBusConfig);
        });
        Assert.assertTrue(expected.containsAll(actual));
        Assert.assertTrue(actual.containsAll(expected));
    }


    @Test
    public void testNestedSeparatorReplacement() throws Exception {
        List<ConfigOverride> expected = List.of(
                ConfigOverride.builder()
                        .originalKey(ParameterConstants.MESSAGEBUS_NESTED_NO_UNDERSCORE_BEFORE)
                        .updatedKey(ParameterConstants.MESSAGEBUS_NESTED_NO_UNDERSCORE_AFTER)
                        .value("1")
                        .source(ConfigOverrideSource.ENV)
                        .build(),
                ConfigOverride.builder()
                        .originalKey(ParameterConstants.MESSAGEBUS_NESTED_UNDERSCORE_BEFORE)
                        .updatedKey(ParameterConstants.MESSAGEBUS_NESTED_UNDERSCORE_AFTER)
                        .value("1")
                        .source(ConfigOverrideSource.ENV)
                        .build());
        Map<String, String> env = expected.stream()
                .filter(x -> x.getSource() == ConfigOverrideSource.ENV)
                .collect(Collectors.toMap(
                        x -> x.getOriginalKey(),
                        x -> x.getValue()));
        List<ConfigOverride> actual = withEnv(env).execute(() -> {
            return application.getConfigOverrides(dummyMessageBusConfig);
        });
        Assert.assertTrue(expected.containsAll(actual));
        Assert.assertTrue(actual.containsAll(expected));
    }


    @Test
    public void testPrefixSeparatorReplacement() throws Exception {
        List<ConfigOverride> expected = List.of(
                ConfigOverride.builder()
                        .originalKey(ParameterConstants.MESSAGEBUS_PREFIX_BEFORE)
                        .updatedKey(ParameterConstants.MESSAGEBUS_PREFIX_AFTER)
                        .value("1")
                        .source(ConfigOverrideSource.ENV)
                        .build());
        Map<String, String> env = expected.stream()
                .filter(x -> x.getSource() == ConfigOverrideSource.ENV)
                .collect(Collectors.toMap(
                        x -> x.getOriginalKey(),
                        x -> x.getValue()));
        List<ConfigOverride> actual = withEnv(env).execute(() -> {
            return application.getConfigOverrides(dummyMessageBusConfig);
        });
        Assert.assertTrue(expected.containsAll(actual));
        Assert.assertTrue(actual.containsAll(expected));
    }


    @Test
    public void testConfigOverrideViaEnvironmentUnderscoreSeparated() throws Exception {
        ServiceConfig expected = ServiceConfigHelper.getDefaultServiceConfig();
        ((HttpEndpointConfig) expected.getEndpoints().get(0)).setPort(1234);
        ServiceConfig config = ServiceConfigHelper.getDefaultServiceConfig();
        Map<String, String> env = Map.of("endpoints[0]_port", "1234");
        List<ConfigOverride> overrides = withEnv(env).execute(() -> {
            return application.getConfigOverrides(config);
        });
        ServiceConfig actual = ServiceConfigHelper.withProperties(config, overrides);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testConfigOverrideViaEnvironmentDotSeparated() throws Exception {
        ServiceConfig expected = ServiceConfigHelper.getDefaultServiceConfig();
        ((HttpEndpointConfig) expected.getEndpoints().get(0)).setPort(1234);
        ServiceConfig config = ServiceConfigHelper.getDefaultServiceConfig();
        Map<String, String> env = Map.of("faaast.config.extension.endpoints[0].port", "1234");
        List<ConfigOverride> overrides = withEnv(env).execute(() -> {
            return application.getConfigOverrides(config);
        });
        ServiceConfig actual = ServiceConfigHelper.withProperties(config, overrides);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testAmbiguitySeparatorReplacement() throws Exception {
        Map<String, String> envProperties = new HashMap<>();
        envProperties.put(ParameterConstants.MESSAGEBUS_AMBIGUITY_BEFORE, "1");

        Assert.assertThrows(InitializationException.class, () -> {
            withEnv(envProperties).execute(() -> {
                application.getConfigOverrides(dummyMessageBusConfig);
            });
        });
    }


    @Test
    public void testConfigFileCLI() {
        executeAssertSuccess("-c", CONFIG);
        Assert.assertEquals(new File(CONFIG), application.configFile);
    }


    @Test
    public void testConfigFileCLIDefault() {
        executeAssertSuccess();
        Assert.assertEquals(new File(App.CONFIG_FILENAME_DEFAULT), application.configFile);
    }


    @Test
    public void testConfigFileENV_DotSeparated() throws Exception {
        File actual = withEnv("faaast.config", CONFIG)
                .execute(() -> {
                    executeAssertSuccess();
                    return application.configFile;
                });
        Assert.assertEquals(new File(CONFIG), actual);
    }


    @Test
    public void testConfigFileENV_UnderscoreSeparated() throws Exception {
        File actual = withEnv("faaast_config", CONFIG)
                .execute(() -> {
                    executeAssertSuccess();
                    return application.configFile;
                });
        Assert.assertEquals(new File(CONFIG), actual);
    }


    @Test
    public void testModelFileCLI() {
        executeAssertSuccess("-m", modelPath.toString());
        Assert.assertEquals(modelPath.toFile(), application.modelFile);
    }


    @Test
    public void testModelFileENV_DotSeparated() throws Exception {
        File actual = withEnv("faaast.model", modelPath.toString())
                .execute(() -> {
                    new CommandLine(application).execute();
                    return application.modelFile;
                });
        Assert.assertEquals(modelPath.toFile(), actual);
    }


    @Test
    public void testModelFileENV_UnderscoreSeparated() throws Exception {
        File actual = withEnv("faaast_model", modelPath.toString())
                .execute(() -> {
                    new CommandLine(application).execute();
                    return application.modelFile;
                });
        Assert.assertEquals(modelPath.toFile(), actual);
    }


    @Test
    public void testModelFilePrio() throws Exception {
        File actual = withEnv(App.ENV_PATH_MODEL_FILE, "env.json")
                .execute(() -> {
                    new CommandLine(application).execute("-m", modelPath.toString());
                    return application.modelFile;
                });
        Assert.assertEquals(modelPath.toFile(), actual);
    }


    @Test
    public void testUseEmptyModelCLI() {
        executeAssertSuccess("--empty-model");
        Assert.assertTrue(application.useEmptyModel);
    }


    @Test
    public void testUseEmptyModelCLIDefault() {
        executeAssertSuccess();
        Assert.assertFalse(application.useEmptyModel);
    }


    @Test
    public void testModelValidationCLI() {
        executeAssertSuccess("--no-validation");
        Assert.assertTrue(application.noValidation);
    }


    @Test
    public void testModelValidationCLIDefault() {
        executeAssertSuccess("-m", modelPath.toString());
        Assert.assertFalse(application.noValidation);
    }


    @Test
    public void testEndpointsCLI() {
        var expected = List.of(EndpointType.HTTP, EndpointType.OPCUA);

        executeAssertSuccess("--endpoint", "http", "--endpoint", "opcua");
        Assert.assertEquals(expected, application.endpoints);

        executeAssertSuccess("--endpoint", "http,opcua");
        Assert.assertEquals(expected, application.endpoints);
    }


    private void executeAssertSuccess(String... args) {
        int result = cmd.execute(args);
        Assert.assertEquals(0, result);
    }
}
