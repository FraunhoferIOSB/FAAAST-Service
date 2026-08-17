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
package de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.referable;

import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;

import java.util.Objects;


/**
 * An access object referencing a submodel element.
 */
public class SubmodelElementObject extends ReferableObject {
    public static final String NOTATION = "$sme";

    private final IdShortPath idShortPath;

    protected SubmodelElementObject(String input) {
        super(input.substring(0, input.lastIndexOf(")")));

        // $sme(SomeIdentifier).<IdShortPath>
        String idShortPathString = input.substring(input.lastIndexOf(")") + 2);

        this.idShortPath = IdShortPath.parse(idShortPathString);
    }


    @Override
    public String getNotation() {
        return NOTATION;
    }


    public IdShortPath getIdShortPath() {
        return idShortPath;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        SubmodelElementObject that = (SubmodelElementObject) o;
        return Objects.equals(idShortPath, that.idShortPath);
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), idShortPath);
    }
}
