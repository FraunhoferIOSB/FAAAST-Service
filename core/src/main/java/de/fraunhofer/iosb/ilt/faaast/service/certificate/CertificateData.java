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
package de.fraunhofer.iosb.ilt.faaast.service.certificate;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.AbstractBuilder;


/**
 * Represents a pair of public/private key pair, corresponding certificate and certificate chain.
 */
public class CertificateData {

    private KeyPair keyPair;
    private X509Certificate certificate;
    private X509Certificate[] certificateChain;

    public KeyPair getKeyPair() {
        return keyPair;
    }


    public void setKeyPair(KeyPair keyPair) {
        this.keyPair = keyPair;
    }


    public X509Certificate getCertificate() {
        return certificate;
    }


    public void setCertificate(X509Certificate certificate) {
        this.certificate = certificate;
    }


    public X509Certificate[] getCertificateChain() {
        return certificateChain;
    }


    public void setCertificateChain(X509Certificate[] certificateChain) {
        this.certificateChain = certificateChain;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CertificateData that = (CertificateData) o;
        return Objects.equals(keyPair, that.keyPair)
                && Objects.equals(certificate, that.certificate)
                && Objects.equals(certificateChain, that.certificateChain);
    }


    @Override
    public int hashCode() {
        return Objects.hash(keyPair, certificate, certificateChain);
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends AbstractBuilder<CertificateData> {

        public Builder certificate(X509Certificate value) {
            getBuildingInstance().setCertificate(value);
            if (Objects.isNull(getBuildingInstance().getCertificateChain()) || getBuildingInstance().getCertificateChain().length == 0) {
                getBuildingInstance().setCertificateChain(new X509Certificate[] {
                        value
                });
            }
            return this;
        }


        public Builder certificateChain(X509Certificate... value) {
            getBuildingInstance().setCertificateChain(value);
            return this;
        }


        public Builder keyPair(KeyPair value) {
            getBuildingInstance().setKeyPair(value);
            return this;
        }


        public Builder keyPair(PublicKey publicKey, PrivateKey privateKey) {
            getBuildingInstance().setKeyPair(new KeyPair(publicKey, privateKey));
            return this;
        }


        @Override
        protected CertificateData newBuildingInstance() {
            return new CertificateData();
        }
    }

}
