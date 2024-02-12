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
package de.fraunhofer.iosb.ilt.faaast.service.eventlistener.mqtt;

import de.fraunhofer.iosb.ilt.faaast.service.certificate.CertificateInformation;
import de.fraunhofer.iosb.ilt.faaast.service.certificate.util.KeyStoreHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.SslHelper;
import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.security.GeneralSecurityException;


public class HttpProvider {

    protected HttpClient httpClient;
    protected final String HTTP_ENDPOINT_KEYSTORE_TYPE = "PKCS12";
    protected final String HTTP_ENDPOINT_KEYSTORE_PASSWORD = "random-pw";
    protected File httpEndpointKeyStoreFile;
    protected final CertificateInformation HTTP_ENDPOINT_KEYSTORE_CERTIFICATE_INFORMATION = CertificateInformation.builder()
            .applicationUri("urn:de:fraunhofer:iosb:ilt:faaast:service:eventlistener")
            .commonName("FA³ST Service Event Listener")
            .countryCode("DE")
            .localityName("Karlsruhe")
            .organization("Fraunhofer IOSB")
            .organizationUnit("ILT")
            .build();

    public String getBaseUrl() {
        return baseUrl;
    }

    protected final String baseUrl;

    public HttpProvider(String baseUrl) {
        try {
            this.baseUrl = baseUrl;
            generateHttpEndpointCertificate();
            httpClient = SslHelper.disableHostnameVerification(
                    HttpClient.newBuilder()
                            .followRedirects(HttpClient.Redirect.NEVER)
                            .sslContext(SslHelper.newContextAcceptingCertificates(
                                    httpEndpointKeyStoreFile,
                                    HTTP_ENDPOINT_KEYSTORE_TYPE,
                                    HTTP_ENDPOINT_KEYSTORE_PASSWORD)));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void generateHttpEndpointCertificate() throws IOException, GeneralSecurityException {
        httpEndpointKeyStoreFile = Files.createTempFile("http-endpoint-cert", "").toFile();
        httpEndpointKeyStoreFile.deleteOnExit();
        KeyStoreHelper.save(
                KeyStoreHelper.generateSelfSigned(HTTP_ENDPOINT_KEYSTORE_CERTIFICATE_INFORMATION),
                httpEndpointKeyStoreFile,
                HTTP_ENDPOINT_KEYSTORE_TYPE,
                null,
                HTTP_ENDPOINT_KEYSTORE_PASSWORD,
                HTTP_ENDPOINT_KEYSTORE_PASSWORD);
    }


    public HttpClient getHttpClient() {
        return httpClient;
    }
}
