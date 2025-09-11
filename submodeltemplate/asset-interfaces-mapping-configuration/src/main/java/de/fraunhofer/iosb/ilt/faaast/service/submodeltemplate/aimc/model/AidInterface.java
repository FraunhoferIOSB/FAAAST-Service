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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model;

import de.fraunhofer.iosb.ilt.faaast.service.model.SemanticIdPath;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.util.Util;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;


public class AidInterface {
    private String title;
    private List<AidEndpointMetadata> endpointMetadata;

    public String getTitle() {
        return title;
    }


    public List<AidEndpointMetadata> getEndpointMetadata() {
        return endpointMetadata;
    }


    public static AidInterface parse(SubmodelElement element) {
        Ensure.requireNonNull(element, "element must be non-null");
        Ensure.requireNonNull(SubmodelElementCollection.class.isInstance(element), "element must be a SubmodelElementCollection");
        Ensure.require(
                Util.semanticIdEquals(element, Constants.AID_ENDPOINT_METADATA_SEMANTIC_ID),
                String.format("Failed to read Interface from AID - invalid semanticId (expected: %s, actual: %s)",
                        Constants.AID_INTERFACE_SEMANTIC_ID,
                        ReferenceHelper.asString(element.getSemanticId())));
        AidInterface instance = new AidInterface();
        instance.title = SemanticIdPath.builder()
                .globalReference(Constants.AID_INTERFACE_TITLE_SEMANTIC_ID)
                .build()
                .resolveUnique(element, Property.class)
                .getValue();
        instance.endpointMetadata = SemanticIdPath.builder()
                .globalReference(Constants.AID_ENDPOINT_METADATA_SEMANTIC_ID)
                .build()
                .resolve(element, SubmodelElementCollection.class).stream()
                .map(AidEndpointMetadata::parse)
                .collect(Collectors.toList());
        return instance;
    }

}
