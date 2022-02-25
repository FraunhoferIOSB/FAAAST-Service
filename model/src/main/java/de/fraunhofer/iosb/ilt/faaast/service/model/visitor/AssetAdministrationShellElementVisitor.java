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
 * Visitor for elements of an
 * {@link io.adminshell.aas.v3.model.AssetAdministrationShell}
 */
public interface AssetAdministrationShellElementVisitor {

    public void visit(Certificate certificate);


    public void visit(Constraint constraint);


    public void visit(DataElement dataElement);


    public void visit(DataSpecificationContent dataSpecificationContent);


    public void visit(Event event);


    public void visit(HasDataSpecification hasDataSpecification);


    public void visit(HasExtensions hasExtensions);


    public void visit(HasKind hasKind);


    public void visit(HasSemantics hasSemantics);


    public void visit(Identifiable identifiable);


    public void visit(SubmodelElement submodelElement);


    public void visit(Qualifiable qualifiable);


    public void visit(Referable referable);


    public void visit(AssetAdministrationShellEnvironment assetAdministrationShellEnvironment);


    public void visit(AccessControl accessControl);


    public void visit(AccessControlPolicyPoints accessControlPolicyPoints);


    public void visit(AccessPermissionRule accessPermissionRule);


    public void visit(AdministrativeInformation administrativeInformation);


    public void visit(AnnotatedRelationshipElement annotatedRelationshipElement);


    public void visit(Asset asset);


    public void visit(AssetAdministrationShell assetAdministrationShell);


    public void visit(AssetInformation assetInformation);


    public void visit(BasicEvent basicEvent);


    public void visit(Blob blob);


    public void visit(BlobCertificate blobCertificate);


    public void visit(Capability capability);


    public void visit(ConceptDescription conceptDescription);


    public void visit(DataSpecificationIEC61360 dataSpecificationIEC61360);


    public void visit(DataSpecificationPhysicalUnit dataSpecificationPhysicalUnit);


    public void visit(EmbeddedDataSpecification embeddedDataSpecification);


    public void visit(Entity entity);


    public void visit(EventElement eventElement);


    public void visit(EventMessage eventMessage);


    public void visit(Extension extension);


    public void visit(File file);


    public void visit(Formula formula);


    public void visit(Identifier identifier);


    public void visit(IdentifierKeyValuePair identifierKeyValuePair);


    public void visit(Key key);


    public void visit(LangString langString);


    public void visit(MultiLanguageProperty multiLanguageProperty);


    public void visit(ObjectAttributes objectAttributes);


    public void visit(Operation operation);


    public void visit(OperationVariable operationVariable);


    public void visit(Permission permission);


    public void visit(PermissionsPerObject permissionsPerObject);


    public void visit(PolicyAdministrationPoint policyAdministrationPoint);


    public void visit(PolicyDecisionPoint policyDecisionPoint);


    public void visit(PolicyEnforcementPoints policyEnforcementPoints);


    public void visit(PolicyInformationPoints policyInformationPoints);


    public void visit(Property property);


    public void visit(Qualifier qualifier);


    public void visit(Range range);


    public void visit(Reference reference);


    public void visit(ReferenceElement referenceElement);


    public void visit(RelationshipElement relationshipElement);


    public void visit(Security security);


    public void visit(SubjectAttributes subjectAttributes);


    public void visit(Submodel submodel);


    public void visit(SubmodelElementCollection submodelElementCollection);


    public void visit(ValueList valueList);


    public void visit(ValueReferencePair valueReferencePair);


    public void visit(View view);
}
