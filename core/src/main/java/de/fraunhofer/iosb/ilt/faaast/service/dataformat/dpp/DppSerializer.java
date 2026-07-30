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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.dpp;

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DigitalProductPassport;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;


/**
 * Seriliazer for DPP instances.
 */
public interface DppSerializer {

    public Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * Serializes given DPP as string.
     *
     * @param dpp the DPP to serialize
     * @param mode serializatin mode to use
     * @return string serialization of DPP
     * @throws SerializationException if serialization fails
     */
    public String write(DigitalProductPassport dpp, DppSerializationMode mode) throws SerializationException;


    /**
     * Serializes given DPP as string with default serialization mode.
     *
     * @param dpp the DPP to serialize
     * @return string serialization of DPP
     * @throws SerializationException if serialization fails
     */
    public default String write(DigitalProductPassport dpp) throws SerializationException {
        return write(dpp, DppSerializationMode.DEFAULT);
    }
}
