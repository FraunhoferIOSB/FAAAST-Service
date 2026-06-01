package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter;

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
        // Intentionally empty
        chain.doFilter(request, response);
    }
}
