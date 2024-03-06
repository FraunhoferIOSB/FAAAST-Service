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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.PagingMetadata;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.FaaastConstants;
import java.io.IOException;
import java.util.Objects;


/**
 * Deserializer for {@code PagingMetadata}.
 */
public class PagingMetadataDeserializer extends StdDeserializer<PagingMetadata> {

    public PagingMetadataDeserializer() {
        this(null);
    }


    public PagingMetadataDeserializer(Class<PagingMetadata> t) {
        super(t);
    }


    @Override
    public PagingMetadata deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonNode node = parser.getCodec().readTree(parser);
        if (Objects.isNull(node)) {
            return null;
        }
        PagingMetadata result = new PagingMetadata();
        if (node.hasNonNull(FaaastConstants.CURSOR)) {
            result.setCursor(EncodingHelper.base64UrlDecode(node.get(FaaastConstants.CURSOR).asText()));
        }
        return result;
    }
}
