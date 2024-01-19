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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.util.function.Consumer;


/**
 * Wrapper class for {@link org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer}.
 */
public class SerializerWrapper extends org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonSerializer {

    public SerializerWrapper() {

    }


    public SerializerWrapper(Consumer<JsonMapper> modifier) {
        if (modifier != null) {
            modifier.accept(mapper);
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            mapper.enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
            mapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
        }
    }


    protected JsonMapper getMapper() {
        return mapper;
    }
}
