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

import de.fraunhofer.iosb.ilt.faaast.service.util.MostSpecificClassComparator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
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
import org.eclipse.digitaltwin.aas4j.v3.model.SpecificAssetID;
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
            visit(blob.getSemanticID());
            blob.getSupplementalSemanticIds().forEach(x -> visit(x));
            blob.getDescription().forEach(x -> visit(x));
            blob.getDisplayName().forEach(x -> visit(x));
            blob.getQualifiers().forEach(x -> visit(x));
            blob.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            blob.getExtensions().forEach(x -> visit(x));
        }
        visitAfter(blob);
    }


    @Override
    public void visit(Capability capability) {
        visitBefore(capability);
        if (capability != null) {
            visit(capability.getSemanticID());
            capability.getSupplementalSemanticIds().forEach(x -> visit(x));
            capability.getDescription().forEach(x -> visit(x));
            capability.getDisplayName().forEach(x -> visit(x));
            capability.getQualifiers().forEach(x -> visit(x));
            capability.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            capability.getExtensions().forEach(x -> visit(x));
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
            visit(file.getSemanticID());
            file.getDescription().forEach(x -> visit(x));
            file.getDisplayName().forEach(x -> visit(x));
            file.getQualifiers().forEach(x -> visit(x));
            file.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            file.getExtensions().forEach(x -> visit(x));
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
            visit(range.getSemanticID());
            range.getDescription().forEach(x -> visit(x));
            range.getDisplayName().forEach(x -> visit(x));
            range.getQualifiers().forEach(x -> visit(x));
            range.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            range.getExtensions().forEach(x -> visit(x));
        }
        visitAfter(range);
    }


    @Override
    public void visit(AnnotatedRelationshipElement annotatedRelationshipElement) {
        visitBefore(annotatedRelationshipElement);
        if (annotatedRelationshipElement != null) {
            annotatedRelationshipElement.getAnnotations().forEach(x -> visit(x));
        }
        visitAfter(annotatedRelationshipElement);
    }


    @Override
    public void visit(AssetAdministrationShell assetAdministrationShell) {
        visitBefore(assetAdministrationShell);
        if (assetAdministrationShell != null) {
            assetAdministrationShell.getExtensions().forEach(x -> visit(x));
            assetAdministrationShell.getDescription().forEach(x -> visit(x));
            assetAdministrationShell.getDisplayName().forEach(x -> visit(x));
            visit(assetAdministrationShell.getAdministration());
            visit(assetAdministrationShell.getDerivedFrom());
            visit(assetAdministrationShell.getAssetInformation());
            assetAdministrationShell.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            assetAdministrationShell.getSubmodels().forEach(x -> visit(x));
        }
        visitAfter(assetAdministrationShell);
    }


    @Override
    public void visit(AssetInformation assetInformation) {
        visitBefore(assetInformation);
        if (assetInformation != null) {
            visit(assetInformation.getDefaultThumbnail());
            assetInformation.getSpecificAssetIds().forEach(x -> visit(x));
        }
        visitAfter(assetInformation);
    }


    @Override
    public void visit(BasicEventElement basicEventElement) {
        visitBefore(basicEventElement);
        if (basicEventElement != null) {
            visit(basicEventElement.getSemanticID());
            basicEventElement.getSupplementalSemanticIds().forEach(x -> visit(x));
            basicEventElement.getDescription().forEach(x -> visit(x));
            basicEventElement.getDisplayName().forEach(x -> visit(x));
            basicEventElement.getQualifiers().forEach(x -> visit(x));
            basicEventElement.getExtensions().forEach(x -> visit(x));
            basicEventElement.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            visit(basicEventElement.getObserved());
        }
        visitAfter(basicEventElement);
    }


    @Override
    public void visit(ConceptDescription conceptDescription) {
        visitBefore(conceptDescription);
        if (conceptDescription != null) {
            visit(conceptDescription.getAdministration());
            conceptDescription.getDescription().forEach(x -> visit(x));
            conceptDescription.getDisplayName().forEach(x -> visit(x));
            conceptDescription.getExtensions().forEach(x -> visit(x));
            conceptDescription.getIsCaseOf().forEach(x -> visit(x));
        }
        visitAfter(conceptDescription);
    }


    @Override
    public void visit(SpecificAssetID specificAssetID) {
        visitBefore(specificAssetID);
        if (specificAssetID != null) {
            visit(specificAssetID.getSemanticID());
            specificAssetID.getSupplementalSemanticIds().forEach(x -> visit(x));
            visit(specificAssetID.getExternalSubjectID());
        }
        visitAfter(specificAssetID);
    }


    @Override
    public void visit(MultiLanguageProperty multiLanguageProperty) {
        visitBefore(multiLanguageProperty);
        if (multiLanguageProperty != null) {
            visit(multiLanguageProperty.getSemanticID());
            multiLanguageProperty.getSupplementalSemanticIds().forEach(x -> visit(x));
            multiLanguageProperty.getDescription().forEach(x -> visit(x));
            multiLanguageProperty.getDisplayName().forEach(x -> visit(x));
            multiLanguageProperty.getQualifiers().forEach(x -> visit(x));
            multiLanguageProperty.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            multiLanguageProperty.getExtensions().forEach(x -> visit(x));
            multiLanguageProperty.getValue().forEach(x -> visit(x));
            visit(multiLanguageProperty.getValueID());
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
            visit(property.getSemanticID());
            property.getSupplementalSemanticIds().forEach(x -> visit(x));
            property.getDescription().forEach(x -> visit(x));
            property.getDisplayName().forEach(x -> visit(x));
            property.getQualifiers().forEach(x -> visit(x));
            property.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            property.getExtensions().forEach(x -> visit(x));
            visit(property.getValueID());
        }
        visitAfter(property);
    }


    @Override
    public void visit(Qualifier qualifier) {
        visitBefore(qualifier);
        if (qualifier != null) {
            visit(qualifier.getValueID());
        }
        visitAfter(qualifier);
    }


    @Override
    public void visit(Reference reference) {
        visitBefore(reference);
        if (reference != null) {
            visit(reference.getReferredSemanticID());
            reference.getKeys().forEach(x -> visit(x));
        }
        visitAfter(reference);
    }


    @Override
    public void visit(ReferenceElement referenceElement) {
        visitBefore(referenceElement);
        if (referenceElement != null) {
            visit(referenceElement.getSemanticID());
            referenceElement.getSupplementalSemanticIds().forEach(x -> visit(x));
            referenceElement.getDescription().forEach(x -> visit(x));
            referenceElement.getDisplayName().forEach(x -> visit(x));
            referenceElement.getQualifiers().forEach(x -> visit(x));
            referenceElement.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            referenceElement.getExtensions().forEach(x -> visit(x));
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
                visit(relationshipElement.getSemanticID());
                relationshipElement.getSupplementalSemanticIds().forEach(x -> visit(x));
                relationshipElement.getDescription().forEach(x -> visit(x));
                relationshipElement.getDisplayName().forEach(x -> visit(x));
                relationshipElement.getQualifiers().forEach(x -> visit(x));
                relationshipElement.getEmbeddedDataSpecifications().forEach(x -> visit(x));
                relationshipElement.getExtensions().forEach(x -> visit(x));
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
            entity.getQualifiers().forEach(x -> visit(x));
            entity.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            entity.getExtensions().forEach(x -> visit(x));
            entity.getDescription().forEach(x -> visit(x));
            entity.getDisplayName().forEach(x -> visit(x));
            entity.getSpecificAssetIds().forEach(x -> visit(x));
            visit(entity.getSemanticID());
            entity.getSupplementalSemanticIds().forEach(x -> visit(x));
            entity.getStatements().forEach(x -> visit(x));
        }
        visitAfter(entity);
    }


    @Override
    public void visit(Extension extension) {
        visitBefore(extension);
        if (extension != null) {
            visit(extension.getSemanticID());
            extension.getSupplementalSemanticIds().forEach(x -> visit(x));
            visit(extension.getRefersTo());
        }
        visitAfter(extension);
    }


    @Override
    public void visit(Environment environment) {
        visitBefore(environment);
        if (environment != null) {
            environment.getAssetAdministrationShells().forEach(x -> visit(x));
            environment.getConceptDescriptions().forEach(x -> visit(x));
            environment.getSubmodels().forEach(x -> visit(x));
        }
        visitAfter(environment);
    }


    @Override
    public void visit(Submodel submodel) {
        visitBefore(submodel);
        if (submodel != null) {
            visit(submodel.getSemanticID());
            submodel.getSupplementalSemanticIds().forEach(x -> visit(x));
            visit(submodel.getAdministration());
            submodel.getDescription().forEach(x -> visit(x));
            submodel.getDisplayName().forEach(x -> visit(x));
            submodel.getQualifiers().forEach(x -> visit(x));
            submodel.getExtensions().forEach(x -> visit(x));
            submodel.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            submodel.getSubmodelElements().forEach(x -> visit(x));
        }
        visitAfter(submodel);
    }


    @Override
    public void visit(SubmodelElementCollection submodelElementCollection) {
        visitBefore(submodelElementCollection);
        if (submodelElementCollection != null) {
            visit(submodelElementCollection.getSemanticID());
            submodelElementCollection.getSupplementalSemanticIds().forEach(x -> visit(x));
            submodelElementCollection.getDescription().forEach(x -> visit(x));
            submodelElementCollection.getDisplayName().forEach(x -> visit(x));
            submodelElementCollection.getQualifiers().forEach(x -> visit(x));
            submodelElementCollection.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            submodelElementCollection.getExtensions().forEach(x -> visit(x));
            submodelElementCollection.getValue().forEach(x -> visit(x));
        }
        visitAfter(submodelElementCollection);
    }


    @Override
    public void visit(SubmodelElementList submodelElementList) {
        visitBefore(submodelElementList);
        if (submodelElementList != null) {
            visit(submodelElementList.getSemanticID());
            submodelElementList.getSupplementalSemanticIds().forEach(x -> visit(x));
            submodelElementList.getDescription().forEach(x -> visit(x));
            submodelElementList.getDisplayName().forEach(x -> visit(x));
            submodelElementList.getQualifiers().forEach(x -> visit(x));
            submodelElementList.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            submodelElementList.getExtensions().forEach(x -> visit(x));
            submodelElementList.getValue().forEach(x -> visit(x));
        }
        visitAfter(submodelElementList);
    }


    @Override
    public void visit(Operation operation) {
        visitBefore(operation);
        if (operation != null) {
            visit(operation.getSemanticID());
            operation.getSupplementalSemanticIds().forEach(x -> visit(x));
            operation.getDescription().forEach(x -> visit(x));
            operation.getDisplayName().forEach(x -> visit(x));
            operation.getQualifiers().forEach(x -> visit(x));
            operation.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            operation.getExtensions().forEach(x -> visit(x));
            operation.getInputVariables().forEach(x -> visit(x.getValue()));
            operation.getInoutputVariables().forEach(x -> visit(x.getValue()));
            operation.getOutputVariables().forEach(x -> visit(x.getValue()));
        }
        visitAfter(operation);
    }


    @Override
    public void visit(ValueList valueList) {
        visitBefore(valueList);
        if (valueList != null) {
            valueList.getValueReferencePairs().forEach(x -> visit(x));
        }
        visitAfter(valueList);
    }


    @Override
    public void visit(ValueReferencePair valueReferencePair) {
        visitBefore(valueReferencePair);
        if (valueReferencePair != null) {
            visit(valueReferencePair.getValueID());
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
                        .sorted(new Comparator<Method>() {
                            @Override
                            public int compare(Method m1, Method m2) {
                                return -1 * new MostSpecificClassComparator().compare(m1.getParameters()[0].getType(), m2.getParameters()[0].getType());
                            }
                        })
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
                        .sorted(new Comparator<Method>() {
                            @Override
                            public int compare(Method m1, Method m2) {
                                return -1 * new MostSpecificClassComparator().compare(m1.getParameters()[0].getType(), m2.getParameters()[0].getType());
                            }
                        }).collect(Collectors.toList());
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
