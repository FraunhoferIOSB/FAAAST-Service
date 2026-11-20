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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.ValueRanks;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.types.opcua.BaseDataVariableType;
import com.prosysopc.ua.types.opcua.server.FileTypeNode;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.nodemanager.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import opc.ua.aas.Ids;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class with general OPC UA helper methods.
 */
public class UaHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(UaHelper.class);

    // Regex für Standard-IRDI: <ICD>-<OID>#<OPID>#<OPID-OID>-<PI>
    private static final Pattern IRDI_REGEX = Pattern.compile("^\\d{4}-\\d+#[0-2]-\\w+#[0-9A-Za-z_-]+$");

    /**
     * Sonar wants a private constructor.
     */
    private UaHelper() {}


    /**
     * Adds an OPC UA String variable to the given node.
     * 
     * @param parentNode The node where the property should be added.
     * @param nodeManager The corresponding NodeManager.
     * @param name The name of the desired property.
     * @param value The value of the desired property.
     * @param namespaceUri The URI of the desired Namespace.
     * @param referenceType The desired reference type.
     * @throws StatusException If an error occurs
     */
    public static void addStringUaVariable(UaNode parentNode, NodeManagerUaNode nodeManager, String name, String value, String namespaceUri, NodeId referenceType)
            throws StatusException {
        NodeId nodeId = new NodeId(nodeManager.getNamespaceIndex(), parentNode.getNodeId().getValue().toString() + "." + name);
        QualifiedName browseName = UaQualifiedName.from(namespaceUri, name).toQualifiedName(nodeManager.getNamespaceTable());
        LocalizedText displayName = new LocalizedText(name);
        BaseDataVariableType variable = nodeManager.createInstance(BaseDataVariableType.class, nodeId, browseName, displayName);
        variable.setDataTypeId(Identifiers.String);
        variable.setValueRank(ValueRanks.Scalar);
        variable.setValue(value);
        parentNode.addReference(variable, referenceType);
    }


    /**
     * Extracts the id from the given reference.
     *
     * @param reference The desired reference.
     * @return The extracted id.
     */
    public static String extractId(Reference reference) {
        Ensure.requireNonNull(reference);
        if ((reference.getKeys() != null) && (reference.getKeys().size() == 1)) {
            return reference.getKeys().get(0).getValue();
        }
        return null;
    }


    public static FileTypeNode createFile(String filePath, UaNode parentNode, String name, AasServiceNodeManager nodeManager) {
        FileTypeNode retval = null;
        Path path = null;
        try {
            URI uri = URI.create(filePath);
            path = Path.of(uri);
        }
        catch (Exception ex) {
            LOGGER.info("createFile: error parsing URI", ex);
        }
        try {
            if (path == null) {
                path = Path.of(filePath);
            }
            if (Files.exists(path)) {
                QualifiedName browseName = UaQualifiedName.from(Ids.AASFileType.getNamespaceUri(), name)
                        .toQualifiedName(nodeManager.getNamespaceTable());
                NodeId nid = nodeManager.createNodeId(parentNode, browseName);
                FileTypeNode file = nodeManager.createInstance(FileTypeNode.class, nid, browseName, new LocalizedText(name));
                file.setFile(path.toFile());
                retval = file;
            }
        }
        catch (Exception ex) {
            LOGGER.info("createFile: error creating file", ex);
        }
        return retval;
    }


    /**
     * Checks if the given string is an IRDI.
     *
     * @param input The corresponding input string.
     * @return True if it's an IRDI, false if not.
     */
    public static boolean isIrdi(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return IRDI_REGEX.matcher(input).matches();
    }


    public static boolean isUri(String input) {
        Ensure.requireNonNull(input);
        boolean retval;
        try {
            new URI(input);
            retval = true;
        }
        catch (URISyntaxException ex) {
            retval = false;
        }
        return retval;
    }
}
