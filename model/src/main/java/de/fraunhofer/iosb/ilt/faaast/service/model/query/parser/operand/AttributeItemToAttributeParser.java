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
package de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.operand;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AttributeItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.Attribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.ClaimAttribute;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.Anonymous;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.ClientNow;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.LocalNow;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.operand.attribute.global.UtcNow;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.AbstractParser;

import java.util.Objects;


public class AttributeItemToAttributeParser extends AbstractParser<AttributeItem, Attribute> {
    @Override
    public Attribute parse(AttributeItem item) {
        Objects.requireNonNull(item, "item must not be null");
        if (item.getClaim() != null) {
            return new ClaimAttribute(item.getClaim());
        }
        if (item.getGlobal() != null) {
            return switch (item.getGlobal()) {
                case LOCALNOW -> new LocalNow();
                case UTCNOW -> new UtcNow();
                case CLIENTNOW -> new ClientNow();
                case ANONYMOUS -> new Anonymous();
            };
        }
        if (item.getReference() != null) {
            throw new UnsupportedOperationException(String.format("Reference attributes are not yet supported in the query AST: '%s'", item.getReference()));
        }
        throw new IllegalArgumentException(String.format("Unsupported attribute item: %s", item));
    }
}
