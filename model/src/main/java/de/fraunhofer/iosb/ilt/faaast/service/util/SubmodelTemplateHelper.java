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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import de.fraunhofer.iosb.ilt.faaast.service.model.submodeltemplate.Cardinality;
import java.util.Objects;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.internal.deserialization.EnumDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.DataTypeDefXsd;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifiable;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.QualifierKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class for working with submodel templates.
 */
public class SubmodelTemplateHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubmodelTemplateHelper.class);

    private SubmodelTemplateHelper() {}


    /**
     * Gets the cardinality defined by the qualifiable. If not (valid) cardinality is found, {@link Cardinality#ONE} is
     * returned.
     *
     * @param element the element to check
     * @return the cardinaility or {@link Cardinality#ONE} is none is defined
     */
    public static Cardinality getCardinality(Qualifiable element) {
        if (Objects.isNull(element)
                || Objects.isNull(element.getQualifiers())
                || element.getQualifiers().isEmpty()) {
            return Cardinality.DEFAULT;
        }
        Optional<Qualifier> cardinalityQualifier = element.getQualifiers().stream()
                .filter(Objects::nonNull)
                .filter(x -> x.getKind() == QualifierKind.TEMPLATE_QUALIFIER)
                .filter(x -> x.getValueType() == DataTypeDefXsd.STRING)
                .filter(x -> Objects.equals(x.getType(), Cardinality.class.getSimpleName()))
                .findFirst();
        if (cardinalityQualifier.isEmpty()) {
            return Cardinality.DEFAULT;
        }
        try {
            return Cardinality.valueOf(EnumDeserializer.deserializeEnumName(cardinalityQualifier.get().getValue()));
        }
        catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("Encountered invalid SMT cardinality qualifier: %s", cardinalityQualifier.get().getValue()));
            return Cardinality.DEFAULT;
        }
    }


    /**
     * Removes the cardinality information from the qualifiable.
     *
     * @param element the element to check
     */
    public static void removeCardinalities(Qualifiable element) {
        if (Objects.isNull(element)
                || Objects.isNull(element.getQualifiers())
                || element.getQualifiers().isEmpty()) {
            return;
        }
        element.getQualifiers().removeIf(x -> Objects.nonNull(x)
                && x.getKind() == QualifierKind.TEMPLATE_QUALIFIER
                && x.getValueType() == DataTypeDefXsd.STRING
                && Objects.equals(x.getType(), Cardinality.class.getSimpleName()));

    }
}
