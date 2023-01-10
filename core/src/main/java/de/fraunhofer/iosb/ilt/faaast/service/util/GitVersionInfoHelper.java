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

import java.io.IOException;
import java.util.Properties;


/**
 * Helper class for handling git version information.
 */
public class GitVersionInfoHelper {

    public static final String GIT_FILENAME = "git.properties";
    public static final String PATH_GIT_BUILD_VERSION = "git.build.version";
    public static final String PATH_GIT_BUILD_TIME = "git.build.time";
    public static final String PATH_GIT_COMMIT_ID_ABBREV = "git.commit.id.abbrev";
    public static final String PATH_GIT_COMMIT_ID_DESCRIBE = "git.commit.id.describe";

    private GitVersionInfoHelper() {}


    /**
     * Gets the git version information from file with {@code GIT_FILENAME}.
     *
     * @return the git version information
     * @throws IOException if reading fails
     */
    public static Properties getGitVersionInfo() throws IOException {
        Properties result = new Properties();
        result.load(GitVersionInfoHelper.class.getClassLoader().getResourceAsStream(GIT_FILENAME));
        return result;
    }


    /**
     * Returns the build version from git version info at default file location.
     *
     * @return the build version, null if not set
     * @throws IOException if reading fails
     */
    public static String getBuildVersion() throws IOException {
        return getGitVersionInfo().getProperty(PATH_GIT_BUILD_VERSION);
    }


    /**
     * Returns the build time from git version info at default file location.
     *
     * @return the build time, null if not set
     * @throws IOException if reading fails
     */
    public static String getBuildTime() throws IOException {
        return getGitVersionInfo().getProperty(PATH_GIT_BUILD_TIME);
    }


    /**
     * Returns the commit ID describe from git version info at default file location.
     *
     * @return the commit ID describe, null if not set
     * @throws IOException if reading fails
     */
    public static String getCommitIdDescribe() throws IOException {
        return getGitVersionInfo().getProperty(PATH_GIT_COMMIT_ID_DESCRIBE);
    }


    /**
     * Returns the commit ID abbrev from git version info at default file location.
     *
     * @return the commit ID abbrev, null if not set
     * @throws IOException if reading fails
     */
    public static String getCommitIdAbbrev() throws IOException {
        return getGitVersionInfo().getProperty(PATH_GIT_COMMIT_ID_ABBREV);
    }
}
