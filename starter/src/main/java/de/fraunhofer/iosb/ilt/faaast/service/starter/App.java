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
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentSerializationManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.InvalidConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValidationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidator;
import de.fraunhofer.iosb.ilt.faaast.service.model.validation.ModelValidatorConfig;
import de.fraunhofer.iosb.ilt.faaast.service.starter.cli.LogLevelTypeConverter;
import de.fraunhofer.iosb.ilt.faaast.service.starter.logging.FaaastFilter;
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
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
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
    // config
    protected static final String CONFIG_FILENAME_DEFAULT = "config.json";
    // environment
    protected static final String ENV_PATH_SEPARATOR = ".";
    protected static final String ENV_PATH_ALTERNATIVE_SEPARATOR = "_";
    protected static final String JSON_PATH_SEPARATOR = ".";
    protected static final String ENV_KEY_PREFIX = "faaast";
    protected static final String ENV_PATH_CONFIG_FILE = envPath(ENV_KEY_PREFIX, "config");
    protected static final String ENV_PATH_MODEL_FILE = envPath(ENV_KEY_PREFIX, "model");
    protected static final String ENV_PREFIX_CONFIG_EXTENSION = envPath(ENV_KEY_PREFIX, "config", "extension", "");
    // model
    protected static final String MODEL_FILENAME_DEFAULT = "aasenvironment.*";
    protected static final String MODEL_FILENAME_PATTERN = "aasenvironment\\..*";
    private static final Configuration JSON_PATH_CONFIG = Configuration
            .builder()
            .mappingProvider(new JacksonMappingProvider())
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build()
            .addOptions(com.jayway.jsonpath.Option.SUPPRESS_EXCEPTIONS);

    @Option(names = "--no-autoCompleteConfig", negatable = true, description = "Autocompletes the configuration with default values for required configuration sections. True by default")
    public boolean autoCompleteConfiguration = true;

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

    @Option(names = "--emptyModel", description = "Starts the FA³ST service with an empty Asset Administration Shell Environment. False by default")
    public boolean useEmptyModel = false;

    @Option(names = "--no-validation", negatable = false, description = "Disables validation, overrides validation configuration in core configuration.")
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
        if (noValidation) {
            config.getCore().setValidationOnLoad(ModelValidatorConfig.NONE);
            config.getCore().setValidationOnCreate(ModelValidatorConfig.NONE);
            config.getCore().setValidationOnUpdate(ModelValidatorConfig.NONE);
        }
        try {
            Environment model = config.getPersistence().getInitialModel() == null
                    ? EnvironmentSerializationManager.deserialize(config.getPersistence().getInitialModelFile()).getEnvironment()
                    : config.getPersistence().getInitialModel();
            if (!config.getCore().getValidationOnLoad().isEnabled()) {
                LOGGER.info("ValidateOnLoad is disabled in core config, no validation will be performed.");
                return;
            }
            LOGGER.debug("Validating model...");
            LOGGER.debug("Constraint validation: {}", config.getCore().getValidationOnLoad().getValidateConstraints());
            LOGGER.debug("ValueType validation: {}", config.getCore().getValidationOnLoad().getValueTypeValidation());
            LOGGER.debug("IdShort uniqueness validation: {}", config.getCore().getValidationOnLoad().getIdShortUniqueness());
            LOGGER.debug("Identifier uniqueness validation: {}", config.getCore().getValidationOnLoad().getIdentifierUniqueness());
            ModelValidator.validate(model, config.getCore().getValidationOnLoad());
            LOGGER.info("Model successfully validated");
        }
        catch (DeserializationException e) {
            throw new InitializationException("Error loading model file", e);
        }
        catch (ValidationException e) {
            throw new InitializationException(
                    String.format("Model validation failed with the following error(s):%s%s",
                            System.lineSeparator(),
                            e.getMessage()));
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
        if (logLevelExternal != null) {
            FaaastFilter.setLevelExternal(logLevelExternal);
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
            throw new InitializationException("Error loading config file", e);
        }
        if (autoCompleteConfiguration) {
            ServiceConfigHelper.autoComplete(config);
        }
        withModel(config);
        try {
            ServiceConfigHelper.apply(config, endpoints.stream()
                    .map(LambdaExceptionHelper.rethrowFunction(
                            x -> x.getImplementation().getDeclaredConstructor().newInstance()))
                    .collect(Collectors.toList()));
        }
        catch (InvalidConfigurationException | ReflectiveOperationException e) {
            throw new InitializationException("Adding endpoints to config failed", e);
        }
        try {
            config = ServiceConfigHelper.withProperties(config, getConfigOverrides(config));
        }
        catch (JsonProcessingException e) {
            throw new InitializationException("Overriding config properties failed", e);
        }
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
            LOGGER.error("Unexpected exception encountered while executing FA³ST Service", e);
        }
    }


    private Optional<File> findDefaultModel() {
        try {
            List<File> modelFiles;
            try (Stream<File> stream = Files.find(Paths.get(""), 1,
                    (file, attributes) -> file.toFile()
                            .getName()
                            .matches(MODEL_FILENAME_PATTERN))
                    .map(Path::toFile)) {
                modelFiles = stream.collect(Collectors.toList());
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


    private void withModel(ServiceConfig config) {
        String fileExtension = FileHelper.getFileExtensionWithoutSeparator(modelFile);
        if (spec.commandLine().getParseResult().hasMatchedOption(COMMAND_MODEL)) {
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
            if (fileExtension.equals("aasx")) {
                config.getFileStorage().setInitialModelFile(modelFile);
            }
            return;

        }
        if (getEnvValue(ENV_PATH_MODEL_FILE) != null && !getEnvValue(ENV_PATH_MODEL_FILE).isBlank()) {
            LOGGER.info("Model: {} (ENV)", getEnvValue(ENV_PATH_MODEL_FILE));
            if (config.getPersistence().getInitialModelFile() != null) {
                LOGGER.info("Overriding model path {} set in Config File with {}",
                        config.getPersistence().getInitialModelFile(),
                        getEnvValue(ENV_PATH_MODEL_FILE));
            }
            config.getPersistence().setInitialModelFile(new File(getEnvValue(ENV_PATH_MODEL_FILE)));
            if (fileExtension.equals("aasx")) {
                config.getFileStorage().setInitialModelFile(new File(getEnvValue(ENV_PATH_MODEL_FILE)));
            }
            modelFile = new File(getEnvValue(ENV_PATH_MODEL_FILE));
            return;
        }

        if (config.getPersistence().getInitialModelFile() != null) {
            LOGGER.info("Model: {} (CONFIG)", config.getPersistence().getInitialModelFile());
            return;
        }

        Optional<File> defaultModel = findDefaultModel();
        if (defaultModel.isPresent()) {
            LOGGER.info("Model: {} (default location)", defaultModel.get().getAbsoluteFile());
            config.getPersistence().setInitialModelFile(defaultModel.get());
            if (fileExtension.equals("aasx")) {
                config.getFileStorage().setInitialModelFile(defaultModel.get());
            }
            modelFile = new File(defaultModel.get().getAbsolutePath());
            return;
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
                // TODO re-add once OPC UA Endpoint is updated to AAS4j
                //else if (OpcUaEndpointConfig.class.isAssignableFrom(x.getClass())) {
                //    LOGGER.info("OPC UA endpoint available on port {}", ((OpcUaEndpointConfig) x).getTcpPort());
                //}
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


    /**
     * Collects config overrides from environment and CLI parameters.
     *
     * @return map of config overrides
     */
    protected Map<String, String> getConfigOverrides() {
        Map<String, String> envParameters = System.getenv().entrySet().stream()
                .filter(x -> x.getKey().startsWith(ENV_PREFIX_CONFIG_EXTENSION))
                .filter(x -> !properties.containsKey(x.getKey().substring(ENV_PREFIX_CONFIG_EXTENSION.length() - 1)))
                .collect(Collectors.toMap(x -> x.getKey().substring(ENV_PREFIX_CONFIG_EXTENSION.length()),
                        Entry::getValue));
        Map<String, String> result = new HashMap<>(envParameters);
        for (var property: properties.entrySet()) {
            if (property.getKey().startsWith(ENV_PREFIX_CONFIG_EXTENSION)) {
                String realKey = property.getKey().substring(ENV_PREFIX_CONFIG_EXTENSION.length());
                LOGGER.info("Found unnecessary prefix for CLI parameter '{}' (remove prefix '{}' to not receive this message any longer)", realKey, ENV_PREFIX_CONFIG_EXTENSION);
                result.put(realKey, property.getValue());
            }
            else {
                result.put(property.getKey(), property.getValue());
            }
        }
        if (!result.isEmpty()) {
            LOGGER.info("Overriding config parameter: {}{}",
                    System.lineSeparator(),
                    result.entrySet().stream()
                            .map(x -> indent(
                                    String.format("%s=%s [%s]",
                                            x.getKey(),
                                            x.getValue(),
                                            properties.containsKey(x.getKey()) ? "CLI" : "ENV"),
                                    1))
                            .collect(Collectors.joining(System.lineSeparator())));
        }
        return result;
    }


    /**
     * Collects config overrides from environment and CLI parameters.
     *
     * @param config used to replace certain separators
     *
     * @return map of config overrides
     */
    protected Map<String, String> getConfigOverrides(ServiceConfig config) {
        return replaceSeparators(config, getConfigOverrides());
    }


    private Map<String, String> replaceSeparators(ServiceConfig config, Map<String, String> configOverrides) {
        Map<String, String> result = new HashMap<>();
        DocumentContext document = JsonPath.using(JSON_PATH_CONFIG).parse(mapper.valueToTree(config));
        configOverrides.forEach((k, v) -> {
            List<String> pathParts = new LinkedList<>(Arrays.asList(k.split(ENV_PATH_ALTERNATIVE_SEPARATOR)));
            String newPath = getNewPathRecursive(document, pathParts.get(0), pathParts, 1);
            result.put(newPath, v);
        });
        return result;
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
