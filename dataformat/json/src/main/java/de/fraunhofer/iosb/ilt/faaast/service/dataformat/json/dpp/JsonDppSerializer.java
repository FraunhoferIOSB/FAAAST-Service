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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.dpp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.dpp.DppSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.*;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Extent;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Level;
import de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DigitalProductPassport;
import de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DppSerializationMode;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.UnsupportedContentModifierException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.FileValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.Objects;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;


/**
 * JSON API serializer for FA³ST supporting different output modifier as defined by specification.
 */
public class JsonDppSerializer implements DppSerializer {

    private final ValueOnlyJsonSerializer valueOnlySerializer;

    public JsonDppSerializer() {
        this.valueOnlySerializer = new ValueOnlyJsonSerializer() {
            @Override
            protected JsonMapper modifyMapper(JsonMapper mapper) {
                JsonMapper result = super.modifyMapper(mapper);
                SimpleModule module = new SimpleModule();
                module.addSerializer(MultiLanguagePropertyValue.class, new DppMultiLanguagePropertyValueSerializer());
                module.addSerializer(FileValue.class, new DppFileValueSerializer());
                return (JsonMapper) result.registerModule(module);
            }
        };
    }


    @Override
    public String write(DigitalProductPassport dpp, DppSerializationMode mode) throws SerializationException {
        if (Objects.isNull(dpp)) {
            return "{}";
        }
        return switch (mode) {
            case COMPRESSED -> writeCompressed(dpp);
            default -> throw new SerializationException(String.format("Unsupported serialization mode '%s'", mode));
        };
    }


    private String writeCompressed(DigitalProductPassport dpp) throws SerializationException {
        try {
            ObjectNode root = JsonNodeFactory.instance.objectNode();
            JsonNode metadataNode = valueOnlySerializer.writeAsNode(dpp.getMetadata(), Level.CORE, Extent.WITHOUT_BLOB_VALUE);
            if (Objects.nonNull(metadataNode) && metadataNode.isObject()) {
                root.setAll((ObjectNode) valueOnlySerializer.writeAsNode(dpp.getMetadata(), Level.CORE, Extent.WITHOUT_BLOB_VALUE));
            }
            for (Submodel content: dpp.getContents()) {
                JsonNode contentNode = valueOnlySerializer.writeAsNode(content, Level.DEEP, Extent.WITHOUT_BLOB_VALUE);
                if (Objects.nonNull(contentNode) && contentNode.isObject()) {
                    root.withObject(ReferenceHelper.getEffectiveKey(content.getSemanticId()).getValue())
                            .setAll((ObjectNode) contentNode);
                }

            }
            return root.toPrettyString();
        }
        catch (UnsupportedContentModifierException e) {
            throw new SerializationException("unsupported conten modifier", e);
        }
    }
}
