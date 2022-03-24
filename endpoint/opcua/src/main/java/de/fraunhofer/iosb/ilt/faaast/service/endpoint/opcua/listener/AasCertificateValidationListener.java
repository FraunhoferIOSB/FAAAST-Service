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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.listener;

import com.prosysopc.ua.stack.cert.CertificateCheck;
import com.prosysopc.ua.stack.cert.DefaultCertificateValidatorListener;
import com.prosysopc.ua.stack.cert.ValidationResult;
import com.prosysopc.ua.stack.core.ApplicationDescription;
import com.prosysopc.ua.stack.transport.security.Cert;
import com.prosysopc.ua.stack.utils.CertificateUtils;
import java.security.cert.CertificateParsingException;
import java.util.EnumSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class for reacting to certificate validation handling results.
 *
 * @author Tino Bischoff
 */
public class AasCertificateValidationListener implements DefaultCertificateValidatorListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AasCertificateValidationListener.class);

    /**
     * Handle certificate validation. The method is called once the actual
     * validator has already checked the certificate and provides the results of
     * the checks in the parameters. If isTrusted, isSignVerified, isValid are
     * all false, you should normally accept the certificate.
     *
     * @param cert the certificate that is being validated
     * @param ad The Application Description
     * @param passedChecks the certification checks that failed
     * @return validation result: accept or reject; once or permanently?
     */
    @Override
    public ValidationResult onValidate(Cert cert, ApplicationDescription ad, EnumSet<CertificateCheck> passedChecks) {

        try {
            LOGGER.info("onValidate: {}, {}", ad, CertificateUtils.getApplicationUriOfCertificate(cert));
        }
        catch (CertificateParsingException ex) {
            LOGGER.error("onValidate Exception", ex);
        }

        /*
         * Checks that all compulsory certificate checks passed. NOTE! The CertificateCheck.Uri is
         * included in this list. This might prevent some client applications from connecting, if e.g.
         * their hostname would change domain. This check is needed for passing the compliance tests
         * provided by the OPC Foundation. But if you need to allow such client to connect, comment this
         * and instead use the commented implementation below.
         */
        if (passedChecks.containsAll(CertificateCheck.COMPULSORY)) {
            return ValidationResult.AcceptPermanently;
        }
        else {
            return ValidationResult.Reject;
        }
    }

}
