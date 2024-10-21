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
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.provider.config.CustomValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.util.AasHelper;
import de.fraunhofer.iosb.ilt.faaast.service.example.assetconnection.custom.util.RandomValueGenerator;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CustomValueProvider implements AssetValueProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomValueProvider.class);
    private final Reference reference;
    private final Datatype datatype;

    public CustomValueProvider(Reference reference, CustomValueProviderConfig config, ServiceContext serviceContext)
            throws ValueMappingException, ResourceNotFoundException, PersistenceException {
        Ensure.requireNonNull(reference, "reference must be non-null");
        Ensure.requireNonNull(config, "config must be non-null");
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        AasHelper.ensureType(reference, Property.class, serviceContext);
        this.reference = reference;
        this.datatype = AasHelper.getDatatype(reference, serviceContext);
        LOGGER.debug(String.format("custom property 'note' of 'CustomValueProvider': %s", config.getNote()));
    }


    @Override
    public DataElementValue getValue() throws AssetConnectionException {
        try {
            return PropertyValue.of(datatype, RandomValueGenerator.generateRandomValue(datatype).toString());
        }
        catch (ValueFormatException e) {
            throw new AssetConnectionException(String.format("error reading value from asset connection (reference: %s)", reference), e);
        }
    }


    @Override
    public void setValue(DataElementValue value) throws AssetConnectionException {
        throw new UnsupportedOperationException(String.format("%s does not support writing", getClass().getName()));
    }

}
