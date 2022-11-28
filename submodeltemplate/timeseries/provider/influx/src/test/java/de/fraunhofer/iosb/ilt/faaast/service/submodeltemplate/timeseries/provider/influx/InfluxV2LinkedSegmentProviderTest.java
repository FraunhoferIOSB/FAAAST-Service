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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx;

import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.Record;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.LinkedSegmentProviderConfig;
import io.apisense.embed.influx.ServerAlreadyRunningException;
import java.io.IOException;
import java.util.List;
import org.junit.AfterClass;
import org.junit.Ignore;


@Ignore
public class InfluxV2LinkedSegmentProviderTest extends AbstractInfluxLinkedSegmentProviderTest {

    @Override
    protected LinkedSegmentProviderConfig<?> getProviderConfig() {
        return InfluxV2LinkedSegmentProviderConfig.builder()
                .endpoint(endpoint)
                .bucket(DATABASE)
                // TODO set correct value
                .fieldKey("???")
                .build();
    }


    @Override
    protected String getQuery() {
        return String.format("from(bucket:\"%s\") "
                + "|> range(start: 0) "
                + "|> filter(fn: (r) => r._measurement == \"%s\")",
                DATABASE,
                MEASUREMENT);
    }


    @Override
    protected void initInflux(List<Record> records) {
        // init database
    }


    @Override
    protected String buildEndpointUrl(int port) {
        return String.format("http://localhost:%d/query?db=%s", port, DATABASE);
    }


    @Override
    protected void startInflux(int port) throws IOException, ServerAlreadyRunningException {
        // start server
    }


    @AfterClass
    public static void stopInflux() {
        // stop server
    }

}
