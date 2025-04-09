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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua;

import com.prosysopc.ua.ApplicationIdentity;
import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.UaApplication;
import com.prosysopc.ua.UaApplication.Protocol;
import com.prosysopc.ua.UserTokenPolicies;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.UaServerException;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.UnsignedShort;
import com.prosysopc.ua.stack.cert.DefaultCertificateValidator;
import com.prosysopc.ua.stack.cert.DefaultCertificateValidatorListener;
import com.prosysopc.ua.stack.cert.PkiDirectoryCertificateStore;
import com.prosysopc.ua.stack.core.ApplicationDescription;
import com.prosysopc.ua.stack.core.ApplicationType;
import com.prosysopc.ua.stack.core.MessageSecurityMode;
import com.prosysopc.ua.stack.core.UserTokenPolicy;
import com.prosysopc.ua.stack.core.UserTokenType;
import com.prosysopc.ua.stack.transport.security.HttpsSecurityPolicy;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import com.prosysopc.ua.types.opcua.server.BuildInfoTypeNode;
import com.prosysopc.ua.types.opcua.server.ServerCapabilitiesTypeNode;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.listener.AasCertificateValidationListener;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.listener.AasServiceIoManagerListener;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for the OPC UA server
 */
public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private static final String APPLICATION_NAME = "Fraunhofer IOSB AAS OPC UA Server";
    private static final String APPLICATION_URI = "urn:hostname:Fraunhofer:OPCUA:AasServer";
    private static final int CERT_KEY_SIZE = 2048;
    private static final String ISSUERS_PATH = "issuers";

    private final int tcpPort;
    private final Environment aasEnvironment;
    private final OpcUaEndpoint endpoint;
    private final OpcUaEndpointConfig config;

    private UaServer uaServer;
    private boolean running;

    private final DefaultCertificateValidatorListener validationListener = new AasCertificateValidationListener();
    private final DefaultCertificateValidatorListener userCertificateValidationListener = new AasCertificateValidationListener();

    /**
     * Creates a new instance of Server.
     *
     * @param portTcp The desired port for the opc.tcp endpoint
     * @param environment The AAS environment
     * @param endpoint the associated endpoint
     * @throws IllegalArgumentException if environment is null
     * @throws IllegalArgumentException if endpoint is null
     */
    public Server(int portTcp, Environment environment, OpcUaEndpoint endpoint) {
        Ensure.requireNonNull(environment, "environment most be non-null");
        Ensure.requireNonNull(endpoint, "endpoint most be non-null");
        this.tcpPort = portTcp;
        this.aasEnvironment = environment;
        this.endpoint = endpoint;
        this.config = endpoint.asConfig();
    }


    /**
     * Starts the server.
     *
     * @throws UaServerException If an error occurs
     * @throws IOException If an error occurs
     * @throws SecureIdentityException If an error occurs
     * @throws URISyntaxException if endpoint URL is invalid
     */
    public void startup() throws UaServerException, IOException, SecureIdentityException, URISyntaxException {
        String hostName;
        hostName = InetAddress.getLocalHost().getHostName();

        ApplicationIdentity.setActualHostName(hostName);

        uaServer = new UaServer();

        // currently without IPv6
        uaServer.setEnableIPv6(false);

        final PkiDirectoryCertificateStore applicationCertificateStore = new PkiDirectoryCertificateStore(config.getServerCertificateBasePath());
        final PkiDirectoryCertificateStore applicationIssuerCertificateStore = new PkiDirectoryCertificateStore(
                Paths.get(config.getServerCertificateBasePath(), ISSUERS_PATH).toString());
        final DefaultCertificateValidator applicationCertificateValidator = new DefaultCertificateValidator(applicationCertificateStore, applicationIssuerCertificateStore);

        uaServer.setCertificateValidator(applicationCertificateValidator);
        applicationCertificateValidator.setValidationListener(validationListener);

        // Handle user certificates
        final PkiDirectoryCertificateStore userCertificateStore = new PkiDirectoryCertificateStore(config.getUserCertificateBasePath());
        final PkiDirectoryCertificateStore userIssuerCertificateStore = new PkiDirectoryCertificateStore(
                Paths.get(config.getUserCertificateBasePath(), ISSUERS_PATH).toString());

        final DefaultCertificateValidator userCertificateValidator = new DefaultCertificateValidator(userCertificateStore, userIssuerCertificateStore);
        userCertificateValidator.setValidationListener(userCertificateValidationListener);

        setApplicationIdentity(applicationCertificateStore);

        setSecurityPolicies();

        uaServer.getHttpsSettings().setCertificateValidator(applicationCertificateValidator);

        if (Objects.isNull(config.getSupportedAuthentications()) || config.getSupportedAuthentications().isEmpty()) {
            throw new IllegalArgumentException("no supported authentications available!");
        }

        config.getSupportedAuthentications().forEach(LambdaExceptionHelper.rethrowConsumer(a -> uaServer.addUserTokenPolicy(getUserTokenPolicy(a))));

        uaServer.setUserValidator(new AasUserValidator(
                userCertificateValidator,
                config.getUserMap(),
                config.getSupportedAuthentications()));

        registerDiscovery();
        uaServer.init();

        initBuildInfo();

        // "Safety limits" for ill-behaving clients
        uaServer.getSessionManager().setMaxSessionCount(500);
        uaServer.getSessionManager().setMaxSessionTimeout(3600000);
        uaServer.getSubscriptionManager().setMaxSubscriptionCount(50);

        ServerCapabilitiesTypeNode serverCapabilities = uaServer.getAddressSpace().getNodeManagerRoot().getServerData().getServerCapabilitiesNode();
        serverCapabilities.setMaxBrowseContinuationPoints(UnsignedShort.MAX_VALUE);
        serverCapabilities.setMaxQueryContinuationPoints(UnsignedShort.MAX_VALUE);
        serverCapabilities.setMaxHistoryContinuationPoints(UnsignedShort.MAX_VALUE);

        createAddressSpace();

        uaServer.start();

        running = true;
    }


    private void setApplicationIdentity(final PkiDirectoryCertificateStore applicationCertificateStore) throws IOException, SecureIdentityException, UaServerException {
        ApplicationDescription appDescription = new ApplicationDescription();
        // 'localhost' (all lower case) in the ApplicationName and
        // ApplicationURI is converted to the actual host name of the computer
        // (including the possible domain part) in which the application is run.
        // (as available from ApplicationIdentity.getActualHostName())
        // 'hostname' is converted to the host name without the domain part.
        // (as available from
        // ApplicationIdentity.getActualHostNameWithoutDomain())
        appDescription.setApplicationName(new LocalizedText(APPLICATION_NAME + "@hostname"));
        appDescription.setApplicationUri(APPLICATION_URI);
        appDescription.setProductUri("urn:de:fraunhofer:iosb:opcua:aas:server");
        appDescription.setApplicationType(ApplicationType.Server);
        uaServer.setPort(Protocol.OpcTcp, tcpPort);
        LOGGER.trace("Loading certificates..");
        File privatePath = new File(applicationCertificateStore.getBaseDir(), "private");
        int[] keySizes = new int[] {
                CERT_KEY_SIZE
        };
        final ApplicationIdentity identity = ApplicationIdentity.loadOrCreateCertificate(appDescription, "Fraunhofer IOSB", null,
                privatePath, null, keySizes, true);
        uaServer.setApplicationIdentity(identity);
    }


    private void setSecurityPolicies() {

        if (Objects.isNull(config.getSupportedSecurityPolicies()) || config.getSupportedSecurityPolicies().isEmpty()) {
            throw new IllegalArgumentException("no supported security policies available!");
        }

        uaServer.getSecurityModes().addAll(
                SecurityMode.combinations(
                        Set.of(MessageSecurityMode.values()),
                        config.getSupportedSecurityPolicies()));

        uaServer.getHttpsSecurityModes().addAll(
                SecurityMode.combinations(
                        Set.of(MessageSecurityMode.None, MessageSecurityMode.Sign),
                        config.getSupportedSecurityPolicies()));

        Set<HttpsSecurityPolicy> supportedHttpsSecurityPolicies = new HashSet<>();
        supportedHttpsSecurityPolicies.addAll(HttpsSecurityPolicy.ALL_102);
        supportedHttpsSecurityPolicies.addAll(HttpsSecurityPolicy.ALL_103);
        supportedHttpsSecurityPolicies.addAll(HttpsSecurityPolicy.ALL_104);
        uaServer.getHttpsSettings().setHttpsSecurityPolicies(supportedHttpsSecurityPolicies);
    }


    private void registerDiscovery() throws URISyntaxException {
        if ((endpoint.asConfig().getDiscoveryServerUrl() != null) && (endpoint.asConfig().getDiscoveryServerUrl().length() > 0)) {
            // Register to the local discovery server (if present)
            uaServer.setDiscoveryServerUrl(endpoint.asConfig().getDiscoveryServerUrl());
        }
    }


    /**
     * Stops the OPC UA server
     *
     * @param secondsTillShutdown The number of seconds until the server stops
     */
    public void shutdown(int secondsTillShutdown) {
        running = false;
        uaServer.shutdown(secondsTillShutdown, new LocalizedText("Server stopped", Locale.ENGLISH));
    }


    /**
     * Indicates whether the server is running
     *
     * @return True if the server is running, false otherwise
     */
    public boolean isRunning() {
        return running;
    }


    /**
     * Initialize the information for the Server BuildInfo structure
     */
    protected void initBuildInfo() {
        // Initialize BuildInfo - using the version info from the SDK
        // You should replace this with your own build information

        final BuildInfoTypeNode buildInfo = uaServer.getNodeManagerRoot().getServerData().getServerStatusNode().getBuildInfoNode();

        buildInfo.setProductName(APPLICATION_NAME);

        final String implementationVersion = UaApplication.getSdkVersion();
        if (implementationVersion != null) {
            int splitIndex = implementationVersion.lastIndexOf("-");
            final String softwareVersion = splitIndex == -1 ? "dev" : implementationVersion.substring(0, splitIndex);
            String buildNumber = splitIndex == -1 ? "dev" : implementationVersion.substring(splitIndex + 1);

            buildInfo.setManufacturerName("Prosys OPC Ltd");
            buildInfo.setSoftwareVersion(softwareVersion);
            buildInfo.setBuildNumber(buildNumber);

        }

        final URL classFile = UaServer.class.getResource("/de/fraunhofer/iosb/ilt/aas/service/protocol/Server.class");
        if (classFile != null && classFile.getFile() != null) {
            final File mfFile = new File(classFile.getFile());
            buildInfo.setBuildDate(DateTime.fromMillis(mfFile.lastModified()));
        }
    }


    private static UserTokenPolicy getUserTokenPolicy(UserTokenType userTokenType) {
        switch (userTokenType) {
            case Anonymous:
                return UserTokenPolicies.ANONYMOUS;
            case UserName:
                return UserTokenPolicies.SECURE_USERNAME_PASSWORD_BASIC256SHA256;
            case Certificate:
                return UserTokenPolicies.SECURE_CERTIFICATE_BASIC256SHA256;
            default:
                throw new IllegalArgumentException(String.format("unsupported UserTokenType '%s'", userTokenType));
        }
    }


    /**
     * Creates the server address space
     */
    private void createAddressSpace() {
        try {
            loadI4AasNodes();
            AasServiceNodeManager aasNodeManager = new AasServiceNodeManager(uaServer, AasServiceNodeManager.NAMESPACE_URI, aasEnvironment, endpoint);
            aasNodeManager.getIoManager().addListeners(new AasServiceIoManagerListener(endpoint, aasNodeManager));
        }
        catch (Exception ex) {
            LOGGER.error("createAddressSpace Exception", ex);
        }
    }


    /**
     * Loads the AAS nodes from the NodeSet file
     */
    private void loadI4AasNodes() {
        long start = System.currentTimeMillis();
        try {
            LOGGER.debug("loadI4AasNodes start I4AAS");
            uaServer.getAddressSpace().loadModel(opc.i4aas.server.ServerInformationModel.getLocationURI());
        }
        catch (Exception ex) {
            LOGGER.error("loadI4AasNodes Exception", ex);
        }

        long duration = System.currentTimeMillis() - start;
        LOGGER.trace("loadI4AasNodes end. Dauer: {} ms", duration);
    }
}
