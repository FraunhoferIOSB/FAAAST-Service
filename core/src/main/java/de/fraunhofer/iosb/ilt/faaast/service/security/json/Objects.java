package de.fraunhofer.iosb.ilt.faaast.service.security.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Objects {

    @JsonProperty("ROUTE")
    private String ROUTE;

    @JsonProperty("IDENTIFIABLE")
    private String IDENTIFIABLE;

    @JsonProperty("REFERABLE")
    private String REFERABLE;

    @JsonProperty("DESCRIPTOR")
    private String DESCRIPTOR;

    public String getROUTE() {
        return ROUTE;
    }

    public void setROUTE(String ROUTE) {
        this.ROUTE = ROUTE;
    }

    public String getIDENTIFIABLE() {
        return IDENTIFIABLE;
    }

    public void setIDENTIFIABLE(String IDENTIFIABLE) {
        this.IDENTIFIABLE = IDENTIFIABLE;
    }

    public String getREFERABLE() {
        return REFERABLE;
    }

    public void setREFERABLE(String REFERABLE) {
        this.REFERABLE = REFERABLE;
    }

    public String getDESCRIPTOR() {
        return DESCRIPTOR;
    }

    public void setDESCRIPTOR(String DESCRIPTOR) {
        this.DESCRIPTOR = DESCRIPTOR;
    }
}