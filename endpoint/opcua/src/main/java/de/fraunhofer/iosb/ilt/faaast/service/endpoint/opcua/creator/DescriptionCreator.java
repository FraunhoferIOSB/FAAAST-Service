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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator;

import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.core.Argument;
import org.eclipse.digitaltwin.aas4j.v3.model.LangString;
import java.util.List;


/**
 * Helper class to create Descriptions and integrate them into the
 * OPC UA address space.
 */
public class DescriptionCreator {

    private DescriptionCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Adds the list of Descriptions to the given node.
     *
     * @param node The desired UA node in which the Descriptions should be created
     * @param descriptions The list of AAS descriptions
     */
    public static void addDescriptions(UaNode node, List<LangString> descriptions) {
        if ((node != null) && (descriptions != null) && (!descriptions.isEmpty())) {
            LangString desc = descriptions.get(0);
            node.setDescription(new LocalizedText(desc.getValue(), desc.getLanguage()));
        }
    }


    /**
     * Adds the descriptions to the given argument.
     *
     * @param arg The desired UA argument
     * @param descriptions The list of AAS descriptions
     */
    public static void addDescriptions(Argument arg, List<LangString> descriptions) {
        if ((arg != null) && (descriptions != null) && (!descriptions.isEmpty())) {
            LangString desc = descriptions.get(0);
            arg.setDescription(new LocalizedText(desc.getValue(), desc.getLanguage()));
        }
    }

}
