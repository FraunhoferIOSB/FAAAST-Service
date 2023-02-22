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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.util;

import java.nio.file.Path;


/**
 * Utitlity class for accessing different sub-directories of the base security path of an OPC UA server or client.
 */
public class SecurityPathHelper {

    public static final String CERTS = "certs";
    public static final String CRL = "crl";
    public static final String PKI = "pki";
    public static final String ISSUERS = "issuers";
    public static final String REJECTED = "rejected";
    public static final String TRUSTED = "trusted";

    /**
     * Gets the "PKI" path relative to the base security path.
     *
     * @param base the base security bath to use
     * @return the "PKI" path relative to the base security path
     */
    public static Path pki(Path base) {
        return base.resolve(PKI);
    }


    /**
     * Gets the "issuers" path relative to the base security path.
     *
     * @param base the base security bath to use
     * @return the "issuers" path relative to the base security path
     */
    public static Path issuers(Path base) {
        return pki(base).resolve(ISSUERS);
    }


    /**
     * Gets the path storing certificates of allowed issuers relative to the base security path.
     *
     * @param base the base security bath to use
     * @return the path storing certificates of allowed issuers relative to the base security path
     */
    public static Path issuers_allowed(Path base) {
        return issuers(base).resolve(CERTS);
    }


    /**
     * Gets the path storing revoked issuers certificates relative to the base security path.
     *
     * @param base the base security bath to use
     * @return the path storing revoked issuers certificates relative to the base security path
     */
    public static Path issuers_revoked(Path base) {
        return issuers(base).resolve(CRL);
    }


    /**
     * Gets the path storing rejected certificates relative to the base security path.
     *
     * @param base the base security bath to use
     * @return the path storing rejected certificates relative to the base security path
     */
    public static Path rejected(Path base) {
        return pki(base).resolve(REJECTED);
    }


    /**
     * Gets the path related to trusted certificates relative to the base security path.
     *
     * @param base the base security bath to use
     * @return the path related to trusted certificates relative to the base security path
     */
    public static Path trusted(Path base) {
        return pki(base).resolve(TRUSTED);
    }


    /**
     * Gets the path storing allowed trusted certificates relative to the base security path.
     *
     * @param base the base security bath to use
     * @return the path storing allowed trusted certificates relative to the base security path
     */
    public static Path trusted_allowed(Path base) {
        return trusted(base).resolve(CERTS);
    }


    /**
     * Gets the path storing revoked trusted certificates relative to the base security path.
     *
     * @param base the base security bath to use
     * @return the path storing revoked trusted certificates relative to the base security path
     */
    public static Path trusted_revoked(Path base) {
        return trusted(base).resolve(CRL);
    }
}
