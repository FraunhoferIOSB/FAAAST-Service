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
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;


public class ReferenceHelper {

    private ReferenceHelper() {}


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


    public static boolean isEqualsIgnoringKeyType(Reference reference, Reference reference1) {
        return isEqualsIgnoringKeyType(reference, reference1.getKeys());
    }


    private static Key deepCopyKeyWithoutKeyElement(Key k) {
        return new DefaultKey.Builder()
                .value(k.getValue())
                .idType(k.getIdType())
                .build();
    }


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
            if (env.getAssetAdministrationShells().stream().anyMatch(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(k.getValue()) ||
                    x.getIdShort().equalsIgnoreCase(k.getValue()))) {
                k.setType(KeyElements.ASSET_ADMINISTRATION_SHELL);
                continue;
            }

            env.getSubmodels().forEach(x -> {
                if (x.getIdentification().getIdentifier().equalsIgnoreCase(k.getValue()) ||
                        x.getIdShort().equalsIgnoreCase(k.getValue())) {
                    k.setType(KeyElements.SUBMODEL);
                    parent[0] = x;
                }
            });
            if (k.getType() != null && k.getType() != KeyElements.SUBMODEL_ELEMENT) {
                continue;
            }

            if (env.getConceptDescriptions().stream().anyMatch(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(k.getValue()) ||
                    x.getIdShort().equalsIgnoreCase(k.getValue()))) {
                k.setType(KeyElements.CONCEPT_DESCRIPTION);
                continue;
            }
            if (env.getAssets().stream().anyMatch(x -> x.getIdentification().getIdentifier().equalsIgnoreCase(k.getValue()) ||
                    x.getIdShort().equalsIgnoreCase(k.getValue()))) {
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
}
