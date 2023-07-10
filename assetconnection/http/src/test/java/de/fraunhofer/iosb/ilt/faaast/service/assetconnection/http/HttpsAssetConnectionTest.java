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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.http;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException;
import de.fraunhofer.iosb.ilt.faaast.service.exception.ConfigurationInitializationException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive.ValueFormatException;
import java.net.MalformedURLException;
import java.net.URL;
import org.junit.Before;
import org.junit.Test;


public class HttpsAssetConnectionTest extends AssetConnectionBaseTest {

    @Before
    public void init() throws MalformedURLException {
        baseUrl = new URL("https", "localhost", wireMockRule.httpsPort(), "");
    }


    @Override
    protected String getKeyStorePath() {
        return keyStoreFile.getAbsolutePath();
    }


    @Override
    protected WireMockRule createWireMockRule() {
        generateSelfSignedServerCertificate();
        return new WireMockRule(options()
                .dynamicHttpsPort()
                .keystoreType(KEYSTORE_TYPE_SERVER)
                .keystorePath(getKeyStorePath())
                .keystorePassword(KEYSTORE_PASSWORD)
                .keyManagerPassword(KEYMANAGER_PASSWORD));
    }


    @Test
    public void testValueProviderPropertySetValueWithTemplateJSON()
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException, InterruptedException {
        String template = "{\"foo\" : \"${value}\", \"bar\": [1, 2, 3]}";
        String value = "5";
        assertValueProviderPropertyWriteJson(
                PropertyValue.of(Datatype.INT, "5"),
                template,
                template.replaceAll("\\$\\{value\\}", value));
    }


    @Override
    public void testValueProviderWithHeaders() {}


    @Override
    public void testValueProviderPropertyGetValueWithQueryJSON() {}


    @Override
    public void testValueProviderPropertySetValueJSON() {}


    @Override
    public void testSubscriptionProviderPropertyJsonGET() {}


    @Override
    public void testSubscriptionProviderPropertyJsonGET2() {}


    @Override
    public void testSubscriptionProviderPropertyJsonPOST() {}


    @Override
    public void testOperationProviderPropertyJsonPOSTNoParameters() {}


    @Override
    public void testOperationProviderPropertyJsonPOSTInputOnly() {}


    @Override
    public void testOperationProviderPropertyJsonPOSTOutputOnly() {}


    @Override
    public void testOperationProviderPropertyJsonPOSTInputOutputOnly() {}


    @Override
    public void testOperationProviderPropertyJsonPOSTInoutputOnly() {}


    @Override
    public void testOperationProviderPropertyJsonPOST() {}


    @Override
    public void testValueProviderPropertyGetValueJSON() {}

}
