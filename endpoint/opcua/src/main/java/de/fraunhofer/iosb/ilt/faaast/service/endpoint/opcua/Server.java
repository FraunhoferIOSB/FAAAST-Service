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
import com.prosysopc.ua.server.UserValidator;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.UnsignedShort;
import com.prosysopc.ua.stack.cert.DefaultCertificateValidator;
import com.prosysopc.ua.stack.cert.DefaultCertificateValidatorListener;
import com.prosysopc.ua.stack.cert.PkiDirectoryCertificateStore;
import com.prosysopc.ua.stack.core.ApplicationDescription;
import com.prosysopc.ua.stack.core.ApplicationType;
import com.prosysopc.ua.stack.core.MessageSecurityMode;
import com.prosysopc.ua.stack.transport.security.HttpsSecurityPolicy;
import com.prosysopc.ua.stack.transport.security.KeyPair;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import com.prosysopc.ua.stack.transport.security.SecurityPolicy;
import com.prosysopc.ua.types.opcua.server.BuildInfoTypeNode;
import com.prosysopc.ua.types.opcua.server.ServerCapabilitiesTypeNode;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.listener.AasCertificateValidationListener;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.listener.AasServiceIoManagerListener;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
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
    private static final String PRIV_KEY_PASS = "opcua";
    private static final String DISCOVERY_SERVER_URL = "opc.tcp://localhost:4840";

    private final int tcpPort;
    private final AssetAdministrationShellEnvironment aasEnvironment;
    private final OpcUaEndpoint endpoint;

    private UaServer uaServer;
    private boolean running;

    protected UserValidator userValidator;
    protected AasServiceNodeManager aasNodeManager;

    protected final DefaultCertificateValidatorListener validationListener = new AasCertificateValidationListener();
    protected final DefaultCertificateValidatorListener userCertificateValidationListener = new AasCertificateValidationListener();

    /**
     * Creates a new instance of Server.
     *
     * @param portTcp The desired port for the opc.tcp endpoint
     * @param environment The AAS environment
     * @param endpoint the associated endpoint
     */
    public Server(int portTcp, AssetAdministrationShellEnvironment environment, OpcUaEndpoint endpoint) {
        this.tcpPort = portTcp;
        this.aasEnvironment = environment;
        this.endpoint = endpoint;
    }


    /**
     * Starts the server
     *
     * @throws UaServerException If an error occurs
     * @throws IOException If an error occurs
     * @throws SecureIdentityException If an error occurs
     */
    public void startup() throws UaServerException, IOException, SecureIdentityException {
        try {
            String hostName;
            hostName = InetAddress.getLocalHost().getHostName();

            ApplicationIdentity.setActualHostName(hostName);

            uaServer = new UaServer();

            // currently without IPv6
            uaServer.setEnableIPv6(false);

            final PkiDirectoryCertificateStore applicationCertificateStore = new PkiDirectoryCertificateStore("PKI/CA");
            final PkiDirectoryCertificateStore applicationIssuerCertificateStore = new PkiDirectoryCertificateStore("PKI/CA/issuers");
            final DefaultCertificateValidator applicationCertificateValidator = new DefaultCertificateValidator(applicationCertificateStore, applicationIssuerCertificateStore);

            uaServer.setCertificateValidator(applicationCertificateValidator);
            applicationCertificateValidator.setValidationListener(validationListener);

            // Handle user certificates
            final PkiDirectoryCertificateStore userCertificateStore = new PkiDirectoryCertificateStore("USERS_PKI/CA");
            final PkiDirectoryCertificateStore userIssuerCertificateStore = new PkiDirectoryCertificateStore("USERS_PKI/CA/issuers");

            final DefaultCertificateValidator userCertificateValidator = new DefaultCertificateValidator(userCertificateStore, userIssuerCertificateStore);

            userValidator = new AasUserValidator(userCertificateValidator, endpoint.asConfig().getUserMap(), endpoint.asConfig().getAllowAnonymous());
            userCertificateValidator.setValidationListener(userCertificateValidationListener);

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

            KeyPair issuerCertificate = ApplicationIdentity.loadOrCreateIssuerCertificate(
                    "FraunhoferIosbSampleCA@" + ApplicationIdentity.getActualHostNameWithoutDomain() + "_https_" + CERT_KEY_SIZE, privatePath, PRIV_KEY_PASS, 3650, false,
                    CERT_KEY_SIZE);

            int[] keySizes = new int[] {
                    CERT_KEY_SIZE
            };

            final ApplicationIdentity identity = ApplicationIdentity.loadOrCreateCertificate(appDescription, "Fraunhofer IOSB", PRIV_KEY_PASS,
                    privatePath, null, keySizes, true);

            hostName = ApplicationIdentity.getActualHostName();
            identity.setHttpsCertificate(
                    ApplicationIdentity.loadOrCreateHttpsCertificate(appDescription, hostName, PRIV_KEY_PASS, issuerCertificate, privatePath, true, CERT_KEY_SIZE));

            uaServer.setApplicationIdentity(identity);

            Set<SecurityPolicy> supportedSecurityPolicies = new HashSet<>();
            supportedSecurityPolicies.add(SecurityPolicy.NONE);
            supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_101);
            supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_102);
            supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_103);
            supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_104);

            Set<MessageSecurityMode> supportedMessageSecurityModes = new HashSet<>();
            supportedMessageSecurityModes.add(MessageSecurityMode.None);
            supportedMessageSecurityModes.add(MessageSecurityMode.Sign);
            supportedMessageSecurityModes.add(MessageSecurityMode.SignAndEncrypt);
            uaServer.getSecurityModes().addAll(SecurityMode.combinations(supportedMessageSecurityModes, supportedSecurityPolicies));

            uaServer.getHttpsSecurityModes().addAll(SecurityMode.combinations(EnumSet.of(MessageSecurityMode.None, MessageSecurityMode.Sign), supportedSecurityPolicies));

            Set<HttpsSecurityPolicy> supportedHttpsSecurityPolicies = new HashSet<>();
            supportedHttpsSecurityPolicies.addAll(HttpsSecurityPolicy.ALL_102);
            supportedHttpsSecurityPolicies.addAll(HttpsSecurityPolicy.ALL_103);
            supportedHttpsSecurityPolicies.addAll(HttpsSecurityPolicy.ALL_104);
            uaServer.getHttpsSettings().setHttpsSecurityPolicies(supportedHttpsSecurityPolicies);

            uaServer.getHttpsSettings().setCertificateValidator(applicationCertificateValidator);

            // Define the supported user authentication methods
            uaServer.addUserTokenPolicy(UserTokenPolicies.ANONYMOUS);
            uaServer.addUserTokenPolicy(UserTokenPolicies.SECURE_USERNAME_PASSWORD);
            uaServer.addUserTokenPolicy(UserTokenPolicies.SECURE_CERTIFICATE);

            uaServer.setUserValidator(userValidator);

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
        catch (Exception ex) {
            LOGGER.error("startup Exception", ex);
            throw ex;
        }
    }


    private void registerDiscovery() {
        if (endpoint.asConfig().getRegisterWithDiscoveryServer()) {
            try {
                // Register to the local discovery server (if present)
                uaServer.setDiscoveryServerUrl(DISCOVERY_SERVER_URL);
            }
            catch (URISyntaxException e) {
                LOGGER.error("DiscoveryURL is not valid", e);
            }
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
            GregorianCalendar c = new GregorianCalendar();
            c.setTimeInMillis(mfFile.lastModified());
            buildInfo.setBuildDate(new DateTime(c));
        }
    }


    /**
     * Creates the server address space
     */
    private void createAddressSpace() {
        try {
            loadI4AasNodes();

            aasNodeManager = new AasServiceNodeManager(uaServer, AasServiceNodeManager.NAMESPACE_URI, aasEnvironment, endpoint);
            aasNodeManager.getIoManager().addListeners(new AasServiceIoManagerListener(endpoint, aasNodeManager));
            LOGGER.debug("Address space created.");
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

        long dauer = System.currentTimeMillis() - start;
        LOGGER.trace("loadI4AasNodes end. Dauer: {} ms", dauer);
    }
}
