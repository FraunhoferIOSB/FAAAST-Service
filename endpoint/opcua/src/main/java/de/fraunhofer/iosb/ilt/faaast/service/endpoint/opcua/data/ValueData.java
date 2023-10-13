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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data;

import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;


/**
 * Class with information for values
 */
public class ValueData {
    private final NodeId nodeId;
    private final QualifiedName browseName;
    private final LocalizedText displayName;
    private final NodeManagerUaNode nodeManager;

    /**
     * Creates a new instance of ValueHelper.
     * 
     * @param nodeId The desired NodeId.
     * @param browseName The desired Browse Name.
     * @param displayName The desired Display Name.
     * @param nodeManager The corresponding Node Manager
     */
    public ValueData(NodeId nodeId, QualifiedName browseName, LocalizedText displayName, NodeManagerUaNode nodeManager) {
        this.browseName = browseName;
        this.displayName = displayName;
        this.nodeId = nodeId;
        this.nodeManager = nodeManager;
    }


    /**
     * Gets the NodeId.
     * 
     * @return The NodeId.
     */
    public NodeId getNodeId() {
        return nodeId;
    }


    /**
     * Gets the Browse Name.
     * 
     * @return The Browse Name.
     */
    public QualifiedName getBrowseName() {
        return browseName;
    }


    /**
     * Gets the Display Name.
     * 
     * @return The Display Name.
     */
    public LocalizedText getDisplayName() {
        return displayName;
    }


    /**
     * Gets the Node Manager.
     * 
     * @return The Node Manager.
     */
    public NodeManagerUaNode getNodeManager() {
        return nodeManager;
    }
}
