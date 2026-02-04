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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iosb.ilt.faaast.service.Service;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.fixtures.bar.BarConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.fixtures.bar.BarSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.fixtures.bar.BarValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.fixtures.foo.FooConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.fixtures.foo.FooOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.fixtures.foo.FooSubscriptionProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.fixtures.foo.FooValueProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.config.Configurable;
import de.fraunhofer.iosb.ilt.faaast.service.config.CoreConfig;
import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonMapperFactory;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.SimpleAbstractTypeResolverFactory;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AssetConnectionManagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssetConnectionManagerTest.class);
    private static final Reference REFRERENCE_1 = ReferenceBuilder.forAas("reference1");
    private static final Reference REFRERENCE_2 = ReferenceBuilder.forAas("reference2");

    private static ObjectMapper mapper;
    private static Service service;

    @BeforeClass
    public static void init() {
        service = mock(Service.class);
        mapper = new JsonMapperFactory().create(new SimpleAbstractTypeResolverFactory().create())
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }


    @Test
    public void testUpdateConnections_addOnlyConnection() throws Exception {
        FooConnectionConfig newConnectionConfig = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("some value")
                        .build())
                .build();
        assertUpdateConnections(
                List.of(),
                List.of(),
                List.of(newConnectionConfig),
                List.of(newConnectionConfig));
    }


    @Test
    public void testUpdateConnections_addConnection() throws Exception {
        FooConnectionConfig initialConnectionConfig = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("some value")
                        .build())
                .build();
        FooConnectionConfig newConnectionConfig = FooConnectionConfig.builder()
                .property1("foo2")
                .property2(1)
                .valueProvider(REFRERENCE_2, FooValueProviderConfig.builder()
                        .property1("some other value")
                        .build())
                .build();
        assertUpdateConnections(
                List.of(initialConnectionConfig),
                List.of(),
                List.of(newConnectionConfig),
                List.of(initialConnectionConfig, newConnectionConfig));
        assertUpdateConnections(
                List.of(initialConnectionConfig),
                List.of(initialConnectionConfig),
                List.of(initialConnectionConfig, newConnectionConfig),
                List.of(initialConnectionConfig, newConnectionConfig));
    }


    @Test
    public void testUpdateConnections_deleteOnlyConnection() throws Exception {
        FooConnectionConfig initialConnectionConfig = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("some value")
                        .build())
                .build();
        assertUpdateConnections(
                List.of(initialConnectionConfig),
                List.of(initialConnectionConfig),
                List.of(),
                List.of());
    }


    @Test
    public void testUpdateConnections_deleteConnection() throws Exception {
        FooConnectionConfig initialConnectionConfig1 = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("some value")
                        .build())
                .build();
        FooConnectionConfig initialConnectionConfig2 = FooConnectionConfig.builder()
                .property1("foo2")
                .property2(1)
                .valueProvider(REFRERENCE_2, FooValueProviderConfig.builder()
                        .property1("some other value")
                        .build())
                .build();
        assertUpdateConnections(
                List.of(initialConnectionConfig1, initialConnectionConfig2),
                List.of(initialConnectionConfig1),
                List.of(),
                List.of(initialConnectionConfig2));
        assertUpdateConnections(
                List.of(initialConnectionConfig1, initialConnectionConfig2),
                List.of(initialConnectionConfig1, initialConnectionConfig2),
                List.of(initialConnectionConfig2),
                List.of(initialConnectionConfig2));
    }


    @Test
    public void testUpdateConnections_updateConnection() throws Exception {
        FooConnectionConfig initialConnectionConfig = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("some value")
                        .build())
                .build();
        FooConnectionConfig newConnectionConfig = FooConnectionConfig.builder()
                .property1("foo new")
                .property2(1)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("some value")
                        .build())
                .build();
        assertUpdateConnections(
                List.of(initialConnectionConfig),
                List.of(initialConnectionConfig),
                List.of(newConnectionConfig),
                List.of(newConnectionConfig));
    }


    @Test
    public void testUpdateConnections_addProvider() throws Exception {
        FooConnectionConfig initialConnectionConfig = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .build();
        FooConnectionConfig newConnectionConfig = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .valueProvider(REFRERENCE_2, FooValueProviderConfig.builder()
                        .property1("intial value")
                        .build())
                .build();
        assertUpdateConnections(
                List.of(initialConnectionConfig),
                List.of(initialConnectionConfig),
                List.of(newConnectionConfig),
                List.of(newConnectionConfig));
    }


    @Test
    public void testUpdateConnections_deleteProvider() throws Exception {
        FooConnectionConfig initialConnectionConfig = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .valueProvider(REFRERENCE_2, FooValueProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .build();
        FooConnectionConfig newConnectionConfig = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .build();
        assertUpdateConnections(
                List.of(initialConnectionConfig),
                List.of(initialConnectionConfig),
                List.of(newConnectionConfig),
                List.of(newConnectionConfig));
    }


    @Test
    public void testUpdateConnections_updateProviderMapping() throws Exception {
        FooConnectionConfig initialConnectionConfig = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .build();
        FooConnectionConfig newConnectionConfig = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_2, FooValueProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .build();
        assertUpdateConnections(
                List.of(initialConnectionConfig),
                List.of(initialConnectionConfig),
                List.of(newConnectionConfig),
                List.of(newConnectionConfig));
    }


    @Test
    public void testUpdateConnections_updateProviderType() throws Exception {
        FooConnectionConfig initialConnectionConfig = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .build();
        FooConnectionConfig newConnectionConfig = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .subscriptionProvider(REFRERENCE_1, FooSubscriptionProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .build();
        assertUpdateConnections(
                List.of(initialConnectionConfig),
                List.of(initialConnectionConfig),
                List.of(newConnectionConfig),
                List.of(newConnectionConfig));
    }


    @Test
    public void testUpdateConnections_updateProviderValue() throws Exception {
        FooConnectionConfig initialConnectionConfig = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .valueProvider(REFRERENCE_2, FooValueProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .build();
        FooConnectionConfig newConnectionConfig = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("new value")
                        .build())
                .valueProvider(REFRERENCE_2, FooValueProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .build();
        assertUpdateConnections(
                List.of(initialConnectionConfig),
                List.of(initialConnectionConfig),
                List.of(newConnectionConfig),
                List.of(newConnectionConfig));
    }


    @Test
    public void testUpdateConnections_complexScenario() throws Exception {
        Reference reference3 = ReferenceBuilder.forAas("reference3");
        Reference reference4 = ReferenceBuilder.forAas("reference4");

        FooConnectionConfig initialConnectionConfig1 = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .subscriptionProvider(REFRERENCE_1, FooSubscriptionProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .build();
        BarConnectionConfig initialConnectionConfig2 = BarConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_2, BarValueProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .subscriptionProvider(REFRERENCE_2, BarSubscriptionProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .build();

        FooConnectionConfig newConnectionConfig1 = FooConnectionConfig.builder()
                .property1("foo2")
                .property2(2)
                .valueProvider(REFRERENCE_1, FooValueProviderConfig.builder()
                        .property1("updated value")
                        .build())
                .operationProvider(reference3, FooOperationProviderConfig.builder()
                        .property1("new value")
                        .build())
                .build();
        FooConnectionConfig newConnectionConfig2 = FooConnectionConfig.builder()
                .property1("foo")
                .property2(1)
                .valueProvider(REFRERENCE_2, FooValueProviderConfig.builder()
                        .property1("updated value")
                        .build())
                .subscriptionProvider(REFRERENCE_2, FooSubscriptionProviderConfig.builder()
                        .property1("initial value")
                        .build())
                .operationProvider(reference4, FooOperationProviderConfig.builder()
                        .property1("new value")
                        .build())
                .build();
        assertUpdateConnections(
                List.of(initialConnectionConfig1, initialConnectionConfig2),
                List.of(initialConnectionConfig1, initialConnectionConfig2),
                List.of(newConnectionConfig1, newConnectionConfig2),
                List.of(newConnectionConfig1, newConnectionConfig2));
    }


    private void assertUpdateConnections(List<AssetConnectionConfig> initialConnectionConfigs,
                                         List<AssetConnectionConfig> oldConnectionConfigs,
                                         List<AssetConnectionConfig> newConnectionConfigs,
                                         List<AssetConnectionConfig> expectedConnectionConfigs)
            throws Exception {
        for (var oldConnectionConfig: oldConnectionConfigs) {
            if (!initialConnectionConfigs.contains(oldConnectionConfig)) {
                LOGGER.warn("old connection config missing from initial connection configs - has been added: \n{}", mapper.writeValueAsString(oldConnectionConfig));
                initialConnectionConfigs.add(oldConnectionConfig);
            }
        }
        List<AssetConnection> initialConnections = initialConnectionConfigs.stream()
                .map(LambdaExceptionHelper.rethrowFunction(x -> (AssetConnection) x.newInstance(CoreConfig.DEFAULT, service)))
                .toList();
        AssetConnectionManager assetConnectionManager = new AssetConnectionManager(CoreConfig.DEFAULT, initialConnections, service);
        assetConnectionManager.updateConnections(oldConnectionConfigs, newConnectionConfigs);
        assertNotNull(assetConnectionManager.getConnections());
        assertEquals(expectedConnectionConfigs.size(), assetConnectionManager.getConnections().size());
        List<AssetConnectionConfig> actualConnectionConfigs = assetConnectionManager.getConnections().stream()
                .map(Configurable::asConfig)
                .map(AssetConnectionConfig.class::cast)
                .toList();
        LOGGER.info("expected: {}", mapper.writeValueAsString(expectedConnectionConfigs));
        LOGGER.info("actual: {}", mapper.writeValueAsString(actualConnectionConfigs));
        assertTrue(actualConnectionConfigs.containsAll(expectedConnectionConfigs));
    }
}
