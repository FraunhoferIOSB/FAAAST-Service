/*
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
package de.fraunhofer.iosb.ilt.faaast.service.checks;

import com.puppycrawl.tools.checkstyle.FileStatefulCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Checkstyle check that verifies the presence of the Apache 2.0 license header in Java source files. It only validates
 * that the required Apache license block exists somewhere in
 * the file header region (not full structural header matching).
 */
@FileStatefulCheck
public class ApacheLicenseHeaderCheck extends AbstractCheck {

    private static final String MSG_MISSING_LICENSE = "license.check.failing";
    private static final String MSG_MISSING_RESOURCES = "license.resources.missing";
    private static final String MSG_UNKNOWN_CONTRIBUTOR = "license.unknown.contributor";
    private static final String MSG_MISSING_START_OF_COMMENT = "license.missing.start.of.block.comment";

    private static String[] cachedLicense = null;
    private static Set<String> cachedContributors = null;

    private Path contributorsFile;
    private Path licenseFile;

    @Override
    public void beginTree(DetailAST rootAST) {
        String[] sourceFileLines = getLines();

        int sourceFileLicenseStartIndex = 0;

        // Get license lines from source file
        if (cachedLicense == null) {
            if (!loadLicense()) {
                log(rootAST, MSG_MISSING_RESOURCES);
                return;
            }
        }

        if (cachedContributors == null) {
            if (!loadContributors()) {
                log(rootAST, MSG_MISSING_RESOURCES);
                return;
            }
        }

        // When does the apache license start?
        while (sourceFileLicenseStartIndex < sourceFileLines.length && !normalize(sourceFileLines[sourceFileLicenseStartIndex]).equals(normalize(cachedLicense[0]))) {
            sourceFileLicenseStartIndex++;
        }
        if (sourceFileLicenseStartIndex >= sourceFileLines.length) {
            log(rootAST, MSG_MISSING_LICENSE);
            return;
        }

        String[] preLicenseSourceLines = Arrays.stream(sourceFileLines, 1, sourceFileLicenseStartIndex).toArray(String[]::new);
        String[] sourceLicense = Arrays.stream(sourceFileLines, sourceFileLicenseStartIndex, sourceFileLines.length).toArray(String[]::new);

        if (!validateBlockCommentStarts(sourceFileLines[0])) {
            log(rootAST, MSG_MISSING_START_OF_COMMENT);
        }

        if (!validateCopyrightSection(preLicenseSourceLines)) {
            log(rootAST, MSG_UNKNOWN_CONTRIBUTOR);
        }

        if (!validateLicense(sourceLicense, cachedLicense)) {
            log(rootAST, MSG_MISSING_LICENSE);
        }
    }


    private boolean validateBlockCommentStarts(String firstLine) {
        return firstLine.trim().startsWith("/*");
    }


    private boolean validateCopyrightSection(String[] preLicenseSourceLines) {
        // At least one contributor's copyright must exist
        boolean foundContributor = false;

        String merged = String.join("", preLicenseSourceLines);

        merged = merged.replace(" *", "");

        String[] sourceContributors = merged.split("Copyright \\(c\\) \\d{4}(-\\d{4})?");

        for (String sourceContributor: sourceContributors) {
            // Splitting creates empty first element
            if (sourceContributor.trim().isEmpty()) {
                continue;
            }

            if (!cachedContributors.contains(normalize(sourceContributor))) {
                return false;
            }
            else {
                foundContributor = true;
            }
        }

        return foundContributor;
    }


    private boolean validateLicense(String[] sourceLicense, String[] license) {
        int i = 0;
        for (; i < license.length; i++) {
            if (!normalize(sourceLicense[i]).equals(normalize(license[i]))) {
                return false;
            }
        }
        // Add end-of-header check to not allow any additional text
        return i < sourceLicense.length
                && sourceLicense[i].trim().equals("*/");
    }


    private boolean loadContributors() {
        try (Stream<String> lines = Files.lines(contributorsFile)) {
            cachedContributors = lines.collect(Collectors.toSet());
        }
        catch (IOException e) {
            return false;
        }
        return true;
    }


    private boolean loadLicense() {
        try (Stream<String> licenseLines = Files.lines(licenseFile)) {
            cachedLicense = licenseLines.toArray(String[]::new);
        }
        catch (IOException e) {
            return false;
        }
        return true;
    }


    private String normalize(String line) {
        return line
                .replaceFirst("^\\s*\\*\\s?", "")
                .trim()
                .replaceAll("\\s+", " ");
    }


    @Override
    public int[] getDefaultTokens() {
        return new int[0];
    }


    @Override
    public int[] getAcceptableTokens() {
        return new int[0];
    }


    @Override
    public int[] getRequiredTokens() {
        return new int[0];
    }


    public void setContributorsFile(String path) {
        this.contributorsFile = Paths.get(path);
    }


    public void setLicenseFile(String path) {
        this.licenseFile = Paths.get(path);
    }
}
