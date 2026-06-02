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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.pre;

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.auth.SharedAttributes.ACL;

import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.acl.repository.AclRepository;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;


/**
 * Helper filter to inject the current ACL rules into a request.
 */
public class AclRulesInceptionFilter implements Filter {

    private final AclRepository aclRepository;

    /**
     * Class constructor.
     *
     * @param aclRepository Retrieval of ACL
     */
    public AclRulesInceptionFilter(AclRepository aclRepository) {
        this.aclRepository = aclRepository;
    }


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setAttribute(ACL.getName(), aclRepository.getAllAccessPermissionRules());
        chain.doFilter(request, response);
    }
}
