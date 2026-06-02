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
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.AccessControlListHelper.getAcl;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.AccessControlListHelper.getAttributes;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.AccessControlListHelper.getObjects;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;


/**
 * Might also be implemented as ACL validator when repository receives a new ACL.
 */
public class AclValidationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        AllAccessPermissionRules acl = (AllAccessPermissionRules) request.getAttribute(ACL.getName());

        acl.getRules().removeIf(
                rule -> getAcl(rule, acl) == null ||
                        getAttributes(getAcl(rule, acl), acl) == null ||
                        getAcl(rule, acl).getRights() == null ||
                        getObjects(rule, acl) == null);

        request.setAttribute(ACL.getName(), acl);
        chain.doFilter(request, response);
    }
}
