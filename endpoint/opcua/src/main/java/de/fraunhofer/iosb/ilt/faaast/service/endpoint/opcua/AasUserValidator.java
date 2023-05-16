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

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.server.ServerUserIdentity;
import com.prosysopc.ua.server.Session;
import com.prosysopc.ua.server.UserValidator;
import com.prosysopc.ua.stack.core.UserIdentityToken;
import com.prosysopc.ua.stack.core.UserTokenType;
import com.prosysopc.ua.stack.transport.security.CertificateValidator;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class used for custom user validation in UaServer.
 */
public class AasUserValidator implements UserValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AasUserValidator.class);

    private final CertificateValidator validator;
    private final Map<String, String> userMap;
    private final Set<UserTokenType> supportedAuthentications;

    /**
     * Creates a new instance of AasUserValidator.
     *
     * @param validator used to validate certificates
     * @param userMap The desired user names (Key) and passwords (Value)
     * @param supportedAuthentications The list of supported authentication types.
     */
    public AasUserValidator(CertificateValidator validator, Map<String, String> userMap, Set<UserTokenType> supportedAuthentications) {
        Ensure.requireNonNull(validator, "validator must be non-null");
        Ensure.requireNonNull(supportedAuthentications, "supportedAuthentications must be non-null");
        this.validator = validator;
        this.userMap = userMap != null
                ? userMap
                : new HashMap<>();
        this.supportedAuthentications = supportedAuthentications;
    }


    @Override
    public boolean onValidate(Session session, ServerUserIdentity userIdentity) throws StatusException {
        // Return true, if the user is allowed access to the server
        // Note that the UserIdentity can be of different actual types,
        // depending on the selected authentication mode (by the client).
        LOGGER.trace("onValidate: userIdentity={}", userIdentity);
        if (userIdentity.getType().equals(UserTokenType.UserName)) {
            return userMap.containsKey(userIdentity.getName())
                    && Objects.equals(userMap.get(userIdentity.getName()), userIdentity.getPassword());
        }

        if (userIdentity.getType().equals(UserTokenType.Certificate)) {
            // Get StatusCode for the certificate
            // SessionManager will throw Bad_IdentityTokenRejected when this method returns false
            return supportedAuthentications.contains(UserTokenType.Certificate)
                    && validator.validateCertificate(userIdentity.getCertificate()).isGood();
        }

        // check anonymous access
        return supportedAuthentications.contains(UserTokenType.Anonymous);
    }


    @Override
    public void onValidationError(Session sn, UserIdentityToken userToken, Exception exception) {
        LOGGER.error("onValidationError: User validation failed: userToken={} error={}", userToken, exception);
    }

}
