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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import java.io.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;


/**
 * Run the bash script to generate cerltificates and save them in the dedicated directories accordingly.
 */

public class ScriptRunnerHelper {

    private static class ProcessReader implements Callable {

        private final InputStream inputStream;

        public ProcessReader(InputStream inputStream) {
            this.inputStream = inputStream;
        }


        @Override
        public Object call() throws Exception {
            return new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.toList());
        }
    }

    /**
     * Run the bash script to generate self-signed certificates.
     *
     * @param scriptPath Path to bash script.
     * @param outputDirectory Path to output directory, where the generated certs are being stored.
     */
    public static void runBashScript(String scriptPath, String outputDirectory) throws IOException, InterruptedException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");

        ProcessBuilder builder = new ProcessBuilder();
        if (isWindows) {
            builder.command("cmd", "/c", scriptPath, outputDirectory);
        }
        else {
            builder.command("sh", "-c", scriptPath, outputDirectory);
        }

        ExecutorService pool = Executors.newSingleThreadExecutor();

        try {
            Process process = builder.start();
            ProcessReader task = new ProcessReader(process.getInputStream());
            Future<List<String>> future = pool.submit(task);

            List<String> results = future.get();
            for (String res: results) {
                System.out.println(res);
            }

            int exitCode = process.waitFor();

            System.out.println("Exit code: " + exitCode);
        }
        catch (IOException | ExecutionException e) {
            e.printStackTrace();
        }
        finally {
            pool.shutdown();
        }
    }
}
