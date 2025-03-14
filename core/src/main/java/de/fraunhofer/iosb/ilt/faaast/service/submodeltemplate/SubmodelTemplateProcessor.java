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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.config.Configurable;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;


/**
 * Interface for template processors.
 *
 * @param <T> type of the matching configuration
 */
public interface SubmodelTemplateProcessor<T extends SubmodelTemplateProcessorConfig> extends Configurable<T> {

    /**
     * Checks if given submodel uses the template.
     *
     * @param submodel the submodel to check
     * @return true if uses the template, otherwise false
     */
    public boolean accept(Submodel submodel);


    /**
     * Processes a given submodel, by adding elements.
     *
     * @param submodel the submodel to check
     * @param assetConnectionManager manager for asset connection, can be used to modify underlying asset connection
     * @return true if submodel has been modified, false otherwise
     */
    public boolean add(Submodel submodel, AssetConnectionManager assetConnectionManager);


    /**
     * Processes a given submodel to update the information.
     *
     * @param submodel the submodel to check
     * @param assetConnectionManager manager for asset connection, can be used to modify underlying asset connection
     * @return true if submodel has been modified, false otherwise
     */
    public boolean update(Submodel submodel, AssetConnectionManager assetConnectionManager);


    /**
     * Processes a given submodel to delete the information.
     *
     * @param submodel the submodel to check
     * @param assetConnectionManager manager for asset connection, can be used to modify underlying asset connection
     * @return true if submodel has been modified, false otherwise
     */
    public boolean delete(Submodel submodel, AssetConnectionManager assetConnectionManager);
}
