package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.pre;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.auth.SharedAttributes.ACL;


public abstract class AbstractAclFilter extends JwtAuthorizationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        AllAccessPermissionRules acl = (AllAccessPermissionRules) request.getAttribute(ACL.getName());
        request.setAttribute(ACL.getName(), doFilter((HttpServletRequest) request, acl));
        chain.doFilter(request, response);
    }


    protected abstract AllAccessPermissionRules doFilter(HttpServletRequest request, AllAccessPermissionRules acl);
}
