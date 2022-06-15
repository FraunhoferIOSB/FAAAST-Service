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
import io.adminshell.aas.v3.model.DataElement;
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
import io.adminshell.aas.v3.model.HasDataSpecification;
import io.adminshell.aas.v3.model.HasExtensions;
import io.adminshell.aas.v3.model.HasKind;
import io.adminshell.aas.v3.model.HasSemantics;
import io.adminshell.aas.v3.model.Identifiable;
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
import io.adminshell.aas.v3.model.Qualifiable;
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


/**
 * Default implementation of {@link AssetAdministrationShellElementVisitor} with
 * all empty methods. This is usefull when implementing only few selected
 * methods of the interface.
 */
public interface DefaultAssetAdministrationShellElementVisitor extends AssetAdministrationShellElementVisitor {

    @Override
    public default void visit(Certificate certificate) {
        // intentionally left empty
    }


    @Override
    public default void visit(Constraint constraint) {
        // intentionally left empty
    }


    @Override
    public default void visit(DataElement dataElement) {
        // intentionally left empty
    }


    @Override
    public default void visit(DataSpecificationContent dataSpecificationContent) {
        // intentionally left empty
    }


    @Override
    public default void visit(Event event) {
        // intentionally left empty
    }


    @Override
    public default void visit(HasDataSpecification hasDataSpecification) {
        // intentionally left empty
    }


    @Override
    public default void visit(HasExtensions hasExtensions) {
        // intentionally left empty
    }


    @Override
    public default void visit(HasKind hasKind) {
        // intentionally left empty
    }


    @Override
    public default void visit(HasSemantics hasSemantics) {
        // intentionally left empty
    }


    @Override
    public default void visit(Identifiable identifiable) {
        // intentionally left empty
    }


    @Override
    public default void visit(SubmodelElement submodelElement) {
        // intentionally left empty
    }


    @Override
    public default void visit(Qualifiable qualifiable) {
        // intentionally left empty
    }


    @Override
    public default void visit(Referable referable) {
        // intentionally left empty
    }


    @Override
    public default void visit(AssetAdministrationShellEnvironment assetAdministrationShellEnvironment) {
        // intentionally left empty
    }


    @Override
    public default void visit(AccessControl accessControl) {
        // intentionally left empty
    }


    @Override
    public default void visit(AccessControlPolicyPoints accessControlPolicyPoints) {
        // intentionally left empty
    }


    @Override
    public default void visit(AccessPermissionRule accessPermissionRule) {
        // intentionally left empty
    }


    @Override
    public default void visit(AdministrativeInformation administrativeInformation) {
        // intentionally left empty
    }


    @Override
    public default void visit(AnnotatedRelationshipElement annotatedRelationshipElement) {
        // intentionally left empty
    }


    @Override
    public default void visit(Asset asset) {
        // intentionally left empty
    }


    @Override
    public default void visit(AssetAdministrationShell assetAdministrationShell) {
        // intentionally left empty
    }


    @Override
    public default void visit(AssetInformation assetInformation) {
        // intentionally left empty
    }


    @Override
    public default void visit(BasicEvent basicEvent) {
        // intentionally left empty
    }


    @Override
    public default void visit(Blob blob) {
        // intentionally left empty
    }


    @Override
    public default void visit(BlobCertificate blobCertificate) {
        // intentionally left empty
    }


    @Override
    public default void visit(Capability capability) {
        // intentionally left empty
    }


    @Override
    public default void visit(ConceptDescription conceptDescription) {
        // intentionally left empty
    }


    @Override
    public default void visit(DataSpecificationIEC61360 dataSpecificationIEC61360) {
        // intentionally left empty
    }


    @Override
    public default void visit(DataSpecificationPhysicalUnit dataSpecificationPhysicalUnit) {
        // intentionally left empty
    }


    @Override
    public default void visit(EmbeddedDataSpecification embeddedDataSpecification) {
        // intentionally left empty
    }


    @Override
    public default void visit(Entity entity) {
        // intentionally left empty
    }


    @Override
    public default void visit(EventElement eventElement) {
        // intentionally left empty
    }


    @Override
    public default void visit(EventMessage eventMessage) {
        // intentionally left empty
    }


    @Override
    public default void visit(Extension extension) {
        // intentionally left empty
    }


    @Override
    public default void visit(File file) {
        // intentionally left empty
    }


    @Override
    public default void visit(Formula formula) {
        // intentionally left empty
    }


    @Override
    public default void visit(Identifier identifier) {
        // intentionally left empty
    }


    @Override
    public default void visit(IdentifierKeyValuePair identifierKeyValuePair) {
        // intentionally left empty
    }


    @Override
    public default void visit(Key key) {
        // intentionally left empty
    }


    @Override
    public default void visit(LangString langString) {
        // intentionally left empty
    }


    @Override
    public default void visit(MultiLanguageProperty multiLanguageProperty) {
        // intentionally left empty
    }


    @Override
    public default void visit(ObjectAttributes objectAttributes) {
        // intentionally left empty
    }


    @Override
    public default void visit(Operation operation) {
        // intentionally left empty
    }


    @Override
    public default void visit(OperationVariable operationVariable) {
        // intentionally left empty
    }


    @Override
    public default void visit(Permission permission) {
        // intentionally left empty
    }


    @Override
    public default void visit(PermissionsPerObject permissionsPerObject) {
        // intentionally left empty
    }


    @Override
    public default void visit(PolicyAdministrationPoint policyAdministrationPoint) {
        // intentionally left empty
    }


    @Override
    public default void visit(PolicyDecisionPoint policyDecisionPoint) {
        // intentionally left empty
    }


    @Override
    public default void visit(PolicyEnforcementPoints policyEnforcementPoints) {
        // intentionally left empty
    }


    @Override
    public default void visit(PolicyInformationPoints policyInformationPoints) {
        // intentionally left empty
    }


    @Override
    public default void visit(Property property) {
        // intentionally left empty
    }


    @Override
    public default void visit(Qualifier qualifier) {
        // intentionally left empty
    }


    @Override
    public default void visit(Range range) {
        // intentionally left empty
    }


    @Override
    public default void visit(Reference reference) {
        // intentionally left empty
    }


    @Override
    public default void visit(ReferenceElement referenceElement) {
        // intentionally left empty
    }


    @Override
    public default void visit(RelationshipElement relationshipElement) {
        // intentionally left empty
    }


    @Override
    public default void visit(Security security) {
        // intentionally left empty
    }


    @Override
    public default void visit(SubjectAttributes subjectAttributes) {
        // intentionally left empty
    }


    @Override
    public default void visit(Submodel submodel) {
        // intentionally left empty
    }


    @Override
    public default void visit(SubmodelElementCollection submodelElementCollection) {
        // intentionally left empty
    }


    @Override
    public default void visit(ValueList valueList) {
        // intentionally left empty
    }


    @Override
    public default void visit(ValueReferencePair valueReferencePair) {
        // intentionally left empty
    }


    @Override
    public default void visit(View view) {
        // intentionally left empty
    }
}
