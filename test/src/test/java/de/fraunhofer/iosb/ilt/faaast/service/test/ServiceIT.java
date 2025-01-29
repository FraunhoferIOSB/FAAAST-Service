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
package de.fraunhofer.iosb.ilt.faaast.service.test;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.EnvironmentSerializationManager;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.filestorage.memory.FileStorageInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.EnvironmentContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.serialization.DataFormat;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import java.io.File;
import java.io.IOException;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultEnvironment;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultFile;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultSubmodel;
import org.junit.Assert;
import org.junit.Test;


public class ServiceIT {

    @Test
    public void testLoadingAuxiliaryFilesFromInitialModel()
            throws ConfigurationException, AssetConnectionException, IOException, SerializationException, ResourceNotFoundException, PersistenceException {
        final String submodelId = "http://example.com/submodel/1";
        final String submodelIdShort = "submodel1";
        final String fileIdShort = "file1";
        final String filePath = "/foo/bar/file1.xyz";
        final byte[] fileContent = "hello world".getBytes();
        File aasxFile = File.createTempFile("model", ".aasx");
        aasxFile.deleteOnExit();
        EnvironmentContext model = EnvironmentContext.builder()
                .environment(new DefaultEnvironment.Builder()
                        .submodels(new DefaultSubmodel.Builder()
                                .id(submodelId)
                                .idShort(submodelIdShort)
                                .submodelElements(new DefaultFile.Builder()
                                        .idShort(fileIdShort)
                                        .contentType("application/octet-stream")
                                        .value(filePath)
                                        .build())
                                .build())
                        .build())
                .file(fileContent, filePath)
                .build();
        EnvironmentSerializationManager.serializerFor(DataFormat.AASX).write(aasxFile, model);
        ServiceConfig config = ServiceConfig.builder()
                .persistence(PersistenceInMemoryConfig.builder()
                        .initialModelFile(aasxFile)
                        .build())
                .fileStorage(FileStorageInMemoryConfig.builder().build())
                .messageBus(MessageBusInternalConfig.builder().build())
                .build();
        Service service = new Service(config);
        byte[] actual = service.getFileStorage().get(filePath);
        Assert.assertArrayEquals(fileContent, actual);
    }
}
