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


public interface DefaultAssetAdministrationShellElementVisitor extends AssetAdministrationShellElementVisitor {

    @Override
    public default void visit(Certificate certificate) {}


    @Override
    public default void visit(Constraint constraint) {}


    @Override
    public default void visit(DataElement dataElement) {}


    @Override
    public default void visit(DataSpecificationContent dataSpecificationContent) {}


    @Override
    public default void visit(Event event) {}


    @Override
    public default void visit(HasDataSpecification hasDataSpecification) {}


    @Override
    public default void visit(HasExtensions hasExtensions) {}


    @Override
    public default void visit(HasKind hasKind) {}


    @Override
    public default void visit(HasSemantics hasSemantics) {}


    @Override
    public default void visit(Identifiable identifiable) {}


    @Override
    public default void visit(SubmodelElement submodelElement) {}


    @Override
    public default void visit(Qualifiable qualifiable) {}


    @Override
    public default void visit(Referable referable) {}


    @Override
    public default void visit(AssetAdministrationShellEnvironment assetAdministrationShellEnvironment) {}


    @Override
    public default void visit(AccessControl accessControl) {}


    @Override
    public default void visit(AccessControlPolicyPoints accessControlPolicyPoints) {}


    @Override
    public default void visit(AccessPermissionRule accessPermissionRule) {}


    @Override
    public default void visit(AdministrativeInformation administrativeInformation) {}


    @Override
    public default void visit(AnnotatedRelationshipElement annotatedRelationshipElement) {}


    @Override
    public default void visit(Asset asset) {}


    @Override
    public default void visit(AssetAdministrationShell assetAdministrationShell) {}


    @Override
    public default void visit(AssetInformation assetInformation) {}


    @Override
    public default void visit(BasicEvent basicEvent) {}


    @Override
    public default void visit(Blob blob) {}


    @Override
    public default void visit(BlobCertificate blobCertificate) {}


    @Override
    public default void visit(Capability capability) {}


    @Override
    public default void visit(ConceptDescription conceptDescription) {}


    @Override
    public default void visit(DataSpecificationIEC61360 dataSpecificationIEC61360) {}


    @Override
    public default void visit(DataSpecificationPhysicalUnit dataSpecificationPhysicalUnit) {}


    @Override
    public default void visit(EmbeddedDataSpecification embeddedDataSpecification) {}


    @Override
    public default void visit(Entity entity) {}


    @Override
    public default void visit(EventElement eventElement) {}


    @Override
    public default void visit(EventMessage eventMessage) {}


    @Override
    public default void visit(Extension extension) {}


    @Override
    public default void visit(File file) {}


    @Override
    public default void visit(Formula formula) {}


    @Override
    public default void visit(Identifier identifier) {}


    @Override
    public default void visit(IdentifierKeyValuePair identifierKeyValuePair) {}


    @Override
    public default void visit(Key key) {}


    @Override
    public default void visit(LangString langString) {}


    @Override
    public default void visit(MultiLanguageProperty multiLanguageProperty) {}


    @Override
    public default void visit(ObjectAttributes objectAttributes) {}


    @Override
    public default void visit(Operation operation) {}


    @Override
    public default void visit(OperationVariable operationVariable) {}


    @Override
    public default void visit(Permission permission) {}


    @Override
    public default void visit(PermissionsPerObject permissionsPerObject) {}


    @Override
    public default void visit(PolicyAdministrationPoint policyAdministrationPoint) {}


    @Override
    public default void visit(PolicyDecisionPoint policyDecisionPoint) {}


    @Override
    public default void visit(PolicyEnforcementPoints policyEnforcementPoints) {}


    @Override
    public default void visit(PolicyInformationPoints policyInformationPoints) {}


    @Override
    public default void visit(Property property) {}


    @Override
    public default void visit(Qualifier qualifier) {}


    @Override
    public default void visit(Range range) {}


    @Override
    public default void visit(Reference reference) {}


    @Override
    public default void visit(ReferenceElement referenceElement) {}


    @Override
    public default void visit(RelationshipElement relationshipElement) {}


    @Override
    public default void visit(Security security) {}


    @Override
    public default void visit(SubjectAttributes subjectAttributes) {}


    @Override
    public default void visit(Submodel submodel) {}


    @Override
    public default void visit(SubmodelElementCollection submodelElementCollection) {}


    @Override
    public default void visit(ValueList valueList) {}


    @Override
    public default void visit(ValueReferencePair valueReferencePair) {}


    @Override
    public default void visit(View view) {}
}
