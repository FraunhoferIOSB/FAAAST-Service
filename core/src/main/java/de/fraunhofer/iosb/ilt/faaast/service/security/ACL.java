package de.fraunhofer.iosb.ilt.faaast.service.security;

import de.fraunhofer.iosb.ilt.faaast.service.security.attributes.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.security.attributes.ClaimAttribute;

import java.util.List;

public class ACL {
    private List<Attribute> attributes;
    private List<Right> rights;
    private AccessType accessType;

    public void setAttributes(List<ClaimAttribute> list) {
    }

    public void setRights(List<Right> rights) {
    }

    public void setAccessType(AccessType accessType) {
    }

    // Constructors, getters, and setters
}

