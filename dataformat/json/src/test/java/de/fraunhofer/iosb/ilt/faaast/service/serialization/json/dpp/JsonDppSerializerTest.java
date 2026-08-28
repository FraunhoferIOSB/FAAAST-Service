/*
 * Copyright 2026 Fraunhofer IOSB.
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

package de.fraunhofer.iosb.ilt.faaast.service.serialization.json.dpp;

import static de.fraunhofer.iosb.ilt.faaast.service.model.DPP.DPP_1;

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.dpp.JsonDppSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DigitalProductPassport;
import de.fraunhofer.iosb.ilt.faaast.service.model.dpp.DppSerializationMode;
import de.fraunhofer.iosb.ilt.faaast.service.serialization.json.fixture.DppExamples;
import java.io.File;
import java.nio.file.Files;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;


public class JsonDppSerializerTest {

    private final JsonDppSerializer serializer = new JsonDppSerializer();

    @Test
    public void test1() throws Exception {
        assertEquals(DppExamples.DPP_1_COMPRESSED_FILE, DPP_1, DppSerializationMode.COMPRESSED);
    }


    private void assertEquals(File expectedFile, DigitalProductPassport dpp, DppSerializationMode mode) throws Exception {
        assertEquals(Files.readString(expectedFile.toPath()), dpp, mode);
    }


    private void assertEquals(String expected, DigitalProductPassport dpp, DppSerializationMode mode) throws Exception {
        String actual = serializer.write(dpp, mode);
        JSONAssert.assertEquals(expected, actual, JSONCompareMode.NON_EXTENSIBLE);
    }
}
