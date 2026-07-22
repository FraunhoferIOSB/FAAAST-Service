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
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelElementSearchCriteria;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetKind;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultAssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultKey;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodelElementList;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;


public class PersistencePostgresPerformanceTest {

    private static final ServiceContext SERVICE_CONTEXT = Mockito.mock(ServiceContext.class);
    private static EmbeddedPostgres embeddedPostgres;
    private static String jdbcUrl;
    private static PrintWriter csvWriter;
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    private static final int WARMUP_ITERATIONS = 3;
    private static final int MEASUREMENT_ITERATIONS = 5;

    @BeforeClass
    public static void setUpDatabase() throws Exception {
        embeddedPostgres = EmbeddedPostgres.start();
        jdbcUrl = embeddedPostgres.getJdbcUrl("postgres", "postgres");
        System.out.println("Embedded PostgreSQL started on: " + jdbcUrl);

        String csvPath = "performance_results.csv";
        csvWriter = new PrintWriter(Files.newBufferedWriter(Paths.get(csvPath)));
        csvWriter.println("test_name,submodel_count,element_count,nesting_depth,avg_time_ms,memory_used_mb,iterations");
    }


    @AfterClass
    public static void tearDownDatabase() throws Exception {
        if (csvWriter != null) {
            csvWriter.flush();
            csvWriter.close();
        }
        if (embeddedPostgres != null) {
            embeddedPostgres.close();
        }
    }


    @Test
    public void testPerformanceDeeplyNestedAAS() throws Exception {
        System.out.println("\n=== Performance Test: Deeply Nested AAS ===");

        int[] nestingLevels = {
                3,
                5,
                7,
                8,
                9
        };
        int elementsPerLevel = 3;

        for (int depth: nestingLevels) {
            Environment environment = createDeeplyNestedAAS(depth, elementsPerLevel);
            PersistencePostgres persistence = createPersistence(environment, true);
            persistence.start();

            try {
                String submodelId = "deeply_nested_submodel";

                long totalTime = 0;
                long maxMemory = 0;

                for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                    Submodel submodel = persistence.getSubmodel(submodelId, QueryModifier.DEFAULT);
                    countElements(submodel);
                }

                System.gc();
                Thread.sleep(100);

                MemoryUsage beforeHeap = memoryBean.getHeapMemoryUsage();

                for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
                    long start = System.nanoTime();
                    Submodel submodel = persistence.getSubmodel(submodelId, QueryModifier.DEFAULT);
                    int elementCount = countElements(submodel);
                    long elapsed = System.nanoTime() - start;
                    totalTime += TimeUnit.NANOSECONDS.toMillis(elapsed);
                }

                MemoryUsage afterHeap = memoryBean.getHeapMemoryUsage();
                maxMemory = Math.max(beforeHeap.getUsed(), afterHeap.getUsed());

                double avgTime = (double) totalTime / MEASUREMENT_ITERATIONS;
                double memoryMB = maxMemory / (1024.0 * 1024.0);
                int totalElements = countElements(persistence.getSubmodel(submodelId, QueryModifier.DEFAULT));

                System.out.printf(Locale.US, "  Depth %d (elements %d): avg=%.2fms, memory=%.2fMB%n",
                        depth, totalElements, avgTime, memoryMB);

                csvWriter.format(Locale.US, "deeply_nested_aas,1,%d,%d,%.2f,%.2f,%d%n",
                        totalElements, depth, avgTime, memoryMB, MEASUREMENT_ITERATIONS);
                csvWriter.flush();

            }
            finally {
                persistence.stop();
            }
        }
    }


    private int countElements(Submodel submodel) {
        return countElementsRecursive(submodel.getSubmodelElements());
    }


    private int countElementsRecursive(List<SubmodelElement> elements) {
        if (elements == null) {
            return 0;
        }
        int count = elements.size();
        for (SubmodelElement element: elements) {
            if (element instanceof org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection) {
                count += countElementsRecursive(((org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection) element).getValue());
            }
            else if (element instanceof org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList) {
                count += countElementsRecursive(((org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList) element).getValue());
            }
        }
        return count;
    }


    @Test
    public void testPerformanceHighElementCount() throws Exception {
        System.out.println("\n=== Performance Test: High Element Count ===");

        int[] elementCounts = {
                100,
                500,
                1000,
                2000,
                5000
        };

        for (int elementCount: elementCounts) {
            Environment environment = createHighElementCountAAS(elementCount);
            PersistencePostgres persistence = createPersistence(environment, true);
            persistence.start();

            try {
                String submodelId = "high_element_submodel";

                SubmodelElementSearchCriteria criteria = new SubmodelElementSearchCriteria();
                criteria.setParent(SubmodelElementIdentifier.builder()
                        .submodelId(submodelId)
                        .build());

                long totalTime = 0;
                long maxMemory = 0;

                for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                    persistence.findSubmodelElements(criteria, QueryModifier.DEFAULT, PagingInfo.ALL);
                }

                System.gc();
                Thread.sleep(100);

                MemoryUsage beforeHeap = memoryBean.getHeapMemoryUsage();

                for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
                    long start = System.nanoTime();
                    Page<SubmodelElement> page = persistence.findSubmodelElements(
                            criteria, QueryModifier.DEFAULT, PagingInfo.ALL);
                    long elapsed = System.nanoTime() - start;
                    totalTime += TimeUnit.NANOSECONDS.toMillis(elapsed);
                }

                MemoryUsage afterHeap = memoryBean.getHeapMemoryUsage();
                maxMemory = Math.max(beforeHeap.getUsed(), afterHeap.getUsed());

                double avgTime = (double) totalTime / MEASUREMENT_ITERATIONS;
                double memoryMB = maxMemory / (1024.0 * 1024.0);

                System.out.printf(Locale.US, "  Elements %d: avg=%.2fms, memory=%.2fMB%n", elementCount, avgTime, memoryMB);

                csvWriter.format(Locale.US, "high_element_count,1,%d,1,%.2f,%.2f,%d%n",
                        elementCount, avgTime, memoryMB, MEASUREMENT_ITERATIONS);
                csvWriter.flush();

            }
            finally {
                persistence.stop();
            }
        }
    }


    @Test
    public void testPerformanceHighSubmodelCount() throws Exception {
        System.out.println("\n=== Performance Test: High Submodel Count ===");

        int[] submodelCounts = {
                10,
                50,
                100,
                200,
                500
        };
        int elementsPerSubmodel = 50;

        for (int submodelCount: submodelCounts) {
            Environment environment = createHighSubmodelCountAAS(submodelCount, elementsPerSubmodel);
            PersistencePostgres persistence = createPersistence(environment, true);
            persistence.start();

            try {
                long totalTime = 0;
                long maxMemory = 0;

                for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                    persistence.findSubmodels(new de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelSearchCriteria(),
                            QueryModifier.DEFAULT, PagingInfo.ALL);
                }

                System.gc();
                Thread.sleep(100);

                MemoryUsage beforeHeap = memoryBean.getHeapMemoryUsage();

                for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
                    long start = System.nanoTime();
                    Page<Submodel> page = persistence.findSubmodels(
                            new de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelSearchCriteria(),
                            QueryModifier.DEFAULT, PagingInfo.ALL);
                    long elapsed = System.nanoTime() - start;
                    totalTime += TimeUnit.NANOSECONDS.toMillis(elapsed);
                }

                MemoryUsage afterHeap = memoryBean.getHeapMemoryUsage();
                maxMemory = Math.max(beforeHeap.getUsed(), afterHeap.getUsed());

                double avgTime = (double) totalTime / MEASUREMENT_ITERATIONS;
                double memoryMB = maxMemory / (1024.0 * 1024.0);
                int totalElements = submodelCount * elementsPerSubmodel;

                System.out.printf(Locale.US, "  Submodels %d (elements %d): avg=%.2fms, memory=%.2fMB%n",
                        submodelCount, totalElements, avgTime, memoryMB);

                csvWriter.format(Locale.US, "high_submodel_count,%d,%d,1,%.2f,%.2f,%d%n",
                        submodelCount, totalElements, avgTime, memoryMB, MEASUREMENT_ITERATIONS);
                csvWriter.flush();

            }
            finally {
                persistence.stop();
            }
        }
    }


    @Test
    public void testPerformancePagingOperations() throws Exception {
        System.out.println("\n=== Performance Test: Paging Operations ===");

        int[] pageSizes = {
                10,
                50,
                100,
                200
        };
        int totalElements = 1000;

        for (int pageSize: pageSizes) {
            Environment environment = createHighElementCountAAS(totalElements);
            PersistencePostgres persistence = createPersistence(environment, true);
            persistence.start();

            try {
                String submodelId = "high_element_submodel";

                SubmodelElementSearchCriteria criteria = new SubmodelElementSearchCriteria();
                criteria.setParent(SubmodelElementIdentifier.builder()
                        .submodelId(submodelId)
                        .build());

                long totalTime = 0;
                int pagesLoaded = 0;
                long maxMemory = 0;

                for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                    String cursor = null;
                    do {
                        PagingInfo paging = PagingInfo.builder()
                                .limit(pageSize)
                                .cursor(cursor)
                                .build();
                        Page<SubmodelElement> page = persistence.findSubmodelElements(
                                criteria, QueryModifier.DEFAULT, paging);
                        cursor = page.getMetadata() != null ? page.getMetadata().getCursor() : null;
                        pagesLoaded++;
                    } while (cursor != null);
                }

                System.gc();
                Thread.sleep(100);

                MemoryUsage beforeHeap = memoryBean.getHeapMemoryUsage();

                for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
                    long start = System.nanoTime();
                    String cursor = null;
                    int loadedPages = 0;
                    do {
                        PagingInfo paging = PagingInfo.builder()
                                .limit(pageSize)
                                .cursor(cursor)
                                .build();
                        Page<SubmodelElement> page = persistence.findSubmodelElements(
                                criteria, QueryModifier.DEFAULT, paging);
                        cursor = page.getMetadata() != null ? page.getMetadata().getCursor() : null;
                        loadedPages++;
                    } while (cursor != null);
                    long elapsed = System.nanoTime() - start;
                    totalTime += TimeUnit.NANOSECONDS.toMillis(elapsed);
                    pagesLoaded = loadedPages;
                }

                MemoryUsage afterHeap = memoryBean.getHeapMemoryUsage();
                maxMemory = Math.max(beforeHeap.getUsed(), afterHeap.getUsed());

                double avgTime = (double) totalTime / MEASUREMENT_ITERATIONS;
                double memoryMB = maxMemory / (1024.0 * 1024.0);
                double avgTimePerPage = avgTime / pagesLoaded;

                System.out.printf(Locale.US, "  PageSize %d (pages=%d): total=%.2fms, perPage=%.3fms, memory=%.2fMB%n",
                        pageSize, pagesLoaded, avgTime, avgTimePerPage, memoryMB);

                csvWriter.format(Locale.US, "paging_operations,1,%d,1,%.2f,%.2f,%d%n",
                        totalElements, avgTime, memoryMB, MEASUREMENT_ITERATIONS);
                csvWriter.flush();

            }
            finally {
                persistence.stop();
            }
        }
    }


    @Test
    public void testPerformanceSingleElementAccess() throws Exception {
        System.out.println("\n=== Performance Test: Single Element Access ===");

        int[] elementCounts = {
                100,
                1000,
                5000
        };

        for (int elementCount: elementCounts) {
            Environment environment = createHighElementCountAAS(elementCount);
            PersistencePostgres persistence = createPersistence(environment, true);
            persistence.start();

            try {
                String submodelId = "high_element_submodel";
                String targetIdShort = "Property_" + (elementCount / 2);

                SubmodelElementIdentifier identifier = SubmodelElementIdentifier.builder()
                        .submodelId(submodelId)
                        .idShortPath(IdShortPath.parse(targetIdShort))
                        .build();

                long totalTime = 0;
                long maxMemory = 0;

                for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                    try {
                        persistence.getSubmodelElement(identifier, QueryModifier.DEFAULT);
                    }
                    catch (ResourceNotFoundException e) {}
                }

                System.gc();
                Thread.sleep(100);

                MemoryUsage beforeHeap = memoryBean.getHeapMemoryUsage();

                for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
                    long start = System.nanoTime();
                    try {
                        persistence.getSubmodelElement(identifier, QueryModifier.DEFAULT);
                    }
                    catch (ResourceNotFoundException e) {}
                    long elapsed = System.nanoTime() - start;
                    totalTime += TimeUnit.NANOSECONDS.toMillis(elapsed);
                }

                MemoryUsage afterHeap = memoryBean.getHeapMemoryUsage();
                maxMemory = Math.max(beforeHeap.getUsed(), afterHeap.getUsed());

                double avgTime = (double) totalTime / MEASUREMENT_ITERATIONS;
                double memoryMB = maxMemory / (1024.0 * 1024.0);

                System.out.printf(Locale.US, "  Elements %d (access middle): avg=%.2fms, memory=%.2fMB%n",
                        elementCount, avgTime, memoryMB);

                csvWriter.format(Locale.US, "single_element_access,1,%d,1,%.2f,%.2f,%d%n",
                        elementCount, avgTime, memoryMB, MEASUREMENT_ITERATIONS);
                csvWriter.flush();

            }
            finally {
                persistence.stop();
            }
        }
    }


    @Test
    public void testMemoryUsageUnderLoad() throws Exception {
        System.out.println("\n=== Memory Usage Test: Under Load ===");

        int elementCount = 2000;
        int operations = 100;

        Environment environment = createHighElementCountAAS(elementCount);
        PersistencePostgres persistence = createPersistence(environment, true);
        persistence.start();

        try {
            String submodelId = "high_element_submodel";

            SubmodelElementSearchCriteria criteria = new SubmodelElementSearchCriteria();
            criteria.setParent(SubmodelElementIdentifier.builder()
                    .submodelId(submodelId)
                    .build());

            System.gc();
            Thread.sleep(500);

            MemoryUsage initialHeap = memoryBean.getHeapMemoryUsage();
            long initialMemory = initialHeap.getUsed();

            System.out.printf(Locale.US, "  Initial memory: %.2f MB%n", initialMemory / (1024.0 * 1024.0));

            List<Long> memorySnapshots = new ArrayList<>();

            for (int i = 0; i < operations; i++) {
                Page<SubmodelElement> page = persistence.findSubmodelElements(
                        criteria, QueryModifier.DEFAULT, PagingInfo.ALL);

                if (i % 10 == 0) {
                    MemoryUsage currentHeap = memoryBean.getHeapMemoryUsage();
                    long usedMemory = currentHeap.getUsed();
                    memorySnapshots.add(usedMemory);
                    System.out.printf(Locale.US, "    Operation %d: %.2f MB%n", i, usedMemory / (1024.0 * 1024.0));
                }
            }

            MemoryUsage finalHeap = memoryBean.getHeapMemoryUsage();
            long peakMemory = memorySnapshots.stream().max(Long::compare).orElse(0L);
            long finalMemory = finalHeap.getUsed();
            long memoryIncrease = finalMemory - initialMemory;

            System.out.printf(Locale.US, "  Final memory: %.2f MB%n", finalMemory / (1024.0 * 1024.0));
            System.out.printf(Locale.US, "  Memory increase: %.2f MB%n", memoryIncrease / (1024.0 * 1024.0));
            System.out.printf(Locale.US, "  Peak memory: %.2f MB%n", peakMemory / (1024.0 * 1024.0));

            csvWriter.format(Locale.US, "memory_under_load,1,%d,1,%.2f,%.2f,%d%n",
                    elementCount, 0.0, peakMemory / (1024.0 * 1024.0), operations);
            csvWriter.flush();

        }
        finally {
            persistence.stop();
        }
    }


    @Test
    public void testFullWorkloadScenario() throws Exception {
        System.out.println("\n=== Full Workload Scenario Test ===");

        int submodelCount = 50;
        int elementsPerSubmodel = 100;
        int nestingDepth = 5;

        Environment environment = createFullWorkloadEnvironment(submodelCount, elementsPerSubmodel, nestingDepth);
        PersistencePostgres persistence = createPersistence(environment, true);
        persistence.start();

        try {
            long totalTime = 0;
            long maxMemory = 0;
            int totalElements = submodelCount * elementsPerSubmodel;

            System.gc();
            Thread.sleep(500);
            MemoryUsage beforeHeap = memoryBean.getHeapMemoryUsage();

            long startAll = System.nanoTime();

            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                persistence.findSubmodels(
                        new de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelSearchCriteria(),
                        QueryModifier.DEFAULT, PagingInfo.ALL);
            }

            for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
                long start = System.nanoTime();

                persistence.findSubmodels(
                        new de.fraunhofer.iosb.ilt.faaast.service.persistence.SubmodelSearchCriteria(),
                        QueryModifier.DEFAULT, PagingInfo.ALL);

                String submodelId = "submodel_25";
                SubmodelElementSearchCriteria criteria = new SubmodelElementSearchCriteria();
                criteria.setParent(SubmodelElementIdentifier.builder()
                        .submodelId(submodelId)
                        .build());
                persistence.findSubmodelElements(criteria, QueryModifier.DEFAULT, PagingInfo.ALL);

                long elapsed = System.nanoTime() - start;
                totalTime += TimeUnit.NANOSECONDS.toMillis(elapsed);
            }

            long totalElapsed = System.nanoTime() - startAll;

            MemoryUsage afterHeap = memoryBean.getHeapMemoryUsage();
            maxMemory = Math.max(beforeHeap.getUsed(), afterHeap.getUsed());

            double avgTime = (double) totalTime / MEASUREMENT_ITERATIONS;
            double totalTimeSec = totalElapsed / 1_000_000_000.0;
            double memoryMB = maxMemory / (1024.0 * 1024.0);

            System.out.printf(Locale.US, "  Submodels: %d, Elements: %d, Depth: %d%n",
                    submodelCount, totalElements, nestingDepth);
            System.out.printf(Locale.US, "  Average time per iteration: %.2f ms%n", avgTime);
            System.out.printf(Locale.US, "  Total time for %d iterations: %.2f s%n", MEASUREMENT_ITERATIONS, totalTimeSec);
            System.out.printf(Locale.US, "  Peak memory: %.2f MB%n", memoryMB);

            csvWriter.format(Locale.US, "full_workload,%d,%d,%d,%.2f,%.2f,%d%n",
                    submodelCount, totalElements, nestingDepth, avgTime, memoryMB, MEASUREMENT_ITERATIONS);
            csvWriter.flush();

        }
        finally {
            persistence.stop();
        }
    }


    private PersistencePostgres createPersistence(Environment environment, boolean override)
            throws ConfigurationInitializationException {
        try {
            PersistencePostgresConfig config = PersistencePostgresConfig.builder()
                    .initialModel(environment)
                    .jdbc(jdbcUrl)
                    .username("postgres")
                    .password("")
                    .override(override)
                    .build();
            return (PersistencePostgres) config.newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        }
        catch (Exception e) {
            throw new ConfigurationInitializationException("Failed to create Postgres config", e);
        }
    }


    private Environment createDeeplyNestedAAS(int depth, int elementsPerLevel) {
        List<SubmodelElement> nestedElements = createNestedElements(depth, elementsPerLevel);

        Submodel submodel = new DefaultSubmodel.Builder()
                .idShort("deeply_nested_submodel")
                .id("deeply_nested_submodel")
                .submodelElements(nestedElements)
                .build();

        AssetAdministrationShell shell = new DefaultAssetAdministrationShell.Builder()
                .idShort("DeepShell")
                .id("https://example.org/DeepShell")
                .assetInformation(new DefaultAssetInformation.Builder()
                        .assetKind(AssetKind.INSTANCE)
                        .globalAssetId("https://example.org/DeepAsset")
                        .build())
                .submodels(new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.SUBMODEL)
                                .value("deeply_nested_submodel")
                                .build())
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .build())
                .build();

        List<ConceptDescription> conceptDescriptions = new ArrayList<>();
        for (int i = 0; i < depth * elementsPerLevel; i++) {
            conceptDescriptions.add(createConceptDescription("Concept_" + i));
        }

        return new DefaultEnvironment.Builder()
                .assetAdministrationShells(shell)
                .submodels(submodel)
                .conceptDescriptions(conceptDescriptions)
                .build();
    }


    private List<SubmodelElement> createNestedElements(int depth, int elementsPerLevel) {
        List<SubmodelElement> elements = new ArrayList<>();

        if (depth == 0) {
            for (int i = 0; i < elementsPerLevel; i++) {
                elements.add(createProperty("Property_" + i, "Value_" + i));
            }
            return elements;
        }

        for (int i = 0; i < elementsPerLevel; i++) {
            List<SubmodelElement> childElements = createNestedElements(depth - 1, elementsPerLevel);

            if (i % 2 == 0) {
                DefaultSubmodelElementCollection collection = new DefaultSubmodelElementCollection.Builder()
                        .idShort("Collection_Level" + depth + "_" + i)
                        .value(childElements)
                        .build();
                elements.add(collection);
            }
            else {
                DefaultSubmodelElementList list = new DefaultSubmodelElementList.Builder()
                        .idShort("List_Level" + depth + "_" + i)
                        .value(childElements)
                        .build();
                elements.add(list);
            }
        }

        return elements;
    }


    private String buildDeepPath(int depth) {
        StringBuilder path = new StringBuilder();
        for (int i = 0; i <= depth; i++) {
            if (i > 0) {
                path.append(i % 2 == 0 ? "/[" : "/");
            }
            path.append(i % 2 == 0 ? i : "Collection_Level" + (depth - i));
        }
        return path.toString().replaceFirst("/", "");
    }


    private Environment createHighElementCountAAS(int elementCount) {
        List<SubmodelElement> elements = new ArrayList<>();

        for (int i = 0; i < elementCount; i++) {
            elements.add(createProperty("Property_" + i, "Value_" + i));
        }

        Submodel submodel = new DefaultSubmodel.Builder()
                .idShort("high_element_submodel")
                .id("high_element_submodel")
                .submodelElements(elements)
                .build();

        AssetAdministrationShell shell = new DefaultAssetAdministrationShell.Builder()
                .idShort("HighElementShell")
                .id("https://example.org/HighElementShell")
                .assetInformation(new DefaultAssetInformation.Builder()
                        .assetKind(AssetKind.INSTANCE)
                        .globalAssetId("https://example.org/HighElementAsset")
                        .build())
                .submodels(new DefaultReference.Builder()
                        .keys(new DefaultKey.Builder()
                                .type(KeyTypes.SUBMODEL)
                                .value("high_element_submodel")
                                .build())
                        .type(ReferenceTypes.EXTERNAL_REFERENCE)
                        .build())
                .build();

        List<ConceptDescription> conceptDescriptions = new ArrayList<>();
        for (int i = 0; i < elementCount; i++) {
            conceptDescriptions.add(createConceptDescription("PropertyConcept_" + i));
        }

        return new DefaultEnvironment.Builder()
                .assetAdministrationShells(shell)
                .submodels(submodel)
                .conceptDescriptions(conceptDescriptions)
                .build();
    }


    private Environment createHighSubmodelCountAAS(int submodelCount, int elementsPerSubmodel) {
        List<Submodel> submodels = new ArrayList<>();
        List<Reference> submodelRefs = new ArrayList<>();
        List<ConceptDescription> conceptDescriptions = new ArrayList<>();

        for (int i = 0; i < submodelCount; i++) {
            String submodelId = "submodel_" + i;

            List<SubmodelElement> elements = new ArrayList<>();
            for (int j = 0; j < elementsPerSubmodel; j++) {
                elements.add(createProperty("Property_" + j, "Value_" + j));
                conceptDescriptions.add(createConceptDescription("PropertyConcept_" + i + "_" + j));
            }

            Submodel submodel = new DefaultSubmodel.Builder()
                    .idShort("Submodel_" + i)
                    .id(submodelId)
                    .submodelElements(elements)
                    .build();
            submodels.add(submodel);

            submodelRefs.add(new DefaultReference.Builder()
                    .keys(new DefaultKey.Builder()
                            .type(KeyTypes.SUBMODEL)
                            .value(submodelId)
                            .build())
                    .type(ReferenceTypes.EXTERNAL_REFERENCE)
                    .build());
        }

        AssetAdministrationShell shell = new DefaultAssetAdministrationShell.Builder()
                .idShort("MultiSubmodelShell")
                .id("https://example.org/MultiSubmodelShell")
                .assetInformation(new DefaultAssetInformation.Builder()
                        .assetKind(AssetKind.INSTANCE)
                        .globalAssetId("https://example.org/MultiSubmodelAsset")
                        .build())
                .submodels(submodelRefs)
                .build();

        return new DefaultEnvironment.Builder()
                .assetAdministrationShells(shell)
                .submodels(submodels)
                .conceptDescriptions(conceptDescriptions)
                .build();
    }


    private Environment createFullWorkloadEnvironment(int submodelCount, int elementsPerSubmodel, int nestingDepth) {
        List<Submodel> submodels = new ArrayList<>();
        List<Reference> submodelRefs = new ArrayList<>();
        List<ConceptDescription> conceptDescriptions = new ArrayList<>();

        for (int i = 0; i < submodelCount; i++) {
            String submodelId = "submodel_" + i;

            List<SubmodelElement> elements = new ArrayList<>();

            for (int j = 0; j < elementsPerSubmodel; j++) {
                if (j < nestingDepth && nestingDepth > 0) {
                    List<SubmodelElement> nested = createNestedElements(nestingDepth - 1, 3);
                    DefaultSubmodelElementCollection collection = new DefaultSubmodelElementCollection.Builder()
                            .idShort("NestedCollection_" + j)
                            .value(nested)
                            .build();
                    elements.add(collection);
                }
                else {
                    elements.add(createProperty("Property_" + j, "Value_" + j));
                }
                conceptDescriptions.add(createConceptDescription("PropertyConcept_" + i + "_" + j));
            }

            Submodel submodel = new DefaultSubmodel.Builder()
                    .idShort("Submodel_" + i)
                    .id(submodelId)
                    .submodelElements(elements)
                    .build();
            submodels.add(submodel);

            submodelRefs.add(new DefaultReference.Builder()
                    .keys(new DefaultKey.Builder()
                            .type(KeyTypes.SUBMODEL)
                            .value(submodelId)
                            .build())
                    .type(ReferenceTypes.EXTERNAL_REFERENCE)
                    .build());
        }

        AssetAdministrationShell shell = new DefaultAssetAdministrationShell.Builder()
                .idShort("FullWorkloadShell")
                .id("https://example.org/FullWorkloadShell")
                .assetInformation(new DefaultAssetInformation.Builder()
                        .assetKind(AssetKind.INSTANCE)
                        .globalAssetId("https://example.org/FullWorkloadAsset")
                        .build())
                .submodels(submodelRefs)
                .build();

        return new DefaultEnvironment.Builder()
                .assetAdministrationShells(shell)
                .submodels(submodels)
                .conceptDescriptions(conceptDescriptions)
                .build();
    }


    private DefaultProperty createProperty(String idShort, String value) {
        return new DefaultProperty.Builder()
                .idShort(idShort)
                .value(value)
                .valueType(DataTypeDefXsd.STRING)
                .build();
    }


    private ConceptDescription createConceptDescription(String id) {
        return new DefaultConceptDescription.Builder()
                .id(id)
                .idShort(id)
                .build();
    }
}
