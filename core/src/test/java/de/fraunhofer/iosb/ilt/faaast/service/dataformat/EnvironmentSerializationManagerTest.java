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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.json.JsonDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.xml.XmlDeserializer;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;


public class EnvironmentSerializationManagerTest {

    @Test
    public void testFromFileJSON() throws IOException, DeserializationException, Exception {
        assertEquals("src/test/resources/AASFull.json", new JsonDeserializer());
    }


    @Test
    public void testFromFileXML() throws IOException, DeserializationException, Exception {
        assertEquals("src/test/resources/AASFull.xml", new XmlDeserializer());
    }


    @Test
    @Ignore("Curently not supported by AAS4j")
    public void testFromFileOPCUA() throws IOException, DeserializationException, Exception {
        //assertEquals("src/test/resources/AASSimple.xml", new I4AASDeserializer());
    }


    @Test
    @Ignore("Curently not supported by AAS4j")
    public void testFromFileRDF() throws IOException, DeserializationException, Exception {
        // assertEquals("src/test/resources/AASFull.rdf", new org.eclipse.digitaltwin.aas4j.v3.dataformat.rdf.Serializer());
    }


    @Test(expected = DeserializationException.class)
    public void testFromFileFileNotExists() throws IOException, DeserializationException, Exception {
        EnvironmentSerializationManager.deserialize(new File("src/test/resources/AASSimple.foo"));
    }


    private void assertEquals(String filePath, JsonDeserializer deserializer) throws Exception, FileNotFoundException, DeserializationException {
        Environment expected = deserializer.read(new FileInputStream(new File(filePath)), Environment.class);
        Environment actual = EnvironmentSerializationManager.deserialize(new File(filePath)).getEnvironment();
        Assert.assertEquals(expected, actual);
    }


    private void assertEquals(String filePath, XmlDeserializer deserializer) throws Exception, FileNotFoundException, DeserializationException {
        Environment expected = deserializer.read(new File(filePath));
        Environment actual = EnvironmentSerializationManager.deserialize(new File(filePath)).getEnvironment();
        Assert.assertEquals(expected, actual);
    }
}
