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
 * Default implementation of {@link AssetAdministrationShellElementVisitor} with all empty methods. This is usefull when
 * implementing only few selected methods of the interface.
 */
public interface DefaultAssetAdministrationShellElementVisitor extends AssetAdministrationShellElementVisitor {

    @Override
    public default void visit(DataElement dataElement) {
        // intentionally left empty
    }


    @Override
    public default void visit(DataSpecificationContent dataSpecificationContent) {
        // intentionally left empty
    }


    @Override
    public default void visit(EventElement event) {
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
    public default void visit(Environment assetAdministrationShellEnvironment) {
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
    public default void visit(AssetAdministrationShell assetAdministrationShell) {
        // intentionally left empty
    }


    @Override
    public default void visit(AssetInformation assetInformation) {
        // intentionally left empty
    }


    @Override
    public default void visit(BasicEventElement basicEventElement) {
        // intentionally left empty
    }


    @Override
    public default void visit(Blob blob) {
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
    public default void visit(DataSpecificationIec61360 dataSpecificationIec61360) {
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
    public default void visit(EventPayload eventPayload) {
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
    public default void visit(SpecificAssetId specificAssetId) {
        // intentionally left empty
    }


    @Override
    public default void visit(Key key) {
        // intentionally left empty
    }


    @Override
    public default void visit(LangStringTextType langString) {
        // intentionally left empty
    }


    @Override
    public default void visit(MultiLanguageProperty multiLanguageProperty) {
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
    public default void visit(Submodel submodel) {
        // intentionally left empty
    }


    @Override
    public default void visit(SubmodelElementCollection submodelElementCollection) {
        // intentionally left empty
    }


    @Override
    public default void visit(SubmodelElementList submodelElementList) {
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
}
