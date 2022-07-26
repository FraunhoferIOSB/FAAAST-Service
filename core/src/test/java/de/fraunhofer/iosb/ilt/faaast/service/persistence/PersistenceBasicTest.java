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
package de.fraunhofer.iosb.ilt.faaast.service.persistence;

import de.fraunhofer.iosb.ilt.faaast.service.model.AASFull;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import org.junit.Assert;
import org.junit.Test;


public class PersistenceBasicTest extends PersistenceSuperTest {

    class PersistenceTest extends PersistenceBasic<PersistenceTestConfig> {
        public String afterInitValue;

        @Override
        public void afterInit() {
            afterInitValue = "initialized";
        }
    }

    static class PersistenceTestConfig extends PersistenceConfig<PersistenceTest> {
        public static Builder builder() {
            return new Builder();
        }

        private abstract static class AbstractBuilder<T extends PersistenceTestConfig, B extends AbstractBuilder<T, B>>
                extends PersistenceConfig.AbstractBuilder<PersistenceTest, T, B> {

        }

        public static class Builder extends AbstractBuilder<PersistenceTestConfig, Builder> {
            @Override
            protected Builder getSelf() {
                return this;
            }


            @Override
            protected PersistenceTestConfig newBuildingInstance() {
                return new PersistenceTestConfig();
            }
        }
    }

    @Override
    public Persistence getPersistenceImplementation() {
        return new PersistenceTest();
    }


    @Override
    public AssetAdministrationShellEnvironment getEnvironment() {
        return AASFull.createEnvironment();
    }


    @Override
    public PersistenceConfig getPersistenceConfig() {
        return PersistenceTestConfig.builder()
                .environment(getEnvironment())
                .build();
    }


    @Test
    public void testAfterInit() {
        Assert.assertEquals("initialized", ((PersistenceTest) persistence).afterInitValue);
    }

}
