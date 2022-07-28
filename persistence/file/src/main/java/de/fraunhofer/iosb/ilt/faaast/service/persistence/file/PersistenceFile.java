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

import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.AbstractInMemoryPersistence;
import de.fraunhofer.iosb.ilt.faaast.service.util.AASEnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import io.adminshell.aas.v3.dataformat.DeserializationException;
import io.adminshell.aas.v3.dataformat.SerializationException;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Implementation of
 * {@link de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence} for a
 * file storage.
 * <p>
 * Following types are not supported in the current version:
 * <ul>
 * <li>AASX packages
 * <li>Package Descriptors
 * <li>SubmodelElementStructs
 * </ul>
 */
public class PersistenceFile extends AbstractInMemoryPersistence<PersistenceFileConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersistenceFile.class);

    @Override
    public void initAASEnvironment(PersistenceFileConfig config) {
        this.config = config;
        try {
            config.init();
            aasEnvironment = loadAASEnvironment();
            Path filePath = config.getFilePath().toAbsolutePath();
            if (aasEnvironment != null) {
                LOGGER.info("File Persistence uses existing model file {}", filePath);
            }
            else {
                LOGGER.info("File Persistence creates model file {}", filePath);
            }

            if (aasEnvironment == null) {
                if (config.getEnvironment() != null) {
                    aasEnvironment = config.isDecoupleEnvironment() ? DeepCopyHelper.deepCopy(config.getEnvironment()) : config.getEnvironment();
                }
                else {
                    aasEnvironment = AASEnvironmentHelper.fromFile(config.getInitialModel());
                }
                save();
            }
        }
        catch (DeserializationException | IOException e) {
            throw new IllegalArgumentException("Error deserializing AAS Environment", e);
        }
        identifiablePersistenceManager.setAasEnvironment(aasEnvironment);
        referablePersistenceManager.setAasEnvironment(aasEnvironment);
        packagePersistenceManager.setAasEnvironment(aasEnvironment);
    }


    private void save() {
        try {
            AASEnvironmentHelper.toFile(aasEnvironment, config.getDataformat(), new File(String.valueOf(config.getFilePath())));
        }
        catch (IOException | SerializationException e) {
            LOGGER.error(String.format("Could not save environment to file %s", config.getFilePath()), e);
        }
    }


    private AssetAdministrationShellEnvironment loadAASEnvironment() throws IOException, DeserializationException {
        File f = new File(config.getFilePath().toString());
        if (f.exists() && !f.isDirectory()) {
            return AASEnvironmentHelper.fromFile(f);
        }
        return null;
    }


    @Override
    public Identifiable put(Identifiable identifiable) {
        Identifiable element = super.put(identifiable);
        save();
        return element;
    }


    @Override
    public SubmodelElement put(Reference parent, Reference referenceToSubmodelElement, SubmodelElement submodelElement) throws ResourceNotFoundException {
        SubmodelElement element = super.put(parent, referenceToSubmodelElement, submodelElement);
        save();
        return element;
    }


    @Override
    public void remove(Identifier id) throws ResourceNotFoundException {
        super.remove(id);
        save();
    }


    @Override
    public void remove(Reference reference) throws ResourceNotFoundException {
        super.remove(reference);
        save();
    }


    @Override
    public void remove(String packageId) {
        super.remove(packageId);
        save();
    }
}
