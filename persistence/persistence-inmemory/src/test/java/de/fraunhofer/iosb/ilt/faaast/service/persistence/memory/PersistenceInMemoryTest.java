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

import de.fraunhofer.iosb.ilt.faaast.service.model.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Extend;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Level;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import io.adminshell.aas.v3.dataformat.core.AASFull;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
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
import java.util.ArrayList;
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
    public void getSubmodelElementTest() {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell";
        String SUBMODEL_IDENTIFIER = "http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial";
        String SUBMODEL_ELEMENT_IDSHORT = "ExampleEntity2";
        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_IDSHORT, KeyElements.ENTITY);
        SubmodelElement submodelElement = persistence.get(reference, new QueryModifier());
        SubmodelElement submodelElementExpected = environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_IDSHORT))
                .findFirst().get();
        Assert.assertEquals(submodelElementExpected, submodelElement);
    }


    @Test
    public void getSubmodelElementTestWithBlob() {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";
        String SUBMODEL_ELEMENT_IDSHORT = "ExampleBlob";
        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT, SUBMODEL_ELEMENT_IDSHORT, KeyElements.BLOB);
        QueryModifier queryModifier = new QueryModifier();
        queryModifier.setExtend(Extend.WithBLOBValue);

        SubmodelElement submodelElement = persistence.get(reference, queryModifier);
        SubmodelElement submodelElementExpected = ((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_COLLECTION_IDSHORT))
                .findFirst().get())
                        .getValues().stream().filter(x -> x.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_IDSHORT)).findFirst().get();
        Assert.assertEquals(submodelElementExpected, submodelElement);
    }


    @Test
    public void getSubmodelElementTestWithOutBlob() {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";
        String SUBMODEL_ELEMENT_IDSHORT = "ExampleBlob";
        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT, SUBMODEL_ELEMENT_IDSHORT, KeyElements.BLOB);
        SubmodelElement submodelElement = persistence.get(reference, new QueryModifier());
        Assert.assertEquals(null, submodelElement);
    }


    @Test
    public void getIdentifiableAASTest() {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        Identifier id = new DefaultIdentifier.Builder()
                .identifier(AAS_IDENTIFIER)
                .idType(IdentifierType.IRI)
                .build();
        Identifiable identifiable = persistence.get(id, new QueryModifier());
        Assert.assertTrue(AssetAdministrationShell.class.isAssignableFrom(identifiable.getClass()));
        Assert.assertEquals(environment.getAssetAdministrationShells().stream().filter(x -> x.getIdentification().equals(id)).findFirst().get(), identifiable);
    }


    @Test
    public void getIdentifiableSubmodelTest() {
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        Identifier id = new DefaultIdentifier.Builder()
                .identifier(SUBMODEL_IDENTIFIER)
                .idType(IdentifierType.IRI)
                .build();
        Identifiable identifiable = persistence.get(id, new QueryModifier());
        Assert.assertTrue(Submodel.class.isAssignableFrom(identifiable.getClass()));
        Assert.assertEquals(environment.getSubmodels().stream().filter(
                x -> x.getIdentification().equals(id)).findFirst().get(), identifiable);
    }


    @Test
    public void getIdentifiableConceptDescriptionTest() {
        Identifier id = new DefaultIdentifier.Builder()
                .idType(IdentifierType.IRI)
                .identifier("https://acplt.org/Test_ConceptDescription")
                .build();
        Identifiable identifiable = persistence.get(id, new QueryModifier());
        Assert.assertTrue(ConceptDescription.class.isAssignableFrom(identifiable.getClass()));
        Assert.assertEquals(environment.getConceptDescriptions().stream().filter(
                x -> x.getIdentification().equals(id)).findFirst().get(), identifiable);

    }


    @Test
    public void getShellsNullTest() {
        String AAS_IDSHORT = "Test_AssetAdministrationShell_Mandatory";
        List<AssetAdministrationShell> shellList = persistence.get(AAS_IDSHORT, new GlobalAssetIdentification(), new QueryModifier());
        Assert.assertEquals(null, shellList);
    }


    @Test
    public void getShellsAllTest() {
        List<AssetAdministrationShell> shellList = persistence.get("", (AssetIdentification) null, new QueryModifier());
        Assert.assertEquals(environment.getAssetAdministrationShells(), shellList);

        Assert.assertEquals(this.environment.getAssetAdministrationShells().get(0), shellList.get(0));
        shellList.get(0).setIdShort("Test");
        Assert.assertNotEquals(this.environment.getAssetAdministrationShells().get(0), shellList.get(0));

        shellList = persistence.get(null, (AssetIdentification) null, new QueryModifier());
        Assert.assertEquals(environment.getAssetAdministrationShells(), shellList);
    }


    @Test
    public void getShellsWithIdShortTest() {
        String AAS_IDSHORT = "Test_AssetAdministrationShell_Mandatory";
        List<AssetAdministrationShell> shellList = persistence.get(AAS_IDSHORT, (AssetIdentification) null, new QueryModifier());
        Assert.assertEquals(environment.getAssetAdministrationShells().stream().filter(
                x -> x.getIdShort().equalsIgnoreCase(AAS_IDSHORT)).collect(Collectors.toList()), shellList);
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
        List<AssetAdministrationShell> shellList = persistence.get(null, globalAssetIdentification, new QueryModifier());
        Assert.assertEquals(environment.getAssetAdministrationShells().stream().filter(
                x -> x.getAssetInformation().getGlobalAssetId().equals(globalAssetIdentification.getReference())).collect(Collectors.toList()), shellList);
    }


    @Test
    public void getSubmodelsNullTest() {
        String SUBMODEL_IDSHORT = "Test_AssetAdministrationShell_Mandatory";
        List<AssetAdministrationShell> submodelList = persistence.get(SUBMODEL_IDSHORT, new DefaultReference(), new QueryModifier());
        Assert.assertEquals(null, submodelList);
    }


    @Test
    public void getSubmodelsAllTest() {
        List<Submodel> submodelList = persistence.get(null, (Reference) null, new QueryModifier());
        Assert.assertEquals(this.environment.getSubmodels(), submodelList);

        Assert.assertEquals(this.environment.getSubmodels().get(0), submodelList.get(0));
        submodelList.get(0).setIdShort("Test");
        Assert.assertNotEquals(this.environment.getSubmodels().get(0), submodelList.get(0));

        submodelList = persistence.get(null, (Reference) null, new QueryModifier());
        Assert.assertEquals(this.environment.getSubmodels(), submodelList);

    }


    @Test
    public void getSubmodelsWithIdShortTest() {
        String SUBMODEL_IDSHORT = "TestSubmodel";
        List<Submodel> submodelList = persistence.get(SUBMODEL_IDSHORT, (Reference) null, new QueryModifier());
        Assert.assertEquals(this.environment.getSubmodels().stream().filter(
                x -> x.getIdShort().equalsIgnoreCase(SUBMODEL_IDSHORT)).collect(Collectors.toList()), submodelList);
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
        List<Submodel> submodelList = persistence.get("", semanticId, new QueryModifier());
        Assert.assertEquals(this.environment.getSubmodels().stream().filter(
                x -> x.getSemanticId() != null && x.getSemanticId().equals(semanticId)).collect(Collectors.toList()), submodelList);
    }


    @Test
    public void getSubmodelElementsTest() {
        String AAS_IDSHORT = "TestAssetAdministrationShell";
        String SUBMODEL_IRI = "http://acplt.org/Submodels/Assets/TestAsset/Identification";
        Reference submodelReference = Utils.createReference(AAS_IDSHORT, SUBMODEL_IRI);
        List<SubmodelElement> submodelElements = persistence.getSubmodelElements(submodelReference, null, new QueryModifier());
        Assert.assertEquals(this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IRI))
                .findFirst().get()
                .getSubmodelElements(), submodelElements);
    }


    @Test
    public void getSubmodelElementsWithSemanticIdTest() {
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
        List<SubmodelElement> submodelElements = persistence.getSubmodelElements(submodelReference, semanticIdReference, new QueryModifier());
        Assert.assertEquals(this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IRI))
                .findFirst().get()
                .getSubmodelElements().stream().filter(x -> x.getSemanticId().equals(semanticIdReference)).collect(Collectors.toList()), submodelElements);
    }


    @Test
    public void getSubmodelElementsFromSubmodelElementCollectionTest() {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";
        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT, KeyElements.SUBMODEL_ELEMENT_COLLECTION);

        List<SubmodelElement> submodelElements = persistence.getSubmodelElements(reference, null, new QueryModifier());
        Assert.assertEquals(new ArrayList<>(((SubmodelElementCollection) this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get()
                .getSubmodelElements().stream().filter(x -> x.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_COLLECTION_IDSHORT)).findFirst().get()).getValues()),
                submodelElements);

        Reference semanticIdReference = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .type(KeyElements.GLOBAL_REFERENCE)
                        .value("0173-1#02-AAO677#002")
                        .idType(KeyType.IRI)
                        .build())
                .build();
        submodelElements = persistence.getSubmodelElements(reference, semanticIdReference, new QueryModifier());

        Assert.assertEquals(new ArrayList<>(), submodelElements);
    }


    @Test
    public void getConceptDescriptionsAllTest() {
        List<ConceptDescription> conceptDescriptions = this.persistence.get(null, null, (Reference) null, new QueryModifier());
        Assert.assertEquals(this.environment.getConceptDescriptions(), conceptDescriptions);
    }


    @Test
    public void getConceptDescriptionsWithIdShortTest() {
        String conceptDescriptionIdShort = "TestConceptDescription";
        List<ConceptDescription> conceptDescriptions = this.persistence.get(conceptDescriptionIdShort, null, (Reference) null, new QueryModifier());
        Assert.assertEquals(this.environment.getConceptDescriptions().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(conceptDescriptionIdShort))
                .collect(Collectors.toList()),
                conceptDescriptions);
    }


    @Test
    public void getConceptDescriptionsWithIsCaseOfTest() {
        String conceptDescriptionIdShort = "TestConceptDescription";
        Reference isCaseOf = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .type(KeyElements.GLOBAL_REFERENCE)
                        .idType(KeyType.IRI)
                        .value("http://acplt.org/DataSpecifications/ConceptDescriptions/TestConceptDescription")
                        .build())
                .build();
        List<ConceptDescription> conceptDescriptions = this.persistence.get(null, isCaseOf, (Reference) null, new QueryModifier());
        Assert.assertEquals(this.environment.getConceptDescriptions().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(conceptDescriptionIdShort))
                .collect(Collectors.toList()),
                conceptDescriptions);
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

        List<ConceptDescription> conceptDescriptions = this.persistence.get(null, null, dataSpecification, new QueryModifier());

        Assert.assertEquals(this.environment.getConceptDescriptions().stream()
                .filter(x -> x.getEmbeddedDataSpecifications() != null
                        && x.getEmbeddedDataSpecifications().stream()
                                .anyMatch(y -> y.getDataSpecification() != null && y.getDataSpecification().equals(dataSpecification)))
                .collect(Collectors.toList()),
                conceptDescriptions);
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
        List<ConceptDescription> conceptDescriptions = this.persistence.get(conceptDescriptionIdShort, isCaseOf, null, new QueryModifier());

        Assert.assertEquals(this.environment.getConceptDescriptions().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(conceptDescriptionIdShort))
                .collect(Collectors.toList()),
                conceptDescriptions);
    }


    @Test
    public void putSubmodelElementNewInSubmodelTest() {
        SubmodelElement newSubmodelElement = Util.deepCopy(this.environment.getSubmodels().get(0).getSubmodelElements().get(0),
                this.environment.getSubmodels().get(0).getSubmodelElements().get(0).getClass());
        String idShort = "NewIdShort";
        newSubmodelElement.setIdShort(idShort);

        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        Reference parent = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER);

        Assert.assertEquals(this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get().getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(idShort))
                .findFirst().orElse(null), null);

        this.persistence.put(parent, newSubmodelElement);

        Assert.assertEquals(this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get().getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(idShort))
                .findFirst().orElse(null), newSubmodelElement);
    }


    @Test
    public void putSubmodelElementChangeInSubmodelTest() {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";

        SubmodelElement submodelElement = this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get().getSubmodelElements().get(0);
        SubmodelElement changedSubmodelElement = Util.deepCopy(submodelElement, submodelElement.getClass());
        String category = "NewCategory";
        changedSubmodelElement.setCategory(category);

        Reference parent = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER);

        this.persistence.put(parent, changedSubmodelElement);

        Assert.assertEquals(this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get().getSubmodelElements().stream().filter(y -> y.getIdShort().equalsIgnoreCase(submodelElement.getIdShort())).findFirst().get().getCategory(),
                category);
    }


    @Test
    public void putSubmodelElementNewInSubmodelElementCollectionTest() {
        SubmodelElement newSubmodelElement = Util.deepCopy(this.environment.getSubmodels().get(0).getSubmodelElements().get(0),
                this.environment.getSubmodels().get(0).getSubmodelElements().get(0).getClass());
        String idShort = "NewIdShort";
        newSubmodelElement.setIdShort(idShort);

        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";
        Reference parent = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT, KeyElements.SUBMODEL_ELEMENT_COLLECTION);

        Assert.assertEquals(((SubmodelElementCollection) this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get().getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_COLLECTION_IDSHORT))
                .findFirst().orElse(null))
                        .getValues().stream()
                        .filter(x -> x.getIdShort().equalsIgnoreCase(idShort)).findFirst().orElse(null),
                null);

        this.persistence.put(parent, newSubmodelElement);

        Assert.assertEquals(((SubmodelElementCollection) this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get().getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_COLLECTION_IDSHORT))
                .findFirst().orElse(null))
                        .getValues().stream()
                        .filter(x -> x.getIdShort().equalsIgnoreCase(idShort)).findFirst().orElse(null),
                newSubmodelElement);
    }


    @Test
    public void putSubmodelElementChangeInSubmodelElementCollectionTest() {
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

        Reference parent = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT, KeyElements.SUBMODEL_ELEMENT_COLLECTION);
        this.persistence.put(parent, changedSubmodelElement);

        Assert.assertEquals(((SubmodelElementCollection) this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get().getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_COLLECTION_IDSHORT))
                .findFirst().orElse(null))
                        .getValues().stream().filter(z -> z.getIdShort().equalsIgnoreCase(submodelElement.getIdShort())).findFirst().get().getCategory(),
                category);
    }


    @Test
    public void removeTest() {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";

        Identifier aasId = new DefaultIdentifier.Builder()
                .idType(IdentifierType.IRI)
                .identifier(AAS_IDENTIFIER)
                .build();

        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        Identifier submodelId = new DefaultIdentifier.Builder()
                .idType(IdentifierType.IRI)
                .identifier(SUBMODEL_IDENTIFIER)
                .build();

        Identifiable submodel = this.persistence.get(submodelId, new QueryModifier());
        Reference submodelReference = AasUtils.toReference(submodel);

        Assert.assertEquals(submodelReference,
                ((AssetAdministrationShell) this.persistence.get(aasId, new QueryModifier())).getSubmodels().stream().filter(x -> x.equals(submodelReference)).findFirst().get());
        Assert.assertEquals(this.environment.getSubmodels().stream().filter(x -> x.getIdentification().equals(submodelId)).findFirst().get(),
                this.persistence.get(submodelId, new QueryModifier()));
        this.persistence.remove(submodelId);
        Assert.assertEquals(null, this.persistence.get(submodelId, new QueryModifier()));
        Assert.assertEquals(null, ((AssetAdministrationShell) this.persistence.get(aasId, new QueryModifier())).getSubmodels().stream().filter(x -> x.equals(submodelReference))
                .findFirst().orElse(null));

        Assert.assertEquals(this.environment.getAssetAdministrationShells().stream().filter(x -> x.getIdentification().equals(aasId)).findFirst().get(),
                this.persistence.get(aasId, new QueryModifier()));
        this.persistence.remove(aasId);
        Assert.assertEquals(null, this.persistence.get(aasId, new QueryModifier()));
    }


    @Test
    public void removeByReferenceTest() {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";
        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT, KeyElements.SUBMODEL_ELEMENT_COLLECTION);

        QueryModifier queryModifier = new QueryModifier();
        queryModifier.setExtend(Extend.WithBLOBValue);
        Assert.assertEquals(AasUtils.resolve(reference, this.environment), this.persistence.get(reference, queryModifier));

        this.persistence.remove(reference);

        Assert.assertEquals(null, this.persistence.getEnvironment().getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get().getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_COLLECTION_IDSHORT))
                .findFirst().orElse(null));

    }


    @Test
    public void removeByReferencePropertyInSubmodelElementCollectionTest() {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";
        String SUBMODEL_ELEMENT_IDSHORT = "ExampleFile";
        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT, SUBMODEL_ELEMENT_IDSHORT, KeyElements.FILE);

        Assert.assertEquals(AasUtils.resolve(reference, this.environment), this.persistence.get(reference, new QueryModifier()));

        this.persistence.remove(reference);

        Assert.assertEquals(null, ((SubmodelElementCollection) this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get().getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_COLLECTION_IDSHORT))
                .findFirst().orElse(null))
                        .getValues().stream().filter(z -> z.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_IDSHORT)).findFirst().orElse(null));
    }


    @Test
    public void removeByReferencePropertyTest() {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell";
        String SUBMODEL_IDENTIFIER = "http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial";
        String SUBMODEL_ELEMENT_IDSHORT = "ExampleEntity2";
        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_IDSHORT, KeyElements.ENTITY);

        Assert.assertEquals(AasUtils.resolve(reference, this.environment), this.persistence.get(reference, new QueryModifier()));

        this.persistence.remove(reference);

        Assert.assertEquals(null, this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(SUBMODEL_IDENTIFIER))
                .findFirst().get().getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_IDSHORT))
                .findFirst().orElse(null));
    }


    @Test
    public void putIdentifiableNewTest() {
        Submodel newSubmodel = Util.deepCopy(this.environment.getSubmodels().get(0),
                this.environment.getSubmodels().get(0).getClass());
        String idShort = "NewIdShort";
        newSubmodel.setIdShort(idShort);
        Identifier newIdentifier = new DefaultIdentifier.Builder()
                .identifier("http://newIdentifier.org")
                .idType(IdentifierType.IRI).build();
        newSubmodel.setIdentification(newIdentifier);

        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        Reference parent = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .idType(KeyType.IRI)
                        .type(KeyElements.ASSET_ADMINISTRATION_SHELL)
                        .value(AAS_IDENTIFIER)
                        .build())
                .build();
        Identifier parentIdentifier = new DefaultIdentifier.Builder()
                .identifier(AAS_IDENTIFIER)
                .idType(IdentifierType.IRI).build();

        Assert.assertEquals(null, this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().equals(newIdentifier))
                .findFirst().orElse(null));

        this.persistence.put((Identifiable) newSubmodel);

        Assert.assertEquals(newSubmodel, this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().equals(newIdentifier))
                .findFirst().orElse(null));

        newIdentifier.setIdentifier("http://newIdentifier_2.org");
        newSubmodel.setIdentification(newIdentifier);
        this.persistence.put((Identifiable) newSubmodel);

        AssetAdministrationShell shell = (AssetAdministrationShell) this.persistence.get(parentIdentifier, new QueryModifier());
        Assert.assertFalse(shell.getSubmodels().stream().anyMatch(x -> x.getKeys().stream().anyMatch(y -> y.getValue().equalsIgnoreCase(newIdentifier.getIdentifier()))));

        Assert.assertEquals(newSubmodel, this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().equals(newIdentifier))
                .findFirst().orElse(null));

    }


    @Test
    public void putIdentifiableChangeTest() {
        ConceptDescription conceptDescription = Util.deepCopy(this.environment.getConceptDescriptions().get(0),
                this.environment.getConceptDescriptions().get(0).getClass());
        String category = "NewCategory";
        conceptDescription.setCategory(category);

        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        Reference parent = new DefaultReference.Builder()
                .key(new DefaultKey.Builder()
                        .idType(KeyType.IRI)
                        .type(KeyElements.ASSET_ADMINISTRATION_SHELL)
                        .value(AAS_IDENTIFIER)
                        .build())
                .build();

        this.persistence.put((Identifiable) conceptDescription);

        Assert.assertEquals(category, this.persistence.get(conceptDescription.getIdentification(), new QueryModifier()).getCategory());

    }


    @Test
    public void testQueryModifierExtend() {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";
        String SUBMODEL_ELEMENT_IDSHORT = "ExampleBlob";

        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER, SUBMODEL_ELEMENT_COLLECTION_IDSHORT, SUBMODEL_ELEMENT_IDSHORT, KeyElements.BLOB);

        QueryModifier queryModifier = new QueryModifier();
        queryModifier.setExtend(Extend.WithBLOBValue);

        Identifier submodelId = new DefaultIdentifier.Builder()
                .idType(IdentifierType.IRI)
                .identifier(SUBMODEL_IDENTIFIER)
                .build();

        Assert.assertEquals(((SubmodelElementCollection) this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().equals(submodelId)).findFirst().get()
                .getSubmodelElements().stream().filter(y -> y.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_COLLECTION_IDSHORT)).findFirst().get())
                        .getValues().stream().filter(z -> z.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_IDSHORT)).findFirst().get(),
                this.persistence.get(reference, queryModifier));

        queryModifier.setExtend(Extend.WithoutBLOBValue);
        Assert.assertEquals(null, this.persistence.get(reference, queryModifier));
    }


    @Test
    public void testQueryModifierLevel() {
        String AAS_IDENTIFIER = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String SUBMODEL_IDENTIFIER = "https://acplt.org/Test_Submodel_Mandatory";
        String SUBMODEL_ELEMENT_COLLECTION_IDSHORT = "ExampleSubmodelCollectionUnordered";
        String SUBMODEL_ELEMENT_IDSHORT = "ExampleBlob";

        Reference reference = Utils.createReference(AAS_IDENTIFIER, SUBMODEL_IDENTIFIER);

        QueryModifier queryModifier = new QueryModifier();
        queryModifier.setLevel(Level.Deep);

        Identifier submodelId = new DefaultIdentifier.Builder()
                .idType(IdentifierType.IRI)
                .identifier(SUBMODEL_IDENTIFIER)
                .build();

        Assert.assertEquals(this.environment.getSubmodels().stream()
                .filter(x -> x.getIdentification().equals(submodelId)).findFirst().get(), this.persistence.get(submodelId, queryModifier));

        queryModifier.setLevel(Level.Core);
        Submodel submodel = (Submodel) this.persistence.get(submodelId, queryModifier);
        Assert.assertEquals(null, ((SubmodelElementCollection) submodel.getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(SUBMODEL_ELEMENT_COLLECTION_IDSHORT)).findFirst().get()).getValues());
    }

}
