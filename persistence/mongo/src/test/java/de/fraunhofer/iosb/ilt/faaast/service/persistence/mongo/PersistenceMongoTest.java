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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.mongo;

import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.StateID;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.Transitions;
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
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.io.File;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


/**
 * Tests for the mongo database persistence implementation.
 */
public class PersistenceMongoTest extends AbstractPersistenceTest<PersistenceMongo, PersistenceMongoConfig> {

    private static final ServiceContext SERVICE_CONTEXT = Mockito.mock(ServiceContext.class);
    private static TransitionWalker.ReachedState<RunningMongodProcess> runningProcess;
    private static de.flapdoodle.embed.mongo.commands.ServerAddress serverAddress = null;

    @Override
    public PersistenceMongoConfig getPersistenceConfig(File initialModelFile, Environment initialModel) throws ConfigurationInitializationException {
        return getPersistenceConfig(initialModelFile, initialModel, true);
    }


    public PersistenceMongoConfig getPersistenceConfig(File initialModelFile, Environment initialModel, boolean override) throws ConfigurationInitializationException {
        try {
            if (runningProcess == null)
                startEmbeddedMongoDB();
            return PersistenceMongoConfig
                    .builder()
                    .initialModel(initialModel)
                    .initialModelFile(initialModelFile)
                    .connectionString("mongodb://" + serverAddress.getHost() + ":" + serverAddress.getPort())
                    .database("faaast")
                    .override(override)
                    .build();
        }
        catch (Exception e) {
            throw new ConfigurationInitializationException(e);
        }
    }


    private void startEmbeddedMongoDB() {
        Transitions transitions = Mongod.instance().transitions(Version.Main.V8_0);
        runningProcess = transitions.walker()
                .initState(StateID.of(RunningMongodProcess.class));
        serverAddress = runningProcess.current().getServerAddress();
    }


    @AfterClass
    public static void cleanup() {
        runningProcess.close();
    }


    @Test
    public void testEnvironmentOverride() throws ConfigurationException, ResourceNotFoundException, PersistenceException {
        Environment environment = AASSimple.createEnvironment();
        Persistence noOverridePersistence = getPersistenceConfig(null, environment, false).newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        noOverridePersistence.start();
        Assert.assertThrows(ResourceNotFoundException.class, () -> {
            noOverridePersistence.getAssetAdministrationShell(AASSimple.AAS_IDENTIFIER, QueryModifier.DEFAULT);
        });
        noOverridePersistence.stop();
        Persistence overridePersistence = getPersistenceConfig(null, environment, true).newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        overridePersistence.start();
        AssetAdministrationShell expected = environment.getAssetAdministrationShells().get(0);
        AssetAdministrationShell actual = overridePersistence.getAssetAdministrationShell(AASSimple.AAS_IDENTIFIER, QueryModifier.DEFAULT);
        Assert.assertEquals(expected, actual);
        overridePersistence.stop();
    }


    @Test
    public void putSubmodelElementNewInDeepSubmodelElementList()
            throws ResourceNotFoundException, ResourceNotAContainerElementException, ConfigurationException, PersistenceException, ResourceAlreadyExistsException {
        Environment environment = AASFull.createEnvironment();
        Persistence persistence = getPersistenceConfig(null, environment, true).newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        persistence.start();
        Reference reference = new ReferenceBuilder()
                .submodel("https://acplt.org/Test_Submodel_Mandatory")
                .element("ExampleSubmodelElementListUnordered")
                .build();
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
        persistence.stop();
    }
}
