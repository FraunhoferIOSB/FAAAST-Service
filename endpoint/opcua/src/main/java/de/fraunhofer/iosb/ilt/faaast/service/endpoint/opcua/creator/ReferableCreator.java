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
import java.util.ArrayList;
import java.util.List;
import opc.ua.aas.datatypes.AASExtension;
import opc.ua.aas.datatypes.AASHasExtensions;
import opc.ua.aas.datatypes.AASReferable;
import opc.ua.aas.datatypes.AASReference;
import org.eclipse.digitaltwin.aas4j.v3.model.Extension;
import org.eclipse.digitaltwin.aas4j.v3.model.HasExtensions;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;


/**
 * Helper class for AAS Referable data.
 */
public class ReferableCreator {

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
        if (referable.getCategory() != null) {
            retval.setCategory(referable.getCategory());
        }

        if (referable.getExtensions() != null) {
            retval.setHasExtensions(getHasExtensions(referable));
        }
        return retval;
    }


    /**
     * Adds DisplayNamer and Descriptions to the given node.
     *
     * @param node The desired node.
     * @param referable The referable.
     */
    public static void setReferebleNodeData(UaNode node, Referable referable) {
        Ensure.requireNonNull(node);
        Ensure.requireNonNull(referable);
        LocalizedText[] textList = ValueConverter.convertLangStringSet(referable.getDisplayName());
        if ((textList != null) && (textList.length > 0)) {
            node.setDisplayName(textList[0]);
        }
        textList = ValueConverter.convertLangStringSet(referable.getDescription());
        if ((textList != null) && (textList.length > 0)) {
            node.setDescription(textList[0]);
        }
    }


    private static AASHasExtensions getHasExtensions(HasExtensions hasExtensions) {
        if (hasExtensions == null) {
            return null;
        }
        AASHasExtensions retval = new AASHasExtensions(getExtensionData(hasExtensions.getExtensions()));
        return retval;
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
            return null;
        }
        List<AASExtension> list = new ArrayList<>();
        for (var ext: extensions) {
            list.add(getExtensionData(ext));
        }
        return list.toArray(AASExtension[]::new);
    }
}
