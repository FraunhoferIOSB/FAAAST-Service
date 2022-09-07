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

import java.io.File;
import java.util.NoSuchElementException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class PersistenceConfigurationTest {

    private static final String MODEL_RESOURCE_PATH = PersistenceConfigurationTest.class.getResource("/AASMinimal.json").getPath();
    protected PersistenceConfig persistenceConfig;

    @Before
    public void init() {
        this.persistenceConfig = new PersistenceConfig();
    }


    @Test
    public void setInitialModelWithValidPathTest() {
        this.persistenceConfig.setInitialModel(new File(MODEL_RESOURCE_PATH));
        Assert.assertEquals(new File(MODEL_RESOURCE_PATH), persistenceConfig.getInitialModel());
    }


    @Test
    public void setInitialModelWithInvalidPathTest() {
        File testFileWithInvalidPath = new File("some/erroneous/path.json");

        Assert.assertThrows(NoSuchElementException.class,
                () -> this.persistenceConfig.setInitialModel(testFileWithInvalidPath));
    }
}
