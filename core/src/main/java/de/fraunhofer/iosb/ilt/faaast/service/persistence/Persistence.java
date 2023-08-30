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
package de.fraunhofer.iosb.ilt.faaast.service.persistence;

import de.fraunhofer.iosb.ilt.faaast.service.config.Configurable;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.List;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * Interface used for managing AAS-related data, i.e. everything that is part of a
 * {@code org.eclipse.digitaltwin.aas4j.v3.model.Environment}. Additionally manages to storing of oepration execution
 * states and results.
 *
 * <p>Implement this interface if you wish to create a custom persistence, e.g. backed by a specific database or to
 * connect
 * to legacy systems.
 *
 * @param <C> type of the corresponding configuration class
 */
public interface Persistence<C extends PersistenceConfig> extends Configurable<C> {

    /**
     * Gets an {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell} by id.
     *
     * @param id the id
     * @param modifier the modifier
     * @return the {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell} with the given id
     * @throws ResourceNotFoundException if there is no
     *             {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell} with the given id
     */
    public AssetAdministrationShell getAssetAdministrationShell(String id, QueryModifier modifier) throws ResourceNotFoundException;


    /**
     * Gets a {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel} by id.
     *
     * @param id the id
     * @param modifier the modifier
     * @return the {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel} with the given id
     * @throws ResourceNotFoundException if there is no {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel} with the
     *             given id
     */
    public Submodel getSubmodel(String id, QueryModifier modifier) throws ResourceNotFoundException;


    /**
     * Gets a {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription} by id.
     *
     * @param id the id
     * @param modifier the modifier
     * @return the {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription} with the given id
     * @throws ResourceNotFoundException if there is no
     *             {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription} with the given id
     */
    public ConceptDescription getConceptDescription(String id, QueryModifier modifier) throws ResourceNotFoundException;


    /**
     * Gets a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} by idShort path.
     *
     * @param identifier the identifier of the SubmodelElement
     * @param modifier the modifier
     * @return the {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} identified by the given path
     * @throws ResourceNotFoundException if there is no {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     *             with the given path
     */
    public SubmodelElement getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier) throws ResourceNotFoundException;


    /**
     * Gets all children {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s of a
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel},
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection}, or
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList}.
     *
     * @param identifier the identifier of the SubmodelElement
     * @param modifier the modifier
     * @return a list of all child {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s of the element
     *         identified by path
     * @throws ResourceNotFoundException if there is no element with the given path
     * @throws ResourceNotAContainerElementException if the element identified by the path is not a container element,
     *             i.e. cannot have any child elements
     */
    public default List<SubmodelElement> getSubmodelElements(SubmodelElementIdentifier identifier, QueryModifier modifier)
            throws ResourceNotFoundException, ResourceNotAContainerElementException {
        return findSubmodelElements(
                SubmodelElementSearchCriteria.builder()
                        .parent(identifier)
                        .build(),
                modifier,
                PagingInfo.ALL);
    }


    /**
     * Gets an {@code de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult} by its handle.
     *
     * @param handle the handle
     * @return the {@code de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult}
     * @throws ResourceNotFoundException if the handle does not exist
     */
    public OperationResult getOperationResult(OperationHandle handle) throws ResourceNotFoundException;


    /**
     * Finds {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}s by search criteria.
     *
     * @param criteria the search criteria
     * @param modifier the modifier
     * @param paging paging information
     * @return the found {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}s
     */
    public List<AssetAdministrationShell> findAssetAdministrationShells(AssetAdministrationShellSearchCriteria criteria, QueryModifier modifier, PagingInfo paging);


    /**
     * Finds {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}s by search criteria.
     *
     * @param criteria the search criteria
     * @param modifier the modifier
     * @param paging paging information
     * @return the found {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}s
     */
    public List<Submodel> findSubmodels(SubmodelSearchCriteria criteria, QueryModifier modifier, PagingInfo paging);


    /**
     * Finds {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s by search criteria.
     *
     * @param criteria the search criteria
     * @param modifier the modifier
     * @param paging paging information
     * @return the found {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s
     */
    public List<SubmodelElement> findSubmodelElements(SubmodelElementSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws ResourceNotFoundException;


    /**
     * Finds {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription}s by search criteria.
     *
     * @param criteria the search criteria
     * @param modifier the modifier
     * @param paging paging information
     * @return the found {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription}s
     */
    public List<ConceptDescription> findConceptDescriptions(ConceptDescriptionSearchCriteria criteria, QueryModifier modifier, PagingInfo paging);


    /**
     * Save an {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}.
     *
     * @param assetAdministrationShell the {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell} to
     *            save
     */
    public void save(AssetAdministrationShell assetAdministrationShell);


    /**
     * Save a {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription}.
     *
     * @param conceptDescription the {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription} to save
     */
    public void save(ConceptDescription conceptDescription);


    /**
     * Save a {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}.
     *
     * @param submodel the {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel} to save
     */
    public void save(Submodel submodel);


    /**
     * Save a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}.
     *
     * @param identifier the identifier of the SubmodelElement
     * @param submodelElement the {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} to save
     * @throws ResourceNotFoundException if the parent cannot be found
     * @throws ResourceNotAContainerElementException if the parent is not a valid container element, i.e. cannot contain
     *             {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s
     */
    public void save(SubmodelElementIdentifier identifier, SubmodelElement submodelElement) throws ResourceNotFoundException, ResourceNotAContainerElementException;


    /**
     * Save a {@code de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult}.
     *
     * @param handle the handle of the {@code de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult}
     * @param result the {@code de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult} to save
     */
    public void save(OperationHandle handle, OperationResult result);


    /**
     * Deletes an {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell} by id.
     *
     * @param id the id
     * @throws ResourceNotFoundException if the resource does not exist
     */
    public void deleteAssetAdministrationShell(String id) throws ResourceNotFoundException;


    /**
     * Deletes a {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel} by id.
     *
     * @param id the id
     * @throws ResourceNotFoundException if the resource does not exist
     */
    public void deleteSubmodel(String id) throws ResourceNotFoundException;


    /**
     * Deletes a {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription} by id.
     *
     * @param id the id
     * @throws ResourceNotFoundException if the resource does not exist
     */
    public void deleteConceptDescription(String id) throws ResourceNotFoundException;


    /**
     * Deletes a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} by idShort path.
     *
     * @param identifier the identifier of the SubmodelElement
     * @throws ResourceNotFoundException if the resource does not exist
     */
    public void deleteSubmodelElement(SubmodelElementIdentifier identifier) throws ResourceNotFoundException;


    /**
     * Deletes an {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}.
     *
     * @param assetAdministrationShell the {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell} to
     *            delete
     * @throws ResourceNotFoundException if the resource does not exist
     */
    public default void deleteAssetAdministrationShell(AssetAdministrationShell assetAdministrationShell) throws ResourceNotFoundException {
        Ensure.requireNonNull(assetAdministrationShell, "assetAdministrationShell must be non-null");
        deleteAssetAdministrationShell(assetAdministrationShell.getId());
    }


    /**
     * Deletes a {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}.
     *
     * @param submodel the {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel} to delete
     * @throws ResourceNotFoundException if the resource does not exist
     */
    public default void deleteSubmodel(Submodel submodel) throws ResourceNotFoundException {
        Ensure.requireNonNull(submodel, "submodel must be non-null");
        deleteSubmodel(submodel.getId());
    }


    /**
     * Deletes a {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription}.
     *
     * @param conceptDescription the {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription} to delete
     * @throws ResourceNotFoundException if the resource does not exist
     */
    public default void deleteConceptDescription(ConceptDescription conceptDescription) throws ResourceNotFoundException {
        Ensure.requireNonNull(conceptDescription, "conceptDescription must be non-null");
        deleteConceptDescription(conceptDescription.getId());
    }


    /**
     * Deletes a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} by reference.
     *
     * @param reference the reference
     * @throws ResourceNotFoundException if the resource does not exist
     */
    public default void deleteSubmodelElement(Reference reference) throws ResourceNotFoundException {
        deleteSubmodelElement(SubmodelElementIdentifier.fromReference(reference));
    }


    /**
     * Save a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}.
     *
     * @param parent the parent of the {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     * @param submodelElement the {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} to save
     * @throws ResourceNotFoundException if the parent cannot be found
     * @throws ResourceNotAContainerElementException if the parent is not a valid container element, i.e. cannot contain
     *             {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s
     */
    public default void save(Reference parent, SubmodelElement submodelElement) throws ResourceNotFoundException, ResourceNotAContainerElementException {
        save(SubmodelElementIdentifier.fromReference(parent), submodelElement);
    }


    /**
     * Gets a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} by idShort path.
     *
     * @param <T> the concrete subtype of {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     * @param identifier the identifier of the SubmodelElement
     * @param modifier the modifier
     * @param type the concrete subtype of {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     * @return the {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} identified by the given path
     * @throws ResourceNotFoundException if there is no {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     *             with the given path
     */
    public default <T extends SubmodelElement> T getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier, Class<T> type) throws ResourceNotFoundException {
        // TODO improve stability
        return type.cast(getSubmodelElement(identifier, modifier));
    }


    /**
     * Gets a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} by reference.
     *
     * @param reference the reference
     * @param modifier the modifier
     * @return the {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} identified by the given path
     * @throws ResourceNotFoundException if there is no {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     *             with the given path
     */
    public default SubmodelElement getSubmodelElement(Reference reference, QueryModifier modifier) throws ResourceNotFoundException {
        String submodelId = ReferenceHelper.findFirstKeyType(reference, KeyTypes.SUBMODEL);
        if (Objects.isNull(submodelId)) {
            throw new ResourceNotFoundException(reference);
        }
        return getSubmodelElement(SubmodelElementIdentifier.fromReference(reference), modifier);
    }


    /**
     * Gets a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} by reference.
     *
     * @param <T> the concrete subtype of {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     * @param reference the reference
     * @param modifier the modifier
     * @param type the concrete subtype of {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     * @return the {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} identified by the given path
     * @throws ResourceNotFoundException if there is no {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     *             with the given reference
     */
    public default <T extends SubmodelElement> T getSubmodelElement(Reference reference, QueryModifier modifier, Class<T> type) throws ResourceNotFoundException {
        String submodelId = ReferenceHelper.findFirstKeyType(reference, KeyTypes.SUBMODEL);
        if (Objects.isNull(submodelId)) {
            throw new ResourceNotFoundException(reference);
        }
        return getSubmodelElement(SubmodelElementIdentifier.fromReference(reference), modifier, type);
    }


    /**
     * Gets all children {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s of a
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel},
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection}, or
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList}.
     *
     * @param reference the reference to the parent/container element
     * @param modifier the modifier
     * @return a list of all child {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s of the element
     *         identified by reference
     * @throws ResourceNotFoundException if there is no element with the given reference
     * @throws ResourceNotAContainerElementException if the element identified by the reference is not a container
     *             element, i.e. cannot have any child elements
     */
    public default List<SubmodelElement> getSubmodelElements(Reference reference, QueryModifier modifier) throws ResourceNotFoundException, ResourceNotAContainerElementException {
        return getSubmodelElements(SubmodelElementIdentifier.fromReference(reference), modifier);
    }


    /**
     * Gets all {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}s.
     *
     * @param modifier the modifier
     * @param paging paging information
     * @return all {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}s
     */
    public default List<AssetAdministrationShell> getAllAssetAdministrationShells(QueryModifier modifier, PagingInfo paging) {
        return findAssetAdministrationShells(AssetAdministrationShellSearchCriteria.NONE, modifier, paging);
    }


    /**
     * Gets all {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}s.
     *
     * @param modifier the modifier
     * @param paging paging information
     * @return all {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}s
     */
    public default List<Submodel> getAllSubmodels(QueryModifier modifier, PagingInfo paging) {
        return findSubmodels(SubmodelSearchCriteria.NONE, modifier, paging);
    }


    /**
     * Gets all {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription}s.
     *
     * @param modifier the modifier
     * @param paging paging information
     * @return all {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription}s
     */
    public default List<ConceptDescription> getAllConceptDescriptions(QueryModifier modifier, PagingInfo paging) {
        return findConceptDescriptions(ConceptDescriptionSearchCriteria.NONE, modifier, paging);
    }


    /**
     * Gets all {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s.
     *
     * @param modifier the modifier
     * @param paging paging information
     * @return all {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s
     */
    public default List<SubmodelElement> getAllSubmodelElements(QueryModifier modifier, PagingInfo paging) throws ResourceNotFoundException {
        return findSubmodelElements(SubmodelElementSearchCriteria.NONE, modifier, paging);
    }
}
