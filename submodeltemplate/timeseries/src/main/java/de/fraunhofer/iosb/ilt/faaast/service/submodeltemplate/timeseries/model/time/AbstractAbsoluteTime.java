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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.time;

import java.util.OptionalLong;


/**
 * Abstract class implementing the AbsoluteTime interface. Contains the semanticID, datatype and methods to get the
 * start and end time.
 * Extending classes should set the startTimestampInEpochMillis and endTimestampInEpochMillis variables in the init
 * method, and should call the constructor of the abstract class in a parameterless constructor.
 */
public abstract class AbstractAbsoluteTime implements AbsoluteTime {

    protected final String semanticID;
    protected final String datatype;

    protected boolean isInitialized = false;
    protected long startTimestampInEpochMillis;
    protected long endTimestampInEpochMillis;

    public AbstractAbsoluteTime(String semanticID, String datatype) {
        this.semanticID = semanticID;
        this.datatype = datatype;
    }


    @Override
    public String getTimeSemanticID() {
        return this.semanticID;
    }


    @Override
    public String getDataValueType() {
        return this.datatype;
    }


    @Override
    public OptionalLong getStartAsEpochMillis() {
        if (this.isInitialized) {
            return OptionalLong.of(this.startTimestampInEpochMillis);
        }
        return OptionalLong.empty();
    }


    @Override
    public OptionalLong getEndAsEpochMillis() {
        if (this.isInitialized) {
            return OptionalLong.of(this.endTimestampInEpochMillis);
        }
        return OptionalLong.empty();
    }

}
