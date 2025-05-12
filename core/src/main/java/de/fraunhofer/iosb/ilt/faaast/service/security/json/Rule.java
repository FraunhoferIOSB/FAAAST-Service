package de.fraunhofer.iosb.ilt.faaast.service.security.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class Rule {

    @JsonProperty("ACL")
    private ACL ACL;

    @JsonProperty("OBJECTS")
    private List<Objects> OBJECTS;

    @JsonProperty("FORMULA")
    private Map<String, Object> FORMULA;

    @JsonProperty("FILTER")
    private Filter FILTER;

    // The missing field that often appears in your JSON:
    @JsonProperty("FRAGMENT")
    private String FRAGMENT;

    // Reusable references
    @JsonProperty("USEACL")
    private String USEACL;

    @JsonProperty("USEOBJECTS")
    private List<String> USEOBJECTS;

    @JsonProperty("USEFORMULA")
    private String USEFORMULA;

    public ACL getACL() {
        return ACL;
    }

    public void setACL(ACL ACL) {
        this.ACL = ACL;
    }

    public List<Objects> getOBJECTS() {
        return OBJECTS;
    }

    public void setOBJECTS(List<Objects> OBJECTS) {
        this.OBJECTS = OBJECTS;
    }

    public Map<String, Object> getFORMULA() {
        return FORMULA;
    }

    public void setFORMULA(Map<String, Object> FORMULA) {
        this.FORMULA = FORMULA;
    }

    public Filter getFILTER() {
        return FILTER;
    }

    public void setFILTER(Filter FILTER) {
        this.FILTER = FILTER;
    }

    public String getFRAGMENT() {
        return FRAGMENT;
    }

    public void setFRAGMENT(String FRAGMENT) {
        this.FRAGMENT = FRAGMENT;
    }

    public String getUSEACL() {
        return USEACL;
    }

    public void setUSEACL(String USEACL) {
        this.USEACL = USEACL;
    }

    public List<String> getUSEOBJECTS() {
        return USEOBJECTS;
    }

    public void setUSEOBJECTS(List<String> USEOBJECTS) {
        this.USEOBJECTS = USEOBJECTS;
    }

    public String getUSEFORMULA() {
        return USEFORMULA;
    }

    public void setUSEFORMULA(String USEFORMULA) {
        this.USEFORMULA = USEFORMULA;
    }
}