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

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Result;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.ExecutionState;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.AssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ExtendHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;


/**
 * A test class for a persistence implementation should inherit from this abstract class.This class provides basic tests
 * for all methods defined in {@link de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence}.
 *
 * @param <T> type of the persistence to test
 * @param <C> type of the config matching the persistence to test
 */
public abstract class AbstractPersistenceTest<T extends Persistence<C>, C extends PersistenceConfig<T>> {

    private static final String FULL_MODEL_FILENAME = "AASFull.json";
    private static final String MINIMAL_MODEL_FILENAME = "AASMinimal.json";
    private static final ServiceContext SERVICE_CONTEXT = Mockito.mock(ServiceContext.class);

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    private File fullModelFile;
    private File minimalModelFile;
    private Environment environment;
    private T persistence;

    /**
     * Gets an instance of the concrete persistence config to use.
     *
     * @param initialModelFile the initial model file to use
     * @param initialModel the initial model to use
     * @return the persistence configuration
     * @throws de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException if configuration fails
     */
    public abstract C getPersistenceConfig(File initialModelFile, Environment initialModel) throws ConfigurationException;


    @Before
    public void init() throws Exception {
        fullModelFile = copyResourceToTempDir(FULL_MODEL_FILENAME);
        minimalModelFile = copyResourceToTempDir(MINIMAL_MODEL_FILENAME);
        environment = AASFull.createEnvironment();
        persistence = getPersistenceConfig(null, environment).newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
    }


    private File copyResourceToTempDir(String resourceName) throws IOException {
        File result = tempDir.newFile(resourceName);
        try (InputStream inputStream = AbstractPersistenceTest.class.getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(inputStream, result.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return result;
    }


    @Test
    public void getEnvironmentTest() {
        Assert.assertEquals(environment, persistence.getEnvironment());
    }


    @Test
    public void withInitialModelFileTest() throws ConfigurationInitializationException, ResourceNotFoundException, ConfigurationException {
        PersistenceConfig<T> config = getPersistenceConfig(fullModelFile, null);
        persistence = config.newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        String id = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getId().equals(id))
                .findFirst().get();
        AssetAdministrationShell actual = persistence.get(id, QueryModifier.DEFAULT, AssetAdministrationShell.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void withInitialModelAndModelFileTest() throws ConfigurationInitializationException, ResourceNotFoundException, ConfigurationException {
        PersistenceConfig<T> config = getPersistenceConfig(minimalModelFile, environment);
        persistence = config.newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        String id = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getId().equals(id))
                .findFirst().get();
        AssetAdministrationShell actual = persistence.get(id, QueryModifier.DEFAULT, AssetAdministrationShell.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementTest() throws ResourceNotFoundException {
        String assId = "https://acplt.org/Test_AssetAdministrationShell";
        String submodelId = "http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial";
        String submodelElementIdShort = "ExampleEntity2";
        Reference reference = ReferenceHelper.build(
                assId,
                submodelId,
                submodelElementIdShort);
        SubmodelElement expected = environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(submodelElementIdShort))
                .findFirst().get();
        SubmodelElement actual = persistence.get(reference, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementWithSimilarIDShortTest() throws ResourceNotFoundException {
        String submodelId = "http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial";
        String submodelElementIdShort = "ExampleEntity2";
        Reference reference = ReferenceHelper.build(
                null,
                submodelId,
                submodelElementIdShort);
        ConceptDescription conceptDescription = new DefaultConceptDescription.Builder()
                .idShort(submodelElementIdShort)
                .id("http://example.org/CD")
                .category("Entity")
                .build();
        AssetAdministrationShell shell = new DefaultAssetAdministrationShell.Builder()
                .idShort(submodelElementIdShort)
                .id("http://example.org/Shell")
                .build();
        persistence.put(shell);
        persistence.put(conceptDescription);
        SubmodelElement expected = environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(submodelElementIdShort))
                .findFirst().get();
        SubmodelElement actual = persistence.get(reference, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementTestWithBlob() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionIdShort = "ExampleSubmodelElementCollection";
        String submodelElementIdShort = "ExampleBlob";
        Reference reference = ReferenceHelper.build(
                aasId,
                submodelId,
                submodelElementCollectionIdShort,
                submodelElementIdShort);
        QueryModifier queryModifier = new QueryModifier.Builder().extend(Extent.WITH_BLOB_VALUE).build();
        SubmodelElement expected = ((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(submodelElementCollectionIdShort))
                .findFirst().get())
                        .getValue().stream()
                        .filter(x -> x.getIdShort().equalsIgnoreCase(submodelElementIdShort))
                        .findFirst().get();
        SubmodelElement actual = persistence.get(reference, queryModifier);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementTestWithOutBlob() throws ResourceNotFoundException {
        Reference reference = new DefaultReference.Builder()
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.ASSET_ADMINISTRATION_SHELL)
                        .value("https://acplt.org/Test_AssetAdministrationShell_Mandatory")
                        .build())
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.SUBMODEL)
                        .value("https://acplt.org/Test_Submodel_Mandatory")
                        .build())
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.SUBMODEL_ELEMENT)
                        .value("ExampleSubmodelElementCollection")
                        .build())
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.BLOB)
                        .value("ExampleBlob")
                        .build())
                .build();
        Blob expected = DeepCopyHelper.deepCopy(EnvironmentHelper.resolve(reference, environment, Blob.class), Blob.class);
        expected.setValue(null);
        SubmodelElement actual = persistence.get(reference, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getIdentifiableAASTest() throws ResourceNotFoundException {
        String id = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getId().equals(id))
                .findFirst().get();
        AssetAdministrationShell actual = persistence.get(id, QueryModifier.DEFAULT, AssetAdministrationShell.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getIdentifiableSubmodelTest() throws ResourceNotFoundException {
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String id = submodelId;
        Submodel expected = environment.getSubmodels().stream()
                .filter(x -> x.getId().equals(id))
                .findFirst().get();
        Submodel actual = persistence.get(id, QueryModifier.DEFAULT, Submodel.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getIdentifiableConceptDescriptionTest() throws ResourceNotFoundException {
        String id = "https://acplt.org/Test_ConceptDescription";
        ConceptDescription expected = environment.getConceptDescriptions().stream()
                .filter(x -> x.getId().equals(id))
                .findFirst().get();
        ConceptDescription actual = persistence.get(id, QueryModifier.DEFAULT, ConceptDescription.class);
        Assert.assertEquals(expected, actual);

    }


    @Test
    public void getShellsNullTest() {
        String aasIdShort = "Test_AssetAdministrationShell_Mandatory";
        List<AssetAdministrationShell> expected = List.of();
        List<AssetAdministrationShell> actual = persistence.get(
                aasIdShort,
                List.of(GlobalAssetIdentification.builder()
                        .build()),
                QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getShellsAllTest() {
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells();
        List<AssetAdministrationShell> actual = persistence.get("", (List<AssetIdentification>) null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getShellsWithIdShortTest() {
        String aasIdShort = "Test_AssetAdministrationShell_Mandatory";
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(aasIdShort))
                .collect(Collectors.toList());
        List<AssetAdministrationShell> actual = persistence.get(aasIdShort, (List<AssetIdentification>) null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getShellsWithGlobalAssetIdentificationTest() {
        GlobalAssetIdentification globalAssetIdentification = new GlobalAssetIdentification();
        globalAssetIdentification.setValue("https://acplt.org/Test_Asset_Mandatory");
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getAssetInformation().getGlobalAssetID().equals(globalAssetIdentification.getValue()))
                .collect(Collectors.toList());
        List<AssetAdministrationShell> actual = persistence.get(null, List.of(globalAssetIdentification), QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelsEmptyTest() {
        String aasId = "Test_AssetAdministrationShell_Mandatory";
        List<Submodel> expected = List.of();
        List<Submodel> actual = persistence.get(aasId, new DefaultReference(), QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelsAllTest() {
        List<Submodel> expected = environment.getSubmodels();
        ExtendHelper.withoutBlobValue(expected);
        List<Submodel> actual = persistence.get(null, (Reference) null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelsWithIdShortTest() {
        String submodelIdShort = "TestSubmodel";
        List<Submodel> expected = environment.getSubmodels().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(submodelIdShort))
                .collect(Collectors.toList());
        List<Submodel> actual = persistence.get(submodelIdShort, (Reference) null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelsWithSemanticIdTest() {
        Reference semanticId = new DefaultReference.Builder()
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.GLOBAL_REFERENCE)
                        .value("http://acplt.org/SubmodelTemplates/ExampleSubmodel")
                        .build())
                .build();
        List<Submodel> expected = environment.getSubmodels().stream()
                .filter(Objects::nonNull)
                .filter(x -> ReferenceHelper.equals(x.getSemanticID(), semanticId))
                .collect(Collectors.toList());
        ExtendHelper.withoutBlobValue(expected);
        List<Submodel> actual = persistence.get("", semanticId, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementsTest() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell";
        String submodelId = "http://acplt.org/Submodels/Assets/TestAsset/Identification";
        Reference submodelReference = ReferenceHelper.build(aasId, submodelId);
        List<SubmodelElement> expected = environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements();
        List<SubmodelElement> actual = persistence.getSubmodelElements(submodelReference, null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementsWithSemanticIdTest() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell";
        String submodelId = "http://acplt.org/Submodels/Assets/TestAsset/Identification";
        Reference submodelReference = ReferenceHelper.build(aasId, submodelId);
        Reference semanticIdReference = new DefaultReference.Builder()
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.GLOBAL_REFERENCE)
                        .value("0173-1#02-AAO677#002")
                        .build())
                .build();
        List<SubmodelElement> expected = environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> ReferenceHelper.equals(x.getSemanticID(), semanticIdReference))
                .collect(Collectors.toList());
        List<SubmodelElement> actual = persistence.getSubmodelElements(submodelReference, semanticIdReference, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementsFromSubmodelElementCollectionTest() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionIdShort = "ExampleSubmodelElementCollection";
        Reference reference = ReferenceHelper.build(aasId, submodelId, submodelElementCollectionIdShort);
        List<SubmodelElement> expected = List.copyOf(((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(submodelElementCollectionIdShort))
                .findFirst().get())
                        .getValue());
        List<SubmodelElement> actual = persistence.getSubmodelElements(reference, null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getConceptDescriptionsAllTest() {
        List<ConceptDescription> expected = environment.getConceptDescriptions();
        List<ConceptDescription> actual = persistence.get(null, null, (Reference) null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getConceptDescriptionsWithIdShortTest() {
        String conceptDescriptionIdShort = "TestConceptDescription";
        List<ConceptDescription> actual = persistence.get(conceptDescriptionIdShort, null, (Reference) null, QueryModifier.DEFAULT);
        List<ConceptDescription> expected = environment.getConceptDescriptions().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(conceptDescriptionIdShort))
                .collect(Collectors.toList());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getConceptDescriptionsWithIsCaseOfTest() {
        String conceptDescriptionIdShort = "TestConceptDescription";
        Reference isCaseOf = new DefaultReference.Builder()
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.GLOBAL_REFERENCE)
                        .value("http://acplt.org/DataSpecifications/Conceptdescription/TestConceptDescription")
                        .build())
                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                .build();
        List<ConceptDescription> expected = environment.getConceptDescriptions().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(conceptDescriptionIdShort))
                .collect(Collectors.toList());
        List<ConceptDescription> actual = persistence.get(null, isCaseOf, (Reference) null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getConceptDescriptionsWithDataSpecificationTest() {
        Reference dataSpecification = new DefaultReference.Builder()
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.GLOBAL_REFERENCE)
                        .value("http://acplt.org/ReferenceElements/DataSpecificationX")
                        .build())
                .build();
        List<ConceptDescription> expected = environment.getConceptDescriptions().stream()
                .filter(x -> x.getEmbeddedDataSpecifications() != null
                        && x.getEmbeddedDataSpecifications().stream()
                                .anyMatch(y -> y.getDataSpecification() != null && y.getDataSpecification().equals(dataSpecification)))
                .collect(Collectors.toList());
        List<ConceptDescription> actual = persistence.get(null, null, dataSpecification, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getConceptDescriptionsWithCombination() {
        String conceptDescriptionIdShort = "TestConceptDescription";
        Reference isCaseOf = new DefaultReference.Builder()
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.GLOBAL_REFERENCE)
                        .value("http://acplt.org/DataSpecifications/Conceptdescription/TestConceptDescription")
                        .build())
                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                .build();
        List<ConceptDescription> expected = environment.getConceptDescriptions().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(conceptDescriptionIdShort))
                .collect(Collectors.toList());
        List<ConceptDescription> actual = persistence.get(conceptDescriptionIdShort, isCaseOf, null, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void putSubmodelElementNewInSubmodelTest() throws ResourceNotFoundException {
        SubmodelElement expected = DeepCopyHelper.deepCopy(environment.getSubmodels().get(0).getSubmodelElements().get(0),
                environment.getSubmodels().get(0).getSubmodelElements().get(0).getClass());
        String idShort = "NewIdShort";
        expected.setIdShort(idShort);
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        Reference parent = ReferenceHelper.build(aasId, submodelId);
        Reference submodelElementReference = ReferenceHelper.build(aasId, submodelId, idShort);
        persistence.put(parent, null, expected);
        SubmodelElement actual = persistence.get(submodelElementReference, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void putSubmodelElementChangeInSubmodelTest() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        Submodel submodel = environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId)).findFirst().get();
        int idxExpected = 0;
        SubmodelElement submodelElement = submodel.getSubmodelElements().get(idxExpected);
        SubmodelElement expected = DeepCopyHelper.deepCopy(submodelElement, submodelElement.getClass());
        String category = "NewCategory";
        expected.setCategory(category);
        Reference reference = ReferenceHelper.build(aasId, submodelId, submodelElement.getIdShort());
        persistence.put(null, reference, expected);
        SubmodelElement actualSubmodelElement = persistence.get(reference, QueryModifier.DEFAULT);
        Submodel actualSubmodel = persistence.get(submodel.getId(), QueryModifier.DEFAULT, Submodel.class);
        int idxActual = actualSubmodel.getSubmodelElements().indexOf(expected);
        Assert.assertEquals(expected, actualSubmodelElement);
        Assert.assertEquals(idxExpected, idxActual);
    }


    @Test
    public void putSubmodelElementNewInSubmodelElementCollectionTest() throws ResourceNotFoundException {
        SubmodelElement expected = DeepCopyHelper.deepCopy(environment.getSubmodels().get(0).getSubmodelElements().get(0),
                environment.getSubmodels().get(0).getSubmodelElements().get(0).getClass());
        String idShort = "NewIdShort";
        expected.setIdShort(idShort);
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionIdShort = "ExampleSubmodelElementCollection";
        Reference parent = ReferenceHelper.build(aasId, submodelId, submodelElementCollectionIdShort);
        Assert.assertNull(((SubmodelElementCollection) Objects.requireNonNull(environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId))
                .findFirst().get().getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(submodelElementCollectionIdShort))
                .findFirst().orElse(null)))
                        .getValue().stream()
                        .filter(x -> x.getIdShort().equalsIgnoreCase(idShort)).findFirst().orElse(null));
        persistence.put(parent, null, expected);
        SubmodelElement actual = persistence.get(ReferenceHelper.build(aasId, submodelId, submodelElementCollectionIdShort, idShort), QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void putSubmodelElementChangeInSubmodelElementCollectionTest() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionIdShort = "ExampleSubmodelElementCollection";
        SubmodelElement submodelElement = ((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(submodelElementCollectionIdShort))
                .findFirst().orElse(null))
                        .getValue().stream()
                        .findFirst()
                        .orElse(null);
        SubmodelElement expected = DeepCopyHelper.deepCopy(submodelElement, submodelElement.getClass());
        String category = "NewCategory";
        expected.setCategory(category);
        Reference reference = ReferenceHelper.build(aasId, submodelId, submodelElementCollectionIdShort, submodelElement.getIdShort());
        persistence.put(null, reference, expected);
        SubmodelElement actual = persistence.get(reference, new QueryModifier.Builder().extend(Extent.WITH_BLOB_VALUE).build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void removeSubmodelTest() throws ResourceNotFoundException {
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        Assert.assertNotNull(persistence.get(submodelId, QueryModifier.DEFAULT, Submodel.class));
        persistence.remove(submodelId);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.get(submodelId, QueryModifier.DEFAULT, Submodel.class));
    }


    @Test
    public void removeAASTest() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        Assert.assertNotNull(persistence.get(aasId, QueryModifier.DEFAULT, AssetAdministrationShell.class));
        persistence.remove(aasId);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.get(aasId, QueryModifier.DEFAULT, AssetAdministrationShell.class));
    }


    @Test
    public void removeByReferenceTest() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionIdShort = "ExampleSubmodelElementCollection";
        Reference reference = ReferenceHelper.build(aasId, submodelId, submodelElementCollectionIdShort);
        Assert.assertNotNull(persistence.get(reference, QueryModifier.DEFAULT));
        persistence.remove(reference);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.get(reference, QueryModifier.DEFAULT));
    }


    @Test
    public void removeByReferencePropertyInSubmodelElementCollectionTest() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionIdShort = "ExampleSubmodelElementCollection";
        String submodelElementIdShort = "ExampleFile";
        Reference reference = ReferenceHelper.build(aasId, submodelId, submodelElementCollectionIdShort, submodelElementIdShort);
        Assert.assertNotNull(persistence.get(reference, new OutputModifier()));
        persistence.remove(reference);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.get(reference, QueryModifier.DEFAULT));
    }


    @Test
    public void removeByReferencePropertyTest() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell";
        String submodelId = "http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial";
        String submodelElementIdShort = "ExampleEntity2";
        Reference reference = ReferenceHelper.build(aasId, submodelId, submodelElementIdShort);
        Assert.assertNotNull(persistence.get(reference, new OutputModifier()));
        persistence.remove(reference);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.get(reference, QueryModifier.DEFAULT));
    }


    @Test
    public void putIdentifiableNewTest() throws ResourceNotFoundException {
        Submodel expected = DeepCopyHelper.deepCopy(environment.getSubmodels().get(0),
                environment.getSubmodels().get(0).getClass());
        String idShort = "NewIdShort";
        expected.setIdShort(idShort);
        expected.setId("http://newIdentifier.org");
        persistence.put(expected);
        Identifiable actual = persistence.get(expected.getId(), QueryModifier.DEFAULT, Submodel.class);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void putIdentifiableChangeTest() throws ResourceNotFoundException {
        int expectedIndex = 0;
        ConceptDescription expected = DeepCopyHelper.deepCopy(environment.getConceptDescriptions().get(expectedIndex),
                environment.getConceptDescriptions().get(expectedIndex).getClass());
        String category = "NewCategory";
        expected.setCategory(category);
        persistence.put(expected);
        ConceptDescription actual = persistence.get(expected.getId(), QueryModifier.DEFAULT, ConceptDescription.class);
        int actualIndex = persistence.get(null, null, null, QueryModifier.DEFAULT).indexOf(actual);
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(expectedIndex, actualIndex);
    }


    @Test
    public void testQueryModifierExtend() throws ResourceNotFoundException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionIdShort = "ExampleSubmodelElementCollection";
        String submodelElementIdShort = "ExampleBlob";
        Reference reference = ReferenceHelper.build(aasId, submodelId, submodelElementCollectionIdShort, submodelElementIdShort);
        QueryModifier queryModifier = new QueryModifier.Builder().extend(Extent.WITH_BLOB_VALUE).build();
        SubmodelElement expected = ((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getId().equals(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(submodelElementCollectionIdShort))
                .findFirst().get())
                        .getValue().stream()
                        .filter(z -> z.getIdShort().equalsIgnoreCase(submodelElementIdShort))
                        .findFirst().get();
        SubmodelElement actual = persistence.get(reference, queryModifier);
        Assert.assertEquals(expected, actual);

        queryModifier = new QueryModifier.Builder().extend(Extent.WITHOUT_BLOB_VALUE).build();
        expected = DeepCopyHelper.deepCopy(expected, SubmodelElement.class);
        ((Blob) expected).setValue(null);
        actual = persistence.get(reference, queryModifier);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testQueryModifierLevel() throws ResourceNotFoundException {
        QueryModifier queryModifier = new QueryModifier.Builder().level(Level.DEEP).build();
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        Submodel expected = environment.getSubmodels().stream()
                .filter(x -> x.getId().equals(submodelId)).findFirst().get();
        Submodel actual = persistence.get(submodelId, queryModifier, Submodel.class);
        Assert.assertEquals(expected, actual);

        queryModifier = new QueryModifier.Builder().level(Level.CORE).build();
        actual = persistence.get(submodelId, queryModifier, Submodel.class);
        List<SubmodelElement> submodelElementCollections = actual.getSubmodelElements().stream()
                .filter(x -> SubmodelElementCollection.class.isAssignableFrom(x.getClass()))
                .collect(Collectors.toList());
        Assert.assertTrue(submodelElementCollections.stream().allMatch(x -> ((SubmodelElementCollection) x).getValue().isEmpty()));
    }


    @Test
    public void testOperationHandle() {
        OperationResult operationResult = new OperationResult.Builder()
                .requestId("Test")
                .executionState(ExecutionState.INITIATED)
                .build();
        OperationHandle actual = persistence.putOperationContext(null, "Test", operationResult);
        OperationHandle expected = new OperationHandle.Builder()
                .handleId(actual.getHandleId())
                .requestId("Test")
                .build();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testUpdateOperationResult() {
        OperationResult expected = new OperationResult.Builder()
                .requestId("Test")
                .executionState(ExecutionState.INITIATED)
                .build();
        OperationHandle operationHandle = persistence.putOperationContext(null, "Test", expected);
        expected.setExecutionState(ExecutionState.COMPLETED);
        expected.setExecutionResult(new Result.Builder()
                .message(new Message.Builder()
                        .code("test")
                        .build())
                .success(true)
                .build());
        persistence.putOperationContext(operationHandle.getHandleId(), null, expected);
        OperationResult actual = persistence.getOperationResult(operationHandle.getHandleId());
        Assert.assertEquals(expected, actual);

    }
}
