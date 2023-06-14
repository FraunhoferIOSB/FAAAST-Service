package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.security;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


  /**
   * Handle self-signed certificates. Identify self-signed certificates by checking the given certificate against a list
   * of CA's and allow self-signed certificates to be authorized.
   */

public class SelfSignedCertificateHandler extends HttpClient {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);
    public static final String JKS = "JKS";
    private final SSLContext sslContext;
    private final SSLParameters sslParameters;
    private final Duration connectTimeout;
    private final Redirect followRedirects;
    private final Version version;

    /**
     * constructor.
     */
    public SelfSignedCertificateHandler() throws NoSuchAlgorithmException, KeyManagementException {
        super();
        this.sslContext = createCustomSSLContext();
        this.sslParameters = sslContext.getDefaultSSLParameters();
        this.connectTimeout = DEFAULT_TIMEOUT;
        this.followRedirects = Redirect.NORMAL;
        this.version = Version.HTTP_1_1;
    }

    private SSLContext createCustomSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustManagers = { new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                // For production use, it is crucial to perform proper certificate validation
                // including checking the certificate chain, expiration, and other security criteria.
               List<X509Certificate> trustedCerts = getTrustedCertificates();

                for (X509Certificate cert : chain) {
                    // Check if the certificate is in the trusted certificates list
                    isSelfSigned(cert);
                    if (!trustedCerts.contains(cert)) {
                        throw new CertificateException("Certificate is not trusted.");
                    }}}
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        } };

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, new SecureRandom());

        return sslContext;
    }

    private List<X509Certificate> getTrustedCertificates() {
        try {
            // Load the keystore file containing the trusted certificates
            KeyStore trustStore = KeyStore.getInstance(JKS);
            FileInputStream fis = new FileInputStream("path/to/truststore.jks");
            trustStore.load(fis, "truststorepassword".toCharArray());

            // Iterate over the trusted certificates and add them to the list
            List<X509Certificate> trustedCerts = new ArrayList<>();
            Enumeration<String> aliases = trustStore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                X509Certificate cert = (X509Certificate) trustStore.getCertificate(alias);
                trustedCerts.add(cert);
            }

            return trustedCerts;
        } catch (Exception e) {
            //TODO- Handle the exception appropriately
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private boolean isSelfSigned(X509Certificate certificate) {
        try {
            certificate.verify(certificate.getPublicKey());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Optional<CookieHandler> cookieHandler() {
        return Optional.empty();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return Optional.of(connectTimeout);
    }

    @Override
    public Redirect followRedirects() {
        return followRedirects;
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return Optional.empty();
    }

    @Override
    public SSLContext sslContext() {
        return sslContext;
    }

    @Override
    public SSLParameters sslParameters() {
        return sslParameters;
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return Optional.empty();
    }

    @Override
    public Version version() {
        return version;
    }

    @Override
    public Optional<Executor> executor() {
        return Optional.empty();
    }

    @Override
    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(sslParameters)
                .build()
                .send(request, responseBodyHandler);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                            HttpResponse.BodyHandler<T> responseBodyHandler) {
        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(sslParameters)
                .build()
                .sendAsync(request, responseBodyHandler);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request,
                                                            HttpResponse.BodyHandler<T> responseBodyHandler,
                                                            HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .sslParameters(sslParameters)
                .build()
                .sendAsync(request, responseBodyHandler, pushPromiseHandler);
    }
}
