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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.managers.identifiable;

import static de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.Util.deepCopy;
import static de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.Util.empty;

import de.fraunhofer.iosb.ilt.faaast.service.model.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.SpecificAssetIdentification;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Asset;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


/**
 * Class to handle identifiable elements
 */
public class IdentifiablePersistenceManager {

    private AssetAdministrationShellEnvironment aasEnvironment;

    public void setAasEnvironment(AssetAdministrationShellEnvironment aasEnvironment) {
        this.aasEnvironment = aasEnvironment;
    }


    public <T extends Identifiable> T getIdentifiableById(Identifier id) {
        if (id == null || this.aasEnvironment == null) {
            return null;
        }
        Identifiable identifiable = Util.findIdentifiableInListsById(id,
                this.aasEnvironment.getAssetAdministrationShells(),
                this.aasEnvironment.getSubmodels(),
                this.aasEnvironment.getConceptDescriptions(),
                this.aasEnvironment.getAssets());

        return (T) identifiable;
    }


    public List<AssetAdministrationShell> getAASs(String idShort, AssetIdentification assetId) {
        if (this.aasEnvironment == null) {
            return null;
        }
        if (!empty(idShort)) {
            List<AssetAdministrationShell> shells = Util.getDeepCopiedShells(x -> x.getIdShort().equalsIgnoreCase(idShort), this.aasEnvironment);
            return shells;
        }

        if (assetId != null) {
            if (GlobalAssetIdentification.class.isAssignableFrom(assetId.getClass())) {
                List<AssetAdministrationShell> shells = Util.getDeepCopiedShells(
                        x -> x.getAssetInformation() != null
                                && x.getAssetInformation().getGlobalAssetId() != null
                                && x.getAssetInformation().getGlobalAssetId().equals(((GlobalAssetIdentification) assetId).getReference()),
                        this.aasEnvironment);
                return shells;
            }

            if (SpecificAssetIdentification.class.isAssignableFrom(assetId.getClass())) {
                List<AssetAdministrationShell> shells = Util.getDeepCopiedShells(
                        x -> x.getAssetInformation() != null
                                && x.getAssetInformation().getSpecificAssetIds().stream()
                                        .anyMatch(y -> y.getKey().equalsIgnoreCase(((SpecificAssetIdentification) assetId).getKey())
                                                && y.getValue().equalsIgnoreCase(((SpecificAssetIdentification) assetId).getValue())),
                        this.aasEnvironment);
                return shells;
            }
            return null;
        }

        //return all
        List<AssetAdministrationShell> shells = Util.getDeepCopiedShells(x -> true, this.aasEnvironment);
        return shells;
    }


    public List<Submodel> getSubmodels(String idShort, Reference semanticId) {
        if (this.aasEnvironment == null) {
            return null;
        }

        if (!empty(idShort)) {
            List<Submodel> submodels = Util.getDeepCopiedSubmodels(x -> x.getIdShort().equalsIgnoreCase(idShort), this.aasEnvironment);
            return submodels;
        }

        if (semanticId != null) {
            List<Submodel> submodels = Util.getDeepCopiedSubmodels(x -> x.getSemanticId() != null
                    && x.getSemanticId().equals(semanticId), this.aasEnvironment);
            return submodels;
        }

        //return all
        List<Submodel> submodels = Util.getDeepCopiedSubmodels(x -> true, this.aasEnvironment);
        return submodels;
    }


    public List<ConceptDescription> getConceptDescriptions(String idShort, Reference isCaseOf, Reference dataSpecification) {
        if (this.aasEnvironment == null) {
            return null;
        }

        List<ConceptDescription> conceptDescriptions = null;

        if (!empty(idShort)) {
            conceptDescriptions = this.aasEnvironment.getConceptDescriptions().stream().filter(x -> x.getIdShort().equalsIgnoreCase(idShort)).collect(Collectors.toList());
        }

        if (isCaseOf != null) {
            Predicate<ConceptDescription> filter = x -> x.getIsCaseOfs().stream().anyMatch(y -> y.equals(isCaseOf));
            if (conceptDescriptions == null) {
                conceptDescriptions = this.aasEnvironment.getConceptDescriptions().stream().filter(filter).collect(Collectors.toList());
            }
            else {
                conceptDescriptions = conceptDescriptions.stream().filter(filter).collect(Collectors.toList());
            }
        }

        if (dataSpecification != null) {
            Predicate<ConceptDescription> filter = x -> x.getEmbeddedDataSpecifications() != null
                    && x.getEmbeddedDataSpecifications().stream()
                            .anyMatch(y -> y.getDataSpecification() != null && y.getDataSpecification().equals(dataSpecification));
            if (conceptDescriptions == null) {
                conceptDescriptions = this.aasEnvironment.getConceptDescriptions().stream()
                        .filter(filter)
                        .collect(Collectors.toList());
            }
            else {
                conceptDescriptions = conceptDescriptions.stream().filter(filter).collect(Collectors.toList());
            }
        }

        if (empty(idShort) && isCaseOf == null && dataSpecification == null) {
            conceptDescriptions = this.aasEnvironment.getConceptDescriptions();
        }

        Class conceptDescriptionClass = conceptDescriptions != null && conceptDescriptions.size() > 0 ? conceptDescriptions.get(0).getClass() : ConceptDescription.class;
        List<ConceptDescription> deepCopiedConceptDescriptions = deepCopy(conceptDescriptions, conceptDescriptionClass);
        return deepCopiedConceptDescriptions;
    }


    public void remove(Identifier id) {
        if (id == null || this.aasEnvironment == null) {
            return;
        }

        Predicate<Identifiable> removeFilter = x -> !x.getIdentification().getIdentifier().equalsIgnoreCase(id.getIdentifier());

        Identifiable identifiable = getIdentifiableById(id);
        if (identifiable == null) {
            return;
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
            //TODO: Remove belonging AssetInformation of AAS?
        }
    }


    public Identifiable put(Reference parent, Identifiable identifiable) {
        if (parent == null || identifiable == null || this.aasEnvironment == null) {
            return null;
        }
        AssetAdministrationShell parentAAS = null;
        Reference referenceOfIdentifiable = AasUtils.toReference(identifiable);

        if (parent.getKeys() != null
                && parent.getKeys().size() > 0) {
            KeyElements lastKeyElementOfReference = parent.getKeys().get(parent.getKeys().size() - 1).getType();
            Class clazz = AasUtils.keyTypeToClass(lastKeyElementOfReference);
            Identifiable parentIdentifiable = (Identifiable) AasUtils.resolve(parent, this.aasEnvironment, clazz);
            if (parentIdentifiable != null && AssetAdministrationShell.class.isAssignableFrom(parentIdentifiable.getClass())) {
                parentAAS = (AssetAdministrationShell) parentIdentifiable;
            }
        }

        if (Submodel.class.isAssignableFrom(identifiable.getClass())) {
            this.aasEnvironment.setSubmodels(
                    Util.updateIdentifiableList(Submodel.class, this.aasEnvironment.getSubmodels(), identifiable));

            if (parentAAS != null && referenceOfIdentifiable != null) {
                parentAAS.getSubmodels().remove(referenceOfIdentifiable);
                parentAAS.getSubmodels().add(referenceOfIdentifiable);
            }
            return identifiable;
        }
        else if (AssetAdministrationShell.class.isAssignableFrom(identifiable.getClass())) {
            this.aasEnvironment.setAssetAdministrationShells(
                    Util.updateIdentifiableList(AssetAdministrationShell.class, this.aasEnvironment.getAssetAdministrationShells(), identifiable));
            return identifiable;
        }
        else if (ConceptDescription.class.isAssignableFrom(identifiable.getClass())) {
            this.aasEnvironment.setConceptDescriptions(
                    Util.updateIdentifiableList(ConceptDescription.class, this.aasEnvironment.getConceptDescriptions(), identifiable));
            return identifiable;
        }
        else if (Asset.class.isAssignableFrom(identifiable.getClass())) {
            this.aasEnvironment.setAssets(
                    Util.updateIdentifiableList(Asset.class, this.aasEnvironment.getAssets(), identifiable));
            //TODO: Add belonging AssetInformation to AAS?
            return identifiable;
        }
        return null;
    }

}
