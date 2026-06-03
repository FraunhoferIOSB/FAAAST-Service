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

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;


/**
 * Contains common logic for extracting and storing the AAS ACL in a request.
 */
public abstract class AbstractAclFilter extends JwtAuthorizationFilter implements Filter {

    @SuppressWarnings("unchecked")
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        List<AccessPermissionRule> acl = ((List<AccessPermissionRule>) request.getAttribute(ACL.getName()));
        request.setAttribute(ACL.getName(), doFilter((HttpServletRequest) request, acl));
        chain.doFilter(request, response);
    }


    /**
     * Perform filtering of the ACL dependent on the HTTP request at hand.
     *
     * @param request The request to filter with
     * @param acl The ACL to filter
     * @return Filtered ACL list
     */
    protected abstract List<AccessPermissionRule> doFilter(HttpServletRequest request, List<AccessPermissionRule> acl);
}
