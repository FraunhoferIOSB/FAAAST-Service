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
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Asset;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;


/**
 * Class to handle {@link io.adminshell.aas.v3.model.Identifiable}
 * Following identifiables are supported:
 * <p>
 * <ul>
 * <li>{@link io.adminshell.aas.v3.model.AssetAdministrationShell}
 * <li>{@link io.adminshell.aas.v3.model.Submodel}
 * <li>{@link io.adminshell.aas.v3.model.ConceptDescription}
 * <li>{@link io.adminshell.aas.v3.model.Asset}
 * </ul>
 * <p>
 */
public class IdentifiablePersistenceManager extends PersistenceManager {

    /**
     * Get an identifiable by its identifier
     * Following identifiables are supported:
     * <p>
     * <ul>
     * <li>{@link io.adminshell.aas.v3.model.AssetAdministrationShell}
     * <li>{@link io.adminshell.aas.v3.model.Submodel}
     * <li>{@link io.adminshell.aas.v3.model.ConceptDescription}
     * <li>{@link io.adminshell.aas.v3.model.Asset}
     * </ul>
     * <p>
     *
     * @param id of the Identifiable
     * @param <T> type of the Identifiable
     * @return the Identifiable
     * @throws ResourceNotFoundException
     */
    public <T extends Identifiable> T getIdentifiableById(Identifier id) throws ResourceNotFoundException {
        if (id == null || this.aasEnvironment == null) {
            return null;
        }
        Identifiable identifiable = EnvironmentHelper.findIdentifiableInListsById(id,
                this.aasEnvironment.getAssetAdministrationShells(),
                this.aasEnvironment.getSubmodels(),
                this.aasEnvironment.getConceptDescriptions(),
                this.aasEnvironment.getAssets());

        if (identifiable == null) {
            throw new ResourceNotFoundException("Resource not found with ID " + id.getIdentifier());
        }

        return (T) identifiable;
    }


    /**
     * Get a list of asset administration shells by idShort or by a list of assetIds.
     * The assetIds could contain two types
     * <p>
     * <ul>
     * <li>{@link de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification}
     * <li>{@link de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification}
     * </ul>
     * <p>
     * If both parameters are null all asset administration shells will be returned.
     *
     * @param idShort of the searched asset administration shells
     * @param assetIds list of asset identifications
     * @return a list of asset administration shells matching the parameters
     */
    public List<AssetAdministrationShell> getAASs(String idShort, List<AssetIdentification> assetIds) {
        if (this.aasEnvironment == null) {
            return null;
        }
        if (StringUtils.isNoneBlank(idShort)) {
            return EnvironmentHelper.getDeepCopiedShells(x -> x.getIdShort().equalsIgnoreCase(idShort), this.aasEnvironment);
        }

        if (assetIds != null) {
            List<AssetAdministrationShell> shells = new ArrayList<>();
            for (AssetIdentification assetId: assetIds) {
                if (GlobalAssetIdentification.class.isAssignableFrom(assetId.getClass())) {
                    shells.addAll(EnvironmentHelper.getDeepCopiedShells(
                            x -> x.getAssetInformation() != null
                                    && x.getAssetInformation().getGlobalAssetId() != null
                                    && x.getAssetInformation().getGlobalAssetId().getKeys().stream()
                                            .anyMatch(y -> ((GlobalAssetIdentification) assetId).getReference().getKeys().stream()
                                                    .anyMatch(z -> z.getValue().equalsIgnoreCase(y.getValue()))),
                            this.aasEnvironment));
                }

                if (SpecificAssetIdentification.class.isAssignableFrom(assetId.getClass())) {
                    shells.addAll(EnvironmentHelper.getDeepCopiedShells(
                            x -> x.getAssetInformation() != null
                                    && x.getAssetInformation().getSpecificAssetIds().stream()
                                            .anyMatch(y -> y.getKey().equalsIgnoreCase(((SpecificAssetIdentification) assetId).getKey())
                                                    && y.getValue().equalsIgnoreCase(((SpecificAssetIdentification) assetId).getValue())),
                            this.aasEnvironment));
                }
            }
            return shells;
        }

        return EnvironmentHelper.getDeepCopiedShells(x -> true, this.aasEnvironment);
    }


    /**
     * Get a list of submodels by idshort or by semantic id.
     * If both parameters are null all submodels will be returned.
     *
     * @param idShort of the searched submodels
     * @param semanticId of the searched submodels
     * @return a list of submodels matching the criteria
     */
    public List<Submodel> getSubmodels(String idShort, Reference semanticId) {
        if (this.aasEnvironment == null) {
            return null;
        }

        if (StringUtils.isNoneBlank(idShort)) {
            return EnvironmentHelper.getDeepCopiedSubmodels(x -> x.getIdShort().equalsIgnoreCase(idShort), this.aasEnvironment);
        }

        if (semanticId != null) {
            return EnvironmentHelper.getDeepCopiedSubmodels(x -> x.getSemanticId() != null
                    && ReferenceHelper.isEqualsIgnoringKeyType(x.getSemanticId(), semanticId), this.aasEnvironment);
        }

        return EnvironmentHelper.getDeepCopiedSubmodels(x -> true, this.aasEnvironment);
    }


    /**
     * Get a list of concept descriptions by idshort, isCaseOf and dataSpecification.
     * Adds all matching concept descriptions for each parameter to the result list.
     *
     * @param idShort of the searched concept descriptions
     * @param isCaseOf of the searched concept descriptions
     * @param dataSpecification of the searched concept descriptions
     * @return a list of all concept descriptions which matches at least one of the criteria
     */
    public List<ConceptDescription> getConceptDescriptions(String idShort, Reference isCaseOf, Reference dataSpecification) {
        if (this.aasEnvironment == null) {
            return null;
        }

        List<ConceptDescription> conceptDescriptions = null;

        if (StringUtils.isNoneBlank(idShort)) {
            conceptDescriptions = this.aasEnvironment.getConceptDescriptions().stream()
                    .filter(x -> x.getIdShort().equalsIgnoreCase(idShort))
                    .collect(Collectors.toList());
        }

        if (isCaseOf != null) {
            Predicate<ConceptDescription> filter = x -> x.getIsCaseOfs().stream().anyMatch(y -> ReferenceHelper.isEqualsIgnoringKeyType(y, isCaseOf));
            if (conceptDescriptions == null) {
                conceptDescriptions = this.aasEnvironment.getConceptDescriptions().stream()
                        .filter(filter)
                        .collect(Collectors.toList());
            }
            else {
                conceptDescriptions = conceptDescriptions.stream()
                        .filter(filter)
                        .collect(Collectors.toList());
            }
        }

        if (dataSpecification != null) {
            Predicate<ConceptDescription> filter = x -> x.getEmbeddedDataSpecifications() != null
                    && x.getEmbeddedDataSpecifications().stream()
                            .anyMatch(y -> y.getDataSpecification() != null && ReferenceHelper.isEqualsIgnoringKeyType(y.getDataSpecification(), dataSpecification));
            if (conceptDescriptions == null) {
                conceptDescriptions = this.aasEnvironment.getConceptDescriptions().stream()
                        .filter(filter)
                        .collect(Collectors.toList());
            }
            else {
                conceptDescriptions = conceptDescriptions.stream().filter(filter).collect(Collectors.toList());
            }
        }

        if (StringUtils.isBlank(idShort) && isCaseOf == null && dataSpecification == null) {
            conceptDescriptions = this.aasEnvironment.getConceptDescriptions();
        }

        Class conceptDescriptionClass = conceptDescriptions != null && !conceptDescriptions.isEmpty() ? conceptDescriptions.get(0).getClass() : ConceptDescription.class;
        return DeepCopyHelper.deepCopy(conceptDescriptions, conceptDescriptionClass);
    }


    /**
     * Remove an identifiable by its identifier.
     * Following identifiables are supported:
     * <p>
     * <ul>
     * <li>{@link io.adminshell.aas.v3.model.AssetAdministrationShell}
     * <li>{@link io.adminshell.aas.v3.model.Submodel}
     * <li>{@link io.adminshell.aas.v3.model.ConceptDescription}
     * <li>{@link io.adminshell.aas.v3.model.Asset}
     * </ul>
     * <p>
     *
     * @param id of the indetifiable which should be removed
     * @throws ResourceNotFoundException if there is no identifiable with such an identifer
     */
    public void remove(Identifier id) throws ResourceNotFoundException {
        if (id == null || this.aasEnvironment == null) {
            return;
        }

        Predicate<Identifiable> removeFilter = x -> !x.getIdentification().getIdentifier().equalsIgnoreCase(id.getIdentifier());

        Identifiable identifiable = getIdentifiableById(id);
        if (identifiable == null) {
            throw new ResourceNotFoundException("Resource not found with ID " + id.getIdentifier());
        }

        //TODO: use reflection?
        if (AssetAdministrationShell.class.isAssignableFrom(identifiable.getClass())) {
            List<AssetAdministrationShell> newAASList;
            newAASList = this.aasEnvironment.getAssetAdministrationShells().stream().filter(removeFilter).collect(Collectors.toList());
            this.aasEnvironment.setAssetAdministrationShells(newAASList);
        }
        else if (Submodel.class.isAssignableFrom(identifiable.getClass())) {
            List<Submodel> newSubmodelList;
            newSubmodelList = this.aasEnvironment.getSubmodels().stream().filter(removeFilter).collect(Collectors.toList());
            this.aasEnvironment.setSubmodels(newSubmodelList);
            Reference referenceOfIdentifiable = AasUtils.toReference(identifiable);
            this.aasEnvironment.getAssetAdministrationShells().forEach(x -> x.getSubmodels().remove(referenceOfIdentifiable));
        }
        else if (ConceptDescription.class.isAssignableFrom(identifiable.getClass())) {
            List<ConceptDescription> newConceptDescriptionList;
            newConceptDescriptionList = this.aasEnvironment.getConceptDescriptions().stream().filter(removeFilter).collect(Collectors.toList());
            this.aasEnvironment.setConceptDescriptions(newConceptDescriptionList);
        }
        else if (Asset.class.isAssignableFrom(identifiable.getClass())) {
            List<Asset> newAssetList;
            newAssetList = this.aasEnvironment.getAssets().stream().filter(removeFilter).collect(Collectors.toList());
            this.aasEnvironment.setAssets(newAssetList);
        }
    }


    /**
     * Create or Update an identifiable
     * Following identifiables are supported:
     * <p>
     * <ul>
     * <li>{@link io.adminshell.aas.v3.model.AssetAdministrationShell}
     * <li>{@link io.adminshell.aas.v3.model.Submodel}
     * <li>{@link io.adminshell.aas.v3.model.ConceptDescription}
     * <li>{@link io.adminshell.aas.v3.model.Asset}
     * </ul>
     * <p>
     *
     * @param identifiable which should be added or updated
     * @return the added or updated identifiable
     */
    public Identifiable put(Identifiable identifiable) {
        if (identifiable == null || this.aasEnvironment == null) {
            return null;
        }

        if (Submodel.class.isAssignableFrom(identifiable.getClass())) {
            this.aasEnvironment.setSubmodels(EnvironmentHelper.updateIdentifiableList(this.aasEnvironment.getSubmodels(), identifiable));
            return identifiable;
        }
        else if (AssetAdministrationShell.class.isAssignableFrom(identifiable.getClass())) {
            this.aasEnvironment.setAssetAdministrationShells(
                    EnvironmentHelper.updateIdentifiableList(this.aasEnvironment.getAssetAdministrationShells(), identifiable));
            return identifiable;
        }
        else if (ConceptDescription.class.isAssignableFrom(identifiable.getClass())) {
            this.aasEnvironment
                    .setConceptDescriptions(EnvironmentHelper.updateIdentifiableList(this.aasEnvironment.getConceptDescriptions(), identifiable));
            return identifiable;
        }
        else if (Asset.class.isAssignableFrom(identifiable.getClass())) {
            this.aasEnvironment.setAssets(EnvironmentHelper.updateIdentifiableList(this.aasEnvironment.getAssets(), identifiable));
            return identifiable;
        }
        return null;
    }

}
