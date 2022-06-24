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

import io.adminshell.aas.v3.model.AccessPermissionRule;
import io.adminshell.aas.v3.model.Asset;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
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
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.Event;
import io.adminshell.aas.v3.model.Extension;
import io.adminshell.aas.v3.model.File;
import io.adminshell.aas.v3.model.Formula;
import io.adminshell.aas.v3.model.HasDataSpecification;
import io.adminshell.aas.v3.model.HasExtensions;
import io.adminshell.aas.v3.model.HasKind;
import io.adminshell.aas.v3.model.HasSemantics;
import io.adminshell.aas.v3.model.Identifiable;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.MultiLanguageProperty;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Qualifiable;
import io.adminshell.aas.v3.model.Qualifier;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.ReferenceElement;
import io.adminshell.aas.v3.model.RelationshipElement;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.View;


/**
 * Default implementation of {@link AssetAdministrationShellElementVisitor}
 * redirecting calls to abstract type to the concrete type. All others methods
 * are empty.
 */
public interface DefaultAssetAdministrationShellElementSubtypeResolvingVisitor extends DefaultAssetAdministrationShellElementVisitor {

    @Override
    public default void visit(Certificate certificate) {
        if (certificate == null) {
            return;
        }
        Class<?> type = certificate.getClass();
        if (BlobCertificate.class.isAssignableFrom(type)) {
            visit((BlobCertificate) certificate);
        }
    }


    @Override
    public default void visit(Constraint constraint) {
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
    public default void visit(DataElement dataElement) {
        if (dataElement == null) {
            return;
        }
        Class<?> type = dataElement.getClass();
        if (Property.class.isAssignableFrom(type)) {
            visit((Property) dataElement);
        }
        else if (MultiLanguageProperty.class.isAssignableFrom(type)) {
            visit((MultiLanguageProperty) dataElement);
        }
        else if (Range.class.isAssignableFrom(type)) {
            visit((Range) dataElement);
        }
        else if (ReferenceElement.class.isAssignableFrom(type)) {
            visit((ReferenceElement) dataElement);
        }
        else if (File.class.isAssignableFrom(type)) {
            visit((File) dataElement);
        }
        else if (Blob.class.isAssignableFrom(type)) {
            visit((Blob) dataElement);
        }
    }


    @Override
    public default void visit(DataSpecificationContent dataSpecificationContent) {
        if (dataSpecificationContent == null) {
            return;
        }
        Class<?> type = dataSpecificationContent.getClass();
        if (DataSpecificationIEC61360.class.isAssignableFrom(type)) {
            visit((DataSpecificationIEC61360) dataSpecificationContent);
        }
    }


    @Override
    public default void visit(Event event) {
        if (event == null) {
            return;
        }
        Class<?> type = event.getClass();
        if (BasicEvent.class.isAssignableFrom(type)) {
            visit((BasicEvent) event);
        }
    }


    @Override
    public default void visit(HasDataSpecification hasDataSpecification) {
        if (hasDataSpecification == null) {
            return;
        }
        Class<?> type = hasDataSpecification.getClass();
        if (AssetAdministrationShell.class.isAssignableFrom(type)) {
            visit((AssetAdministrationShell) hasDataSpecification);
        }
        else if (Submodel.class.isAssignableFrom(type)) {
            visit((Submodel) hasDataSpecification);
        }
        else if (View.class.isAssignableFrom(type)) {
            visit((View) hasDataSpecification);
        }
        else if (Asset.class.isAssignableFrom(type)) {
            visit((Asset) hasDataSpecification);
        }
        else if (SubmodelElement.class.isAssignableFrom(type)) {
            visit((SubmodelElement) hasDataSpecification);
        }

    }


    @Override
    public default void visit(HasExtensions hasExtensions) {
        if (hasExtensions == null) {
            return;
        }
        Class<?> type = hasExtensions.getClass();
        if (Referable.class.isAssignableFrom(type)) {
            visit((Referable) hasExtensions);
        }
    }


    @Override
    public default void visit(HasKind hasKind) {
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
    public default void visit(HasSemantics hasSemantics) {
        if (hasSemantics == null) {
            return;
        }
        Class<?> type = hasSemantics.getClass();
        if (Extension.class.isAssignableFrom(type)) {
            visit((Extension) hasSemantics);
        }
        else if (IdentifierKeyValuePair.class.isAssignableFrom(type)) {
            visit((IdentifierKeyValuePair) hasSemantics);
        }
        else if (Submodel.class.isAssignableFrom(type)) {
            visit((Submodel) hasSemantics);
        }
        else if (SubmodelElement.class.isAssignableFrom(type)) {
            visit((SubmodelElement) hasSemantics);
        }
        else if (View.class.isAssignableFrom(type)) {
            visit((View) hasSemantics);
        }
        else if (Qualifier.class.isAssignableFrom(type)) {
            visit((Qualifier) hasSemantics);
        }
    }


    @Override
    public default void visit(Identifiable identifiable) {
        if (identifiable == null) {
            return;
        }
        Class<?> type = identifiable.getClass();
        if (AssetAdministrationShell.class.isAssignableFrom(type)) {
            visit((AssetAdministrationShell) identifiable);
        }
        else if (Asset.class.isAssignableFrom(type)) {
            visit((Asset) identifiable);
        }
        else if (Submodel.class.isAssignableFrom(type)) {
            visit((Submodel) identifiable);
        }
        else if (ConceptDescription.class.isAssignableFrom(type)) {
            visit((ConceptDescription) identifiable);
        }
    }


    @Override
    public default void visit(SubmodelElement submodelElement) {
        if (submodelElement == null) {
            return;
        }
        Class<?> type = submodelElement.getClass();
        if (RelationshipElement.class.isAssignableFrom(type)) {
            visit((RelationshipElement) submodelElement);
        }
        else if (DataElement.class.isAssignableFrom(type)) {
            visit((DataElement) submodelElement);
        }
        else if (Capability.class.isAssignableFrom(type)) {
            visit((Capability) submodelElement);
        }
        else if (SubmodelElementCollection.class.isAssignableFrom(type)) {
            visit((SubmodelElementCollection) submodelElement);
        }
        else if (Operation.class.isAssignableFrom(type)) {
            visit((Operation) submodelElement);
        }
        else if (Event.class.isAssignableFrom(type)) {
            visit((Event) submodelElement);
        }
        else if (Entity.class.isAssignableFrom(type)) {
            visit((Entity) submodelElement);
        }
    }


    @Override
    public default void visit(Qualifiable qualifiable) {
        if (qualifiable == null) {
            return;
        }
        Class<?> type = qualifiable.getClass();
        if (Submodel.class.isAssignableFrom(type)) {
            visit((Submodel) qualifiable);
        }
        else if (SubmodelElement.class.isAssignableFrom(type)) {
            visit((SubmodelElement) qualifiable);
        }
        else if (AccessPermissionRule.class.isAssignableFrom(type)) {
            visit((AccessPermissionRule) qualifiable);
        }
    }


    @Override
    public default void visit(Referable referable) {
        if (referable == null) {
            return;
        }
        Class<?> type = referable.getClass();
        if (Identifiable.class.isAssignableFrom(type)) {
            visit((Identifiable) referable);
        }
        else if (SubmodelElement.class.isAssignableFrom(type)) {
            visit((SubmodelElement) referable);
        }
        else if (View.class.isAssignableFrom(type)) {
            visit((View) referable);
        }
        else if (AccessPermissionRule.class.isAssignableFrom(type)) {
            visit((AccessPermissionRule) referable);
        }
    }
}
