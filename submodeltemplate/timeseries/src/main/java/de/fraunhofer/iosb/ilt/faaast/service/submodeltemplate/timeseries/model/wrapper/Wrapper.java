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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.model.wrapper;

import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;


/**
 * Support class allowing to define wrapping of java values to AAS SubmodelElements.
 *
 * @param <T> type of the java value to map
 * @param <A> type of the target AAS type
 */
public interface Wrapper<T, A extends SubmodelElement> {

    /**
     * Gets the target AAS type.
     *
     * @return the target AAS type
     */
    public Class<A> getAASType();


    /**
     * Converts the Java value to AAS.
     *
     * @return the AAS representation of the value
     */
    public List<A> toAAS();


    /**
     * Gets the underlying Java value.
     *
     * @return the underlying value
     */
    public T getValue();


    /**
     * Gets the underlying Java value.
     *
     * @param newValue the value to set
     */
    public void setValue(T newValue);


    /**
     * Checks if a given AAS element can be parsed to the Java type.
     *
     * @param aasValue the AAS element to check
     * @return true if can be parsed, false otherwise
     */
    public boolean canParse(A aasValue);


    /**
     * Parses a given AAS element.The internal value must be update accordingly.
     *
     * @param aasValue the AAS element to parse
     */
    public void parse(A aasValue);
}
