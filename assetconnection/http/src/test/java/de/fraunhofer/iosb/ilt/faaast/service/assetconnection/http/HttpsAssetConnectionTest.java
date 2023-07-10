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
            throws AssetConnectionException, ValueFormatException, ConfigurationInitializationException {
        String template = "{\"foo\" : \"${value}\", \"bar\": [1, 2, 3]}";
        String value = "5";
        assertValueProviderPropertyWriteJson(
                PropertyValue.of(Datatype.INT, "5"),
                template,
                template.replaceAll("\\$\\{value\\}", value));
    }


    /**
     * This test case is intentionally left empty.
     * <p>
     * Explanation: implemented in HttpAssetConnectionTest
     */
    @Override
    public void testValueProviderWithHeaders() {
        // No implementation required for this test case in this test class
    }


    /**
     * This test case is intentionally left empty.
     * <p>
     * Explanation: implemented in HttpAssetConnectionTest
     */
    @Override
    public void testValueProviderPropertyGetValueWithQueryJSON() {
        // No implementation required for this test case in this test class
    }


    /**
     * This test case is intentionally left empty.
     * <p>
     * Explanation: implemented in HttpAssetConnectionTest
     */
    @Override
    public void testValueProviderPropertySetValueJSON() {
        // No implementation required for this test case in this test class
    }


    /**
     * This test case is intentionally left empty.
     * <p>
     * Explanation: implemented in HttpAssetConnectionTest
     */
    @Override
    public void testSubscriptionProviderPropertyJsonGET() {
        // No implementation required for this test case in this test class
    }


    /**
     * This test case is intentionally left empty.
     * <p>
     * Explanation: implemented in HttpAssetConnectionTest
     */
    @Override
    public void testSubscriptionProviderPropertyJsonGET2() {
        // No implementation required for this test case in this test class
    }


    /**
     * This test case is intentionally left empty.
     * <p>
     * Explanation: implemented in HttpAssetConnectionTest
     */
    @Override
    public void testSubscriptionProviderPropertyJsonPOST() {
        // No implementation required for this test case in this test class
    }


    /**
     * This test case is intentionally left empty.
     * <p>
     * Explanation: implemented in HttpAssetConnectionTest
     */
    @Override
    public void testOperationProviderPropertyJsonPOSTNoParameters() {
        // No implementation required for this test case in this test class
    }


    /**
     * This test case is intentionally left empty.
     * <p>
     * Explanation: implemented in HttpAssetConnectionTest
     */
    @Override
    public void testOperationProviderPropertyJsonPOSTInputOnly() {
        // No implementation required for this test case in this test class
    }


    /**
     * This test case is intentionally left empty.
     * <p>
     * Explanation: implemented in HttpAssetConnectionTest
     */
    @Override
    public void testOperationProviderPropertyJsonPOSTOutputOnly() {
        // No implementation required for this test case in this test class
    }


    /**
     * This test case is intentionally left empty.
     * <p>
     * Explanation: implemented in HttpAssetConnectionTest
     */
    @Override
    public void testOperationProviderPropertyJsonPOSTInputOutputOnly() {
        // No implementation required for this test case in this test class
    }


    /**
     * This test case is intentionally left empty.
     * <p>
     * Explanation: implemented in HttpAssetConnectionTest
     */
    @Override
    public void testOperationProviderPropertyJsonPOSTInoutputOnly() {
        // No implementation required for this test case in this test class
    }


    /**
     * This test case is intentionally left empty.
     * <p>
     * Explanation: implemented in HttpAssetConnectionTest
     */
    @Override
    public void testOperationProviderPropertyJsonPOST() {
        // No implementation required for this test case in this test class
    }


    /**
     * This test case is intentionally left empty.
     * <p>
     * Explanation: implemented in HttpAssetConnectionTest
     */
    @Override
    public void testValueProviderPropertyGetValueJSON() {
        // No implementation required for this test case in this test class
    }

}
