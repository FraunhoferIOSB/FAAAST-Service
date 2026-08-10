package de.fraunhofer.iosb.ilt.faaast.service.model.query;

import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import java.time.Instant;
import java.util.Map;


public class EvaluationContext {

    Map<String, String> claims = Map.ofEntries(
            Map.entry("iat", Instant.now().toString()),
            Map.entry("test", "itsTheNameOfTheClaim")
    );

    private KeyTypes requestedResourceType;

    private Reference requestedResource;


    public String getClaim(String claimName) {
        return claims.getOrDefault(claimName, "does not exist :-D");
    }


    public boolean isAnonymous() {
        return claims.isEmpty();
    }


    public KeyTypes getRequestedResourceType() {
        return requestedResourceType;
    }


    public Reference getRequestedResource() {
        return requestedResource;
    }
}
