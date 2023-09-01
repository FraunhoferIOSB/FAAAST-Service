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

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.TimeSeriesData;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.ExternalSegment;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Metadata;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import java.util.List;


public class DummyExternalSegmentProvider implements ExternalSegmentProvider<DummyExternalSegmentProviderConfig> {

    public static List<Record> RECORDS = List.of(
            TimeSeriesData.RECORD_05,
            TimeSeriesData.RECORD_06,
            TimeSeriesData.RECORD_07);

    @Override
    public List<Record> getRecords(Metadata metadata, ExternalSegment segment) throws SegmentProviderException {
        return RECORDS;
    }


    @Override
    public void init(CoreConfig coreConfig, DummyExternalSegmentProviderConfig config, ServiceContext serviceContext)
            throws ConfigurationInitializationException {
        // do nothing

    }


    @Override
    public DummyExternalSegmentProviderConfig asConfig() {
        return null;
    }

}
