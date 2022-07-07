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
 * Enum describing supported serialization formats and their corresponding
 * well-known file extensions
 */
public enum DataFormat {
    JSON(MediaType.JSON_UTF_8, "json"),
    XML(MediaType.XML_UTF_8, "xml"),
    RDF(MediaType.PLAIN_TEXT_UTF_8, "rdf", "xml", "ttl", "n3", "nt", "nq"),
    JSONLD(MediaType.JSON_UTF_8, "jsonld", "json-ld"),
    AML(MediaType.XML_UTF_8, "aml", "xml"),
    AASX(MediaType.ZIP, "aasx"),
    UANODESET(MediaType.XML_UTF_8, "xml");

    private final MediaType contentType;
    private final List<String> fileExtensions;

    /**
     * Find potential data formats for given file extension. Returned list is
     * sorted by number of supported file extensions, i.e. data types which only
     * support given file extension should appear first in list.
     *
     * @param fileExtension file extension
     * @return list of potential data formats that support the given file
     *         extension
     */
    public static List<DataFormat> forFileExtension(String fileExtension) {
        return Stream.of(DataFormat.values())
                .filter(x -> x.getFileExtensions().contains(fileExtension))
                .sorted(Comparator.comparingInt(x -> x.getFileExtensions().size()))
                .collect(Collectors.toList());
    }


    private DataFormat(MediaType contentType, String... fileExtensions) {
        this.contentType = contentType;
        this.fileExtensions = Arrays.asList(fileExtensions);
    }


    public MediaType getContentType() {
        return contentType;
    }


    public List<String> getFileExtensions() {
        return fileExtensions;
    }
}
