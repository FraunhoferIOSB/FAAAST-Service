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

import static de.fraunhofer.iosb.ilt.faaast.service.starter.App.APP_NAME;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentSerializationManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.OpcUaEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValidationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidator;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidatorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.starter.cli.LogLevelTypeConverter;
import de.fraunhofer.iosb.ilt.faaast.service.starter.logging.FaaastFilter;
import de.fraunhofer.iosb.ilt.faaast.service.starter.model.ConfigOverride;
import de.fraunhofer.iosb.ilt.faaast.service.starter.model.ConfigOverrideSource;
import de.fraunhofer.iosb.ilt.faaast.service.starter.model.EndpointType;
import de.fraunhofer.iosb.ilt.faaast.service.starter.util.ServiceConfigHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.FileHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ImplementationManager;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;


/**
 * Class for configuring and starting a FA³ST Service.
 */
@Command(name = APP_NAME, mixinStandardHelpOptions = true, description = "Starts a FA³ST Service", versionProvider = App.PropertiesVersionProvider.class, usageHelpAutoWidth = true)
public class App implements Runnable {

    protected static final String APP_NAME = "FA³ST Service Starter";
    private static final int INDENT_DEFAULT = 20;
    private static final int INDENT_STEP = 3;
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final CountDownLatch SHUTDOWN_FINISHED = new CountDownLatch(1);
    private static final CountDownLatch SHUTDOWN_REQUESTED = new CountDownLatch(1);
    private static final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    private static final AtomicReference<Service> serviceRef = new AtomicReference<>();
    // commands
    protected static final String COMMAND_CONFIG = "--config";
    protected static final String COMMAND_MODEL = "--model";
    protected static final String COMMAND_NO_VALIDATION = "--no-validation";
    // config
    protected static final String CONFIG_FILENAME_DEFAULT = "config.json";
    // environment
    protected static final String ENV_PATH_SEPARATOR = ".";
    protected static final String ENV_PATH_ALTERNATIVE_SEPARATOR = "_";
    protected static final String JSON_PATH_SEPARATOR = ".";
    protected static final String ENV_KEY_PREFIX = "faaast";
    protected static final String ENV_PATH_CONFIG_FILE = envPath(ENV_KEY_PREFIX, "config");
    protected static final String ENV_PATH_MODEL_FILE = envPath(ENV_KEY_PREFIX, "model");
    protected static final String ENV_PATH_LOGLEVEL_EXTERNAL = envPath(ENV_KEY_PREFIX, "loglevel_external");
    protected static final String ENV_PATH_LOGLEVEL_FAAAAST = envPath(ENV_KEY_PREFIX, "loglevel_faaast");
    protected static final String ENV_PATH_NO_VALIDATION = envPath(ENV_KEY_PREFIX, "no_validation");
    protected static final String ENV_PREFIX_CONFIG_EXTENSION = envPath(ENV_KEY_PREFIX, "config", "extension", "");
    protected static final String ENV_PREFIX_CONFIG_EXTENSION_ALTERNATIVE = envPathWithAlternativeSeparator(ENV_PREFIX_CONFIG_EXTENSION);
    // model
    protected static final String MODEL_FILENAME_DEFAULT = "model.*";
    protected static final String MODEL_FILENAME_PATTERN = "model\\..*";
    private static final Configuration JSON_PATH_CONFIG = Configuration
            .builder()
            .mappingProvider(new JacksonMappingProvider())
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build()
            .addOptions(com.jayway.jsonpath.Option.SUPPRESS_EXCEPTIONS);

    @Option(names = {
            "-c",
            COMMAND_CONFIG
    }, description = "The config file path. Default Value = ${DEFAULT-VALUE}", defaultValue = CONFIG_FILENAME_DEFAULT)
    public File configFile;

    @Option(names = "--endpoint", split = ",", description = "Additional endpoints that should be started.")
    public List<EndpointType> endpoints = new ArrayList<>();
    @Option(names = {
            "-m",
            COMMAND_MODEL
    }, description = "Asset Administration Shell Environment FilePath. Default Value = ${DEFAULT-VALUE}", defaultValue = MODEL_FILENAME_DEFAULT)
    public File modelFile;

    @Parameters(description = "Additional properties to override values of configuration using JSONPath notation without starting '$.' (see https://goessner.net/articles/JsonPath/)")
    public Map<String, String> properties = new HashMap<>();

    @Option(names = {
            "-e",
            "--empty-model"
    }, negatable = false, description = "Starts the FA³ST service with an empty Asset Administration Shell Environment. False by default")
    public boolean useEmptyModel = false;

    @Option(names = COMMAND_NO_VALIDATION, negatable = false, description = "Disables validation, overrides validation configuration in core configuration.")
    public boolean noValidation = false;

    @Option(names = "--loglevel-faaast", description = "Sets the log level for FA³ST packages. This overrides the log level defined by other commands such as -q or -v.")
    public Level logLevelFaaast;

    @Option(names = "--loglevel-external", description = "Sets the log level for external packages. This overrides the log level defined by other commands such as -q or -v.")
    public Level logLevelExternal;

    @Option(names = {
            "-q",
            "--quite"
    }, description = "Reduces log output (ERROR for FA³ST packages, ERROR for all other packages). Default information about the starting process will still be printed.")
    public boolean quite = false;

    @Option(names = {
            "-v",
            "--verbose"
    }, description = "Enables verbose logging (INFO for FA³ST packages, WARN for all other packages).")
    public boolean verbose = false;

    @Option(names = "-vv", description = "Enables very verbose logging (DEBUG for FA³ST packages, INFO for all other packages).")
    public boolean veryVerbose = false;

    @Option(names = "-vvv", description = "Enables very very verbose logging (TRACE for FA³ST packages, DEBUG for all other packages).")
    public boolean veryVeryVerbose = false;

    protected boolean dryRun = false;

    @Spec
    private CommandSpec spec;
    private static int exitCode = -1;

    /**
     * Main entry point.
     *
     * @param args CLI arguments
     */
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (exitCode == CommandLine.ExitCode.OK) {
                    SHUTDOWN_REQUESTED.countDown();
                    try {
                        SHUTDOWN_FINISHED.await();
                    }
                    catch (InterruptedException ex) {
                        LOGGER.error("Error while waiting for FA³ST Service to gracefully shutdown");
                        Thread.currentThread().interrupt();
                    }
                    finally {
                        LOGGER.info("Goodbye!");
                    }
                }
            }
        });
        CommandLine commandLine = new CommandLine(new App())
                .registerConverter(Level.class, new LogLevelTypeConverter())
                .setExecutionExceptionHandler(new ExecutionExceptionHandler())
                .setCaseInsensitiveEnumValuesAllowed(true);
        try {
            CommandLine.ParseResult result = commandLine.parseArgs(args);
            if (result.isUsageHelpRequested()) {
                commandLine.usage(System.out);
                return;
            }
            else if (result.isVersionHelpRequested()) {
                commandLine.printVersionHelp(System.out);
                return;
            }
        }
        catch (CommandLine.ParameterException e) {
            // intentionally left empty
        }
        exitCode = commandLine.execute(args);
        if (exitCode == CommandLine.ExitCode.OK) {
            try {
                SHUTDOWN_REQUESTED.await();
            }
            catch (InterruptedException e) {
                LOGGER.error("Interrupted!", e);
                Thread.currentThread().interrupt();
            }
            finally {
                LOGGER.info("Shutting down FA³ST Service...");
                if (serviceRef.get() != null) {
                    serviceRef.get().stop();
                }
                LOGGER.info("FA³ST Service successfully shut down");
                SHUTDOWN_FINISHED.countDown();
            }
        }
        else {
            System.exit(exitCode);
        }
    }


    private static String envPath(String... args) {
        return Stream.of(args).collect(Collectors.joining(ENV_PATH_SEPARATOR));
    }


    private static String indent(String value, int steps) {
        return String.format(String.format("%%%ds%s", INDENT_DEFAULT + (INDENT_STEP * steps), value), "");
    }


    private void validate(ServiceConfig config) {
        boolean disableValidation;
        if (getEnvValue(ENV_PATH_NO_VALIDATION) != null
                && !getEnvValue(ENV_PATH_NO_VALIDATION).isBlank()) {
            disableValidation = Boolean.parseBoolean(getEnvValue(ENV_PATH_NO_VALIDATION));
        }
        else {
            disableValidation = noValidation;
        }
        if (disableValidation) {
            config.getCore().setValidationOnLoad(ModelValidatorConfig.NONE);
            config.getCore().setValidationOnCreate(ModelValidatorConfig.NONE);
            config.getCore().setValidationOnUpdate(ModelValidatorConfig.NONE);
        }

        if (!config.getCore().getValidationOnLoad().isEnabled()) {
            LOGGER.info("ValidateOnLoad is disabled in core config, no validation will be performed.");
            return;
        }
        LOGGER.debug("Validating model...");
        LOGGER.debug("Constraint validation: {}", config.getCore().getValidationOnLoad().getValidateConstraints());
        LOGGER.debug("IdShort uniqueness validation: {}", config.getCore().getValidationOnLoad().getIdShortUniqueness());
        LOGGER.debug("Identifier uniqueness validation: {}", config.getCore().getValidationOnLoad().getIdentifierUniqueness());
        if (useEmptyModel) {
            return;
        }
        Environment model = config.getPersistence().getInitialModel();
        if (Objects.isNull(model) && Objects.isNull(config.getPersistence().getInitialModelFile())) {
            return;
        }
        try {
            model = EnvironmentSerializationManager.deserialize(config.getPersistence().getInitialModelFile()).getEnvironment();
            ModelValidator.validate(model, config.getCore().getValidationOnLoad());
            LOGGER.info("Model successfully validated");
        }
        catch (ValidationException e) {
            throw new InitializationException(
                    String.format(
                            "Model validation failed with the following error(s):%s%s%sUse '%s' to disable all validation. This might enables FA³ST Service to start even with an invalid model, but be aware that this might cause unexpected or even erroneous behavior (e.g. when IDs are not unique).",
                            System.lineSeparator(),
                            e.getMessage(),
                            System.lineSeparator(),
                            COMMAND_NO_VALIDATION));
        }
        catch (Exception e) {
            throw new InitializationException("Error loading model file. Ensure that the model is valid and conformant to v3 of the AAS specficiation.", e);
        }

    }


    private void configureLogging() {
        if (veryVeryVerbose) {
            FaaastFilter.setLevelFaaast(Level.TRACE);
            FaaastFilter.setLevelExternal(Level.DEBUG);
        }
        else if (veryVerbose) {
            FaaastFilter.setLevelFaaast(Level.DEBUG);
            FaaastFilter.setLevelExternal(Level.INFO);
        }
        else if (verbose) {
            FaaastFilter.setLevelFaaast(Level.INFO);
            FaaastFilter.setLevelExternal(Level.WARN);
        }
        else if (quite) {
            FaaastFilter.setLevelFaaast(Level.ERROR);
            FaaastFilter.setLevelExternal(Level.ERROR);
        }
        if (logLevelFaaast != null) {
            FaaastFilter.setLevelFaaast(logLevelFaaast);
        }
        if (getEnvValue(ENV_PATH_LOGLEVEL_FAAAAST) != null && !getEnvValue(ENV_PATH_LOGLEVEL_FAAAAST).isBlank()) {
            FaaastFilter.setLevelFaaast(Level.toLevel(getEnvValue(ENV_PATH_LOGLEVEL_FAAAAST), FaaastFilter.getLevelFaaast()));
        }
        if (logLevelExternal != null) {
            FaaastFilter.setLevelExternal(logLevelExternal);
        }
        if (getEnvValue(ENV_PATH_LOGLEVEL_EXTERNAL) != null && !getEnvValue(ENV_PATH_LOGLEVEL_EXTERNAL).isBlank()) {
            FaaastFilter.setLevelExternal(Level.toLevel(getEnvValue(ENV_PATH_LOGLEVEL_EXTERNAL), FaaastFilter.getLevelExternal()));
        }
        LOGGER.info("Using log level for FA³ST packages: {}", FaaastFilter.getLevelFaaast());
        LOGGER.info("Using log level for external packages: {}", FaaastFilter.getLevelExternal());
    }


    @Override
    public void run() {
        configureLogging();
        printHeader();
        ImplementationManager.init();
        ServiceConfig config = null;
        try {
            config = getConfig();
        }
        catch (IOException e) {
            throw new InitializationException("Error loading config file: " + e.getMessage(), e);
        }
        config = ServiceConfigHelper.autoComplete(config);
        config = withModel(config);
        config = withEndpoints(config);
        config = withOverrides(config);
        validate(config);
        if (!dryRun) {
            runService(config);
        }
    }


    private void runService(ServiceConfig config) {
        try {
            serviceRef.set(new Service(config));
            LOGGER.info("Starting FA³ST Service...");
            LOGGER.debug("Using configuration file: ");
            printConfig(config);
            serviceRef.get().start();
            LOGGER.info("FA³ST Service successfully started");
            printEndpointInfo(config);
            LOGGER.info("Press CTRL + C to stop");
        }
        catch (Exception e) {
            LOGGER.error(String.format(
                    "Unexpected exception encountered while executing FA³ST Service (%s)",
                    e.getMessage()),
                    e);
        }
    }


    private Optional<File> findDefaultModel() {
        try {
            List<File> modelFiles;
            List<String> fileExtensions = new ArrayList<>(DataFormat.AASX.getFileExtensions());
            fileExtensions.addAll(DataFormat.JSON.getFileExtensions());
            try (Stream<File> stream = Files.find(Paths.get(""), 1,
                    (file, attributes) -> file.toFile()
                            .getName()
                            .matches(MODEL_FILENAME_PATTERN))
                    .map(Path::toFile)) {
                modelFiles = stream.filter(f -> fileExtensions.stream()
                        .anyMatch(FileHelper.getFileExtensionWithoutSeparator(f.getName())::equalsIgnoreCase))
                        .toList();
            }
            if (modelFiles.size() > 1 && LOGGER.isWarnEnabled()) {
                LOGGER.warn("Found multiple model files matching the default pattern. To use a specific one use command '{} <filename>' (files found: {}, file pattern: {})",
                        COMMAND_MODEL,
                        modelFiles.stream()
                                .map(File::getName)
                                .collect(Collectors.joining(",", "[", "]")),
                        MODEL_FILENAME_PATTERN);
            }
            return modelFiles.stream().findFirst();
        }
        catch (IOException ex) {
            return Optional.empty();
        }
    }


    private ServiceConfig getConfig() throws IOException {
        if (spec.commandLine().getParseResult().hasMatchedOption(COMMAND_CONFIG)) {
            try {
                LOGGER.info("Config: {} (CLI)", configFile.getCanonicalFile());
            }
            catch (IOException e) {
                LOGGER.info("Retrieving path of config file failed with {}", e.getMessage());
            }
            return ServiceConfigHelper.load(configFile);
        }
        if (getEnvValue(ENV_PATH_CONFIG_FILE) != null && !getEnvValue(ENV_PATH_CONFIG_FILE).isBlank()) {
            LOGGER.info("Config: {} (ENV)", getEnvValue(ENV_PATH_CONFIG_FILE));
            configFile = new File(getEnvValue(ENV_PATH_CONFIG_FILE));
            return ServiceConfigHelper.load(new File(getEnvValue(ENV_PATH_CONFIG_FILE)));
        }
        if (new File(CONFIG_FILENAME_DEFAULT).exists()) {
            configFile = new File(CONFIG_FILENAME_DEFAULT);
            try {
                LOGGER.info("Config: {} (default location)", configFile.getCanonicalFile());
            }
            catch (IOException e) {
                LOGGER.info("Retrieving path of config file failed with {}", e.getMessage());
            }
            return ServiceConfigHelper.load(configFile);
        }
        LOGGER.info("Config: empty (default)");
        return ServiceConfigHelper.getDefaultServiceConfig();
    }


    private static String getEnvValue(String key) {
        return System.getenv().containsKey(key)
                ? System.getenv(key)
                : System.getenv(envPathWithAlternativeSeparator(key));
    }


    /**
     * Replaces env path separators with alternative separators to support using both separators in env variable names.
     *
     * @param key the env path to replace separators in
     * @return input with replaced separators
     */
    protected static String envPathWithAlternativeSeparator(String key) {
        return key.replace(ENV_PATH_SEPARATOR, ENV_PATH_ALTERNATIVE_SEPARATOR);
    }


    private void withModelFromCommandLine(ServiceConfig config) {
        try {
            LOGGER.info("Model: {} (CLI)", modelFile.getCanonicalFile());
            if (config.getPersistence().getInitialModelFile() != null) {
                LOGGER.info("Overriding Model Path {} set in Config File with {}",
                        config.getPersistence().getInitialModelFile(),
                        modelFile.getCanonicalFile());
            }
        }
        catch (IOException e) {
            LOGGER.info("Retrieving path of model file failed with {}", e.getMessage());
        }
        config.getPersistence().setInitialModelFile(modelFile);
    }


    private void withModelFromEnvironmentVariable(ServiceConfig config) {
        LOGGER.info("Model: {} (ENV)", getEnvValue(ENV_PATH_MODEL_FILE));
        if (config.getPersistence().getInitialModelFile() != null) {
            LOGGER.info("Overriding model path {} set in Config File with {}",
                    config.getPersistence().getInitialModelFile(),
                    getEnvValue(ENV_PATH_MODEL_FILE));
        }
        config.getPersistence().setInitialModelFile(new File(getEnvValue(ENV_PATH_MODEL_FILE)));
        modelFile = new File(getEnvValue(ENV_PATH_MODEL_FILE));
    }


    private ServiceConfig withModel(ServiceConfig config) {
        if (spec.commandLine().getParseResult().hasMatchedOption(COMMAND_MODEL)) {
            withModelFromCommandLine(config);
            return config;
        }
        if (getEnvValue(ENV_PATH_MODEL_FILE) != null && !getEnvValue(ENV_PATH_MODEL_FILE).isBlank()) {
            withModelFromEnvironmentVariable(config);
            return config;
        }
        if (config.getPersistence().getInitialModelFile() != null) {
            LOGGER.info("Model: {} (CONFIG)", config.getPersistence().getInitialModelFile());
            return config;
        }
        if (!useEmptyModel) {
            Optional<File> defaultModel = findDefaultModel();
            if (defaultModel.isPresent()) {
                LOGGER.info("Model: {} (default location)", defaultModel.get().getAbsoluteFile());
                config.getPersistence().setInitialModelFile(defaultModel.get());
                modelFile = new File(defaultModel.get().getAbsolutePath());
                return config;
            }
        }
        if (useEmptyModel) {
            LOGGER.info("Model: empty (CLI)");
        }
        else {
            LOGGER.info("Model: empty (default)");
        }
        LOGGER.info("Model validation is disabled when using empty model");
        config.getCore().setValidationOnLoad(ModelValidatorConfig.NONE);
        config.getPersistence().setInitialModel(new DefaultEnvironment.Builder().build());
        return config;
    }


    private ServiceConfig withEndpoints(ServiceConfig config) {
        try {
            ServiceConfigHelper.apply(config, endpoints.stream()
                    .map(LambdaExceptionHelper.rethrowFunction(
                            x -> x.getImplementation().getDeclaredConstructor().newInstance()))
                    .collect(Collectors.toList()));
            return config;
        }
        catch (InvalidConfigurationException | ReflectiveOperationException e) {
            throw new InitializationException("Adding endpoints to config failed", e);
        }
    }


    private ServiceConfig withOverrides(ServiceConfig config) {
        try {
            return ServiceConfigHelper.withProperties(config, getConfigOverrides(config));
        }
        catch (JsonProcessingException e) {
            throw new InitializationException("Overriding config properties failed", e);
        }
    }


    private void printConfig(ServiceConfig config) {
        if (LOGGER.isDebugEnabled()) {
            try {
                LOGGER.debug(mapper.writeValueAsString(config));
            }
            catch (JsonProcessingException e) {
                LOGGER.debug("Printing config failed", e);
            }
        }
    }


    private void printEndpointInfo(ServiceConfig config) {
        if (LOGGER.isInfoEnabled()) {
            config.getEndpoints().stream().forEach(x -> {
                if (HttpEndpointConfig.class.isAssignableFrom(x.getClass())) {
                    LOGGER.info("HTTP endpoint available on port {}", ((HttpEndpointConfig) x).getPort());
                }
                else if (OpcUaEndpointConfig.class.isAssignableFrom(x.getClass())) {
                    LOGGER.info("OPC UA endpoint available on port {}", ((OpcUaEndpointConfig) x).getTcpPort());
                }
            });
        }
    }


    private void printHeader() {
        LOGGER.info("            _____                                                       ");
        LOGGER.info("           |___ /                                                       ");
        LOGGER.info(" ______      |_ \\    _____ _______     _____                 _          ");
        LOGGER.info("|  ____/\\   ___) | / ____|__   __|    / ____|               (_)         ");
        LOGGER.info("| |__ /  \\ |____/ | (___    | |      | (___   ___ _ ____   ___  ___ ___ ");
        LOGGER.info("|  __/ /\\ \\        \\___ \\   | |       \\___ \\ / _ \\ '__\\ \\ / / |/ __/ _ \\");
        LOGGER.info("| | / ____ \\       ____) |  | |       ____) |  __/ |   \\ V /| | (_|  __/");
        LOGGER.info("|_|/_/    \\_\\     |_____/   |_|      |_____/ \\___|_|    \\_/ |_|\\___\\___|");
        LOGGER.info("");
        LOGGER.info("-------------------------------------------------------------------------");
        try {
            Stream.of(new PropertiesVersionProvider().getVersion()).forEach(LOGGER::info);
        }
        catch (Exception e) {
            LOGGER.info("error determining version info (reason: {})", e.getMessage());
        }
        LOGGER.info("-------------------------------------------------------------------------");
    }


    private List<ConfigOverride> getConfigOverridesFromEnv() {
        return List.of(ENV_PREFIX_CONFIG_EXTENSION, ENV_PREFIX_CONFIG_EXTENSION_ALTERNATIVE)
                .stream()
                .flatMap(prefix -> System.getenv().entrySet().stream()
                        .filter(x -> x.getKey().startsWith(prefix))
                        .filter(x -> !properties.containsKey(x.getKey().substring(prefix.length() - 1)))
                        .map(x -> ConfigOverride.builder()
                                .originalKey(x.getKey().substring(prefix.length()))
                                .value(x.getValue())
                                .source(ConfigOverrideSource.ENV)
                                .build()))
                .toList();
    }


    private List<ConfigOverride> getConfigOverridesFromCli() {
        return properties.entrySet().stream()
                .map(x -> {
                    String key = x.getKey();
                    if (key.startsWith(ENV_PREFIX_CONFIG_EXTENSION)) {
                        key = key.substring(ENV_PREFIX_CONFIG_EXTENSION.length());
                        LOGGER.info("Found unnecessary prefix for CLI parameter '{}' (remove prefix '{}' to not receive this message any longer)", key,
                                ENV_PREFIX_CONFIG_EXTENSION);
                    }
                    return ConfigOverride.builder()
                            .originalKey(key)
                            .value(x.getValue())
                            .source(ConfigOverrideSource.CLI)
                            .build();
                }).toList();
    }


    private List<ConfigOverride> enforceSourcePrecedence(List<ConfigOverride> overrides) {
        Predicate<ConfigOverride> condition = x -> x.getSource() == ConfigOverrideSource.ENV
                && overrides.stream().anyMatch(y -> y.getSource() == ConfigOverrideSource.CLI && Objects.equals(x.getUpdatedKey(), y.getUpdatedKey()));
        return overrides.stream()
                .filter(condition.negate())
                .toList();
    }


    /**
     * Collects config overrides from environment and CLI parameters.
     *
     * @return map of config overrides
     */
    protected List<ConfigOverride> getConfigOverrides() {
        return enforceSourcePrecedence(
                Stream.concat(
                        getConfigOverridesFromEnv().stream(),
                        getConfigOverridesFromCli().stream())
                        .toList());
    }


    /**
     * Collects config overrides from environment and CLI parameters.
     *
     * @param config used to replace certain separators
     *
     * @return map of config overrides
     */
    protected List<ConfigOverride> getConfigOverrides(ServiceConfig config) {
        List<ConfigOverride> result = getConfigOverrides();
        result = replaceSeparators(config, result);
        if (!result.isEmpty()) {
            LOGGER.info("Overriding config parameter: {}{}",
                    System.lineSeparator(),
                    result.stream()
                            .map(x -> indent(
                                    String.format("%s=%s [%s]",
                                            x.getUpdatedKey(),
                                            x.getValue(),
                                            x.getSource()),
                                    1))
                            .collect(Collectors.joining(System.lineSeparator())));
        }
        return result;
    }


    private List<ConfigOverride> replaceSeparators(ServiceConfig config, List<ConfigOverride> configOverrides) {
        DocumentContext document = JsonPath.using(JSON_PATH_CONFIG).parse(mapper.valueToTree(config));
        return configOverrides.stream()
                .map(x -> {
                    List<String> pathParts = new LinkedList<>(Arrays.asList(x.getOriginalKey().split(ENV_PATH_ALTERNATIVE_SEPARATOR)));
                    return ConfigOverride.builder()
                            .originalKey(x.getOriginalKey())
                            .updatedKey(getNewPathRecursive(document, pathParts.get(0), pathParts, 1))
                            .value(x.getValue())
                            .source(x.getSource())
                            .build();
                }).toList();
    }


    private String getNewPathRecursive(DocumentContext document, String currPath, List<String> pathParts, int nextPartIndex) {
        String jsonPath = String.format("$.%s", currPath);
        JsonNode node = document.read(jsonPath);

        if (nextPartIndex >= pathParts.size()) {
            return node == null ? null : currPath;
        }

        String nextPart = pathParts.get(nextPartIndex);

        if (node == null) {
            return getNewPathRecursive(document,
                    currPath + ENV_PATH_ALTERNATIVE_SEPARATOR + nextPart,
                    pathParts,
                    nextPartIndex + 1);
        }
        else {
            String pathWithDot = getNewPathRecursive(document,
                    currPath + ENV_PATH_SEPARATOR + nextPart,
                    pathParts,
                    nextPartIndex + 1);
            String pathWithSeparator = getNewPathRecursive(document,
                    currPath + ENV_PATH_ALTERNATIVE_SEPARATOR + nextPart,
                    pathParts,
                    nextPartIndex + 1);
            if (Objects.nonNull(pathWithDot) && Objects.nonNull(pathWithSeparator)) {
                throw new InitializationException(String.format(
                        "Ambiguity between '%s' and '%s', please set properties through the CLI or a configuration file.",
                        pathWithDot,
                        pathWithSeparator));
            }
            if (Objects.isNull(pathWithDot) && Objects.isNull(pathWithSeparator)) {
                throw new InitializationException(String.format(
                        "Unresolvable environment variable found '%s'.",
                        pathWithDot));
            }
            return Objects.nonNull(pathWithDot) ? pathWithDot : pathWithSeparator;
        }
    }

    /**
     * Provides version information from properies.
     */
    protected static class PropertiesVersionProvider implements IVersionProvider {

        private static final String PATH_GIT_BUILD_VERSION = "git.build.version";
        private static final String PATH_GIT_COMMIT_TIME = "git.commit.time";
        private static final String PATH_GIT_COMMIT_ID_DESCRIBE = "git.commit.id.describe";
        private static final TypeReference<Map<String, String>> TYPE_MAP_STRING_STRING = new TypeReference<Map<String, String>>() {
            // Empty on purpose.
        };

        @Override
        public String[] getVersion() throws Exception {
            Map<String, String> gitInfo = new ObjectMapper().readValue(App.class.getClassLoader().getResourceAsStream("git.json"), TYPE_MAP_STRING_STRING);
            return new String[] {
                    String.format("Version %s", gitInfo.get(PATH_GIT_BUILD_VERSION)),
                    String.format("Git Commit ID: %s", gitInfo.get(PATH_GIT_COMMIT_ID_DESCRIBE)),
                    String.format("Commit Time: %s", gitInfo.get(PATH_GIT_COMMIT_TIME)),
            };
        }
    }
}
