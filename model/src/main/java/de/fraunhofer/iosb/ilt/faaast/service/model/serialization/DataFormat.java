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
package de.fraunhofer.iosb.ilt.faaast.service.model.serialization;

import com.google.common.net.MediaType;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Enum describing supported serialization formats, their corresponding content type, well-known file extensions and
 * priority.
 */
public enum DataFormat {

    JSON(MediaType.JSON_UTF_8, false, 0, "json"),
    XML(MediaType.APPLICATION_XML_UTF_8, false, 1, "xml"),
    RDF(MediaType.create("text", "turtle"), false, 2, "rdf", "xml", "ttl", "n3", "nt", "nq"),
    JSONLD(MediaType.create("application", "ld+json"), false, 3, "jsonld", "json-ld"),
    AASX(MediaType.create("application", "asset-administration-shell-package+xml"), true, 4, "aasx");

    private final MediaType contentType;
    private final boolean canStoreFiles;
    private final List<String> fileExtensions;
    private final int priority;

    /**
     * Find potential data formats for given file extension. Returned list is sorted by number of supported file
     * extensions, i.e. data types which only support given file extension should appear first in list.
     *
     * @param fileExtension file extension
     * @return list of potential data formats that support the given file extension
     */
    public static List<DataFormat> forFileExtension(String fileExtension) {
        return Stream.of(DataFormat.values())
                .filter(x -> x.getFileExtensions().contains(fileExtension))
                .sorted(Comparator.<DataFormat> comparingInt(x -> x.getFileExtensions().size())
                        .thenComparing(Comparator.comparingInt(DataFormat::getPriority)))
                .collect(Collectors.toList());
    }


    /**
     * Find data format for given content type.
     *
     * @param contentType file extension
     * @return DataFormat for the given contentType
     * @throws IllegalArgumentException if the given contenType does not match any data format.
     */
    public static DataFormat forContentType(MediaType contentType) {
        return Stream.of(DataFormat.values())
                .filter(x -> x.getContentType().is(contentType))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(String.format("unsupported dataformat '%s'", contentType)));
    }


    private DataFormat(MediaType contentType, boolean canStoreFiles, int priority, String... fileExtensions) {
        this.contentType = contentType;
        this.canStoreFiles = canStoreFiles;
        this.priority = priority;
        this.fileExtensions = Arrays.asList(fileExtensions);
    }


    public MediaType getContentType() {
        return contentType;
    }


    public boolean getCanStoreFiles() {
        return canStoreFiles;
    }


    public List<String> getFileExtensions() {
        return fileExtensions;
    }


    public int getPriority() {
        return priority;
    }
}
