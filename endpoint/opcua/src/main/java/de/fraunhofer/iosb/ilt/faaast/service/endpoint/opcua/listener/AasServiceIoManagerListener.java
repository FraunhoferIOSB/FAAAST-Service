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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.listener;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.nodes.UaMethod;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaValueNode;
import com.prosysopc.ua.nodes.UaVariable;
import com.prosysopc.ua.server.ServiceContext;
import com.prosysopc.ua.server.io.IoManagerListener;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.builtintypes.DiagnosticInfo;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.AttributeWriteMask;
import com.prosysopc.ua.stack.core.StatusCodes;
import com.prosysopc.ua.stack.core.TimestampsToReturn;
import com.prosysopc.ua.stack.utils.NumericRange;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.OpcUaEndpoint;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Class that listens to I/O-Events
 */
public class AasServiceIoManagerListener implements IoManagerListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AasServiceIoManagerListener.class);

    private final OpcUaEndpoint endpoint;
    private final AasServiceNodeManager nodeManager;

    /**
     * Creates a new instance of AasServiceIoManagerListener
     *
     * @param ep the associated endpoint
     * @param nodeMan the associated NodeManager
     */
    public AasServiceIoManagerListener(OpcUaEndpoint ep, AasServiceNodeManager nodeMan) {
        endpoint = ep;
        nodeManager = nodeMan;
    }


    @Override
    public AccessLevelType onGetUserAccessLevel(ServiceContext sc, NodeId nodeid, UaVariable uv) {
        if (uv == null) {
            throw new IllegalArgumentException("UaVariable is null!");
        }

        return uv.getAccessLevel();
    }


    @Override
    public Boolean onGetUserExecutable(ServiceContext sc, NodeId nodeid, UaMethod um) {
        // Enable execution of all methods that are allowed by default
        return true;
    }


    @Override
    public AttributeWriteMask onGetUserWriteMask(ServiceContext sc, NodeId nodeid, UaNode uanode) {
        // Enable writing to everything that is allowed by default
        // The WriteMask defines the writable attributes, except for Value,
        // which is controlled by UserAccessLevel (above)

        return AttributeWriteMask.of(AttributeWriteMask.Fields.values());
    }


    @Override
    public boolean onReadNonValue(ServiceContext sc, NodeId nodeid, UaNode uanode, UnsignedInteger ui, DataValue dv) throws StatusException {
        return false;
    }


    @Override
    public boolean onReadValue(ServiceContext sc, NodeId nodeId, UaValueNode uvn, NumericRange nr, TimestampsToReturn ttr, DateTime dt, DataValue dv) throws StatusException {
        return false;
    }


    @Override
    public boolean onWriteNonValue(ServiceContext sc, NodeId nodeid, UaNode uanode, UnsignedInteger ui, DataValue dv) throws StatusException {
        if (uanode != null) {
            LOGGER.trace("onWriteNonValue: Node BrowseName {}", uanode.getBrowseName());
        }
        return false;
    }


    @Override
    public boolean onWriteValue(ServiceContext sc, NodeId nodeId, UaValueNode uvn, NumericRange indexRange, DataValue dv) throws StatusException {
        LOGGER.trace(
                "onWriteValue: nodeId={}{}{} value={}", nodeId, uvn != null ? " node=" + uvn.getBrowseName() : "", indexRange != null ? " indexRange=" + indexRange : "", dv);

        try {
            if (endpoint == null) {
                LOGGER.warn("onWriteValue: no Endpoint available");
            }
            else if (dv.getStatusCode().isNotGood()) {
                LOGGER.warn("onWriteValue: StatusCode not good");
            }
            else {
                boolean rv;
                SubmodelElementData data = nodeManager.getAasData(nodeId);
                if (data != null) {
                    if (data.getType() == null) {
                        LOGGER.warn("onWriteValue: Node {}: unkown type", nodeId);
                        rv = false;
                    }
                    else {
                        ValueConverter.setSubmodelElementValue(data, dv);

                        rv = endpoint.writeValue(data.getSubmodelElement(), data.getSubmodel(), data.getReference());
                    }
                }
                else {
                    LOGGER.warn("onWriteValue: Node {}: SubmodelElementData not found", nodeId);
                    rv = false;
                }

                if (rv) {
                    LOGGER.debug("onWriteValue: NodeId {} written successfully", nodeId);
                }
                else {
                    LOGGER.warn("onWriteValue: NodeId {} write failed", nodeId);
                    throw new StatusException(StatusCodes.Bad_InternalError);
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error("onWriteValue Exception", ex);
            DiagnosticInfo di = new DiagnosticInfo();
            di.setAdditionalInfo("error in onWriteValue");
            throw new StatusException(ex.getMessage(), StatusCodes.Bad_UnexpectedError, di, ex);
        }

        // We return true here. So, the value is not written to the node here. 
        // The node is written in the callback from the MessageBus.
        return true;
    }

}
