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
package io.adminshell.aas.v3.model.impl;

import io.adminshell.aas.v3.model.*;
import io.adminshell.aas.v3.model.annotations.IRI;
import io.adminshell.aas.v3.model.builder.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;


/**
 * Important: This class has been patched because the equals method did not properly respect/ignore element order.
 * Default implementation of package io.adminshell.aas.v3.model.SubmodelElementCollection
 *
 * <p>A submodel element collection is a set or list of submodel elements. Constraint AASd-059: If the semanticId of a
 * SubmodelElementCollection references a ConceptDescription then the category of the ConceptDescription shall be
 * COLLECTION or ENTITY. Constraint AASd-092: If the semanticId of a SubmodelElementCollection with
 * SubmodelElementCollection/allowDuplicates == false references a ConceptDescription then the
 * ConceptDescription/category shall be ENTITY. Constraint AASd-093: If the semanticId of a SubmodelElementCollection
 * with SubmodelElementCollection/allowDuplicates == true references a ConceptDescription then the
 * ConceptDescription/category shall be COLLECTION.
 */
@IRI("aas:SubmodelElementCollection")
public class DefaultSubmodelElementCollection implements SubmodelElementCollection {

    @IRI("https://admin-shell.io/aas/3/0/RC01/HasDataSpecification/embeddedDataSpecification")
    protected List<EmbeddedDataSpecification> embeddedDataSpecifications = new ArrayList<>();

    @IRI("https://admin-shell.io/aas/3/0/RC01/HasExtensions/extension")
    protected List<Extension> extensions = new ArrayList<>();

    @IRI("https://admin-shell.io/aas/3/0/RC01/HasKind/kind")
    protected ModelingKind kind;

    @IRI("https://admin-shell.io/aas/3/0/RC01/HasSemantics/semanticId")
    protected Reference semanticId;

    @IRI("https://admin-shell.io/aas/3/0/RC01/Qualifiable/qualifier")
    protected List<Constraint> qualifiers = new ArrayList<>();

    @IRI("https://admin-shell.io/aas/3/0/RC01/Referable/category")
    protected String category;

    @IRI("https://admin-shell.io/aas/3/0/RC01/Referable/description")
    protected List<LangString> descriptions = new ArrayList<>();

    @IRI("https://admin-shell.io/aas/3/0/RC01/Referable/displayName")
    protected List<LangString> displayNames = new ArrayList<>();

    @IRI("https://admin-shell.io/aas/3/0/RC01/Referable/idShort")
    protected String idShort;

    @IRI("https://admin-shell.io/aas/3/0/RC01/SubmodelElementCollection/allowDuplicates")
    protected boolean allowDuplicates;

    @IRI("https://admin-shell.io/aas/3/0/RC01/SubmodelElementCollection/ordered")
    protected boolean ordered;

    @IRI("https://admin-shell.io/aas/3/0/RC01/SubmodelElementCollection/value")
    protected Collection<SubmodelElement> values = new ArrayList<>();

    @Override
    public int hashCode() {
        return Objects.hash(this.allowDuplicates,
                this.ordered,
                this.values,
                this.category,
                this.descriptions,
                this.displayNames,
                this.idShort,
                this.extensions,
                this.qualifiers,
                this.embeddedDataSpecifications,
                this.kind,
                this.semanticId);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (obj == null) {
            return false;
        }
        else if (this.getClass() != obj.getClass()) {
            return false;
        }
        else {
            DefaultSubmodelElementCollection other = (DefaultSubmodelElementCollection) obj;
            return Objects.equals(this.allowDuplicates, other.allowDuplicates)
                    && Objects.equals(this.ordered, other.ordered)
                    && Objects.equals(this.category, other.category)
                    && Objects.equals(this.descriptions, other.descriptions)
                    && Objects.equals(this.displayNames, other.displayNames)
                    && Objects.equals(this.idShort, other.idShort)
                    && Objects.equals(this.extensions, other.extensions)
                    && Objects.equals(this.qualifiers, other.qualifiers)
                    && Objects.equals(this.embeddedDataSpecifications, other.embeddedDataSpecifications)
                    && Objects.equals(this.kind, other.kind)
                    && Objects.equals(this.semanticId, other.semanticId)
                    // need to override default behavior as property 'ordered' should also be respected when comparing values
                    && (ordered
                            ? Objects.equals(this.values, other.values)
                            : (Objects.isNull(this.values) && Objects.isNull(other.values))
                                    || (Objects.nonNull(this.values)
                                            && Objects.nonNull(other.values)
                                            && Objects.equals(this.values.size(), other.values.size())
                                            && this.values.containsAll(other.values)
                                            && other.values.containsAll(this.values)));
        }
    }


    @Override
    public boolean getAllowDuplicates() {
        return allowDuplicates;
    }


    @Override
    public void setAllowDuplicates(boolean allowDuplicates) {
        this.allowDuplicates = allowDuplicates;
    }


    @Override
    public boolean getOrdered() {
        return ordered;
    }


    @Override
    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }


    @Override
    public Collection<SubmodelElement> getValues() {
        return values;
    }


    @Override
    public void setValues(Collection<SubmodelElement> values) {
        this.values = values;
    }


    @Override
    public String getCategory() {
        return category;
    }


    @Override
    public void setCategory(String category) {
        this.category = category;
    }


    @Override
    public List<LangString> getDescriptions() {
        return descriptions;
    }


    @Override
    public void setDescriptions(List<LangString> descriptions) {
        this.descriptions = descriptions;
    }


    @Override
    public List<LangString> getDisplayNames() {
        return displayNames;
    }


    @Override
    public void setDisplayNames(List<LangString> displayNames) {
        this.displayNames = displayNames;
    }


    @Override
    public String getIdShort() {
        return idShort;
    }


    @Override
    public void setIdShort(String idShort) {
        this.idShort = idShort;
    }


    @Override
    public List<Extension> getExtensions() {
        return extensions;
    }


    @Override
    public void setExtensions(List<Extension> extensions) {
        this.extensions = extensions;
    }


    @Override
    public List<Constraint> getQualifiers() {
        return qualifiers;
    }


    @Override
    public void setQualifiers(List<Constraint> qualifiers) {
        this.qualifiers = qualifiers;
    }


    @Override
    public List<EmbeddedDataSpecification> getEmbeddedDataSpecifications() {
        return embeddedDataSpecifications;
    }


    @Override
    public void setEmbeddedDataSpecifications(List<EmbeddedDataSpecification> embeddedDataSpecifications) {
        this.embeddedDataSpecifications = embeddedDataSpecifications;
    }


    @Override
    public ModelingKind getKind() {
        return kind;
    }


    @Override
    public void setKind(ModelingKind kind) {
        this.kind = kind;
    }


    @Override
    public Reference getSemanticId() {
        return semanticId;
    }


    @Override
    public void setSemanticId(Reference semanticId) {
        this.semanticId = semanticId;
    }

    /**
     * This builder class can be used to construct a DefaultSubmodelElementCollection bean.
     */
    public static class Builder extends SubmodelElementCollectionBuilder<DefaultSubmodelElementCollection, Builder> {

        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected DefaultSubmodelElementCollection newBuildingInstance() {
            return new DefaultSubmodelElementCollection();
        }
    }
}
