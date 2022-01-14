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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http;

import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Extend;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.api.Level;
import org.junit.Assert;


public class EnumMapperTest {

    @org.junit.Test
    public void testContentMapper() {
        Assert.assertEquals(Content.Normal, Content.fromString(""));
        Assert.assertEquals(Content.Normal, Content.fromString("normal"));
        Assert.assertEquals(Content.Trimmed, Content.fromString("trimmed"));
        Assert.assertEquals(Content.Reference, Content.fromString("reference"));
        Assert.assertEquals(Content.Path, Content.fromString("path"));
    }


    @org.junit.Test
    public void testLevelMapper() {
        Assert.assertEquals(Level.Core, Level.fromString(""));
        Assert.assertEquals(Level.Core, Level.fromString("core"));
        Assert.assertEquals(Level.Deep, Level.fromString("deep"));
    }


    @org.junit.Test
    public void testExtendMapper() {
        Assert.assertEquals(Extend.WithoutBLOBValue, Extend.fromString(""));
        Assert.assertEquals(Extend.WithoutBLOBValue, Extend.fromString("withoutblobvalue"));
        Assert.assertEquals(Extend.WithBLOBValue, Extend.fromString("withblobvalue"));
    }
}
