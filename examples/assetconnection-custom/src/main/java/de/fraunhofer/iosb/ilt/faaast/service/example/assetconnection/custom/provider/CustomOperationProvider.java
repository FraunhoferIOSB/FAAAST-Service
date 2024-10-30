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
package de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.provider;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProvider;
import de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.provider.config.CustomOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.util.RandomValueGenerator;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.util.ReflectionHelper;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;


public class CustomOperationProvider implements AssetOperationProvider<CustomOperationProviderConfig> {

    private static final String BASE_ERROR_MSG = "error creating custom operation provider";
    private final CustomOperationProviderConfig config;
    private final Reference reference;
    private final ServiceContext serviceContext;
    private final OperationVariable[] outputVariables;

    public CustomOperationProvider(Reference reference, CustomOperationProviderConfig config, ServiceContext serviceContext) throws ConfigurationInitializationException {
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(config, "config must be non-null");
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        this.config = config;
        this.reference = reference;
        this.serviceContext = serviceContext;
        try {
            outputVariables = serviceContext.getOperationOutputVariables(reference);
        }
        catch (ResourceNotFoundException | PersistenceException e) {
            throw new ConfigurationInitializationException(
                    String.format(
                            "Operation not found in AAS model (reference: %s)",
                            ReferenceHelper.toString(reference)),
                    e);
        }
        for (OperationVariable outputVariable: outputVariables) {
            if (outputVariable != null && outputVariable.getValue() != null && !Property.class.isAssignableFrom(outputVariable.getValue().getClass())) {
                throw new ConfigurationInitializationException(String.format("%s - only output variables of type property are supported (actual type: %s)",
                        BASE_ERROR_MSG,
                        ReflectionHelper.getAasInterface(outputVariable.getClass())));
            }
        }
    }


    @Override
    public CustomOperationProviderConfig getConfig() {
        return config;
    }


    @Override
    public OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
        OperationVariable[] result = new OperationVariable[outputVariables.length];
        for (int i = 0; i < outputVariables.length; i++) {
            if (outputVariables[i] != null && outputVariables[i].getValue() != null) {
                Property property = DeepCopyHelper.deepCopy(outputVariables[i].getValue(), Property.class);
                property.setValue(RandomValueGenerator.generateRandomValue(Datatype.fromAas4jDatatype(property.getValueType())).toString());
                result[i] = new DefaultOperationVariable.Builder()
                        .value(property)
                        .build();
            }
            else {
                result[i] = null;
            }
        }
        return result;
    }

}
