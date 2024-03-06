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

import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.AssetAdministrationShellElementWalker;
import de.fraunhofer.iosb.ilt.faaast.service.model.visitor.DefaultAssetAdministrationShellElementVisitor;
import org.eclipse.digitaltwin.aas4j.v3.model.Blob;


/**
 * Helper class to apply {@link Extent} parameter to given AAS structures.
 */
public class ExtendHelper {

    private ExtendHelper() {}


    /**
     * Applies given extend to object structure. This directly modifies the {@code obj}.
     *
     * @param obj the object to apply given extend
     * @param extend the extend to apply
     */
    public static void applyExtend(Object obj, Extent extend) {
        if (extend == Extent.WITHOUT_BLOB_VALUE) {
            withoutBlobValue(obj);
        }
    }


    /**
     * Applies {@link Extent#WITHOUT_BLOB_VALUE} to object structure. This directly modifies the {@code obj}.
     *
     * @param obj the object to apply given extend
     */
    public static void withoutBlobValue(Object obj) {
        ObjectHelper.forEach(obj, x -> AssetAdministrationShellElementWalker.builder()
                .visitor(new DefaultAssetAdministrationShellElementVisitor() {
                    @Override
                    public void visit(Blob blob) {
                        blob.setValue(null);
                    }
                })
                .build()
                .walk(x));
    }

}
