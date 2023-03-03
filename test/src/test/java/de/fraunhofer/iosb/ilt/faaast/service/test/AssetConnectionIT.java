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
package de.fraunhofer.iosb.ilt.faaast.service.test;

import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.OpcUaAssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.provider.config.OpcUaValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.ServiceConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.HttpEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.OpcUaEndpointConfig;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.internal.MessageBusInternalConfig;
import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import de.fraunhofer.iosb.ilt.faaast.service.persistence.memory.PersistenceInMemoryConfig;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.ApiPaths;
import de.fraunhofer.iosb.ilt.faaast.service.test.util.SocketHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;


public class AssetConnectionIT {

    public static MessageBus messageBus;

    private static final String HOST = "http://localhost";
    private static int PORT;
    private static ApiPaths API_PATHS;
    private static AssetAdministrationShellEnvironment environment;
    private static Service service;

    @BeforeClass
    public static void initClass() throws IOException {
        PORT = SocketHelper.findFreePort();
        API_PATHS = new ApiPaths(HOST, PORT);
    }


    private static ServiceConfig serviceConfigWithInValidAssetConnection() throws MalformedURLException {
        Submodel submodel = environment.getSubmodels().get(0);
        SubmodelElement source = submodel.getSubmodelElements().get(1);
        SubmodelElement target = submodel.getSubmodelElements().get(0);
        return ServiceConfig.builder()
                .core(CoreConfig.builder()
                        .requestHandlerThreadPoolSize(2)
                        .build())
                .persistence(PersistenceInMemoryConfig.builder()
                        .initialModel(DeepCopyHelper.deepCopy(environment))
                        .build())
                .endpoints(List.of(HttpEndpointConfig.builder()
                        .port(PORT)
                        .build()))
                .messageBus(MessageBusInternalConfig.builder()
                        .build())
                .assetConnection(OpcUaAssetConnectionConfig.builder()
                        .host("opc.tcp://" + "localhost" + PORT)
                        .valueProvider(AasUtils.toReference(AasUtils.toReference(submodel), target),
                                OpcUaValueProviderConfig.builder()
                                        .nodeId("invalid")
                                        .build())
                        .build())
                .build();
    }


    private static ServiceConfig serviceConfigWithValidAssetConnection() throws MalformedURLException {
        Submodel submodel = environment.getSubmodels().get(0);
        SubmodelElement source = submodel.getSubmodelElements().get(1);
        SubmodelElement target = submodel.getSubmodelElements().get(0);
        return ServiceConfig.builder()
                .core(CoreConfig.builder()
                        .requestHandlerThreadPoolSize(2)
                        .build())
                .persistence(PersistenceInMemoryConfig.builder()
                        .initialModel(DeepCopyHelper.deepCopy(environment))
                        .build())
                .endpoints(List.of(OpcUaEndpointConfig.builder()
                        .tcpPort(PORT)
                        .build()))
                .messageBus(MessageBusInternalConfig.builder()
                        .build())
                .assetConnection(OpcUaAssetConnectionConfig.builder()
                        .host("opc.tcp://" + "localhost" + PORT)
                        .valueProvider(AasUtils.toReference(AasUtils.toReference(submodel), target),
                                OpcUaValueProviderConfig.builder()
                                        .nodeId("node.id")
                                        .build())
                        .build())
                .build();
    }


    @Test
    public void startServiceWithInvalidAssetConnection() throws Exception {
        environment = AASFull.createEnvironment();
        service = new Service(serviceConfigWithInValidAssetConnection());
        messageBus = service.getMessageBus();
        service.start();
        Thread.sleep(5000);
    }


    @Test
    public void startServiceWithValidAssetConnection() throws Exception {
        environment = AASFull.createEnvironment();
        service = new Service(serviceConfigWithValidAssetConnection());
        messageBus = service.getMessageBus();
        service.start();
    }


    @After
    public void shutdown() {
        service.stop();
    }

}
