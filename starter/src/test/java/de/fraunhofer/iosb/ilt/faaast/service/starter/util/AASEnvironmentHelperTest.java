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
package de.fraunhofer.iosb.ilt.faaast.service.starter.util;

import de.fraunhofer.iosb.ilt.faaast.service.util.AASEnvironmentHelper;
import io.adminshell.aas.v3.dataformat.DeserializationException;
import io.adminshell.aas.v3.dataformat.Deserializer;
import io.adminshell.aas.v3.dataformat.aml.AmlDeserializer;
import io.adminshell.aas.v3.dataformat.i4aas.I4AASDeserializer;
import io.adminshell.aas.v3.dataformat.json.JsonDeserializer;
import io.adminshell.aas.v3.dataformat.xml.XmlDeserializer;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


public class AASEnvironmentHelperTest {

    @Test
    public void testFromFileJSON() throws IOException, DeserializationException, Exception {
        assertEquals("src/test/resources/AASFull.json", new JsonDeserializer());
    }


    @Test
    public void testFromFileXML() throws IOException, DeserializationException, Exception {
        assertEquals("src/test/resources/AASFull.xml", new XmlDeserializer());
    }


    @Test
    @Ignore("Not Yet")
    public void testFromFileAML() throws IOException, DeserializationException, Exception {
        assertEquals("src/test/resources/AASFull.aml", new AmlDeserializer());
    }


    @Test
    @Ignore("Not yet")
    public void testFromFileOPCUA() throws IOException, DeserializationException, Exception {
        assertEquals("src/test/resources/AASSimple.xml", new I4AASDeserializer());
    }


    @Test
    public void testFromFileRDF() throws IOException, DeserializationException, Exception {
        assertEquals("src/test/resources/AASFull.rdf", new io.adminshell.aas.v3.dataformat.rdf.Serializer());
    }


    @Test(expected = DeserializationException.class)
    public void testFromFileFileNotExists() throws IOException, DeserializationException, Exception {
        AASEnvironmentHelper.fromFile(new File("src/test/resources/AASSimple.foo"));
    }


    private void assertEquals(String filePath, Deserializer deserializer) throws Exception, FileNotFoundException, DeserializationException {
        AssetAdministrationShellEnvironment expected = deserializer.read(new File(filePath));
        AssetAdministrationShellEnvironment actual = AASEnvironmentHelper.fromFile(new File(filePath));
        Assert.assertEquals(expected, actual);
    }
}
