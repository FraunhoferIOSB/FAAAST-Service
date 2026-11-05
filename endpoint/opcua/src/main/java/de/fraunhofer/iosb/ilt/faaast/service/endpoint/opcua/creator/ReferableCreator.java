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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator;

import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.util.List;
import opc.ua.aas.datatypes.AASExtension;
import opc.ua.aas.datatypes.AASHasExtensions;
import opc.ua.aas.datatypes.AASReferable;
import opc.ua.aas.datatypes.AASReference;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.HasExtensions;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class for AAS Referable data.
 */
public class ReferableCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReferableCreator.class);

    private ReferableCreator() {}


    /**
     * Gets the AASReferable from the given Referable.
     *
     * @param referable The desired Referable.
     * @return The corresponding AASReferable.
     */
    public static AASReferable getReferable(Referable referable) {
        if (referable == null) {
            return null;
        }
        AASReferable retval = new AASReferable();
        if (referable.getDisplayName() != null) {
            retval.setDisplayName(ValueConverter.convertLangStringSet(referable.getDisplayName()));
        }

        if (referable.getDescription() != null) {
            retval.setDescription(ValueConverter.convertLangStringSet(referable.getDescription()));
        }

        if (referable.getCategory() != null) {
            retval.setCategory(referable.getCategory());
        }

        if (referable.getExtensions() != null) {
            retval.setHasExtensions(getHasExtensions(referable));
        }
        return retval;
    }


    /**
     * Adds DisplayNames and Descriptions to the given node.
     *
     * @param node The desired node.
     * @param referable The referable.
     */
    public static void setReferableNodeData(UaNode node, Referable referable) {
        Ensure.requireNonNull(node);
        Ensure.requireNonNull(referable);
        LocalizedText[] textList = ValueConverter.convertLangStringSet(referable.getDisplayName());
        if ((textList != null) && (textList.length > 0)) {
            // Initially, we remove the default display name.
            node.setDisplayName(LocalizedText.EMPTY);
            var builder = LocalizedText.builder();
            for (LocalizedText lt: textList) {
                LOGGER.debug("setReferableNodeData: node: {}; DisplayName: {}", node, lt);
                builder.setTexts(lt);
            }
            node.setDisplayName(builder.build());
        }
        // workaround for "bug" in SDK
        textList = ValueConverter.convertLangStringSet(referable.getDescription());
        node.setDescription(LocalizedText.EMPTY);
        if ((textList != null) && (textList.length > 0)) {
            var builder = LocalizedText.builder();
            for (LocalizedText lt: textList) {
                LOGGER.debug("setReferableNodeData: node: {}; Description: {}", node, lt);
                builder.setTexts(lt);
            }
            node.setDescription(builder.build());
        }
    }


    private static AASHasExtensions getHasExtensions(HasExtensions hasExtensions) {
        if (hasExtensions == null) {
            return null;
        }
        return new AASHasExtensions(getExtensionData(hasExtensions.getExtensions()), null);
    }


    private static AASExtension getExtensionData(Extension extension) {
        if (extension == null) {
            return null;
        }

        AASExtension retval = new AASExtension();
        if (extension.getName() != null) {
            retval.setName(extension.getName());
        }
        if (extension.getValueType() != null) {
            retval.setValueType(ValueConverter.convertDataTypeDefToString(extension.getValueType()));
        }
        if (extension.getValue() != null) {
            retval.setValue(extension.getValue());
        }
        if (extension.getRefersTo() != null) {
            retval.setRefersTo(ReferenceCreator.getAasReferences(extension.getRefersTo()).toArray(AASReference[]::new));
        }
        retval.setHasSemantics(BaseDataCreator.getHasSemantics(extension));
        return retval;
    }


    private static AASExtension[] getExtensionData(List<Extension> extensions) {
        if (extensions == null) {
            return new AASExtension[0];
        }
        List<AASExtension> list = extensions.stream().map(ReferableCreator::getExtensionData).toList();
        return list.toArray(AASExtension[]::new);
    }
}
