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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.csv;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValueFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.ExternalSegmentProvider;
import java.util.Objects;


/**
 * Base class for data providers for external segments referencing an CSV data file or blob containing a CSV file.
 *
 * @param <T> type of corresponding config to use
 */
public abstract class AbstractCSVExternalSegmentProvider<T extends AbstractCSVExternalSegmentProviderConfig> implements ExternalSegmentProvider<T> {

    protected static final String ACCEPTED_MIMETYPE = "text/csv";

    private T config;

    @Override
    public T asConfig() {
        return config;
    }


    /**
     * Parse a value to AAS {@link de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.TypedValue}.
     *
     * @param value the value to parse
     * @param datatype the datatype
     * @return the parse value
     * @throws ValueFormatException if parsign fails
     */
    protected static TypedValue parseValue(Object value, Datatype datatype) throws ValueFormatException {
        Object valuePreprocessed = value;
        switch (datatype) {
            case BYTE:
            case INT:
            case INTEGER:
            case SHORT: {
                if (value instanceof Number) {
                    valuePreprocessed = ((Number) value).intValue();
                }
                break;
            }
            default:
                // intentionally left empty
        }
        return TypedValueFactory.create(datatype, Objects.toString(valuePreprocessed));
    }
}
