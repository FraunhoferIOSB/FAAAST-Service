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
package de.fraunhofer.iosb.ilt.faaast.service.model.descriptor;

import java.io.Serializable;


/**
 * Registry Descriptor for ProtocolInformation.
 */
public interface ProtocolInformation extends Serializable {

    public String getEndpointAddress();


    public void setEndpointAddress(String endpointAddress);


    public String getEndpointProtocol();


    public void setEndpointProtocol(String endpointProtocol);


    public String getEndpointProtocolVersion();


    public void setEndpointProtocolVersion(String endpointProtocolVersion);


    public String getSubprotocol();


    public void setSubprotocol(String subprotocol);


    public String getSubprotocolBody();


    public void setSubprotocolBody(String subprotocolBody);


    public String getSubprotocolBodyEncoding();


    public void setSubprotocolBodyEncoding(String subprotocolBodyEncoding);

}
