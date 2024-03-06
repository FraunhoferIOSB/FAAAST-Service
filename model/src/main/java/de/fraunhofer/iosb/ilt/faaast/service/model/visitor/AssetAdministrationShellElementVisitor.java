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

import org.eclipse.digitaltwin.aas4j.v3.model.AdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.BasicEventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;
import org.eclipse.digitaltwin.aas4j.v3.model.Capability;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.DataElement;
import org.eclipse.digitaltwin.aas4j.v3.model.DataSpecificationContent;
import org.eclipse.digitaltwin.aas4j.v3.model.DataSpecificationIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.EmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.Entity;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.EventElement;
import org.eclipse.digitaltwin.aas4j.v3.model.EventPayload;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.File;
import org.eclipse.digitaltwin.aas4j.v3.model.HasDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.HasExtensions;
import org.eclipse.digitaltwin.aas4j.v3.model.HasKind;
import org.eclipse.digitaltwin.aas4j.v3.model.HasSemantics;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifiable;
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


/**
 * Visitor for elements of an {@link org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell}.
 */
public interface AssetAdministrationShellElementVisitor {

    /**
     * Visit dataElement.
     *
     * @param dataElement the dataElement
     */
    public void visit(DataElement dataElement);


    /**
     * Visit dataSpecificationContent.
     *
     * @param dataSpecificationContent the dataSpecificationContent
     */
    public void visit(DataSpecificationContent dataSpecificationContent);


    /**
     * Visit hasDataSpecification.
     *
     * @param hasDataSpecification the hasDataSpecification
     */
    public void visit(HasDataSpecification hasDataSpecification);


    /**
     * Visit hasExtensions.
     *
     * @param hasExtensions the hasExtensions
     */
    public void visit(HasExtensions hasExtensions);


    /**
     * Visit hasKind.
     *
     * @param hasKind the hasKind
     */
    public void visit(HasKind hasKind);


    /**
     * Visit hasSemantics.
     *
     * @param hasSemantics the hasSemantics
     */
    public void visit(HasSemantics hasSemantics);


    /**
     * Visit identifiable.
     *
     * @param identifiable the identifiable
     */
    public void visit(Identifiable identifiable);


    /**
     * Visit submodelElement.
     *
     * @param submodelElement the submodelElement
     */
    public void visit(SubmodelElement submodelElement);


    /**
     * Visit qualifiable.
     *
     * @param qualifiable the qualifiable
     */
    public void visit(Qualifiable qualifiable);


    /**
     * Visit referable.
     *
     * @param referable the referable
     */
    public void visit(Referable referable);


    /**
     * Visit assetAdministrationShellEnvironment.
     *
     * @param assetAdministrationShellEnvironment the assetAdministrationShellEnvironment
     */
    public void visit(Environment assetAdministrationShellEnvironment);


    /**
     * Visit administrativeInformation.
     *
     * @param administrativeInformation the administrativeInformation
     */
    public void visit(AdministrativeInformation administrativeInformation);


    /**
     * Visit annotatedRelationshipElement.
     *
     * @param annotatedRelationshipElement the annotatedRelationshipElement
     */
    public void visit(AnnotatedRelationshipElement annotatedRelationshipElement);


    /**
     * Visit assetAdministrationShell.
     *
     * @param assetAdministrationShell the assetAdministrationShell
     */
    public void visit(AssetAdministrationShell assetAdministrationShell);


    /**
     * Visit assetInformation.
     *
     * @param assetInformation the assetInformation
     */
    public void visit(AssetInformation assetInformation);


    /**
     * Visit basicEvent.
     *
     * @param basicEvent the basicEvent
     */
    public void visit(BasicEventElement basicEvent);


    /**
     * Visit blob.
     *
     * @param blob the blob
     */
    public void visit(Blob blob);


    /**
     * Visit capability.
     *
     * @param capability the capability
     */
    public void visit(Capability capability);


    /**
     * Visit conceptDescription.
     *
     * @param conceptDescription the conceptDescription
     */
    public void visit(ConceptDescription conceptDescription);


    /**
     * Visit DataSpecificationIec61360.
     *
     * @param dataSpecificationIec61360 the dataSpecificationIec61360
     */
    public void visit(DataSpecificationIec61360 dataSpecificationIec61360);


    /**
     * Visit embeddedDataSpecification.
     *
     * @param embeddedDataSpecification the embeddedDataSpecification
     */
    public void visit(EmbeddedDataSpecification embeddedDataSpecification);


    /**
     * Visit entity.
     *
     * @param entity the entity
     */
    public void visit(Entity entity);


    /**
     * Visit eventElement.
     *
     * @param eventElement the eventElement
     */
    public void visit(EventElement eventElement);


    /**
     * Visit eventPayload.
     *
     * @param eventPayload the eventPayload
     */
    public void visit(EventPayload eventPayload);


    /**
     * Visit extension.
     *
     * @param extension the extension
     */
    public void visit(Extension extension);


    /**
     * Visit file.
     *
     * @param file the file
     */
    public void visit(File file);


    /**
     * Visit specificAssetID.
     *
     * @param specificAssetID the specificAssetID
     */
    public void visit(SpecificAssetId specificAssetID);


    /**
     * Visit key.
     *
     * @param key the key
     */
    public void visit(Key key);


    /**
     * Visit langString.
     *
     * @param langString the langString
     */
    public void visit(LangStringTextType langString);


    /**
     * Visit multiLanguageProperty.
     *
     * @param multiLanguageProperty the multiLanguageProperty
     */
    public void visit(MultiLanguageProperty multiLanguageProperty);


    /**
     * Visit operation.
     *
     * @param operation the operation
     */
    public void visit(Operation operation);


    /**
     * Visit operationVariable.
     *
     * @param operationVariable the operationVariable
     */
    public void visit(OperationVariable operationVariable);


    /**
     * Visit property.
     *
     * @param property the property
     */
    public void visit(Property property);


    /**
     * Visit qualifier.
     *
     * @param qualifier the qualifier
     */
    public void visit(Qualifier qualifier);


    /**
     * Visit range.
     *
     * @param range the range
     */
    public void visit(Range range);


    /**
     * Visit reference.
     *
     * @param reference the reference
     */
    public void visit(Reference reference);


    /**
     * Visit referenceElement.
     *
     * @param referenceElement the referenceElement
     */
    public void visit(ReferenceElement referenceElement);


    /**
     * Visit relationshipElement.
     *
     * @param relationshipElement the relationshipElement
     */
    public void visit(RelationshipElement relationshipElement);


    /**
     * Visit submodel.
     *
     * @param submodel the submodel
     */
    public void visit(Submodel submodel);


    /**
     * Visit submodelElementCollection.
     *
     * @param submodelElementCollection the submodelElementCollection
     */
    public void visit(SubmodelElementCollection submodelElementCollection);


    /**
     * Visit submodelElementList.
     *
     * @param submodelElementList the submodelElementList
     */
    public void visit(SubmodelElementList submodelElementList);


    /**
     * Visit valueList.
     *
     * @param valueList the valueList
     */
    public void visit(ValueList valueList);


    /**
     * Visit valueReferencePair.
     *
     * @param valueReferencePair the valueReferencePair
     */
    public void visit(ValueReferencePair valueReferencePair);

}
