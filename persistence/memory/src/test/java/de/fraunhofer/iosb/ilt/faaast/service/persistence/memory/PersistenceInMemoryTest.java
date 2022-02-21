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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.memory;

import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extend;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.ExecutionState;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultIdentifier;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class PersistenceInMemoryTest {

    private AssetAdministrationShellEnvironment environment;
    private Persistence persistence;

    @Before
    public void init() {
        this.environment = AASFull.createEnvironment();
        this.persistence = new PersistenceInMemory();
        this.persistence.setEnvironment(this.environment);
    }


    @Test
    public void getEnvironmentTest() {
        Assert.assertEquals(this.environment, persistence.getEnvironment());
    }


    @Test
    public void getSubmodelElementTest() throws ResourceNotFoundException {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell";
        String SUBMODEL_IDENTIFIER = "http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial";
        String SUBMODEL_ELEMENT_IDSHORT = "ExampleEntity2";
        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_IDSHORT);
        SubmodelElement actualSubmodelElement = persistence.get(reference, new QueryModifier());
        SubmodelElement expectedSubmodelElement = environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_IDSHORT))
                .findFirst().get();
        Assert.assertEquals(expectedSubmodelElement, actualSubmodelElement);
    }


    @Test
    public void getSubmodelElementTestWithBlob() throws ResourceNotFoundException {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";
        String SUBMODEL_ELEMENT_IDSHORT = "ExampleBlob";
        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT, SUBMODEL_ELEMENT_IDSHORT);
        QueryModifier queryModifier = new QueryModifier.Builder().extend(Extend.WithBLOBValue).build();

        SubmodelElement actualSubmodelElement = persistence.get(reference, queryModifier);
        SubmodelElement expectedSubmodelElementExpected = ((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_COLLECTION_IDSHORT))
                .findFirst().get())
                        .getValues().stream().filter(x -> x.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_IDSHORT)).findFirst().get();
        Assert.assertEquals(expectedSubmodelElementExpected, actualSubmodelElement);
    }


    @Test
    public void getSubmodelElementTestWithOutBlob() throws ResourceNotFoundException {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";
        String SUBMODEL_ELEMENT_IDSHORT = "ExampleBlob";
        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT, SUBMODEL_ELEMENT_IDSHORT);
        SubmodelElement submodelElement = persistence.get(reference, new QueryModifier());
        Assert.assertEquals(null, submodelElement);
    }


    @Test
    public void getIdentifiableAASTest() throws ResourceNotFoundException {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        Identifier id = new DefaultIdentifier.Builder()
                .identifier(AAS_IDENTIFIER)
                .idType(IdentifierType.IRI)
                .build();
        AssetAdministrationShell actual = (AssetAdministrationShell) persistence.get(id, new QueryModifier());
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().stream().filter(x -> x.getIdentification().equals(id)).findFirst().get();

        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getIdentifiableSubmodelTest() throws ResourceNotFoundException {
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        Identifier id = new DefaultIdentifier.Builder()
                .identifier(SUBMODEL_IDENTIFIER)
                .idType(IdentifierType.IRI)
                .build();
        Submodel actual = (Submodel) persistence.get(id, new QueryModifier());
        Submodel expected = environment.getSubmodels().stream().filter(x -> x.getIdentification().equals(id)).findFirst().get();

        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getIdentifiableConceptDescriptionTest() throws ResourceNotFoundException {
        Identifier id = new DefaultIdentifier.Builder()
                .idType(IdentifierType.IRI)
                .identifier("https://acplt.org/Test_ConceptDescription")
                .build();
        ConceptDescription actualConceptDescription = (ConceptDescription) persistence.get(id, new QueryModifier());
        ConceptDescription expectedConceptDescription = environment.getConceptDescriptions().stream().filter(
                x -> x.getIdentification().equals(id)).findFirst().get();

        Assert.assertEquals(expectedConceptDescription, actualConceptDescription);

    }


    @Test
    public void getShellsNullTest() {
        String AAS_IDSHORT = "Test_AssetAdministrationShell_Mandatory";
        List<AssetAdministrationShell> actualAASList = persistence.get(AAS_IDSHORT, List.of(new GlobalAssetIdentification()), new QueryModifier());
        List<AssetAdministrationShell> expectedAASList = null;
        Assert.assertEquals(expectedAASList, actualAASList);
    }


    @Test
    public void getShellsAllTest() {
        List<AssetAdministrationShell> actualAASList = persistence.get("", (List<AssetIdentification>) null, new QueryModifier());
        List<AssetAdministrationShell> expectedAASList = environment.getAssetAdministrationShells();
        Assert.assertEquals(expectedAASList, actualAASList);
    }


    @Test
    public void getShellsWithIdShortTest() {
        String AAS_IDSHORT = "Test_AssetAdministrationShell_Mandatory";
        List<AssetAdministrationShell> actualAASList = persistence.get(AAS_IDSHORT, (List<AssetIdentification>) null, new QueryModifier());
        List<AssetAdministrationShell> expectedAASList = environment.getAssetAdministrationShells().stream().filter(
                x -> x.getIdShort().equalsIgnoreCase(AAS_IDSHORT)).collect(Collectors.toList());
        Assert.assertEquals(expectedAASList, actualAASList);
    }


    @Test
    public void getShellsWithGlobalAssetIdentificationTest() {
        GlobalAssetIdentification globalAssetIdentification = new GlobalAssetIdentification();
        globalAssetIdentification.setReference(new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .type(KeyElements.ASSET)
                        .idType(KeyType.IRI)
                        .value("https://acplt.org/Test_Asset_Mandatory")
                        .build())
                .build());
        List<AssetAdministrationShell> actualAASList = persistence.get(null, List.of(globalAssetIdentification), new QueryModifier());
        List<AssetAdministrationShell> expectedAASList = environment.getAssetAdministrationShells().stream().filter(
                x -> x.getAssetInformation().getGlobalAssetId().equals(globalAssetIdentification.getReference())).collect(Collectors.toList());
        Assert.assertEquals(expectedAASList, actualAASList);
    }


    @Test
    public void getSubmodelsNullTest() {
        String AAS_ID = "Test_AssetAdministrationShell_Mandatory";
        List<AssetAdministrationShell> actualAASList = persistence.get(AAS_ID, new DefaultReference(), new QueryModifier());
        List<AssetAdministrationShell> expectedAASList = null;
        Assert.assertEquals(expectedAASList, actualAASList);
    }


    @Test
    public void getSubmodelsAllTest() {
        List<Submodel> actualSubmodelList = persistence.get(null, (Reference) null, new QueryModifier());
        List<Submodel> expectedSubmodelList = this.environment.getSubmodels();
        Assert.assertEquals(expectedSubmodelList, actualSubmodelList);
    }


    @Test
    public void getSubmodelsWithIdShortTest() {
        String SUBMODEL_IDSHORT = "TestSubmodel";
        List<Submodel> actualSubmodels = persistence.get(SUBMODEL_IDSHORT, (Reference) null, new QueryModifier());
        List<Submodel> expectedSubmodels = this.environment.getSubmodels().stream().filter(
                x -> x.getIdShort().equalsIgnoreCase(SUBMODEL_IDSHORT)).collect(Collectors.toList());
        Assert.assertEquals(expectedSubmodels, actualSubmodels);
    }


    @Test
    public void getSubmodelsWithSemanticIdTest() {
        Reference semanticId = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .type(KeyElements.GLOBAL_REFERENCE)
                        .idType(KeyType.IRI)
                        .value("http://acplt.org/SubmodelTemplates/ExampleSubmodel")
                        .build())
                .build();
        List<Submodel> actualSubmodels = persistence.get("", semanticId, new QueryModifier());
        List<Submodel> expectedSubmodels = this.environment.getSubmodels().stream().filter(
                x -> x.getSemanticId() != null && x.getSemanticId().equals(semanticId)).collect(Collectors.toList());
        Assert.assertEquals(expectedSubmodels, actualSubmodels);
    }


    @Test
    public void getSubmodelElementsTest() throws ResourceNotFoundException {
        String AAS_IDSHORT = "TestAssetAdministrationShell";
        String SUBMODEL_IRI = "http://acplt.org/Submodels/Assets/TestAsset/Identification";
        Reference submodelReference = Utils.createReference(AAS_IDSHORT, SUBMODEL_IRI);
        List<SubmodelElement> actualSubmodelElements = persistence.getSubmodelElements(submodelReference, null, new QueryModifier());
        List<SubmodelElement> expectedSubmodelElements = this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IRI))
                .findFirst().get()
                .getSubmodelElements();
        Assert.assertEquals(expectedSubmodelElements, actualSubmodelElements);
    }


    @Test
    public void getSubmodelElementsWithSemanticIdTest() throws ResourceNotFoundException {
        String AAS_IDSHORT = "TestAssetAdministrationShell";
        String SUBMODEL_IRI = "http://acplt.org/Submodels/Assets/TestAsset/Identification";
        Reference submodelReference = Utils.createReference(AAS_IDSHORT, SUBMODEL_IRI);
        Reference semanticIdReference = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .type(KeyElements.GLOBAL_REFERENCE)
                        .value("0173-1#02-AAO677#002")
                        .idType(KeyType.IRI)
                        .build())
                .build();
        List<SubmodelElement> actualSubmodelElements = persistence.getSubmodelElements(submodelReference, semanticIdReference, new QueryModifier());
        List<SubmodelElement> expectedSubmodelElements = this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IRI))
                .findFirst().get()
                .getSubmodelElements().stream().filter(x -> x.getSemanticId().equals(semanticIdReference)).collect(Collectors.toList());
        Assert.assertEquals(expectedSubmodelElements, actualSubmodelElements);
    }


    @Test
    public void getSubmodelElementsFromSubmodelElementCollectionTest() throws ResourceNotFoundException {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";
        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT);

        List<SubmodelElement> actualSubmodelElements = persistence.getSubmodelElements(reference, null, new QueryModifier());
        List<SubmodelElement> expectedSubmodelElements = (List<SubmodelElement>) ((SubmodelElementCollection) this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get()
                .getSubmodelElements().stream().filter(x -> x.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_COLLECTION_IDSHORT)).findFirst().get()).getValues();

        Assert.assertEquals(expectedSubmodelElements, actualSubmodelElements);
    }


    @Test
    public void getConceptDescriptionsAllTest() {
        List<ConceptDescription> conceptDescriptions = this.persistence.get(null, null, (Reference) null, new QueryModifier());
        List<ConceptDescription> expectedConceptDescriptions = this.environment.getConceptDescriptions();
        Assert.assertEquals(expectedConceptDescriptions, conceptDescriptions);
    }


    @Test
    public void getConceptDescriptionsWithIdShortTest() {
        String conceptDescriptionIdShort = "TestConceptDescription";
        List<ConceptDescription> actualConceptDescriptions = this.persistence.get(conceptDescriptionIdShort, null, (Reference) null, new QueryModifier());
        List<ConceptDescription> expectedConceptDescriptions = this.environment.getConceptDescriptions().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(conceptDescriptionIdShort))
                .collect(Collectors.toList());
        Assert.assertEquals(expectedConceptDescriptions,
                actualConceptDescriptions);
    }


    @Test
    public void getConceptDescriptionsWithIsCaseOfTest() {
        String conceptDescriptionIdShort = "TestConceptDescription";
        Reference isCaseOf = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .type(null)
                        .idType(KeyType.IRI)
                        .value("http://acplt.org/DataSpecifications/ConceptDescriptions/TestConceptDescription")
                        .build())
                .build();
        List<ConceptDescription> actualConceptDescriptions = this.persistence.get(null, isCaseOf, (Reference) null, new QueryModifier());
        List<ConceptDescription> expectedConceptDescriptions = this.environment.getConceptDescriptions().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(conceptDescriptionIdShort))
                .collect(Collectors.toList());

        Assert.assertEquals(actualConceptDescriptions, expectedConceptDescriptions);
    }


    @Test
    public void getConceptDescriptionsWithDataSpecificationTest() {
        Reference dataSpecification = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .type(KeyElements.GLOBAL_REFERENCE)
                        .idType(KeyType.IRI)
                        .value("http://acplt.org/ReferenceElements/DataSpecificationX")
                        .build())
                .build();

        List<ConceptDescription> actualConceptDescriptions = this.persistence.get(null, null, dataSpecification, new QueryModifier());
        List<ConceptDescription> expectedConceptDescriptions = this.environment.getConceptDescriptions().stream()
                .filter(x -> x.getEmbeddedDataSpecifications() != null
                        && x.getEmbeddedDataSpecifications().stream()
                                .anyMatch(y -> y.getDataSpecification() != null && y.getDataSpecification().equals(dataSpecification)))
                .collect(Collectors.toList());

        Assert.assertEquals(expectedConceptDescriptions, actualConceptDescriptions);
    }


    @Test
    public void getConceptDescriptionsWithCombination() {
        String conceptDescriptionIdShort = "TestConceptDescription";
        Reference isCaseOf = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .type(KeyElements.GLOBAL_REFERENCE)
                        .idType(KeyType.IRI)
                        .value("http://acplt.org/DataSpecifications/ConceptDescriptions/TestConceptDescription")
                        .build())
                .build();
        List<ConceptDescription> actualConceptDescriptions = this.persistence.get(conceptDescriptionIdShort, isCaseOf, null, new QueryModifier());
        List<ConceptDescription> expectedConceptDescriptions = this.environment.getConceptDescriptions().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(conceptDescriptionIdShort))
                .collect(Collectors.toList());

        Assert.assertEquals(expectedConceptDescriptions, actualConceptDescriptions);
    }


    @Test
    public void putSubmodelElementNewInSubmodelTest() throws ResourceNotFoundException {
        SubmodelElement newSubmodelElement = Util.deepCopy(this.environment.getSubmodels().get(0).getSubmodelElements().get(0),
                this.environment.getSubmodels().get(0).getSubmodelElements().get(0).getClass());
        String idShort = "NewIdShort";
        newSubmodelElement.setIdShort(idShort);

        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        Reference parent = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER);
        Reference submodelElementReference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, idShort);

        this.persistence.put(parent, null, newSubmodelElement);
        SubmodelElement actualSubmodelElement = this.persistence.get(submodelElementReference, new QueryModifier());

        Assert.assertEquals(newSubmodelElement, actualSubmodelElement);
    }


    @Test
    public void putSubmodelElementChangeInSubmodelTest() throws ResourceNotFoundException {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";

        SubmodelElement submodelElement = this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get().getSubmodelElements().get(0);
        SubmodelElement changedSubmodelElement = Util.deepCopy(submodelElement, submodelElement.getClass());
        String category = "NewCategory";
        changedSubmodelElement.setCategory(category);

        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, submodelElement.getIdShort());

        this.persistence.put(null, reference, changedSubmodelElement);
        SubmodelElement actualSubmodelElement = this.persistence.get(reference, new QueryModifier());

        Assert.assertEquals(changedSubmodelElement, actualSubmodelElement);
    }


    @Test
    public void putSubmodelElementNewInSubmodelElementCollectionTest() throws ResourceNotFoundException {
        SubmodelElement newSubmodelElement = Util.deepCopy(this.environment.getSubmodels().get(0).getSubmodelElements().get(0),
                this.environment.getSubmodels().get(0).getSubmodelElements().get(0).getClass());
        String idShort = "NewIdShort";
        newSubmodelElement.setIdShort(idShort);

        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";
        Reference parent = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT);

        Assert.assertEquals(((SubmodelElementCollection) this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get().getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_COLLECTION_IDSHORT))
                .findFirst().orElse(null))
                        .getValues().stream()
                        .filter(x -> x.getIdShort().equalsIgnoreCase(idShort)).findFirst().orElse(null),
                null);

        this.persistence.put(parent, null, newSubmodelElement);
        SubmodelElement actualSubmodelelement = this.persistence.get(
                Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT, idShort), new QueryModifier());

        Assert.assertEquals(newSubmodelElement, actualSubmodelelement);
    }


    @Test
    public void putSubmodelElementChangeInSubmodelElementCollectionTest() throws ResourceNotFoundException {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";

        SubmodelElement submodelElement = ((SubmodelElementCollection) this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get().getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_COLLECTION_IDSHORT))
                .findFirst().orElse(null))
                        .getValues().stream()
                        .findFirst().orElse(null);

        SubmodelElement changedSubmodelElement = Util.deepCopy(submodelElement, submodelElement.getClass());
        String category = "NewCategory";
        changedSubmodelElement.setCategory(category);

        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT, submodelElement.getIdShort());
        this.persistence.put(null, reference, changedSubmodelElement);
        SubmodelElement actualSubmodelelement = this.persistence.get(reference, new QueryModifier.Builder().extend(Extend.WithBLOBValue).build());

        Assert.assertEquals(changedSubmodelElement, actualSubmodelelement);
    }


    @Test
    public void removeSubmodelTest() throws ResourceNotFoundException {
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        Identifier submodelId = new DefaultIdentifier.Builder()
                .idType(IdentifierType.IRI)
                .identifier(SUBMODEL_IDENTIFIER)
                .build();

        Identifiable submodel = this.persistence.get(submodelId, new QueryModifier());
        this.persistence.remove(submodelId);

        Assert.assertThrows(ResourceNotFoundException.class, () -> this.persistence.get(submodelId, new QueryModifier()));
    }


    @Test
    public void removeAASTest() throws ResourceNotFoundException {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";

        Identifier aasId = new DefaultIdentifier.Builder()
                .idType(IdentifierType.IRI)
                .identifier(AAS_IDENTIFIER)
                .build();

        this.persistence.remove(aasId);
        Assert.assertThrows(ResourceNotFoundException.class, () -> this.persistence.get(aasId, new QueryModifier()));
    }


    @Test
    public void removeByReferenceTest() throws ResourceNotFoundException {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";
        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT);
        this.persistence.remove(reference);
        Assert.assertThrows(ResourceNotFoundException.class, () -> this.persistence.get(reference, new QueryModifier()));
    }


    @Test
    public void removeByReferencePropertyInSubmodelElementCollectionTest() throws ResourceNotFoundException {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";
        String SUBMODEL_ELEMENT_IDSHORT = "ExampleFile";
        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT, SUBMODEL_ELEMENT_IDSHORT);

        SubmodelElement submodelElement = this.persistence.get(reference, new OutputModifier());
        this.persistence.remove(reference);
        Assert.assertThrows(ResourceNotFoundException.class, () -> this.persistence.get(reference, new QueryModifier()));
    }


    @Test
    public void removeByReferencePropertyTest() throws ResourceNotFoundException {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell";
        String SUBMODEL_IDENTIFIER = "http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial";
        String SUBMODEL_ELEMENT_IDSHORT = "ExampleEntity2";
        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_IDSHORT);

        SubmodelElement submodelElement = this.persistence.get(reference, new OutputModifier());
        this.persistence.remove(reference);
        Assert.assertThrows(ResourceNotFoundException.class, () -> this.persistence.get(reference, new QueryModifier()));
    }


    @Test
    public void putIdentifiableNewTest() throws ResourceNotFoundException {
        Submodel newSubmodel = Util.deepCopy(this.environment.getSubmodels().get(0),
                this.environment.getSubmodels().get(0).getClass());
        String idShort = "NewIdShort";
        newSubmodel.setIdShort(idShort);
        Identifier newIdentifier = new DefaultIdentifier.Builder()
                .identifier("http://newIdentifier.org")
                .idType(IdentifierType.IRI).build();
        newSubmodel.setIdentification(newIdentifier);

        this.persistence.put(newSubmodel);
        Submodel expected = this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().equals(newIdentifier))
                .findFirst().orElse(null);

        Assert.assertEquals(expected, newSubmodel);
    }


    @Test
    public void putIdentifiableChangeTest() throws ResourceNotFoundException {
        ConceptDescription expected = Util.deepCopy(this.environment.getConceptDescriptions().get(0),
                this.environment.getConceptDescriptions().get(0).getClass());
        String category = "NewCategory";
        expected.setCategory(category);
        this.persistence.put(expected);

        ConceptDescription actual = (ConceptDescription) this.persistence.get(expected.getIdentification(), new QueryModifier());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testQueryModifierExtend() throws ResourceNotFoundException {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";
        String SUBMODEL_ELEMENT_IDSHORT = "ExampleBlob";

        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT, SUBMODEL_ELEMENT_IDSHORT);
        QueryModifier queryModifier = new QueryModifier.Builder().extend(Extend.WithBLOBValue).build();
        Identifier submodelId = new DefaultIdentifier.Builder()
                .idType(IdentifierType.IRI)
                .identifier(SUBMODEL_IDENTIFIER)
                .build();

        SubmodelElement actual = this.persistence.get(reference, queryModifier);
        SubmodelElement expected = ((SubmodelElementCollection) this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().equals(submodelId)).findFirst().get()
                .getSubmodelElements().stream().filter(y -> y.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_COLLECTION_IDSHORT)).findFirst().get())
                        .getValues().stream().filter(z -> z.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_IDSHORT)).findFirst().get();

        Assert.assertEquals(expected, actual);
        queryModifier = new QueryModifier.Builder().extend(Extend.WithoutBLOBValue).build();
        actual = this.persistence.get(reference, queryModifier);
        expected = null;

        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testQueryModifierLevel() throws ResourceNotFoundException {
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";

        QueryModifier queryModifier = new QueryModifier.Builder().level(Level.Deep).build();
        Identifier submodelId = new DefaultIdentifier.Builder()
                .idType(IdentifierType.IRI)
                .identifier(SUBMODEL_IDENTIFIER)
                .build();

        Submodel expected = this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().equals(submodelId)).findFirst().get();
        Submodel actual = (Submodel) this.persistence.get(submodelId, queryModifier);
        Assert.assertEquals(expected, actual);

        queryModifier = new QueryModifier.Builder().level(Level.Core).build();
        actual = (Submodel) this.persistence.get(submodelId, queryModifier);
        List<SubmodelElement> submodelElementCollections = actual.getSubmodelElements().stream().filter(x -> SubmodelElementCollection.class.isAssignableFrom(x.getClass()))
                .collect(Collectors.toList());
        Assert.assertTrue(submodelElementCollections.stream().allMatch(x -> ((SubmodelElementCollection) x).getValues() == null));
    }


    @Test
    public void testOperationHandle() {
        OperationResult expectedResult = new OperationResult.Builder()
                .requestId("Test")
                .executionState(ExecutionState.Initiated)
                .build();

        OperationHandle actualOperationHandle = this.persistence.putOperationContext(null, "Test", expectedResult);

        OperationHandle expectedOperationHandle = new OperationHandle.Builder()
                .handleId(actualOperationHandle.getHandleId())
                .requestId("Test")
                .build();

        Assert.assertEquals(expectedOperationHandle, actualOperationHandle);

    }


    @Test
    public void testUpdateOperationResult() {
        OperationResult expectedResult = new OperationResult.Builder()
                .requestId("Test")
                .executionState(ExecutionState.Initiated)
                .build();

        OperationHandle actualOperationHandle = this.persistence.putOperationContext(null, "Test", expectedResult);

        expectedResult.setExecutionState(ExecutionState.Completed);
        expectedResult.setExecutionResult(new Result.Builder()
                .message(new Message.Builder()
                        .code("test")
                        .build())
                .success(true)
                .build());
        this.persistence.putOperationContext(actualOperationHandle.getHandleId(), null, expectedResult);

        OperationResult actualResult = this.persistence.getOperationResult(actualOperationHandle.getHandleId());
        Assert.assertEquals(expectedResult, actualResult);

    }

}
