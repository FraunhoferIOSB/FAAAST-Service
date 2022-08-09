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


public class AbstractInMemoryPersistenceTest extends AbstractInMemoryPersistenceBaseTest {

    @Override
    public Persistence getPersistenceImplementation() {
        return new Implementation();
    }


    @Override
    public AssetAdministrationShellEnvironment getEnvironment() {
        return AASFull.createEnvironment();
    }


    @Override
    public PersistenceConfig getPersistenceConfig() {
        return new Config(getEnvironment());
    }

    static class Implementation extends AbstractInMemoryPersistence<Config> {

    }

    static class Config extends PersistenceConfig<Implementation> {

        protected Config(AssetAdministrationShellEnvironment environment) {
            this.setEnvironment(environment);
        }
    }
}
