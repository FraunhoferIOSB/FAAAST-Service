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
package de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.impl.DefaultCloudEventMapper;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.impl.ElementDeletedCloudEventMapper;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.impl.OperationCloudEventMapper;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.cloudevents.mapper.impl.ValueChangedCloudEventMapper;
import java.util.function.Function;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Provides a default CloudEvent mapper registry, filled with necessary mappers.
 */
public final class CloudEventMapperRegistryProvider {

    private CloudEventMapperRegistryProvider() {}


    /**
     * Return the default registry with CloudEvent mappers to fulfill async-aas spec.
     *
     * @param config Config for all mappers
     * @param referableSupplier FA³ST persistence connection to resolve referables from references
     * @return The default CloudEventMapperRegistry
     */
    public static CloudEventMapperRegistry defaultRegistry(
                                                           CloudEventMapperConfig config,
                                                           Function<Reference, Referable> referableSupplier) {

        CloudEventMapperRegistry registry = new CloudEventMapperRegistry();
        registry.register(new DefaultCloudEventMapper(config));
        registry.register(new ElementDeletedCloudEventMapper(config));
        registry.register(new ValueChangedCloudEventMapper(config, referableSupplier));
        registry.register(new OperationCloudEventMapper(config, referableSupplier));
        return registry;
    }
}
