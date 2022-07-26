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

import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.PersistenceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.PersistenceSuperTest;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.file.util.FileHelper;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.AfterClass;


public class PersistenceFileTest extends PersistenceSuperTest {

    @Override
    public Persistence getPersistenceImplementation() {
        return new PersistenceFile();
    }


    @Override
    public AssetAdministrationShellEnvironment getEnvironment() {
        return AASFull.createEnvironment();
    }


    @Override
    public PersistenceConfig getPersistenceConfig() {
        return PersistenceFileConfig.builder()
                .environment(getEnvironment())
                .loadOriginalFileOnStartup(true)
                .build();
    }


    @AfterClass
    public static void cleanUp() throws IOException {
        Files.deleteIfExists(Path.of(PersistenceFileConfig.builder()
                .build().getDestination(), FileHelper.DEFAULT_FILENAME));
    }

}
