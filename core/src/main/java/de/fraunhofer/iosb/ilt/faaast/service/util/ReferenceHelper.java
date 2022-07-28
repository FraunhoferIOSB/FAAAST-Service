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

import de.fraunhofer.iosb.ilt.faaast.service.exception.ResourceNotFoundException;
import io.adminshell.aas.v3.dataformat.core.ReflectionHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Helper class with methods to handle with
 * <ul>
 * <li>{@link io.adminshell.aas.v3.model.Reference}
 * <li>{@link io.adminshell.aas.v3.model.Key}
 * </ul>
 */
public class ReferenceHelper {

    private ReferenceHelper() {}


    /**
     * Compares a reference and a list of keys to equality. Ignores the key
     * types of the keys of both parameters.
     *
     * @param reference parameter 1
     * @param keys parameter 2
     * @return true if the reference contains the same keys as in the specified
     *         list and vice versa. Otherwise, false.
     */
    public static boolean isEqualsIgnoringKeyType(Reference reference, List<Key> keys) {
        if (reference == null || reference.getKeys() == null || keys == null) {
            if (reference == null && keys == null) {
                return true;
            }
            else if (reference != null) {
                return reference.getKeys() == null && keys == null;
            }
            return false;
        }
        List<Key> deepCopiedKeys1 = new ArrayList<>();
        List<Key> deepCopiedKeys2 = new ArrayList<>();
        reference.getKeys().forEach(x -> deepCopiedKeys1.add(deepCopyKeyWithoutKeyElement(x)));
        keys.forEach(x -> deepCopiedKeys2.add(deepCopyKeyWithoutKeyElement(x)));
        return deepCopiedKeys1.containsAll(deepCopiedKeys2) && deepCopiedKeys2.containsAll(deepCopiedKeys1);
    }


    /**
     * Compares two references to equality. Ignores the key types of the keys of
     * both parameters.
     *
     * @param reference parameter 1
     * @param reference1 parameter 2
     * @return true if both references contains the same keys. Otherwise, false.
     */
    public static boolean isEqualsIgnoringKeyType(Reference reference, Reference reference1) {
        return isEqualsIgnoringKeyType(reference, reference1.getKeys());
    }


    private static Key deepCopyKeyWithoutKeyElement(Key k) {
        return new DefaultKey.Builder()
                .value(k.getValue())
                .idType(k.getIdType())
                .build();
    }


    /**
     * Browse the keys of a reference and try to find the referenced element in
     * the asset administration shell environment to set the right
     * {@link io.adminshell.aas.v3.model.KeyElements} of the key. All key types
     * must be null or SUBMODEL_ELEMENT.
     *
     * @param reference with keys which should be completed
     * @param env the asset administration shell environment which contains the
     *            referenced elements
     * @throws ResourceNotFoundException if an element referenced by a key could
     *             not be found
     */
    public static void completeReferenceWithProperKeyElements(Reference reference, AssetAdministrationShellEnvironment env) throws ResourceNotFoundException {
        if (reference == null) {
            return;
        }
        List<Key> keys = reference.getKeys();
        if (keys.stream().allMatch(x -> x.getType() != null && x.getType() != KeyElements.SUBMODEL_ELEMENT)) {
            return;
        }
        final Referable[] parent = {
                null
        };
        for (Key k: keys) {
            if (env.getAssetAdministrationShells().stream().anyMatch(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(k.getValue())
                    || x.getIdShort().equalsIgnoreCase(k.getValue()))) {
                k.setType(KeyElements.ASSET_ADMINISTRATION_SHELL);
                continue;
            }

            env.getSubmodels().forEach(x -> {
                if (x.getIdentification().getIdentifier().equalsIgnoreCase(k.getValue())
                        || x.getIdShort().equalsIgnoreCase(k.getValue())) {
                    k.setType(KeyElements.SUBMODEL);
                    parent[0] = x;
                }
            });
            if (k.getType() != null && k.getType() != KeyElements.SUBMODEL_ELEMENT) {
                continue;
            }

            if (env.getConceptDescriptions().stream().anyMatch(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(k.getValue())
                    || x.getIdShort().equalsIgnoreCase(k.getValue()))) {
                k.setType(KeyElements.CONCEPT_DESCRIPTION);
                continue;
            }
            if (env.getAssets().stream().anyMatch(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(k.getValue())
                    || x.getIdShort().equalsIgnoreCase(k.getValue()))) {
                k.setType(KeyElements.ASSET);
                continue;
            }
            if (parent[0] != null && Submodel.class.isAssignableFrom(parent[0].getClass())) {
                Submodel submodel = (Submodel) parent[0];
                submodel.getSubmodelElements().forEach(y -> {
                    if (y.getIdShort().equalsIgnoreCase(k.getValue())) {
                        k.setType(AasUtils.referableToKeyType(y));
                        parent[0] = y;
                    }
                });
            }
            else if (SubmodelElementCollection.class.isAssignableFrom(parent[0].getClass())) {
                ((SubmodelElementCollection) parent[0]).getValues().forEach(x -> {
                    if (x.getIdShort().equalsIgnoreCase(k.getValue())) {
                        k.setType(AasUtils.referableToKeyType(x));
                        parent[0] = x;
                    }
                });
            }
            else if (Operation.class.isAssignableFrom(parent[0].getClass())) {
                Operation operation = (Operation) parent[0];
                Stream.concat(Stream.concat(operation.getInoutputVariables().stream(),
                        operation.getInputVariables().stream()),
                        operation.getOutputVariables().stream()).forEach(x -> {
                            if (x.getValue().getIdShort().equalsIgnoreCase(k.getValue())) {
                                k.setType(AasUtils.referableToKeyType(x.getValue()));
                                parent[0] = x.getValue();
                            }
                        });
            }

            if (k.getType() == null) {
                throw new ResourceNotFoundException("Resource with ID " + k.getValue() + " was not found!");
            }
        }
    }


    /**
     * Converts a submodel element reference to a list of keys. Each key in the
     * list have the general key element "SUBMODEL_ELEMENT"
     *
     * @param submodelElementRef reference of the submodel element
     * @return the list of keys
     */
    public static List<Key> toKeys(Reference submodelElementRef) {
        return submodelElementRef.getKeys().stream()
                .filter(x -> SubmodelElement.class.isAssignableFrom(AasUtils.keyTypeToClass(x.getType())))
                .map(x -> {
                    x.setType(KeyElements.SUBMODEL_ELEMENT);
                    return x;
                })
                .collect(Collectors.toList());
    }


    /**
     * Converst a list of key to a reference
     *
     * @param keys which are converted to a reference
     * @return the reference with the keys
     */
    public static Reference toReference(List<Key> keys) {
        return new DefaultReference.Builder()
                .keys(keys)
                .build();
    }


    /**
     * Combines a list of keys of a child element with a parent to a reference
     *
     * @param keys of the child
     * @param parentId of the parent
     * @param parentClass type of the parent
     * @return the full reference to the child element
     */
    public static Reference toReference(List<Key> keys, Identifier parentId, Class<?> parentClass) {
        Reference parentReference = toReference(parentId, parentClass);
        Reference childReference = new DefaultReference.Builder()
                .keys(keys)
                .build();
        return toReference(parentReference, childReference);
    }


    /**
     * Combine a parent reference and a child reference to one reference
     *
     * @param parentReference reference of the parent
     * @param childReference reference of the child
     * @return the combined reference
     */
    public static Reference toReference(Reference parentReference, Reference childReference) {
        List<Key> keys = new ArrayList<>(parentReference.getKeys());
        childReference.getKeys().forEach(x -> {
            if (!keys.contains(x)) {
                keys.add(x);
            }
        });
        return new DefaultReference.Builder()
                .keys(keys)
                .build();
    }


    /**
     * Create a reference for an {@link io.adminshell.aas.v3.model.Identifiable}
     * with KeyType IRI
     *
     * @param id of the identifiable
     * @param clazz of the identifiable
     * @return reference of the identifiable
     */
    public static Reference toReference(Identifier id, Class<?> clazz) {
        return new DefaultReference.Builder()
                .keys(List.of(new DefaultKey.Builder()
                        .value(id.getIdentifier())
                        .type(referableToKeyType(clazz))
                        .idType(KeyType.IRI)
                        .build()))
                .build();
    }


    /**
     * Get the corresponding {@link KeyElements} to the given class
     *
     * @param clazz to convert to a KeyElement
     * @return the corresponding KeyElement of the class
     */
    public static KeyElements referableToKeyType(Class<?> clazz) {
        Class<?> aasInterface = ReflectionHelper.getAasInterface(clazz);
        return aasInterface != null ? KeyElements.valueOf(AasUtils.deserializeEnumName(aasInterface.getSimpleName())) : null;
    }


    /**
     * Checks if a given reference is null or empty
     *
     * @param reference the reference to check
     * @return true if reference is null or empty, otherwise false
     */
    public static boolean isNullOrEmpty(Reference reference) {
        return reference == null || reference.getKeys() == null || reference.getKeys().isEmpty();
    }


    /**
     * Gets the reference to the parent element of the element addressed by
     * given reference.
     *
     * @param reference the reference to the element to find the parent for
     * @return The reference to the parent element of the element addressed by
     *         given reference. If no parent exists null is returned.
     * @throws IllegalArgumentException if reference is null
     */
    public static Reference getParent(Reference reference) {
        if (reference == null) {
            throw new IllegalArgumentException("reference must be non-null");
        }
        if (reference.getKeys() == null || reference.getKeys().size() < 2) {
            return null;
        }
        Reference result = AasUtils.clone(reference);
        result.getKeys().remove(result.getKeys().size() - 1);
        return result;
    }


    /**
     * Builds a reference identifying a submodel element
     *
     * @param aasIdentifier the AAS identifier
     * @param submodelIdentifier the submodel identifier
     * @param submodelElementIdshorts the submodel element idShort path
     * @return a reference to the submodel element
     */
    public static Reference build(String aasIdentifier, String submodelIdentifier, String... submodelElementIdshorts) {
        List<Key> keyList = new ArrayList<>();
        keyList.add(new DefaultKey.Builder()
                .idType(KeyType.IRI)
                .type(null)
                .value(aasIdentifier)
                .build());
        keyList.add(new DefaultKey.Builder()
                .idType(KeyType.IRI)
                .type(null)
                .value(submodelIdentifier)
                .build());
        Stream.of(submodelElementIdshorts).forEach(
                x -> keyList.add(new DefaultKey.Builder()
                        .idType(KeyType.ID_SHORT)
                        .type(null)
                        .value(x)
                        .build()));
        return new DefaultReference.Builder()
                .keys(keyList)
                .build();
    }


    /**
     * Builds a reference identifying a submodel
     *
     * @param aasIdentifier the AAS identifier
     * @param submodelIdentifier the submodel identifier
     * @return a reference to the submodel
     */
    public static Reference build(String aasIdentifier, String submodelIdentifier) {
        List<Key> keyList = new ArrayList<>();
        keyList.add(new DefaultKey.Builder()
                .idType(KeyType.IRI)
                .type(null)
                .value(aasIdentifier)
                .build());
        keyList.add(new DefaultKey.Builder()
                .idType(KeyType.IRI)
                .type(null)
                .value(submodelIdentifier)
                .build());
        return new DefaultReference.Builder()
                .keys(keyList)
                .build();
    }
}
