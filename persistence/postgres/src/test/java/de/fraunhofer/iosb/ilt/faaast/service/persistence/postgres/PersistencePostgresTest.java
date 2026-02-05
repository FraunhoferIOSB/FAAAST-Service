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
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceAlreadyExistsException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotAContainerElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AbstractPersistenceTest;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.File;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
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
