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

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import java.io.*;
import java.util.Scanner;


/**
 * Creates a classgraph once at build-time to be saved and loaded in a json file.
 */
public class BuildTimeScanner {
    private static final String SCAN_ROOT = "de.fraunhofer.iosb.ilt.faaast.service";
    private static final int INDENT_WIDTH = 2;
    private static final String OUTPUT_FILENAME = "classgraphScanResult.json";

    private BuildTimeScanner() {
        // intentionally empty
    }


    /**
     * Main entry for build-time scanning.
     *
     * @param args dir for outputting the json file.
     * @throws Exception
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            throw new RuntimeException("Please specify output directory");
        }
        String dir = args[0];
        File jsonFile = new File(dir, OUTPUT_FILENAME);
        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages(SCAN_ROOT)
                .enableAllInfo()
                .scan()) {
            String scanResultJson = scanResult.toJSON(INDENT_WIDTH);
            try (PrintWriter writer = new PrintWriter(jsonFile)) {
                writer.print(scanResultJson);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * loads the json string that can be used to create a new classgraph instance.
     *
     * @return json string of the scanned classgraph.
     */
    public static String loadScanResultString() {
        try (InputStream inputStream = BuildTimeScanner.class.getClassLoader().getResourceAsStream(OUTPUT_FILENAME);
                Scanner scanner = new Scanner(inputStream)) {
            StringBuilder jsonStringBuilder = new StringBuilder();

            while (scanner.hasNextLine()) {
                jsonStringBuilder.append(scanner.nextLine());
            }

            return jsonStringBuilder.toString();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
