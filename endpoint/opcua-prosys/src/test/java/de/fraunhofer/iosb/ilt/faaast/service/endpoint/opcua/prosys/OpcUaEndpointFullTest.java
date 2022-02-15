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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys;

import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.StatusCode;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.ReferenceDescription;
import com.prosysopc.ua.stack.core.ServerState;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys.helper.TestDefines;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys.helper.TestService;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys.helper.TestUtils;
import io.adminshell.aas.v3.model.Qualifier;
import io.adminshell.aas.v3.model.impl.DefaultQualifier;
import java.util.ArrayList;
import java.util.List;
import opc.i4aas.AASIdentifierTypeDataType;
import opc.i4aas.AASModelingKindDataType;
import opc.i4aas.AASValueTypeDataType;
import opc.i4aas.VariableIds;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Test class for the general OPC UA Endpoint test with the full example
 *
 * @author Tino Bischoff
 */
public class OpcUaEndpointFullTest {

    private static final Logger logger = LoggerFactory.getLogger(OpcUaEndpointFullTest.class);

    private static final int OPC_TCP_PORT = 18123;

    private static final String ENDPOINT_URL = "opc.tcp://localhost:" + OPC_TCP_PORT;

    private static OpcUaEndpoint endpoint;
    private static TestService service;
    private static int aasns;

    @BeforeClass
    public static void startTest() throws Exception {

        CoreConfig coreConfig = new CoreConfig();

        OpcUaEndpointConfig config = new OpcUaEndpointConfig();
        config.setTcpPort(OPC_TCP_PORT);

        endpoint = new OpcUaEndpoint();
        endpoint.init(coreConfig, config, service);
        service = new TestService(endpoint, true);
        service.start();
    }


    @AfterClass
    public static void stopTest() {
        logger.trace("stopTest");
        if (endpoint != null) {
            endpoint.stop();
        }

        if (service != null) {
            service.stop();
        }
    }


    /**
     * Test method for testing the OPC UA Endpoint
     *
     * @throws InterruptedException If the operation fails
     * @throws Exception If the operation fails
     */
    @Test
    public void testOpcUaEndpointFull() throws InterruptedException, Exception {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        Assert.assertTrue("client not connected", client.isConnected());
        System.out.println("client connected");

        DataValue value = client.readValue(Identifiers.Server_ServerStatus_State);
        System.out.println(value);
        Assert.assertEquals(StatusCode.GOOD, value.getStatusCode());
        Assert.assertEquals(ServerState.Running.ordinal(), value.getValue().intValue());

        aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        // browse for AAS Environment
        List<ReferenceDescription> refs = client.getAddressSpace().browse(Identifiers.ObjectsFolder);
        Assert.assertNotNull("Browse ObjectsFolder Refs Null", refs);
        Assert.assertFalse("Browse ObjectsFolder Refs empty", refs.isEmpty());
        NodeId envNode = null;
        for (ReferenceDescription ref: refs) {
            if (ref.getBrowseName().getName().equals(TestDefines.AAS_ENVIRONMENT_NAME)) {
                envNode = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
                break;
            }
        }

        Assert.assertNotNull("AASEnvironment Null", envNode);

        // browse AAS Environment
        refs = client.getAddressSpace().browse(envNode);
        Assert.assertNotNull("Browse Environment Refs Null", refs);
        Assert.assertTrue("Browse Environment Refs empty", refs.size() > 0);

        NodeId submodel1Node = null;
        for (ReferenceDescription ref: refs) {
            NodeId rid = client.getAddressSpace().getNamespaceTable().toNodeId(ref.getNodeId());
            switch (ref.getBrowseName().getName()) {
                case TestDefines.FULL_SUBMODEL_1_NAME:
                    submodel1Node = rid;
                    break;
            }
        }

        Assert.assertNotNull("Submodel 1 Node not found", submodel1Node);

        testSubmodel1(client, submodel1Node);

        System.out.println("disconnect client");
        client.disconnect();
    }


    /**
     * Tests the submodel 1 (Identification).
     *
     * @param client The OPC UA Client.
     * @param submodelNode The desired Submodel
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    private void testSubmodel1(UaClient client, NodeId submodelNode) throws ServiceException, AddressSpaceException, ServiceResultException, StatusException {
        TestUtils.checkDisplayName(client, submodelNode, "Submodel:" + TestDefines.FULL_SUBMODEL_1_NAME);
        TestUtils.checkType(client, submodelNode, new NodeId(aasns, TestDefines.AAS_SUBMODEL_TYPE_ID));

        TestUtils.checkIdentificationNode(client, submodelNode, aasns, AASIdentifierTypeDataType.IRI, TestDefines.FULL_SUBMODEL_1_ID);
        TestUtils.checkAdministrationNode(client, submodelNode, aasns, "0.9", "0");
        TestUtils.checkModelingKindNode(client, submodelNode, aasns, AASModelingKindDataType.Instance);
        TestUtils.checkCategoryNode(client, submodelNode, aasns, "");
        TestUtils.checkDataSpecificationNode(client, submodelNode, aasns);

        TestUtils.checkQualifierNode(client, submodelNode, aasns, new ArrayList<>());

        ArrayList<Qualifier> list = new ArrayList<>();
        list.add(new DefaultQualifier.Builder()
                .value("100")
                .valueType("int")
                .type("http://acplt.org/Qualifier/ExampleQualifier")
                .build());
        list.add(new DefaultQualifier.Builder()
                .value("50")
                .valueType("int")
                .type("http://acplt.org/Qualifier/ExampleQualifier2")
                .build());
        TestUtils.checkAasPropertyString(client, submodelNode, aasns, "ManufacturerName", AASModelingKindDataType.Instance, "", AASValueTypeDataType.String,
                "http://acplt.org/ValueId/ACPLT", list);
    }
}
