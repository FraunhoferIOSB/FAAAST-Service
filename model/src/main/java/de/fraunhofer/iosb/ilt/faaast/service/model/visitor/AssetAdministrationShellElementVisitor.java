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

    /**
     * Visit certificate
     *
     * @param certificate the certificate
     */
    public void visit(Certificate certificate);


    /**
     * Visit constraint
     *
     * @param constraint the constraint
     */
    public void visit(Constraint constraint);


    /**
     * Visit dataElement
     *
     * @param dataElement the dataElement
     */
    public void visit(DataElement dataElement);


    /**
     * Visit dataSpecificationContent
     *
     * @param dataSpecificationContent the dataSpecificationContent
     */
    public void visit(DataSpecificationContent dataSpecificationContent);


    /**
     * Visit event
     *
     * @param event the event
     */
    public void visit(Event event);


    /**
     * Visit hasDataSpecification
     *
     * @param hasDataSpecification the hasDataSpecification
     */
    public void visit(HasDataSpecification hasDataSpecification);


    /**
     * Visit hasExtensions
     *
     * @param hasExtensions the hasExtensions
     */
    public void visit(HasExtensions hasExtensions);


    /**
     * Visit hasKind
     *
     * @param hasKind the hasKind
     */
    public void visit(HasKind hasKind);


    /**
     * Visit hasSemantics
     *
     * @param hasSemantics the hasSemantics
     */
    public void visit(HasSemantics hasSemantics);


    /**
     * Visit identifiable
     *
     * @param identifiable the identifiable
     */
    public void visit(Identifiable identifiable);


    /**
     * Visit submodelElement
     *
     * @param submodelElement the submodelElement
     */
    public void visit(SubmodelElement submodelElement);


    /**
     * Visit qualifiable
     *
     * @param qualifiable the qualifiable
     */
    public void visit(Qualifiable qualifiable);


    /**
     * Visit referable
     *
     * @param referable the referable
     */
    public void visit(Referable referable);


    /**
     * Visit assetAdministrationShellEnvironment
     *
     * @param assetAdministrationShellEnvironment the
     *            assetAdministrationShellEnvironment
     */
    public void visit(AssetAdministrationShellEnvironment assetAdministrationShellEnvironment);


    /**
     * Visit accessControl
     *
     * @param accessControl the accessControl
     */
    public void visit(AccessControl accessControl);


    /**
     * Visit accessControlPolicyPoints
     *
     * @param accessControlPolicyPoints the accessControlPolicyPoints
     */
    public void visit(AccessControlPolicyPoints accessControlPolicyPoints);


    /**
     * Visit accessPermissionRule
     *
     * @param accessPermissionRule the accessPermissionRule
     */
    public void visit(AccessPermissionRule accessPermissionRule);


    /**
     * Visit administrativeInformation
     *
     * @param administrativeInformation the administrativeInformation
     */
    public void visit(AdministrativeInformation administrativeInformation);


    /**
     * Visit annotatedRelationshipElement
     *
     * @param annotatedRelationshipElement the annotatedRelationshipElement
     */
    public void visit(AnnotatedRelationshipElement annotatedRelationshipElement);


    /**
     * Visit asset
     *
     * @param asset the asset
     */
    public void visit(Asset asset);


    /**
     * Visit assetAdministrationShell
     *
     * @param assetAdministrationShell the assetAdministrationShell
     */
    public void visit(AssetAdministrationShell assetAdministrationShell);


    /**
     * Visit assetInformation
     *
     * @param assetInformation the assetInformation
     */
    public void visit(AssetInformation assetInformation);


    /**
     * Visit basicEvent
     *
     * @param basicEvent the basicEvent
     */
    public void visit(BasicEvent basicEvent);


    /**
     * Visit blob
     *
     * @param blob the blob
     */
    public void visit(Blob blob);


    /**
     * Visit blobCertificate
     *
     * @param blobCertificate the blobCertificate
     */
    public void visit(BlobCertificate blobCertificate);


    /**
     * Visit capability
     *
     * @param capability the capability
     */
    public void visit(Capability capability);


    /**
     * Visit conceptDescription
     *
     * @param conceptDescription the conceptDescription
     */
    public void visit(ConceptDescription conceptDescription);


    /**
     * Visit dataSpecificationIEC61360
     *
     * @param dataSpecificationIEC61360 the dataSpecificationIEC61360
     */
    public void visit(DataSpecificationIEC61360 dataSpecificationIEC61360);


    /**
     * Visit dataSpecificationPhysicalUnit
     *
     * @param dataSpecificationPhysicalUnit the dataSpecificationPhysicalUnit
     */
    public void visit(DataSpecificationPhysicalUnit dataSpecificationPhysicalUnit);


    /**
     * Visit embeddedDataSpecification
     *
     * @param embeddedDataSpecification the embeddedDataSpecification
     */
    public void visit(EmbeddedDataSpecification embeddedDataSpecification);


    /**
     * Visit entity
     *
     * @param entity the entity
     */
    public void visit(Entity entity);


    /**
     * Visit eventElement
     *
     * @param eventElement the eventElement
     */
    public void visit(EventElement eventElement);


    /**
     * Visit eventMessage
     *
     * @param eventMessage the eventMessage
     */
    public void visit(EventMessage eventMessage);


    /**
     * Visit extension
     *
     * @param extension the extension
     */
    public void visit(Extension extension);


    /**
     * Visit file
     *
     * @param file the file
     */
    public void visit(File file);


    /**
     * Visit formula
     *
     * @param formula the formula
     */
    public void visit(Formula formula);


    /**
     * Visit identifier
     *
     * @param identifier the identifier
     */
    public void visit(Identifier identifier);


    /**
     * Visit identifierKeyValuePair
     *
     * @param identifierKeyValuePair the identifierKeyValuePair
     */
    public void visit(IdentifierKeyValuePair identifierKeyValuePair);


    /**
     * Visit key
     *
     * @param key the key
     */
    public void visit(Key key);


    /**
     * Visit langString
     *
     * @param langString the langString
     */
    public void visit(LangString langString);


    /**
     * Visit multiLanguageProperty
     *
     * @param multiLanguageProperty the multiLanguageProperty
     */
    public void visit(MultiLanguageProperty multiLanguageProperty);


    /**
     * Visit objectAttributes
     *
     * @param objectAttributes the objectAttributes
     */
    public void visit(ObjectAttributes objectAttributes);


    /**
     * Visit operation
     *
     * @param operation the operation
     */
    public void visit(Operation operation);


    /**
     * Visit operationVariable
     *
     * @param operationVariable the operationVariable
     */
    public void visit(OperationVariable operationVariable);


    /**
     * Visit permission
     *
     * @param permission the permission
     */
    public void visit(Permission permission);


    /**
     * Visit permissionsPerObject
     *
     * @param permissionsPerObject the permissionsPerObject
     */
    public void visit(PermissionsPerObject permissionsPerObject);


    /**
     * Visit policyAdministrationPoint
     *
     * @param policyAdministrationPoint the policyAdministrationPoint
     */
    public void visit(PolicyAdministrationPoint policyAdministrationPoint);


    /**
     * Visit policyDecisionPoint
     *
     * @param policyDecisionPoint the policyDecisionPoint
     */
    public void visit(PolicyDecisionPoint policyDecisionPoint);


    /**
     * Visit policyEnforcementPoints
     *
     * @param policyEnforcementPoints the policyEnforcementPoints
     */
    public void visit(PolicyEnforcementPoints policyEnforcementPoints);


    /**
     * Visit policyInformationPoints
     *
     * @param policyInformationPoints the policyInformationPoints
     */
    public void visit(PolicyInformationPoints policyInformationPoints);


    /**
     * Visit property
     *
     * @param property the property
     */
    public void visit(Property property);


    /**
     * Visit qualifier
     *
     * @param qualifier the qualifier
     */
    public void visit(Qualifier qualifier);


    /**
     * Visit range
     *
     * @param range the range
     */
    public void visit(Range range);


    /**
     * Visit reference
     *
     * @param reference the reference
     */
    public void visit(Reference reference);


    /**
     * Visit referenceElement
     *
     * @param referenceElement the referenceElement
     */
    public void visit(ReferenceElement referenceElement);


    /**
     * Visit relationshipElement
     *
     * @param relationshipElement the relationshipElement
     */
    public void visit(RelationshipElement relationshipElement);


    /**
     * Visit security
     *
     * @param security the security
     */
    public void visit(Security security);


    /**
     * Visit subjectAttributes
     *
     * @param subjectAttributes the subjectAttributes
     */
    public void visit(SubjectAttributes subjectAttributes);


    /**
     * Visit submodel
     *
     * @param submodel the submodel
     */
    public void visit(Submodel submodel);


    /**
     * Visit submodelElementCollection
     *
     * @param submodelElementCollection the submodelElementCollection
     */
    public void visit(SubmodelElementCollection submodelElementCollection);


    /**
     * Visit valueList
     *
     * @param valueList the valueList
     */
    public void visit(ValueList valueList);


    /**
     * Visit valueReferencePair
     *
     * @param valueReferencePair the valueReferencePair
     */
    public void visit(ValueReferencePair valueReferencePair);


    /**
     * Visit view
     *
     * @param view the view
     */
    public void visit(View view);
}
