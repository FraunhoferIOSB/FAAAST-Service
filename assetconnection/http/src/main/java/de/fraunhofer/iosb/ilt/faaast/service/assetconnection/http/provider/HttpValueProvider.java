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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.provider;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetValueProvider;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.content.ContentDeserializerFactory;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http.content.ContentSerializerFactory;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.Reference;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;


public class HttpValueProvider implements AssetValueProvider {

    private final HttpValueProviderConfig providerConfig;
    private final Reference reference;
    private final ServiceContext serviceContext;
    private final String serverUri;

    /**
     * Creates new instance.
     *
     * @param serverUri serverUri to use, must be non-null
     * @param providerConfig configuration, must be non-null
     * @throws IllegalArgumentException if client is null
     * @throws IllegalArgumentException if providerConfig is null
     */
    public HttpValueProvider(ServiceContext serviceContext, Reference reference, String serverUri, HttpValueProviderConfig providerConfig) {
        if (serverUri == null) {
            throw new IllegalArgumentException("serverUri must be non-null");
        }
        if (providerConfig == null) {
            throw new IllegalArgumentException("providerConfig must be non-null");
        }
        this.serverUri = serverUri;
        this.providerConfig = providerConfig;
        this.serviceContext = serviceContext;
        this.reference = reference;
    }

    @Override
    public DataElementValue getValue() throws AssetConnectionException {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(serverUri + providerConfig.getPath()).openConnection();
            connection.setRequestMethod("GET");
            int statusCode = connection.getResponseCode();
            String body = readInputStream(connection.getInputStream());
            if(statusCode != connection.HTTP_OK) {
                throw new AssetConnectionException(String.format("error reading value from asset conenction (reference: %s)", AasUtils.asString(reference)));
            }
            connection.disconnect();
            return ContentDeserializerFactory
                    .create(providerConfig.getContentFormat())
                    .read(body,
                            providerConfig.getQuery(),
                            serviceContext.getTypeInfo(reference));
        } catch (IOException e) {
            e.printStackTrace();
            throw new AssetConnectionException(String.format("error reading value from asset conenction (reference: %s)", AasUtils.asString(reference)), e);
        }
    }

    private String readInputStream(InputStream inputStream) throws IOException {
        StringBuffer buffer = new StringBuffer();
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        }
        return buffer.toString();
    }



    @Override
    public void setValue(DataElementValue value) throws AssetConnectionException {
        try {
            if (!(value instanceof PropertyValue)) {
                throw new AssetConnectionException(String.format("unsupported value (%s)", value.getClass().getSimpleName()));
            }
            HttpURLConnection connection = (HttpURLConnection) new URL(serverUri + providerConfig.getPath()).openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setDoOutput(true);
            OutputStream os = connection.getOutputStream();
            os.write(ContentSerializerFactory
                    .create(providerConfig.getContentFormat())
                    .write(value, providerConfig.getQuery())
                    .getBytes());
            int statusCode = connection.getResponseCode();
            if(statusCode != connection.HTTP_OK) {
                throw new AssetConnectionException(String.format("error writing value with asset conenction (reference: %s)", AasUtils.asString(reference)));
            }
        }
        catch (ProtocolException e) {
            throw new AssetConnectionException("writing value via HTTP asset connection failed", e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
