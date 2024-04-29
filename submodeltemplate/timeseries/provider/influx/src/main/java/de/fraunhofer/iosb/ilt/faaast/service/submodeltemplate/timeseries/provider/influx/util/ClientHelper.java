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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider.influx.util;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import java.util.Objects;


/**
 * Helper for creating InfluxDB clients.
 */
public class ClientHelper {

    private ClientHelper() {

    }


    /**
     * Creates a new cient.
     *
     * @param endpoint the endpoint
     * @param bucket the bucket
     * @param organization the organization
     * @param username the username
     * @param password the password
     * @param token the token
     * @return a new client instance
     */
    public static InfluxDBClient createClient(String endpoint, String bucket, String organization, String username, String password, String token) {
        InfluxDBClientOptions.Builder clientOptions = InfluxDBClientOptions.builder()
                .url(endpoint)
                .bucket(bucket)
                .org(organization);
        if (Objects.nonNull(username) && Objects.nonNull(password)) {
            clientOptions = clientOptions.authenticate(username, password.toCharArray());
        }
        if (Objects.nonNull(token)) {
            clientOptions = clientOptions.authenticateToken(token.toCharArray());
        }
        return InfluxDBClientFactory.create(clientOptions.build());
    }
}
