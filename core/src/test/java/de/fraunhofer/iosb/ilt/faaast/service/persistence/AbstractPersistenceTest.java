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
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.OutputModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationHandle;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.GlobalAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ExtendHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.ExecutionState;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationResult;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.junit.After;
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
        persistence.start();
    }


    @After
    public void stop() {
        persistence.stop();
    }


    @Test
    public void withInitialModelFile() throws ResourceNotFoundException, ConfigurationException, PersistenceException {
        PersistenceConfig<T> config = getPersistenceConfig(fullModelFile, null);
        persistence = config.newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        persistence.start();
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getId().equals(aasId))
                .findFirst().get();
        AssetAdministrationShell actual = persistence.getAssetAdministrationShell(aasId, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
        persistence.stop();
    }


    @Test
    public void withInitialModelAndModelFile() throws ResourceNotFoundException, ConfigurationException, PersistenceException {
        PersistenceConfig<T> config = getPersistenceConfig(minimalModelFile, environment);
        Persistence tempPersistence = config.newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        tempPersistence.start();
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getId().equals(aasId))
                .findFirst().get();
        AssetAdministrationShell actual = tempPersistence.getAssetAdministrationShell(aasId, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
        tempPersistence.stop();
    }


    @Test
    public void getSubmodelElement() throws ResourceNotFoundException, PersistenceException {
        String submodelId = "http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial";
        String submodelElementId = "ExampleEntity2";
        IdShortPath path = IdShortPath.builder()
                .idShort(submodelElementId)
                .build();
        SubmodelElement expected = environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(submodelElementId))
                .findFirst().get();
        SubmodelElement actual = persistence.getSubmodelElement(
                SubmodelElementIdentifier.builder()
                        .submodelId(submodelId)
                        .idShortPath(path)
                        .build(),
                QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementWithSimilarIDShort() throws ResourceNotFoundException, PersistenceException {
        String submodelId = "http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial";
        String submodelElementId = "ExampleEntity2";
        ConceptDescription conceptDescription = new DefaultConceptDescription.Builder()
                .idShort(submodelElementId)
                .id("http://example.org/CD")
                .category("Entity")
                .build();
        AssetAdministrationShell shell = new DefaultAssetAdministrationShell.Builder()
                .idShort(submodelElementId)
                .id("http://example.org/Shell")
                .build();
        persistence.save(shell);
        persistence.save(conceptDescription);
        SubmodelElement expected = environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(submodelElementId))
                .findFirst().get();
        IdShortPath path = IdShortPath.builder()
                .idShort(submodelElementId)
                .build();
        SubmodelElement actual = persistence.getSubmodelElement(
                SubmodelElementIdentifier.builder()
                        .submodelId(submodelId)
                        .idShortPath(path)
                        .build(),
                QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementWithBlob() throws ResourceNotFoundException, PersistenceException {
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionId = "ExampleSubmodelElementCollection";
        String submodelElementId = "ExampleBlob";
        QueryModifier queryModifier = new QueryModifier.Builder().extend(Extent.WITH_BLOB_VALUE).build();
        SubmodelElement expected = ((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(submodelElementCollectionId))
                .findFirst().get())
                .getValue().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(submodelElementId))
                .findFirst().get();
        IdShortPath path = IdShortPath.builder()
                .idShort(submodelElementCollectionId)
                .idShort(submodelElementId)
                .build();
        SubmodelElement actual = persistence.getSubmodelElement(
                SubmodelElementIdentifier.builder()
                        .submodelId(submodelId)
                        .idShortPath(path)
                        .build(),
                queryModifier);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementWithOutBlob() throws ResourceNotFoundException, PersistenceException {
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionId = "ExampleSubmodelElementCollection";
        String submodelElementId = "ExampleBlob";
        Reference reference = new ReferenceBuilder()
                .submodel(submodelId)
                .element(submodelElementCollectionId)
                .element(submodelElementId)
                .build();
        Blob expected = DeepCopyHelper.deepCopy(EnvironmentHelper.resolve(reference, environment, Blob.class), Blob.class);
        expected.setValue(null);
        SubmodelElement actual = persistence.getSubmodelElement(
                SubmodelElementIdentifier.builder()
                        .submodelId(submodelId)
                        .idShortPath(IdShortPath.fromReference(reference))
                        .build(),
                QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getIdentifiableAAS() throws ResourceNotFoundException, PersistenceException {
        String id = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getId().equals(id))
                .findFirst().get();
        AssetAdministrationShell actual = persistence.getAssetAdministrationShell(id, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getIdentifiableSubmodel() throws ResourceNotFoundException, PersistenceException {
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String id = submodelId;
        Submodel expected = environment.getSubmodels().stream()
                .filter(x -> x.getId().equals(id))
                .findFirst().get();
        Submodel actual = persistence.getSubmodel(id, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getIdentifiableConceptDescription() throws ResourceNotFoundException, PersistenceException {
        String id = "https://acplt.org/Test_ConceptDescription";
        ConceptDescription expected = environment.getConceptDescriptions().stream()
                .filter(x -> x.getId().equals(id))
                .findFirst().get();
        ConceptDescription actual = persistence.getConceptDescription(id, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);

    }


    @Test
    public void getShellsNull() throws PersistenceException {
        String aasIdShort = "Test_AssetAdministrationShell_Mandatory";
        List<AssetAdministrationShell> expected = List.of();
        List<AssetAdministrationShell> actual = persistence.findAssetAdministrationShells(
                AssetAdministrationShellSearchCriteria.builder()
                        .idShort(aasIdShort)
                        .assetIds(List.of(GlobalAssetIdentification.builder().build()))
                        .build(),
                QueryModifier.DEFAULT,
                PagingInfo.ALL)
                .getContent();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getShellsAll() throws PersistenceException {
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells();
        List<AssetAdministrationShell> actual = persistence
                .getAllAssetAdministrationShells(QueryModifier.DEFAULT, PagingInfo.ALL)
                .getContent();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getShellsWithIdShort() throws PersistenceException {
        String aasIdShort = "Test_AssetAdministrationShell_Mandatory";
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(aasIdShort))
                .collect(Collectors.toList());
        List<AssetAdministrationShell> actual = persistence.findAssetAdministrationShells(
                AssetAdministrationShellSearchCriteria.builder()
                        .idShort(aasIdShort)
                        .build(),
                QueryModifier.DEFAULT,
                PagingInfo.ALL)
                .getContent();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getShellsWithGlobalAssetIdentification() throws PersistenceException {
        GlobalAssetIdentification globalAssetIdentification = new GlobalAssetIdentification();
        globalAssetIdentification.setValue("https://acplt.org/Test_Asset_Mandatory");
        List<AssetAdministrationShell> expected = environment.getAssetAdministrationShells().stream()
                .filter(x -> x.getAssetInformation().getGlobalAssetId().equals(globalAssetIdentification.getValue()))
                .collect(Collectors.toList());
        List<AssetAdministrationShell> actual = persistence.findAssetAdministrationShells(
                AssetAdministrationShellSearchCriteria.builder()
                        .assetIds(List.of(globalAssetIdentification))
                        .build(),
                QueryModifier.DEFAULT,
                PagingInfo.ALL)
                .getContent();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelsAll() throws PersistenceException {
        List<Submodel> expected = environment.getSubmodels();
        ExtendHelper.withoutBlobValue(expected);
        List<Submodel> actual = persistence
                .getAllSubmodels(QueryModifier.DEFAULT, PagingInfo.ALL)
                .getContent();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelsWithIdShort() throws PersistenceException {
        String submodelIdShort = "TestSubmodel";
        List<Submodel> expected = environment.getSubmodels().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(submodelIdShort))
                .collect(Collectors.toList());
        List<Submodel> actual = persistence.findSubmodels(
                SubmodelSearchCriteria.builder()
                        .idShort(submodelIdShort)
                        .build(),
                QueryModifier.DEFAULT,
                PagingInfo.ALL)
                .getContent();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelsWithsemanticId() throws PersistenceException {
        Reference semanticId = new DefaultReference.Builder()
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.GLOBAL_REFERENCE)
                        .value("http://acplt.org/SubmodelTemplates/ExampleSubmodel")
                        .build())
                .build();
        List<Submodel> expected = environment.getSubmodels().stream()
                .filter(Objects::nonNull)
                .filter(x -> ReferenceHelper.equals(x.getSemanticId(), semanticId))
                .collect(Collectors.toList());
        ExtendHelper.withoutBlobValue(expected);
        List<Submodel> actual = persistence.findSubmodels(
                SubmodelSearchCriteria.builder()
                        .semanticId(semanticId)
                        .build(),
                QueryModifier.DEFAULT,
                PagingInfo.ALL)
                .getContent();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElements() throws ResourceNotFoundException, PersistenceException, ResourceNotAContainerElementException {
        String submodelId = "http://acplt.org/Submodels/Assets/TestAsset/Identification";
        IdShortPath path = IdShortPath.builder()
                .build();
        List<SubmodelElement> expected = environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements();
        List<SubmodelElement> actual = persistence.getSubmodelElements(
                SubmodelElementIdentifier.builder()
                        .submodelId(submodelId)
                        .idShortPath(path)
                        .build(),
                QueryModifier.DEFAULT,
                PagingInfo.ALL)
                .getContent();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementsWithsemanticId() throws ResourceNotFoundException, PersistenceException {
        String submodelId = "http://acplt.org/Submodels/Assets/TestAsset/Identification";
        Reference semanticId = ReferenceBuilder.global("0173-1#02-AAO677#002");
        List<SubmodelElement> expected = environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(x -> ReferenceHelper.equals(x.getSemanticId(), semanticId))
                .collect(Collectors.toList());
        List<SubmodelElement> actual = persistence.findSubmodelElements(
                SubmodelElementSearchCriteria.builder()
                        .parent(SubmodelElementIdentifier.builder()
                                .submodelId(submodelId)
                                .build())
                        .semanticId(semanticId)
                        .build(),
                QueryModifier.DEFAULT,
                PagingInfo.ALL)
                .getContent();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementsFromSubmodelElementCollection() throws ResourceNotFoundException, PersistenceException, ResourceNotAContainerElementException {
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementId = "ExampleSubmodelElementCollection";
        Reference reference = ReferenceBuilder.forSubmodel(submodelId, submodelElementId);
        Collection<SubmodelElement> expected = EnvironmentHelper.resolve(reference, environment, SubmodelElementCollection.class).getValue();
        List<SubmodelElement> actual = persistence
                .getSubmodelElements(reference, QueryModifier.DEFAULT, PagingInfo.ALL)
                .getContent();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getSubmodelElementsFromSubmodelElementList() throws ResourceNotFoundException, PersistenceException, ResourceNotAContainerElementException {
        String submodelId = "https://acplt.org/Test_Submodel";
        String submodelElementId = "ExampleSubmodelElementListOrdered";
        Reference reference = ReferenceBuilder.forSubmodel(submodelId, submodelElementId);
        List<SubmodelElement> expected = EnvironmentHelper.resolve(reference, environment, SubmodelElementList.class).getValue();
        List<SubmodelElement> actual = persistence
                .getSubmodelElements(reference, QueryModifier.DEFAULT, PagingInfo.ALL)
                .getContent();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getConceptDescriptionsAll() throws PersistenceException {
        List<ConceptDescription> expected = environment.getConceptDescriptions();
        List<ConceptDescription> actual = persistence
                .getAllConceptDescriptions(QueryModifier.DEFAULT, PagingInfo.ALL)
                .getContent();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getConceptDescriptionsWithIdShort() throws PersistenceException {
        String idShort = "TestConceptDescription";
        List<ConceptDescription> actual = persistence.findConceptDescriptions(
                ConceptDescriptionSearchCriteria.builder()
                        .idShort(idShort)
                        .build(),
                QueryModifier.DEFAULT,
                PagingInfo.ALL)
                .getContent();
        List<ConceptDescription> expected = environment.getConceptDescriptions().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(idShort))
                .collect(Collectors.toList());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getConceptDescriptionsWithIsCaseOf() throws PersistenceException {
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
        List<ConceptDescription> actual = persistence.findConceptDescriptions(
                ConceptDescriptionSearchCriteria.builder()
                        .isCaseOf(isCaseOf)
                        .build(),
                QueryModifier.DEFAULT,
                PagingInfo.ALL)
                .getContent();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getConceptDescriptionsWithDataSpecification() throws PersistenceException {
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
        List<ConceptDescription> actual = persistence.findConceptDescriptions(
                ConceptDescriptionSearchCriteria.builder()
                        .dataSpecification(dataSpecification)
                        .build(),
                QueryModifier.DEFAULT,
                PagingInfo.ALL)
                .getContent();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void getConceptDescriptionsWithCombination() throws PersistenceException {
        String idShort = "TestConceptDescription";
        Reference isCaseOf = new DefaultReference.Builder()
                .keys(new DefaultKey.Builder()
                        .type(KeyTypes.GLOBAL_REFERENCE)
                        .value("http://acplt.org/DataSpecifications/Conceptdescription/TestConceptDescription")
                        .build())
                .type(ReferenceTypes.EXTERNAL_REFERENCE)
                .build();
        List<ConceptDescription> expected = environment.getConceptDescriptions().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(idShort))
                .collect(Collectors.toList());
        List<ConceptDescription> actual = persistence.findConceptDescriptions(
                ConceptDescriptionSearchCriteria.builder()
                        .idShort(idShort)
                        .isCaseOf(isCaseOf)
                        .build(),
                QueryModifier.DEFAULT,
                PagingInfo.ALL)
                .getContent();
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void putSubmodelElementNewInSubmodel() throws ResourceNotFoundException, PersistenceException, ResourceNotAContainerElementException, ResourceAlreadyExistsException {
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        SubmodelElement expected = DeepCopyHelper.deepCopy(environment.getSubmodels().get(0).getSubmodelElements().get(0),
                environment.getSubmodels().get(0).getSubmodelElements().get(0).getClass());
        String idShort = "NewIdShort";
        expected.setIdShort(idShort);
        persistence.insert(ReferenceBuilder.forSubmodel(submodelId), expected);
        SubmodelElement actual = persistence.getSubmodelElement(
                SubmodelElementIdentifier.builder()
                        .submodelId(submodelId)
                        .idShortPath(IdShortPath.builder()
                                .idShort(idShort)
                                .build())
                        .build(),
                QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void putSubmodelElementChangeInSubmodel() throws ResourceNotFoundException, PersistenceException {
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        Submodel submodel = environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId)).findFirst().get();
        int idxExpected = 0;
        SubmodelElement submodelElement = submodel.getSubmodelElements().get(idxExpected);
        SubmodelElement expected = DeepCopyHelper.deepCopy(submodelElement, submodelElement.getClass());
        String category = "NewCategory";
        expected.setCategory(category);
        Reference reference = new ReferenceBuilder()
                .submodel(submodelId)
                .element(submodelElement)
                .build();
        persistence.update(reference, expected);
        SubmodelElement actualSubmodelElement = persistence.getSubmodelElement(reference, QueryModifier.DEFAULT);
        Submodel actualSubmodel = persistence.getSubmodel(submodel.getId(), QueryModifier.DEFAULT);
        int idxActual = actualSubmodel.getSubmodelElements().indexOf(expected);
        Assert.assertEquals(expected, actualSubmodelElement);
        Assert.assertEquals(idxExpected, idxActual);
    }


    @Test
    public void putSubmodelElementNewInSubmodelElementCollection()
            throws ResourceNotFoundException, PersistenceException, ResourceNotAContainerElementException, ResourceAlreadyExistsException {
        SubmodelElement expected = DeepCopyHelper.deepCopy(environment.getSubmodels().get(0).getSubmodelElements().get(0),
                environment.getSubmodels().get(0).getSubmodelElements().get(0).getClass());
        String idShort = "NewIdShort";
        expected.setIdShort(idShort);
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionId = "ExampleSubmodelElementCollection";
        Reference parent = new ReferenceBuilder()
                .submodel(submodelId)
                .element(submodelElementCollectionId)
                .build();
        Assert.assertNull(((SubmodelElementCollection) Objects.requireNonNull(environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId))
                .findFirst().get().getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(submodelElementCollectionId))
                .findFirst().orElse(null)))
                .getValue().stream()
                .filter(x -> x.getIdShort().equalsIgnoreCase(idShort)).findFirst().orElse(null));
        persistence.insert(parent, expected);
        SubmodelElement actual = persistence.getSubmodelElement(
                ReferenceBuilder
                        .with(parent)
                        .element(idShort)
                        .build(),
                QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void putSubmodelElementNewInSubmodelElementList()
            throws ResourceNotFoundException, PersistenceException, ResourceNotAContainerElementException, ResourceAlreadyExistsException {
        String submodelId = "https://acplt.org/Test_Submodel";
        String submodelElementListId = "ExampleSubmodelElementListOrdered";
        Reference reference = ReferenceBuilder.forSubmodel(submodelId, submodelElementListId);
        SubmodelElementList submodelElementList = EnvironmentHelper.resolve(reference, environment, SubmodelElementList.class);
        SubmodelElement newElement = DeepCopyHelper.deepCopy(submodelElementList.getValue().get(0), SubmodelElement.class);
        newElement.setIdShort("new");
        SubmodelElementList expected = DeepCopyHelper.deepCopy(submodelElementList, submodelElementList.getClass());
        expected.getValue().add(newElement);
        persistence.insert(reference, newElement);
        SubmodelElement actual = persistence.getSubmodelElement(
                reference,
                new QueryModifier.Builder()
                        .extend(Extent.WITH_BLOB_VALUE)
                        .build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void putSubmodelElementChangeInSubmodelElementCollection() throws ResourceNotFoundException, PersistenceException {
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionId = "ExampleSubmodelElementCollection";
        SubmodelElement submodelElement = ((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getId().equalsIgnoreCase(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(submodelElementCollectionId))
                .findFirst().orElse(null))
                .getValue().stream()
                .findFirst()
                .orElse(null);
        SubmodelElement expected = DeepCopyHelper.deepCopy(submodelElement, submodelElement.getClass());
        String category = "NewCategory";
        expected.setCategory(category);
        Reference reference = new ReferenceBuilder()
                .submodel(submodelId)
                .element(submodelElementCollectionId)
                .element(submodelElement)
                .build();
        persistence.update(reference, expected);
        SubmodelElement actual = persistence.getSubmodelElement(reference, new QueryModifier.Builder().extend(Extent.WITH_BLOB_VALUE).build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void putSubmodelElementChangeInSubmodelElementList() throws ResourceNotFoundException, PersistenceException {
        Reference reference = ReferenceHelper.parse("(Submodel)https://acplt.org/Test_Submodel, (SubmodelElementList)ExampleSubmodelElementListOrdered, (SubmodelElement)0");
        SubmodelElement submodelElement = EnvironmentHelper.resolve(reference, environment, SubmodelElement.class);
        SubmodelElement expected = DeepCopyHelper.deepCopy(submodelElement, submodelElement.getClass());
        String category = "NewCategory";
        expected.setCategory(category);
        persistence.update(reference, expected);
        SubmodelElement actual = persistence.getSubmodelElement(reference, new QueryModifier.Builder().extend(Extent.WITH_BLOB_VALUE).build());
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void removeSubmodel() throws ResourceNotFoundException, PersistenceException {
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        Assert.assertNotNull(persistence.getSubmodel(submodelId, QueryModifier.DEFAULT));
        persistence.deleteSubmodel(submodelId);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.getSubmodel(submodelId, QueryModifier.DEFAULT));
    }


    @Test
    public void removeAAS() throws ResourceNotFoundException, PersistenceException {
        String aasId = "https://acplt.org/Test_AssetAdministrationShell_Mandatory";
        Assert.assertNotNull(persistence.getAssetAdministrationShell(aasId, QueryModifier.DEFAULT));
        persistence.deleteAssetAdministrationShell(aasId);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.getAssetAdministrationShell(aasId, QueryModifier.DEFAULT));
    }


    @Test
    public void removeAll() throws PersistenceException {
        persistence.deleteAll();
        Assert.assertTrue(persistence.getAllAssetAdministrationShells(QueryModifier.MINIMAL, PagingInfo.ALL).getContent().isEmpty());
        Assert.assertTrue(persistence.getAllSubmodels(QueryModifier.MINIMAL, PagingInfo.ALL).getContent().isEmpty());
        Assert.assertTrue(persistence.getAllConceptDescriptions(QueryModifier.MINIMAL, PagingInfo.ALL).getContent().isEmpty());
    }


    @Test
    public void removeByReference() throws ResourceNotFoundException, PersistenceException {
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionId = "ExampleSubmodelElementCollection";
        Reference reference = new ReferenceBuilder()
                .submodel(submodelId)
                .element(submodelElementCollectionId)
                .build();
        Assert.assertNotNull(persistence.getSubmodelElement(reference, QueryModifier.DEFAULT));
        persistence.deleteSubmodelElement(reference);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.getSubmodelElement(reference, QueryModifier.DEFAULT));
    }


    @Test
    public void removeByReferencePropertyInSubmodelElementCollection() throws ResourceNotFoundException, PersistenceException {
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionId = "ExampleSubmodelElementCollection";
        String submodelElementId = "ExampleFile";
        Reference reference = new ReferenceBuilder()
                .submodel(submodelId)
                .element(submodelElementCollectionId)
                .element(submodelElementId)
                .build();
        Assert.assertNotNull(persistence.getSubmodelElement(reference, new OutputModifier()));
        persistence.deleteSubmodelElement(reference);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.getSubmodelElement(reference, QueryModifier.DEFAULT));
    }


    @Test
    public void removeByReferencePropertyInSubmodelElementList() throws ResourceNotFoundException, PersistenceException {
        String submodelId = "https://acplt.org/Test_Submodel";
        String submodelElementCollectionId = "ExampleSubmodelElementListOrdered";
        Reference reference = new ReferenceBuilder()
                .submodel(submodelId)
                .element(submodelElementCollectionId)
                .index(0)
                .build();
        SubmodelElement original = persistence.getSubmodelElement(reference, QueryModifier.DEFAULT);
        persistence.deleteSubmodelElement(reference);
        SubmodelElement actual = persistence.getSubmodelElement(reference, QueryModifier.DEFAULT);
        Assert.assertNotEquals(original, actual);
    }


    @Test
    public void removeByReferenceProperty() throws ResourceNotFoundException, PersistenceException {
        String submodelId = "http://acplt.org/Submodels/Assets/TestAsset/BillOfMaterial";
        String submodelElementId = "ExampleEntity2";
        Reference reference = new ReferenceBuilder()
                .submodel(submodelId)
                .element(submodelElementId)
                .build();
        Assert.assertNotNull(persistence.getSubmodelElement(reference, new OutputModifier()));
        persistence.deleteSubmodelElement(reference);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.getSubmodelElement(reference, QueryModifier.DEFAULT));
    }


    @Test
    public void putIdentifiableNew() throws ResourceNotFoundException, PersistenceException {
        Submodel expected = DeepCopyHelper.deepCopy(environment.getSubmodels().get(0),
                environment.getSubmodels().get(0).getClass());
        String idShort = "NewIdShort";
        expected.setIdShort(idShort);
        expected.setId("http://newIdentifier.org");
        persistence.save(expected);
        Submodel actual = persistence.getSubmodel(expected.getId(), QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void putIdentifiableChange() throws ResourceNotFoundException, PersistenceException {
        int expectedIndex = 0;
        ConceptDescription expected = DeepCopyHelper.deepCopy(
                environment.getConceptDescriptions().get(expectedIndex),
                environment.getConceptDescriptions().get(expectedIndex).getClass());
        String category = "NewCategory";
        expected.setCategory(category);
        persistence.save(expected);
        ConceptDescription actual = persistence.getConceptDescription(expected.getId(), QueryModifier.DEFAULT);
        int actualIndex = persistence
                .getAllConceptDescriptions(QueryModifier.DEFAULT, PagingInfo.ALL)
                .getContent()
                .indexOf(actual);
        Assert.assertEquals(expected, actual);
        Assert.assertEquals(expectedIndex, actualIndex);
    }


    @Test
    public void testQueryModifierExtend() throws ResourceNotFoundException, PersistenceException {
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        String submodelElementCollectionId = "ExampleSubmodelElementCollection";
        String submodelElementId = "ExampleBlob";
        Reference reference = new ReferenceBuilder()
                .submodel(submodelId)
                .element(submodelElementCollectionId)
                .element(submodelElementId)
                .build();
        QueryModifier queryModifier = new QueryModifier.Builder().extend(Extent.WITH_BLOB_VALUE).build();
        SubmodelElement expected = ((SubmodelElementCollection) environment.getSubmodels().stream()
                .filter(x -> x.getId().equals(submodelId))
                .findFirst().get()
                .getSubmodelElements().stream()
                .filter(y -> y.getIdShort().equalsIgnoreCase(submodelElementCollectionId))
                .findFirst().get())
                .getValue().stream()
                .filter(z -> z.getIdShort().equalsIgnoreCase(submodelElementId))
                .findFirst().get();
        SubmodelElement actual = persistence.getSubmodelElement(reference, queryModifier);
        Assert.assertEquals(expected, actual);

        queryModifier = new QueryModifier.Builder().extend(Extent.WITHOUT_BLOB_VALUE).build();
        expected = DeepCopyHelper.deepCopy(expected, SubmodelElement.class);
        ((Blob) expected).setValue(null);
        actual = persistence.getSubmodelElement(reference, queryModifier);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void testQueryModifierLevel() throws ResourceNotFoundException, PersistenceException {
        QueryModifier queryModifier = new QueryModifier.Builder().level(Level.DEEP).build();
        String submodelId = "https://acplt.org/Test_Submodel_Mandatory";
        Submodel expected = environment.getSubmodels().stream()
                .filter(x -> x.getId().equals(submodelId)).findFirst().get();
        Submodel actual = persistence.getSubmodel(submodelId, queryModifier);
        Assert.assertEquals(expected, actual);

        queryModifier = new QueryModifier.Builder().level(Level.CORE).build();
        actual = persistence.getSubmodel(submodelId, queryModifier);
        List<SubmodelElement> submodelElementCollections = actual.getSubmodelElements().stream()
                .filter(x -> SubmodelElementCollection.class.isAssignableFrom(x.getClass()))
                .collect(Collectors.toList());
        Assert.assertTrue(submodelElementCollections.stream().allMatch(x -> ((SubmodelElementCollection) x).getValue().isEmpty()));
    }


    @Test
    public void testUpdateOperationResult() throws ResourceNotFoundException, PersistenceException {
        OperationResult expected = new DefaultOperationResult.Builder()
                .executionState(ExecutionState.INITIATED)
                .build();
        OperationHandle operationHandle = new OperationHandle();
        persistence.save(operationHandle, expected);
        expected.setExecutionState(ExecutionState.COMPLETED);
        expected.setMessages(List.of(
                new Message.Builder()
                        .code("test")
                        .build()));
        persistence.save(operationHandle, expected);
        OperationResult actual = persistence.getOperationResult(operationHandle);
        Assert.assertEquals(expected, actual);
    }


    private File copyResourceToTempDir(String resourceName) throws IOException {
        File result = tempDir.newFile(resourceName);
        try (InputStream inputStream = AbstractPersistenceTest.class.getClassLoader().getResourceAsStream(resourceName)) {
            Files.copy(inputStream, result.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return result;
    }
}
