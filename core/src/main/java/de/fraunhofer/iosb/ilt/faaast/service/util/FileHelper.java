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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;


/**
 * Helper class for working with file, e.g. extracting filename or file extension.
 */
public class FileHelper {

    private static final String PATH_SEPARATOR = "/";
    private static final String MSG_FILE_NON_NULL = "file must be non-null";
    private static final String MSG_FILENAME_NON_NULL = "filename must be non-null";

    private FileHelper() {}


    /**
     * Gets the filename without file extension.
     *
     * @param file the file to get the filename from
     * @return the filename without file extension
     * @throws IllegalArgumentException if file is null
     */
    public static String getFilenameWithoutExtension(File file) {
        Ensure.requireNonNull(file, MSG_FILE_NON_NULL);
        return getFilenameWithoutExtension(file.getName());
    }


    /**
     * Gets the filename without file extension.
     *
     * @param filename the filename to get the file extension from
     * @return the filename without file extension
     * @throws IllegalArgumentException if filename is null
     */
    public static String getFilenameWithoutExtension(String filename) {
        Ensure.requireNonNull(filename, MSG_FILENAME_NON_NULL);
        return filename.replaceFirst("[.][^.]+$", "");
    }


    /**
     * Gets the file extension in form of ".xyz", i.e. including leading dot assuming the last occuring . separates
     * extension from filename. If filename does not contain a dot an empty string is returned.
     *
     * @param file the file to get the extension from
     * @return the file extension of the file in the form ".xyz", i.e. including leading dot
     * @throws IllegalArgumentException if file is null
     */
    public static String getFileExtensionWithSeparator(File file) {
        Ensure.requireNonNull(file, MSG_FILE_NON_NULL);
        return getFileExtensionWithSeparator(file.getName());
    }


    /**
     * Gets the file extension in form of ".xyz", i.e. including leading dot assuming the last occuring . separates
     * extension from filename. If filename does not contain a dot an empty string is returned.
     *
     * @param filename the filename to get the extension from
     * @return the file extension of the filename in the form ".xyz", i.e. including leading dot
     * @throws IllegalArgumentException if filename is null
     */
    public static String getFileExtensionWithSeparator(String filename) {
        Ensure.requireNonNull(filename, MSG_FILENAME_NON_NULL);
        int index = filename.lastIndexOf('.');
        if (index < 0 || index >= filename.length() - 1) {
            return "";
        }
        return filename.substring(index);
    }


    /**
     * Gets the file extension in form of "xyz", i.e. not including leading dot assuming the last occuring . separates
     * extension from filename. If filename does not contain a dot an empty string is returned.
     *
     * @param file the file to get the extension from
     * @return the file extension of the file in the form "xyz", i.e. not including leading dot
     * @throws IllegalArgumentException if file is null
     */
    public static String getFileExtensionWithoutSeparator(File file) {
        Ensure.requireNonNull(file, MSG_FILE_NON_NULL);
        return getFileExtensionWithoutSeparator(file.getName());
    }


    /**
     * Gets the file extension in form of "xyz", i.e. not including leading dot assuming the last occuring . separates
     * extension from filename. If filename does not contain a dot an empty string is returned.
     *
     * @param filename the filename to get the extension from
     * @return the file extension of the filename in the form "xyz", i.e. not including leading dot
     * @throws IllegalArgumentException if filename is null
     */
    public static String getFileExtensionWithoutSeparator(String filename) {
        Ensure.requireNonNull(filename, MSG_FILENAME_NON_NULL);
        int index = filename.lastIndexOf('.');
        if (index < 0 || index >= filename.length() - 1) {
            return "";
        }
        return filename.substring(index + 1);
    }


    /**
     * Extracts the filename from a given path.
     *
     * @param path the path to the file containing the filename
     * @return the filename
     */
    public static String getFilenameFromPath(String path) {
        Ensure.requireNonNull(path, "path must be non-null");
        try {
            return Paths.get(new URI(path)).getFileName().toString();
        }
        catch (URISyntaxException | IllegalArgumentException ex) {
            if (path.contains(PATH_SEPARATOR)) {
                return path.substring(path.lastIndexOf(PATH_SEPARATOR));
            }
            return path;
        }
    }
}
