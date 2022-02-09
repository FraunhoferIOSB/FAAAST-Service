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

import com.prosysopc.ua.SecureIdentityException;
import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.client.UaClient;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.core.BrowsePathResult;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.stack.core.RelativePath;
import com.prosysopc.ua.stack.core.RelativePathElement;
import com.prosysopc.ua.stack.transport.security.SecurityMode;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys.helper.AASSimple;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys.helper.TestDefines;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys.helper.TestService;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys.helper.TestUtils;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import io.adminshell.aas.v3.model.KeyElements;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.impl.DefaultKey;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import opc.i4aas.VariableIds;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tino Bischoff
 */
public class OpcUaEndpoint2Test {
    private static final Logger logger = LoggerFactory.getLogger(OpcUaEndpoint2Test.class);

    private static final int OPC_TCP_PORT = 18123;
    private static final long DEFAULT_TIMEOUT = 1000;

    private static final String ENDPOINT_URL = "opc.tcp://localhost:" + OPC_TCP_PORT;

    private static OpcUaEndpoint endpoint;
    private static TestService service;

    /**
     * Initialize and start the test.
     * 
     * @throws ConfigurationException If the operation fails
     * @throws Exception If the operation fails
     */
    @BeforeClass
    public static void startTest() throws ConfigurationException, Exception {
        logger.trace("startTest");

        CoreConfig coreConfig = new CoreConfig();

        OpcUaEndpointConfig config = new OpcUaEndpointConfig();
        config.setTcpPort(OPC_TCP_PORT);
        config.setSecondsTillShutdown(0);

        endpoint = new OpcUaEndpoint();
        endpoint.init(coreConfig, config, service);
        service = new TestService(endpoint, null, false);
        endpoint.setService(service);
        service.start();
    }


    /**
     * Stop the test.
     */
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
     * Test method for deleting a complete submodel.
     * 
     * @throws SecureIdentityException If the operation fails
     * @throws IOException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws Exception If the operation fails
     */
    @Test
    public void testDeleteSubmodel() throws SecureIdentityException, IOException, ServiceException, Exception {
        UaClient client = new UaClient(ENDPOINT_URL);
        client.setSecurityMode(SecurityMode.NONE);
        TestUtils.initialize(client);
        client.connect();
        System.out.println("testDeleteSubmodel: client connected");

        int aasns = client.getAddressSpace().getNamespaceTable().getIndex(VariableIds.AASAssetAdministrationShellType_AssetInformation_AssetKind.getNamespaceUri());

        // make sure the element exists
        List<RelativePath> relPath = new ArrayList<>();
        List<RelativePathElement> browsePath = new ArrayList<>();
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.AAS_ENVIRONMENT_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.SUBMODEL_TECH_DATA_NODE_NAME)));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        // add more elements to the browse path
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, TestDefines.IDENTIFICATION_NAME)));
        browsePath.add(new RelativePathElement(Identifiers.HierarchicalReferences, false, true, new QualifiedName(aasns, "Id")));
        relPath.add(new RelativePath(browsePath.toArray(RelativePathElement[]::new)));

        BrowsePathResult[] bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testDeleteSubmodel Browse Result Null", bpres);
        Assert.assertTrue("testDeleteSubmodel Browse Result: size doesn't match", bpres.length == 2);
        Assert.assertTrue("testDeleteSubmodel Browse Result 1 Good", bpres[0].getStatusCode().isGood());
        Assert.assertTrue("testDeleteSubmodel Browse Result 2 Good", bpres[1].getStatusCode().isGood());

        // Send event to MessageBus
        ElementDeleteEventMessage msg = new ElementDeleteEventMessage();
        msg.setElement(new DefaultReference.Builder()
                .key(new DefaultKey.Builder().idType(KeyType.IRI).type(KeyElements.SUBMODEL).value(AASSimple.SUBMODEL_TECHNICAL_DATA_ID).build())
                .build());
        service.getMessageBus().publish(msg);

        Thread.sleep(DEFAULT_TIMEOUT);

        // check that the element is not there anymore
        bpres = client.getAddressSpace().translateBrowsePathsToNodeIds(Identifiers.ObjectsFolder, relPath.toArray(RelativePath[]::new));
        Assert.assertNotNull("testDeleteSubmodel Browse Result Null", bpres);
        Assert.assertTrue("testDeleteSubmodel Browse Result: size doesn't match", bpres.length == 2);
        Assert.assertTrue("testDeleteSubmodel Browse Result 1 Bad", bpres[0].getStatusCode().isBad());
        Assert.assertTrue("testDeleteSubmodel Browse Result 2 Bad", bpres[1].getStatusCode().isBad());
    }
}
