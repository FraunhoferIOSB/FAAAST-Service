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
package de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.model.value.FileValue;
import io.adminshell.aas.v3.model.File;


public class FileValueMapper implements DataValueMapper<File, FileValue> {

    @Override
    public FileValue toValue(File submodelElement) {
        if (submodelElement == null) {
            return null;
        }
        FileValue fileValue = new FileValue();
        fileValue.setValue(submodelElement.getValue());
        fileValue.setMimeType(submodelElement.getMimeType());
        return fileValue;
    }


    @Override
    public File setValue(File submodelElement, FileValue value) {
        DataValueMapper.super.setValue(submodelElement, value);
        submodelElement.setValue(value.getValue());
        submodelElement.setMimeType(value.getMimeType());
        return submodelElement;
    }
}
