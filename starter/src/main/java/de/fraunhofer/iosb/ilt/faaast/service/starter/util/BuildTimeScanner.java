package de.fraunhofer.iosb.ilt.faaast.service.starter.util;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;

import java.io.File;
import java.io.PrintWriter;

public class BuildTimeScanner {
    private static final String SCAN_ROOT = "de.fraunhofer.iosb.ilt.faaast.service.request";
    private static final boolean INDENT_JSON = true;
    private static final String OUTPUT_FILENAME = "scanresult.json";
    private static final String OUTPUT_DIRECTORY = "D:\\Fraunhofer\\FAAAST-Service\\FAAAST-Service\\starter\\src\\main\\resources";

    public void scanFromRoot() {
        File jsonFile = new File(OUTPUT_DIRECTORY, OUTPUT_FILENAME);
        try (ScanResult scanResult = new ClassGraph()
                .acceptPackages(SCAN_ROOT)
                .enableAllInfo()
                .scan()) {
            String scanResultJson = scanResult.toJSON(INDENT_JSON ? 2 : 0);
            try (PrintWriter writer = new PrintWriter(jsonFile)) {
                writer.print(scanResultJson);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
