/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.service.model;

import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Identifies a SubmodelElement within a submodel.
 */
public class SubmodelElementIdentifier {

    private String submodelId;
    private IdShortPath idShortPath;

    public SubmodelElementIdentifier() {
        idShortPath = IdShortPath.builder().build();
    }


    public String getSubmodelId() {
        return submodelId;
    }


    public void setSubmodelId(String submodelId) {
        this.submodelId = submodelId;
    }


    public IdShortPath getIdShortPath() {
        return idShortPath;
    }


    public void setIdShortPath(IdShortPath idShortPath) {
        this.idShortPath = idShortPath;
    }


    /**
     * Creates a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} pointing to the submodel element.
     *
     * @return the reference
     * @throws IllegalArgumentException if submodelId is null
     * @throws IllegalArgumentException if idShortPath is null
     */
    public Reference toReference() {
        Ensure.requireNonNull(submodelId, "submodelId must be non-null");
        Ensure.requireNonNull(idShortPath, "idShortPath must be non-null");
        return ReferenceHelper.combine(
                ReferenceBuilder.forSubmodel(submodelId),
                idShortPath.toReference());
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SubmodelElementIdentifier other = (SubmodelElementIdentifier) o;
        return Objects.equals(submodelId, other.submodelId)
                && Objects.equals(idShortPath, other.idShortPath);
    }


    @Override
    public int hashCode() {
        return Objects.hash(submodelId, idShortPath);
    }


    /**
     * Creates an SubmodelElement identifier equaivalent to the reference. The reference can either be of the form AAS
     * -> Submodel -> SubmodelElements* or Submodel -> SubmodelElements*. The key types must be of the expected type or
     * any suitable/related type, e.g. the key type for a SubmodelElement may be
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.KeyType#SUBMODEL_ELEMENT} or
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.KeyType#PROPERTY} or of any other subclass of SubmodelElement.
     *
     * <p>If the reference starts with an AAS key, the AAS key is discarded.
     *
     * @param reference the reference
     * @return the SubmodelElement identifier
     * @throws IllegalArgumentException if reference is null or does not contain the required elements
     * @throws IllegalArgumentException if the key types do not match the stated requirements
     */
    public static SubmodelElementIdentifier fromReference(Reference reference) {
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.require(Objects.nonNull(reference.getKeys()) && !reference.getKeys().isEmpty(), "reference must contain at least one keys");
        Ensure.require(Objects.equals(reference.getType(), ReferenceTypes.MODEL_REFERENCE), "reference must be a model reference");
        int startIndex = 0;
        if (ReferenceHelper.isKeyType(reference.getKeys().get(0), AssetAdministrationShell.class)) {
            startIndex = 1;
        }
        ReferenceHelper.ensureKeyType(reference.getKeys().get(startIndex), Submodel.class);
        return SubmodelElementIdentifier.builder()
                .submodelId(reference.getKeys().get(startIndex).getValue())
                .idShortPath(IdShortPath.fromReference(reference))
                .build();
    }


    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends SubmodelElementIdentifier, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B submodelId(String value) {
            getBuildingInstance().setSubmodelId(value);
            return getSelf();
        }


        public B idShortPath(IdShortPath value) {
            getBuildingInstance().setIdShortPath(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<SubmodelElementIdentifier, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected SubmodelElementIdentifier newBuildingInstance() {
            return new SubmodelElementIdentifier();
        }
    }
}
