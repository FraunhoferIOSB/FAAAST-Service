package de.fraunhofer.iosb.ilt.faaast.service.security.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Attribute {

    @JsonProperty("CLAIM")
    private String CLAIM;

    @JsonProperty("GLOBAL")
    private String GLOBAL;

    public String getCLAIM() {
        return CLAIM;
    }

    public void setCLAIM(String CLAIM) {
        this.CLAIM = CLAIM;
    }

    public String getGLOBAL() {
        return GLOBAL;
    }

    public void setGLOBAL(String GLOBAL) {
        this.GLOBAL = GLOBAL;
    }
}