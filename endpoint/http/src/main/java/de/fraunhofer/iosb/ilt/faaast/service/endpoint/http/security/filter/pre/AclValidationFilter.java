package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.pre;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.auth.SharedAttributes.ACL;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.AccessControlListHelper.getAcl;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.AccessControlListHelper.getAttributes;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.AccessControlListHelper.getObjects;


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
