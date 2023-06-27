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
package de.fraunhofer.iosb.ilt.faaast.service.starter;

import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.junit.Before;
import picocli.CommandLine;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;


public abstract class AbstractAppTest {
    protected App application;
    protected CommandLine cmd;

    @Before
    public void initCmd() throws IOException {
        application = new App();
        application.dryRun = true;
        cmd = new CommandLine(application)
                .setCaseInsensitiveEnumValuesAllowed(true);
        cmd.setOut(new PrintWriter(new StringWriter()));
    }


    protected EnvironmentVariables withEnv(Map<String, String> variables) {
        return withEnv(variables.entrySet().stream()
                .map(x -> new String[] {
                        x.getKey(),
                        x.getValue()
                })
                .flatMap(x -> Stream.of(x))
                .toArray(String[]::new));
    }


    protected EnvironmentVariables withEnv(String... variables) {
        Ensure.requireNonNull(variables, "variables must be non-null");
        Ensure.require(variables.length >= 2, "variables must contain at least one element");
        Ensure.require(variables.length % 2 == 0, "variables must contain an even number of elements");

        EnvironmentVariables result = null;
        for (int i = 0; i < variables.length; i += 2) {
            String key = variables[i];
            if (!Objects.equals(App.ENV_CONFIG_FILE_PATH, key) && !Objects.equals(App.ENV_MODEL_FILE_PATH, key)) {
                key = key.startsWith(App.ENV_CONFIG_EXTENSION_PREFIX)
                        ? key
                        : String.format("%s%s", App.ENV_CONFIG_EXTENSION_PREFIX, key);
            }
            String value = variables[i + 1];
            result = result == null
                    ? new EnvironmentVariables(key, value)
                    : result.and(key, value);
        }
        return result;
    }
}
