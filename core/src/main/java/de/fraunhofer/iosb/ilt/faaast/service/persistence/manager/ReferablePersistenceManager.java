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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.manager;

import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.AssetAdministrationShellElementWalker;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.DefaultAssetAdministrationShellElementVisitor;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.Blob;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


/**
 * Class to handle {@link Referable}
 */
public class ReferablePersistenceManager extends PersistenceManager {

    /**
     * Get a submodel element by reference
     *
     * @param reference of the submodel element
     * @param modifier of the return value
     * @return the searched submodel or null
     * @throws ResourceNotFoundException if resource is not found
     */
    public SubmodelElement getSubmodelElement(Reference reference, QueryModifier modifier) throws ResourceNotFoundException {
        ensureInitialized();
        Ensure.requireNonNull(modifier, "modifier must be non-null");
        if (reference == null || reference.getKeys() == null) {
            return null;
        }
        try {
            SubmodelElement result = DeepCopyHelper.deepCopy(AasUtils.resolve(reference, aasEnvironment, SubmodelElement.class), SubmodelElement.class);
            if (result != null && modifier.getExtent() == Extent.WITHOUT_BLOB_VALUE && Blob.class.isAssignableFrom(result.getClass())) {
                ((Blob) result).setValue(null);
            }
            return result;
        }
        catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException(reference, e);
        }
    }


    /**
     * Get the submodel elements associated to the reference.Supported are two
     * possible parents of submodel elements:
     * <ul>
     * <li>{@link Submodel}
     * <li>{@link SubmodelElementCollection}
     * </ul>
     *
     * @param reference to the submodel or submodel element collection
     * @param semanticId of the submodel elements
     * @return List of submodel elements matching the parameters
     * @throws
     * ResourceNotFoundException
     *             if resource is not found
     */
    public List<SubmodelElement> getSubmodelElements(Reference reference, Reference semanticId) throws ResourceNotFoundException {
        ensureInitialized();
        if (reference == null || reference.getKeys() == null || reference.getKeys().isEmpty()) {
            return List.of();
        }
        Referable referable = AasUtils.resolve(reference, aasEnvironment);
        if (referable == null) {
            throw new ResourceNotFoundException(String.format(ERROR_MSG_RESOURCE_NOT_FOUND_BY_REF, AasUtils.asString(reference)));
        }
        Collection<SubmodelElement> result = List.of();
        if (Submodel.class.isAssignableFrom(referable.getClass())) {
            result = ((Submodel) referable).getSubmodelElements();
        }
        else if (SubmodelElementCollection.class.isAssignableFrom(referable.getClass())) {
            result = ((SubmodelElementCollection) referable).getValues();
        }
        if (semanticId != null) {
            result.removeIf(x -> !Objects.equals(x.getSemanticId(), semanticId));
        }
        return DeepCopyHelper.deepCopy(result, SubmodelElement.class);
    }


    /**
     * Create or update a submodel element. Parent reference and reference of
     * the submodel element must not both be null. Otherwise the location of the
     * submodel element cannot be determined. Supported parent references could
     * be references to a
     * <ul>
     * <li>{@link Submodel} or to a
     * <li>{@link SubmodelElementCollection}
     * </ul>
     *
     * @param parent reference to the parent
     * @param reference reference to the submodel element
     * @param submodelElement which should be updated or created
     * @return the updated or created submodel element
     * @throws
     * ResourceNotFoundException
     *             if resource is not found
     */
    public SubmodelElement putSubmodelElement(Reference parent, Reference reference, SubmodelElement submodelElement) throws ResourceNotFoundException {
        ensureInitialized();
        Ensure.requireNonNull(submodelElement, "submodelElement must be non-null");
        Ensure.require(!ReferenceHelper.isNullOrEmpty(parent) || !ReferenceHelper.isNullOrEmpty(reference), "either parent or referenceToSubmodelElement must be non-empty");
        Reference parentRef = ReferenceHelper.isNullOrEmpty(parent)
                ? ReferenceHelper.getParent(reference)
                : parent;
        Ensure.requireNonNull(parentRef, "could not determine parent reference");
        Referable referable = AasUtils.resolve(parentRef, aasEnvironment);
        if (referable == null) {
            throw new ResourceNotFoundException(String.format(ERROR_MSG_RESOURCE_NOT_FOUND_BY_REF, AasUtils.asString(parentRef)));
        }
        Collection<SubmodelElement> submodelElements;
        if (Submodel.class.isAssignableFrom(referable.getClass())) {
            submodelElements = ((Submodel) referable).getSubmodelElements();
        }
        else if (SubmodelElementCollection.class.isAssignableFrom(referable.getClass())) {
            submodelElements = ((SubmodelElementCollection) referable).getValues();
        }
        else {
            throw new IllegalArgumentException(String.format("illegal parent type %s, must be one of %s, %s",
                    referable.getClass(),
                    Submodel.class,
                    SubmodelElementCollection.class));
        }
        submodelElements.removeIf(x -> x.getIdShort().equalsIgnoreCase(submodelElement.getIdShort()));
        submodelElements.add(submodelElement);
        return submodelElement;
    }


    /**
     * Remove a {@link Referable}
     *
     * @param reference of the referable which should be removed
     * @throws
     * ResourceNotFoundException
     *             if resource is not found
     */
    public void remove(Reference reference) throws ResourceNotFoundException {
        Ensure.require(!ReferenceHelper.isNullOrEmpty(reference), "reference must be non-empty");
        final Referable referable = AasUtils.resolve(reference, aasEnvironment);
        if (referable == null) {
            throw new ResourceNotFoundException(String.format(ERROR_MSG_RESOURCE_NOT_FOUND_BY_REF, AasUtils.asString(reference)));
        }
        Reference parentRef = ReferenceHelper.getParent(reference);
        if (parentRef == null) {
            AssetAdministrationShellElementWalker.builder()
                    .visitor(new DefaultAssetAdministrationShellElementVisitor() {
                        @Override
                        public void visit(AssetAdministrationShell assetAdministrationShell) {
                            aasEnvironment.getAssetAdministrationShells().remove(assetAdministrationShell);
                        }


                        @Override
                        public void visit(Submodel submodel) {
                            aasEnvironment.getSubmodels().remove(submodel);
                        }
                    })
                    .build()
                    .walk(referable);
            return;
        }
        Referable parent = AasUtils.resolve(parentRef, aasEnvironment);
        Ensure.requireNonNull(parent, String.format("unable to resolve parent reference: %s", AasUtils.asString(parentRef)));
        AssetAdministrationShellElementWalker.builder()
                .visitor(new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(SubmodelElementCollection submodelElementCollection) {
                        // type-safety guaranteed
                        submodelElementCollection.getValues().remove(referable);
                    }


                    @Override
                    public void visit(Submodel submodel) {
                        // type-safety guaranteed
                        submodel.getSubmodelElements().remove(referable);
                    }
                })
                .build()
                .walk(parent);
    }
}
