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
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AbstractPersistenceTest;
import de.fraunhofer.iosb.ilt.faaast.service.util.FileHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import org.bouncycastle.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


public class PersistenceFileTest extends AbstractPersistenceTest<PersistenceFile, PersistenceFileConfig> {

    private static final File RESOURCE_MODEL_FILE_JSON = new File(Thread.currentThread().getContextClassLoader().getResource("model.json").getFile());
    private static final File RESOURCE_MODEL_FILE_XML = new File(Thread.currentThread().getContextClassLoader().getResource("model.xml").getFile());
    private static final ServiceContext SERVICE_CONTEXT = Mockito.mock(ServiceContext.class);

    private File modelFileJson;
    private File modelFileXml;
    private Environment model;
    private static Path tempDir;

    static {
        try {
            tempDir = Files.createTempDirectory("faaast-temp");
        }
        catch (IOException ex) {
            Assert.fail();
        }
    }

    @Override
    public PersistenceFileConfig getPersistenceConfig(File initialModelFile, Environment initialModel) throws ConfigurationInitializationException {
        PersistenceFileConfig result = PersistenceFileConfig
                .builder()
                .initialModel(initialModel)
                .initialModelFile(initialModelFile)
                .dataDir(tempDir.toString())
                .build();
        result.init();
        return result;
    }


    @Before
    public void initialize() throws Exception {
        model = AASFull.createEnvironment();
        modelFileJson = copyToTempDir(RESOURCE_MODEL_FILE_JSON);
        modelFileXml = copyToTempDir(RESOURCE_MODEL_FILE_XML);
    }


    private File copyToTempDir(File baseFile) throws IOException {
        Path result = Files.createTempFile(
                tempDir,
                FileHelper.getFilenameWithoutExtension(baseFile),
                FileHelper.getFileExtensionWithSeparator(baseFile));
        Files.copy(baseFile.toPath(), result, StandardCopyOption.REPLACE_EXISTING);
        return result.toFile();
    }


    private File getDefaultModelFile() throws ConfigurationInitializationException {
        PersistenceFileConfig config = PersistenceFileConfig.builder().build();
        config.init();
        return config.getFilePath().toFile();
    }


    @Test
    public void testDefaultDataDir() throws ConfigurationException {
        PersistenceFileConfig.builder()
                .initialModelFile(modelFileJson)
                .keepInitial(true)
                .build()
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        File modelFile = getDefaultModelFile();
        modelFile.deleteOnExit();
        Assert.assertTrue(modelFile.exists());
    }


    @Test
    public void testCustomDataDir() throws ConfigurationException {
        PersistenceFileConfig.builder()
                .initialModelFile(modelFileJson)
                .dataDir(tempDir.toString())
                .keepInitial(true)
                .build()
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        File modelFile = Paths
                .get(tempDir.toString(), PersistenceFileConfig.DEFAULT_FILENAME_PREFIX + ".json")
                .toFile();
        Assert.assertTrue(modelFile.exists());
    }


    @Test
    public void testInvalidDataDir() throws ConfigurationException {
        Assert.assertThrows(ConfigurationInitializationException.class, () -> PersistenceFileConfig.builder()
                .initialModelFile(modelFileJson)
                .dataDir("[/:/]")
                .keepInitial(true)
                .build()
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT));
    }


    @Test
    public void testOverrideInitial() throws ResourceNotFoundException, ConfigurationException, AssetConnectionException, IOException {
        PersistenceFileConfig config = PersistenceFileConfig.builder()
                .initialModelFile(modelFileJson)
                .keepInitial(false)
                .build();
        PersistenceFile persistence = config.newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        String identifier = model.getAssetAdministrationShells().get(0).getId();
        persistence.deleteAssetAdministrationShell(identifier);
        PersistenceFile newPersistence = config.newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        Assert.assertEquals(2,
                Files.list(modelFileJson.getParentFile().toPath())
                        .filter(file -> !Files.isDirectory(file))
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .filter(x -> x.endsWith(".json"))
                        .count());
        Assert.assertThrows(ResourceNotFoundException.class, () -> newPersistence.getAssetAdministrationShell(identifier, QueryModifier.DEFAULT));
    }


    @Test
    public void testLoadXml() throws ConfigurationException, AssetConnectionException {
        PersistenceFileConfig.builder()
                .initialModelFile(modelFileXml)
                .dataDir(tempDir.toString())
                .keepInitial(true)
                .build()
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        File persistenceModelFile = Paths
                .get(tempDir.toString(), PersistenceFileConfig.DEFAULT_FILENAME_PREFIX + ".xml")
                .toFile();
        Assert.assertTrue(persistenceModelFile.exists());
    }


    @Test
    public void testUsingDifferentDataFormat() throws ConfigurationException, AssetConnectionException {
        PersistenceFileConfig.builder()
                .initialModelFile(modelFileXml)
                .dataDir(tempDir.toString())
                .keepInitial(true)
                .dataformat(DataFormat.JSON)
                .build()
                .newInstance(CoreConfig.DEFAULT, SERVICE_CONTEXT);
        File persistenceModelFile = Paths
                .get(tempDir.toString(), PersistenceFileConfig.DEFAULT_FILENAME_PREFIX + ".json")
                .toFile();
        Assert.assertTrue(persistenceModelFile.exists());
    }


    @After
    public void deleteTempFiles() throws IOException {
        Files.walk(tempDir)
                .sorted(Comparator.reverseOrder())
                .filter(x -> !Objects.areEqual(x, tempDir))
                .map(Path::toFile)
                .forEach(File::delete);
    }

}
