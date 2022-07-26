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
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.file.util.FileHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Identifier;
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

    private static final String ENV_FILE_JSON = "src/test/resources/AASFull.json";
    private static final String ENV_FILE_XML = "src/test/resources/AASFull.xml";

    public void init(PersistenceFileConfig persistenceFileConfig) throws ConfigurationException, AssetConnectionException {
        this.environment = AASFull.createEnvironment();
        this.persistence = new PersistenceFile();
        ServiceContext serviceContext = Mockito.mock(ServiceContext.class);
        persistence.init(CoreConfig.builder().build(),
                persistenceFileConfig,
                serviceContext);
    }


    private PersistenceFileConfig createPersistenceConfig(boolean loadOriginalFileOnStartup, boolean overrideOriginalFile) {
        return createPersistenceConfig(loadOriginalFileOnStartup, overrideOriginalFile, ENV_FILE_JSON);
    }


    private PersistenceFileConfig createPersistenceConfig(boolean loadOriginalFileOnStartup, boolean overrideOriginalFile, String modelPath) {
        return PersistenceFileConfig.builder()
                .modelPath(modelPath)
                .destination(SRC_TEST_RESOURCES)
                .loadOriginalFileOnStartup(loadOriginalFileOnStartup)
                .overrideOriginalModelFile(overrideOriginalFile)
                .build();
    }


    private void removeElementAndReloadPersistence(PersistenceFileConfig config, Identifier identifier)
            throws ConfigurationException, AssetConnectionException, ResourceNotFoundException {
        persistence.remove(identifier);
        init(config);
    }


    @Test
    public void loadChangedFileTest() throws ResourceNotFoundException, ConfigurationException, AssetConnectionException {
        PersistenceFileConfig config = createPersistenceConfig(false, false);
        init(config);
        Identifier identifier = environment.getAssetAdministrationShells().get(0).getIdentification();
        removeElementAndReloadPersistence(config, identifier);
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.get(identifier, QueryModifier.DEFAULT));
    }


    @Test
    public void loadOriginalFileTest() throws ResourceNotFoundException, ConfigurationException, AssetConnectionException {
        PersistenceFileConfig config = createPersistenceConfig(true, false);
        init(config);
        Identifier identifier = environment.getAssetAdministrationShells().get(0).getIdentification();
        removeElementAndReloadPersistence(config, identifier);
        Identifiable actual = persistence.get(identifier, QueryModifier.DEFAULT);
        Identifiable expected = environment.getAssetAdministrationShells().get(0);
        Assert.assertEquals(expected, actual);
    }


    @Test
    public void overridingFileTest() throws ResourceNotFoundException, ConfigurationException, AssetConnectionException, IOException {
        File copied = new File(
                SRC_TEST_RESOURCES + "/AASFull_temp.json");
        File original = new File(ENV_FILE_JSON);
        FileUtils.copyFile(original, copied);

        PersistenceFileConfig config = createPersistenceConfig(false, true, copied.getPath());
        init(config);
        Identifier identifier = environment.getAssetAdministrationShells().get(0).getIdentification();
        removeElementAndReloadPersistence(config, identifier);
        String path = Path.of(config.getDestination(), FileHelper.DEFAULT_FILENAME).toString();
        File f = new File(path);
        Assert.assertFalse(f.exists());
        Assert.assertThrows(ResourceNotFoundException.class, () -> persistence.get(identifier, QueryModifier.DEFAULT));
    }


    @Test
    public void loadXMLFileTest() throws ResourceNotFoundException, ConfigurationException, AssetConnectionException, IOException {
        File copied = new File(ENV_FILE_XML);
        PersistenceFileConfig config = createPersistenceConfig(false, false, copied.getPath());
        init(config);
        String path = Path.of(config.getDestination(), FileHelper.DEFAULT_FILENAME_PREFIX + "." + "xml").toString();
        File f = new File(path);
        Assert.assertTrue(f.exists());
    }


    @Test
    public void desiredDataFormatTest() throws ResourceNotFoundException, ConfigurationException, AssetConnectionException, IOException {
        File copied = new File(ENV_FILE_XML);
        PersistenceFileConfig config = PersistenceFileConfig.builder()
                .modelPath(copied.getAbsolutePath())
                .destination(SRC_TEST_RESOURCES)
                .loadOriginalFileOnStartup(false)
                .overrideOriginalModelFile(false)
                .desiredDataformat(DataFormat.JSONLD)
                .build();
        init(config);
        String path = Path.of(config.getDestination(), FileHelper.DEFAULT_FILENAME_PREFIX + "." + "jsonld").toString();
        File f = new File(path);
        Assert.assertTrue(f.exists());
    }


    @After
    public void cleanUp() throws IOException {
        Files.deleteIfExists(((PersistenceFile) persistence).getFilePath());
    }

}
