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
import java.net.URL;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tino Bischoff
 */
@SuppressWarnings({
        "java:S125",
        "java:S2139"
})
public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);
    private static final String APPLICATION_NAME = "Fraunhofer IOSB AAS OPC UA Server";
    private static final String APPLICATION_URI = "urn:hostname:Fraunhofer:OPCUA:AasServer";
    private static final int CERT_KEY_SIZE = 2048;
    private static final String PRIV_KEY_PASS = "opcua";

    private final int tcpPort;
    private final AssetAdministrationShellEnvironment aasEnvironment;
    private final OpcUaEndpoint endpoint;

    private UaServer uaServer;
    private boolean running;
    @SuppressWarnings("java:S1450")
    private UserValidator userValidator;
    @SuppressWarnings("java:S1450")
    private AasServiceNodeManager aasNodeManager;

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

            // *** Create the server
            uaServer = new UaServer();

            // currently without IPv6
            uaServer.setEnableIPv6(false);

            // Use PKI files to keep track of the trusted and rejected client
            // certificates...
            final PkiDirectoryCertificateStore applicationCertificateStore = new PkiDirectoryCertificateStore("PKI/CA");
            final PkiDirectoryCertificateStore applicationIssuerCertificateStore = new PkiDirectoryCertificateStore("PKI/CA/issuers");
            final DefaultCertificateValidator applicationCertificateValidator = new DefaultCertificateValidator(applicationCertificateStore, applicationIssuerCertificateStore);

            uaServer.setCertificateValidator(applicationCertificateValidator);
            // ...and react to validation results with a custom handler
            applicationCertificateValidator.setValidationListener(validationListener);

            // Handle user certificates
            final PkiDirectoryCertificateStore userCertificateStore = new PkiDirectoryCertificateStore("USERS_PKI/CA");
            final PkiDirectoryCertificateStore userIssuerCertificateStore = new PkiDirectoryCertificateStore("USERS_PKI/CA/issuers");

            final DefaultCertificateValidator userCertificateValidator = new DefaultCertificateValidator(userCertificateStore, userIssuerCertificateStore);

            userValidator = new AasUserValidator(userCertificateValidator);
            // ...and react to validation results with a custom handler
            userCertificateValidator.setValidationListener(userCertificateValidationListener);

            // *** Application Description is sent to the clients
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

            // *** Server Endpoints
            // TCP Port number for the UA TCP protocol
            uaServer.setPort(Protocol.OpcTcp, tcpPort);
            // TCP Port for the HTTPS protocol - currently disabled
            //server.setPort(Protocol.OpcHttps, httpsPort);

            // optional server name part of the URI (default for all protocols)
            //server.setServerName("OPCUA/" + applicationName);
            // Optionally restrict the InetAddresses to which the server is bound.
            // You may also specify the addresses for each Protocol.
            // The default is binding to IPv6 wildcard '[::]' when isEnableIPv6 is true
            // or to IPv4 wildcard '0.0.0.0' otherwise.
            // Alternatively, the Server can be bound to all available InetAddresses.
            // isEnableIPv6 defines whether IPv6 address should be included in the bound addresses.
            // Note that it requires Java 7 or later to work in practice in Windows
            // server.setBindAddresses(EndpointUtil.getInetAddresses(server.isEnableIPv6()));
            // *** Certificates
            LOGGER.info("Loading certificates..");

            File privatePath = new File(applicationCertificateStore.getBaseDir(), "private");

            // Define a certificate for a Certificate Authority (CA) which is used
            // to issue the keys. Especially
            // the HTTPS certificate should be signed by a CA certificate, in order
            // to make the .NET applications trust it.
            //
            // If you have a real CA, you should use that instead of this sample CA
            // and create the keys with it.
            // Here we use the IssuerCertificate only to sign the HTTPS certificate
            // (below) and not the Application Instance Certificate.
            KeyPair issuerCertificate = ApplicationIdentity.loadOrCreateIssuerCertificate(
                    "FraunhoferIosbSampleCA@" + ApplicationIdentity.getActualHostNameWithoutDomain() + "_https_" + CERT_KEY_SIZE, privatePath, PRIV_KEY_PASS, 3650, false,
                    CERT_KEY_SIZE);

            int[] keySizes = new int[] {
                    CERT_KEY_SIZE
            };

            // If you wish to use big certificates (4096 bits), you will need to
            // define two certificates for your application, since to interoperate
            // with old applications, you will also need to use a small certificate
            // (up to 2048 bits).
            // Also, 4096 bits can only be used with Basic256Sha256 security
            // profile, which is currently not enabled by default, so we will also
            // leave the the keySizes array as null. In that case, the default key
            // size defined by CertificateUtils.getKeySize() is used.
            // keySizes = new int[] { 2048, 4096 };
            // *** Application Identity
            // Define the Server application identity, including the Application
            // Instance Certificate (but don't sign it with the issuerCertificate as
            // explained above).
            final ApplicationIdentity identity = ApplicationIdentity.loadOrCreateCertificate(appDescription, "Fraunhofer IOSB", /* Private Key Password */ PRIV_KEY_PASS,
                    /* Key File Path */ privatePath, /* Issuer Certificate & Private Key */ null, /* Key Sizes for instance certificates to create */ keySizes,
                    /* Enable renewing the certificate */ true);

            // Create the HTTPS certificate bound to the hostname.
            // The HTTPS certificate must be created, if you enable HTTPS.
            hostName = ApplicationIdentity.getActualHostName();
            identity.setHttpsCertificate(
                    ApplicationIdentity.loadOrCreateHttpsCertificate(appDescription, hostName, PRIV_KEY_PASS, issuerCertificate, privatePath, true, CERT_KEY_SIZE));

            uaServer.setApplicationIdentity(identity);

            // *** Security settings
            /*
             * Define the security modes to support for the Binary protocol.
             * Note that different versions of the specification might add/deprecate some modes, in this
             * example all the modes are added, but you should add some way in your application to configure
             * these. The set is empty by default, you must add at least one SecurityMode for the server to
             * start.
             */
            Set<SecurityPolicy> supportedSecurityPolicies = new HashSet<>();

            /*
             * This policy does not support any security. Should only be used in isolated networks.
             */
            supportedSecurityPolicies.add(SecurityPolicy.NONE);

            // Modes defined in previous versions of the specification
            supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_101);
            supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_102);
            supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_103);

            /*
             * Per the 1.04 specification, only these should be used. However in practice this list only
             * contains very new security policies, which most of the client applications as of today that
             * are used might not be unable to (yet) use.
             */
            supportedSecurityPolicies.addAll(SecurityPolicy.ALL_SECURE_104);

            Set<MessageSecurityMode> supportedMessageSecurityModes = new HashSet<>();

            /*
             * This mode does not support any security. Should only be used in isolated networks. This is
             * also the only mode, which does not require certificate exchange between the client and server
             * application (when used in conjunction of only ANONYMOUS UserTokenPolicy).
             */
            supportedMessageSecurityModes.add(MessageSecurityMode.None);

            /*
             * This mode support signing, so it is possible to detect if messages are tampered. Note that
             * they are not encrypted.
             */
            supportedMessageSecurityModes.add(MessageSecurityMode.Sign);

            /*
             * This mode signs and encrypts the messages. Only this mode is recommended outside of isolated
             * networks.
             */
            supportedMessageSecurityModes.add(MessageSecurityMode.SignAndEncrypt);

            /*
             * This creates all possible combinations (NONE pairs only with None) of the configured
             * MessageSecurityModes and SecurityPolicies) for opc.tcp communication.
             */
            uaServer.getSecurityModes().addAll(SecurityMode.combinations(supportedMessageSecurityModes, supportedSecurityPolicies));

            /*
             * NOTE! The MessageSecurityMode.None for HTTPS means Application level authentication is not
             * used. If used in combination with the UserTokenPolicy ANONYMOUS anyone can access the server
             * (but the traffic is encrypted). HTTPS mode is always encrypted, therefore the given
             * MessageSecurityMode only affects if the UA certificates are exchanged when forming the
             * Session.
             */
            uaServer.getHttpsSecurityModes().addAll(SecurityMode.combinations(EnumSet.of(MessageSecurityMode.None, MessageSecurityMode.Sign), supportedSecurityPolicies));

            // The TLS security policies to use for HTTPS
            Set<HttpsSecurityPolicy> supportedHttpsSecurityPolicies = new HashSet<>();
            // (HTTPS was defined starting from OPC UA Specification 1.02)
            supportedHttpsSecurityPolicies.addAll(HttpsSecurityPolicy.ALL_102);
            supportedHttpsSecurityPolicies.addAll(HttpsSecurityPolicy.ALL_103);
            // Only these are recommended by the 1.04 Specification
            supportedHttpsSecurityPolicies.addAll(HttpsSecurityPolicy.ALL_104);
            uaServer.getHttpsSettings().setHttpsSecurityPolicies(supportedHttpsSecurityPolicies);

            // Number of threads to reserve for the HTTPS server, default is 10
            // server.setHttpsWorkerThreadCount(10);
            // Define the certificate validator for the HTTPS certificates;
            // we use the same validator that we use for Application Instance Certificates
            uaServer.getHttpsSettings().setCertificateValidator(applicationCertificateValidator);

            // Define the supported user authentication methods
            uaServer.addUserTokenPolicy(UserTokenPolicies.ANONYMOUS);
            uaServer.addUserTokenPolicy(UserTokenPolicies.SECURE_USERNAME_PASSWORD);
            uaServer.addUserTokenPolicy(UserTokenPolicies.SECURE_CERTIFICATE);

            // Define a validator for checking the user accounts
            uaServer.setUserValidator(userValidator);

            // currently skip discovery
            //        // Register to the local discovery server (if present)
            //        try {
            //            server.setDiscoveryServerUrl(DISCOVERY_SERVER_URL);
            //        }
            //        catch (URISyntaxException e) {
            //            logger.error("DiscoveryURL is not valid", e);
            //        }
            // *** 'init' creates the service handlers and the default endpoints
            // *** according to the settings defined above
            uaServer.init();

            initBuildInfo();

            // "Safety limits" for ill-behaving clients
            uaServer.getSessionManager().setMaxSessionCount(500);
            uaServer.getSessionManager().setMaxSessionTimeout(3600000); // one hour
            uaServer.getSubscriptionManager().setMaxSubscriptionCount(50);

            /*
             * Safety limits for XXXContinuationPoints. Note! These are the current defaults. Technically a
             * value of 0 (unlimited) is allowed by the OPC UA Specification, but our implementation does
             * allocate server-side memory, thus do not use value of 0 (or you can run out of memory).
             * Future SDK releases may improve this.
             */
            ServerCapabilitiesTypeNode serverCapabilities = uaServer.getAddressSpace().getNodeManagerRoot().getServerData().getServerCapabilitiesNode();
            serverCapabilities.setMaxBrowseContinuationPoints(UnsignedShort.MAX_VALUE);
            serverCapabilities.setMaxQueryContinuationPoints(UnsignedShort.MAX_VALUE);
            serverCapabilities.setMaxHistoryContinuationPoints(UnsignedShort.MAX_VALUE);

            // You can do your own additions to server initializations here
            createAddressSpace();

            uaServer.start();

            running = true;
        }
        catch (Exception ex) {
            LOGGER.error("startup Exception", ex);
            throw ex;
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

            // AASService Node Manager
            aasNodeManager = new AasServiceNodeManager(uaServer, AasServiceNodeManager.NAMESPACE_URI, aasEnvironment, endpoint);

            // My I/O Manager Listener
            aasNodeManager.getIoManager().addListeners(new AasServiceIoManagerListener(endpoint, aasNodeManager));

            LOGGER.info("Address space created.");
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
            LOGGER.info("loadI4AasNodes start I4AAS");
            uaServer.getAddressSpace().loadModel(opc.i4aas.server.ServerInformationModel.getLocationURI());
        }
        catch (Exception ex) {
            LOGGER.error("loadI4AasNodes Exception", ex);
        }

        long dauer = System.currentTimeMillis() - start;
        LOGGER.info("loadI4AasNodes end. Dauer: {} ms", dauer);
    }
}
