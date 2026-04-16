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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.modbus.provider.model;

/**
 * Define which word of a list of words is the most significant one. This varies between different modbus server
 * implementations.
 */
public enum MostSignificantWord {
    /**
     * The word at the lowest read address is the most significant.
     * Example: Addresses 42, 43, 44 are read, then the word at address 42 is the most significant.
     */
    LOW,
    /**
     * The word at the highest read address is the most significant.
     * Example: Addresses 42, 43, 44 are read, then the word at address 44 is the most significant.
     */
    HIGH
}
