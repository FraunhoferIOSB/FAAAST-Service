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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Identifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.KeyTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceTypes;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.builder.AbstractBuilder;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultReference;


/**
 * Helper class for building {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference}.
 */
public class ReferenceBuilder extends AbstractBuilder<Reference> {

    /**
     * Builds a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} to an AAS.
     *
     * @param id the id of the AAS
     * @return the reference
     */
    public static Reference forAas(String id) {
        return new ReferenceBuilder()
                .aas(id)
                .build();
    }


    /**
     * Builds a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} to an AAS.
     *
     * @param aas the AAS
     * @return the reference
     */
    public static Reference forAas(AssetAdministrationShell aas) {
        return new ReferenceBuilder()
                .aas(aas)
                .build();
    }


    /**
     * Builds a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} to a ConceptDescription.
     *
     * @param id the id of the ConceptDescription
     * @return the reference
     */
    public static Reference forConceptDescription(String id) {
        return new ReferenceBuilder()
                .conceptDescription(id)
                .build();
    }


    /**
     * Builds a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} to a ConceptDescription.
     *
     * @param cd the ConceptDescription
     * @return the reference
     */
    public static Reference forConceptDescription(ConceptDescription cd) {
        return new ReferenceBuilder()
                .conceptDescription(cd)
                .build();
    }


    /**
     * Builds a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} to a Submodel.
     *
     * @param id the id of the submodel
     * @return the reference
     */
    public static Reference forSubmodel(String id) {
        return new ReferenceBuilder()
                .submodel(id)
                .build();
    }


    /**
     * Builds a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} to a submodel element inside a submodel.
     *
     * @param submodelId the id of the submodel
     * @param elementIds the ids of the path to the submodel element
     * @return the reference
     */
    public static Reference forSubmodel(String submodelId, String... elementIds) {
        return new ReferenceBuilder()
                .submodel(submodelId)
                .elements(elementIds)
                .build();
    }


    /**
     * Builds a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} to a submodel element inside a submodel.
     *
     * @param submodelId the id of the submodel
     * @param elements the submodel elements in the path
     * @return the reference
     */
    public static Reference forSubmodel(String submodelId, SubmodelElement... elements) {
        return new ReferenceBuilder()
                .submodel(submodelId)
                .elements(elements)
                .build();
    }


    /**
     * Builds a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} to a Submodel.
     *
     * @param submodel the submodel
     * @return the reference
     */
    public static Reference forSubmodel(Submodel submodel) {
        return new ReferenceBuilder()
                .submodel(submodel)
                .build();
    }


    /**
     * Builds a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} to a submodel element inside a submodel.
     *
     * @param submodel the submodel
     * @param elementIds the ids of the path to the submodel element
     * @return the reference
     */
    public static Reference forSubmodel(Submodel submodel, String... elementIds) {
        return new ReferenceBuilder()
                .submodel(submodel)
                .elements(elementIds)
                .build();
    }


    /**
     * Builds a {@link org.eclipse.digitaltwin.aas4j.v3.model.Reference} to a submodel element inside a submodel.
     *
     * @param submodel the submodel
     * @param elements the submodel elements in the path
     * @return the reference
     */
    public static Reference forSubmodel(Submodel submodel, SubmodelElement... elements) {
        return new ReferenceBuilder()
                .submodel(submodel)
                .elements(elements)
                .build();
    }


    /**
     * Creates a reference by concatenating a parent reference with submodel element ids. Only leading keys pointing to
     * an identifiable are kept from the parent reference.
     *
     * @param parent the parent reference
     * @param elementIds the submodel element Ids
     * @return the new reference
     */
    public static Reference forParent(Reference parent, String... elementIds) {
        return new ReferenceBuilder()
                .identifiables(parent)
                .elements(elementIds)
                .build();
    }


    /**
     * Creates a reference by concatenating a parent reference with submodel element ids. Only leading keys pointing to
     * an identifiable are kept from the parent reference.
     *
     * @param parent the parent reference
     * @param elements the submodel elements
     * @return the new reference
     */
    public static Reference forParent(Reference parent, SubmodelElement... elements) {
        return new ReferenceBuilder()
                .identifiables(parent)
                .elements(elements)
                .build();
    }


    /**
     * Creates a global reference.
     *
     * @param value the value
     * @return the reference
     */
    public static Reference global(String value) {
        return new ReferenceBuilder()
                .element(value, KeyTypes.GLOBAL_REFERENCE)
                .build();
    }


    /**
     * Create a builder with an existing Reference as base, i.e. the builder will only add to the existing reference.
     *
     * @param reference the base reference
     * @return a new builder base of the existing reference
     */
    public static ReferenceBuilder with(Reference reference) {
        ReferenceBuilder result = new ReferenceBuilder();
        if (Objects.isNull(reference)) {
            return result;
        }
        Reference clone = ReferenceHelper.clone(reference);
        result.referredSemanticId(clone.getReferredSemanticId());
        result.type(clone.getType());
        result.getBuildingInstance().setKeys(clone.getKeys());
        return result;
    }


    /**
     * Builds a new reference.
     *
     * @return the new reference
     */
    @Override
    public Reference build() {
        getBuildingInstance().setType(ReferenceHelper.determineReferenceType(getBuildingInstance()));
        return getBuildingInstance();
    }


    /**
     * Add an AAS to the reference.
     *
     * @param id the id
     * @return the builder
     */
    public ReferenceBuilder aas(String id) {
        return identifiable(id, KeyTypes.ASSET_ADMINISTRATION_SHELL);
    }


    /**
     * Add an AAS to the reference.
     *
     * @param aas the AAS
     * @return the builder
     */
    public ReferenceBuilder aas(AssetAdministrationShell aas) {
        return identifiable(aas);
    }


    /**
     * Add a Conceptdescription to the reference.
     *
     * @param id the id
     * @return the builder
     */
    public ReferenceBuilder conceptDescription(String id) {
        return identifiable(id, KeyTypes.CONCEPT_DESCRIPTION);
    }


    /**
     * Add a Conceptdescription to the reference.
     *
     * @param cd the Conceptdescription
     * @return the builder
     */
    public ReferenceBuilder conceptDescription(ConceptDescription cd) {
        return identifiable(cd);
    }


    /**
     * Add a Submodel to the reference.
     *
     * @param id the id
     * @return the builder
     */
    public ReferenceBuilder submodel(String id) {
        return identifiable(id, KeyTypes.SUBMODEL);
    }


    /**
     * Add a Submodel the reference.
     *
     * @param submodel the Submodel
     * @return the builder
     */
    public ReferenceBuilder submodel(Submodel submodel) {
        return identifiable(submodel);
    }


    /**
     * Add an Identifiable to the reference with given key type.
     *
     * @param id the id
     * @param type the key type
     * @return the builder
     */
    public ReferenceBuilder identifiable(String id, KeyTypes type) {
        getBuildingInstance().getKeys().add(ReferenceHelper.newKey(type, id));
        return this;
    }


    /**
     * Add an Identifiable to the reference.
     *
     * @param identifiable the Identifiable
     * @return the builder
     */
    public ReferenceBuilder identifiable(Identifiable identifiable) {
        if (Objects.isNull(identifiable)) {
            return this;
        }
        return identifiable(identifiable.getId(), ReferenceHelper.toKeyType(identifiable.getClass()));
    }


    /**
     * Extracts the leading keys pointing to an identifiable from the reference and adds them.
     *
     * @param reference the reference
     * @return the builder
     */
    public ReferenceBuilder identifiables(Reference reference) {
        if (Objects.isNull(reference)
                || Objects.isNull(reference.getKeys())
                || reference.getKeys().isEmpty()) {
            return this;
        }
        for (int i = 0; i < reference.getKeys().size(); i++) {
            if (Objects.isNull(reference.getKeys().get(i))) {
                return this;
            }
            KeyTypes currentType = reference.getKeys().get(i).getType();
            if (!isIdentifiable(currentType)) {
                return this;
            }
            getBuildingInstance().getKeys().add(ReferenceHelper.newKey(currentType, reference.getKeys().get(i).getValue()));
        }
        return this;
    }


    /**
     * Adds a submodel element to the reference.
     *
     * @param id the id
     * @return the builder
     */
    public ReferenceBuilder element(String id) {
        getBuildingInstance().getKeys().add(ReferenceHelper.newKey(KeyTypes.SUBMODEL_ELEMENT, id));
        return this;
    }


    /**
     * Adds submodel elements to the reference.
     *
     * @param ids the ids to add
     * @return the builder
     */
    public ReferenceBuilder elements(String... ids) {
        if (Objects.isNull(ids)) {
            return this;
        }
        return elements(Arrays.asList(ids));
    }


    /**
     * Adds submodel elements to the reference.
     *
     * @param ids the ids to add
     * @return the builder
     */
    public ReferenceBuilder elements(List<String> ids) {
        if (Objects.isNull(ids)) {
            return this;
        }
        getBuildingInstance().getKeys().addAll(
                ids.stream()
                        .map(x -> ReferenceHelper.newKey(KeyTypes.SUBMODEL_ELEMENT, x))
                        .collect(Collectors.toList()));
        return this;
    }


    /**
     * Adds a submodel element with given key type to the reference.
     *
     * @param id the id
     * @param type the key type
     * @return the builder
     */
    public ReferenceBuilder element(String id, KeyTypes type) {
        getBuildingInstance().getKeys().add(ReferenceHelper.newKey(type, id));
        return this;
    }


    /**
     * Adds a submodel element with given key type to the reference.
     *
     * @param id the id
     * @param type the Java class to deduce the key type from
     * @return the builder
     */
    public ReferenceBuilder element(String id, Class<?> type) {
        getBuildingInstance().getKeys().add(ReferenceHelper.newKey(ReferenceHelper.toKeyType(type), id));
        return this;
    }


    /**
     * Adds a submodel element to the reference.
     *
     * @param element the submodel element
     * @return the builder
     */
    public ReferenceBuilder element(SubmodelElement element) {
        return elements(element);
    }


    /**
     * Adds submodel elements to the reference.
     *
     * @param elements the elements to add
     * @return the builder
     */
    public ReferenceBuilder elements(SubmodelElement... elements) {
        getBuildingInstance().getKeys().addAll(
                Stream.of(elements)
                        .map(x -> ReferenceHelper.newKey(ReferenceHelper.toKeyType(x.getClass()), x.getIdShort()))
                        .collect(Collectors.toList()));
        return this;
    }


    /**
     * Adds submodel elements from an idShortPath to the reference.
     *
     * @param path the idShortPath
     * @return the builder
     */
    public ReferenceBuilder idShortPath(String path) {
        return idShortPath(IdShortPath.parse(path));
    }


    /**
     * Adds submodel elements from an idShortPath to the reference.
     *
     * @param path the idShortPath
     * @return the builder
     */
    public ReferenceBuilder idShortPath(IdShortPath path) {
        if (Objects.isNull(path)) {
            return this;
        }
        getBuildingInstance().getKeys().addAll(path.toReference().getKeys());
        return this;
    }


    /**
     * Adds a key representing a SubmodelElement with a given index inside a SubmodelElementList.
     *
     * @param index the index
     * @return the builder
     */
    public ReferenceBuilder index(int index) {
        return element(Integer.toString(index));
    }


    /**
     * Adds a key representing a SubmodelElement with a given index inside a SubmodelElementList using a given key type.
     *
     * @param index the index
     * @param type the key type
     * @return the builder
     */
    public ReferenceBuilder index(int index, KeyTypes type) {
        return element(Integer.toString(index), type);
    }


    /**
     * Sets the type of the reference.
     *
     * @param type the type
     * @return the builder
     */
    public ReferenceBuilder type(ReferenceTypes type) {
        getBuildingInstance().setType(type);
        return this;
    }


    /**
     * Sets the referredSemanticId of the reference.
     *
     * @param referredSemanticId the referredSemanticId
     * @return the builder
     */
    public ReferenceBuilder referredSemanticId(Reference referredSemanticId) {
        getBuildingInstance().setReferredSemanticId(referredSemanticId);
        return this;
    }


    private static boolean isIdentifiable(KeyTypes type) {
        switch (type) {
            case IDENTIFIABLE:
            case CONCEPT_DESCRIPTION:
            case ASSET_ADMINISTRATION_SHELL:
            case SUBMODEL:
                return true;
            default:
                return false;
        }
    }


    @Override
    protected Reference newBuildingInstance() {
        return new DefaultReference();
    }
}
