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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider;

import com.digitalpetri.modbus.client.ModbusTcpClient;
import com.digitalpetri.modbus.server.ModbusTcpServer;
import com.digitalpetri.modbus.server.ProcessImage;
import com.digitalpetri.modbus.server.ReadWriteModbusServices;
import com.digitalpetri.modbus.tcp.client.NettyClientTransportConfig;
import com.digitalpetri.modbus.tcp.client.NettyTcpClientTransport;
import com.digitalpetri.modbus.tcp.server.NettyServerTransportConfig;
import com.digitalpetri.modbus.tcp.server.NettyTcpServerTransport;
import de.fraunhofer.iosb.ilt.faaast.service.util.PortHelper;
import java.net.InetAddress;
import java.util.Optional;


public class AbstractModbusProviderTest {

    protected ModbusTcpServer server;
    private final int serverPort = PortHelper.findFreePort();

    protected AbstractModbusProviderTest() {
        server = getServer();
    }


    protected ModbusTcpClient getClient() {
        return ModbusTcpClient.create(new NettyTcpClientTransport(new NettyClientTransportConfig.Builder()
                .setHostname(InetAddress.getLoopbackAddress().getHostName())
                .setPort(serverPort)
                .build()));

    }


    private ModbusTcpServer getServer() {
        return ModbusTcpServer.create(
                new NettyTcpServerTransport(new NettyServerTransportConfig.Builder()
                        .setBindAddress("0.0.0.0")
                        .setPort(serverPort)
                        .build()),
                new ReadWriteModbusServices() {
                    @Override
                    protected Optional<ProcessImage> getProcessImage(int unitId) {
                        return Optional.empty();
                    }
                });
    }
}
