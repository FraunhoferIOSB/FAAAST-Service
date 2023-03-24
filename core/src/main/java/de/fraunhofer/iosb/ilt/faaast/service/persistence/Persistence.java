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
import de.fraunhofer.iosb.ilt.faaast.service.model.aasx.AASXPackage;
import de.fraunhofer.iosb.ilt.faaast.service.model.aasx.PackageDescription;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.List;
import java.util.Set;


/**
 * An implementation of a persistence inherits from this interface. The persistence manages create, read, update and
 * delete actions with the element in the corresponding
 * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}. Each persistence instance needs one instance
 * of an Asset Administration Shell Environment. There can only be one running instance of a persistence implementation.
 *
 * @param <C> type of the corresponding configuration class
 */
public interface Persistence<C extends PersistenceConfig> extends Configurable<C> {

    /**
     * Gets an Identifiable by id and type.
     *
     * @param <T> defines the type of the requested Identifiable
     * @param id the Identifier of the requested Identifiable
     * @param modifier QueryModifier to define Level and Extent of the query
     * @param type the expected type of the Identifiable
     * @return the Identifiable with the given Identifier
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException if no resource can be found for
     *             id or type of found resource does not match requeste type
     * @throws IllegalArgumentException if id is null
     * @throws IllegalArgumentException if modifier is null
     * @throws IllegalArgumentException if type is null
     */
    public <T extends Identifiable> T get(Identifier id, QueryModifier modifier, Class<T> type) throws ResourceNotFoundException;


    /**
     * Get a Submodel Element by a Reference.
     *
     * @param reference of the requested Submodel Element
     * @param modifier QueryModifier to define Level and Extent of the query
     * @return the Submodel Element with the given Reference
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException if reference does not point to
     *             valid resource
     * @throws IllegalArgumentException if modifier is null
     */
    public SubmodelElement get(Reference reference, QueryModifier modifier) throws ResourceNotFoundException;


    /**
     * All Asset Administration Shell that are linked to a globally unique asset identifier, to specific asset ids or
     * with a specific idShort.
     * 
     * <p>If idShort and assetId are null, all AssetAdministrationShells will be returned.
     *
     * @param idShort of the AssetAdministrationShells which should be considered. This parameter is optional and may be
     *            null
     * @param assetIds A List of Global asset ids (use GlobalAssetIdentification.class) which refers to
     *            AssetInformation/globalAssetId of a shell and specific asset ids (use SpecificAssetIdentification.class)
     *            which
     *            refers to IdentifierKeyValuePair/key of a shell. The given asset ids are combined with a logical "or".
     *            This
     *            parameter is optional and may be null
     * @param modifier QueryModifier to define Level and Extent of the query
     * @return List of AssetAdministrationShells
     * @throws IllegalArgumentException if modifier is null
     */
    public List<AssetAdministrationShell> get(String idShort, List<AssetIdentification> assetIds, QueryModifier modifier);


    /**
     * All Submodels with a specific semanticId or a specific idShort. If semanticId and idShort are null, all Submodels
     * will be returned.
     *
     * @param idShort of the Submodels which should be considered. This parameter is optional and may be null
     * @param semanticId of the Submodels which should be considered. This parameter is optional and may be null
     * @param modifier QueryModifier to define Level and Extent of the query
     * @return List of Submodels
     * @throws IllegalArgumentException if modifier is null
     */
    public List<Submodel> get(String idShort, Reference semanticId, QueryModifier modifier);


    /**
     * All Submodel Elements including their hierarchy.
     *
     * @param reference of the Submodel or of the parent Submodel Element
     * @param semanticId of the Submodel Elements which should be considered. This parameter is optional and may be null
     * @param modifier QueryModifier to define Level and Extent of the query
     * @return List of Submodel Elements
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException if reference does not point to
     *             valid resource
     * @throws IllegalArgumentException if modifier is null
     */
    public List<SubmodelElement> getSubmodelElements(Reference reference, Reference semanticId, QueryModifier modifier) throws ResourceNotFoundException;


    /**
     * All Concept Descriptions with a specific idShort, isCaseOf-reference or dataSpecification-reference. If idShort,
     * isCaseOf and dataSpecification are null, all Concept Descriptions will be returned.
     *
     * @param idShort of the Concept Description which should considered. This parameter is optional and may be null
     * @param isCaseOf of the Concept Description which should considered. This parameter is optional and may be null
     * @param dataSpecification of the Concept Description which should considered. This parameter is optional and may
     *            be null
     * @param modifier QueryModifier to define Level and Extent of the query
     * @return List of Concept Descriptions
     * @throws IllegalArgumentException if modifier is null
     */
    public List<ConceptDescription> get(String idShort, Reference isCaseOf, Reference dataSpecification, QueryModifier modifier);


    /**
     * Get a specific AASX package by its packageId.
     *
     * @param packageId of the desired AASX package
     * @return a AASX package
     */
    public AASXPackage get(String packageId);


    /**
     * Get the AssetAdministrationShellEnvironment.
     *
     * @return AssetAdministrationShellEnvironment
     */
    public AssetAdministrationShellEnvironment getEnvironment();


    /**
     * Create or Update an Identifiable.
     *
     * @param identifiable to save
     * @param <T> the type of the Identifiable
     * @return the saved Identifiable as confirmation
     */
    public <T extends Identifiable> T put(T identifiable);


    /**
     * Create or Update the Submodel Element on the given reference.
     *
     * @param parent of the new Submodel Element. Could be null if a direct reference to the submodelelement is set.
     * @param referenceToSubmodelElement reference to the submodelelement which should be updated
     * @param submodelElement which should be added to the parent
     * @return the created Submodel Element
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException if parent and reference does
     *             not point to valid resource
     */
    public SubmodelElement put(Reference parent, Reference referenceToSubmodelElement, SubmodelElement submodelElement) throws ResourceNotFoundException;


    /**
     * Create or Update an AASX package.
     *
     * @param packageId of the existing AASX package
     * @param aasIds the included AAS Ids
     * @param file the AASX package
     * @param fileName of the AASX package
     * @return the updated AASX package
     */
    public AASXPackage put(String packageId, Set<Identifier> aasIds, AASXPackage file, String fileName);


    /**
     * Remove an Identifiable.
     *
     * @param id of the Identifiable
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException if resource is not found
     */
    public void remove(Identifier id) throws ResourceNotFoundException;


    /**
     * Remove a Referable.
     *
     * @param reference to the Referable
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException if resource is not found
     */
    public void remove(Reference reference) throws ResourceNotFoundException;


    /**
     * Remove an AASX package.
     *
     * @param packageId of the AASX package to be removed
     */
    public void remove(String packageId);


    /**
     * Get a list of available AASX package descriptions. If aasId is null, all AASX package descriptions will be
     * returned
     *
     * @param aasId of AASX packages which should be considered. This parameter is optional and may be null
     * @return List of package descriptions
     */
    public List<PackageDescription> get(Identifier aasId);


    /**
     * Save an AASX package.
     *
     * @param aasIds the included AAS Ids
     * @param file the AASX package
     * @param fileName of the AASX package
     * @return the package id of the created AASX package
     */
    public String put(Set<Identifier> aasIds, AASXPackage file, String fileName);


    /**
     * Get an OperationResult of an Operation if available. If not available returns null.
     *
     * @param handleId of the OperationResult
     * @return the OperationResult if available else null
     */
    public OperationResult getOperationResult(String handleId);


    /**
     * Creates a new OperationHandle instance with a unique id. If handleId is empty or null Otherwise updates the
     * existing OperationHandle / OperationResult combination
     *
     * @param handleId of the OperationRequest - could be null if the OperationHandle still not exists
     * @param requestId of the client
     * @param operationResult of the Operation - if null a initial OperationResult will be created
     * @return the belonging OperationHandleInstance or create a new one
     */
    public OperationHandle putOperationContext(String handleId, String requestId, OperationResult operationResult);


    /**
     * Provides type information about an element identified by reference.
     *
     * @param reference reference identifying the element
     * @return type information of the referenced element, empty
     *         {@link de.fraunhofer.iosb.ilt.faaast.service.typing.ContainerTypeInfo} if no matching type is found, null if
     *         reference is null
     * @throws IllegalArgumentException if reference can not be resolved on AAS environment of the service
     */
    public TypeInfo<?> getTypeInfo(Reference reference);


    /**
     * Returns the output variables of an operation identified by a reference.
     *
     * @param reference the reference identifying the operation
     * @return output variables of the operation identified by the reference
     * @throws IllegalArgumentException if reference is null
     * @throws IllegalArgumentException if reference cannot be resolved
     * @throws IllegalArgumentException if reference does not point to an operation
     */
    public OperationVariable[] getOperationOutputVariables(Reference reference);

}
