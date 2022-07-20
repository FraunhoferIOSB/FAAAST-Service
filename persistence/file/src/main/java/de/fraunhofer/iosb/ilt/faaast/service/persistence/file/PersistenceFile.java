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
import de.fraunhofer.iosb.ilt.faaast.service.persistence.PersistenceBasic;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.file.util.FileHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.AASEnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import io.adminshell.aas.v3.dataformat.DeserializationException;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.io.File;
import java.io.IOException;


/**
 * Implementation of
 * {@link de.fraunhofer.iosb.ilt.faaast.service.persistence.Persistence} for a file storage.
 * <p>
 * Following types are not supported in the
 * current version:
 * <ul>
 * <li>AASX packages
 * <li>Package Descriptors
 * <li>SubmodelElementStructs
 * </ul>
 */
public class PersistenceFile extends PersistenceBasic<PersistenceFileConfig> {

    private FileHelper fileHelper;

    @Override
    public void initAASEnvironment(PersistenceFileConfig config) {
        fileHelper = new FileHelper(config);
        if (!config.isLoadOriginalFileOnStartUp()) {
            try {
                aasEnvironment = fileHelper.loadAASEnvironment();
            }
            catch (IOException | DeserializationException e) {
                throw new RuntimeException(e);
            }
        }

        if (aasEnvironment == null) {
            try {
                if (config.getEnvironment() != null) {
                    aasEnvironment = config.isDecoupleEnvironment() ? DeepCopyHelper.deepCopy(config.getEnvironment()) : config.getEnvironment();
                }
                else {
                    aasEnvironment = AASEnvironmentHelper.fromFile(new File(config.getModelPath()));
                }
            }
            catch (DeserializationException e) {
                throw new IllegalArgumentException("Error deserializing AAS Environment", e);
            }
        }
        identifiablePersistenceManager.setAasEnvironment(aasEnvironment);
        referablePersistenceManager.setAasEnvironment(aasEnvironment);
        packagePersistenceManager.setAasEnvironment(aasEnvironment);
    }


    @Override
    public void afterInit() {

        fileHelper.save(super.getEnvironment());
    }


    @Override
    public Identifiable put(Identifiable identifiable) {
        Identifiable element = super.put(identifiable);
        fileHelper.save(getEnvironment());
        return element;
    }


    @Override
    public SubmodelElement put(Reference parent, Reference referenceToSubmodelElement, SubmodelElement submodelElement) throws ResourceNotFoundException {
        SubmodelElement element = super.put(parent, referenceToSubmodelElement, submodelElement);
        fileHelper.save(getEnvironment());
        return element;
    }


    @Override
    public void remove(Identifier id) throws ResourceNotFoundException {
        super.remove(id);
        fileHelper.save(getEnvironment());
    }


    @Override
    public void remove(Reference reference) throws ResourceNotFoundException {
        super.remove(reference);
        fileHelper.save(getEnvironment());
    }


    @Override
    public void remove(String packageId) {
        super.remove(packageId);
        fileHelper.save(getEnvironment());
    }
}
