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

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Asset;
import opc.i4aas.AASAssetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create Assets and integrate them into the OPC UA address space.
 */
public class AssetCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetCreator.class);

    private AssetCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Adds an Asset to the given Node.
     *
     * @param node The UA node in which the Asset should be created
     * @param asset The desired Asset
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addAsset(UaNode node, Asset asset, AasServiceNodeManager nodeManager) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(AasServiceNodeManager.NODE_NULL);
        }
        else if (asset == null) {
            throw new IllegalArgumentException("asset = null");
        }

        String name = asset.getIdShort();
        String displayName = "Asset:" + name;
        LOGGER.debug("addAsset {}; to Node: {}", name, node);
        QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASAssetType.getNamespaceUri(), name).toQualifiedName(nodeManager.getNamespaceTable());
        NodeId nid = nodeManager.createNodeId(node, browseName);
        AASAssetType assetNode = nodeManager.createInstance(AASAssetType.class, nid, browseName, LocalizedText.english(displayName));

        IdentifiableCreator.addIdentifiable(assetNode, asset.getIdentification(), asset.getAdministration(), asset.getCategory(), nodeManager);

        // DataSpecifications
        EmbeddedDataSpecificationCreator.addEmbeddedDataSpecifications(assetNode, asset.getEmbeddedDataSpecifications(), nodeManager);

        node.addComponent(assetNode);

        nodeManager.addReferable(AasUtils.toReference(asset), new ObjectData(asset, assetNode));
    }

}
