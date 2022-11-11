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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import io.adminshell.aas.v3.model.MultiLanguageProperty;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.impl.DefaultProperty;
import io.adminshell.aas.v3.model.impl.DefaultSubmodelElementCollection;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * Represents a segment according to SMT TimeSeries.
 */
public abstract class Segment extends DefaultSubmodelElementCollection {

    protected MultiLanguageProperty name;
    protected long samplingInterval;
    protected TimeUnit samplingIntervalUnit;
    protected long samplingRate;
    protected TimeUnit samplingRateUnit;
    protected String kind;
    protected List<Record> records;

    private static final class ZonedDateTimeComparator implements Comparator<ZonedDateTime> {

        @Override
        public int compare(ZonedDateTime dateTime1, ZonedDateTime dateTime2) {
            return dateTime1.compareTo(dateTime2);
        }

    }

    @Override
    public Collection<SubmodelElement> getValues() {
        return new ArrayList<>(Arrays.asList(
                new DefaultProperty.Builder()
                        .idShort("RecordCount")
                        .valueType(Datatype.LONG.getName())
                        .value(Integer.toString(records.size()))
                        .build(),
                new DefaultProperty.Builder()
                        .idShort("StartTime")
                        .valueType(Datatype.DATE_TIME.getName())
                        .value(records.stream().map(x -> x.getTime()).min(new ZonedDateTimeComparator()).toString())
                        .build(),
                new DefaultProperty.Builder()
                        .idShort("EndTime")
                        .valueType(Datatype.DATE_TIME.getName())
                        .value(records.stream().map(x -> x.getTime()).max(new ZonedDateTimeComparator()).toString())
                        .build(),
                new DefaultSubmodelElementCollection.Builder()
                        .idShort("Records")
                        .values(records.stream().map(x -> (SubmodelElement) x).collect(Collectors.toList()))
                        .build()));
    }
}
