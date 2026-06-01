package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.auth;

public enum SharedAttributes {
    AUTH_STATE("auth.state"),
    CLAIMS("claims"),
    ACL("acl");

    private final String name;


    public String getName() {
        return name;
    }

    SharedAttributes(String name) {
        this.name = name;
    }
}
