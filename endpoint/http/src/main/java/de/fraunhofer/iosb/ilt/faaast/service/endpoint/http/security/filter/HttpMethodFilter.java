package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AllAccessPermissionRules;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.RightsEnum;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod.POST;
import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.auth.SharedAttributes.ACL;


public class HttpMethodFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        String method = ((HttpServletRequest) request).getMethod();

        AllAccessPermissionRules filteredAcl = (AllAccessPermissionRules) request.getAttribute(ACL.getName());

        String requiredRight = isOperationRequest(method, ((HttpServletRequest) request).getContextPath()) ? "EXECUTE" : getRequiredRight(method);

        filteredAcl.getRules().removeIf(
                rules -> rules.getAcl().getRights().contains(RightsEnum.ALL) || rules.getAcl().getRights().contains(RightsEnum.valueOf(requiredRight))
        );

        request.setAttribute(ACL.getName(), filteredAcl);
        chain.doFilter(request, response);
    }


    private static boolean isOperationRequest(String method, String path) {
        // Requirements: POST and URL suffix: invoke, invoke-async, invoke/$value, invoke-async/$value
        String cleanPath;
        String[] pathParts = path.split("/");

        if (pathParts.length > 1 && "$value".equals(pathParts[pathParts.length - 1])) {
            cleanPath = pathParts[pathParts.length - 2];
        }
        else {
            cleanPath = pathParts[pathParts.length - 1];
        }

        return POST.name().equals(method) && ("invoke".equals(cleanPath) || "invoke-async".equals(cleanPath));
    }


    private static String getRequiredRight(String method) {
        return switch (method) {
            case "GET" -> "READ";
            case "POST" -> "CREATE";
            case "PUT" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
    }

}
