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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.security;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.SubmodelTemplateProcessor;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Adds logic for submodel instances of template Security.
 */
public class SecuritySubmodelTemplateProcessor implements SubmodelTemplateProcessor<SecuritySubmodelTemplateProcessorConfig> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecuritySubmodelTemplateProcessor.class);

    private SecuritySubmodelTemplateProcessorConfig config;
    private ServiceContext serviceContext;
    private Submodel submodel;

    public SecuritySubmodelTemplateProcessor() {}


    @Override
    public boolean accept(Submodel submodel) {
        return submodel != null
                && (Objects.equals(ReferenceBuilder.global(Constants.SECURITY_SUBMODEL_SEMANTIC_ID), submodel.getSemanticId())
                        || Objects.equals(ReferenceBuilder.global(Constants.SECURITY_SUBMODEL_SEMANTIC_ID), submodel.getSemanticId()));
    }


    @Override
    public boolean add(Submodel submodel, AssetConnectionManager assetConnectionManager) {
        Ensure.requireNonNull(submodel);
        Ensure.requireNonNull(assetConnectionManager);
        boolean retval;
        try {
            // in add we only process AIMC submodel
            if (Objects.equals(ReferenceBuilder.global(Constants.SECURITY_SUBMODEL_SEMANTIC_ID), submodel.getSemanticId())) {
                LOGGER.atInfo().log("process submodel {} ({})", submodel.getIdShort(), AasUtils.asString(AasUtils.toReference(submodel)));
                //processSubmodel(submodel, assetConnectionManager, ProcessingMode.ADD);
                this.submodel = submodel;
            }

            retval = true;
        }
        catch (Exception ex) {
            LOGGER.error("error processing SMT AIMC (submodel: {})", AasUtils.asString(AasUtils.toReference(submodel)), ex);
            retval = false;
        }

        return retval;
    }


    @Override
    public void init(CoreConfig coreConfig, SecuritySubmodelTemplateProcessorConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        this.config = config;
        this.serviceContext = serviceContext;
    }


    @Override
    public SecuritySubmodelTemplateProcessorConfig asConfig() {
        return config;
    }


    @Override
    public boolean update(Submodel submodel, AssetConnectionManager assetConnectionManager) {
        Ensure.requireNonNull(submodel);
        Ensure.requireNonNull(assetConnectionManager);
        boolean retval;

        try {
            LOGGER.atInfo().log("update submodel {} ({})", submodel.getIdShort(), AasUtils.asString(AasUtils.toReference(submodel)));
            Submodel updateSubmodel = submodel;
            // we always use AIMC submodel for processSubmodel
            if (Objects.equals(ReferenceBuilder.global(Constants.SECURITY_SUBMODEL_SEMANTIC_ID), submodel.getSemanticId())) {
                updateSubmodel = this.submodel;
            }
            //processSubmodel(updateSubmodel, assetConnectionManager, ProcessingMode.UPDATE);
            retval = true;
        }
        catch (Exception ex) {
            LOGGER.error("error updating SMT AIMC (submodel: {})", AasUtils.asString(AasUtils.toReference(submodel)), ex);
            retval = false;
        }
        return retval;
    }


    @Override
    public boolean delete(Submodel submodel, AssetConnectionManager assetConnectionManager) {
        Ensure.requireNonNull(submodel);
        Ensure.requireNonNull(assetConnectionManager);
        boolean retval;
        try {
            // in delete we only process AIMC submodel
            if (Objects.equals(ReferenceBuilder.global(Constants.SECURITY_SUBMODEL_SEMANTIC_ID), submodel.getSemanticId())) {
                LOGGER.atInfo().log("delete submodel {} ({})", submodel.getIdShort(), AasUtils.asString(AasUtils.toReference(submodel)));
                //processSubmodel(submodel, assetConnectionManager, ProcessingMode.DELETE);
            }
            retval = true;
        }
        catch (Exception ex) {
            LOGGER.error("error deleting SMT AIMC (submodel: {})", AasUtils.asString(AasUtils.toReference(submodel)), ex);
            retval = false;
        }
        return retval;
    }


    private static List<RelationshipElement> getRelationshipElements(List<SubmodelElement> relations) {
        List<RelationshipElement> retval = new ArrayList<>();
        for (var r: relations) {
            if (r instanceof RelationshipElement relationshipElement) {
                retval.add(relationshipElement);
            }
        }

        return retval;
    }


    private static SubmodelElementList getMappingConfiguration(Submodel submodel) {
        Optional<SubmodelElement> element = submodel.getSubmodelElements().stream().filter(s -> Constants.SECURITY_SUBMODEL_SEMANTIC_ID.equals(s.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel invalid: MappingConfigurations not found.");
        }
        if (element.get() instanceof SubmodelElementList list) {
            return list;
        }
        else {
            throw new IllegalArgumentException("Submodel invalid: MappingConfigurations not a list.");
        }
    }


    private static List<RelationshipElement> getMappingRelations(SubmodelElementCollection configuration) {
        Optional<SubmodelElement> element = configuration.getValue().stream().filter(e -> Constants.SECURITY_SUBMODEL_SEMANTIC_ID.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel invalid: MappingSourceSinkRelations not found.");
        }
        List<RelationshipElement> relations = null;
        if (element.get() instanceof SubmodelElementList list) {
            relations = getRelationshipElements(list.getValue());
        }
        if (relations == null) {
            relations = new ArrayList<>();
        }
        return relations;
    }


    private static ReferenceElement getInterfaceReference(SubmodelElementCollection configuration) {
        Optional<SubmodelElement> element = configuration.getValue().stream().filter(e -> Constants.SECURITY_SUBMODEL_SEMANTIC_ID.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel invalid: InterfaceReference not found.");
        }
        if (element.get() instanceof ReferenceElement interfaceReference) {
            return interfaceReference;
        }
        else {
            throw new IllegalArgumentException("Submodel invalid: InterfaceReference not a ReferenceElement.");
        }
    }
}
