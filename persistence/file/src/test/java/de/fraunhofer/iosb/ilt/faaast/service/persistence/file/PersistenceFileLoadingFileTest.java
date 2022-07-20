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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.file;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.file.util.FileHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Identifiable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;


public class PersistenceFileLoadingFileTest {

    public static final String SRC_TEST_RESOURCES = "src/test/resources";
    private AssetAdministrationShellEnvironment environment;
    private Persistence persistence;
    private ServiceContext serviceContext;

    private static PersistenceFileConfig config;

    private static final String ENV_FILE = "src/test/resources/AASFull.json";

    public void init(PersistenceFileConfig persistenceFileConfig) throws ConfigurationException, AssetConnectionException {
        this.environment = AASFull.createEnvironment();
        this.persistence = new PersistenceFile();
        serviceContext = Mockito.mock(ServiceContext.class);
        config = persistenceFileConfig;
        persistence.init(CoreConfig.builder().build(),
                config,
                serviceContext);
    }


    private PersistenceFileConfig createPersistenceConfig(boolean loadOriginalFileOnStartup, boolean overrideOriginalFile) {
        return createPersistenceConfig(loadOriginalFileOnStartup, overrideOriginalFile, ENV_FILE);
    }


    private PersistenceFileConfig createPersistenceConfig(boolean loadOriginalFileOnStartup, boolean overrideOriginalFile, String modelPath) {
        return PersistenceFileConfig.builder()
                .modelPath(modelPath)
                .destination(SRC_TEST_RESOURCES)
                .loadOriginalFileOnStartup(loadOriginalFileOnStartup)
                .overrideOriginalModelFile(overrideOriginalFile)
                .build();
    }


    private void removeElementAndReloadPersistence(PersistenceFileConfig config) throws ConfigurationException, AssetConnectionException, ResourceNotFoundException {
        init(config);
        persistence.remove(environment.getAssetAdministrationShells().get(0).getIdentification());
        init(config);
    }


    @Test
    public void loadChangedFileTest() throws ResourceNotFoundException, ConfigurationException, AssetConnectionException {
        PersistenceFileConfig config = createPersistenceConfig(false, false);
        removeElementAndReloadPersistence(config);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), QueryModifier.DEFAULT));
    }


    @Test
    public void loadOriginalFileTest() throws ResourceNotFoundException, ConfigurationException, AssetConnectionException {
        PersistenceFileConfig config = createPersistenceConfig(true, false);
        removeElementAndReloadPersistence(config);
        Identifiable actual = persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), QueryModifier.DEFAULT);
        Identifiable expected = environment.getAssetAdministrationShells().get(0);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void loadInitialFileTest() throws ResourceNotFoundException, ConfigurationException, AssetConnectionException, IOException {
        File copied = new File(
                SRC_TEST_RESOURCES + "/AASFull_temp.json");
        File original = new File(ENV_FILE);
        FileUtils.copyFile(original, copied);

        PersistenceFileConfig config = createPersistenceConfig(false, true, copied.getPath());

        removeElementAndReloadPersistence(config);
        String path = Path.of(config.getDestination(), FileHelper.DEFAULT_FILENAME).toString();
        File f = new File(path);
        Assert.assertFalse(f.exists());
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.get(environment.getAssetAdministrationShells().get(0).getIdentification(), QueryModifier.DEFAULT));
    }


    @After
    public void cleanUp() throws IOException {
        Files.deleteIfExists(Path.of(config.getDestination(), FileHelper.FILENAME));
    }

}
