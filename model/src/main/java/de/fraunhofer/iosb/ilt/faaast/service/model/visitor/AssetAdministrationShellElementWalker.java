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
package de.fraunhofer.iosb.ilt.faaast.service.model.visitor;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import de.fraunhofer.iosb.ilt.faaast.service.util.MostSpecificClassComparator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.AdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.BasicEventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.Capability;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.DataSpecificationContent;
import org.eclipse.digitaltwin.aas4j.v3.model.DataSpecificationIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.EmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.EventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.EventPayload;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.HasExtensions;
import org.eclipse.digitaltwin.aas4j.v3.model.HasKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.Range;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.RelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetId;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;
import org.eclipse.digitaltwin.aas4j.v3.model.ValueList;
import org.eclipse.digitaltwin.aas4j.v3.model.ValueReferencePair;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Special kind of visitor that recursively walks the whole element structure and applies given visitors to each
 * element.
 */
public class AssetAdministrationShellElementWalker implements DefaultAssetAdministrationShellElementSubtypeResolvingVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetAdministrationShellElementWalker.class);
    protected AssetAdministrationShellElementVisitor after;
    protected AssetAdministrationShellElementVisitor before;
    protected WalkingMode mode;
    protected AssetAdministrationShellElementVisitor visitor;

    protected AssetAdministrationShellElementWalker() {
        mode = WalkingMode.DEFAULT;
    }


    @Override
    public void visit(DataSpecificationContent dataSpecificationContent) {
        if (dataSpecificationContent == null) {
            return;
        }
        Class<?> type = dataSpecificationContent.getClass();
        if (DataSpecificationIec61360.class.isAssignableFrom(type)) {
            visit((DataSpecificationIec61360) dataSpecificationContent);
        }
    }


    @Override
    public void visit(EventElement event) {
        if (event == null) {
            return;
        }
        Class<?> type = event.getClass();
        if (BasicEventElement.class.isAssignableFrom(type)) {
            visit((BasicEventElement) event);
        }
    }


    @Override
    public void visit(HasExtensions hasExtensions) {
        if (hasExtensions == null) {
            return;
        }
        Class<?> type = hasExtensions.getClass();
        if (Referable.class.isAssignableFrom(type)) {
            visit((Referable) hasExtensions);
        }
    }


    @Override
    public void visit(HasKind hasKind) {
        if (hasKind == null) {
            return;
        }
        Class<?> type = hasKind.getClass();
        if (Submodel.class.isAssignableFrom(type)) {
            visit((Submodel) hasKind);
        }
        else if (SubmodelElement.class.isAssignableFrom(type)) {
            visit((SubmodelElement) hasKind);
        }
    }


    @Override
    public void visit(AdministrativeInformation administrativeInformation) {
        visitBefore(administrativeInformation);
        visitAfter(administrativeInformation);
    }


    @Override
    public void visit(Blob blob) {
        visitBefore(blob);
        if (blob != null) {
            visit(blob.getSemanticId());
            if (blob.getSupplementalSemanticIds() != null) {
                blob.getSupplementalSemanticIds().forEach(this::visit);
            }
            if (blob.getDescription() != null) {
                blob.getDescription().forEach(this::visit);
            }
            if (blob.getDisplayName() != null) {
                blob.getDisplayName().forEach(this::visit);
            }
            if (blob.getQualifiers() != null) {
                blob.getQualifiers().forEach(this::visit);
            }
            if (blob.getEmbeddedDataSpecifications() != null) {
                blob.getEmbeddedDataSpecifications().forEach(this::visit);
            }
            if (blob.getExtensions() != null) {
                blob.getExtensions().forEach(this::visit);
            }
        }
        visitAfter(blob);
    }


    @Override
    public void visit(Capability capability) {
        visitBefore(capability);
        if (capability != null) {
            visit(capability.getSemanticId());
            if (capability.getSupplementalSemanticIds() != null) {
                capability.getSupplementalSemanticIds().forEach(this::visit);
            }
            if (capability.getDescription() != null) {
                capability.getDescription().forEach(this::visit);
            }
            if (capability.getDisplayName() != null) {
                capability.getDisplayName().forEach(this::visit);
            }
            if (capability.getQualifiers() != null) {
                capability.getQualifiers().forEach(this::visit);
            }
            if (capability.getEmbeddedDataSpecifications() != null) {
                capability.getEmbeddedDataSpecifications().forEach(this::visit);
            }
            if (capability.getExtensions() != null) {
                capability.getExtensions().forEach(this::visit);
            }
        }
        visitAfter(capability);
    }


    @Override
    public void visit(DataSpecificationIec61360 dataSpecificationIec61360) {
        visitBefore(dataSpecificationIec61360);
        visitAfter(dataSpecificationIec61360);
    }


    @Override
    public void visit(EmbeddedDataSpecification embeddedDataSpecification) {
        visitBefore(embeddedDataSpecification);
        visitAfter(embeddedDataSpecification);
    }


    @Override
    public void visit(EventPayload eventPayload) {
        visitBefore(eventPayload);
        visitAfter(eventPayload);
    }


    @Override
    public void visit(File file) {
        visitBefore(file);
        if (file != null) {
            visit(file.getSemanticId());
            if (file.getDescription() != null) {
                file.getDescription().forEach(this::visit);
            }
            if (file.getDisplayName() != null) {
                file.getDisplayName().forEach(this::visit);
            }
            if (file.getQualifiers() != null) {
                file.getQualifiers().forEach(this::visit);
            }
            if (file.getEmbeddedDataSpecifications() != null) {
                file.getEmbeddedDataSpecifications().forEach(this::visit);
            }
            if (file.getExtensions() != null) {
                file.getExtensions().forEach(this::visit);
            }
        }
        visitAfter(file);
    }


    @Override
    public void visit(Key key) {
        visitBefore(key);
        visitAfter(key);
    }


    @Override
    public void visit(LangStringTextType langString) {
        visitBefore(langString);
        visitAfter(langString);
    }


    @Override
    public void visit(Range range) {
        visitBefore(range);
        if (range != null) {
            visit(range.getSemanticId());
            if (range.getDescription() != null) {
                range.getDescription().forEach(this::visit);
            }
            if (range.getDisplayName() != null) {
                range.getDisplayName().forEach(this::visit);
            }
            if (range.getQualifiers() != null) {
                range.getQualifiers().forEach(this::visit);
            }
            if (range.getEmbeddedDataSpecifications() != null) {
                range.getEmbeddedDataSpecifications().forEach(this::visit);
            }
            if (range.getExtensions() != null) {
                range.getExtensions().forEach(this::visit);
            }
        }
        visitAfter(range);
    }


    @Override
    public void visit(AnnotatedRelationshipElement annotatedRelationshipElement) {
        visitBefore(annotatedRelationshipElement);
        if (annotatedRelationshipElement != null && annotatedRelationshipElement.getAnnotations() != null) {
            annotatedRelationshipElement.getAnnotations().forEach(this::visit);
        }
        visitAfter(annotatedRelationshipElement);
    }


    @Override
    public void visit(AssetAdministrationShell assetAdministrationShell) {
        visitBefore(assetAdministrationShell);
        if (assetAdministrationShell != null) {
            if (assetAdministrationShell.getExtensions() != null) {
                assetAdministrationShell.getExtensions().forEach(this::visit);
            }
            if (assetAdministrationShell.getDescription() != null) {
                assetAdministrationShell.getDescription().forEach(this::visit);
            }
            if (assetAdministrationShell.getDisplayName() != null) {
                assetAdministrationShell.getDisplayName().forEach(this::visit);
            }
            visit(assetAdministrationShell.getAdministration());
            visit(assetAdministrationShell.getDerivedFrom());
            visit(assetAdministrationShell.getAssetInformation());
            if (assetAdministrationShell.getEmbeddedDataSpecifications() != null) {
                assetAdministrationShell.getEmbeddedDataSpecifications().forEach(this::visit);
            }
            if (assetAdministrationShell.getSubmodels() != null) {
                assetAdministrationShell.getSubmodels().forEach(this::visit);
            }
        }
        visitAfter(assetAdministrationShell);
    }


    @Override
    public void visit(AssetInformation assetInformation) {
        visitBefore(assetInformation);
        if (assetInformation != null) {
            visit(assetInformation.getDefaultThumbnail());
            if (assetInformation.getSpecificAssetIds() != null) {
                assetInformation.getSpecificAssetIds().forEach(this::visit);
            }
        }
        visitAfter(assetInformation);
    }


    @Override
    public void visit(BasicEventElement basicEventElement) {
        visitBefore(basicEventElement);
        if (basicEventElement != null) {
            visit(basicEventElement.getSemanticId());
            if (basicEventElement.getSupplementalSemanticIds() != null) {
                basicEventElement.getSupplementalSemanticIds().forEach(this::visit);
            }
            if (basicEventElement.getDescription() != null) {
                basicEventElement.getDescription().forEach(this::visit);
            }
            if (basicEventElement.getDisplayName() != null) {
                basicEventElement.getDisplayName().forEach(this::visit);
            }
            if (basicEventElement.getQualifiers() != null) {
                basicEventElement.getQualifiers().forEach(this::visit);
            }
            if (basicEventElement.getExtensions() != null) {
                basicEventElement.getExtensions().forEach(this::visit);
            }
            if (basicEventElement.getEmbeddedDataSpecifications() != null) {
                basicEventElement.getEmbeddedDataSpecifications().forEach(this::visit);
            }
            visit(basicEventElement.getObserved());
        }
        visitAfter(basicEventElement);
    }


    @Override
    public void visit(ConceptDescription conceptDescription) {
        visitBefore(conceptDescription);
        if (conceptDescription != null) {
            visit(conceptDescription.getAdministration());
            if (conceptDescription.getDescription() != null) {
                conceptDescription.getDescription().forEach(this::visit);
            }
            if (conceptDescription.getDisplayName() != null) {
                conceptDescription.getDisplayName().forEach(this::visit);
            }
            if (conceptDescription.getExtensions() != null) {
                conceptDescription.getExtensions().forEach(this::visit);
            }
            if (conceptDescription.getIsCaseOf() != null) {
                conceptDescription.getIsCaseOf().forEach(this::visit);
            }
        }
        visitAfter(conceptDescription);
    }


    @Override
    public void visit(SpecificAssetId specificAssetId) {
        visitBefore(specificAssetId);
        if (specificAssetId != null) {
            visit(specificAssetId.getSemanticId());
            if (specificAssetId.getSupplementalSemanticIds() != null) {
                specificAssetId.getSupplementalSemanticIds().forEach(this::visit);
            }
            visit(specificAssetId.getExternalSubjectId());
        }
        visitAfter(specificAssetId);
    }


    @Override
    public void visit(MultiLanguageProperty multiLanguageProperty) {
        visitBefore(multiLanguageProperty);
        if (multiLanguageProperty != null) {
            visit(multiLanguageProperty.getSemanticId());
            if (multiLanguageProperty.getSupplementalSemanticIds() != null) {
                multiLanguageProperty.getSupplementalSemanticIds().forEach(this::visit);
            }
            if (multiLanguageProperty.getDescription() != null) {
                multiLanguageProperty.getDescription().forEach(this::visit);
            }
            if (multiLanguageProperty.getDisplayName() != null) {
                multiLanguageProperty.getDisplayName().forEach(this::visit);
            }
            if (multiLanguageProperty.getQualifiers() != null) {
                multiLanguageProperty.getQualifiers().forEach(this::visit);
            }
            if (multiLanguageProperty.getEmbeddedDataSpecifications() != null) {
                multiLanguageProperty.getEmbeddedDataSpecifications().forEach(this::visit);
            }
            if (multiLanguageProperty.getExtensions() != null) {
                multiLanguageProperty.getExtensions().forEach(this::visit);
            }
            if (multiLanguageProperty.getValue() != null) {
                multiLanguageProperty.getValue().forEach(this::visit);
            }
            visit(multiLanguageProperty.getValueId());
        }
        visitAfter(multiLanguageProperty);
    }


    @Override
    public void visit(OperationVariable operationVariable) {
        visitBefore(operationVariable);
        if (operationVariable != null) {
            visit(operationVariable.getValue());
        }
        visitAfter(operationVariable);
    }


    @Override
    public void visit(Property property) {
        visitBefore(property);
        if (property != null) {
            visit(property.getSemanticId());
            if (property.getSupplementalSemanticIds() != null) {
                property.getSupplementalSemanticIds().forEach(this::visit);
            }
            if (property.getDescription() != null) {
                property.getDescription().forEach(this::visit);
            }
            if (property.getDisplayName() != null) {
                property.getDisplayName().forEach(this::visit);
            }
            if (property.getQualifiers() != null) {
                property.getQualifiers().forEach(this::visit);
            }
            if (property.getEmbeddedDataSpecifications() != null) {
                property.getEmbeddedDataSpecifications().forEach(this::visit);
            }
            if (property.getExtensions() != null) {
                property.getExtensions().forEach(this::visit);
            }
            visit(property.getValueId());
        }
        visitAfter(property);
    }


    @Override
    public void visit(Qualifier qualifier) {
        visitBefore(qualifier);
        if (qualifier != null) {
            visit(qualifier.getValueId());
        }
        visitAfter(qualifier);
    }


    @Override
    public void visit(Reference reference) {
        visitBefore(reference);
        if (reference != null) {
            visit(reference.getReferredSemanticId());
            if (reference.getKeys() != null) {
                reference.getKeys().forEach(this::visit);
            }
        }
        visitAfter(reference);
    }


    @Override
    public void visit(ReferenceElement referenceElement) {
        visitBefore(referenceElement);
        if (referenceElement != null) {
            visit(referenceElement.getSemanticId());
            if (referenceElement.getSupplementalSemanticIds() != null) {
                referenceElement.getSupplementalSemanticIds().forEach(this::visit);
            }
            if (referenceElement.getDescription() != null) {
                referenceElement.getDescription().forEach(this::visit);
            }
            if (referenceElement.getDisplayName() != null) {
                referenceElement.getDisplayName().forEach(this::visit);
            }
            if (referenceElement.getQualifiers() != null) {
                referenceElement.getQualifiers().forEach(this::visit);
            }
            if (referenceElement.getEmbeddedDataSpecifications() != null) {
                referenceElement.getEmbeddedDataSpecifications().forEach(this::visit);
            }
            if (referenceElement.getExtensions() != null) {
                referenceElement.getExtensions().forEach(this::visit);
            }
            visit(referenceElement.getValue());
        }
        visitAfter(referenceElement);
    }


    @Override
    public void visit(RelationshipElement relationshipElement) {
        if (relationshipElement != null && AnnotatedRelationshipElement.class.isAssignableFrom(relationshipElement.getClass())) {
            visit((AnnotatedRelationshipElement) relationshipElement);
        }
        else {
            visitBefore(relationshipElement);
            if (relationshipElement != null) {
                visit(relationshipElement.getSemanticId());
                if (relationshipElement.getSupplementalSemanticIds() != null) {
                    relationshipElement.getSupplementalSemanticIds().forEach(this::visit);
                }
                if (relationshipElement.getDescription() != null) {
                    relationshipElement.getDescription().forEach(this::visit);
                }
                if (relationshipElement.getDisplayName() != null) {
                    relationshipElement.getDisplayName().forEach(this::visit);
                }
                if (relationshipElement.getQualifiers() != null) {
                    relationshipElement.getQualifiers().forEach(this::visit);
                }
                if (relationshipElement.getEmbeddedDataSpecifications() != null) {
                    relationshipElement.getEmbeddedDataSpecifications().forEach(this::visit);
                }
                if (relationshipElement.getExtensions() != null) {
                    relationshipElement.getExtensions().forEach(this::visit);
                }
                visit(relationshipElement.getFirst());
                visit(relationshipElement.getSecond());
            }
            visitAfter(relationshipElement);
        }
    }


    @Override
    public void visit(Entity entity) {
        visitBefore(entity);
        if (entity != null) {
            if (entity.getQualifiers() != null) {
                entity.getQualifiers().forEach(this::visit);
            }
            if (entity.getEmbeddedDataSpecifications() != null) {
                entity.getEmbeddedDataSpecifications().forEach(this::visit);
            }
            if (entity.getExtensions() != null) {
                entity.getExtensions().forEach(this::visit);
            }
            if (entity.getDescription() != null) {
                entity.getDescription().forEach(this::visit);
            }
            if (entity.getDisplayName() != null) {
                entity.getDisplayName().forEach(this::visit);
            }
            if (entity.getSpecificAssetIds() != null) {
                entity.getSpecificAssetIds().forEach(this::visit);
            }
            visit(entity.getSemanticId());
            if (entity.getSupplementalSemanticIds() != null) {
                entity.getSupplementalSemanticIds().forEach(this::visit);
            }
            if (entity.getStatements() != null) {
                entity.getStatements().forEach(this::visit);
            }
        }
        visitAfter(entity);
    }


    @Override
    public void visit(Extension extension) {
        visitBefore(extension);
        if (extension != null) {
            visit(extension.getSemanticId());
            if (extension.getSupplementalSemanticIds() != null) {
                extension.getSupplementalSemanticIds().forEach(this::visit);
            }
            visit(extension.getRefersTo());
        }
        visitAfter(extension);
    }


    @Override
    public void visit(Environment environment) {
        visitBefore(environment);
        if (environment != null) {
            if (environment.getAssetAdministrationShells() != null) {
                environment.getAssetAdministrationShells().forEach(this::visit);
            }
            if (environment.getConceptDescriptions() != null) {
                environment.getConceptDescriptions().forEach(this::visit);
            }
            if (environment.getSubmodels() != null) {
                environment.getSubmodels().forEach(this::visit);
            }
        }
        visitAfter(environment);
    }


    @Override
    public void visit(Submodel submodel) {
        visitBefore(submodel);
        if (submodel != null) {
            visit(submodel.getSemanticId());
            if (submodel.getSupplementalSemanticIds() != null) {
                submodel.getSupplementalSemanticIds().forEach(this::visit);
            }
            visit(submodel.getAdministration());
            if (submodel.getDescription() != null) {
                submodel.getDescription().forEach(this::visit);
            }
            if (submodel.getDisplayName() != null) {
                submodel.getDisplayName().forEach(this::visit);
            }
            if (submodel.getQualifiers() != null) {
                submodel.getQualifiers().forEach(this::visit);
            }
            if (submodel.getExtensions() != null) {
                submodel.getExtensions().forEach(this::visit);
            }
            if (submodel.getEmbeddedDataSpecifications() != null) {
                submodel.getEmbeddedDataSpecifications().forEach(this::visit);
            }
            if (submodel.getSubmodelElements() != null) {
                submodel.getSubmodelElements().forEach(this::visit);
            }
        }
        visitAfter(submodel);
    }


    @Override
    public void visit(SubmodelElementCollection submodelElementCollection) {
        visitBefore(submodelElementCollection);
        if (submodelElementCollection != null) {
            visit(submodelElementCollection.getSemanticId());
            if (submodelElementCollection.getSupplementalSemanticIds() != null) {
                submodelElementCollection.getSupplementalSemanticIds().forEach(this::visit);
            }
            if (submodelElementCollection.getDescription() != null) {
                submodelElementCollection.getDescription().forEach(this::visit);
            }
            if (submodelElementCollection.getDisplayName() != null) {
                submodelElementCollection.getDisplayName().forEach(this::visit);
            }
            if (submodelElementCollection.getQualifiers() != null) {
                submodelElementCollection.getQualifiers().forEach(this::visit);
            }
            if (submodelElementCollection.getEmbeddedDataSpecifications() != null) {
                submodelElementCollection.getEmbeddedDataSpecifications().forEach(this::visit);
            }
            if (submodelElementCollection.getExtensions() != null) {
                submodelElementCollection.getExtensions().forEach(this::visit);
            }
            if (submodelElementCollection.getValue() != null) {
                submodelElementCollection.getValue().forEach(this::visit);
            }
        }
        visitAfter(submodelElementCollection);
    }


    @Override
    public void visit(SubmodelElementList submodelElementList) {
        visitBefore(submodelElementList);
        if (submodelElementList != null) {
            visit(submodelElementList.getSemanticId());
            if (submodelElementList.getSupplementalSemanticIds() != null) {
                submodelElementList.getSupplementalSemanticIds().forEach(this::visit);
            }
            if (submodelElementList.getDescription() != null) {
                submodelElementList.getDescription().forEach(this::visit);
            }
            if (submodelElementList.getDisplayName() != null) {
                submodelElementList.getDisplayName().forEach(this::visit);
            }
            if (submodelElementList.getQualifiers() != null) {
                submodelElementList.getQualifiers().forEach(this::visit);
            }
            if (submodelElementList.getEmbeddedDataSpecifications() != null) {
                submodelElementList.getEmbeddedDataSpecifications().forEach(this::visit);
            }
            if (submodelElementList.getExtensions() != null) {
                submodelElementList.getExtensions().forEach(this::visit);
            }
            if (submodelElementList.getValue() != null) {
                submodelElementList.getValue().forEach(this::visit);
            }
        }
        visitAfter(submodelElementList);
    }


    @Override
    public void visit(Operation operation) {
        visitBefore(operation);
        if (operation != null) {
            visit(operation.getSemanticId());
            if (operation.getSupplementalSemanticIds() != null) {
                operation.getSupplementalSemanticIds().forEach(this::visit);
            }
            if (operation.getDescription() != null) {
                operation.getDescription().forEach(this::visit);
            }
            if (operation.getDisplayName() != null) {
                operation.getDisplayName().forEach(this::visit);
            }
            if (operation.getQualifiers() != null) {
                operation.getQualifiers().forEach(this::visit);
            }
            if (operation.getEmbeddedDataSpecifications() != null) {
                operation.getEmbeddedDataSpecifications().forEach(this::visit);
            }
            if (operation.getExtensions() != null) {
                operation.getExtensions().forEach(this::visit);
            }
            if (operation.getInputVariables() != null) {
                operation.getInputVariables().forEach(x -> visit(x.getValue()));
            }
            if (operation.getInoutputVariables() != null) {
                operation.getInoutputVariables().forEach(x -> visit(x.getValue()));
            }
            if (operation.getOutputVariables() != null) {
                operation.getOutputVariables().forEach(x -> visit(x.getValue()));
            }
        }
        visitAfter(operation);
    }


    @Override
    public void visit(ValueList valueList) {
        visitBefore(valueList);
        if (valueList != null) {
            if (valueList.getValueReferencePairs() != null) {
                valueList.getValueReferencePairs().forEach(this::visit);
            }
        }
        visitAfter(valueList);
    }


    @Override
    public void visit(ValueReferencePair valueReferencePair) {
        visitBefore(valueReferencePair);
        if (valueReferencePair != null) {
            visit(valueReferencePair.getValueId());
        }
        visitAfter(valueReferencePair);
    }


    /**
     * Walks the given object.
     *
     * @param obj object to walk, must be some element of an AAS
     */
    public void walk(Object obj) {
        visit(obj);
    }


    /**
     * Visit an object.
     *
     * @param obj the object to visit
     */
    protected void visit(Object obj) {
        if (obj != null) {
            try {
                Optional<Method> method = Stream.of(this.getClass().getMethods())
                        .filter(x -> x.getName().equals("visit"))
                        .filter(x -> x.getParameterCount() == 1)
                        .filter(x -> x.getParameters()[0].getType().isAssignableFrom(obj.getClass()))
                        .sorted((Method m1, Method m2) -> -1 * new MostSpecificClassComparator().compare(m1.getParameters()[0].getType(), m2.getParameters()[0].getType()))
                        .findFirst();
                if (method.isPresent()) {
                    method.get().invoke(this, obj);
                }
            }
            catch (Exception e) {
                LOGGER.debug("invoking visit method via refection failed", e);
            }
        }
    }


    /**
     * Visit a page by visiting all elements.
     *
     * @param page the page to visit
     */
    public void visit(Page<?> page) {
        if (Objects.isNull(page) || Objects.isNull(page.getContent())) {
            return;
        }
        page.getContent().forEach(this::visit);
    }


    /**
     * Visitor after-visitor.
     *
     * @param obj the object to visit
     */
    protected void visitAfter(Object obj) {
        if (mode == WalkingMode.VISIT_AFTER_DESCENT) {
            walk(visitor, obj);
        }
        walk(after, obj);
    }


    /**
     * Visitor before-visitor.
     *
     * @param obj the object to visit
     */
    protected void visitBefore(Object obj) {
        walk(before, obj);
        if (mode == WalkingMode.VISIT_BEFORE_DESCENT) {
            walk(visitor, obj);
        }
    }


    private void walk(AssetAdministrationShellElementVisitor visitor, Object obj) {
        if (visitor != null && obj != null) {
            try {
                List<Method> methods = Stream.of(visitor.getClass().getMethods())
                        .filter(x -> x.getName().equals("visit"))
                        .filter(x -> x.getParameterCount() == 1)
                        .filter(x -> x.getParameters()[0].getType().isAssignableFrom(obj.getClass()))
                        .sorted((Method m1, Method m2) -> -1 * new MostSpecificClassComparator().compare(m1.getParameters()[0].getType(), m2.getParameters()[0].getType()))
                        .collect(Collectors.toList());
                for (Method method: methods) {
                    try {
                        method.setAccessible(true);
                        method.invoke(visitor, obj);
                    }
                    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        LOGGER.debug(String.format("invoking visit(%s) method via refection failed",
                                method.getParameterTypes()[0].getSimpleName()),
                                e);
                    }
                }
            }
            catch (SecurityException e) {
                LOGGER.debug("invoking visit method via refection failed", e);
            }
        }
    }

    /**
     * Enum of supported walking modes.
     */
    public enum WalkingMode {
        /**
         * Visit an element after visiting all of its subelements.
         */
        VISIT_AFTER_DESCENT,
        /**
         * Visit an element before visiting all of its subelements.
         */
        VISIT_BEFORE_DESCENT;

        public static final WalkingMode DEFAULT = VISIT_BEFORE_DESCENT;
    }

    public static Builder builder() {
        return new Builder();
    }

    public abstract static class AbstractBuilder<T extends AssetAdministrationShellElementWalker, B extends AbstractBuilder<T, B>> extends ExtendableBuilder<T, B> {

        public B before(AssetAdministrationShellElementVisitor value) {
            getBuildingInstance().before = value;
            return getSelf();
        }


        public B after(AssetAdministrationShellElementVisitor value) {
            getBuildingInstance().after = value;
            return getSelf();
        }


        public B visitor(AssetAdministrationShellElementVisitor value) {
            getBuildingInstance().visitor = value;
            return getSelf();
        }


        public B mode(WalkingMode value) {
            getBuildingInstance().mode = value;
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<AssetAdministrationShellElementWalker, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected AssetAdministrationShellElementWalker newBuildingInstance() {
            return new AssetAdministrationShellElementWalker();
        }
    }
}
