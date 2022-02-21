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
package de.fraunhofer.iosb.ilt.faaast.service.requesthandlers;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.ElementValueMapper;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.util.List;
import java.util.stream.Collectors;


public class Util {

    private Util() {}


    public static List<ElementValue> toValues(List<OperationVariable> variables) {
        return variables.stream()
                .map(x -> ElementValueMapper.<SubmodelElement, ElementValue> toValue(x.getValue()))
                .collect(Collectors.toList());
    }

}
