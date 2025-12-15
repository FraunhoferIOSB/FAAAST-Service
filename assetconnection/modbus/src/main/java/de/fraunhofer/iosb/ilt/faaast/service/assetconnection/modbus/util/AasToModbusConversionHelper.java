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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.util;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.DataElementValue;
import java.nio.charset.StandardCharsets;
import org.eclipse.digitaltwin.aas4j.v3.model.File;


public class AasToModbusConversionHelper {

    private AasToModbusConversionHelper() {

    }


    public static byte[] convert(DataElementValue dataElementValue) {
        if (dataElementValue instanceof BlobValue blobValue) {
            return blobValue.getValue();
        }
        else if (dataElementValue instanceof File file) {
            return file.getValue().getBytes(StandardCharsets.UTF_8);
        }
        else {
            throw new UnsupportedOperationException(
                    String.format("Data type currently not supported for writing to modbus server: %s", dataElementValue.getClass().getName()));
        }
    }

}
