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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.manager;

import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extend;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Class to handle {@link io.adminshell.aas.v3.model.Referable}
 */
public class ReferablePersistenceManager extends PersistenceManager {

    private static final String ERROR_MSG_RESOURCE_NOT_FOUND_BY_REF = "Resource not found by reference %s";

    /**
     * Get a submodel element by its reference
     *
     * @param reference of the submodel element
     * @param modifier of the return value
     * @return the searched submodel or null
     * @throws ResourceNotFoundException
     */
    public SubmodelElement getSubmodelElement(Reference reference, QueryModifier modifier) throws ResourceNotFoundException {
        if (reference == null || reference.getKeys() == null || modifier == null || this.aasEnvironment == null) {
            return null;
        }
        if (modifier.getExtend() == Extend.WITHOUT_BLOB_VALUE) {
            if (reference.getKeys().get(reference.getKeys().size() - 1).getType() == KeyElements.BLOB) {
                return null;
            }
        }

        try {
            SubmodelElement submodelElement = AasUtils.resolve(reference, this.aasEnvironment, SubmodelElement.class);
            return DeepCopyHelper.deepCopy(submodelElement, submodelElement.getClass());
        }
        catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException(reference, e);
        }
    }


    /**
     * Get the submodel elements associated to the reference.
     * Supported are two possible parents of submodel elements:
     * <p>
     * <ul>
     * <li>{@link io.adminshell.aas.v3.model.Submodel}
     * <li>{@link io.adminshell.aas.v3.model.SubmodelElementCollection}
     * </ul>
     * <p>
     * If the semanticId is not null the submodel element list filtered by the semantic id
     *
     * @param reference to the submodel or submodel element collection
     * @param semanticId of the submodel elements
     * @return a list of the submodel elements associated to the parent reference
     */
    public List<SubmodelElement> getSubmodelElements(Reference reference, Reference semanticId) throws ResourceNotFoundException {
        if (reference == null) {
            return null;
        }

        if (reference.getKeys() != null
                && !reference.getKeys().isEmpty()) {
            List<SubmodelElement> submodelElements = null;
            KeyElements lastKeyElementOfReference = reference.getKeys().get(reference.getKeys().size() - 1).getType();

            if (lastKeyElementOfReference == KeyElements.SUBMODEL) {
                Submodel submodel = AasUtils.resolve(reference, this.aasEnvironment, Submodel.class);
                if (submodel == null) {
                    throw new ResourceNotFoundException(String.format(ERROR_MSG_RESOURCE_NOT_FOUND_BY_REF, AasUtils.asString(reference)));
                }
                Submodel deepCopiedSubmodel = DeepCopyHelper.deepCopy(submodel, submodel.getClass());
                submodelElements = deepCopiedSubmodel.getSubmodelElements();

            }
            else if (lastKeyElementOfReference == KeyElements.SUBMODEL_ELEMENT_COLLECTION) {
                SubmodelElementCollection submodelElementCollection = AasUtils.resolve(reference, this.aasEnvironment, SubmodelElementCollection.class);
                if (submodelElementCollection == null) {
                    throw new ResourceNotFoundException(String.format(ERROR_MSG_RESOURCE_NOT_FOUND_BY_REF, AasUtils.asString(reference)));
                }
                SubmodelElementCollection deepCopiedSubmodelElementCollection = DeepCopyHelper.deepCopy(submodelElementCollection, submodelElementCollection.getClass());
                submodelElements = new ArrayList<>(deepCopiedSubmodelElementCollection.getValues());
            }

            if (semanticId != null) {
                assert submodelElements != null;
                submodelElements = submodelElements.stream().filter(x -> x.getSemanticId() != null
                        && x.getSemanticId().equals(semanticId)).collect(Collectors.toList());
            }

            return submodelElements;
        }
        return null;
    }


    /**
     * Create or update a submodel element.
     * Parent reference and reference of the submodel element must not both be null.
     * Otherwise the location of the submodel element cannot be determined.
     * Supported parent references could be references to a
     * <ul>
     * <li>{@link io.adminshell.aas.v3.model.Submodel} or to a
     * <li>{@link io.adminshell.aas.v3.model.SubmodelElementCollection}
     * </ul>
     * To add a new submodel element give the parent reference and the submodel element.
     * To update an existing submodel element give the reference to the submodel element and the submodel element.
     *
     * @param parent reference to the parent
     * @param referenceToSubmodelElement reference to the submodel element
     * @param submodelElement which should be updated or created
     * @return the updated or created submodel element
     */
    public SubmodelElement putSubmodelElement(Reference parent, Reference referenceToSubmodelElement, SubmodelElement submodelElement) throws ResourceNotFoundException {
        if ((parent == null && referenceToSubmodelElement == null) || submodelElement == null) {
            return null;
        }
        KeyElements lastKeyElementOfParent;

        if (parent != null
                && parent.getKeys() != null
                && !parent.getKeys().isEmpty()) {
            lastKeyElementOfParent = parent.getKeys().get(parent.getKeys().size() - 1).getType();
        }
        else if (referenceToSubmodelElement != null
                && referenceToSubmodelElement.getKeys() != null
                && referenceToSubmodelElement.getKeys().size() > 1) {
            lastKeyElementOfParent = referenceToSubmodelElement.getKeys().get(referenceToSubmodelElement.getKeys().size() - 2).getType();
            parent = new DefaultReference.Builder()
                    .keys(referenceToSubmodelElement.getKeys().subList(0, referenceToSubmodelElement.getKeys().size() - 1))
                    .build();
        }
        else {
            return null;
        }

        Predicate<SubmodelElement> filter = x -> x.getIdShort().equalsIgnoreCase(submodelElement.getIdShort());

        if (lastKeyElementOfParent == KeyElements.SUBMODEL) {
            Submodel submodel = AasUtils.resolve(parent, this.aasEnvironment, Submodel.class);
            if (submodel == null) {
                throw new ResourceNotFoundException(String.format(ERROR_MSG_RESOURCE_NOT_FOUND_BY_REF, AasUtils.asString(parent)));
            }
            submodel.getSubmodelElements().removeIf(filter);
            submodel.getSubmodelElements().add(submodelElement);

        }
        else if (lastKeyElementOfParent == KeyElements.SUBMODEL_ELEMENT_COLLECTION) {
            SubmodelElementCollection submodelElementCollection = AasUtils.resolve(parent, this.aasEnvironment, SubmodelElementCollection.class);
            if (submodelElementCollection == null) {
                throw new ResourceNotFoundException(String.format(ERROR_MSG_RESOURCE_NOT_FOUND_BY_REF, AasUtils.asString(parent)));
            }
            submodelElementCollection.getValues().removeIf(filter);
            submodelElementCollection.getValues().add(submodelElement);
        }
        else {
            return null;
        }
        return submodelElement;
    }


    /**
     * Remove a {@link io.adminshell.aas.v3.model.Referable}
     *
     * @param reference of the referable which should be removed
     */
    public void remove(Reference reference) throws ResourceNotFoundException {
        if (reference == null) {
            return;
        }
        if (reference.getKeys() != null
                && !reference.getKeys().isEmpty()) {
            KeyElements lastKeyElementOfReference = reference.getKeys().get(reference.getKeys().size() - 1).getType();
            Class clazz = AasUtils.keyTypeToClass(lastKeyElementOfReference);

            if (Identifiable.class.isAssignableFrom(clazz)) {
                //TODO: Build Identifier and forward remove reuquest to remove(Identifier id)
                return;
            }
            if (SubmodelElement.class.isAssignableFrom(clazz)) {
                clazz = SubmodelElement.class;
            }

            Referable referable = AasUtils.resolve(reference, this.aasEnvironment);

            if (referable == null) {
                throw new ResourceNotFoundException(String.format(ERROR_MSG_RESOURCE_NOT_FOUND_BY_REF, AasUtils.asString(reference)));
            }

            if (reference.getKeys().size() > 1) {
                Reference parent = new DefaultReference.Builder()
                        .keys(reference.getKeys().subList(0, reference.getKeys().size() - 1))
                        .build();
                Referable parentReferable = AasUtils.resolve(parent, this.aasEnvironment);

                Method method = EnvironmentHelper.getGetReferableListMethod(clazz, parentReferable);
                if (method != null) {
                    try {
                        List<Referable> referableList = (List<Referable>) method.invoke(parentReferable);
                        referableList.remove(referable);
                    }
                    catch (InvocationTargetException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
