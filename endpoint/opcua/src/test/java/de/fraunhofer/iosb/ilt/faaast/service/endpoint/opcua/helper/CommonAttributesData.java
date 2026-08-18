/*
 * Copyright 2026 Fraunhofer IOSB.
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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper;

import java.util.List;
import opc.ua.aas.datatypes.AASModellingKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;


public record CommonAttributesData(String version, String revision, String category, String id, AASModellingKind modelingKind, List<Qualifier> qualifier) {

    public CommonAttributesData(String version, String revision, String category, String id) {
        this(version, revision, category, id, null, null);
    }
}
