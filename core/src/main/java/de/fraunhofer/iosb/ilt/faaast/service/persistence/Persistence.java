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
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.AASXPackage;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.PackageDescription;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.List;
import java.util.Set;


public interface Persistence<T extends PersistenceConfig> extends Configurable<T> {

    /**
     * Set the AssetAdministrationShellEnvironment<br>
     */
    public void setEnvironment(AssetAdministrationShellEnvironment environment);


    /**
     * Get an Identifiable by an Identifier
     *
     * @param id the Identifier of the requested Identifiable<br>
     * @param modifier QueryModifier to define Level and Extent of the query<br>
     * @param <T> defines the type of the requested Identifiable<br>
     * @return the Identifiable with the given Identifier
     */
    public <T extends Identifiable> T get(Identifier id, QueryModifier modifier) throws ResourceNotFoundException;


    /**
     * Get a Submodel Element by a Reference
     *
     * @param reference of the requested Submodel Element<br>
     * @param modifier QueryModifier to define Level and Extent of the query<br>
     * @return the Submodel Element with the given Reference
     */
    public SubmodelElement get(Reference reference, QueryModifier modifier) throws ResourceNotFoundException;


    /**
     * All Asset Administration Shell that are linked to a globally unique asset identifier, to specific asset ids
     * or with a specific idShort.<br>
     * If idShort and assetId are null, all AssetAdministrationShells will be returned.
     *
     * @param idShort of the AssetAdministrationShells which should be considered. This parameter is optional and may be
     *            null<br>
     * @param assetIds A List of Global asset ids (use GlobalAssetIdentification.class) which refers to
     *            AssetInformation/globalAssetId
     *            of a shell
     *            and specific asset ids (use SpecificAssetIdentification.class) which refers to IdentifierKeyValuePair/key
     *            of a shell.
     *            The given asset ids are combined with a logical "or".
     *            This parameter is optional and may be null<br>
     * @param modifier QueryModifier to define Level and Extent of the query<br>
     * @return List of AssetAdministrationShells
     */
    public List<AssetAdministrationShell> get(String idShort, List<AssetIdentification> assetIds, QueryModifier modifier);


    /**
     * All Submodels with a specific semanticId or a specific idShort.
     * If semanticId and idShort are null, all Submodels will be returned.<br>
     *
     * @param idShort of the Submodels which should be considered. This parameter is optional and may be null<br>
     * @param semanticId of the Submodels which should be considered. This parameter is optional and may be null<br>
     * @param modifier QueryModifier to define Level and Extent of the query<br>
     * @return List of Submodels
     */
    public List<Submodel> get(String idShort, Reference semanticId, QueryModifier modifier);


    /**
     * All Submodel Elements including their hierarchy<br>
     *
     * @param reference of the Submodel or of the parent Submodel Element<br>
     * @param semanticId of the Submodel Elements which should be considered. This parameter is optional and may be null<br>
     * @param modifier QueryModifier to define Level and Extent of the query<br>
     * @return List of Submodel Elements
     */
    public List<SubmodelElement> getSubmodelElements(Reference reference, Reference semanticId, QueryModifier modifier) throws ResourceNotFoundException;


    /**
     * All Concept Descriptions with a specific idShort, isCaseOf-reference or dataSpecification-reference.
     * If idShort, isCaseOf and dataSpecification are null, all Concept Descriptions will be returned<br>
     *
     * @param idShort of the Concept Description which should considered. This parameter is optional and may be null<br>
     * @param isCaseOf of the Concept Description which should considered. This parameter is optional and may be null<br>
     * @param dataSpecification of the Concept Description which should considered. This parameter is optional and may be
     *            null<br>
     * @param modifier QueryModifier to define Level and Extent of the query<br>
     * @return List of Concept Descriptions
     */
    public List<ConceptDescription> get(String idShort, Reference isCaseOf, Reference dataSpecification, QueryModifier modifier);


    /**
     * Get a specific AASX package by its packageId<br>
     *
     * @param packageId of the desired AASX package<br>
     * @return a AASX package
     */
    public AASXPackage get(String packageId);


    /**
     * Get the AssetAdministrationShellEnvironment<br>
     *
     * @return AssetAdministrationShellEnvironment
     */
    public AssetAdministrationShellEnvironment getEnvironment();


    /**
     * Create or Update an Identifiable<br>
     *
     * @param identifiable to save<br>
     * @param <T> the type of the Identifiable<br>
     * @return the saved Identifiable as confirmation
     */
    public <T extends Identifiable> T put(T identifiable);


    /**
     * Create or Update the Submodel Element on the given reference<br>
     *
     * @param parent of the new Submodel Element. Could be null if a direct reference to the submodelelement is set.
     * @param referenceToSubmodelElement reference to the submodelelement which should be updated
     * @param submodelElement which should be added to the parent
     * @return the created Submodel Element
     */
    public SubmodelElement put(Reference parent, Reference referenceToSubmodelElement, SubmodelElement submodelElement) throws ResourceNotFoundException;


    /**
     * Create or Update an AASX package<br>
     *
     * @param packageId of the existing AASX package<br>
     * @param aasIds the included AAS Ids<br>
     * @param file the AASX package<br>
     * @param fileName of the AASX package<br>
     * @return the updated AASX package
     */
    public AASXPackage put(String packageId, Set<Identifier> aasIds, AASXPackage file, String fileName);


    /**
     * Remove an Identifiable<br>
     *
     * @param id of the Identifiable
     */
    public void remove(Identifier id) throws ResourceNotFoundException;


    /**
     * Remove a Referable<br>
     *
     * @param reference to the Referable
     */
    public void remove(Reference reference) throws ResourceNotFoundException;


    /**
     * Remove an AASX package<br>
     *
     * @param packageId of the AASX package to be removed
     */
    public void remove(String packageId);


    /**
     * Get a list of available AASX package descriptions<br>
     * If aasId is null, all AASX package descriptions will be returned<br>
     *
     * @param aasId of AASX packages which should be considered. This parameter is optional and may be null<br>
     * @return List of package descriptions
     */
    public List<PackageDescription> get(Identifier aasId);


    /**
     * Save an AASX package<br>
     *
     * @param aasIds the included AAS Ids<br>
     * @param file the AASX package<br>
     * @param fileName of the AASX package<br>
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
     * Creates a new OperationHandle instance with a unique id if handleId is empty or null
     * Otherwise updates the existing OperationHandle / OperationResult combination
     *
     * @param handleId of the OperationRequest - could be null if the OperationHandle still not exists
     * @param requestId of the client
     * @param operationResult of the Operation - if null a initial OperationResult will be created
     * @return the belonging OperationHandleInstance or create a new one
     */
    public OperationHandle putOperationContext(String handleId, String requestId, OperationResult operationResult);

}
