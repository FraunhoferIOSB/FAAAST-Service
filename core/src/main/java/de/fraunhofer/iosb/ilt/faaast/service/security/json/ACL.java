package de.fraunhofer.iosb.ilt.faaast.service.security.json;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Describes the access control list.
 */
public class ACL {

    @JsonProperty("ATTRIBUTES")
    private List<Attribute> ATTRIBUTES; // e.g., "CLAIM", "GLOBAL"

    @JsonProperty("RIGHTS")
    private List<String> RIGHTS;

    @JsonProperty("ACCESS")
    private String ACCESS;

    public List<Attribute> getATTRIBUTES() {
        return ATTRIBUTES;
    }

    public void setATTRIBUTES(List<Attribute> ATTRIBUTES) {
        this.ATTRIBUTES = ATTRIBUTES;
    }

    public List<String> getRIGHTS() {
        return RIGHTS;
    }

    public void setRIGHTS(List<String> RIGHTS) {
        this.RIGHTS = RIGHTS;
    }

    public String getACCESS() {
        return ACCESS;
    }

    public void setACCESS(String ACCESS) {
        this.ACCESS = ACCESS;
    }
}