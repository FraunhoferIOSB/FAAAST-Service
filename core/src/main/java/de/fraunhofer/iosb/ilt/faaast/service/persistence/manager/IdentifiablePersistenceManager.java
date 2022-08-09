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

import com.google.common.base.Objects;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.IdentifierHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Asset;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.impl.DefaultIdentifierKeyValuePair;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;


/**
 * Class to handle {@link Identifiable} Following
 * identifiables are supported:
 * <ul>
 * <li>{@link AssetAdministrationShell}
 * <li>{@link Submodel}
 * <li>{@link ConceptDescription}
 * <li>{@link Asset}
 * </ul>
 */
public class IdentifiablePersistenceManager extends PersistenceManager {

    /**
     * Get an identifiable by its identifier Following identifiables are
     * supported:
     * <ul>
     * <li>{@link AssetAdministrationShell}
     * <li>{@link Submodel}
     * <li>{@link ConceptDescription}
     * <li>{@link Asset}
     * </ul>
     *
     * @param id of the Identifiable
     * @param <T> type of the Identifiable
     * @return the Identifiable
     * @throws ResourceNotFoundException if resource is not found
     */
    public <T extends Identifiable> T getIdentifiableById(Identifier id) throws ResourceNotFoundException {
        ensureInitialized();
        if (id == null) {
            return null;
        }
        return (T) Stream.of(aasEnvironment.getAssetAdministrationShells(),
                aasEnvironment.getSubmodels(),
                aasEnvironment.getConceptDescriptions(),
                aasEnvironment.getAssets())
                .flatMap(Collection::stream)
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(id.getIdentifier()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(String.format(ERROR_MSG_RESOURCE_NOT_FOUND_BY_ID, IdentifierHelper.asString(id))));
    }


    /**
     * Get a list of asset administration shells by idShort and by a list of
     * assetIds. The assetIds could contain two types
     * <ul>
     * <li>{@link GlobalAssetIdentification}
     * <li>{@link SpecificAssetIdentification}
     * </ul>
     * If both parameters are null all asset administration shells will be
     * returned.
     *
     * @param idShort of the searched asset administration shells
     * @param assetIds list of asset identifications
     * @return a list of asset administration shells matching the parameters
     */
    public List<AssetAdministrationShell> getAASs(String idShort, List<AssetIdentification> assetIds) {
        ensureInitialized();
        List<AssetAdministrationShell> result = aasEnvironment.getAssetAdministrationShells()
                .stream()
                .filter(aas -> StringUtils.isAllBlank(idShort) || aas.getIdShort().equalsIgnoreCase(idShort))
                // globalAssetId
                .filter(aas -> assetIds == null || assetIds.stream().noneMatch(x -> GlobalAssetIdentification.class.isAssignableFrom(x.getClass()))
                        || (aas.getAssetInformation() != null
                                && assetIds.stream()
                                        .filter(x -> GlobalAssetIdentification.class.isAssignableFrom(x.getClass()))
                                        .map(GlobalAssetIdentification.class::cast)
                                        .anyMatch(x -> Objects.equal(aas.getAssetInformation().getGlobalAssetId(), x.getReference()))))
                // specificAssetId
                .filter(aas -> assetIds == null || assetIds.stream().noneMatch(x -> SpecificAssetIdentification.class.isAssignableFrom(x.getClass()))
                        || (aas.getAssetInformation() != null
                                && assetIds.stream()
                                        .filter(x -> SpecificAssetIdentification.class.isAssignableFrom(x.getClass()))
                                        .map(x -> new DefaultIdentifierKeyValuePair.Builder()
                                                .key(((SpecificAssetIdentification) x).getKey())
                                                .value(((SpecificAssetIdentification) x).getValue())
                                                .build())
                                        .anyMatch(x -> aas.getAssetInformation().getSpecificAssetIds().contains(x))))
                .collect(Collectors.toList());
        return DeepCopyHelper.deepCopy(result, AssetAdministrationShell.class);
    }


    /**
     * Get a list of submodels by idshort and by semantic id. If both parameters
     * are null all submodels will be returned.
     *
     * @param idShort of the searched submodels
     * @param semanticId of the searched submodels
     * @return a list of submodels matching the criteria
     */
    public List<Submodel> getSubmodels(String idShort, Reference semanticId) {
        ensureInitialized();
        List<Submodel> result = aasEnvironment.getSubmodels()
                .stream()
                .filter(x -> StringUtils.isAllBlank(idShort) || x.getIdShort().equalsIgnoreCase(idShort))
                .filter(x -> semanticId == null || (x.getSemanticId() != null
                        && ReferenceHelper.isEqualsIgnoringKeyType(x.getSemanticId(), semanticId)))
                .collect(Collectors.toList());
        return DeepCopyHelper.deepCopy(result, Submodel.class);
    }


    /**
     * Get a list of concept descriptions by idshort, isCaseOf and
     * dataSpecification. Adds all matching concept descriptions for each
     * parameter to the result list.
     *
     * @param idShort of the searched concept descriptions
     * @param isCaseOf of the searched concept descriptions
     * @param dataSpecification of the searched concept descriptions
     * @return a list of all concept descriptions which matches at least one of
     *         the criteria
     */
    public List<ConceptDescription> getConceptDescriptions(String idShort, Reference isCaseOf, Reference dataSpecification) {
        ensureInitialized();
        List<ConceptDescription> result = aasEnvironment.getConceptDescriptions().stream()
                .filter(x -> StringUtils.isAllBlank(idShort) || x.getIdShort().equalsIgnoreCase(idShort))
                .filter(x -> isCaseOf == null || x.getIsCaseOfs().stream().anyMatch(y -> ReferenceHelper.isEqualsIgnoringKeyType(y, isCaseOf)))
                .filter(x -> dataSpecification == null
                        || (x.getEmbeddedDataSpecifications() != null
                                && x.getEmbeddedDataSpecifications().stream()
                                        .anyMatch(y -> y.getDataSpecification() != null
                                                && ReferenceHelper.isEqualsIgnoringKeyType(y.getDataSpecification(), dataSpecification))))
                .collect(Collectors.toList());
        return DeepCopyHelper.deepCopy(result, ConceptDescription.class);
    }


    /**
     * Remove an identifiable by its identifier. Following identifiables are
     * supported:
     * <ul>
     * <li>{@link AssetAdministrationShell}
     * <li>{@link Submodel}
     * <li>{@link ConceptDescription}
     * <li>{@link Asset}
     * </ul>
     *
     * @param id of the indetifiable which should be removed
     * @throws ResourceNotFoundException if there is no identifiable with such
     *             an identifer
     */
    public void remove(Identifier id) throws ResourceNotFoundException {
        ensureInitialized();
        if (id == null) {
            return;
        }
        Predicate<Identifiable> predicate = x -> x.getIdentification().getIdentifier().equalsIgnoreCase(id.getIdentifier());
        if (aasEnvironment.getAssetAdministrationShells().removeIf(predicate)
                || aasEnvironment.getConceptDescriptions().removeIf(predicate)
                || aasEnvironment.getAssets().removeIf(predicate)) {
            return;
        }
        Optional<Submodel> submodelToDelete = aasEnvironment.getSubmodels().stream().filter(predicate).findAny();
        if (submodelToDelete.isPresent()) {
            aasEnvironment.getSubmodels().remove(submodelToDelete.get());
            Reference submodelRef = AasUtils.toReference(submodelToDelete.get());
            aasEnvironment.getAssetAdministrationShells().forEach(x -> x.getSubmodels().remove(submodelRef));
            return;
        }
        throw new ResourceNotFoundException(String.format(ERROR_MSG_RESOURCE_NOT_FOUND_BY_ID, IdentifierHelper.asString(id)));
    }


    /**
     * Create or Update an identifiable. Following identifiables are supported:
     * <ul>
     * <li>{@link AssetAdministrationShell}
     * <li>{@link Submodel}
     * <li>{@link ConceptDescription}
     * <li>{@link Asset}
     * </ul>
     *
     * @param identifiable which should be added or updated
     * @return the added or updated identifiable
     */
    public Identifiable put(Identifiable identifiable) {
        ensureInitialized();
        Ensure.requireNonNull(identifiable, "identifiable must be non-null");
        List<? extends Identifiable> list;
        if (Submodel.class.isAssignableFrom(identifiable.getClass())) {
            list = aasEnvironment.getSubmodels();
        }
        else if (AssetAdministrationShell.class.isAssignableFrom(identifiable.getClass())) {
            list = aasEnvironment.getAssetAdministrationShells();
        }
        else if (ConceptDescription.class.isAssignableFrom(identifiable.getClass())) {
            list = aasEnvironment.getConceptDescriptions();
        }
        else if (Asset.class.isAssignableFrom(identifiable.getClass())) {
            list = aasEnvironment.getAssets();
        }
        else {
            throw new IllegalArgumentException(String.format("illegal type for identifiable: %s. Must be one of: %s, %s, %s, %s",
                    identifiable.getClass(),
                    Submodel.class,
                    AssetAdministrationShell.class,
                    ConceptDescription.class,
                    Asset.class));
        }
        EnvironmentHelper.updateIdentifiableList(list, identifiable);
        return identifiable;
    }

}
