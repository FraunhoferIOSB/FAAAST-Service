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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.fixtures.bar;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BarValueProvider implements AssetValueProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(BarValueProvider.class);
    private final Reference reference;
    private final BarValueProviderConfig providerConfig;

    public BarValueProvider(Reference reference, BarValueProviderConfig providerConfig) {
        this.reference = reference;
        this.providerConfig = providerConfig;
    }


    @Override
    public DataElementValue getValue() throws AssetConnectionException {
        LOGGER.info("reading value from bar value provider");
        try {
            return PropertyValue.of(Datatype.STRING, "bar value");
        }
        catch (ValueFormatException e) {
            throw new AssetConnectionException("failed to read from bar value provider", e);
        }
    }


    @Override
    public void setValue(DataElementValue value) throws AssetConnectionException {
        try {
            LOGGER.info("writing value to bar value provider (value: {})", new ObjectMapper().writeValueAsString(value));
        }
        catch (JsonProcessingException e) {
            throw new AssetConnectionException("failed to write to bar value provider", e);
        }
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), reference, providerConfig);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BarValueProvider that = (BarValueProvider) obj;
        return super.equals(that)
                && Objects.equals(reference, that.reference)
                && Objects.equals(providerConfig, that.providerConfig);
    }


    @Override
    public BarValueProviderConfig asConfig() {
        return providerConfig;
    }
}
