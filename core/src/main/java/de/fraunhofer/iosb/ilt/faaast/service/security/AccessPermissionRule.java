package de.fraunhofer.iosb.ilt.faaast.service.security;

import de.fraunhofer.iosb.ilt.faaast.service.security.objects.AccessObject;
import de.fraunhofer.iosb.ilt.faaast.service.security.objects.IdentifiableObject;

import java.util.List;

public class AccessPermissionRule {
    private ACL acl;
    private List<AccessObject> objects;
    private Condition formula;
    private Filter filter; // Optional

    public void setAcl(ACL acl) {
    }

    public void setObjects(List<IdentifiableObject> list) {
    }

    public void setFormula(Condition condition) {
    }

    // Constructors, getters, and setters
}
