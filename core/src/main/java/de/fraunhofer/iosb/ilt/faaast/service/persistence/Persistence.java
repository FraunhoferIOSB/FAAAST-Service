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
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Query;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
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
     * Starts the persistence implementation.
     *
     * @throws PersistenceException if there was an error with the storage.
     */
    public void start() throws PersistenceException;


    /**
     * Stops the persistence implementation.
     */
    public void stop();


    /**
     * Gets an {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell} by id.
     *
     * @param id the id
     * @param modifier the modifier
     * @return the {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell} with the given id
     * @throws ResourceNotFoundException if there is no
     *             {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell} with the given id
     * @throws PersistenceException if there was an error with the storage.
     */
    public AssetAdministrationShell getAssetAdministrationShell(String id, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException;


    /**
     * Gets the referenced {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}s of an
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}.
     *
     * @param aasId the id of the AAS
     * @param paging paging information
     * @return the referenced {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}s of an
     *         {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}
     * @throws ResourceNotFoundException if there is no
     *             {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell} with the given id
     * @throws PersistenceException if there was an error with the storage.
     */
    public Page<Reference> getSubmodelRefs(String aasId, PagingInfo paging) throws ResourceNotFoundException, PersistenceException;


    /**
     * Gets a {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel} by id.
     *
     * @param id the id
     * @param modifier the modifier
     * @return the {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel} with the given id
     * @throws ResourceNotFoundException if there is no
     *             {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel} with the
     *             given id
     * @throws PersistenceException if there was an error with the storage.
     */
    public Submodel getSubmodel(String id, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException;


    /**
     * Gets a {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription} by id.
     *
     * @param id the id
     * @param modifier the modifier
     * @return the {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription} with the given id
     * @throws ResourceNotFoundException if there is no
     *             {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription} with the given id
     * @throws PersistenceException if there was an error with the storage.
     */
    public ConceptDescription getConceptDescription(String id, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException;


    /**
     * Gets a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} by idShort path.
     *
     * @param identifier the identifier of the SubmodelElement
     * @param modifier the modifier
     * @return the {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} identified by the given path
     * @throws ResourceNotFoundException if there is no
     *             {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     *             with the given path
     * @throws PersistenceException if there was an error with the storage.
     */
    public SubmodelElement getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException;


    /**
     * Gets all children {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s of a
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel},
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection}, or
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList}.
     *
     * @param identifier the identifier of the SubmodelElement
     * @param modifier the modifier
     * @param paging paging information
     * @return a list of all child {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s of the element
     *         identified by path
     * @throws ResourceNotFoundException if there is no element with the given path
     * @throws ResourceNotAContainerElementException if the element identified by the path is not a container element,
     *             i.e. cannot have any child elements
     * @throws PersistenceException if there was an error with the storage.
     */
    public default Page<SubmodelElement> getSubmodelElements(SubmodelElementIdentifier identifier, QueryModifier modifier, PagingInfo paging)
            throws ResourceNotFoundException, PersistenceException, ResourceNotAContainerElementException {
        return findSubmodelElements(
                SubmodelElementSearchCriteria.builder()
                        .parent(identifier)
                        .build(),
                modifier,
                paging);
    }


    /**
     * Gets all children {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s of a
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel},
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection}, or
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList} that are supported by valueOnly serialization.
     *
     * @param identifier the identifier of the SubmodelElement
     * @param modifier the modifier
     * @param paging paging information
     * @return a list of all child {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s that are supported by
     *         valueOnly serialization of the element identified by path
     * @throws ResourceNotFoundException if there is no element with the given path
     * @throws ResourceNotAContainerElementException if the element identified by the path is not a container element,
     *             i.e. cannot have any child elements
     * @throws PersistenceException if there was an error with the storage.
     */
    public default Page<SubmodelElement> getSubmodelElementsValueOnly(SubmodelElementIdentifier identifier, QueryModifier modifier, PagingInfo paging)
            throws ResourceNotFoundException, PersistenceException, ResourceNotAContainerElementException {
        return findSubmodelElements(
                SubmodelElementSearchCriteria.builder()
                        .parent(identifier)
                        .valueOnly()
                        .build(),
                modifier,
                paging);
    }


    /**
     * Gets all children {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s of a
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel},
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection}, or
     * {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList} that are supported by valueOnly serialization.
     *
     * @param reference the reference to the parent/container element
     * @param modifier the modifier
     * @param paging paging information
     * @return a list of all child {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s that are supported by
     *         valueOnly serialization of the element identified by reference
     * @throws ResourceNotFoundException if there is no element with the given reference
     * @throws ResourceNotAContainerElementException if the element identified by the reference is not a container
     *             element, i.e. cannot have any child elements
     * @throws PersistenceException if there was an error with the storage.
     */
    public default Page<SubmodelElement> getSubmodelElementsValueOnly(Reference reference, QueryModifier modifier, PagingInfo paging)
            throws ResourceNotFoundException, PersistenceException, ResourceNotAContainerElementException {
        return getSubmodelElementsValueOnly(SubmodelElementIdentifier.fromReference(reference), modifier, paging);
    }


    /**
     * Gets an {@code de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult} by its handle.
     *
     * @param handle the handle
     * @return the {@code de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult}
     * @throws ResourceNotFoundException if the handle does not exist
     * @throws PersistenceException if there was an error with the storage.
     */
    public OperationResult getOperationResult(OperationHandle handle) throws ResourceNotFoundException, PersistenceException;


    /**
     * Finds {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}s by search criteria.
     *
     * @param criteria the search criteria
     * @param modifier the modifier
     * @param paging paging information
     * @return the found {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}s
     * @throws PersistenceException if there was an error with the storage.
     */
    public Page<AssetAdministrationShell> findAssetAdministrationShells(AssetAdministrationShellSearchCriteria criteria, QueryModifier modifier, PagingInfo paging)
            throws PersistenceException;


    /**
     * Finds {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}s by search criteria and query.
     *
     * @param criteria the search criteria
     * @param modifier the modifier
     * @param paging paging information
     * @param query the query to be executed
     * @return the found {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}s
     * @throws PersistenceException if there was an error with the storage.
     */
    public Page<AssetAdministrationShell> findAssetAdministrationShellsWithQuery(AssetAdministrationShellSearchCriteria criteria, QueryModifier modifier, PagingInfo paging,
                                                                                 Query query)
            throws PersistenceException;


    /**
     * Finds {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}s by search criteria.
     *
     * @param criteria the search criteria
     * @param modifier the modifier
     * @param paging paging information
     * @return the found {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}s
     * @throws PersistenceException if there was an error with the storage.
     */
    public Page<Submodel> findSubmodels(SubmodelSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws PersistenceException;


    /**
     * Finds {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}s by search criteria and query.
     *
     * @param criteria the search criteria
     * @param modifier the modifier
     * @param paging paging information
     * @param query query to execute
     * @return the found {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}s
     * @throws PersistenceException if there was an error with the storage.
     */
    public Page<Submodel> findSubmodelsWithQuery(SubmodelSearchCriteria criteria, QueryModifier modifier, PagingInfo paging, Query query) throws PersistenceException;


    /**
     * Finds {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s by search criteria.
     *
     * @param criteria the search criteria
     * @param modifier the modifier
     * @param paging paging information
     * @return the found {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s
     * @throws ResourceNotFoundException if the parent does not exist
     * @throws PersistenceException if there was an error with the storage.
     */
    public Page<SubmodelElement> findSubmodelElements(SubmodelElementSearchCriteria criteria, QueryModifier modifier, PagingInfo paging)
            throws ResourceNotFoundException, PersistenceException;


    /**
     * Finds {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription}s by search criteria.
     *
     * @param criteria the search criteria
     * @param modifier the modifier
     * @param paging paging information
     * @return the found {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription}s
     * @throws PersistenceException if there was an error with the storage.
     */
    public Page<ConceptDescription> findConceptDescriptions(ConceptDescriptionSearchCriteria criteria, QueryModifier modifier, PagingInfo paging) throws PersistenceException;


    /**
     * Finds {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription}s by search criteria and query.
     *
     * @param criteria the search criteria
     * @param modifier the modifier
     * @param paging paging information
     * @param query query to execute
     * @return the found {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription}s
     * @throws PersistenceException if there was an error with the storage.
     */
    public Page<ConceptDescription> findConceptDescriptionsWithQuery(ConceptDescriptionSearchCriteria criteria, QueryModifier modifier, PagingInfo paging, Query query)
            throws PersistenceException;


    /**
     * Save an {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}.
     *
     * @param assetAdministrationShell the {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell} to
     *            insert
     * @throws PersistenceException if there was an error with the storage.
     */
    public void save(AssetAdministrationShell assetAdministrationShell) throws PersistenceException;


    /**
     * Save a {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription}.
     *
     * @param conceptDescription the {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription} to insert
     * @throws PersistenceException if there was an error with the storage.
     */
    public void save(ConceptDescription conceptDescription) throws PersistenceException;


    /**
     * Save a {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}.
     *
     * @param submodel the {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel} to insert
     * @throws PersistenceException if there was an error with the storage.
     */
    public void save(Submodel submodel) throws PersistenceException;


    /**
     * Inserts a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} relative to a parent.
     *
     * @param parentIdentifier the identifier of the SubmodelElement
     * @param submodelElement the {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} to insert
     * @throws ResourceNotFoundException if the parent cannot be found
     * @throws ResourceNotAContainerElementException if the parent is not a valid container element, i.e. cannot contain
     *             {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s
     * @throws ResourceAlreadyExistsException if the resource to be created already exists
     * @throws PersistenceException if there was an error with the storage.
     */
    public void insert(SubmodelElementIdentifier parentIdentifier, SubmodelElement submodelElement)
            throws ResourceNotFoundException, ResourceNotAContainerElementException, ResourceAlreadyExistsException, PersistenceException;


    /**
     * Updates a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}.
     *
     * @param identifier the identifier of the SubmodelElement
     * @param submodelElement the {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} to update
     * @throws ResourceNotFoundException if the element cannot be found
     * @throws PersistenceException if there was an error with the storage.
     */
    public void update(SubmodelElementIdentifier identifier, SubmodelElement submodelElement) throws ResourceNotFoundException, PersistenceException;


    /**
     * Save a {@code de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult}.
     *
     * @param handle the handle of the {@code de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult}
     * @param result the {@code de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult} to insert
     * @throws PersistenceException if there was an error with the storage.
     */
    public void save(OperationHandle handle, OperationResult result) throws PersistenceException;


    /**
     * Deletes an {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell} by id.
     *
     * @param id the id
     * @throws ResourceNotFoundException if the resource does not exist
     * @throws PersistenceException if there was an error with the storage.
     */
    public void deleteAssetAdministrationShell(String id) throws ResourceNotFoundException, PersistenceException;


    /**
     * Deletes a {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel} by id.
     *
     * @param id the id
     * @throws ResourceNotFoundException if the resource does not exist
     * @throws PersistenceException if there was an error with the storage.
     */
    public void deleteSubmodel(String id) throws ResourceNotFoundException, PersistenceException;


    /**
     * Deletes a {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription} by id.
     *
     * @param id the id
     * @throws ResourceNotFoundException if the resource does not exist
     * @throws PersistenceException if there was an error with the storage.
     */
    public void deleteConceptDescription(String id) throws ResourceNotFoundException, PersistenceException;


    /**
     * Deletes a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} by idShort path.
     *
     * @param identifier the identifier of the SubmodelElement
     * @throws ResourceNotFoundException if the resource does not exist
     * @throws PersistenceException if there was an error with the storage.
     */
    public void deleteSubmodelElement(SubmodelElementIdentifier identifier) throws ResourceNotFoundException, PersistenceException;


    /**
     * Deletes all data in the persistence.
     *
     * @throws PersistenceException if there was an error with the storage.
     */
    public void deleteAll() throws PersistenceException;


    /**
     * Deletes an {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}.
     *
     * @param assetAdministrationShell the {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell} to
     *            delete
     * @throws ResourceNotFoundException if the resource does not exist
     * @throws PersistenceException if there was an error with the storage.
     */
    public default void deleteAssetAdministrationShell(AssetAdministrationShell assetAdministrationShell) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(assetAdministrationShell, "assetAdministrationShell must be non-null");
        deleteAssetAdministrationShell(assetAdministrationShell.getId());
    }


    /**
     * Deletes a {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}.
     *
     * @param submodel the {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel} to delete
     * @throws ResourceNotFoundException if the resource does not exist
     * @throws PersistenceException if there was an error with the storage.
     */
    public default void deleteSubmodel(Submodel submodel) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(submodel, "submodel must be non-null");
        deleteSubmodel(submodel.getId());
    }


    /**
     * Deletes a {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription}.
     *
     * @param conceptDescription the {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription} to delete
     * @throws ResourceNotFoundException if the resource does not exist
     * @throws PersistenceException if there was an error with the storage.
     */
    public default void deleteConceptDescription(ConceptDescription conceptDescription) throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(conceptDescription, "conceptDescription must be non-null");
        deleteConceptDescription(conceptDescription.getId());
    }


    /**
     * Deletes a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} by reference.
     *
     * @param reference the reference
     * @throws ResourceNotFoundException if the resource does not exist
     * @throws PersistenceException if there was an error with the storage.
     */
    public default void deleteSubmodelElement(Reference reference) throws ResourceNotFoundException, PersistenceException {
        deleteSubmodelElement(SubmodelElementIdentifier.fromReference(reference));
    }


    /**
     * Inserts a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} relative to the parent.
     *
     * @param parent the parent of the {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     * @param submodelElement the {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} to insert
     * @throws ResourceNotFoundException if the parent cannot be found
     * @throws ResourceNotAContainerElementException if the parent is not a valid container element, i.e. cannot contain
     *             {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s
     * @throws ResourceAlreadyExistsException if the resource to be created already exists
     * @throws PersistenceException if there was an error with the storage.
     */
    public default void insert(Reference parent, SubmodelElement submodelElement)
            throws ResourceNotFoundException, ResourceNotAContainerElementException, ResourceAlreadyExistsException, PersistenceException {
        insert(SubmodelElementIdentifier.fromReference(parent), submodelElement);
    }


    /**
     * Updates a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}.
     *
     * @param reference the reference of the SubmodelElement
     * @param submodelElement the {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} to update
     * @throws ResourceNotFoundException if the element cannot be found
     * @throws PersistenceException if there was an error with the storage.
     */
    public default void update(Reference reference, SubmodelElement submodelElement) throws ResourceNotFoundException, PersistenceException {
        update(SubmodelElementIdentifier.fromReference(reference), submodelElement);
    }


    /**
     * Gets a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} by idShort path.
     *
     * @param <T> the concrete subtype of {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     * @param identifier the identifier of the SubmodelElement
     * @param modifier the modifier
     * @param type the concrete subtype of {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     * @return the {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} identified by the given path
     * @throws ResourceNotFoundException if there is no
     *             {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     *             with the given path
     * @throws IllegalArgumentException if type is null
     * @throws ClassCastException if casting fails
     * @throws PersistenceException if there was an error with the storage.
     */
    public default <T extends SubmodelElement> T getSubmodelElement(SubmodelElementIdentifier identifier, QueryModifier modifier, Class<T> type)
            throws ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(type, "type must be non-null");
        return type.cast(getSubmodelElement(identifier, modifier));
    }


    /**
     * Gets a {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} by reference.
     *
     * @param reference the reference
     * @param modifier the modifier
     * @return the {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} identified by the given path
     * @throws ResourceNotFoundException if there is no
     *             {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     *             with the given path
     * @throws PersistenceException if there was an error with the storage.
     */
    public default SubmodelElement getSubmodelElement(Reference reference, QueryModifier modifier) throws ResourceNotFoundException, PersistenceException {
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
     * @throws ResourceNotFoundException if there is no
     *             {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}
     *             with the given reference
     * @throws PersistenceException if there was an error with the storage.
     */
    public default <T extends SubmodelElement> T getSubmodelElement(Reference reference, QueryModifier modifier, Class<T> type)
            throws ResourceNotFoundException, PersistenceException {
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
     * @param paging paging information
     * @return a list of all child {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s of the element
     *         identified by reference
     * @throws ResourceNotFoundException if there is no element with the given reference
     * @throws ResourceNotAContainerElementException if the element identified by the reference is not a container
     *             element, i.e. cannot have any child elements
     * @throws PersistenceException if there was an error with the storage.
     */
    public default Page<SubmodelElement> getSubmodelElements(Reference reference, QueryModifier modifier, PagingInfo paging)
            throws ResourceNotFoundException, PersistenceException, ResourceNotAContainerElementException {
        return getSubmodelElements(SubmodelElementIdentifier.fromReference(reference), modifier, paging);
    }


    /**
     * Gets all {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}s.
     *
     * @param modifier the modifier
     * @param paging paging information
     * @return all {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}s
     * @throws PersistenceException if there was an error with the storage.
     */
    public default Page<AssetAdministrationShell> getAllAssetAdministrationShells(QueryModifier modifier, PagingInfo paging) throws PersistenceException {
        return findAssetAdministrationShells(AssetAdministrationShellSearchCriteria.NONE, modifier, paging);
    }


    /**
     * Gets all {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}s.
     *
     * @param modifier the modifier
     * @param paging paging information
     * @return all {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel}s
     * @throws PersistenceException if there was an error with the storage.
     */
    public default Page<Submodel> getAllSubmodels(QueryModifier modifier, PagingInfo paging) throws PersistenceException {
        return findSubmodels(SubmodelSearchCriteria.NONE, modifier, paging);
    }


    /**
     * Gets all {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription}s.
     *
     * @param modifier the modifier
     * @param paging paging information
     * @return all {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription}s
     * @throws PersistenceException if there was an error with the storage.
     */
    public default Page<ConceptDescription> getAllConceptDescriptions(QueryModifier modifier, PagingInfo paging) throws PersistenceException {
        return findConceptDescriptions(ConceptDescriptionSearchCriteria.NONE, modifier, paging);
    }


    /**
     * Gets all {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s.
     *
     * @param modifier the modifier
     * @param paging paging information
     * @return all {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement}s
     * @throws PersistenceException if there was an error with the storage.
     */
    public default Page<SubmodelElement> getAllSubmodelElements(QueryModifier modifier, PagingInfo paging) throws PersistenceException {
        try {
            return findSubmodelElements(SubmodelElementSearchCriteria.NONE, modifier, paging);
        }
        catch (ResourceNotFoundException e) {
            throw new PersistenceException("unexpected persistence exception", e);
        }
    }


    /**
     * Checks if a given {@code org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell} exists.
     *
     * @param id the id
     * @return true if exists, false otherwise
     */
    public default boolean assetAdministrationShellExists(String id) {
        try {
            return Objects.nonNull(getAssetAdministrationShell(id, QueryModifier.MINIMAL));
        }
        catch (ResourceNotFoundException | PersistenceException e) {
            return false;
        }
    }


    /**
     * Checks if a given {@code org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription} exists.
     *
     * @param id the id
     * @return true if exists, false otherwise
     */
    public default boolean conceptDescriptionExists(String id) {
        try {
            return Objects.nonNull(getConceptDescription(id, QueryModifier.DEFAULT));
        }
        catch (ResourceNotFoundException | PersistenceException e) {
            return false;
        }
    }


    /**
     * Checks if a given {@code org.eclipse.digitaltwin.aas4j.v3.model.Submodel} exists.
     *
     * @param id the id
     * @return true if exists, false otherwise
     */
    public default boolean submodelExists(String id) {
        try {
            return Objects.nonNull(getSubmodel(id, QueryModifier.DEFAULT));
        }
        catch (ResourceNotFoundException | PersistenceException e) {
            return false;
        }
    }


    /**
     * Checks if a given {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} exists.
     *
     * @param reference the reference
     * @return true if exists, false otherwise
     */
    public default boolean submodelElementExists(Reference reference) {
        try {
            return Objects.nonNull(getSubmodelElement(reference, QueryModifier.DEFAULT));
        }
        catch (ResourceNotFoundException | PersistenceException e) {
            return false;
        }
    }


    /**
     * Checks if a given {@code org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement} exists.
     *
     * @param identifier the identifier
     * @return true if exists, false otherwise
     */
    public default boolean submodelElementExists(SubmodelElementIdentifier identifier) {
        try {
            return Objects.nonNull(getSubmodelElement(identifier, QueryModifier.DEFAULT));
        }
        catch (ResourceNotFoundException | PersistenceException e) {
            return false;
        }
    }
}
