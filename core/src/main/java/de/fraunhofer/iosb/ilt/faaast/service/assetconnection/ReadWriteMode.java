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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection;

/**
 * Represents different read/write modes for asset connections.
 */
public enum ReadWriteMode {
    NONE(false, false),
    READ(true, false),
    WRITE(false, true),
    READ_WRITE(true, true);

    private final boolean read;
    private final boolean write;

    private ReadWriteMode(boolean read, boolean write) {
        this.read = read;
        this.write = write;
    }


    /**
     * Returns if this mode supports reading.
     *
     * @return true if this mode supports reading, otherwise false.
     */
    public boolean supportsRead() {
        return read;
    }


    /**
     * Returns if this mode supports writing.
     *
     * @return true if this mode supports writing, otherwise false.
     */
    public boolean supportsWrite() {
        return write;
    }


    /**
     * Find a read/write mode from boolean properties.
     *
     * @param read if read is supported
     * @param write if write is supported
     * @return the enum representation with given read/write values, NONE if no matching enum element is found.
     */
    public static ReadWriteMode from(boolean read, boolean write) {
        for (var value: values()) {
            if (value.read == read && value.write == write) {
                return value;
            }
        }
        return NONE;
    }
}
