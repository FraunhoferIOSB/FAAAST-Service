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
package de.fraunhofer.iosb.ilt.faaast.service.starter.cli;

import ch.qos.logback.classic.Level;
import picocli.CommandLine.ITypeConverter;


/**
 * Utility class to parse {@link ch.qos.logback.classic.Level} from string when provided as CLI argument.
 */
public class LogLevelTypeConverter implements ITypeConverter<Level> {

    @Override
    public Level convert(String value) throws Exception {
        return Level.toLevel(value);
    }

}
