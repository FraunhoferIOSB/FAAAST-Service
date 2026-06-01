package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter;

import com.auth0.jwt.interfaces.Claim;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.util.Map;

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.auth.SharedAttributes.ACL;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.auth.SharedAttributes.CLAIMS;


/**
 * Filter all ACL for the Claims they require
 */
public class AttributeClaimFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        Map<String, Claim> claims = (Map<String, Claim>) request.getAttribute(CLAIMS.getName());
        AllAccessPermissionRules filteredAcl = (AllAccessPermissionRules) request.getAttribute(ACL.getName());

        filteredAcl.getRules().removeIf(rule -> rule.getAcl().getAttributes().stream()
                .anyMatch(attributeItem -> {
                    // claim, global and reference should be subtypes of AttributeItem...
                    if (attributeItem.getGlobal().equals(AttributeItem.Global.ANONYMOUS)) {
                        return false;
                    }
                    else if (attributeItem.getClaim() != null) {
                        return !claims.containsKey(attributeItem.getClaim());
                    }
                    return false;
                })
        );

        request.setAttribute(ACL.getName(), filteredAcl);

        chain.doFilter(request, response);
    }
}
