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
package de.fraunhofer.iosb.ilt.faaast.service;

import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Request;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.typing.TypeInfo;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Reference;


/**
 * Abstraction of Service to be used in other components to limit their access
 * to the service.
 */
public interface ServiceContext {

    /**
     * Provides type information about an element identified by reference.
     *
     * @param reference reference identifying the element
     * @return type information of the referenced element, empty
     *         {@link de.fraunhofer.iosb.ilt.faaast.service.typing.ContainerTypeInfo} if
     *         no matching type is found, null if reference is null
     * @throws IllegalArgumentException if reference can not be resolved on AAS
     *             environment of the service
     */
    public TypeInfo getTypeInfo(Reference reference);


    /**
     * Executes a request
     *
     * @param request request to execute
     * @return result of executing the request
     */
    public Response execute(Request request);


    /**
     * Get a copied version of the AssetAdministrationShellEnvironment instance
     * of the service
     *
     * @return a deep copied AssetAdministrationShellEnvironment instance of the
     *         service
     */
    public AssetAdministrationShellEnvironment getAASEnvironment();


    /**
     * Returns the message bus of the service
     *
     * @return the message bus of the service
     */
    public MessageBus getMessageBus();


    /**
     * Returns the output variables of an operation identified by a reference
     *
     * @param reference the reference identifying the operation
     * @return output variables of the operation identified by the reference
     * @throws IllegalArgumentException if reference is null
     * @throws IllegalArgumentException if reference cannot be resolved
     * @throws IllegalArgumentException if reference does not point to an
     *             operation
     */
    public OperationVariable[] getOperationOutputVariables(Reference reference);
}
