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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.util;

import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Helper class for working with {@link java.util.concurrent.TimeUnit}.
 */
public class TimeUnitHelper {
    private static final Map<Reference, TimeUnit> timeUnitMapping = Map.of(
            ReferenceBuilder.global(Constants.TIMEUNIT_MILLISECOND), TimeUnit.MILLISECONDS,
            ReferenceBuilder.global(Constants.TIMEUNIT_SECOND), TimeUnit.SECONDS,
            ReferenceBuilder.global(Constants.TIMEUNIT_MINUTE), TimeUnit.MINUTES);

    /**
     * Returns {@link java.util.concurrent.TimeUnit} from a semanticId.
     *
     * @param semanticId the semanticId
     * @return the corresponding {@link java.util.concurrent.TimeUnit}
     * @throws IllegalArgumentException if semanticId does not match supported {@link java.util.concurrent.TimeUnit}
     */
    public static TimeUnit fromSemanticId(Reference semanticId) {
        if (timeUnitMapping.containsKey(semanticId)) {
            return timeUnitMapping.get(semanticId);
        }
        throw new IllegalArgumentException(String.format("unsupported time unit (semanticId: '%s')", semanticId));
    }


    /**
     * Returns the semanticId for a given {@link java.util.concurrent.TimeUnit}.
     *
     * @param timeUnit the time unit to convert
     * @return the corresponding semanticId
     * @throws IllegalArgumentException if {@link java.util.concurrent.TimeUnit} is not supported
     */
    public static Reference toSemanticId(TimeUnit timeUnit) {
        return timeUnitMapping.entrySet().stream()
                .filter(x -> Objects.equals(timeUnit, x.getValue()))
                .map(Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("unsupported time unit (timeUnit: '%s')", timeUnit)));
    }


    private TimeUnitHelper() {

    }
}
