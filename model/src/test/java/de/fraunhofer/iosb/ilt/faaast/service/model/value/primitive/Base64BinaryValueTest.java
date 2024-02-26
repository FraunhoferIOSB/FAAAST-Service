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
package de.fraunhofer.iosb.ilt.faaast.service.model.value.primitive;

import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.Datatype;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.TypedValueFactory;
import java.util.Base64;
import org.junit.Assert;
import org.junit.Test;


public class Base64BinaryValueTest {

    @Test
    public void testBase64Binary() throws ValueFormatException {
        byte[] expected = "test".getBytes();
        String value = Base64.getEncoder().encodeToString(expected);
        TypedValue actual = TypedValueFactory.create(Datatype.BASE64_BINARY, value);
        Assert.assertArrayEquals(expected, (byte[]) actual.getValue());
        Assert.assertEquals(value, actual.asString());
    }

}
