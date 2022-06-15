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
import io.adminshell.aas.v3.model.AccessControl;
import io.adminshell.aas.v3.model.AccessControlPolicyPoints;
import io.adminshell.aas.v3.model.AccessPermissionRule;
import io.adminshell.aas.v3.model.AdministrativeInformation;
import io.adminshell.aas.v3.model.AnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.Asset;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.AssetInformation;
import io.adminshell.aas.v3.model.BasicEvent;
import io.adminshell.aas.v3.model.Blob;
import io.adminshell.aas.v3.model.BlobCertificate;
import io.adminshell.aas.v3.model.Capability;
import io.adminshell.aas.v3.model.Certificate;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Constraint;
import io.adminshell.aas.v3.model.DataSpecificationContent;
import io.adminshell.aas.v3.model.DataSpecificationIEC61360;
import io.adminshell.aas.v3.model.DataSpecificationPhysicalUnit;
import io.adminshell.aas.v3.model.EmbeddedDataSpecification;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.Event;
import io.adminshell.aas.v3.model.EventElement;
import io.adminshell.aas.v3.model.EventMessage;
import io.adminshell.aas.v3.model.Extension;
import io.adminshell.aas.v3.model.File;
import io.adminshell.aas.v3.model.Formula;
import io.adminshell.aas.v3.model.HasExtensions;
import io.adminshell.aas.v3.model.HasKind;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.MultiLanguageProperty;
import io.adminshell.aas.v3.model.ObjectAttributes;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Permission;
import io.adminshell.aas.v3.model.PermissionsPerObject;
import io.adminshell.aas.v3.model.PolicyAdministrationPoint;
import io.adminshell.aas.v3.model.PolicyDecisionPoint;
import io.adminshell.aas.v3.model.PolicyEnforcementPoints;
import io.adminshell.aas.v3.model.PolicyInformationPoints;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Qualifier;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.ReferenceElement;
import io.adminshell.aas.v3.model.RelationshipElement;
import io.adminshell.aas.v3.model.Security;
import io.adminshell.aas.v3.model.SubjectAttributes;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.ValueList;
import io.adminshell.aas.v3.model.ValueReferencePair;
import io.adminshell.aas.v3.model.View;
import io.adminshell.aas.v3.model.builder.ExtendableBuilder;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Special kind of visitor that recursively walks the whole element structure and
 * applies given visitors to each element.
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
    public void visit(Constraint constraint) {
        if (constraint == null) {
            return;
        }
        Class<?> type = constraint.getClass();
        if (Qualifier.class.isAssignableFrom(type)) {
            visit((Qualifier) constraint);
        }
        else if (Formula.class.isAssignableFrom(type)) {
            visit((Formula) constraint);
        }
    }


    @Override
    public void visit(DataSpecificationContent dataSpecificationContent) {
        if (dataSpecificationContent == null) {
            return;
        }
        Class<?> type = dataSpecificationContent.getClass();
        if (DataSpecificationIEC61360.class.isAssignableFrom(type)) {
            visit((DataSpecificationIEC61360) dataSpecificationContent);
        }
    }


    @Override
    public void visit(Event event) {
        if (event == null) {
            return;
        }
        Class<?> type = event.getClass();
        if (BasicEvent.class.isAssignableFrom(type)) {
            visit((BasicEvent) event);
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
    public void visit(Asset asset) {
        visitBefore(asset);
        if (asset != null) {
            visit(asset.getAdministration());
            visit(asset.getIdentification());
            asset.getDescriptions().forEach(x -> visit(x));
            asset.getDisplayNames().forEach(x -> visit(x));
            asset.getExtensions().forEach(x -> visit(x));
            asset.getEmbeddedDataSpecifications().forEach(x -> visit(x));
        }
        visitAfter(asset);
    }


    @Override
    public void visit(Blob blob) {
        visitBefore(blob);
        if (blob != null) {
            visit(blob.getSemanticId());
            blob.getDescriptions().forEach(x -> visit(x));
            blob.getDisplayNames().forEach(x -> visit(x));
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
            visit(capability.getSemanticId());
            capability.getDescriptions().forEach(x -> visit(x));
            capability.getDisplayNames().forEach(x -> visit(x));
            capability.getQualifiers().forEach(x -> visit(x));
            capability.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            capability.getExtensions().forEach(x -> visit(x));
        }
        visitAfter(capability);
    }


    @Override
    public void visit(DataSpecificationIEC61360 dataSpecificationIEC61360) {
        visitBefore(dataSpecificationIEC61360);
        visitAfter(dataSpecificationIEC61360);
    }


    @Override
    public void visit(DataSpecificationPhysicalUnit dataSpecificationPhysicalUnit) {
        visitBefore(dataSpecificationPhysicalUnit);
        visitAfter(dataSpecificationPhysicalUnit);
    }


    @Override
    public void visit(EmbeddedDataSpecification embeddedDataSpecification) {
        visitBefore(embeddedDataSpecification);
        visitAfter(embeddedDataSpecification);
    }


    @Override
    public void visit(EventElement eventElement) {
        visitBefore(eventElement);
        visitAfter(eventElement);
    }


    @Override
    public void visit(EventMessage eventMessage) {
        visitBefore(eventMessage);
        visitAfter(eventMessage);
    }


    @Override
    public void visit(File file) {
        visitBefore(file);
        if (file != null) {
            visit(file.getSemanticId());
            file.getDescriptions().forEach(x -> visit(x));
            file.getDisplayNames().forEach(x -> visit(x));
            file.getQualifiers().forEach(x -> visit(x));
            file.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            file.getExtensions().forEach(x -> visit(x));
        }
        visitAfter(file);
    }


    @Override
    public void visit(Identifier identifier) {
        visitBefore(identifier);
        visitAfter(identifier);
    }


    @Override
    public void visit(Key key) {
        visitBefore(key);
        visitAfter(key);
    }


    @Override
    public void visit(LangString langString) {
        visitBefore(langString);
        visitAfter(langString);
    }


    @Override
    public void visit(PolicyDecisionPoint policyDecisionPoint) {
        visitBefore(policyDecisionPoint);
        visitAfter(policyDecisionPoint);
    }


    @Override
    public void visit(PolicyEnforcementPoints policyEnforcementPoints) {
        visitBefore(policyEnforcementPoints);
        visitAfter(policyEnforcementPoints);
    }


    @Override
    public void visit(Range range) {
        visitBefore(range);
        if (range != null) {
            visit(range.getSemanticId());
            range.getDescriptions().forEach(x -> visit(x));
            range.getDisplayNames().forEach(x -> visit(x));
            range.getQualifiers().forEach(x -> visit(x));
            range.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            range.getExtensions().forEach(x -> visit(x));
        }
        visitAfter(range);
    }


    @Override
    public void visit(AccessControl accessControl) {
        visitBefore(accessControl);
        if (accessControl != null) {
            visit(accessControl.getDefaultEnvironmentAttributes());
            visit(accessControl.getDefaultPermissions());
            visit(accessControl.getDefaultSubjectAttributes());
            visit(accessControl.getSelectableEnvironmentAttributes());
            visit(accessControl.getSelectablePermissions());
            visit(accessControl.getSelectableSubjectAttributes());
            accessControl.getAccessPermissionRules().forEach(x -> visit(x));
        }
        visitAfter(accessControl);
    }


    @Override
    public void visit(AccessControlPolicyPoints accessControlPolicyPoints) {
        visitBefore(accessControlPolicyPoints);
        if (accessControlPolicyPoints != null) {
            visit(accessControlPolicyPoints.getPolicyAdministrationPoint());
            visit(accessControlPolicyPoints.getPolicyDecisionPoint());
            visit(accessControlPolicyPoints.getPolicyEnforcementPoint());
            visit(accessControlPolicyPoints.getPolicyInformationPoints());
        }
        visitAfter(accessControlPolicyPoints);
    }


    @Override
    public void visit(AccessPermissionRule accessPermissionRule) {
        visitBefore(accessPermissionRule);
        if (accessPermissionRule != null) {
            visit(accessPermissionRule.getTargetSubjectAttributes());
            accessPermissionRule.getDescriptions().forEach(x -> visit(x));
            accessPermissionRule.getDisplayNames().forEach(x -> visit(x));
            accessPermissionRule.getExtensions().forEach(x -> visit(x));
            accessPermissionRule.getPermissionsPerObjects().forEach(x -> visit(x));
        }
        visitAfter(accessPermissionRule);
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
            assetAdministrationShell.getDescriptions().forEach(x -> visit(x));
            assetAdministrationShell.getDisplayNames().forEach(x -> visit(x));
            visit(assetAdministrationShell.getAdministration());
            visit(assetAdministrationShell.getIdentification());
            visit(assetAdministrationShell.getDerivedFrom());
            visit(assetAdministrationShell.getSecurity());
            visit(assetAdministrationShell.getAssetInformation());
            assetAdministrationShell.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            assetAdministrationShell.getSubmodels().forEach(x -> visit(x));
            assetAdministrationShell.getViews().forEach(x -> visit(x));
        }
        visitAfter(assetAdministrationShell);
    }


    @Override
    public void visit(AssetInformation assetInformation) {
        visitBefore(assetInformation);
        if (assetInformation != null) {
            visit(assetInformation.getGlobalAssetId());
            visit(assetInformation.getDefaultThumbnail());
            assetInformation.getSpecificAssetIds().forEach(x -> visit(x));
            assetInformation.getBillOfMaterials().forEach(x -> visit(x));
        }
        visitAfter(assetInformation);
    }


    @Override
    public void visit(BasicEvent basicEvent) {
        visitBefore(basicEvent);
        if (basicEvent != null) {
            visit(basicEvent.getSemanticId());
            basicEvent.getDescriptions().forEach(x -> visit(x));
            basicEvent.getDisplayNames().forEach(x -> visit(x));
            basicEvent.getQualifiers().forEach(x -> visit(x));
            basicEvent.getExtensions().forEach(x -> visit(x));
            basicEvent.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            visit(basicEvent.getObserved());
        }
        visitAfter(basicEvent);
    }


    @Override
    public void visit(Certificate certificate) {
        visitBefore(certificate);
        if (certificate != null) {
            visit(certificate.getPolicyAdministrationPoint());
        }
        visitAfter(certificate);
    }


    @Override
    public void visit(ConceptDescription conceptDescription) {
        visitBefore(conceptDescription);
        if (conceptDescription != null) {
            visit(conceptDescription.getAdministration());
            visit(conceptDescription.getIdentification());
            conceptDescription.getDescriptions().forEach(x -> visit(x));
            conceptDescription.getDisplayNames().forEach(x -> visit(x));
            conceptDescription.getExtensions().forEach(x -> visit(x));
            conceptDescription.getIsCaseOfs().forEach(x -> visit(x));
        }
        visitAfter(conceptDescription);
    }


    @Override
    public void visit(IdentifierKeyValuePair identifierKeyValuePair) {
        visitBefore(identifierKeyValuePair);
        if (identifierKeyValuePair != null) {
            visit(identifierKeyValuePair.getSemanticId());
            visit(identifierKeyValuePair.getExternalSubjectId());
        }
        visitAfter(identifierKeyValuePair);
    }


    @Override
    public void visit(MultiLanguageProperty multiLanguageProperty) {
        visitBefore(multiLanguageProperty);
        if (multiLanguageProperty != null) {
            visit(multiLanguageProperty.getSemanticId());
            multiLanguageProperty.getDescriptions().forEach(x -> visit(x));
            multiLanguageProperty.getDisplayNames().forEach(x -> visit(x));
            multiLanguageProperty.getQualifiers().forEach(x -> visit(x));
            multiLanguageProperty.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            multiLanguageProperty.getExtensions().forEach(x -> visit(x));
            multiLanguageProperty.getValues().forEach(x -> visit(x));
            visit(multiLanguageProperty.getValueId());
        }
        visitAfter(multiLanguageProperty);
    }


    @Override
    public void visit(ObjectAttributes objectAttributes) {
        visitBefore(objectAttributes);
        if (objectAttributes != null) {
            objectAttributes.getObjectAttributes().forEach(x -> visit(x));
        }
        visitAfter(objectAttributes);
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
    public void visit(PolicyInformationPoints policyInformationPoints) {
        visitBefore(policyInformationPoints);
        if (policyInformationPoints != null) {
            policyInformationPoints.getInternalInformationPoints().forEach(x -> visit(x));
        }
        visitAfter(policyInformationPoints);
    }


    @Override
    public void visit(Property property) {
        visitBefore(property);
        if (property != null) {
            visit(property.getSemanticId());
            property.getDescriptions().forEach(x -> visit(x));
            property.getDisplayNames().forEach(x -> visit(x));
            property.getQualifiers().forEach(x -> visit(x));
            property.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            property.getExtensions().forEach(x -> visit(x));
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
            reference.getKeys().forEach(x -> visit(x));
        }
        visitAfter(reference);
    }


    @Override
    public void visit(ReferenceElement referenceElement) {
        visitBefore(referenceElement);
        if (referenceElement != null) {
            visit(referenceElement.getSemanticId());
            referenceElement.getDescriptions().forEach(x -> visit(x));
            referenceElement.getDisplayNames().forEach(x -> visit(x));
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
                visit(relationshipElement.getSemanticId());
                relationshipElement.getDescriptions().forEach(x -> visit(x));
                relationshipElement.getDisplayNames().forEach(x -> visit(x));
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
    public void visit(Security security) {
        visitBefore(security);
        if (security != null) {
            visit(security.getAccessControlPolicyPoints());
            security.getCertificates().forEach(x -> visit(x));
            security.getRequiredCertificateExtensions().forEach(x -> visit(x));
        }
        visitAfter(security);
    }


    @Override
    public void visit(SubjectAttributes subjectAttributes) {
        visitBefore(subjectAttributes);
        if (subjectAttributes != null) {
            subjectAttributes.getSubjectAttributes().forEach(x -> visit(x));
        }
        visitAfter(subjectAttributes);
    }


    @Override
    public void visit(Permission permission) {
        visitBefore(permission);
        if (permission != null) {
            visit(permission.getPermission());
        }
        visitAfter(permission);
    }


    @Override
    public void visit(PermissionsPerObject permissionsPerObject) {
        visitBefore(permissionsPerObject);
        if (permissionsPerObject != null) {
            visit(permissionsPerObject.getObject());
            visit(permissionsPerObject.getTargetObjectAttributes());
            permissionsPerObject.getPermissions().forEach(x -> visit(x));
        }
        visitAfter(permissionsPerObject);
    }


    @Override
    public void visit(PolicyAdministrationPoint policyAdministrationPoint) {
        visitBefore(policyAdministrationPoint);
        if (policyAdministrationPoint != null) {
            visit(policyAdministrationPoint.getLocalAccessControl());
        }
        visitAfter(policyAdministrationPoint);
    }


    @Override
    public void visit(Entity entity) {
        visitBefore(entity);
        if (entity != null) {
            entity.getQualifiers().forEach(x -> visit(x));
            entity.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            entity.getExtensions().forEach(x -> visit(x));
            entity.getDescriptions().forEach(x -> visit(x));
            entity.getDisplayNames().forEach(x -> visit(x));
            visit(entity.getSemanticId());
            visit(entity.getGlobalAssetId());
            visit(entity.getSpecificAssetId());
            entity.getStatements().forEach(x -> visit(x));
        }
        visitAfter(entity);
    }


    @Override
    public void visit(Formula formula) {
        visitBefore(formula);
        if (formula != null) {
            formula.getDependsOns().forEach(x -> visit(x));
        }
        visitAfter(formula);
    }


    @Override
    public void visit(Extension extension) {
        visitBefore(extension);
        if (extension != null) {
            visit(extension.getSemanticId());
            visit(extension.getRefersTo());
        }
        visitAfter(extension);
    }


    @Override
    public void visit(AssetAdministrationShellEnvironment assetAdministrationShellEnvironment) {
        visitBefore(assetAdministrationShellEnvironment);
        if (assetAdministrationShellEnvironment != null) {
            assetAdministrationShellEnvironment.getAssetAdministrationShells().forEach(x -> visit(x));
            assetAdministrationShellEnvironment.getConceptDescriptions().forEach(x -> visit(x));
            assetAdministrationShellEnvironment.getSubmodels().forEach(x -> visit(x));
        }
        visitAfter(assetAdministrationShellEnvironment);
    }


    @Override
    public void visit(Submodel submodel) {
        visitBefore(submodel);
        if (submodel != null) {
            visit(submodel.getSemanticId());
            visit(submodel.getAdministration());
            visit(submodel.getIdentification());
            submodel.getDescriptions().forEach(x -> visit(x));
            submodel.getDisplayNames().forEach(x -> visit(x));
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
            visit(submodelElementCollection.getSemanticId());
            submodelElementCollection.getDescriptions().forEach(x -> visit(x));
            submodelElementCollection.getDisplayNames().forEach(x -> visit(x));
            submodelElementCollection.getQualifiers().forEach(x -> visit(x));
            submodelElementCollection.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            submodelElementCollection.getExtensions().forEach(x -> visit(x));
            submodelElementCollection.getValues().forEach(x -> visit(x));
        }
        visitAfter(submodelElementCollection);
    }


    @Override
    public void visit(Operation operation) {
        visitBefore(operation);
        if (operation != null) {
            visit(operation.getSemanticId());
            operation.getDescriptions().forEach(x -> visit(x));
            operation.getDisplayNames().forEach(x -> visit(x));
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
    public void visit(BlobCertificate blobCertificate) {
        visitBefore(blobCertificate);
        if (blobCertificate != null) {
            visit(blobCertificate.getBlobCertificate());
            visit(blobCertificate.getPolicyAdministrationPoint());
            blobCertificate.getContainedExtensions().forEach(x -> visit(x));
        }
        visitAfter(blobCertificate);
    }


    @Override
    public void visit(ValueList valueList) {
        visitBefore(valueList);
        if (valueList != null) {
            valueList.getValueReferencePairTypes().forEach(x -> visit(x));
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


    @Override
    public void visit(View view) {
        visitBefore(view);
        if (view != null) {
            view.getDescriptions().forEach(x -> visit(x));
            view.getDisplayNames().forEach(x -> visit(x));
            view.getEmbeddedDataSpecifications().forEach(x -> visit(x));
            view.getExtensions().forEach(x -> visit(x));
            view.getContainedElements().forEach(x -> visit(x));
        }
        visitAfter(view);
    }


    /**
     * Walks the given object.
     *
     * @param obj object to walk, must be some element of an AAS
     */
    public void walk(Object obj) {
        visit(obj);
    }


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


    protected void visitAfter(Object obj) {
        if (mode == WalkingMode.VISIT_AFTER_DESCENT) {
            walk(visitor, obj);
        }
        walk(after, obj);
    }


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
     * Enum of supported walking modes
     */
    public enum WalkingMode {
        /**
         * Visit an element after visiting all of its subelements
         */
        VISIT_AFTER_DESCENT,
        /**
         * Visit an element before visiting all of its subelements
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
