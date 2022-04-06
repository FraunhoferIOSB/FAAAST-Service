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
package de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.util;

import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extend;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import io.adminshell.aas.v3.model.Blob;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.util.List;
import java.util.function.Predicate;


/**
 * Helper class to apply query modifier
 */
public class QueryModifierHelper {

    private QueryModifierHelper() {}


    /**
     * Apply the {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier} to a list of referables
     * Consider the {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extend} and
     * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level} of a query modifier
     * If the extend of the query modifier is "WithoutBlobValue" all submodel elements of type
     * {@link io.adminshell.aas.v3.model.Blob} are removed.
     * If the level of the query modifier is "Core" all underlying submodel element collection values are removed.
     *
     * @param referableList which should be adapted by the query modifier
     * @param modifier which should be applied
     * @param <T> type of referable
     */
    public static <T extends Referable> void applyQueryModifier(List<T> referableList, QueryModifier modifier) {
        if (referableList == null) {
            return;
        }
        for (Referable referable: referableList) {
            applyQueryModifier(referable, modifier);
        }
    }


    /**
     * Apply the {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier} to a referable
     * Consider the {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extend} and
     * {@link de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level} of a query modifier
     * If the extend of the query modifier is "WithoutBlobValue" all submodel elements of type
     * {@link io.adminshell.aas.v3.model.Blob} are removed.
     * If the level of the query modifier is "Core" all underlying submodel element collection values are removed.
     *
     * @param referable which should be adapted by the query modifier
     * @param modifier which should be applied
     */
    public static void applyQueryModifier(Referable referable, QueryModifier modifier) {
        if (referable == null || modifier == null) {
            return;
        }
        applyQueryModifierExtend(referable, modifier);
        applyQueryModifierLevel(referable, modifier);
    }


    private static void applyQueryModifierExtend(Referable referable, QueryModifier modifier) {
        Predicate<SubmodelElement> removeFilter = x -> Blob.class.isAssignableFrom(x.getClass());
        if (modifier.getExtend() == Extend.WITHOUT_BLOB_VALUE) {
            if (Submodel.class.isAssignableFrom(referable.getClass())) {
                ((Submodel) referable).getSubmodelElements().removeIf(removeFilter);
            }
            else if (SubmodelElementCollection.class.isAssignableFrom(referable.getClass())) {
                ((SubmodelElementCollection) referable).getValues().removeIf(removeFilter);
            }
        }
    }


    private static void applyQueryModifierLevel(Referable referable, QueryModifier modifier) {
        if (modifier.getLevel() == Level.DEEP) {
            //nothing to do here
        }
        else if (modifier.getLevel() == Level.CORE) {
            if (Submodel.class.isAssignableFrom(referable.getClass())) {
                ((Submodel) referable).getSubmodelElements().forEach(x -> {
                    if (SubmodelElementCollection.class.isAssignableFrom(x.getClass())) {
                        ((SubmodelElementCollection) x).getValues().clear();
                    }
                });
            }
            else if (SubmodelElementCollection.class.isAssignableFrom(referable.getClass())) {
                ((SubmodelElementCollection) referable).getValues().forEach(x -> {
                    if (SubmodelElementCollection.class.isAssignableFrom(x.getClass())) {
                        ((SubmodelElementCollection) x).getValues().clear();
                    }
                });
            }
        }
    }
}
