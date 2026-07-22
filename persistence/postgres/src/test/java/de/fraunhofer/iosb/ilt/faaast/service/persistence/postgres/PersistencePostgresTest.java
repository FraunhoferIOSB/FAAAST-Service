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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.postgres;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASSimple;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.asset.SpecificAssetIdentification;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AbstractPersistenceTest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AssetAdministrationShellSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelElementSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelSearchCriteria;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultBlob;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;


public class PersistencePostgresTest extends AbstractPersistenceTest<PersistencePostgres, PersistencePostgresConfig> {

    private static final ServiceContext SERVICE_CONTEXT = Mockito.mock(ServiceContext.class);
    private static EmbeddedPostgres embeddedPostgres;
    private static String jdbcUrl;

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        // Start  embedded PostgreSQL
        embeddedPostgres = EmbeddedPostgres.start();
        jdbcUrl = embeddedPostgres.getJdbcUrl("postgres", "postgres");

        System.out.println("Embedded PostgreSQL started on: " + jdbcUrl);
    }


    @AfterClass
    public static void tearDownDatabase() throws Exception {
        if (embeddedPostgres != null) {
            embeddedPostgres.close();
        }
    }


    @Override
    public PersistencePostgresConfig getPersistenceConfig(File initialModelFile, Environment initialModel)
            throws ConfigurationInitializationException {
        return getPersistenceConfig(initialModelFile, initialModel, true);
    }


    public PersistencePostgresConfig getPersistenceConfig(File initialModelFile, Environment initialModel, boolean override)
            throws ConfigurationInitializationException {
        try {
            return PersistencePostgresConfig.builder()
                    .initialModel(initialModel)
                    .initialModelFile(initialModelFile)
                    .jdbc(jdbcUrl)
                    .username("postgres") // default user
                    .password("") // default empty password
                    .override(override)
                    .build();
        }
        catch (Exception e) {
            throw new ConfigurationInitializationException("Failed to create Postgres config", e);
        }
    }


    @Test
    public void testPagingOverMultiplePages()
            throws ConfigurationException, PersistenceException {
        PersistencePostgres persistence = getPersistenceConfig(null, null, true)
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        persistence.start();

        final int submodelCount = 10;
        final long pageSize = 3;
        for (int i = 0; i < submodelCount; i++) {
            persistence.save(new DefaultSubmodel.Builder()
                    .id("http://example.org/submodel/" + i)
                    .idShort("submodel" + i)
                    .build());
        }

        Set<String> seenIds = new HashSet<>();
        String cursor = null;
        int pages = 0;
        do {
            Page<Submodel> page = persistence.findSubmodels(
                    SubmodelSearchCriteria.NONE,
                    QueryModifier.DEFAULT,
                    PagingInfo.builder()
                            .cursor(cursor)
                            .limit(pageSize)
                            .build());
            Assert.assertTrue(page.getContent().size() <= pageSize);
            page.getContent().forEach(x -> Assert.assertTrue(
                    "duplicate submodel returned while paging: " + x.getId(),
                    seenIds.add(x.getId())));
            cursor = page.getMetadata().getCursor();
            pages++;
        } while (cursor != null);

        Assert.assertEquals(submodelCount, seenIds.size());
        Assert.assertEquals((submodelCount + pageSize - 1) / pageSize, pages);

        persistence.stop();
    }


    @Test
    public void getShellsWithSpecificAssetIdentification()
            throws ConfigurationException, PersistenceException {
        PersistencePostgres persistence = getPersistenceConfig(null, null, true)
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        persistence.start();

        AssetAdministrationShell expected = new DefaultAssetAdministrationShell.Builder()
                .id("http://example.org/aas/1")
                .idShort("aas1")
                .assetInformation(new DefaultAssetInformation.Builder()
                        .specificAssetIds(new DefaultSpecificAssetId.Builder()
                                .name("serialNumber")
                                .value("4711")
                                .build())
                        .build())
                .build();
        AssetAdministrationShell other = new DefaultAssetAdministrationShell.Builder()
                .id("http://example.org/aas/2")
                .idShort("aas2")
                .assetInformation(new DefaultAssetInformation.Builder()
                        .specificAssetIds(new DefaultSpecificAssetId.Builder()
                                .name("serialNumber")
                                .value("0815")
                                .build())
                        .build())
                .build();
        persistence.save(expected);
        persistence.save(other);

        Page<AssetAdministrationShell> actual = persistence.findAssetAdministrationShells(
                AssetAdministrationShellSearchCriteria.builder()
                        .assetId(SpecificAssetIdentification.builder()
                                .key("serialNumber")
                                .value("4711")
                                .build())
                        .build(),
                QueryModifier.DEFAULT,
                PagingInfo.ALL);

        Assert.assertEquals(List.of(expected), actual.getContent());

        persistence.stop();
    }


    @Test
    public void testDeleteNonExistentThrowsResourceNotFound()
            throws ConfigurationException, PersistenceException {
        PersistencePostgres persistence = getPersistenceConfig(null, null, true)
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        persistence.start();

        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.deleteSubmodel("http://example.org/does-not-exist"));
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.deleteAssetAdministrationShell("http://example.org/does-not-exist"));
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.deleteConceptDescription("http://example.org/does-not-exist"));

        persistence.stop();
    }


    @Test
    public void updateSubmodelElementInListIndexAboveTen()
            throws ConfigurationException, ResourceNotFoundException, PersistenceException {
        PersistencePostgres persistence = getPersistenceConfig(null, null, true)
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        persistence.start();

        String submodelId = "http://example.org/submodel/list";
        List<SubmodelElement> listElements = new java.util.ArrayList<>();
        for (int i = 0; i < 12; i++) {
            listElements.add(new DefaultProperty.Builder()
                    .value("value" + i)
                    .valueType(DataTypeDefXsd.STRING)
                    .build());
        }
        persistence.save(new DefaultSubmodel.Builder()
                .id(submodelId)
                .idShort("listSubmodel")
                .submodelElements(new DefaultSubmodelElementList.Builder()
                        .idShort("list")
                        .value(listElements)
                        .build())
                .build());

        Reference elementRef = new ReferenceBuilder()
                .submodel(submodelId)
                .element("list")
                .index(11)
                .build();
        SubmodelElement expected = new DefaultProperty.Builder()
                .value("updated")
                .valueType(DataTypeDefXsd.STRING)
                .build();
        persistence.update(elementRef, expected);

        Assert.assertEquals(expected, persistence.getSubmodelElement(elementRef, QueryModifier.DEFAULT));

        persistence.stop();
    }


    @Test
    public void submodelElementDeepNestedGetUpdateDelete()
            throws ConfigurationException, ResourceNotFoundException, PersistenceException {
        PersistencePostgres persistence = getPersistenceConfig(null, null, true)
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        persistence.start();

        String submodelId = "http://example.org/submodel/nested";
        persistence.save(new DefaultSubmodel.Builder()
                .id(submodelId)
                .idShort("nestedSubmodel")
                .submodelElements(new DefaultSubmodelElementCollection.Builder()
                        .idShort("outer")
                        .value(new DefaultSubmodelElementCollection.Builder()
                                .idShort("inner")
                                .value(new DefaultProperty.Builder()
                                        .idShort("prop")
                                        .value("original")
                                        .valueType(DataTypeDefXsd.STRING)
                                        .build())
                                .build())
                        .build())
                .build());

        Reference propRef = new ReferenceBuilder()
                .submodel(submodelId)
                .element("outer")
                .element("inner")
                .element("prop")
                .build();

        SubmodelElement original = persistence.getSubmodelElement(propRef, QueryModifier.DEFAULT);
        Assert.assertEquals("original", ((Property) original).getValue());

        SubmodelElement updated = new DefaultProperty.Builder()
                .idShort("prop")
                .value("changed")
                .valueType(DataTypeDefXsd.STRING)
                .build();
        persistence.update(propRef, updated);
        Assert.assertEquals(updated, persistence.getSubmodelElement(propRef, QueryModifier.DEFAULT));

        persistence.deleteSubmodelElement(propRef);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.getSubmodelElement(propRef, QueryModifier.DEFAULT));

        persistence.stop();
    }


    @Test
    public void submodelElementOperationsOnNonExistentPathThrow()
            throws ConfigurationException, PersistenceException {
        PersistencePostgres persistence = getPersistenceConfig(null, null, true)
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        persistence.start();

        String submodelId = "http://example.org/submodel/empty";
        persistence.save(new DefaultSubmodel.Builder()
                .id(submodelId)
                .idShort("emptySubmodel")
                .build());

        Reference missingRef = new ReferenceBuilder()
                .submodel(submodelId)
                .element("doesNotExist")
                .build();
        SubmodelElement element = new DefaultProperty.Builder()
                .idShort("doesNotExist")
                .value("x")
                .valueType(DataTypeDefXsd.STRING)
                .build();

        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.getSubmodelElement(missingRef, QueryModifier.DEFAULT));
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.update(missingRef, element));
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.deleteSubmodelElement(missingRef));

        Reference missingSubmodelRef = new ReferenceBuilder()
                .submodel("http://example.org/submodel/missing")
                .element("doesNotExist")
                .build();
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.getSubmodelElement(missingSubmodelRef, QueryModifier.DEFAULT));
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.update(missingSubmodelRef, element));
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.deleteSubmodelElement(missingSubmodelRef));

        persistence.stop();
    }


    @Test
    public void concurrentUpdatesToDifferentElementsBothPersist() throws Exception {
        PersistencePostgres persistence = getPersistenceConfig(null, null, true)
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        persistence.start();

        String submodelId = "http://example.org/submodel/concurrent";
        persistence.save(new DefaultSubmodel.Builder()
                .id(submodelId)
                .idShort("concurrentSubmodel")
                .submodelElements(new DefaultProperty.Builder()
                        .idShort("p1")
                        .value("init")
                        .valueType(DataTypeDefXsd.STRING)
                        .build())
                .submodelElements(new DefaultProperty.Builder()
                        .idShort("p2")
                        .value("init")
                        .valueType(DataTypeDefXsd.STRING)
                        .build())
                .build());

        final int iterations = 25;
        CyclicBarrier barrier = new CyclicBarrier(2);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> first = executor.submit(() -> updateRepeatedly(persistence, submodelId, "p1", barrier, iterations));
            Future<?> second = executor.submit(() -> updateRepeatedly(persistence, submodelId, "p2", barrier, iterations));
            first.get(60, TimeUnit.SECONDS);
            second.get(60, TimeUnit.SECONDS);
        }
        finally {
            executor.shutdownNow();
        }

        String expected = "value" + (iterations - 1);
        Property p1 = (Property) persistence.getSubmodelElement(
                new ReferenceBuilder().submodel(submodelId).element("p1").build(), QueryModifier.DEFAULT);
        Property p2 = (Property) persistence.getSubmodelElement(
                new ReferenceBuilder().submodel(submodelId).element("p2").build(), QueryModifier.DEFAULT);
        Assert.assertEquals(expected, p1.getValue());
        Assert.assertEquals(expected, p2.getValue());

        persistence.stop();
    }


    private static void updateRepeatedly(PersistencePostgres persistence, String submodelId, String idShort, CyclicBarrier barrier, int iterations) {
        try {
            barrier.await();
            for (int i = 0; i < iterations; i++) {
                persistence.update(
                        new ReferenceBuilder().submodel(submodelId).element(idShort).build(),
                        new DefaultProperty.Builder()
                                .idShort(idShort)
                                .value("value" + i)
                                .valueType(DataTypeDefXsd.STRING)
                                .build());
            }
        }
        catch (Exception e) {
            throw new IllegalStateException("concurrent update failed", e);
        }
    }


    @Test
    public void findSubmodelElementsAcrossSubmodelsBySemanticId()
            throws ConfigurationException, ResourceNotFoundException, PersistenceException {
        PersistencePostgres persistence = getPersistenceConfig(null, null, true)
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        persistence.start();

        Reference semanticId = ReferenceBuilder.global("http://example.org/semantics/findAcrossSubmodels");
        SubmodelElement topLevel = new DefaultProperty.Builder()
                .idShort("topLevel")
                .semanticId(semanticId)
                .value("1")
                .valueType(DataTypeDefXsd.STRING)
                .build();
        SubmodelElement nested = new DefaultProperty.Builder()
                .idShort("nested")
                .semanticId(semanticId)
                .value("2")
                .valueType(DataTypeDefXsd.STRING)
                .build();
        SubmodelElement supplemental = new DefaultProperty.Builder()
                .idShort("supplemental")
                .supplementalSemanticIds(semanticId)
                .value("3")
                .valueType(DataTypeDefXsd.STRING)
                .build();
        persistence.save(new DefaultSubmodel.Builder()
                .id("http://example.org/submodel/semantic/1")
                .idShort("semanticSubmodel1")
                .submodelElements(DeepCopyHelper.deepCopy(topLevel, SubmodelElement.class))
                .submodelElements(new DefaultProperty.Builder()
                        .idShort("other")
                        .semanticId(ReferenceBuilder.global("http://example.org/semantics/other"))
                        .value("x")
                        .valueType(DataTypeDefXsd.STRING)
                        .build())
                .build());
        persistence.save(new DefaultSubmodel.Builder()
                .id("http://example.org/submodel/semantic/2")
                .idShort("semanticSubmodel2")
                .submodelElements(new DefaultSubmodelElementCollection.Builder()
                        .idShort("outer")
                        .value(DeepCopyHelper.deepCopy(nested, SubmodelElement.class))
                        .build())
                .submodelElements(DeepCopyHelper.deepCopy(supplemental, SubmodelElement.class))
                .build());

        Page<SubmodelElement> actual = persistence.findSubmodelElements(
                SubmodelElementSearchCriteria.builder()
                        .semanticId(semanticId)
                        .build(),
                QueryModifier.DEFAULT,
                PagingInfo.ALL);

        Assert.assertEquals(List.of(topLevel, nested, supplemental), actual.getContent());

        persistence.stop();
    }


    @Test
    public void submodelElementIndexTracksWrites() throws Exception {
        PersistencePostgres persistence = getPersistenceConfig(null, null, true)
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        persistence.start();

        String submodelId = "http://example.org/submodel/indexed";
        persistence.save(new DefaultSubmodel.Builder()
                .id(submodelId)
                .idShort("indexedSubmodel")
                .submodelElements(new DefaultProperty.Builder()
                        .idShort("speed")
                        .value("42.5")
                        .valueType(DataTypeDefXsd.DOUBLE)
                        .build())
                .submodelElements(new DefaultSubmodelElementCollection.Builder()
                        .idShort("data")
                        .value(new DefaultProperty.Builder()
                                .idShort("active")
                                .value("true")
                                .valueType(DataTypeDefXsd.BOOLEAN)
                                .build())
                        .build())
                .build());

        Assert.assertEquals(3, countIndexRows(submodelId));
        Assert.assertEquals(0, java.math.BigDecimal.valueOf(42.5).compareTo(queryIndexValueNum(submodelId, "speed")));
        Assert.assertEquals(Boolean.TRUE, queryIndexColumn(submodelId, "data.active", "value_bool", Boolean.class));

        persistence.update(
                new ReferenceBuilder().submodel(submodelId).element("speed").build(),
                new DefaultProperty.Builder()
                        .idShort("speed")
                        .value("99.9")
                        .valueType(DataTypeDefXsd.DOUBLE)
                        .build());
        Assert.assertEquals(0, java.math.BigDecimal.valueOf(99.9).compareTo(queryIndexValueNum(submodelId, "speed")));

        persistence.deleteSubmodelElement(new ReferenceBuilder().submodel(submodelId).element("speed").build());
        Assert.assertEquals(2, countIndexRows(submodelId));
        Assert.assertNull(queryIndexColumn(submodelId, "speed", "id_short", String.class));

        persistence.deleteSubmodel(submodelId);
        Assert.assertEquals(0, countIndexRows(submodelId));

        persistence.stop();
    }


    private static int countIndexRows(String submodelId) throws Exception {
        try (java.sql.Connection c = java.sql.DriverManager.getConnection(jdbcUrl);
                java.sql.PreparedStatement stmt = c.prepareStatement(
                        "SELECT COUNT(*) FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT_INDEX + " WHERE submodel_id = ?")) {
            stmt.setString(1, submodelId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }


    private static java.math.BigDecimal queryIndexValueNum(String submodelId, String idShortPath) throws Exception {
        return queryIndexColumn(submodelId, idShortPath, "value_num", java.math.BigDecimal.class);
    }


    private static <T> T queryIndexColumn(String submodelId, String idShortPath, String column, Class<T> type) throws Exception {
        try (java.sql.Connection c = java.sql.DriverManager.getConnection(jdbcUrl);
                java.sql.PreparedStatement stmt = c.prepareStatement(
                        "SELECT " + column + " FROM " + DatabaseSchema.TABLE_SUBMODEL_ELEMENT_INDEX + " WHERE submodel_id = ? AND id_short_path = ?")) {
            stmt.setString(1, submodelId);
            stmt.setString(2, idShortPath);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getObject(1, type) : null;
            }
        }
    }


    @Test
    public void blobValuesAreExternalizedAndRoundTrip() throws Exception {
        PersistencePostgres persistence = getPersistenceConfig(null, null, true)
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        persistence.start();

        String submodelId = "http://example.org/submodel/blob";
        byte[] content = new byte[100_000];
        new java.util.Random(42).nextBytes(content);
        persistence.save(new DefaultSubmodel.Builder()
                .id(submodelId)
                .idShort("blobSubmodel")
                .submodelElements(new DefaultBlob.Builder()
                        .idShort("doc")
                        .contentType("application/pdf")
                        .value(content)
                        .build())
                .submodelElements(new DefaultProperty.Builder()
                        .idShort("counter")
                        .value("0")
                        .valueType(DataTypeDefXsd.INT)
                        .build())
                .build());

        // blob content is stored in the blob store, not in the document
        Assert.assertEquals(1, countBlobRows(submodelId));
        Assert.assertTrue("document should not contain the blob content",
                documentSize(submodelId) < content.length);

        // default read strips the blob value, WITH_BLOB_VALUE returns the original content
        QueryModifier withBlob = new QueryModifier.Builder().extend(Extent.WITH_BLOB_VALUE).build();
        Reference blobRef = new ReferenceBuilder().submodel(submodelId).element("doc").build();
        Assert.assertNull(((Blob) persistence.getSubmodelElement(blobRef, QueryModifier.DEFAULT)).getValue());
        Assert.assertArrayEquals(content, ((Blob) persistence.getSubmodelElement(blobRef, withBlob)).getValue());
        Submodel submodel = persistence.getSubmodel(submodelId, withBlob);
        Assert.assertArrayEquals(content, ((Blob) submodel.getSubmodelElements().get(0)).getValue());

        // updating an unrelated element keeps the blob
        persistence.update(
                new ReferenceBuilder().submodel(submodelId).element("counter").build(),
                new DefaultProperty.Builder().idShort("counter").value("1").valueType(DataTypeDefXsd.INT).build());
        Assert.assertEquals(1, countBlobRows(submodelId));
        Assert.assertArrayEquals(content, ((Blob) persistence.getSubmodelElement(blobRef, withBlob)).getValue());

        // replacing the blob stores the new content and purges the old row
        byte[] newContent = new byte[50_000];
        new java.util.Random(7).nextBytes(newContent);
        persistence.update(blobRef, new DefaultBlob.Builder()
                .idShort("doc")
                .contentType("application/pdf")
                .value(newContent)
                .build());
        Assert.assertEquals(1, countBlobRows(submodelId));
        Assert.assertArrayEquals(newContent, ((Blob) persistence.getSubmodelElement(blobRef, withBlob)).getValue());

        // deleting the blob element purges the stored content
        persistence.deleteSubmodelElement(blobRef);
        Assert.assertEquals(0, countBlobRows(submodelId));

        persistence.stop();
    }


    private static int countBlobRows(String submodelId) throws Exception {
        try (java.sql.Connection c = java.sql.DriverManager.getConnection(jdbcUrl);
                java.sql.PreparedStatement stmt = c.prepareStatement(
                        "SELECT COUNT(*) FROM " + DatabaseSchema.TABLE_BLOB_STORE + " WHERE submodel_id = ?")) {
            stmt.setString(1, submodelId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }


    private static int documentSize(String submodelId) throws Exception {
        try (java.sql.Connection c = java.sql.DriverManager.getConnection(jdbcUrl);
                java.sql.PreparedStatement stmt = c.prepareStatement(
                        "SELECT length(content::text) FROM " + DatabaseSchema.TABLE_SUBMODEL + " WHERE id = ?")) {
            stmt.setString(1, submodelId);
            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }


    @Test
    public void testEnvironmentOverride()
            throws ConfigurationException, ResourceNotFoundException, PersistenceException {
        Environment environment = AASSimple.createEnvironment();

        // override = false
        PersistencePostgres noOverride = getPersistenceConfig(null, environment, false)
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        noOverride.start();
        Assert.assertThrows(ResourceNotFoundException.class, () -> noOverride.getAssetAdministrationShell(AASSimple.AAS_IDENTIFIER, QueryModifier.DEFAULT));
        noOverride.stop();

        // override = true
        PersistencePostgres override = getPersistenceConfig(null, environment, true)
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        override.start();
        var actual = override.getAssetAdministrationShell(AASSimple.AAS_IDENTIFIER, QueryModifier.DEFAULT);
        Assert.assertEquals(environment.getAssetAdministrationShells().get(0), actual);
        override.stop();
    }


    @Test
    public void putSubmodelElementNewInDeepSubmodelElementList()
            throws ResourceNotFoundException, ResourceNotAContainerElementException,
            ConfigurationException, PersistenceException, ResourceAlreadyExistsException {

        Environment environment = AASFull.createEnvironment();
        PersistencePostgres persistence = getPersistenceConfig(null, environment, true)
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        persistence.start();

        Reference parentRef = new ReferenceBuilder()
                .submodel("https://acplt.org/Test_Submodel_Mandatory")
                .element("ExampleSubmodelElementListUnordered")
                .build();

        SubmodelElementList list = EnvironmentHelper.resolve(parentRef, environment, SubmodelElementList.class);
        SubmodelElement newElement = DeepCopyHelper.deepCopy(list.getValue().get(0), SubmodelElement.class);
        newElement.setIdShort("newElement");

        persistence.insert(parentRef, newElement);

        SubmodelElementList expected = DeepCopyHelper.deepCopy(list, SubmodelElementList.class);
        expected.getValue().add(newElement);

        SubmodelElement actual = persistence.getSubmodelElement(
                parentRef,
                new QueryModifier.Builder().extend(Extent.WITH_BLOB_VALUE).build());

        Assert.assertEquals(expected, actual);

        persistence.stop();
    }
}
