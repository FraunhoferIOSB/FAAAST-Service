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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.helper;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.AimcSubmodelTemplateProcessorConfig;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;


/**
 * Class with data of AIMC relations.
 */
public class RelationData {

    private final ServiceContext serviceContext;
    private final List<RelationshipElement> relations;
    private final String contentType;
    private AimcSubmodelTemplateProcessorConfig config;

    public RelationData(ServiceContext serviceContext, List<RelationshipElement> relations, String contentType, AimcSubmodelTemplateProcessorConfig config) {
        this.serviceContext = serviceContext;
        this.relations = relations;
        this.contentType = contentType;
        this.config = config;
    }


    /**
     * Get the service context.
     *
     * @return the serviceContext
     */
    public ServiceContext getServiceContext() {
        return serviceContext;
    }


    /**
     * Get the list of relations.
     *
     * @return the relations
     */
    public List<RelationshipElement> getRelations() {
        return relations;
    }


    /**
     * Get the content type.
     *
     * @return the contentType
     */
    public String getContentType() {
        return contentType;
    }


    /**
     * Gets the configuration.
     *
     * @return The configuration.
     */
    public AimcSubmodelTemplateProcessorConfig getConfig() {
        return config;
    }
}
